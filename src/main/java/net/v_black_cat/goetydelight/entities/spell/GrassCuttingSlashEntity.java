package net.v_black_cat.goetydelight.entities.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.spell.GrassCuttingSpell;

import java.util.UUID;

public class GrassCuttingSlashEntity extends Entity {
    public static final int FADE_TICKS = 5;
    public static final int TOTAL_LIFETIME_TICKS = 18;
    public static final double FLIGHT_SPEED = 0.72D;
    public static final int MAX_RADIUS = 7;

    private static final EntityDataAccessor<Integer> DATA_RADIUS =
            SynchedEntityData.defineId(GrassCuttingSlashEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SILK_TOUCH =
            SynchedEntityData.defineId(GrassCuttingSlashEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FORTUNE =
            SynchedEntityData.defineId(GrassCuttingSlashEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MAGNET =
            SynchedEntityData.defineId(GrassCuttingSlashEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerId;

    public GrassCuttingSlashEntity(EntityType<? extends GrassCuttingSlashEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setInvisible(true);
    }

    public GrassCuttingSlashEntity(Level level, LivingEntity caster, int radius,
                                   boolean silkTouch, int fortune, boolean magnet) {
        this(ModEntities.GRASS_CUTTING_SLASH.get(), level);
        this.ownerId = caster.getUUID();
        this.setRadius(radius);
        this.setSilkTouch(silkTouch);
        this.setFortune(fortune);
        this.setMagnet(magnet);
        Vec3 look = caster.getLookAngle();
        this.setYRot(caster.getYRot());
        this.setXRot(caster.getXRot() * 0.5F);
        this.setDeltaMovement(look.scale(FLIGHT_SPEED));
        this.setPos(caster.getX() + look.x, caster.getEyeY() - 0.25D + look.y,
                caster.getZ() + look.z);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.tickCount == 1) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.35F);
        }

        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        ServerLevel serverLevel = (ServerLevel) this.level();
        LivingEntity owner = this.ownerId == null
                ? null
                : serverLevel.getEntity(this.ownerId) instanceof LivingEntity living ? living : null;
        int cutRadius = Math.max(1, Math.min(this.getRadius(), 2));
        int harvested = GrassCuttingSpell.harvestArea(serverLevel, owner, this.blockPosition(),
                cutRadius, this.isSilkTouch(), this.getFortune(), this.isMagnet());
        if (harvested > 0 && this.tickCount % 2 == 0) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.7F, 0.9F);
        }
        this.spawnTrailParticles();

        if (this.tickCount >= TOTAL_LIFETIME_TICKS) {
            this.discard();
        }
    }

    private void spawnTrailParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel) || serverLevel.random.nextInt(2) != 0) {
            return;
        }

        double angle = this.getYRot() * Mth.DEG_TO_RAD + Math.PI;
        double side = (serverLevel.random.nextDouble() - 0.5D) * (2.0D + this.getRadius() * 0.7D);
        double forward = (serverLevel.random.nextDouble() - 0.65D) * 1.2D;
        double offsetX = -Mth.sin((float) angle) * forward + Mth.cos((float) angle) * side;
        double offsetZ = Mth.cos((float) angle) * forward + Mth.sin((float) angle) * side;
        double y = this.getY() + (serverLevel.random.nextDouble() - 0.5D) * 1.25D;

        serverLevel.sendParticles(ParticleTypes.END_ROD,
                this.getX() + offsetX, y, this.getZ() + offsetZ,
                1, 0.01D, 0.02D, 0.01D, 0.01D);
        if (serverLevel.random.nextInt(3) == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    this.getX() + offsetX, y, this.getZ() + offsetZ,
                    1, 0.02D, 0.03D, 0.02D, 0.01D);
        }
    }

    public int getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(int radius) {
        this.entityData.set(DATA_RADIUS, Mth.clamp(radius, 1, MAX_RADIUS));
    }

    public boolean isSilkTouch() {
        return this.entityData.get(DATA_SILK_TOUCH);
    }

    public void setSilkTouch(boolean silkTouch) {
        this.entityData.set(DATA_SILK_TOUCH, silkTouch);
    }

    public int getFortune() {
        return this.entityData.get(DATA_FORTUNE);
    }

    public void setFortune(int fortune) {
        this.entityData.set(DATA_FORTUNE, Mth.clamp(fortune, 0, 10));
    }

    public boolean isMagnet() {
        return this.entityData.get(DATA_MAGNET);
    }

    public void setMagnet(boolean magnet) {
        this.entityData.set(DATA_MAGNET, magnet);
    }

    public float animationTime(float partialTick) {
        return Math.max(0.0F, this.tickCount + partialTick - 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RADIUS, 2);
        this.entityData.define(DATA_SILK_TOUCH, false);
        this.entityData.define(DATA_FORTUNE, 0);
        this.entityData.define(DATA_MAGNET, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean save(CompoundTag tag) {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0D * 128.0D;
    }
}
