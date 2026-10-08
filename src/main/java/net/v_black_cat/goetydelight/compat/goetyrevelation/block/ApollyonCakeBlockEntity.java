package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.compat.goetyrevelation.RevelationCompatRegistry;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApollyonCakeItem;

import javax.annotation.Nullable;
import java.util.UUID;

public class ApollyonCakeBlockEntity extends BlockEntity implements Nameable {

    private static final String TAG_OWNER_UUID = "OwnerUuid";

    @Nullable
    private UUID ownerUuid;

    public ApollyonCakeBlockEntity(BlockPos pos, BlockState state) {
        super(RevelationCompatRegistry.APOLLYON_CAKE_BE.get(), pos, state);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.ownerUuid = uuid;
        this.setChanged();
    }

    @Nullable
    public UUID getOwnerUuid() {
        return ownerUuid;
    }


    private boolean isTheEnd() {
        BlockState state = this.getBlockState();
        return state.hasProperty(ApollyonCakeBlock.IS_THE_END) && state.getValue(ApollyonCakeBlock.IS_THE_END);
    }

    @Override
    public boolean hasCustomName() {
        return isTheEnd();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return isTheEnd() ? Component.translatable(ApollyonCakeItem.THE_END_NAME_KEY) : null;
    }

    @Override
    public Component getName() {
        return isTheEnd()
                ? Component.translatable(ApollyonCakeItem.THE_END_NAME_KEY)
                : Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerUuid != null) {
            tag.putUUID(TAG_OWNER_UUID, ownerUuid);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID(TAG_OWNER_UUID)) {
            ownerUuid = tag.getUUID(TAG_OWNER_UUID);
        } else {
            ownerUuid = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (ownerUuid != null) {
            tag.putUUID(TAG_OWNER_UUID, ownerUuid);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.hasUUID(TAG_OWNER_UUID)) {
            ownerUuid = tag.getUUID(TAG_OWNER_UUID);
        }
    }
}