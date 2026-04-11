package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.ally.IServant;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.Objects;

public class HiddenPancakeItem extends Item {
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
        if (player.getMainHandItem().getItem() == ModItems.HIDDEN_PANCAKE.get()){
            player.stopRiding();
        }
        // 服务端判定
        if (!player.level().isClientSide) {
            player.stopRiding();
            if (isHiddenPancakeCopy) {
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

                // 设置属性
                newServant.setTrueOwner(player);
                newServant.setOwnerId(player.getUUID());
                newServant.setLimitedLife(6000);

                // 添加实体到世界
                target.level().addFreshEntity(newEntity);
                if(!player.isCreative()){
                stack.shrink(1);}

                return super.onLeftClickEntity(stack, player, target);
            }
        }

        return super.onLeftClickEntity(stack, player, target);
    }

//    @Override
//    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
//        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
//        boolean isHiddenPancakeCopy = isIsHiddenPancakeCopy(entityId);
//        if (player.getMainHandItem().getItem() == ModItems.HIDDEN_PANCAKE.get()){
//            player.stopRiding();
//        }
//        // 服务端判定
//        if (!player.level().isClientSide) {
//            player.stopRiding();
//            if (isHiddenPancakeCopy) {
//                LivingEntity newEntity = (LivingEntity) target.getType().create(target.level());
//                if (newEntity == null) {
//                    System.out.println("Failed to create entity of type: " + target.getType());
//                    return InteractionResult.FAIL;
//                }
//
//                // 复制装备
//                for (EquipmentSlot slot : EquipmentSlot.values()) {
//                    ItemStack equipment = target.getItemBySlot(slot);
//                    if (!equipment.isEmpty()) {
//                        newEntity.setItemSlot(slot, equipment.copy());
//                    }
//                }
//
//                // 复制药水效果
//                for (MobEffectInstance effect : target.getActiveEffects()) {
//                    newEntity.addEffect(new MobEffectInstance(
//                            effect.getEffect(),
//                            effect.getDuration(),
//                            effect.getAmplifier(),
//                            effect.isAmbient(),
//                            effect.isVisible()
//                    ));
//                }
//
//                // 设置生命值和位置
//                Float healeh = target.getHealth();
//                newEntity.setHealth(healeh);
//                newEntity.moveTo(target.getX(), target.getY(), target.getZ());
//
//                IServant newServant = (IServant) newEntity;
//
//                // 设置属性
//                newServant.setTrueOwner(player);
//                newServant.setOwnerId(player.getUUID());
//                newServant.setLimitedLife(6000);
//
//                // 添加实体到世界
//                target.level().addFreshEntity(newEntity);
//                stack.shrink(1);
//
//                return InteractionResult.SUCCESS;
//            }
//        }
//
//        return InteractionResult.PASS;
//    }

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
