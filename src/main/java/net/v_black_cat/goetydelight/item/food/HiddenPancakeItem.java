package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.entities.ally.IServant;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.v_black_cat.goetydelight.init.ModItems;

import java.util.UUID;

public class HiddenPancakeItem extends Item {
    public HiddenPancakeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player) {
            if (!level.isClientSide()) {
                boolean hasTERMINUS = SEHelper.hasResearch(player, ResearchList.TERMINUS);
                if (!hasTERMINUS) {
                    if (SEHelper.addResearch(player, ResearchList.TERMINUS)) {
                        if (!SEHelper.hasResearch(player, ResearchList.BURIED)) {
                            SEHelper.addResearch(player, ResearchList.BURIED);
                        }

                        if (player instanceof ServerPlayer serverPlayer) {

                            AdvancementHolder advancementHolder = serverPlayer.getServer().getAdvancements().get(
                                    ResourceLocation.parse("goety:goety/read_terminus_scroll")
                            );
                            if (advancementHolder != null) {
                                serverPlayer.getAdvancements().award(advancementHolder, "terminus_scroll");
                            }
                        }
                        player.displayClientMessage(Component.translatable("info.goety.research.terminus"), true);

                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        return stack;
                    }
                } else {
                    if (player instanceof ServerPlayer serverPlayer) {

                        AdvancementHolder advancementHolder = serverPlayer.getServer().getAdvancements().get(
                                ResourceLocation.parse("goety:goety/read_terminus_scroll")
                        );
                        if (advancementHolder != null) {
                            serverPlayer.getAdvancements().award(advancementHolder, "terminus_scroll");
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
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        boolean isHiddenPancakeCopy = isIsHiddenPancakeCopy(entityId);
        if (player.getMainHandItem().getItem() == ModItems.HIDDEN_PANCAKE.get()) {
            player.stopRiding();
        }
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

                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack equipment = newEntity.getItemBySlot(slot);
                    if (!equipment.isEmpty()) {
                        newEntity.setItemSlot(slot, equipment.copy());
                    }
                }

                for (MobEffectInstance effect : newEntity.getActiveEffects()) {
                    newEntity.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    effect.getDuration(),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible()
                    ));
                }

                Float health = newEntity.getHealth();
                newEntity.setHealth(health);
                newEntity.addTag("HiddenPancake");
                newEntity.moveTo(target.getX(), target.getY(), target.getZ());

                IServant newServant = (IServant) newEntity;

                newServant.setTrueOwner(player);
                newServant.setOwnerId(player.getUUID());
                newServant.setLimitedLife(6000);

                target.level().addFreshEntity(newEntity);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                return super.onLeftClickEntity(stack, player, target);
            }
        }

        return super.onLeftClickEntity(stack, player, target);
    }

    public static boolean isIsHiddenPancakeCopy(ResourceLocation entityId) {
        return entityId.equals(ResourceLocation.parse("goety:redstone_monstrosity")) ||
                entityId.equals(ResourceLocation.parse("goety_cataclysm:netherite_monstrosity")) ||
                entityId.equals(ResourceLocation.parse("goety_cataclysm:ancient_remnant")) ||
                entityId.equals(ResourceLocation.parse("goetyawaken:ender_keeper_servant")) ||
                entityId.equals(ResourceLocation.parse("goetyawaken:mushroom_monstrosity"));
    }
}