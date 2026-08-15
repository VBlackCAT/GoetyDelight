package net.v_black_cat.goetydelight.block;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.ItemInteractionResult;
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
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.List;
import java.util.function.Supplier;

public class RoastLaowangBlock extends HorizontalDirectionalBlock {

    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 13);
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;

    public static final int EAR_CUT_SERVINGS_1 = 13;
    public static final int EAR_CUT_SERVINGS_2 = 12;
    public static final int BONEMEAL_DROP_MIN = 3;
    public static final int BONEMEAL_DROP_MAX = 8;

    private final List<Supplier<Item>> servingItems;

    private static final VoxelShape[][][] ROTATED_SHAPES = new VoxelShape[14][4][];

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            makeShape0(),
            makeShape3(),
            makeShape4(),
            makeShape5(),
            makeShape6(),
            makeShape7(),
            makeShape13()
    };

    public RoastLaowangBlock(Properties properties, List<Supplier<Item>> servingItems, boolean hasLeftovers) {
        super(properties);
        this.servingItems = servingItems;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SERVINGS, getMaxServings())
                .setValue(PART, BedPart.HEAD));
    }

    public static final MapCodec<RoastLaowangBlock> CODEC = simpleCodec(p -> new RoastLaowangBlock(p, List.of(), true));

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    static {
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        // 将碰撞/轮廓箱限制在服务端交互校验允许的范围内（距离方块中心不超过 1 格），
        // 避免命中点落在越界外沿时被 handleUseItemOn 拒绝。
        VoxelShape BOUNDS = Shapes.box(-0.5, -1.0, -0.5, 1.5, 2.0, 1.5);

        for (int servings = 0; servings < 14; servings++) {
            for (int i = 0; i < facings.length; i++) {
                Direction facing = facings[i];
                VoxelShape originalShape = getShapeForServings(servings);
                VoxelShape boundedShape = Shapes.join(originalShape, BOUNDS, BooleanOp.AND);
                ROTATED_SHAPES[servings][i] = new VoxelShape[]{rotateVoxelShapeStatic(boundedShape, facing)};
            }
        }
    }

    private static VoxelShape getShapeForServings(int servings) {
        if (servings >= 11 && servings <= 13) {
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

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
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
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void breakEntireStructure(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        BedPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos headPos = part == BedPart.HEAD ? pos : pos.relative(facing.getOpposite());
        BlockPos footPos = headPos.relative(facing);

        if (level.getBlockState(headPos).getBlock() instanceof RoastLaowangBlock) {
            BlockState oldState = level.getBlockState(headPos);
            level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 3);
            if (player != null) {
                level.levelEvent(player, 2001, headPos, Block.getId(oldState));
            }
        }

        if (level.getBlockState(footPos).getBlock() instanceof RoastLaowangBlock) {
            BlockState oldState = level.getBlockState(footPos);
            level.setBlock(footPos, Blocks.AIR.defaultBlockState(), 3);
            if (player != null) {
                level.levelEvent(player, 2001, footPos, Block.getId(oldState));
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(PART) != BedPart.HEAD) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 客户端只返回结果，服务端执行真正的取餐/切耳/破坏逻辑，
        // 避免客户端预测改动背包与方块状态，从而消除 "特殊角度不消耗" 的复制漏洞。
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int servings = state.getValue(SERVINGS);
        ItemStack heldItem = player.getItemInHand(hand);

        boolean isKnife = heldItem.is(ModTags.Items.KNIVES);
        boolean isBowl = heldItem.is(Items.BOWL);

        if (requiresKnife(servings)) {
            if (isKnife) {
                return cutEar(level, pos, state, player, hand);
            } else {
                player.displayClientMessage(getUseKnifeMessage(), true);
                return ItemInteractionResult.SUCCESS;
            }
        }

        if (servings == 0) {
            RandomSource random = level.getRandom();
            int boneMealCount = BONEMEAL_DROP_MIN + random.nextInt(BONEMEAL_DROP_MAX - BONEMEAL_DROP_MIN + 1);
            Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.5, pos.getZ(),
                    new ItemStack(Items.BONE_MEAL, boneMealCount));
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            breakEntireStructure(level, pos, state, player);
            return ItemInteractionResult.SUCCESS;
        }

        if (isBowl) {
            return takeServing(level, pos, state, player, hand);
        }

        player.displayClientMessage(getUseBowlMessage(new ItemStack(Items.BOWL)), true);
        return ItemInteractionResult.SUCCESS;
    }

    protected ItemInteractionResult cutEar(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        int servings = state.getValue(SERVINGS);

        if (!requiresKnife(servings)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int newServings = servings - 1;
        Direction facing = state.getValue(FACING);
        BlockPos headPos = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(facing.getOpposite());
        BlockPos footPos = headPos.relative(facing);

        BlockState headState = level.getBlockState(headPos);
        BlockState footState = level.getBlockState(footPos);

        if (headState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(headPos, headState.setValue(SERVINGS, newServings), 3);
        }
        if (footState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(footPos, footState.setValue(SERVINGS, newServings), 3);
        }

        ItemStack earStack = servings == EAR_CUT_SERVINGS_1
                ? new ItemStack(servingItems.get(0).get(), 1)
                : new ItemStack(servingItems.get(0).get());

        giveOrDropItem(player, earStack, level, pos);

        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.5F, 0.8F);

        return ItemInteractionResult.SUCCESS;
    }

    protected ItemInteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        int servings = state.getValue(SERVINGS);
        Direction facing = state.getValue(FACING);
        BlockPos headPos = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(facing.getOpposite());
        BlockPos footPos = headPos.relative(facing);

        ItemStack servingItem = getServingItem(state);

        int newServings = servings - 1;
        BlockState headState = level.getBlockState(headPos);
        BlockState footState = level.getBlockState(footPos);

        if (headState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(headPos, headState.setValue(SERVINGS, newServings), 3);
        }
        if (footState.getBlock() instanceof RoastLaowangBlock) {
            level.setBlock(footPos, footState.setValue(SERVINGS, newServings), 3);
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (!player.isCreative()) {
            heldItem.shrink(1);
        }

        if (!player.getInventory().add(servingItem.copy())) {
            player.drop(servingItem.copy(), false);
        }

        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == BedPart.HEAD ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS, PART);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

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
