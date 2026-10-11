package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomDollBlockEntity extends BlockEntity {
    private static final String NBT_MODEL_ID = "DollModelId";

    private String modelId = StringUtils.EMPTY;

    private final InfiniteWaterTank waterTank = new InfiniteWaterTank();

    @Nullable
    private LazyOptional<IFluidHandler> fluidHandler;

    public CustomDollBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public CustomDollBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CUSTOM_DOLL_BE.get(), pos, state);
    }

    protected boolean providesInfiniteWater() {
        return this.getBlockState().getBlock() instanceof BakawuDollBlock;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && this.providesInfiniteWater()) {
            LazyOptional<IFluidHandler> handler = this.fluidHandler;
            if (handler == null || !handler.isPresent()) {
                // 首次访问，或此前被 invalidateCaps 作废：重新创建
                handler = LazyOptional.of(() -> this.waterTank);
                this.fluidHandler = handler;
            }
            // 六面皆可抽水
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        LazyOptional<IFluidHandler> handler = this.fluidHandler;
        if (handler != null) {
            handler.invalidate();
            this.fluidHandler = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(NBT_MODEL_ID, modelId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.modelId = tag.getString(NBT_MODEL_ID);
    }

    public void refresh() {
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
        refresh();
    }
}
