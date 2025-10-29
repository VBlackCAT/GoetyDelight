package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class BoatStuffedRoastedWardenBlock extends FeastBlock {

     public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 7);
     public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
     // 预先计算所有朝向的碰撞箱
     private static final VoxelShape[][][] ROTATED_SHAPES = new VoxelShape[8][4][]; // [servings][facing]
     private final List<Supplier<Item>> servingItems;

     private Part getPartFromOffset(int dx, int dz) {
          if (dx == -1 && dz == -1) return Part.NORTH_WEST;
          if (dx == -1 && dz == 0) return Part.NORTH;
          if (dx == -1 && dz == 1) return Part.NORTH_EAST;
          if (dx == 0 && dz == -1) return Part.WEST;
          if (dx == 0 && dz == 1) return Part.EAST;
          if (dx == 1 && dz == -1) return Part.SOUTH_WEST;
          if (dx == 1 && dz == 0) return Part.SOUTH;
          if (dx == 1 && dz == 1) return Part.SOUTH_EAST;
          return Part.CENTER; 
     }
     public enum Part implements StringRepresentable {
          CENTER("center", 0, 0),
          NORTH_WEST("north_west", -1, -1),
          NORTH("north", -1, 0),
          NORTH_EAST("north_east", -1, 1),
          WEST("west", 0, -1),
          EAST("east", 0, 1),
          SOUTH_WEST("south_west", 1, -1),
          SOUTH("south", 1, 0),
          SOUTH_EAST("south_east", 1, 1);

          private final String name;
          public final int dx; 
          public final int dz; 

          Part(String name, int dx, int dz) {
               this.name = name;
               this.dx = dx;
               this.dz = dz;
          }

          @Override
          public String getSerializedName() {
               return name;
          }
     }
     @Override
     public ItemStack getServingItem(BlockState state) {
          int servings = state.getValue(SERVINGS);
          // 根据剩余份数选择不同的物品
          int itemIndex = (getMaxServings() - servings) % servingItems.size();
          return new ItemStack(servingItems.get(itemIndex).get());
     }
     @Override
     public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
          super.setPlacedBy(level, pos, state, placer, stack);

          if (!level.isClientSide) {
               
               for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                         if (dx == 0 && dz == 0) continue;

                         BlockPos partPos = pos.offset(dx, 0, dz);
                         Part part = getPartFromOffset(dx, dz);
                         
                         BlockState partState = this.defaultBlockState()
                                 .setValue(PART, part)
                                 .setValue(FACING, state.getValue(FACING))
                                 .setValue(SERVINGS, state.getValue(SERVINGS));

                         level.setBlock(partPos, partState, 3);
                    }
               }
          }
     }


     private BlockPos getCenterPos(BlockPos pos, Part part) {
          return pos.offset(-part.dx, 0, -part.dz); 
     }

     @Override
     public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
          if (state.getValue(PART) == Part.CENTER) {
               super.playerDestroy(level, player, pos, state, blockEntity, tool);
          }
          
     }

     
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            makeShape0(),
            makeShape1(),
            makeShape2(),
            makeShape3(),
            makeShape4(),
            makeShape5(),
            makeShape6(),
            makeShape7()
    };

     // 静态初始化块，在类加载时预计算所有旋转
     static {
          // 定义四个基本朝向
          Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

          for (int servings = 0; servings < 8; servings++) {
               for (int i = 0; i < facings.length; i++) {
                    Direction facing = facings[i];
                    VoxelShape originalShape = SHAPES[servings];
                    ROTATED_SHAPES[servings][i] = new VoxelShape[]{rotateVoxelShapeStatic(originalShape, facing)};
               }
          }
     }

     static VoxelShape makeShape0(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          return shape;
     }

     static VoxelShape makeShape1(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.125, 0.125, 1.375, 0.8125, 1.4375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.1875, 0.8125, 0.375, 1.25, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.25, 0.8125, 0.5625, 1.3125, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.25, 0.8125, 0.8125, 1.3125, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.1875, 0.8125, 1, 1.25, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.4375, 1.0625, 0.625, 1.0625), BooleanOp.OR);
          return shape;
     }

     static VoxelShape makeShape2(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.0625, 0.125, 0.125, 1.0625, 0.8125, 1.4375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.375, 0.875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.5625, 1, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.8125, 1, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 1, 0.875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.375, 0.1875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.5625, 0.0625, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.8125, 0.0625, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 1, 0.1875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.4375, 0.8125, 1.3125, 1.0625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.625, 0, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1, 0.125, -0.625, 1.625, 0.1875, -0.3125), BooleanOp.OR);
          return shape;
     }

     static VoxelShape makeShape3(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.1875, 1.5625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.0625, 0.125, 0.125, 1.0625, 0.8125, 1.4375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.375, 0.875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.5625, 1, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.8125, 1, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 1, 0.875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.375, 0.1875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.5625, 0.0625, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.8125, 0.0625, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 1, 0.1875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.4375, 0.8125, 1.3125, 1.0625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.625, 0, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1, 0.125, -0.625, 1.625, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.625, 1.4375, 0.5, -0.25), BooleanOp.OR);
          return shape;
     }

     static VoxelShape makeShape4(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.5625, 0.125, -0.1875, -0.0625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.1875, 1.5625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.0625, 0.125, 0.125, 1.0625, 0.8125, 1.4375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.375, 0.875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.5625, 1, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.8125, 1, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 1, 0.875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.375, 0.1875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.5625, 0.0625, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.8125, 0.0625, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 1, 0.1875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.4375, 0.8125, 1.3125, 1.0625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.625, 0, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1, 0.125, -0.625, 1.625, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.4375, 0.125, -0.625, -0.0625, 0.5, -0.25), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.625, 1.4375, 0.5, -0.25), BooleanOp.OR);
          return shape;
     }

     static VoxelShape makeShape5(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.5625, 0.125, -0.1875, -0.0625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.1875, 1.5625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.0625, 0.125, 0.125, 1.0625, 0.8125, 1.4375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.375, 0.875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.5625, 1, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.8125, 1, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 1, 0.875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.375, 0.1875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.5625, 0.0625, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.8125, 0.0625, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 1, 0.1875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.4375, 0.8125, 1.3125, 1.0625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.625, 0.125, 1.4375, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.625, 0, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1, 0.125, -0.625, 1.625, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.4375, 0.125, -0.625, -0.0625, 0.5, -0.25), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.625, 1.4375, 0.5, -0.25), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.125, 0.6875, 1.0625, 1.5, 1.0625, 1.4375), BooleanOp.OR);

          return shape;
     }

     static VoxelShape makeShape6(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.5625, 0.125, -0.1875, -0.0625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.1875, 1.5625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.0625, 0.125, 0.125, 1.0625, 0.8125, 1.4375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.375, 0.875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.5625, 1, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.8125, 1, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 1, 0.875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.375, 0.1875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.5625, 0.0625, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.8125, 0.0625, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 1, 0.1875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.4375, 0.8125, 1.3125, 1.0625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.625, 0.125, 1.4375, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.4375, 0.625, 0.0625, -0.0625, 1, 0.875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.625, 0, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1, 0.125, -0.625, 1.625, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.4375, 0.125, -0.625, -0.0625, 0.5, -0.25), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.5, 0.6875, 1, -0.125, 1.0625, 1.375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.625, 1.4375, 0.5, -0.25), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.125, 0.6875, 1.0625, 1.5, 1.0625, 1.4375), BooleanOp.OR);

          return shape;
     }

     static VoxelShape makeShape7(){
          VoxelShape shape = Shapes.empty();
          shape = Shapes.join(shape, Shapes.box(-0.5, 0, -0.5, 1.5, 0.0625, 1.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.0625, -0.75, 1.75, 0.125, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.75, 1.625, 0.1875, -0.625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.75, 0.125, -0.75, -0.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, 1.625, 1.625, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.625, 0.125, -0.75, 1.75, 0.1875, 1.75), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.5625, 0.125, -0.1875, -0.0625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.1875, 1.5625, 0.625, 1.5625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.0625, 0.125, 0.125, 1.0625, 0.8125, 1.4375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.375, 0.875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.5625, 1, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.9375, 0.8125, 0.8125, 1, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 1, 0.875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 0.375, 0.1875, 1, 0.5), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.5625, 0.0625, 1, 0.6875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.8125, 0.8125, 0.0625, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.125, 0.8125, 1, 0.1875, 1, 1.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0.1875, 0.8125, 0.4375, 0.8125, 1.3125, 1.0625), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(0, 0.125, -0.5, 1, 1.125, 0.125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.625, 0.125, 1.4375, 1, 0.9375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.4375, 0.625, 0.0625, -0.0625, 1, 0.875), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.625, 0.125, -0.625, 0, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1, 0.125, -0.625, 1.625, 0.1875, -0.3125), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.4375, 0.125, -0.625, -0.0625, 0.5, -0.25), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(-0.5, 0.6875, 1, -0.125, 1.0625, 1.375), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.0625, 0.125, -0.625, 1.4375, 0.5, -0.25), BooleanOp.OR);
          shape = Shapes.join(shape, Shapes.box(1.125, 0.6875, 1.0625, 1.5, 1.0625, 1.4375), BooleanOp.OR);

          return shape;
     }
     public BoatStuffedRoastedWardenBlock(Properties properties, List<Supplier<Item>> servingItems, boolean hasLeftovers) {
          super(properties, () -> servingItems.get(0).get(), hasLeftovers);
          this.servingItems = servingItems;
         this.registerDefaultState(this.stateDefinition.any()
                  .setValue(FACING, Direction.NORTH)
                  .setValue(SERVINGS, getMaxServings())
                  .setValue(PART, Part.CENTER)); 
     }

     @Override
     public IntegerProperty getServingsProperty() {
          return SERVINGS;
     }

    @Override
    public int getMaxServings() {
        return 7;
    }

     @Nullable
     @Override
     public BlockState getStateForPlacement(BlockPlaceContext context) {
          BlockPos pos = context.getClickedPos();
          Level level = context.getLevel();

          
          for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos partPos = pos.offset(dx, 0, dz);
                    if (!level.getBlockState(partPos).canBeReplaced(context)) {
                         return null;
                    }
               }
          }

          return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
     }

     @Override
     public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
          if (!level.isClientSide() && shouldBreakStructure(level, currentPos, state)) {
               level.scheduleTick(currentPos, this, 1);
          }
          return state;
     }
     @Override
     public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
          return state.getValue(PART) == Part.CENTER ? getShape(state, level, pos, CollisionContext.empty()) : Shapes.empty();
     }

     @Override
     public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
          if (!level.isClientSide) {
               
               if (player.isCreative()) {
                    breakEntireStructure(level, pos, state, player);
               }
          }
          super.playerWillDestroy(level, pos, state, player);
     }

     @Override
     public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
          if (shouldBreakStructure(level, pos, state)) {
               breakEntireStructure(level, pos, state, null);
          }
     }
     private boolean shouldBreakStructure(LevelAccessor level, BlockPos pos, BlockState state) {
          Part part = state.getValue(PART);
          BlockPos centerPos = getCenterPos(pos, part);

          
          for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                    BlockPos checkPos = centerPos.offset(dx, 0, dz);
                    BlockState checkState = level.getBlockState(checkPos);

                    if (!(checkState.getBlock() instanceof BoatStuffedRoastedWardenBlock)) {
                         return true; 
                    }

                    
                    Part expectedPart = getPartFromOffset(dx, dz);
                    if (checkState.getValue(PART) != expectedPart) {
                         return true;
                    }
               }
          }
          return false;
     }
     private void breakEntireStructure(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
          Part part = state.getValue(PART);
          BlockPos centerPos = getCenterPos(pos, part);

          for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                    BlockPos partPos = centerPos.offset(dx, 0, dz);
                    BlockState partState = level.getBlockState(partPos);

                    if (partState.getBlock() instanceof BoatStuffedRoastedWardenBlock) {
                         level.setBlock(partPos, Blocks.AIR.defaultBlockState(), 3);
                         if (player != null) {
                              level.levelEvent(player, 2001, partPos, Block.getId(partState));
                         }
                    }
               }
          }
     }
     @Override
     public RenderShape getRenderShape(BlockState state) {
          return state.getValue(PART) == Part.CENTER ? RenderShape.MODEL : RenderShape.INVISIBLE;
     }
     @Override
     public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
          if (state.getValue(PART) != Part.CENTER) {
               return Shapes.empty();
          }

          int servings = state.getValue(SERVINGS);
          if (servings < 0 || servings >= SHAPES.length) {
               servings = 0;
          }

          Direction facing = state.getValue(FACING);
          int facingIndex = getFacingIndex(facing);

          // 直接从预计算数组中获取
          return ROTATED_SHAPES[servings][facingIndex][0];
     }
     @Override
     public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
          return state.getValue(PART) == Part.CENTER ? super.getOcclusionShape(state, level, pos) : Shapes.empty();
     }

     /**
      * 静态方法用于预计算旋转
      */
     private static VoxelShape rotateVoxelShapeStatic(VoxelShape shape, Direction facing) {
          if (facing == Direction.NORTH) {
               return shape;
          }

          return shape.toAabbs().stream()
                  .map(aabb -> rotateAABBStatic(aabb, facing))
                  .map(Shapes::create)
                  .reduce(Shapes.empty(), Shapes::or);
     }

     /**
      * 静态方法用于预计算AABB旋转
      */
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

     /**
      * 将Direction转换为索引
      */
     private int getFacingIndex(Direction facing) {
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
}