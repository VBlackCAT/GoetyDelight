package net.v_black_cat.goetydelight.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import net.v_black_cat.goetydelight.util.ModSounds;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3f;

import javax.annotation.Nullable;

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

    /** 共用右键文本提示的玩偶 ID */
    private static final java.util.Set<String> MESSAGE_DOLL_IDS = java.util.Set.of("doll_p0", "doll_p7");
    /** 共用文本条数（message.goetydelight.doll_p7.1 ~ .63） */
    private static final int MESSAGE_COUNT = 63;
    /** 记录上一次的文本索引，避免连续重复（仅服务端使用，无需同步/存档） */
    private int lastDollMessageIndex = -1;

    private long bounceTime = 0;
    private static final int TOUCH_ANIMATION_DURATION = 17;

    public final AnimationState touchAnimationState = new AnimationState();
    /** 服务端本地倒计时，不参与同步；避免触摸动画期间每 tick 都写 SynchedEntityData。 */
    public int touchAnimationTimeout = 0;
    /** 客户端本地倒计时，用于渲染插值；只在开始/结束时接收同步包。 */
    private int clientTouchAnimationTick = 0;

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
            applyWu1WU2Wu3DollEffect(player);

            // 特定玩偶的右键文本提示（共用一套文本）
            sendDollMessage(player, randomSource);

            this.setTouchAnimationTick(TOUCH_ANIMATION_DURATION);
            this.touchAnimationTimeout = TOUCH_ANIMATION_DURATION;

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 对 doll_p0 / doll_p7 发送随机文本提示。
     * 两个玩偶共用语言键：message.goetydelight.doll_p7.<1~63>
     * 使用 Component.translatable 让客户端完成本地化。
     */
    private void sendDollMessage(Player player, RandomSource random) {
        String customDollId = getCustomDollId();
        if (StringUtils.isBlank(customDollId) || !MESSAGE_DOLL_IDS.contains(customDollId)) {
            return;
        }

        int index;
        if (MESSAGE_COUNT <= 1) {
            index = 1;
        } else {
            do {
                index = random.nextInt(MESSAGE_COUNT) + 1;
            } while (index == lastDollMessageIndex);
        }
        lastDollMessageIndex = index;

        String key = "message.goetydelight.doll_p7." + index;
        player.displayClientMessage(Component.translatable(key), false);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
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
            if (this.clientTouchAnimationTick > 0) {
                this.clientTouchAnimationTick--;
            }
            setupTouchAnimationState();
        } else if (this.touchAnimationTimeout > 0) {
            // 只在动画开始和结束时同步，中间由客户端本地倒计时。
            if (--this.touchAnimationTimeout == 0) {
                this.setTouchAnimationTick(0);
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
        if (this.level().isClientSide()) {
            return this.clientTouchAnimationTick;
        }
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
    public double getMyRidingOffset() {
        if (this.getVehicle() instanceof Phantom) {
            return 0.125;
        }
        return 0.3125;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return DollEntityItem.createItemWithEntity(this);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_BLOCK_STATE, Blocks.AIR.defaultBlockState());
        this.entityData.define(DATA_SCALE, new Vector3f(1.0f));
        this.entityData.define(DATA_TRANSLATION, new Vector3f());
        this.entityData.define(CUSTOM_DOLL_ID, StringUtils.EMPTY);
        this.entityData.define(TOUCH_ANIMATION_TICK, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_SCALE.equals(pKey)) {
            this.refreshDimensions();
        }
        if (TOUCH_ANIMATION_TICK.equals(pKey)) {
            this.clientTouchAnimationTick = this.entityData.get(TOUCH_ANIMATION_TICK);
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

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

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
                case "doll_fox" -> soundEvent = SoundEvents.FOX_AMBIENT;
                case "doll_skillupper" -> soundEvent = SoundEvents.AXOLOTL_SPLASH;
                case "doll_vblackcat", "doll_lamiao", "doll_sim" -> soundEvent = SoundEvents.CAT_AMBIENT;
                case "doll_maid1" -> soundEvent = SoundEvents.PLAYER_SWIM;
                case "doll_kunkun" -> soundEvent = SoundEvents.CHICKEN_AMBIENT;
                case "doll_luo" -> soundEvent = com.Polarice3.Goety.init.ModSounds.CRONE_LAUGH.get();
                default -> soundEvent = ModSounds.TOUCH_DOLL.get();
            }
        } else {
            soundEvent = ModSounds.TOUCH_DOLL.get();
        }

        float pitchVariation = 0.75f + randomSource.nextFloat() * 0.5f;
        float volume = soundEvent == SoundEvents.FOX_AMBIENT ? 1.5f : 1.0f;
        this.playSound(soundEvent, volume, basePitch * pitchVariation);
    }

    private void applyWu1WU2Wu3DollEffect(Player player) {
        if (!"doll_wu1wu2wu3".equals(getCustomDollId())) {
            return;
        }
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        float saturation = foodData.getSaturationLevel();
        if (foodLevel <= 0) {
            player.hurt(player.damageSources().starve(), 1.0F);
            return;
        }
        foodData.setFoodLevel(Math.max(0, foodLevel - 1));
        if (saturation > 0.0F) {
            foodData.setSaturation(Math.max(0.0F, saturation - 1.0F));
        }
        player.hurt(player.damageSources().starve(), 0.5F);
    }
}