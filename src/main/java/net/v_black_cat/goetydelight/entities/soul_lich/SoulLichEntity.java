package net.v_black_cat.goetydelight.entities.soul_lich;

import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.magic.spells.SoulBoltSpell;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.api.GetSpellAttributeFactory;

import java.util.EnumSet;

public class SoulLichEntity extends Summoned {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int idleAnimationTimeout = 0;
    private int walkAnimationTimeout = 0;
    private int attackAnimationTimeout = 0;

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(
                    SoulLichEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(
                ATTACKING,
                false
        );
    }
    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(
                ATTACKING,
                attacking
        );
    }

    private boolean flying = false;

    private int spellCooldown = 0;

    private boolean casting = false;

    private int castingTicks = 0;

    private boolean spellFired = false;

    private double groundY;

    private double hoverY;

    public SoulLichEntity(EntityType<? extends Summoned> entityType,
                          Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                2,
                new SoulBoltGoal()
        );

        this.goalSelector.addGoal(
                8,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        1.0D
                )
        );
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 5.0F, 2.0F));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
//        this.targetSelector.addGoal(
//                2,
//                new NearestAttackableTargetGoal<>(
//                        this,
//                        Monster.class,
//                        true
//                )
//        );
    }
    private void setupAnimationStates() {

        boolean moving =
                this.getDeltaMovement()
                        .horizontalDistanceSqr() > 0.0001D;

        
        if (isAttacking()) {

            idleAnimationState.stop();
            walkAnimationState.stop();

            if (attackAnimationTimeout <= 0) {

                attackAnimationTimeout = 999999;

                attackAnimationState.start(
                        this.tickCount
                );
            }

        } else {

            attackAnimationTimeout = 0;
            attackAnimationState.stop();

            
            if (moving) {

                idleAnimationState.stop();

                if (walkAnimationTimeout <= 0) {

                    walkAnimationTimeout = 999999;

                    walkAnimationState.start(
                            this.tickCount
                    );
                }

            } else {

                walkAnimationTimeout = 0;
                walkAnimationState.stop();

                
                if (idleAnimationTimeout <= 0) {

                    idleAnimationTimeout = 60;

                    idleAnimationState.start(
                            this.tickCount
                    );
                } else {
                    --idleAnimationTimeout;
                }
            }
        }
    }
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            setupAnimationStates();
            return;
        }

        if (spellCooldown > 0) {
            spellCooldown--;
        }

        LivingEntity target = this.getTarget();

        flying =
                target != null
                        && target.isAlive();

        if (flying) {

            this.setNoGravity(true);

            double desiredY;

            if (casting) {
                desiredY = hoverY;
            } else {
                desiredY = target.getY() + 3.0D;
            }

            double motionY =
                    (desiredY - this.getY()) * 0.15D;

            this.setDeltaMovement(
                    this.getDeltaMovement().x,
                    motionY,
                    this.getDeltaMovement().z
            );

            if (casting) {

                castingTicks--;

                target = this.getTarget();

                
                if (!spellFired
                        && castingTicks <= 20
                        && target != null
                        && target.isAlive()) {

                    spellFired = true;

                    this.lookAt(
                            target,
                            30.0F,
                            30.0F
                    );

                    new SoulBoltSpell().mobSpellResult(
                            this,
                            ItemStack.EMPTY
                    );
                }

                
                if (castingTicks <= 0) {

                    casting = false;
                    spellFired = false;

                    setAttacking(false);
                }
            }

        } else {

            casting = false;
            setAttacking(false);

            this.setNoGravity(false);
        }
    }

    public static AttributeSupplier createAttributes() {
        return Summoned.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        24.0D
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.25D
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        32.0D
                )
                .add(
                        Attributes.ARMOR,
                        2.0D
                )
                .add(
                        GetSpellAttributeFactory.createGetSpellAttributeImplementation().getCooldownDiscountAttributeModifier(),
                        0.5D
                )
                .build();
    }

    private class SoulBoltGoal extends Goal {

        private static final double ATTACK_DISTANCE = 8.0D;

        public SoulBoltGoal() {
            this.setFlags(
                    EnumSet.of(
                            Flag.MOVE,
                            Flag.LOOK
                    )
            );
        }

        @Override
        public boolean canUse() {
            LivingEntity target =
                    SoulLichEntity.this.getTarget();

            return target != null
                    && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target =
                    SoulLichEntity.this.getTarget();

            return target != null
                    && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {

            LivingEntity target =
                    SoulLichEntity.this.getTarget();

            if (target == null) {
                return;
            }

            SoulLichEntity.this.getLookControl()
                    .setLookAt(
                            target,
                            30.0F,
                            30.0F
                    );

            if (casting) {
                return;
            }

            double distanceSqr =
                    SoulLichEntity.this.distanceToSqr(
                            target
                    );

            double distance =
                    Math.sqrt(distanceSqr);

            if (distance > ATTACK_DISTANCE) {

                Vec3 targetPos =
                        target.position()
                                .add(
                                        0.0D,
                                        3.0D,
                                        0.0D
                                );

                Vec3 direction =
                        targetPos.subtract(
                                SoulLichEntity.this.position()
                        );

                if (direction.lengthSqr() > 0.01D) {

                    direction =
                            direction.normalize()
                                    .scale(0.25D);

                    SoulLichEntity.this.setDeltaMovement(
                            direction.x,
                            SoulLichEntity.this.getDeltaMovement().y,
                            direction.z
                    );

                    float yaw = (float)(
                            Mth.atan2(
                                    direction.z,
                                    direction.x
                            ) * (180F / Math.PI)
                    ) - 90.0F;

                    SoulLichEntity.this.setYRot(yaw);
                    SoulLichEntity.this.setYHeadRot(yaw);
                    SoulLichEntity.this.setYBodyRot(yaw);
                    SoulLichEntity.this.yRotO = yaw;
                }

            } else {

                SoulLichEntity.this.setDeltaMovement(
                        0.0D,
                        SoulLichEntity.this.getDeltaMovement().y,
                        0.0D
                );

                if (spellCooldown <= 0) {

                    hoverY =
                            target.getY() + 3.0D;

                    casting = true;
                    spellFired = false;

                    setAttacking(true);

                    
                    castingTicks = 35;

                    SoulLichEntity.this.lookAt(
                            target,
                            30.0F,
                            30.0F
                    );

                    spellCooldown = 40;
                }
            }
        }
    }
}