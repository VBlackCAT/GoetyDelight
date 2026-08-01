package net.v_black_cat.goetydelight.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;

/**
 * 1.21.1 移植版：
 * 1) 用娃娃物品右键实体，让娃娃骑乘到目标身上（最多 5 层，对应 1.20.1 DollClickEntityEvent）；
 * 2) 用打火石右键特定定制娃娃触发特效彩蛋（对应 1.20.1 VisualHandler）。
 */
@EventBusSubscriber(modid = GoetyDelight.MODID)
public class DollInteractEventHandler {
    private static final int MAX_RIDING_LAYERS = 5;

    private static final ResourceLocation DOLL_ENTITY_ID = ResourceLocation.fromNamespaceAndPath("goetydelight", "doll_entity");
    private static final ResourceLocation FLAME_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("goetydelight", "volumetric_flame");

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        Player player = event.getEntity();
        ItemStack mainHandItem = event.getItemStack();

        if (mainHandItem.is(ModItems.DOLL_ITEM.get())) {
            handleDollRiding(event, player, mainHandItem, target);
            return;
        }

        if (mainHandItem.is(Items.FLINT_AND_STEEL)) {
            handleFlintAndSteel(event, player, mainHandItem, target);
        }
    }

    // ========== 娃娃骑乘 ==========
    private static void handleDollRiding(PlayerInteractEvent.EntityInteract event, Player player,
                                         ItemStack mainHandItem, Entity target) {
        if (getRidingLayers(target) >= MAX_RIDING_LAYERS) {
            return;
        }

        DollEntity dollEntity = DollEntityItem.getDollEntity(player.level(), mainHandItem);
        dollEntity.setPos(target.getX(), target.getY() + 1.5, target.getZ());
        dollEntity.setYRot(player.getYRot() - 180);
        if (dollEntity.startRiding(target)) {
            if (!player.level().isClientSide) {
                player.level().addFreshEntity(dollEntity);
            }
            target.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                mainHandItem.shrink(1);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static int getRidingLayers(Entity entity) {
        int layers = 1;
        Entity current = entity;
        while (current.getVehicle() != null) {
            current = current.getVehicle();
            layers++;
        }
        return layers;
    }

    // ========== 打火石点娃娃特效 ==========
    private static void handleFlintAndSteel(PlayerInteractEvent.EntityInteract event, Player player,
                                            ItemStack flintStack, Entity target) {
        if (event.getLevel().isClientSide) {
            return;
        }

        ResourceLocation targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (!DOLL_ENTITY_ID.equals(targetId)) {
            return;
        }

        CompoundTag entityNbt = new CompoundTag();
        target.saveWithoutId(entityNbt);

        if ("doll_p7".equals(entityNbt.getString("custom_doll_id"))) {
            try {
                CompoundTag data = TagParser.parseTag(
                        "{Color:[0.12f,0.0f,0.0f],CoreColor:[0.55f,0.02f,0.0f],TipColor:[0.22f,0.0f,0.0f],SmokeColor:[0.0f,0.0f,0.0f],Intensity:0.65f}"
                );
                if (EntityVisualEffectSystem.addEffect(target, FLAME_EFFECT_ID, 0, data)) {
                    consumeFlint(event, player, flintStack);
                }
            } catch (CommandSyntaxException e) {
                GoetyDelight.LOGGER.warn("Failed to parse flame effect data for doll_p7", e);
            }
        } else if ("doll_vblackcat".equals(entityNbt.getString("custom_doll_id"))) {
            if (target.level().random.nextBoolean()) {
                int duration = 36000;
                ResourceLocation[] effectIds = {
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "doom_corona"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "abyssal_rift_eye"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "holy_judgement_halo"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "astral_crown"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "blood_moon_backwheel"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "causal_chains"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "inverted_cross_mark"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "depth_refraction_pressure"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "red_eye_flash"),
                        ResourceLocation.fromNamespaceAndPath("goetydelight", "volumetric_flame")
                };

                CompoundTag flameData;
                try {
                    flameData = TagParser.parseTag(
                            "{Color:[0.0f,0.28f,0.68f],CoreColor:[0.06f,0.47f,0.91f],TipColor:[0.23f,0.62f,1.0f],SmokeColor:[0.003f,0.012f,0.024f],Intensity:0.70f}"
                    );
                } catch (CommandSyntaxException e) {
                    flameData = new CompoundTag();
                }

                for (Entity nearby : target.level().getEntities(target, target.getBoundingBox().inflate(1.5, 1.5, 1.5))) {
                    for (ResourceLocation effectId : effectIds) {
                        CompoundTag data = effectId.equals(FLAME_EFFECT_ID) ? flameData.copy() : new CompoundTag();
                        EntityVisualEffectSystem.addEffect(nearby, effectId, duration, data);
                    }
                }
            } else {
                for (Entity nearby : target.level().getEntities(target, target.getBoundingBox().inflate(1.5, 1.5, 1.5))) {
                    var effects = EntityVisualEffectSystem.getEffects(nearby);
                    if (effects != null) {
                        effects.clear();
                        EntityVisualEffectSystem.sync(nearby);
                    }
                }
            }
            consumeFlint(event, player, flintStack);
        }
    }

    private static void consumeFlint(PlayerInteractEvent.EntityInteract event, Player player, ItemStack flintStack) {
        if (!player.isCreative()) {
            EquipmentSlot slot = event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            flintStack.hurtAndBreak(1, player, slot);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
