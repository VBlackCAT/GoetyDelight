package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.compat.goetyrevelation.RevelationCompatRegistry;

import javax.annotation.Nullable;
import java.util.UUID;

public class ApollyonCakeBlockEntity extends BlockEntity {

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