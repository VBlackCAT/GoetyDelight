package net.v_black_cat.goetydelight.entities.display_entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public class DisplayEntity extends LivingEntity {
    private static final EntityDataAccessor<Integer> DATA_SKIN_REVISION =
            SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_ANIMATION =
            SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.STRING);

    public DisplayEntity(EntityType<? extends DisplayEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKIN_REVISION, 0);
        this.entityData.define(DATA_ANIMATION, "");
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
                this.bumpSkinRevision();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.interact(player, hand);
    }

    public int getSkinRevision() {
        return this.entityData.get(DATA_SKIN_REVISION);
    }

    public void bumpSkinRevision() {
        this.entityData.set(DATA_SKIN_REVISION, this.getSkinRevision() + 1);
    }

    // ------------------------------------------------------------------
    // 动画字段
    // ------------------------------------------------------------------

    public String getAnimation() {
        return this.entityData.get(DATA_ANIMATION);
    }

    public void setAnimation(String name) {
        this.entityData.set(DATA_ANIMATION, name == null ? "" : name);
    }

    // ------------------------------------------------------------------
    // NBT 持久化
    // ------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Animation", getAnimation());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Animation")) {
            setAnimation(tag.getString("Animation"));
        }
    }

    // ------------------------------------------------------------------
    // LivingEntity 抽象方法
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