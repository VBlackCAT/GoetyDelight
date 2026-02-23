package net.v_black_cat.goetydelight.block;

import com.Polarice3.Goety.common.items.WaystoneItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.v_black_cat.goetydelight.screen.RestaurantMenu;

import javax.annotation.Nullable;

import static com.Polarice3.Goety.common.items.ModItems.WAYSTONE;

public class RestaurantBlockEntity extends BlockEntity implements MenuProvider {
    private BlockPos[] rangeMarker = new BlockPos[2];
    private BlockPos[] diningAreaRange = new BlockPos[2];
    private BlockPos[] pickupAreaRange = new BlockPos[2];
    private BlockPos[] entranceAreaRange = new BlockPos[2];  // 入口区域
    private BlockPos[] exitAreaRange = new BlockPos[2];      // 出口区域
    private ItemStackHandler inventory = this.createHandler();
    private static final int[] RANGE_MARKER_INVENTORY_INDEX = {1,2};
    private static final int[] DINING_AREA_RANGE_INVENTORY_INDEX = {3,4};
    private static final int[] PICKUP_AREA_RANGE_INVENTORY_INDEX = {5,6};
    private static final int[] ENTRANCE_AREA_RANGE_INVENTORY_INDEX = {7,8};  // 入口区域物品槽位索引
    private static final int[] EXIT_AREA_RANGE_INVENTORY_INDEX = {9,10};     // 出口区域物品槽位索引
    private static final int INVENTORY_SIZE = 10;
    private ContainerData dataAccess;
    public void setDiningArea(BlockPos start, BlockPos end) {
        this.diningAreaRange[0] = start;
        this.diningAreaRange[1] = end;
    }

    public void setPickupArea(BlockPos start, BlockPos end) {
        this.pickupAreaRange[0] = start;
        this.pickupAreaRange[1] = end;
    }

    public BlockPos[] getDiningAreaRange() {
        return diningAreaRange;
    }

    public BlockPos[] getPickupAreaRange() {
        return pickupAreaRange;
    }

    public void setEntranceArea(BlockPos start, BlockPos end) {
        this.entranceAreaRange[0] = start;
        this.entranceAreaRange[1] = end;
    }

    public void setExitArea(BlockPos start, BlockPos end) {
        this.exitAreaRange[0] = start;
        this.exitAreaRange[1] = end;
    }

    public BlockPos[] getEntranceAreaRange() {
        return entranceAreaRange;
    }

    public BlockPos[] getExitAreaRange() {
        return exitAreaRange;
    }

    public RestaurantBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, ItemStackHandler inventory, ContainerData dataAccess) {
        super(type, pos, blockState);
        this.inventory = inventory;
        this.dataAccess = dataAccess;
    }


    public RestaurantBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        // 初始化 dataAccess
        this.dataAccess = new ContainerData() {
            @Override
            public int get(int index) {
                return 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }



    RestaurantBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntities.RESTAURANT_BE.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RestaurantBlockEntity blockEntity) {


    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(INVENTORY_SIZE){
            @Override
            protected void onContentsChanged(int slot) {
                RestaurantBlockEntity.this.updateRangesFromInventory(slot);
            }
        };
    }
    private void updateRangesFromInventory(int slot) {
        if (slot == RANGE_MARKER_INVENTORY_INDEX[0] - 1 || slot == RANGE_MARKER_INVENTORY_INDEX[1] - 1) {
            updateRangeMarker();
        } else if (slot == DINING_AREA_RANGE_INVENTORY_INDEX[0] - 1 || slot == DINING_AREA_RANGE_INVENTORY_INDEX[1] - 1) {
            updateDiningAreaRange();
        } else if (slot == PICKUP_AREA_RANGE_INVENTORY_INDEX[0] - 1 || slot == PICKUP_AREA_RANGE_INVENTORY_INDEX[1] - 1) {
            updatePickupAreaRange();
        } else if (slot == ENTRANCE_AREA_RANGE_INVENTORY_INDEX[0] - 1 || slot == ENTRANCE_AREA_RANGE_INVENTORY_INDEX[1] - 1) {
            updateEntranceAreaRange();
        } else if (slot == EXIT_AREA_RANGE_INVENTORY_INDEX[0] - 1 || slot == EXIT_AREA_RANGE_INVENTORY_INDEX[1] - 1) {
            updateExitAreaRange();
        }
    }
    private BlockPos parsePositionFromItem(ItemStack stack) {
        GlobalPos position = WaystoneItem.getPosition(stack);
        if (position != null && position.dimension().equals(this.getLevel().dimension())) {
            return position.pos();
        }
        return BlockPos.ZERO;
    }
    private void updateRangeMarker() {
        ItemStack stack1 = inventory.getStackInSlot(RANGE_MARKER_INVENTORY_INDEX[0] - 1);
        ItemStack stack2 = inventory.getStackInSlot(RANGE_MARKER_INVENTORY_INDEX[1] - 1);
        
        BlockPos start = parsePositionFromItem(stack1);
        BlockPos end = parsePositionFromItem(stack2);
        
        this.rangeMarker[0] = start;
        this.rangeMarker[1] = end;
    }

    private void updateDiningAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(DINING_AREA_RANGE_INVENTORY_INDEX[0] - 1);
        ItemStack stack2 = inventory.getStackInSlot(DINING_AREA_RANGE_INVENTORY_INDEX[1] - 1);
        BlockPos start = parsePositionFromItem(stack1);
        BlockPos end = parsePositionFromItem(stack2);
        setDiningArea(start, end);
    }

    private void updatePickupAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(PICKUP_AREA_RANGE_INVENTORY_INDEX[0] - 1);
        ItemStack stack2 = inventory.getStackInSlot(PICKUP_AREA_RANGE_INVENTORY_INDEX[1] - 1);
        BlockPos start = parsePositionFromItem(stack1);
        BlockPos end = parsePositionFromItem(stack2);
        setPickupArea(start, end);
    }

    private void updateEntranceAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(ENTRANCE_AREA_RANGE_INVENTORY_INDEX[0] - 1);
        ItemStack stack2 = inventory.getStackInSlot(ENTRANCE_AREA_RANGE_INVENTORY_INDEX[1] - 1);
        BlockPos start = parsePositionFromItem(stack1);
        BlockPos end = parsePositionFromItem(stack2);
        setEntranceArea(start, end);
    }

    private void updateExitAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(EXIT_AREA_RANGE_INVENTORY_INDEX[0] - 1);
        ItemStack stack2 = inventory.getStackInSlot(EXIT_AREA_RANGE_INVENTORY_INDEX[1] - 1);
        BlockPos start = parsePositionFromItem(stack1);
        BlockPos end = parsePositionFromItem(stack2);
        setExitArea(start, end);
    }




    private static void trySpawnVindicator(ServerLevel level, BlockPos[] range,RestaurantBlockEntity blockEntity) {

        RandomSource random = level.getRandom();
        int x = Mth.randomBetweenInclusive(random, Math.min(range[0].getX(), range[1].getX()), Math.max(range[0].getX(), range[1].getX()));
        int z = Mth.randomBetweenInclusive(random, Math.min(range[0].getZ(), range[1].getZ()), Math.max(range[0].getZ(), range[1].getZ()));

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawnPos = new BlockPos(x, y, z);

        Vindicator vindicator = SpawnUtils.safeSpawnEntity(EntityType.VINDICATOR, level, spawnPos, MobSpawnType.EVENT);
        if (vindicator != null) {
            vindicator.goalSelector.removeAllGoals(goal -> true);
            vindicator.targetSelector.removeAllGoals(goal -> true);
            vindicator.getBrain().removeAllBehaviors();
            // 添加前往用餐区域的目标行为
            if (blockEntity.diningAreaRange[0] != null && blockEntity.diningAreaRange[1] != null) {
                BlockPos diningTarget = new BlockPos(
                    Mth.randomBetweenInclusive(random,
                        Math.min(blockEntity.diningAreaRange[0].getX(), blockEntity.diningAreaRange[1].getX()),
                        Math.max(blockEntity.diningAreaRange[0].getX(), blockEntity.diningAreaRange[1].getX())),
                    blockEntity.diningAreaRange[0].getY(),
                    Mth.randomBetweenInclusive(random,
                        Math.min(blockEntity.diningAreaRange[0].getZ(), blockEntity.diningAreaRange[1].getZ()),
                        Math.max(blockEntity.diningAreaRange[0].getZ(), blockEntity.diningAreaRange[1].getZ()))
                );

                // 添加移动到用餐区域的目标
                vindicator.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.MoveToBlockGoal(vindicator, 1.0D, 10) {
                    @Override
                    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
                        return pos.equals(diningTarget);
                    }

                    @Override
                    protected boolean findNearestBlock() {
                        this.blockPos = diningTarget;
                        return true;
                    }
                });
            }
        }
        }

    @Override
    public Component getDisplayName() {
        return Component.literal("Restaurant");
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        // 确保 dataAccess 不为 null
        if (this.dataAccess == null) {
            this.dataAccess = new ContainerData() {
                @Override
                public int get(int index) {
                    return 0;
                }

                @Override
                public void set(int index, int value) {
                }

                @Override
                public int getCount() {
                    return 4;
                }
            };
        }
        return new RestaurantMenu(i, inventory, this, this.dataAccess);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }

    class SpawnUtils {

        /**
         * 在指定位置安全地生成一个生物
         * @param entityType 生物类型
         * @param level      世界
         * @param pos        目标坐标
         * @param spawnType  生成类型（例如 MobSpawnType.EVENT 或 NATURAL）
         * @return 生成成功返回实体，失败返回 null
         */
        @Nullable
        public static <T extends Mob> T safeSpawnEntity(EntityType<T> entityType, ServerLevel level, BlockPos pos, MobSpawnType spawnType) {
            if (!isSpaceEmpty(level, pos, entityType)) {
                return null;
            }

            SpawnPlacements.Type placementType = SpawnPlacements.getPlacementType(entityType);
            if (!SpawnPlacements.checkSpawnRules(entityType, level, spawnType, pos, level.random)) {
                return null;
            }

            T entity = entityType.create(level);
            if (entity == null) return null;

            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;
            entity.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);

            if (!level.noCollision(entity) || !level.isUnobstructed(entity)) {
                entity.discard();
                return null;
            }

            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), spawnType, null, null);

            if (level.addFreshEntity(entity)) {
                return entity;
            }

            return null;
        }

        /**
         * 模拟 NaturalSpawner 中的空位检查逻辑
         */
        private static boolean isSpaceEmpty(ServerLevel level, BlockPos pos, EntityType<?> type) {
            BlockState footState = level.getBlockState(pos);
            BlockState headState = level.getBlockState(pos.above());
            if (footState.isRedstoneConductor(level, pos) || headState.isRedstoneConductor(level, pos.above())) {
                return false;
            }
            if (!footState.getFluidState().isEmpty()) {
                return false;
            }

            return true;
        }
    }

    }






