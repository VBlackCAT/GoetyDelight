package net.v_black_cat.goetydelight.entities.spell;

import com.Polarice3.Goety.common.entities.projectiles.SlashProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.spell.GrassCuttingSpell;

public class GrassCuttingSlashEntity extends SlashProjectile {
    public static final int FADE_TICKS = 5;

    private static final EntityDataAccessor<Float> DATA_MAX_RADIUS =
            SynchedEntityData.defineId(GrassCuttingSlashEntity.class, EntityDataSerializers.FLOAT);

    private boolean silkTouch;
    private int fortune;
    private boolean magnet;

    public GrassCuttingSlashEntity(EntityType<? extends SlashProjectile> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.setDamage(0.0F);
    }

    public GrassCuttingSlashEntity(Level level, LivingEntity caster, float speed, float maxRadius,
                                   int lifeSpan, boolean silkTouch, int fortune, boolean magnet) {
        this(ModEntities.GRASS_CUTTING_SLASH.get(), level);
        this.setOwner(caster);
        this.setMaxRadius(Math.max(0.75F, maxRadius));
        this.setRadius(Math.min(0.65F, this.getMaxRadius()));
        this.setMaxLifeSpan(Math.max(4, lifeSpan));
        this.setSilkTouch(silkTouch);
        this.setFortune(fortune);
        this.setMagnet(magnet);

        Vec3 look = caster.getLookAngle();
        this.setPos(caster.getX() + look.x, caster.getEyeY() - 0.25D + look.y,
                caster.getZ() + look.z);
        this.setYRot(caster.getYRot());
        this.setXRot(caster.getXRot() * 0.5F);
        this.slash(look, speed);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            this.cutGrassAlongPath();
        }

        if (this.tickCount == 1 && !this.level().isClientSide()) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.35F);
        }

        super.tick();
        if (!this.level().isClientSide() && !this.isRemoved()) {
            float progress = Mth.clamp(this.tickCount / (float) this.getMaxLifeSpan(), 0.0F, 1.0F);
            float eased = progress * progress * (3.0F - 2.0F * progress);
            this.setRadius(0.65F + (this.getMaxRadius() - 0.65F) * eased);
        }
    }

    private void cutGrassAlongPath() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
        int cutRadius = Mth.clamp(Mth.ceil(this.getRadius()), 1, 3);
        int harvested = GrassCuttingSpell.harvestArea(serverLevel, owner, this.blockPosition(),
                cutRadius, this.isSilkTouch(), this.getFortune(), this.isMagnet());
        if (harvested > 0 && this.tickCount % 2 == 0) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.65F, 0.9F);
        }
    }

    @Override
    public void damageEntity(Entity entity) {
        // 斩击只切割植物，不伤害生物。
    }

    @Override
    public void spawnParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.tickCount % 2 != 0) {
            return;
        }

        float width = this.getRadius();
        int count = Math.max(2, Mth.ceil(width * 2.0F));
        for (int i = 0; i < count; ++i) {
            double side = (serverLevel.random.nextDouble() - 0.5D) * width * 2.0D;
            double angle = this.getYRot() * Mth.DEG_TO_RAD + Math.PI * 0.5D;
            double x = this.getX() + Math.cos(angle) * side;
            double z = this.getZ() + Math.sin(angle) * side;
            double y = this.getY() + (serverLevel.random.nextDouble() - 0.5D) * 0.7D;
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z,
                    1, 0.01D, 0.02D, 0.01D, 0.01D);
        }
        if (serverLevel.random.nextInt(3) == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY(), this.getZ(),
                    1, width * 0.25D, 0.1D, width * 0.25D, 0.01D);
        }
    }

    public boolean isSilkTouch() {
        return this.silkTouch;
    }

    public void setSilkTouch(boolean silkTouch) {
        this.silkTouch = silkTouch;
    }

    public int getFortune() {
        return this.fortune;
    }

    public void setFortune(int fortune) {
        this.fortune = Mth.clamp(fortune, 0, 10);
    }

    public boolean isMagnet() {
        return this.magnet;
    }

    public void setMagnet(boolean magnet) {
        this.magnet = magnet;
    }

    public float animationTime(float partialTick) {
        return Math.max(0.0F, this.tickCount + partialTick - 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SlashProjectile.DATA_RADIUS, 0.5F);
        this.entityData.define(DATA_MAX_RADIUS, 3.0F);
    }

    @Override
    public float getMaxRadius() {
        return this.entityData.get(DATA_MAX_RADIUS);
    }

    @Override
    public void setMaxRadius(float radius) {
        this.maxRadius = radius;
        this.entityData.set(DATA_MAX_RADIUS, radius);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SilkTouch")) {
            this.setSilkTouch(tag.getBoolean("SilkTouch"));
        }
        if (tag.contains("Fortune")) {
            this.setFortune(tag.getInt("Fortune"));
        }
        if (tag.contains("Magnet")) {
            this.setMagnet(tag.getBoolean("Magnet"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SilkTouch", this.isSilkTouch());
        tag.putInt("Fortune", this.getFortune());
        tag.putBoolean("Magnet", this.isMagnet());
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }
}
