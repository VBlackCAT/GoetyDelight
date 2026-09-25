package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.SyncEntityVisualEffectsPacket;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.WeakHashMap;


@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public final class EntityVisualEffectSystem {


    public static final Capability<EntityVisualEffects> ENTITY_VISUAL_EFFECTS =
            CapabilityManager.get(new CapabilityToken<>() {});


    private static final ResourceLocation CAPABILITY_ID =
            new ResourceLocation(
                    GoetyDelight.MODID,
                    "entity_visual_effects"
            );

    private static final PriorityQueue<ScheduledEffects> EXPIRATION_QUEUE = new PriorityQueue<>(
            Comparator.comparingLong(ScheduledEffects::expiresAt)
                    .thenComparingLong(ScheduledEffects::sequence)
    );
    private static final Map<Entity, ScheduledEffects> SCHEDULED_BY_ENTITY = new WeakHashMap<>();
    private static long scheduleSequence;


    private EntityVisualEffectSystem() {
    }



    public static boolean addEffect(
            Entity entity,
            ResourceLocation effectId,
            int durationTicks
    ) {
        return addEffect(
                entity,
                effectId,
                durationTicks,
                new CompoundTag()
        );
    }



    public static boolean addEffect(
            Entity entity,
            ResourceKey<EntityVisualEffectType> effectKey,
            int durationTicks
    ) {
        return addEffect(
                entity,
                effectKey.location(),
                durationTicks,
                new CompoundTag()
        );
    }



    public static boolean addEffect(
            Entity entity,
            ResourceKey<EntityVisualEffectType> effectKey,
            int durationTicks,
            CompoundTag data
    ) {

        return addEffect(
                entity,
                effectKey.location(),
                durationTicks,
                data
        );
    }



    public static boolean addEffect(
            Entity entity,
            ResourceLocation effectId,
            int durationTicks,
            CompoundTag data
    ) {


        EntityVisualEffectType type =
                GDVisualEffects.get(effectId);


        if (entity.level().isClientSide || type == null) {
            return false;
        }



        EntityVisualEffects effects =
                entity.getCapability(
                        ENTITY_VISUAL_EFFECTS
                ).resolve().orElse(null);



        if (effects == null) {
            return false;
        }



        CompoundTag effectData =
                data.copy();

        long gameTime = entity.level().getGameTime();


        if (!effectData.contains("StartGameTime")) {

            effectData.putLong(
                    "StartGameTime",
                    gameTime
            );
        }

        // 地狱火焰在施加时锁定世界位置：黑烟外圈不会跟着玩家移动。
        if (usesWorldAnchor(effectId)
                && !(effectData.contains("AnchorX")
                && effectData.contains("AnchorY")
                && effectData.contains("AnchorZ"))) {
            effectData.putDouble("AnchorX", entity.getX());
            effectData.putDouble("AnchorY", entity.getY() + entity.getBbHeight() * 0.52D);
            effectData.putDouble("AnchorZ", entity.getZ());
        }




        effects.add(
                type,
                effectId,
                durationTicks,
                effectData,
                gameTime
        );

        schedule(entity, effects);

        sync(entity);


        return true;
    }

    private static boolean usesWorldAnchor(ResourceLocation effectId) {
        return effectId.equals(GDVisualEffects.MALEVOLENT_SHRINE_FIRE.getId())
                || effectId.equals(GDVisualEffects.MALEVOLENT_SHRINE_FIRE_LEGACY.getId())
                || effectId.equals(GDVisualEffects.MALEVOLENT_SHRINE_BLACK_DOMAIN.getId())
                || effectId.equals(GDVisualEffects.MALEVOLENT_SHRINE_BLACK_MIST.getId());
    }






    public static boolean removeEffect(
            Entity entity,
            ResourceLocation effectId
    ) {


        if (entity.level().isClientSide) {
            return false;
        }



        EntityVisualEffects effects =
                entity.getCapability(
                        ENTITY_VISUAL_EFFECTS
                ).resolve().orElse(null);



        if (effects == null) {
            return false;
        }



        boolean removed =
                effects.remove(effectId);



        if (removed) {
            schedule(entity, effects);
            sync(entity);
        }


        return removed;
    }






    public static boolean removeEffect(
            Entity entity,
            ResourceKey<EntityVisualEffectType> effectKey
    ) {

        return removeEffect(
                entity,
                effectKey.location()
        );
    }





    public static boolean hasEffect(
            Entity entity,
            ResourceLocation effectId
    ) {


        EntityVisualEffects effects =
                entity.getCapability(
                        ENTITY_VISUAL_EFFECTS
                ).resolve().orElse(null);



        return effects != null
                && effects.has(effectId);
    }





    public static void sync(Entity entity) {
        if (entity.level().isClientSide) return;

        if (entity instanceof IVisualEffectHolder holder) {
            EntityVisualEffects effects = holder.goetydelight$getVisualEffects();
            if (effects == null) return;

            SyncEntityVisualEffectsPacket packet = new SyncEntityVisualEffectsPacket(
                    entity.getId(),
                    effects.serializeNBTForSync(entity.level().getGameTime())
            );
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    packet
            );
        }
    }






    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        EntityVisualEffectsProvider provider = new EntityVisualEffectsProvider(event.getObject());
        event.addCapability(CAPABILITY_ID, provider);
        event.addListener(provider::invalidate);
    }






    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        long gameTime = event.getServer().overworld().getGameTime();
        while (!EXPIRATION_QUEUE.isEmpty()) {
            ScheduledEffects scheduled = EXPIRATION_QUEUE.peek();
            if (scheduled.expiresAt() > gameTime) {
                break;
            }

            EXPIRATION_QUEUE.poll();
            Entity entity = scheduled.entity();
            if (entity == null) {
                continue;
            }
            if (SCHEDULED_BY_ENTITY.get(entity) != scheduled) {
                continue;
            }
            SCHEDULED_BY_ENTITY.remove(entity);

            if (entity.isRemoved() || entity.level().isClientSide) {
                continue;
            }

            EntityVisualEffects effects = getEffects(entity);
            if (effects != scheduled.effects()
                    || effects == null
                    || effects.revision() != scheduled.revision()) {
                continue;
            }

            if (effects.tick(gameTime)) {
                sync(entity);
            }

            schedule(entity, effects);
        }
    }

    static void schedule(Entity entity, EntityVisualEffects effects) {
        if (entity.level().isClientSide || effects == null || effects.isEmpty()) {
            return;
        }

        long expiresAt = effects.nextExpiration();
        if (expiresAt == Long.MAX_VALUE) {
            return;
        }

        ScheduledEffects scheduled = new ScheduledEffects(
                new WeakReference<>(entity),
                effects,
                effects.revision(),
                expiresAt,
                scheduleSequence++
        );
        EXPIRATION_QUEUE.add(scheduled);
        SCHEDULED_BY_ENTITY.put(entity, scheduled);
        compactScheduleQueueIfNeeded();
    }

    private static void compactScheduleQueueIfNeeded() {
        if (EXPIRATION_QUEUE.size() <= SCHEDULED_BY_ENTITY.size() * 2 + 64) {
            return;
        }

        EXPIRATION_QUEUE.clear();
        EXPIRATION_QUEUE.addAll(SCHEDULED_BY_ENTITY.values());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        EXPIRATION_QUEUE.clear();
        SCHEDULED_BY_ENTITY.clear();
        scheduleSequence = 0L;
    }

    private static EntityVisualEffects getEffects(Entity entity) {
        if (entity instanceof IVisualEffectHolder holder) {
            EntityVisualEffects effects = holder.goetydelight$getVisualEffects();
            if (effects != null) {
                return effects;
            }
        }
        return entity.getCapability(ENTITY_VISUAL_EFFECTS).resolve().orElse(null);
    }







    @SubscribeEvent
    public static void onStartTracking(
            PlayerEvent.StartTracking event
    ) {


        if (event.getEntity() instanceof ServerPlayer player) {

            sendToPlayer(
                    event.getTarget(),
                    player
            );
        }
    }







    @SubscribeEvent
    public static void onPlayerLoggedIn(
            PlayerEvent.PlayerLoggedInEvent event
    ) {


        if (event.getEntity() instanceof ServerPlayer player) {

            sendTrackedEffectsTo(player);
        }
    }







    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) return;

        EntityVisualEffects oldEffects = getEffects(event.getOriginal());
        EntityVisualEffects newEffects = getEffects(event.getEntity());

        if (oldEffects != null && newEffects != null) {
            long gameTime = event.getEntity().level().getGameTime();
            newEffects.deserializeNBT(oldEffects.serializeNBT(gameTime), gameTime);
            schedule(event.getEntity(), newEffects);
        }
    }






    @SubscribeEvent
    public static void onEntityJoinLevel(
            EntityJoinLevelEvent event
    ) {


        if (
                !event.getLevel().isClientSide
                        && event.getEntity() instanceof ServerPlayer player
        ) {

            sendTrackedEffectsTo(player);

        }
    }







    private static void sendTrackedEffectsTo(
            ServerPlayer player
    ) {


        for (Entity entity :
                player.serverLevel().getAllEntities()) {


            sendToPlayer(
                    entity,
                    player
            );
        }
    }







    private static void sendToPlayer(
            Entity entity,
            ServerPlayer player
    ) {


        EntityVisualEffects effects =
                entity.getCapability(
                        ENTITY_VISUAL_EFFECTS
                ).resolve().orElse(null);



        if (effects == null || effects.isEmpty()) {
            return;
        }



        NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new SyncEntityVisualEffectsPacket(
                    entity.getId(),
                    effects.serializeNBTForSync(entity.level().getGameTime())
            )
        );
    }

    private record ScheduledEffects(
            WeakReference<Entity> entityReference,
            EntityVisualEffects effects,
            long revision,
            long expiresAt,
            long sequence
    ) {
        private Entity entity() {
            return entityReference.get();
        }
    }







    @Mod.EventBusSubscriber(
            modid = GoetyDelight.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static final class ModEvents {


        private ModEvents() {
        }



        @SubscribeEvent
        public static void registerCapabilities(
                RegisterCapabilitiesEvent event
        ) {

            event.register(
                    EntityVisualEffects.class
            );
        }
    }
}
