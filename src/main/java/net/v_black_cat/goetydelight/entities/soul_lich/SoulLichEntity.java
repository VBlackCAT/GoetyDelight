package net.v_black_cat.goetydelight.entities.soul_lich;

import com.Polarice3.Goety.common.entities.ally.Summoned;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class SoulLichEntity extends Summoned{
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();

    private int idleAnimationTimeout = 0;
    private int attackAnimationTimeout = 0;
    private static final int ATTACK_DURATION = 25;
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(SoulLichEntity.class, EntityDataSerializers.BOOLEAN);

    public SoulLichEntity(EntityType<? extends Summoned> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulLichMeleeAttackGoal());
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        } else {
            if (isAttacking()) {
                if (attackAnimationTimeout > 0) {
                    --attackAnimationTimeout;
                    if (attackAnimationTimeout <= 0) {
                        setAttacking(false);
                    }
                }
            }
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    private void setupAnimationStates() {
        if (this.attackAnimationTimeout > 0) {
            --this.attackAnimationTimeout;
            if (this.attackAnimationTimeout <= 0) {
                this.attackAnimationState.stop();
            }
        }

        if (!isAttacking()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistance() > 0.01F;

            if (isMoving) {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.idleAnimationState.stop();
            } else {
                this.walkAnimationState.stop();

                if (this.idleAnimationTimeout <= 0) {
                    this.idleAnimationTimeout = 60;
                    this.idleAnimationState.start(this.tickCount);
                } else {
                    --this.idleAnimationTimeout;
                }
            }
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();

            if (attackAnimationTimeout <= 0) {
                attackAnimationTimeout = ATTACK_DURATION;
                attackAnimationState.start(this.tickCount);
            } else if (attackAnimationTimeout > 0) {
                --this.attackAnimationTimeout;
                if (this.attackAnimationTimeout <= 0) {
                    this.attackAnimationState.stop();
                }
            }
        }

        if (!isAttacking()) {
            attackAnimationState.stop();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && !this.isAttacking()) {
            this.setAttacking(true);
            this.attackAnimationTimeout = ATTACK_DURATION;
            this.attackAnimationState.start(this.tickCount);
            this.idleAnimationState.stop();
        }
        return result;
    }

    public static AttributeSupplier createAttributes() {
        return Summoned.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 0.0)
                .build();
    }

    class SoulLichMeleeAttackGoal extends MeleeAttackGoal {
        public SoulLichMeleeAttackGoal() {
            super(SoulLichEntity.this, 1.0, true);
        }

        @Override
        public void start() {
            super.start();
            setAttacking(false);
        }

        @Override
        public void stop() {
            super.stop();
            setAttacking(false);
        }
    }
}
