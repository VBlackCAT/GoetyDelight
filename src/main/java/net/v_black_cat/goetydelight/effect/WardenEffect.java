package net.v_black_cat.goetydelight.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static net.v_black_cat.goetydelight.GoetyDelight.LOGGER;


public class WardenEffect extends MobEffect {
    private final Map<UUID, VibrationSystem.Data> activeData = new HashMap<>();
    private final Map<UUID, VibrationSystem.User> activeUsers = new HashMap<>();
    private final Map<UUID, DynamicGameEventListener<VibrationSystem.Listener>> activeListeners = new HashMap<>();
    private final Map<UUID, VibrationTargetData> vibrationTargets = new HashMap<>();

    public WardenEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x000000);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        UUID id = entity.getUUID();

        
        updateVibrationTargets(entity);

        VibrationSystem.Data data = activeData.computeIfAbsent(id, k -> new VibrationSystem.Data());
        VibrationSystem.User user = activeUsers.computeIfAbsent(id, k -> new EffectVibrationUser(entity));

        DynamicGameEventListener<VibrationSystem.Listener> listener = activeListeners.computeIfAbsent(id, k -> {
            DynamicGameEventListener<VibrationSystem.Listener> newListener = new DynamicGameEventListener<>(
                    new VibrationSystem.Listener(new VibrationSystem() {
                        @Override
                        public Data getVibrationData() {
                            return data;
                        }

                        @Override
                        public User getVibrationUser() {
                            return user;
                        }
                    })
            );
            newListener.add(serverLevel);
            LOGGER.debug("[WardenEffect] Registered new vibration listener for {}", entity.getName().getString());
            return newListener;
        });

        VibrationSystem.Ticker.tick(serverLevel, data, user);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);

        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        UUID id = entity.getUUID();

        
        DynamicGameEventListener<VibrationSystem.Listener> listener = activeListeners.remove(id);
        if (listener != null) {
            listener.remove(serverLevel);
        }

        activeData.remove(id);
        activeUsers.remove(id);
        vibrationTargets.remove(id); 

        LOGGER.debug("[WardenEffect] Removed vibration listener for {}", entity.getName().getString());
    }

    
    private void updateVibrationTargets(LivingEntity entity) {
        UUID id = entity.getUUID();
        VibrationTargetData targetData = vibrationTargets.get(id);
        if (targetData != null) {
            targetData.remainingTicks--;
            if (targetData.remainingTicks <= 0) {
                vibrationTargets.remove(id);
                LOGGER.debug("[WardenEffect] Damage boost effect for {} against {} has ended",
                        entity.getName().getString(),
                        targetData.targetName != null ? targetData.targetName : "Unknown entity");
            }
        }
    }

    
    public static boolean hasDamageBoostAgainst(LivingEntity attacker, LivingEntity target) {
        if (attacker == null || target == null) return false;

        
        if (attacker.hasEffect(ModEffects.WARDEN.get())) {
            
            if (BuffUtil.hasBuff(target, ModBuffTypes.WARDEN_DETECTED.getId())) {
                return true;
            }
            
            
            WardenEffect effect = (WardenEffect) Objects.requireNonNull(attacker.getEffect(ModEffects.WARDEN.get())).getEffect();
            UUID attackerId = attacker.getUUID();
            UUID targetId = target.getUUID();

            VibrationTargetData targetData = effect.vibrationTargets.get(attackerId);
            if (targetData != null && targetData.targetId != null && targetData.targetId.equals(targetId)) {
                return targetData.remainingTicks > 0;
            }
        }
        return false;
    }

    
    public static float getDamageBoostMultiplier() {
        return 1.5f; 
    }

    
    private static class VibrationTargetData {
        public UUID targetId;
        public String targetName;
        public int remainingTicks;

        public VibrationTargetData(UUID targetId, String targetName, int durationTicks) {
            this.targetId = targetId;
            this.targetName = targetName;
            this.remainingTicks = durationTicks;
        }
    }

    private static class EffectVibrationUser implements VibrationSystem.User {
        private static final int LISTENER_RANGE = 16;
        private static final int DAMAGE_BOOST_DURATION = 100; 
        private final EntityPositionSource positionSource;
        private final LivingEntity owner;

        public EffectVibrationUser(LivingEntity owner) {
            this.owner = owner;
            this.positionSource = new EntityPositionSource(owner, owner.getEyeHeight());
        }

        @Override
        public int getListenerRadius() {
            return LISTENER_RANGE;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.WARDEN_CAN_LISTEN;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event, GameEvent.Context context) {
            if (context != null && context.sourceEntity() == owner) return false;
            return true;
        }

        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event,
                                       @Nullable Entity source, @Nullable Entity projectile, float distance) {
            String srcName = (source != null ? source.getName().getString() : "Environment");
            LOGGER.debug("[WardenEffect] {} sensed vibration event from {}", owner.getName().getString(), srcName);

            
            if (source instanceof LivingEntity target) {
                UUID ownerId = owner.getUUID();
                UUID targetId = target.getUUID();

                
                if (owner.hasEffect(ModEffects.WARDEN.get())) {
                    WardenEffect effect = (WardenEffect) owner.getEffect(ModEffects.WARDEN.get()).getEffect();

                    
                    VibrationTargetData targetData = new VibrationTargetData(targetId, target.getName().getString(), DAMAGE_BOOST_DURATION);
                    effect.vibrationTargets.put(ownerId, targetData);
                    
                    
                    BuffUtil.applyBuff(target, ModBuffTypes.WARDEN_DETECTED.getId(), 100, 0); 

                    LOGGER.debug("[WardenEffect] {} gained damage boost against {} for 5 seconds",
                            owner.getName().getString(), target.getName().getString());
                }
            }
            double dx = pos.getX() + level.random.nextDouble();
            double dy = pos.getY() + 1.0;
            double dz = pos.getZ() + level.random.nextDouble();
            level.sendParticles(ParticleTypes.SONIC_BOOM, dx, dy, dz, 1, 0, 0, 0, 0);
            

        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return false;
        }

        @Override
        public int calculateTravelTimeInTicks(float distance) {
            return Mth.floor(distance);
        }

        @Override
        public boolean isValidVibration(GameEvent event, GameEvent.Context context) {
            return true;
        }

        @Override
        public void onDataChanged() {
            
        }
    }


}