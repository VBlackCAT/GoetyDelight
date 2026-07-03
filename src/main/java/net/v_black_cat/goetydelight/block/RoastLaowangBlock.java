package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class RoastLaowangBlock extends HorizontalDirectionalBlock {

    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 13);
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;

    // 小刀切耳朵的常量
    public static final int EAR_CUT_SERVINGS_1 = 13;
    public static final int EAR_CUT_SERVINGS_2 = 12;
    public static final int BONEMEAL_DROP_MIN = 3;
    public static final int BONEMEAL_DROP_MAX = 8;

    private final List<Supplier<Item>> servingItems;

    // 预先计算所有朝向的碰撞箱 [servings][facing]
    private static final VoxelShape[][][] ROTATED_SHAPES = new VoxelShape[14][4][];

    // 定义7个不同的形状，对应不同的阶段
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            makeShape0(),  // stage 0 (servings=13)
            makeShape3(),  // stage 3 (servings=10-12)
            makeShape4(),  // stage 4 (servings=9)
            makeShape5(),  // stage 5 (servings=8)
            makeShape6(),  // stage 6 (servings=7)
            makeShape7(),  // stage 7 (servings=6)
            makeShape13()  // stage 13 (servings=0)
    };

    public RoastLaowangBlock(Properties properties, List<Supplier<Item>> servingItems, boolean hasLeftovers) {
        super(properties);
        this.servingItems = servingItems;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SERVINGS, getMaxServings())
                .setValue(PART, BedPart.HEAD));
    }

    // 静态初始化块，预计算所有旋转
    static {
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        for (int servings = 0; servings < 14; servings++) {
            for (int i = 0; i < facings.length; i++) {
                Direction facing = facings[i];
                VoxelShape originalShape = getShapeForServings(servings);
                ROTATED_SHAPES[servings][i] = new VoxelShape[]{rotateVoxelShapeStatic(originalShape, facing)};
            }
        }
    }

    private static VoxelShape getShapeForServings(int servings) {
        if (servings >= 11 &&servings <= 13) {
            return SHAPES[0];
        } else if (servings == 10) {
            return SHAPES[1];
        } else if (servings == 9) {
            return SHAPES[2];
        } else if (servings == 8) {
            return SHAPES[3];
        } else if (servings == 7) {
            return SHAPES[4];
        } else if (servings >= 1 && servings <= 6) {
            return SHAPES[5];
        } else if (servings == 0) {
            return SHAPES[6];
        }
        return Shapes.empty();
    }

    public ItemStack getServingItem(BlockState state) {
        int servings = state.getValue(SERVINGS);
        int takenCount = getMaxServings() - servings;
        int itemIndex = takenCount;
        if (itemIndex >= servingItems.size()) {
            itemIndex = servingItems.size() - 1;
        }
        if (itemIndex < 0) {
            itemIndex = 0;
        }
        return new ItemStack(servingItems.get(itemIndex).get());
    }

    public int getMaxServings() {
        return 13;
    }

    // ================ 工具方法 ================

    private boolean requiresKnife(int servings) {
        return servings == EAR_CUT_SERVINGS_1 || servings == EAR_CUT_SERVINGS_2;
    }

    private Component getUseKnifeMessage() {
        return Component.translatable("block.goetydelight.roast_laowang_block.use_knife");
    }

    private Component getUseBowlMessage(ItemStack bowl) {
        return Component.translatable("block.goetydelight.feast.use_container", bowl.getHoverName());
    }

    private Direction getDirectionToOther(BedPart part, Direction direction) {
        return part == BedPart.HEAD ? direction : direction.getOpposite();
    }

    private void giveOrDropItem(Player player, ItemStack stack, Level level, BlockPos pos) {
        if (stack.isEmpty()) {
            return;
        }

        if (!player.isCreative()) {
            if (!player.getInventory().add(stack.copy())) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        } else {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    // ================ 放置和结构管理 ================

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        BlockPos footPos = pos.relative(direction);
        Level level = context.getLevel();

        if (!level.getBlockState(footPos).canBeReplaced(context) ||
                !level.getWorldBorder().isWithinBounds(footPos)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(PART, BedPart.HEAD);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            Direction direction = state.getValue(FACING);
            BlockPos footPos = pos.relative(direction);

            BlockState footState = this.defaultBlockState()
                    .setValue(PART, BedPart.FOOT)
                    .setValue(FACING, direction)
                    .setValue(SERVINGS, state.getValue(SERVINGS));

            level.setBlock(footPos, footState, 3);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        BedPart part = state.getValue(PART);
        Direction direction = state.getValue(FACING);

        if (facing == getDirectionToOther(part, direction)) {
            return state.canSurvive(level, currentPos) &&
                    facingState.is(this) &&
                    facingState.getValue(PART) != part ?
                    state : Blocks.AIR.defaultBlockState();
        }
        return !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() :
                super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    private BlockPos getOtherPartPos(BlockPos pos, BedPart part, Direction facing) {
        if (part == BedPart.HEAD) {
            return pos.relative(facing);
        } else {
            return pos.relative(facing.getOpposite());
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BedPart part = state.getValue(PART);
            if (part == BedPart.FOOT) {
                BlockPos headPos = pos.relative(getDirectionToOther(part, state.getValue(FACING)));
                BlockState headState = level.getBlockState(headPos);
                if (headState.is(this) && headState.getValue(PART) == BedPart.HEAD) {
                    level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, headPos, Block.getId(headState));
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private void breakEntireStructure(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        BedPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos headPos = part == BedPart.HEAD ? pos : pos.relative(facing.getOpposite());
        BlockPos footPos = headPos.relative(facing);

        // 破坏 HEAD
        if (level.getBlockState(headPos).getBlock() instanceof RoastLaowangBlock) {
            BlockState oldState = level.getBlockState(headPos);
            level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 3);
            if (player != null) {
                level.levelEvent(player, 2001, headPos, Block.getId(oldState));
            }
        }

        // 破坏 FOOT
        if (level.getBlockState(footPos).getBlock() instanceof RoastLaowangBlock) {
            BlockState oldState = level.getBlockState(footPos);
            level.setBlock(footPos, Blocks.AIR.defaultBlockState(), 3);
            if (player != null) {
                level.levelEvent(player, 2001, footPos, Block.getId(oldState));
            }
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (state.getValue(PART) == BedPart.HEAD) {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
        }
    }

    // ================ 交互方法 ================

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 只有 HEAD 部分可以交互
        if (state.getValue(PART) != BedPart.HEAD) {
            return InteractionResult.PASS;
        }

        int servings = state.getValue(SERVINGS);
        ItemStack heldItem = player.getItemInHand(hand);

        // 检查是否持有小刀 (使用 FarmersDelight 的刀具标签)
        boolean isKnife = heldItem.is(ModTags.Items.KNIVES);
        boolean isBowl = heldItem.is(Items.BOWL);

        // 情况1: servings 为 13 或 12，需要使用小刀
        if (requiresKnife(servings)) {
            if (isKnife) {
                return cutEar(level, pos, state, player, hand);
            } else {
                player.displayClientMessage(getUseKnifeMessage(), true);
                return InteractionResult.SUCCESS;
            }
        }

        // 情况2: servings 为 0，破坏方块并掉落骨粉
        if (servings == 0) {
            RandomSource random = level.getRandom();
            int boneMealCount = BONEMEAL_DROP_MIN + random.nextInt(BONEMEAL_DROP_MAX - BONEMEAL_DROP_MIN + 1);
            Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.5, pos.getZ(),
                    new ItemStack(Items.BONE_MEAL, boneMealCount));
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            breakEntireStructure(level, pos, state, player);
            return InteractionResult.SUCCESS;
        }

        // 情况3: 使用碗获取食物
        if (isBowl) {
            return takeServing(level, pos, state, player, hand);
        }

        // 情况4: 持有其他物品，提示使用碗
        if (!isBowl) {
            player.displayClientMessage(getUseBowlMessage(new ItemStack(Items.BOWL)), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    protected InteractionResult cutEar(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        int servings = state.getValue(SERVINGS);

        // 检查是否处于需要小刀的阶段
        if (!requiresKnife(servings)) {
            return InteractionResult.PASS;
        }

        // 减少份数
        int newServings = servings - 1;
        Direction facing = state.getValue(FACING);
        BlockPos headPos = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(facing.getOpposite());
        BlockPos footPos = headPos.relative(facing);

        // 更新两个方块
        BlockState headState = level.getBlockState(headPos);
        BlockState footState = level.getBlockState(footPos);

        if (headState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(headPos, headState.setValue(SERVINGS, newServings), 3);
        }
        if (footState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(footPos, footState.setValue(SERVINGS, newServings), 3);
        }

        // 创建猪耳物品栈
        ItemStack earStack = servings == EAR_CUT_SERVINGS_1
                ? new ItemStack(servingItems.get(0).get(), 1)
                : new ItemStack(servingItems.get(0).get());

        // 优先放入玩家背包，满了才掉落
        giveOrDropItem(player, earStack, level, pos);

        // 播放切的声音
        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.5F, 0.8F);

        return InteractionResult.SUCCESS;
    }

    protected InteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        int servings = state.getValue(SERVINGS);
        Direction facing = state.getValue(FACING);
        BlockPos headPos = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(facing.getOpposite());
        BlockPos footPos = headPos.relative(facing);

        // 获取食物
        ItemStack servingItem = getServingItem(state);

        // 减少份数
        int newServings = servings - 1;
        BlockState headState = level.getBlockState(headPos);
        BlockState footState = level.getBlockState(footPos);

        if (headState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(headPos, headState.setValue(SERVINGS, newServings), 3);
        }
        if (footState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(footPos, footState.setValue(SERVINGS, newServings), 3);
        }

        // 消耗碗
        ItemStack heldItem = player.getItemInHand(hand);
        if (!player.isCreative()) {
            heldItem.shrink(1);
        }

        // 优先放入玩家背包，满了才掉落
        if (!player.getInventory().add(servingItem.copy())) {
            player.drop(servingItem.copy(), false);
        }

        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 0.7F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    // ================ 渲染和碰撞箱 ================

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == BedPart.HEAD ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 只有 HEAD 部分显示模型
        if (state.getValue(PART) != BedPart.HEAD) {
            return Shapes.empty();
        }

        int servings = state.getValue(SERVINGS);
        if (servings < 0 || servings >= ROTATED_SHAPES.length) {
            servings = 0;
        }

        Direction facing = state.getValue(FACING);
        int facingIndex = getFacingIndex(facing);

        return ROTATED_SHAPES[servings][facingIndex][0];
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(PART) == BedPart.HEAD ? super.getOcclusionShape(state, level, pos) : Shapes.empty();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(PART) == BedPart.HEAD ? getShape(state, level, pos, CollisionContext.empty()) : Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
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

    // ================ 方块状态定义 ================

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS, PART);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    // ================ 形状旋转工具 ================

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

    // ================ 从JSON文件转换的形状定义方法 ================

    // stage 0 - 完整形状
    static VoxelShape makeShape0() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 0.625, 0.8125, 0.625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 1.0, 0.8125, 0.625, 1.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.375, 1.625, 0.5, 0.625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.3125, 0.25, 0.75, 0.75, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.25, 0.25, 0.75, 0.3125, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.25, 0.25, 0.375, 0.3125, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.3125, 0.1875, 0.625, 0.5, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.375, 0.3125, 0.8125, 0.75, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.375, 0.3125, 0.25, 0.75, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.25, 0.28125, 0.625, 0.3125, 0.65625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.5, 0.375, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.5, 0.875, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.25, 0.25, 0.5625, 0.375, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.46875, 0.13125, 0.0625, 0.46875, 0.31875, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 0.4625, 0.8875, 0.3125, 0.9625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 0.46875, 0.3, 0.3125, 1.03125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        return shape;
    }

    // stage 3 - servings 10-12
    static VoxelShape makeShape3() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 0.625, 0.8125, 0.625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 1.0, 0.8125, 0.625, 1.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.375, 1.625, 0.5, 0.625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.25, 0.5625, 0.625, 0.4375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.5, 0.375, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.5, 0.875, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 0.4625, 0.8875, 0.3125, 0.9625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 0.46875, 0.3, 0.3125, 1.03125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        return shape;
    }

    // stage 4 - servings 9
    static VoxelShape makeShape4() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.5, 0.375, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 0.46875, 0.3, 0.3125, 1.03125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.0625, 0.375, 0.9375, 0.1875, 0.75), BooleanOp.OR);
        return shape;
    }

    // stage 5 - servings 8
    static VoxelShape makeShape5() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1875, 0.1875, 0.1875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.125, 0.19375, 0.4375, 0.25, 0.31875), BooleanOp.OR);
        return shape;
    }

    // stage 6 - servings 7
    static VoxelShape makeShape6() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1875, 0.1875, 0.1875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.125, 0.19375, 0.4375, 0.25, 0.31875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.125, 0.38125, 0.4375, 0.25, 0.50625), BooleanOp.OR);
        return shape;
    }

    // stage 7 - servings 1-6
    static VoxelShape makeShape7() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.0625, 0.3125, 0.875, 0.1875, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.125, 0.125, 0.8125, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.46875, 0.0625, 0.375, 0.65625, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.51875, 0.025, 0.1875, 0.51875, 0.2125, 0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1625, 0.1875, 0.1875, 0.5375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 0.13125, 0.375, 0.1875, 0.50625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.19375, 0.4125, 0.25, 0.31875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.35, 0.4125, 0.25, 0.475), BooleanOp.OR);
        return shape;
    }

    // stage 13 - servings 0 (只剩骨架)
    static VoxelShape makeShape13() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.4375, 0.6875, 0.5625, 0.5, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.4375, 1.0, 0.5625, 0.5, 1.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 0.6875, 0.75, 0.5, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 1.0, 0.75, 0.5, 1.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.19375, 0.4125, 0.25, 0.31875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.35, 0.4125, 0.25, 0.475), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 0.13125, 0.375, 0.1875, 0.50625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1625, 0.1875, 0.1875, 0.5375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        return shape;
    }
}