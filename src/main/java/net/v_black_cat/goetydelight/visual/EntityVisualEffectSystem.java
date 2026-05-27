package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.SyncEntityVisualEffectsPacket;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public final class EntityVisualEffectSystem {
    public static final Capability<EntityVisualEffects> ENTITY_VISUAL_EFFECTS =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(GoetyDelight.MODID, "entity_visual_effects");

    private EntityVisualEffectSystem() {
    }

    public static boolean addEffect(Entity entity, ResourceLocation effectId, int durationTicks) {
        return addEffect(entity, effectId, durationTicks, new CompoundTag());
    }

    public static boolean addEffect(Entity entity, ResourceKey<EntityVisualEffectType> effectKey, int durationTicks) {
        return addEffect(entity, effectKey.location(), durationTicks, new CompoundTag());
    }

    public static boolean addEffect(Entity entity, ResourceKey<EntityVisualEffectType> effectKey, int durationTicks, CompoundTag data) {
        return addEffect(entity, effectKey.location(), durationTicks, data);
    }

    public static boolean addEffect(Entity entity, ResourceLocation effectId, int durationTicks, CompoundTag data) {
        EntityVisualEffectType type = GDVisualEffects.get(effectId);
        if (entity.level().isClientSide || type == null) {
            return false;
        }

        return entity.getCapability(ENTITY_VISUAL_EFFECTS).map(effects -> {
            CompoundTag effectData = data.copy();
            if (!effectData.contains("StartGameTime")) {
                effectData.putLong("StartGameTime", entity.level().getGameTime());
            }
            effects.add(type, effectId, durationTicks, effectData);
            sync(entity);
            return true;
        }).orElse(false);
    }

    public static boolean removeEffect(Entity entity, ResourceLocation effectId) {
        if (entity.level().isClientSide) {
            return false;
        }

        return entity.getCapability(ENTITY_VISUAL_EFFECTS).map(effects -> {
            boolean removed = effects.remove(effectId);
            if (removed) {
                sync(entity);
            }
            return removed;
        }).orElse(false);
    }

    public static boolean removeEffect(Entity entity, ResourceKey<EntityVisualEffectType> effectKey) {
        return removeEffect(entity, effectKey.location());
    }

    public static boolean hasEffect(Entity entity, ResourceLocation effectId) {
        return entity.getCapability(ENTITY_VISUAL_EFFECTS)
                .map(effects -> effects.has(effectId))
                .orElse(false);
    }

    public static void sync(Entity entity) {
        if (entity.level().isClientSide) {
            return;
        }

        entity.getCapability(ENTITY_VISUAL_EFFECTS).ifPresent(effects -> {
            SyncEntityVisualEffectsPacket packet = new SyncEntityVisualEffectsPacket(entity.getId(), effects.serializeNBTForSync());
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
        });
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        EntityVisualEffectsProvider provider = new EntityVisualEffectsProvider();
        event.addCapability(CAPABILITY_ID, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (Entity entity : serverLevel.getAllEntities()) {
            entity.getCapability(ENTITY_VISUAL_EFFECTS).ifPresent(effects -> {
                if (effects.tick()) {
                    sync(entity);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendToPlayer(event.getTarget(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendTrackedEffectsTo(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(ENTITY_VISUAL_EFFECTS).ifPresent(oldEffects ->
                event.getEntity().getCapability(ENTITY_VISUAL_EFFECTS).ifPresent(newEffects ->
                        newEffects.deserializeNBT(oldEffects.serializeNBT())));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            sendTrackedEffectsTo(player);
        }
    }

    private static void sendTrackedEffectsTo(ServerPlayer player) {
        for (Entity entity : player.serverLevel().getAllEntities()) {
            sendToPlayer(entity, player);
        }
    }

    private static void sendToPlayer(Entity entity, ServerPlayer player) {
        entity.getCapability(ENTITY_VISUAL_EFFECTS).ifPresent(effects -> {
            if (!effects.isEmpty()) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncEntityVisualEffectsPacket(entity.getId(), effects.serializeNBTForSync())
                );
            }
        });
    }

    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(EntityVisualEffects.class);
        }
    }
}
