package net.v_black_cat.goetydelight.entities.display_entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;

public class DisplayEntity extends LivingEntity {

    // ------------------------------------------------------------------
    // 同步数据
    // ------------------------------------------------------------------

    private static final EntityDataAccessor<String> DATA_ANIMATION =
            SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> DATA_MUSIC_GROUP =
            SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.STRING);

    // ------------------------------------------------------------------
    // 时停
    // ------------------------------------------------------------------

    private int timeStopTickCount = 0;

    private static final int TIME_STOP_IMMUNE_LIFETIME = 200;
    private int timeStopImmuneTicks = -1;

    // ------------------------------------------------------------------
    // 皮肤缓存（仅客户端）
    // ------------------------------------------------------------------

    @Nullable
    private ResourceLocation cachedSkin = null;

    public DisplayEntity(EntityType<? extends DisplayEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ANIMATION, "");
        this.entityData.define(DATA_MUSIC_GROUP, "");
    }

    // ------------------------------------------------------------------
    // 皮肤缓存
    // ------------------------------------------------------------------

    @Nullable
    public ResourceLocation getCachedSkin() {
        return cachedSkin;
    }

    public void setCachedSkin(@Nullable ResourceLocation skin) {
        this.cachedSkin = skin;
    }

    // ------------------------------------------------------------------
    // 命名牌
    // ------------------------------------------------------------------

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        notifyClientSkinRefresh(name);
    }

    private void notifyClientSkinRefresh(@Nullable Component name) {
        if (!this.level().isClientSide()) return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            DisplayEntityRender.onNameChanged(this, name);
        });
    }

    // ------------------------------------------------------------------
    // 同步数据变化
    // ------------------------------------------------------------------

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (!this.level().isClientSide()) return;

        boolean isAnim  = DATA_ANIMATION.equals(key);
        boolean isGroup = DATA_MUSIC_GROUP.equals(key);
        boolean isName  = DATA_CUSTOM_NAME.equals(key);

        if (isAnim) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    DisplayEntityMusicManager.onAnimationChanged(this, this.getAnimation()));
        } else if (isGroup) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    DisplayEntityMusicManager.onMusicGroupChanged(this, this.getMusicGroup()));
        } else if (isName) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    DisplayEntityRender.onNameChanged(this, this.getCustomName()));
        }
    }

    // ------------------------------------------------------------------
    // 移除：通知音乐管理器 + 清理渲染缓存
    // ------------------------------------------------------------------

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        if (this.level().isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                DisplayEntityMusicManager.onEntityRemoved(this);
            });
        }
    }

    // ------------------------------------------------------------------
    // tick（已删除强制测试音乐 + 周期日志）
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        tickTimeStopImmuneLifetime();
    }

    // ------------------------------------------------------------------
    // 免疫伤害
    // ------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public void kill() {
        this.discard();
    }

    @Override
    protected void dropAllDeathLoot(DamageSource source) {
    }

    // ------------------------------------------------------------------
    // 命名牌交互
    // ------------------------------------------------------------------

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.NAME_TAG) && stack.hasCustomHoverName()) {
            if (!this.level().isClientSide) {
                this.setCustomName(stack.getHoverName());
                this.setCustomNameVisible(true);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.interact(player, hand);
    }

    // ------------------------------------------------------------------
    // 动画字段
    // ------------------------------------------------------------------

    public String getAnimation() {
        return this.entityData.get(DATA_ANIMATION);
    }

    public void setAnimation(String name) {
        String v = name == null ? "" : name;
        this.entityData.set(DATA_ANIMATION, v);
    }

    // ------------------------------------------------------------------
    // 音乐分组
    // ------------------------------------------------------------------

    public String getMusicGroup() {
        return this.entityData.get(DATA_MUSIC_GROUP);
    }

    public void setMusicGroup(String group) {
        String v = group == null ? "" : group;
        this.entityData.set(DATA_MUSIC_GROUP, v);
    }

    // ------------------------------------------------------------------
    // 时停
    // ------------------------------------------------------------------

    public void tickTimeStop() {
        this.timeStopTickCount++;
        tickTimeStopImmuneLifetime();
    }

    public int getTimeStopTickCount() {
        return timeStopTickCount;
    }

    private void tickTimeStopImmuneLifetime() {
        if (this.level().isClientSide) return;

        if (!isTimeStopImmune()) {
            timeStopImmuneTicks = -1;
            return;
        }
        if (timeStopImmuneTicks < 0) timeStopImmuneTicks = TIME_STOP_IMMUNE_LIFETIME;
        if (timeStopImmuneTicks > 0) timeStopImmuneTicks--;
        if (timeStopImmuneTicks == 0) this.discard();
    }

    // ------------------------------------------------------------------
    // NBT
    // ------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Animation", getAnimation());
        tag.putString("MusicGroup", getMusicGroup());
        tag.putInt("TimeStopTickCount", timeStopTickCount);
        tag.putInt("TimeStopImmuneTicks", timeStopImmuneTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Animation")) setAnimation(tag.getString("Animation"));
        if (tag.contains("MusicGroup")) setMusicGroup(tag.getString("MusicGroup"));
        if (tag.contains("TimeStopTickCount")) timeStopTickCount = tag.getInt("TimeStopTickCount");
        if (tag.contains("TimeStopImmuneTicks")) timeStopImmuneTicks = tag.getInt("TimeStopImmuneTicks");
    }

    public boolean isTimeStopImmune() {
        return this.getPersistentData().getBoolean("TimeStopImmune");
    }

    // ------------------------------------------------------------------
    // LivingEntity 抽象
    // ------------------------------------------------------------------

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return java.util.Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5152D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10D)
                .add(Attributes.ARMOR, 0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0D);
    }
}