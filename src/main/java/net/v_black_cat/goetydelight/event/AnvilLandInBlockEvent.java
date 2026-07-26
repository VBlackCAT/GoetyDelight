package net.v_black_cat.goetydelight.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;

/**
 * 当铁砧落地并尝试替换或放置在某个方块中时触发此事件。
 * 此事件针对铁砧与方块交互的特定场景。
 *
 * <p>此事件是可取消的，取消后将阻止铁砧的落地行为。</p>
 */
@Cancelable
@Mod.EventBusSubscriber(modid = "goetydelight", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilLandInBlockEvent extends Event {

    private final Level level;
    private BlockPos pos;
    private BlockState newState;
    private final BlockState oldState;
    private final FallingBlockEntity entity;
    private boolean dropAsItem = false;
    private float damageAmount = 0;

    // 缓存铁砧下方方块的原始状态（在铁砧放置之前）
    private BlockState originalBlockState;

    /**
     * 构造一个新的 AnvilLandInBlockEvent 实例
     *
     * @param level 铁砧所在的维度
     * @param pos 铁砧尝试放置的位置
     * @param newState 将要放置的新方块状态
     * @param oldState 被替换的旧方块状态
     * @param entity 下落中的铁砧实体
     */
    public AnvilLandInBlockEvent(Level level, BlockPos pos, BlockState newState, BlockState oldState, FallingBlockEntity entity) {
        this.level = level;
        this.pos = pos ;
        this.newState = newState;
        this.oldState = oldState;
        this.entity = entity;
        // 在构造时记录铁砧下方的原始方块状态
        this.originalBlockState = level.getBlockState(pos.below());
    }

    /**
     * 获取铁砧所在的维度
     *
     * @return 维度对象
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * 获取铁砧尝试放置的位置
     *
     * @return 方块位置
     */
    public BlockPos getPos() {
        return this.pos;
    }

    /**
     * 设置铁砧放置的位置
     *
     * @param pos 新的方块位置
     */
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * 获取将要放置的新方块状态
     *
     * @return 新的方块状态
     */
    public BlockState getNewState() {
        return this.newState;
    }

    /**
     * 设置将要放置的新方块状态
     *
     * @param newState 新的方块状态
     */
    public void setNewState(BlockState newState) {
        this.newState = newState;
    }

    /**
     * 获取被替换的旧方块状态
     * 注意：此方法返回的是传入的 oldState 参数，可能不准确
     *
     * @return 旧的方块状态
     */
    public BlockState getOldState() {
        return this.oldState;
    }

    /**
     * 获取铁砧下方方块的原始状态（在铁砧放置之前）
     * 这是最准确的方法来获取铁砧落地位置原本的方块
     *
     * @return 铁砧下方方块的原始状态
     */
    public BlockState getBlockBelow() {
        return this.originalBlockState;
    }

    /**
     * 检查铁砧下方方块是否是指定的方块
     *
     * @param targetBlockState 目标方块状态
     * @return 如果是则返回 true
     */
    public boolean isBlockBelow(BlockState targetBlockState) {
        return this.originalBlockState == targetBlockState ||
                (this.originalBlockState != null && targetBlockState != null &&
                        this.originalBlockState.getBlock() == targetBlockState.getBlock());
    }

    /**
     * 检查铁砧下方方块是否是指定的方块类型
     *
     * @param blockClass 方块类
     * @return 如果是则返回 true
     */
    public boolean isBlockBelow(Class<?> blockClass) {
        return this.originalBlockState != null && blockClass.isInstance(this.originalBlockState.getBlock());
    }

    /**
     * 获取下落中的铁砧实体
     *
     * @return 铁砧实体对象
     */
    public FallingBlockEntity getEntity() {
        return this.entity;
    }

    /**
     * 检查事件是否发生在服务器端
     *
     * @return 如果是服务器端则返回 true
     */
    public boolean isServer() {
        return !this.level.isClientSide;
    }

    /**
     * 检查事件是否发生在客户端
     *
     * @return 如果是客户端则返回 true
     */
    public boolean isClient() {
        return this.level.isClientSide;
    }

    /**
     * 获取是否将铁砧作为物品掉落而不是放置
     *
     * @return 如果掉落为物品则返回 true
     */
    public boolean isDropAsItem() {
        return dropAsItem;
    }

    /**
     * 设置是否将铁砧作为物品掉落而不是放置
     *
     * @param dropAsItem 如果为 true，铁砧将作为物品掉落
     */
    public void setDropAsItem(boolean dropAsItem) {
        this.dropAsItem = dropAsItem;
    }

    /**
     * 获取铁砧造成的伤害值
     *
     * @return 伤害值
     */
    public float getDamageAmount() {
        return damageAmount;
    }

    /**
     * 设置铁砧造成的伤害值
     *
     * @param damageAmount 新的伤害值
     */
    public void setDamageAmount(float damageAmount) {
        this.damageAmount = damageAmount;
    }

    /**
     * 获取铁砧下落的时间（如果实体存在）
     * 对应 FallingBlockEntity 中的 time 字段
     *
     * @return 下落时间（刻），如果实体不存在则返回 0
     */
    public int getFallTime() {
        return this.entity != null ? this.entity.time : 0;
    }

    /**
     * 检查铁砧是否在下落过程中被破坏
     *
     * @return 如果被破坏则返回 true
     */
    public boolean isEntityRemoved() {
        return this.entity == null || !this.entity.isAlive();
    }

    @Override
    public String toString() {
        return String.format("AnvilLandInBlockEvent{level=%s, pos=%s, newState=%s, oldState=%s, originalBlockState=%s, entity=%s, canceled=%s}",
                level.dimension().location(), pos, newState, oldState, originalBlockState, entity, isCanceled());
    }
}