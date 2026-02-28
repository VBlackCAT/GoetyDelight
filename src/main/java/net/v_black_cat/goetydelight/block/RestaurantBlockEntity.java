package net.v_black_cat.goetydelight.block;

import com.Polarice3.Goety.common.items.WaystoneItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi;
import net.v_black_cat.goetydelight.screen.RestaurantMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public class RestaurantBlockEntity extends BlockEntity implements MenuProvider {

    private BlockPos[] rangeMarker = new BlockPos[2];
    private BlockPos[] diningAreaRange = new BlockPos[2];
    private BlockPos[] pickupAreaRange = new BlockPos[2];
    private BlockPos[] entranceAreaRange = new BlockPos[2];
    private BlockPos[] exitAreaRange = new BlockPos[2];
    private ItemStackHandler inventory = this.createHandler();
    private static final int[] RANGE_MARKER_INVENTORY_INDEX = {0,1};
    private static final int[] DINING_AREA_RANGE_INVENTORY_INDEX = {2,3};
    private static final int[] PICKUP_AREA_RANGE_INVENTORY_INDEX = {4,5};
    private static final int[] ENTRANCE_AREA_RANGE_INVENTORY_INDEX = {6,7};
    private static final int[] EXIT_AREA_RANGE_INVENTORY_INDEX = {8,9};
    private static final int INVENTORY_SIZE = 10;
    public boolean shouldRenderArea;
    private static final Set<GlobalPos> restaurantPositions = new HashSet<>();
    private ArrayList<ItemStack> dishesList = new ArrayList<ItemStack>();
    private float restaurantExperience = 0.0f;
    private boolean isOpen = false;


public void setOpen(boolean open) {
    this.isOpen = open;
    this.setChanged();
    if (this.level != null) {
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
    }
}

    public ArrayList<ItemStack> getDishesList() {
        return dishesList;
    }

    public float getRestaurantExperience() {
        return restaurantExperience;
    }

    public void setRestaurantExperience(float experience) {
        this.restaurantExperience = experience;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
    }

    public static float getTotalExpForLevel(int level) {
        if (level <= 1) {
            return 0.0f;
        }
        int n = level - 1;
        return 1500.0f * n * (n + 1) / 2;
    }

    public int getRestaurantLevel() {
        float totalExp = restaurantExperience;
        int level = 1;
        float requiredExp = 0;

        while (true) {
            requiredExp += level * 1500;
            if (totalExp < requiredExp) {
                return level;
            }
            level++;
        }
    }

    public void addRestaurantExperience(float experience) {
        setRestaurantExperience(this.restaurantExperience + experience);
    }
    public static void addRestaurantPosition(Level level, BlockPos pos) {
        restaurantPositions.add(GlobalPos.of(level.dimension(), pos));
    }

    public static void removeRestaurantPosition(Level level, BlockPos pos) {
        restaurantPositions.remove(GlobalPos.of(level.dimension(), pos));
    }

    public static boolean isRestaurantPosition(Level level, BlockPos pos) {
        return restaurantPositions.contains(GlobalPos.of(level.dimension(), pos));
    }

    public static java.util.Set<GlobalPos> getRestaurantPositions() {
        return restaurantPositions;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level instanceof ServerLevel serverLevel) {
            removeRestaurantPosition(serverLevel, this.worldPosition);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (this.level instanceof ServerLevel serverLevel) {
            addRestaurantPosition(serverLevel, this.worldPosition);
        }
    }
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (this.level instanceof ServerLevel serverLevel) {
            removeRestaurantPosition(serverLevel, this.worldPosition);
        }
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    private ContainerData dataAccess;
    private final int NUM_DATA_VALUES = 4;
    public void setDiningArea(BlockPos start, BlockPos end) {
        this.diningAreaRange[0] = start;
        this.diningAreaRange[1] = end;
    }

    public BlockPos[] getRangeMarker() {
        return rangeMarker;
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
    public RestaurantBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.dataAccess = createDataAccess();
    }

    private @NotNull ContainerData createDataAccess() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                switch (index) {
                    default:
                        return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                }
                setChanged();
            }

            @Override
            public int getCount() {
                return NUM_DATA_VALUES;
            }
        };
    }

    private void executeCommand(int value) {

        switch (value) {
        }

    }

    public ArrayList<ItemStack> findFoodItems() {
        ArrayList<ItemStack> foodList = new ArrayList<>();
        int range = 8;
        BlockPos center = this.worldPosition;

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(checkPos)) continue;
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    if (blockEntity != null) {
                        LazyOptional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
                        capability.ifPresent(handler -> {
                            for (int slot = 0; slot < handler.getSlots(); slot++) {
                                ItemStack stackInSlot = handler.getStackInSlot(slot).copy();
                                if (!stackInSlot.isEmpty()) {
                                    if (stackInSlot.getItem().isEdible()) {
                                        ItemStack foodStack = stackInSlot.copy();
                                        foodStack.setCount(1);
                                        boolean isDuplicate = false;
                                        for (ItemStack existingStack : foodList) {
                                            if (ItemStack.matches(foodStack, existingStack)) {
                                                isDuplicate = true;
                                                break;
                                            }
                                        }
                                        if (!isDuplicate) {
                                            foodList.add(foodStack);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
        return foodList;
    }

    RestaurantBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntities.RESTAURANT_BE.get(), pos, blockState);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, RestaurantBlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.getRandom().nextInt(100) == 0) {
                blockEntity.affectNearbyEntities();
            }
        }


    }






    private void affectNearbyEntities() {
        int restaurantLevel = getRestaurantLevel();
        int range = Math.min(32 * restaurantLevel, 256);
        float chance = 0.01f * restaurantLevel;

        if (level instanceof ServerLevel serverLevel) {
            List<Entity> nearbyEntities = serverLevel.getEntitiesOfClass(Entity.class, 
                new AABB(worldPosition).inflate(range));

            for (Entity entity : nearbyEntities) {
                if (serverLevel.getRandom().nextFloat() < chance) {
                    if (entity instanceof ICustomerEntity customer) {
                       customer.goetyDelight$enterCustomerModeAndCheckCoolDown();
                    }
                }
            }
        }
    }




    private ItemStackHandler createHandler() {
        return new ItemStackHandler(INVENTORY_SIZE){
            @Override
            protected void onContentsChanged(int slot) {
                RestaurantBlockEntity.this.inventoryChanged();
            }
        };
    }

    private void inventoryChanged() {
        super.setChanged();
    }
    public void updateDishesList() {
        this.dishesList = findFoodItems();
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
    }


    public void updateRangesFromInventory() {
        updateRangeMarker();
        updateDiningAreaRange();
        updatePickupAreaRange();
        updateEntranceAreaRange();
        updateExitAreaRange();
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
    }
    private void updateRangeMarker() {
        ItemStack stack1 = inventory.getStackInSlot(RANGE_MARKER_INVENTORY_INDEX[0]);
        ItemStack stack2 = inventory.getStackInSlot(RANGE_MARKER_INVENTORY_INDEX[1]);
        
        BlockPos start = WaystoneItem.getBlockPos(stack1);
        BlockPos end = WaystoneItem.getBlockPos(stack2);
        
        this.rangeMarker[0] = start;
        this.rangeMarker[1] = end;
    }

    private void updateDiningAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(DINING_AREA_RANGE_INVENTORY_INDEX[0]);
        ItemStack stack2 = inventory.getStackInSlot(DINING_AREA_RANGE_INVENTORY_INDEX[1]);
        BlockPos start = WaystoneItem.getBlockPos(stack1);
        BlockPos end = WaystoneItem.getBlockPos(stack2);
        setDiningArea(start, end);
    }

    private void updatePickupAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(PICKUP_AREA_RANGE_INVENTORY_INDEX[0]);
        ItemStack stack2 = inventory.getStackInSlot(PICKUP_AREA_RANGE_INVENTORY_INDEX[1]);
        BlockPos start = WaystoneItem.getBlockPos(stack1);
        BlockPos end = WaystoneItem.getBlockPos(stack2);
        setPickupArea(start, end);
    }

    private void updateEntranceAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(ENTRANCE_AREA_RANGE_INVENTORY_INDEX[0]);
        ItemStack stack2 = inventory.getStackInSlot(ENTRANCE_AREA_RANGE_INVENTORY_INDEX[1]);
        BlockPos start = WaystoneItem.getBlockPos(stack1);
        BlockPos end = WaystoneItem.getBlockPos(stack2);
        setEntranceArea(start, end);
    }

    private void updateExitAreaRange() {
        ItemStack stack1 = inventory.getStackInSlot(EXIT_AREA_RANGE_INVENTORY_INDEX[0]);
        ItemStack stack2 = inventory.getStackInSlot(EXIT_AREA_RANGE_INVENTORY_INDEX[1]);
        BlockPos start = WaystoneItem.getBlockPos(stack1);
        BlockPos end = WaystoneItem.getBlockPos(stack2);
        setExitArea(start, end);
    }


    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    private static Vindicator trySpawnVindicator(ServerLevel level, BlockPos[] range,RestaurantBlockEntity blockEntity) {

        RandomSource random = level.getRandom();
        int x = Mth.randomBetweenInclusive(random, Math.min(range[0].getX(), range[1].getX()), Math.max(range[0].getX(), range[1].getX()));
        int z = Mth.randomBetweenInclusive(random, Math.min(range[0].getZ(), range[1].getZ()), Math.max(range[0].getZ(), range[1].getZ()));

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos spawnPos = new BlockPos(x, y, z);

        return SpawnUtils.safeSpawnEntity(EntityType.VINDICATOR, level, spawnPos, MobSpawnType.EVENT);

    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Restaurant");
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new RestaurantMenu(i, inventory, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", this.inventory.serializeNBT());
        
        saveAreaRange(tag, "RangeMarker", this.rangeMarker);
        saveAreaRange(tag, "DiningAreaRange", this.diningAreaRange);
        saveAreaRange(tag, "PickupAreaRange", this.pickupAreaRange);
        saveAreaRange(tag, "EntranceAreaRange", this.entranceAreaRange);
        saveAreaRange(tag, "ExitAreaRange", this.exitAreaRange);
        
        tag.putBoolean("ShouldRenderArea", this.shouldRenderArea);
        tag.putFloat("RestaurantExperience", this.restaurantExperience);

        CompoundTag dishesListTag = new CompoundTag();
        int index = 0;
        for (ItemStack dish : this.dishesList) {
            dishesListTag.put("dish_" + index, dish.save(new CompoundTag()));
            index++;
        }
        tag.put("DishesList", dishesListTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ItemStackHandler tempHandler = new ItemStackHandler(INVENTORY_SIZE);
        CompoundTag inventoryTag = tag.getCompound("Inventory");

        if (!inventoryTag.isEmpty()) {
            tempHandler.deserializeNBT(inventoryTag);
            for (int i = 0; i < Math.min(tempHandler.getSlots(), INVENTORY_SIZE); i++) {
                this.inventory.setStackInSlot(i, tempHandler.getStackInSlot(i));
            }
        }
        
        loadAreaRange(tag, "RangeMarker", this.rangeMarker);
        loadAreaRange(tag, "DiningAreaRange", this.diningAreaRange);
        loadAreaRange(tag, "PickupAreaRange", this.pickupAreaRange);
        loadAreaRange(tag, "EntranceAreaRange", this.entranceAreaRange);
        loadAreaRange(tag, "ExitAreaRange", this.exitAreaRange);
        
        if (tag.contains("ShouldRenderArea")) {
            this.shouldRenderArea = tag.getBoolean("ShouldRenderArea");
        }
        
        if (tag.contains("RestaurantExperience")) {
            this.restaurantExperience = tag.getFloat("RestaurantExperience");
        }
        if (tag.contains("DishesList")) {
            CompoundTag dishesListTag = tag.getCompound("DishesList");
            this.dishesList.clear();
            for (String key : dishesListTag.getAllKeys()) {
                CompoundTag itemTag = dishesListTag.getCompound(key);
                ItemStack dish = ItemStack.of(itemTag);
                if (!dish.isEmpty()) {
                    this.dishesList.add(dish);
                }
            }
        }

    }

    private void saveAreaRange(CompoundTag tag, String key, BlockPos[] range) {
        if (range[0] != null && range[1] != null) {
            tag.putLongArray(key, new long[]{
                range[0].asLong(),
                range[1].asLong()
            });
        }
    }

    private void loadAreaRange(CompoundTag tag, String key, BlockPos[] range) {
        if (tag.contains(key)) {
            long[] areaData = tag.getLongArray(key);
            if (areaData.length == 2) {
                range[0] = BlockPos.of(areaData[0]);
                range[1] = BlockPos.of(areaData[1]);
            }
        }
    }

    public void switchRenderArea() {
        shouldRenderArea = !shouldRenderArea;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
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






