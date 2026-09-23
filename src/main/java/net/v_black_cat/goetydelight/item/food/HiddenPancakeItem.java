package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.entities.ally.IServant;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.api.ITimedEntityManager;
import net.v_black_cat.goetydelight.compat.goetyrevelation.ApocalyptiumData;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.util.TimedEntityManager;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class HiddenPancakeItem extends Item {

    /** 仆从寿命（tick），与 setLimitedLife 保持一致 */
    private static final int SERVANT_LIFETIME = 6000;

    /** 全局计时管理器 */
    private static final TimedEntityManager MANAGER = TimedEntityManager.getInstance();

    public HiddenPancakeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player) {
            // 判断是否为服务端
            if (!level.isClientSide()) {
                // 检查是否拥有 TERMINUS 研究
                boolean hasTERMINUS = SEHelper.hasResearch(player, ResearchList.TERMINUS);
                if (!hasTERMINUS) {
                    // 添加 TERMINUS 研究
                    if (SEHelper.addResearch(player, ResearchList.TERMINUS)) {
                        // 检查并添加 BURIED 研究
                        if (!SEHelper.hasResearch(player, ResearchList.BURIED)) {
                            SEHelper.addResearch(player, ResearchList.BURIED);
                        }

                        // 触发成就
                        if (player instanceof ServerPlayer serverPlayer) {
                            Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("goety:goety/read_terminus_scroll"));
                            if (advancement != null) {
                                serverPlayer.getAdvancements().award(advancement, "terminus_scroll");
                            }
                        }
                        // 发送成功消息（服务端通知客户端）
                        player.displayClientMessage(Component.translatable("info.goety.research.terminus"), true);

                        // 消耗物品
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        return stack;
                    }
                } else {
                    // 已拥有研究，触发成就并发送提示
                    if (player instanceof ServerPlayer serverPlayer) {
                        Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("goety:goety/read_terminus_scroll"));
                        if (advancement != null) {
                            serverPlayer.getAdvancements().award(advancement, "terminus_scroll");
                        }
                    }
                    player.displayClientMessage(Component.translatable("info.goety.research.already"), true);
                }
            }

            if (player.getAbilities().instabuild) {
                return result;
            }
        }
        return result;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        boolean isHiddenPancakeCopy = isIsHiddenPancakeCopy(entityId);
        if (player.getMainHandItem().getItem() == ModItems.HIDDEN_PANCAKE.get()) {
            player.stopRiding();
        }
        // 服务端判定
        if (!player.level().isClientSide) {
            player.stopRiding();
            if (isHiddenPancakeCopy) {
                UUID ownerUUID = null;
                if (target instanceof OwnableEntity ownableEntity) {
                    ownerUUID = ownableEntity.getOwnerUUID();
                } else if (target instanceof IOwned iOwned) {
                    if (iOwned.getTrueOwner() != null) {
                        ownerUUID = iOwned.getTrueOwner().getUUID();
                    }
                }

                if (ownerUUID != null && !ownerUUID.equals(player.getUUID())) {
                    return super.onLeftClickEntity(stack, player, target);
                }

                LivingEntity newEntity = (LivingEntity) target.getType().create(target.level());
                if (newEntity == null) {
                    System.out.println("Failed to create entity of type: " + target.getType());
                    return super.onLeftClickEntity(stack, player, target);
                }

                // 复制装备
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack equipment = newEntity.getItemBySlot(slot);
                    if (!equipment.isEmpty()) {
                        newEntity.setItemSlot(slot, equipment.copy());
                    }
                }

                // 复制药水效果
                for (MobEffectInstance effect : newEntity.getActiveEffects()) {
                    newEntity.addEffect(new MobEffectInstance(
                            effect.getEffect(),
                            effect.getDuration(),
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible()
                    ));
                }

                // 设置生命值和位置
                Float health = newEntity.getHealth();
                newEntity.setHealth(health);
                newEntity.addTag("HiddenPancake");
                newEntity.moveTo(target.getX(), target.getY(), target.getZ());

                IServant newServant = (IServant) newEntity;

                // 设置属性（原有寿命设置保持不变）
                newServant.setTrueOwner(player);
                newServant.setOwnerId(player.getUUID());
                newServant.setLimitedLife(SERVANT_LIFETIME);

                // 添加实体到世界
                target.level().addFreshEntity(newEntity);

                // ★ 额外一层保险：用计时管理器兜底注册
                if (target.level() instanceof ServerLevel serverLevel) {
                    UUID servantUUID = MANAGER.track(
                            player,
                            newEntity,
                            SERVANT_LIFETIME,
                            ITimedEntityManager.Category.SERVANT,
                            entityId
                    );
                    // 补缓存到 ApocalyptiumData，保持与其他业务一致
                    ApocalyptiumData.get(serverLevel).cacheEntity(servantUUID, newEntity);
                }

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                return super.onLeftClickEntity(stack, player, target);
            }
        }

        return super.onLeftClickEntity(stack, player, target);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getTags().contains("HiddenPancake")) {
            event.getDrops().clear();
        }
    }

    public static boolean isIsHiddenPancakeCopy(ResourceLocation entityId) {
        boolean isHiddenPancakeCopy = false;
        if (entityId.equals(new ResourceLocation("goety:redstone_monstrosity")) ||
                entityId.equals(new ResourceLocation("goety_cataclysm:netherite_monstrosity")) ||
                entityId.equals(new ResourceLocation("goety_cataclysm:ancient_remnant")) ||
                entityId.equals(new ResourceLocation("goetyawaken:ender_keeper_servant")) ||
                entityId.equals(new ResourceLocation("goetyawaken:mushroom_monstrosity"))
        ){isHiddenPancakeCopy = true;}else {isHiddenPancakeCopy = false;}
        return isHiddenPancakeCopy;
    }

}