package net.v_black_cat.goetydelight.entities.spell;

import com.Polarice3.Goety.common.entities.projectiles.SpellEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.spell.MalevolentShrineSpell;
import net.v_black_cat.goetydelight.spell.MalevolentShrineTargets;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;
import net.v_black_cat.goetydelight.visual.EntityVisualEffects;
import net.v_black_cat.goetydelight.visual.GDVisualEffects;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 石油雾的领域实体。
 * <p>
 * 实体本身不可见、无碰撞、无重力，只负责三件事：
 * 1. 跟随施法者并每 tick 被 {@link net.v_black_cat.goetydelight.spell.MalevolentShrineSpell} 刷新；
 * 2. 定期给领域实体挂 {@link GDVisualEffects#MALEVOLENT_SHRINE_DOMAIN} 深度渲染效果；
 * 3. 定期对范围内的目标实体造成斩击，并给它们挂血红光芒效果。
 */
public class MalevolentShrineEntity extends SpellEntity {

    private static final int SLASH_INTERVAL_TICKS = 4;
    private static final int VISUAL_REFRESH_INTERVAL_TICKS = 2;
    private static final int FADE_OUT_TICKS = 20;
    private static final int TARGET_GLOW_TICKS = 16;

    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(MalevolentShrineEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(MalevolentShrineEntity.class, EntityDataSerializers.FLOAT);

    private int remainingTicks = 3;
    private boolean fading;
    private int fadeTicks = FADE_OUT_TICKS;

    public MalevolentShrineEntity(EntityType<? extends MalevolentShrineEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setInvisible(true);
    }

    public MalevolentShrineEntity(Level level, LivingEntity caster, float radius, float damage) {
        this(ModEntities.MALEVOLENT_SHRINE.get(), level);
        this.setOwner(caster);
        this.setRadius(radius);
        this.setDamage(damage);
        this.setPos(caster.getX(), caster.getY(), caster.getZ());
        this.refreshDuration();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SpellEntity.OWNER_UNIQUE_ID, Optional.empty());
        this.entityData.define(SpellEntity.OWNER_CLIENT_ID, -1);
        this.entityData.define(SpellEntity.TARGET_UNIQUE_ID, Optional.empty());
        this.entityData.define(SpellEntity.TARGET_CLIENT_ID, -1);
        this.entityData.define(SpellEntity.DATA_EXTRA_DAMAGE, 0.0F);
        this.entityData.define(DATA_RADIUS, 8.0F);
        this.entityData.define(DATA_DAMAGE, 3.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        LivingEntity owner = this.getOwner();

        if (!this.fading && --this.remainingTicks <= 0) {
            this.beginFade();
        }
        if (!this.fading && (owner == null || owner.isDeadOrDying())) {
            this.beginFade();
        }

        if (this.fading) {
            if (--this.fadeTicks <= 0) {
                this.discard();
                return;
            }
            this.refreshDomainVisual((float) this.fadeTicks / FADE_OUT_TICKS);
            return;
        }

        // 领域位置固定在展开点，不跟随玩家移动。
        if (this.tickCount % VISUAL_REFRESH_INTERVAL_TICKS == 0) {
            this.refreshDomainVisual(1.0F);
        }

        if (this.tickCount % SLASH_INTERVAL_TICKS == 0) {
            this.slashTargets(owner);
        }
    }

    private void refreshDomainVisual(float opacity) {
        EntityVisualEffects effects = this.getCapability(EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS)
                .resolve().orElse(null);
        if (effects == null) {
            return;
        }

        CompoundTag data = new CompoundTag();
        data.putFloat("Radius", this.getRadius());
        data.putFloat("Height", Math.max(4.0F, this.getRadius() * 0.9F));
        data.putFloat("Intensity", opacity);
        updateVisualEffect(effects, GDVisualEffects.MALEVOLENT_SHRINE_DOMAIN.getId(), data);

        CompoundTag slashData = new CompoundTag();
        slashData.putFloat("Radius", this.getRadius());
        slashData.putFloat("Height", Math.max(4.0F, this.getRadius() * 0.9F));
        slashData.putFloat("Intensity", (0.85F + this.getDamage() * 0.05F) * opacity);
        updateVisualEffect(effects, GDVisualEffects.MALEVOLENT_SHRINE_SLASH.getId(), slashData);

        EntityVisualEffectSystem.sync(this);
    }

    private void updateVisualEffect(EntityVisualEffects effects, ResourceLocation id, CompoundTag data) {
        ActiveEntityVisualEffect existing = effects.get(id);
        if (existing == null) {
            EntityVisualEffectSystem.addEffect(this, id, 0, data);
            return;
        }

        // 保留原始 StartGameTime，让雾场/斩击的“逐渐浮现”不会被刷新打断。
        CompoundTag updated = data.copy();
        if (existing.data().contains("StartGameTime")) {
            updated.putLong("StartGameTime", existing.data().getLong("StartGameTime"));
        }
        existing.setData(updated);
    }
    private void slashTargets(LivingEntity owner) {
        AABB area = this.getBoundingBox().inflate(this.getRadius());
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area);
        DamageSource source = owner.damageSources().indirectMagic(owner, owner);

        for (LivingEntity target : targets) {
            if (!MalevolentShrineTargets.isTarget(target, owner)) {
                continue;
            }

            if (target.hurt(source, this.getDamage())) {
                target.invulnerableTime = 0;
                CompoundTag glowData = new CompoundTag();
                glowData.putFloat("Radius", Math.max(0.9F, target.getBbWidth() * 1.6F));
                glowData.putFloat("Height", Math.max(1.1F, target.getBbHeight()));
                glowData.putFloat("Intensity", 1.0F);
                EntityVisualEffectSystem.addEffect(
                        target,
                        GDVisualEffects.MALEVOLENT_SHRINE_TARGET_GLOW.getId(),
                        TARGET_GLOW_TICKS,
                        glowData
                );
            }
        }
    }

    private void beginFade() {
        this.fading = true;
        this.fadeTicks = FADE_OUT_TICKS;
    }

    public void refreshDuration() {
        this.remainingTicks = 3;
        this.fading = false;
        this.fadeTicks = FADE_OUT_TICKS;
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, Math.max(1.0F, radius));
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, Math.max(0.0F, damage));
    }


    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("ShrineRadius")) {
            this.setRadius(compound.getFloat("ShrineRadius"));
        }
        if (compound.contains("ShrineDamage")) {
            this.setDamage(compound.getFloat("ShrineDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("ShrineRadius", this.getRadius());
        compound.putFloat("ShrineDamage", this.getDamage());
    }

    @Override
    public UUID getOwnerUUID() {
        return this.getOwnerId();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        MalevolentShrineSpell.unregisterDomain(this);
    }

}