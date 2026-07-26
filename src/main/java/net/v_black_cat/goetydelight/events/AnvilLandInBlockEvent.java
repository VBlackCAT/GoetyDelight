package net.v_black_cat.goetydelight.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 当铁砧落地并尝试替换或放置在某个方块中时触发此事件。
 * 此事件针对铁砧与方块交互的特定场景。
 *
 * <p>此事件是可取消的，取消后将阻止铁砧的落地行为（需配合监听器实现）。</p>
 */
public class AnvilLandInBlockEvent extends Event implements ICancellableEvent {

    private final Level level;
    private BlockPos pos;
    private BlockState newState;
    private final BlockState oldState;
    private final FallingBlockEntity entity;
    private boolean dropAsItem = false;
    private float damageAmount = 0;
    private boolean canceled = false;   // 手动管理取消状态

    // 缓存铁砧下方方块的原始状态（在铁砧放置之前）
    private BlockState originalBlockState;

    /**
     * 构造一个新的 AnvilLandInBlockEvent 实例
     *
     * @param level    铁砧所在的维度
     * @param pos      铁砧尝试放置的位置
     * @param newState 将要放置的新方块状态
     * @param oldState 被替换的旧方块状态
     * @param entity   下落中的铁砧实体
     */
    public AnvilLandInBlockEvent(Level level, BlockPos pos, BlockState newState, BlockState oldState, FallingBlockEntity entity) {
        this.level = level;
        this.pos = pos;
        this.newState = newState;
        this.oldState = oldState;
        this.entity = entity;
        this.originalBlockState = level.getBlockState(pos.below());
    }

    // ---------- Getter / Setter ----------

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public BlockState getNewState() {
        return newState;
    }

    public void setNewState(BlockState newState) {
        this.newState = newState;
    }

    public BlockState getOldState() {
        return oldState;
    }

    public FallingBlockEntity getEntity() {
        return entity;
    }

    public boolean isDropAsItem() {
        return dropAsItem;
    }

    public void setDropAsItem(boolean dropAsItem) {
        this.dropAsItem = dropAsItem;
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public void setDamageAmount(float damageAmount) {
        this.damageAmount = damageAmount;
    }

    // ---------- 取消状态管理 ----------
    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    // ---------- 下方方块相关 ----------

    public BlockState getBlockBelow() {
        return originalBlockState;
    }

    public boolean isBlockBelow(BlockState targetBlockState) {
        return this.originalBlockState == targetBlockState ||
                (this.originalBlockState != null && targetBlockState != null &&
                        this.originalBlockState.getBlock() == targetBlockState.getBlock());
    }

    public boolean isBlockBelow(Class<?> blockClass) {
        return this.originalBlockState != null && blockClass.isInstance(this.originalBlockState.getBlock());
    }

    // ---------- 辅助判断 ----------

    public boolean isServer() {
        return !this.level.isClientSide;
    }

    public boolean isClient() {
        return this.level.isClientSide;
    }

    public int getFallTime() {
        return this.entity != null ? this.entity.time : 0;
    }

    public boolean isEntityRemoved() {
        return this.entity == null || !this.entity.isAlive();
    }

    @Override
    public String toString() {
        return String.format("AnvilLandInBlockEvent{level=%s, pos=%s, newState=%s, oldState=%s, originalBlockState=%s, entity=%s, canceled=%s}",
                level.dimension().location(), pos, newState, oldState, originalBlockState, entity, isCanceled());
    }
}