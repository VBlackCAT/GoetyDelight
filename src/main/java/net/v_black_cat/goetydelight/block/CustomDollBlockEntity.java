package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.init.ModBlockEntities;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class CustomDollBlockEntity extends BlockEntity {
    private static final String NBT_MODEL_ID = "DollModelId";

    // 可选：如果某些娃娃需要自定义 modelId，可以保留这个字段
    private String customModelId = StringUtils.EMPTY;

    public CustomDollBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public CustomDollBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.DOLL_BLOCK.get(), pos, state);
    }

    /**
     * 获取 modelId
     * 优先返回自定义的 modelId，如果没有则从 BlockState 获取注册名
     */
    public String getModelId() {
        // 如果有自定义的 modelId，使用自定义的
        if (StringUtils.isNotBlank(customModelId)) {
            return customModelId;
        }

        // 否则从 BlockState 获取方块的注册名作为 modelId
        return getModelIdFromBlockState();
    }

    /**
     * 从当前 BlockState 获取方块的注册名（去掉命名空间前缀）
     */
    private String getModelIdFromBlockState() {
        if (level == null) {
            return StringUtils.EMPTY;
        }

        BlockState state = getBlockState();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (blockId == null) {
            return StringUtils.EMPTY;
        }

        // 返回注册名，例如 "doll_maid1"
        // 如果需要完整路径 "goetydelight:doll_maid1"，使用 blockId.toString()
        return blockId.getPath();
    }

    /**
     * 获取完整的 modelId（包含命名空间）
     */
    public String getFullModelId() {
        if (StringUtils.isNotBlank(customModelId)) {
            return customModelId;
        }

        if (level == null) {
            return StringUtils.EMPTY;
        }

        BlockState state = getBlockState();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        return blockId != null ? blockId.toString() : StringUtils.EMPTY;
    }

    /**
     * 设置自定义 modelId（可选，用于特殊需求）
     */
    public void setModelId(String modelId) {
        this.customModelId = modelId;
        refresh();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // 只在有自定义 modelId 时才保存
        if (StringUtils.isNotBlank(customModelId)) {
            tag.putString(NBT_MODEL_ID, customModelId);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // 读取自定义 modelId（如果存在）
        this.customModelId = tag.getString(NBT_MODEL_ID);
    }

    public void refresh() {
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}