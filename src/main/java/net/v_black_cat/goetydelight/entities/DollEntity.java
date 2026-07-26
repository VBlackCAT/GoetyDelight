package net.v_black_cat.goetydelight.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.init.ModEntities;
import net.v_black_cat.goetydelight.init.ModSounds;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DollEntity extends Entity {
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE = SynchedEntityData.defineId(DollEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Vector3f> DATA_SCALE = SynchedEntityData.defineId(DollEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> DATA_TRANSLATION = SynchedEntityData.defineId(DollEntity.class, EntityDataSerializers.VECTOR3);

    private static final EntityDataAccessor<String> CUSTOM_DOLL_ID = SynchedEntityData.defineId(DollEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TOUCH_ANIMATION_TICK = SynchedEntityData.defineId(DollEntity.class, EntityDataSerializers.INT);

    public static final String TAG_BLOCK_STATE = "doll_block_state";
    public static final String TAG_CUSTOM_DOLL_ID = "custom_doll_id";

    private static final String TAG_SCALE = "doll_scale";
    private static final String TAG_TRANSLATION = "doll_translation";


    private long bounceTime = 0;
    private static final int TOUCH_ANIMATION_DURATION = 17;

    public final AnimationState touchAnimationState = new AnimationState();
    public int touchAnimationTimeout = 0;


    public DollEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.refreshDimensions();
    }

    public DollEntity(Level level, double x, double y, double z, float yaw) {
        this(ModEntities.DOLL_ENTITY.get(), level);
        this.setPos(x, y, z);
        this.setYRot(yaw);
    }

    public boolean canSurvives() {
        if (!this.level().noCollision(this)) {
            return false;
        }
        return this.level().getEntities(this, this.getBoundingBox(), e -> e instanceof DollEntity).isEmpty();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        long time = this.bounceTime - System.currentTimeMillis();
        if (time > 0) {
            return InteractionResult.PASS;
        }
        this.bounceTime = System.currentTimeMillis() + 840;
        if (player.level() instanceof ServerLevel serverLevel) {
            RandomSource randomSource = serverLevel.getRandom();
            float pitch = 0.75f + randomSource.nextFloat() * 0.5f;
            playSoundByCustomId(serverLevel, randomSource, pitch);

            Vec3 notePos = this.position().add(
                    randomSource.nextFloat() / 2 - 0.25,
                    1 + randomSource.nextFloat() / 5,
                    randomSource.nextFloat() / 2 - 0.25
            );
            float color = randomSource.nextInt(4) / 24.0F;
            serverLevel.sendParticles(ParticleTypes.NOTE, notePos.x(), notePos.y(), notePos.z(), 0, color, 0, 0, 1);

            this.setTouchAnimationTick(TOUCH_ANIMATION_DURATION);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999F);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return pDistance < 128 * 128;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            // 必须是玩家直接造成的伤害才能打掉
            if (!this.isRemoved() && !this.level().isClientSide && source.getDirectEntity() instanceof Player) {
                this.kill();
                this.markHurt();
                this.dropItem(source.getEntity());
                return true;
            }
            return false;
        }
    }

    private void dropItem(@Nullable Entity pBrokenEntity) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.WOOL_BREAK, 1.0F, 1.0F);
            if (pBrokenEntity instanceof Player player) {
                if (player.getAbilities().instabuild) {
                    return;
                }
            }
            ItemStack dollItem = DollEntityItem.createItemWithEntity(this);
            this.spawnAtLocation(dollItem);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupTouchAnimationState();
        } else {
            int tick = this.getTouchAnimationTick();
            if (tick > 0) {
                this.setTouchAnimationTick(tick - 1);
            }
        }
    }

    private void setupTouchAnimationState() {
        int touchTick = this.getTouchAnimationTick();

        if (touchTick > 0 && !touchAnimationState.isStarted()) {
            touchAnimationState.start(this.tickCount);
        }

        if (touchTick <= 0 && touchAnimationState.isStarted()) {
            touchAnimationState.stop();
        }
    }

    public int getTouchAnimationTick() {
        return this.entityData.get(TOUCH_ANIMATION_TICK);
    }

    public void setTouchAnimationTick(int tick) {
        this.entityData.set(TOUCH_ANIMATION_TICK, tick);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof Phantom phantom) {
            this.setXRot(-phantom.getXRot());
        }
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        // 默认行为
        Vec3 defaultAttachment = super.getPassengerAttachmentPoint(entity, dimensions, partialTick);

        // 如果是幻翼，调整位置
        if (this.getVehicle() instanceof Phantom) {
            return new Vec3(0.0, 0.125, 0.0);  // 自定义 X, Y, Z 偏移
        }

        return defaultAttachment;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return DollEntityItem.createItemWithEntity(this);
    }

    // 修复 1: 新的 defineSynchedData 方法签名 - 使用 Builder
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, Blocks.AIR.defaultBlockState());
        builder.define(DATA_SCALE, new Vector3f(1.0f));
        builder.define(DATA_TRANSLATION, new Vector3f());
        builder.define(CUSTOM_DOLL_ID, StringUtils.EMPTY);
        builder.define(TOUCH_ANIMATION_TICK, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_SCALE.equals(pKey)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(TAG_BLOCK_STATE)) {
            HolderLookup<Block> lookup = this.level().holderLookup(Registries.BLOCK);
            setDisplayBlockState(NbtUtils.readBlockState(lookup, tag.getCompound(TAG_BLOCK_STATE)));
        }
        if (tag.contains(TAG_CUSTOM_DOLL_ID)) {
            setCustomDollId(tag.getString(TAG_CUSTOM_DOLL_ID));
        }
        if (tag.contains(TAG_SCALE)) {
            setDisplayScale(readVector3f(tag.getCompound(TAG_SCALE)));
        }
        if (tag.contains(TAG_TRANSLATION)) {
            setDisplayTranslation(readVector3f(tag.getCompound(TAG_TRANSLATION)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        BlockState blockState = getDisplayBlockState();
        if (blockState != Blocks.AIR.defaultBlockState()) {
            tag.put(TAG_BLOCK_STATE, NbtUtils.writeBlockState(blockState));
        }
        if (StringUtils.isNotBlank(getCustomDollId())) {
            tag.putString(TAG_CUSTOM_DOLL_ID, getCustomDollId());
        }
        tag.put(TAG_SCALE, writeVector3f(getDisplayScale()));
        tag.put(TAG_TRANSLATION, writeVector3f(getDisplayTranslation()));
    }

    private Vector3f readVector3f(CompoundTag tag) {
        return new Vector3f(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z"));
    }

    private CompoundTag writeVector3f(Vector3f vector) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("x", vector.x);
        tag.putFloat("y", vector.y);
        tag.putFloat("z", vector.z);
        return tag;
    }


    public BlockState getDisplayBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    public void setDisplayBlockState(BlockState blockState) {
        this.entityData.set(DATA_BLOCK_STATE, blockState);
    }

    public Vector3f getDisplayScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public void setDisplayScale(Vector3f scale) {
        this.entityData.set(DATA_SCALE, scale, true);
    }

    public Vector3f getDisplayTranslation() {
        return this.entityData.get(DATA_TRANSLATION);
    }

    public void setDisplayTranslation(Vector3f translation) {
        this.entityData.set(DATA_TRANSLATION, translation, true);
    }

    public void setCustomDollId(String customDollId) {
        this.entityData.set(CUSTOM_DOLL_ID, customDollId);
    }

    public String getCustomDollId() {
        return this.entityData.get(CUSTOM_DOLL_ID);
    }

    // 修复 2: 不再需要 getAddEntityPacket 方法 - 删除或注释掉
    // NeoForge 1.21+ 使用 Bundle 机制自动处理
    // @Override
    // public Packet<ClientGamePacketListener> getAddEntityPacket() {
    //     return NetworkHooks.getEntitySpawningPacket(this);
    // }

    @Override
    public void setRot(float yRot, float xRot) {
        super.setRot(yRot, xRot);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        Vector3f displayScale = this.getDisplayScale();
        EntityDimensions dimensions = super.getDimensions(pose);
        float width = Math.max(Math.abs(displayScale.x), Math.abs(displayScale.z));
        float height = Math.abs(displayScale.y);
        return dimensions.scale(width, height);
    }

    private void playSoundByCustomId(ServerLevel serverLevel, RandomSource randomSource, float basePitch) {
        String customDollId = getCustomDollId();
        SoundEvent soundEvent;

        if (StringUtils.isNotBlank(customDollId)) {
            switch (customDollId) {
                case "doll_lll252", "doll_maid2" -> soundEvent = SoundEvents.WITHER_AMBIENT;
                case "doll_dwky" -> soundEvent = SoundEvents.ENDER_DRAGON_AMBIENT;
                case "doll_m3" -> soundEvent = SoundEvents.BEACON_ACTIVATE;
                case "doll_baka" -> soundEvent = SoundEvents.PIG_AMBIENT;
                case "doll_bai" -> soundEvent = SoundEvents.PIG_DEATH;
                case "doll_windis" -> soundEvent = SoundEvents.DROWNED_AMBIENT;
                case "doll_fox", "doll_xiaoarin" -> soundEvent = SoundEvents.FOX_AMBIENT;
                case "doll_skillupper" -> soundEvent = SoundEvents.AXOLOTL_SPLASH;
                case "doll_vblackcat", "doll_lamiao", "doll_sim" -> soundEvent = SoundEvents.CAT_AMBIENT;
                case "doll_maid1" -> soundEvent = SoundEvents.PLAYER_SWIM;
                case "doll_kunkun" -> soundEvent = SoundEvents.CHICKEN_AMBIENT;
                default -> soundEvent = ModSounds.TOUCH_DOLL.get();
            }
        } else {
            soundEvent = ModSounds.TOUCH_DOLL.get();
        }

        float pitchVariation = 0.75f + randomSource.nextFloat() * 0.5f;
        float volume = soundEvent == SoundEvents.FOX_AMBIENT ? 1.5f : 1.0f;
        this.playSound(soundEvent, volume, basePitch * pitchVariation);
    }
}