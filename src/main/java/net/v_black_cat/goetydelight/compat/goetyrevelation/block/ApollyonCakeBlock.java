package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.v_black_cat.goetydelight.compat.farmersdelight.FeastServingHelper;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApollyonCakeItem;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApollyonCakeSliceItem;
import vectorwing.farmersdelight.common.block.FeastBlock;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ApollyonCakeBlock extends FeastBlock implements EntityBlock {

    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 3);
    public static final BooleanProperty IS_THE_END = BooleanProperty.create("is_the_end");

    private static final int REGEN_INTERVAL = 600;

    private static final VoxelShape FULL_SHAPE = Shapes.or(
            Shapes.box(0.0, 0.0, 0.0, 16.25 / 16.0, 10.25 / 16.0, 16.25 / 16.0),
            Shapes.box(1.75 / 16.0, 9.75 / 16.0, 1.75 / 16.0, 14.25 / 16.0, 18.25 / 16.0, 14.25 / 16.0),
            Shapes.box(4.0 / 16.0, 18.0 / 16.0, 4.0 / 16.0, 12.0 / 16.0, 24.0 / 16.0, 12.0 / 16.0)
    );

    private static final VoxelShape[][] ROTATED_SHAPES = new VoxelShape[4][4];

    private final List<Supplier<Item>> servingItems;

    static {
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int servings = 0; servings < 4; servings++) {
            for (int i = 0; i < facings.length; i++) {
                ROTATED_SHAPES[servings][i] = rotateVoxelShapeStatic(FULL_SHAPE, facings[i]);
            }
        }
    }

    public ApollyonCakeBlock(Properties properties, List<Supplier<Item>> servingItems, boolean hasLeftovers) {
        super(properties, () -> servingItems.get(0).get(), hasLeftovers);
        this.servingItems = servingItems;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SERVINGS, getMaxServings())
                .setValue(IS_THE_END, false));
    }

    @Override
    public IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    @Override
    public int getMaxServings() {
        return 3;
    }

    @Override
    public ItemStack getServingItem(BlockState state) {
        int servings = state.getValue(SERVINGS);
        int itemIndex = (getMaxServings() - servings) % servingItems.size();
        Item item = servingItems.get(itemIndex).get();

        boolean isTheEnd = state.hasProperty(IS_THE_END) && state.getValue(IS_THE_END);

        if (item instanceof ApollyonCakeSliceItem) {
            return ApollyonCakeSliceItem.createStack(item, isTheEnd);
        }
        return new ItemStack(item);
    }

    public static void setCakeOwner(Level level, BlockPos pos, @Nullable UUID ownerUuid) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ApollyonCakeBlockEntity cakeBe) {
            cakeBe.setOwnerUuid(ownerUuid);
        }
    }

    @Nullable
    public static UUID getCakeOwner(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ApollyonCakeBlockEntity cakeBe) {
            return cakeBe.getOwnerUuid();
        }
        return null;
    }

    public static boolean canInteract(Level level, BlockPos pos, BlockState state, Player player) {
        if (!state.hasProperty(IS_THE_END) || !state.getValue(IS_THE_END)) {
            return true;
        }
        UUID owner = getCakeOwner(level, pos);
        return owner != null && owner.equals(player.getUUID());
    }

    // ==================== 跨版本 takeServing ====================

    /**
     * 内部逻辑，统一挂载到 FeastServingHelper。
     * 因为这里不需要联动“多方块结构”，所以传入简单的 setServings 和 breakStructure 即可。
     */
    private InteractionResult takeServingInternal(LevelAccessor level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        // 1. 前置权限校验（只有 1.3+ 终末模式才有意义，放置到 LevelAccessor 上没问题）
        if (!level.isClientSide() && !canInteract((Level) level, pos, state, player)) {
            return InteractionResult.PASS;
        }

        // 2. 份数为 0 时不取餐（ApollyonCakeBlock 特性：0 份不直接破坏）
        if (state.getValue(SERVINGS) == 0) {
            return InteractionResult.PASS;
        }

        // 3. 复用通用取餐逻辑
        return FeastServingHelper.takeServing(
                level, pos, state, player, hand, this,
                // setServings: 直接修改自身的份数
                (lvl, p, st, servings) -> lvl.setBlock(p, st.setValue(SERVINGS, servings), 3),
                // breakStructure: 当 servings - 1 == 0 且无残留时，拆除方块本身
                (lvl, p, st, ply) -> lvl.removeBlock(p, false)
        );
    }

    // FD 1.2.x 覆写（LevelAccessor）
    protected InteractionResult takeServing(LevelAccessor level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return takeServingInternal(level, pos, state, player, hand);
    }

    // FD 1.3.0+ 覆写（Level）
    protected InteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return takeServingInternal(level, pos, state, player, hand);
    }

    // ==================== 交互重定向与客户端预测 ====================

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 客户端预测，服务端执行
        if (level.isClientSide) {
            if (this.takeServing(level, pos, state, player, hand).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
        }
        return this.takeServing(level, pos, state, player, hand);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection());

        CompoundTag tag = ApollyonCakeItem.getCakeTag(context.getItemInHand());
        if (tag != null) {
            if (tag.contains(ApollyonCakeItem.TAG_SERVINGS)) {
                int servings = tag.getInt(ApollyonCakeItem.TAG_SERVINGS);
                servings = Math.max(0, Math.min(getMaxServings(), servings));
                state = state.setValue(SERVINGS, servings);
            }
            if (tag.contains(ApollyonCakeItem.TAG_IS_THE_END)) {
                state = state.setValue(IS_THE_END, tag.getBoolean(ApollyonCakeItem.TAG_IS_THE_END));
            }
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        CompoundTag tag = ApollyonCakeItem.getCakeTag(stack);
        if (tag != null && tag.hasUUID(ApollyonCakeItem.TAG_OWNER)) {
            setCakeOwner(level, pos, tag.getUUID(ApollyonCakeItem.TAG_OWNER));
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        int servings = state.getValue(SERVINGS);
        boolean isTheEnd = state.hasProperty(IS_THE_END) && state.getValue(IS_THE_END);
        UUID owner = level.getBlockEntity(pos) instanceof ApollyonCakeBlockEntity cakeBe
                ? cakeBe.getOwnerUuid()
                : null;
        return ApollyonCakeItem.createStack(this, servings, isTheEnd, owner);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ApollyonCakeBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int servings = state.getValue(SERVINGS);
        if (servings < 0 || servings >= ROTATED_SHAPES.length) {
            servings = 0;
        }
        int facingIndex = getFacingIndex(state.getValue(FACING));
        return ROTATED_SHAPES[servings][facingIndex];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    // ==================== 放置、破坏与再生 ====================

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!oldState.is(state.getBlock()) && level instanceof ServerLevel serverLevel) {
            ApollyonCakeData.get(serverLevel).addCake(serverLevel.dimension(), pos);
            level.scheduleTick(pos, this, REGEN_INTERVAL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            ApollyonCakeData.get(serverLevel).removeCake(serverLevel.dimension(), pos);
            int servings = state.getValue(SERVINGS);
            boolean isTheEnd = state.hasProperty(IS_THE_END) && state.getValue(IS_THE_END);
            UUID owner = getCakeOwner(level, pos);

            ItemStack drop = ApollyonCakeItem.createStack(this, servings, isTheEnd, owner);
            Block.popResource(level, pos, drop);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int servings = state.getValue(SERVINGS);
        if (servings < getMaxServings()) {
            level.setBlock(pos, state.setValue(SERVINGS, servings + 1), 3);
        }
        level.scheduleTick(pos, this, REGEN_INTERVAL);
    }

    // ==================== 形状工具 ====================

    private static VoxelShape rotateVoxelShapeStatic(VoxelShape shape, Direction facing) {
        if (facing == Direction.NORTH) {
            return shape;
        }
        return shape.toAabbs().stream()
                .map(aabb -> rotateAABBStatic(aabb, facing))
                .map(Shapes::create)
                .reduce(Shapes.empty(), Shapes::or);
    }

    private static AABB rotateAABBStatic(AABB aabb, Direction facing) {
        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        switch (facing) {
            case EAST:
                return new AABB(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX);
            case SOUTH:
                return new AABB(1 - maxX, minY, 1 - maxZ, 1 - minX, maxY, 1 - minZ);
            case WEST:
                return new AABB(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX);
            default:
                return aabb;
        }
    }

    public int getFacingIndex(Direction facing) {
        switch (facing) {
            case NORTH: return 0;
            case EAST: return 1;
            case SOUTH: return 2;
            case WEST: return 3;
            default: return 0;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS, IS_THE_END);
    }
}