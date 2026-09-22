package net.v_black_cat.goetydelight.block;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.utils.SEHelper;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.v_black_cat.goetydelight.screen.CursedIngotPotMenu;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.block.entity.inventory.CookingPotItemHandler;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.ItemUtils;
import net.v_black_cat.goetydelight.util.TextUtils;

import static com.Polarice3.Goety.common.items.ModItems.SOUL_TRANSFER;

public class CursedIngotPotBlockEntity extends SyncedBlockEntity implements MenuProvider, HeatableBlockEntity, Nameable, RecipeHolder {
    public static final int MEAL_DISPLAY_SLOT = 6;
    public static final int CONTAINER_SLOT = 7;
    public static final int OUTPUT_SLOT = 8;
    public static final int SOUL_SOURCE_SLOT = 9; //图腾/宝石插槽
    public static final int INVENTORY_SIZE = 10;
    public static final Map<Item, Item> INGREDIENT_REMAINDER_OVERRIDES;

    private final ItemStackHandler inventory = this.createHandler();
    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(() -> {
        return new CookingPotItemHandler(this.inventory, Direction.UP);
    });
    private final LazyOptional<IItemHandler> outputHandler = LazyOptional.of(() -> {
        return new CookingPotItemHandler(this.inventory, Direction.DOWN);
    });

    // 【优化】进度改为 float：倍率加成不再靠"每 N tick 补 1"的取整 hack，1.2 倍速精确生效
    private float cookTime;
    private int cookTimeTotal;
    private ItemStack mealContainerStack;
    private Component customName;
    protected final ContainerData cookingPotData;
    private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker;

    // 【优化】配方缓存：只在库存变化 / 每 5 秒自愈时刷新，不再每 tick 全量匹配配方
    @Nullable
    private CookingPotRecipe cachedRecipe;
    // 【优化】输入状态缓存，省掉每 tick 遍历前 6 格
    private boolean hasInputCache;
    // 【优化】随配方一起刷新的衍生结果：省掉每 tick 拷贝结果栈与重复计算灵魂消耗
    private ItemStack cachedResultStack = ItemStack.EMPTY;
    private int cachedSoulCost;
    // 【优化】批量修改库存（加载 / 一次烹饪结算）期间挂起逐槽事件，结束后统一刷新一次
    private boolean deferInventoryEvents;

    public CursedIngotPotBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)ModBlockEntities.CURSED_INGOT_POT_BE.get(), pos, state);
        this.mealContainerStack = ItemStack.EMPTY;
        this.cookingPotData = this.createIntArray();
        this.usedRecipeTracker = new Object2IntOpenHashMap();
    }

    public static ItemStack getMealFromItem(ItemStack cookingPotStack) {
        if (!cookingPotStack.is((Item)ModItems.COOKING_POT.get())) {
            return ItemStack.EMPTY;
        } else {
            CompoundTag compound = cookingPotStack.getTagElement("BlockEntityTag");
            if (compound != null) {
                CompoundTag inventoryTag = compound.getCompound("Inventory");
                if (inventoryTag.contains("Items", 9)) {
                    ItemStackHandler handler = new ItemStackHandler();
                    handler.deserializeNBT(inventoryTag);
                    return handler.getStackInSlot(6);
                }
            }

            return ItemStack.EMPTY;
        }
    }

    public static void takeServingFromItem(ItemStack cookingPotStack) {
        if (cookingPotStack.is((Item)ModItems.COOKING_POT.get())) {
            CompoundTag compound = cookingPotStack.getTagElement("BlockEntityTag");
            if (compound != null) {
                CompoundTag inventoryTag = compound.getCompound("Inventory");
                if (inventoryTag.contains("Items", 9)) {
                    ItemStackHandler handler = new ItemStackHandler();
                    handler.deserializeNBT(inventoryTag);
                    ItemStack newMealStack = handler.getStackInSlot(6);
                    newMealStack.shrink(1);
                    compound.remove("Inventory");
                    compound.put("Inventory", handler.serializeNBT());
                }
            }

        }
    }

    public static ItemStack getContainerFromItem(ItemStack cookingPotStack) {
        if (!cookingPotStack.is((Item)ModItems.COOKING_POT.get())) {
            return ItemStack.EMPTY;
        } else {
            CompoundTag compound = cookingPotStack.getTagElement("BlockEntityTag");
            return compound != null ? ItemStack.of(compound.getCompound("Container")) : ItemStack.EMPTY;
        }
    }

    public void load(CompoundTag compound) {
        super.load(compound);

        // 【优化】加载期间挂起逐槽刷新，反序列化完成后只同步一次
        this.deferInventoryEvents = true;
        try {
            if (compound.contains("Inventory")) {
                ItemStackHandler tempHandler = new ItemStackHandler(INVENTORY_SIZE);
                tempHandler.deserializeNBT(compound.getCompound("Inventory"));

                for (int i = 0; i < Math.min(tempHandler.getSlots(), INVENTORY_SIZE); i++) {
                    this.inventory.setStackInSlot(i, tempHandler.getStackInSlot(i));
                }
            }
        } finally {
            this.deferInventoryEvents = false;
        }

        // 兼容旧存档：CookTime 过去是 int，现在按任意数字读取
        this.cookTime = compound.contains("CookTime", 99) ? compound.getFloat("CookTime") : 0.0F;
        this.cookTimeTotal = compound.getInt("CookTimeTotal");
        this.mealContainerStack = compound.contains("Container") ? ItemStack.of(compound.getCompound("Container")) : ItemStack.EMPTY;

        if (compound.contains("CustomName", 8)) {
            this.customName = Serializer.fromJson(compound.getString("CustomName"));
        }

        if (compound.contains("RecipesUsed")) {
            CompoundTag compoundRecipes = compound.getCompound("RecipesUsed");
            for (String key : compoundRecipes.getAllKeys()) {
                this.usedRecipeTracker.put(new ResourceLocation(key), compoundRecipes.getInt(key));
            }
        }

        // 逐槽事件被挂起，这里显式重建输入缓存后再刷新配方
        this.hasInputCache = this.hasInputInternal();
        this.refreshCurrentRecipe();
    }

    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putFloat("CookTime", this.cookTime);
        compound.putInt("CookTimeTotal", this.cookTimeTotal);

        if (this.mealContainerStack != null && !this.mealContainerStack.isEmpty()) {
            compound.put("Container", this.mealContainerStack.serializeNBT());
        }

        if (this.customName != null) {
            compound.putString("CustomName", Serializer.toJson(this.customName));
        }

        compound.put("Inventory", this.inventory.serializeNBT());
        CompoundTag compoundRecipes = new CompoundTag();
        this.usedRecipeTracker.forEach((recipeId, craftedAmount) -> {
            compoundRecipes.putInt(recipeId.toString(), craftedAmount);
        });
        compound.put("RecipesUsed", compoundRecipes);
    }

    private CompoundTag writeItems(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Container", this.mealContainerStack.serializeNBT());
        compound.put("Inventory", this.inventory.serializeNBT());
        return compound;
    }

    public CompoundTag writeMeal(CompoundTag compound) {
        if (this.getMeal().isEmpty()) {
            return compound;
        } else {
            ItemStackHandler drops = new ItemStackHandler(INVENTORY_SIZE);

            for (int i = 0; i < INVENTORY_SIZE; ++i) {
                drops.setStackInSlot(i, i == MEAL_DISPLAY_SLOT ? this.inventory.getStackInSlot(i) : ItemStack.EMPTY);
            }

            if (this.customName != null) {
                compound.putString("CustomName", Serializer.toJson(this.customName));
            }

            if (!this.mealContainerStack.isEmpty()) {
                compound.put("Container", this.mealContainerStack.serializeNBT());
            }

            compound.put("Inventory", drops.serializeNBT());
            return compound;
        }
    }

    // ======================== 灵魂系统 ========================

    //检查是否有可用的灵魂能量
    public boolean hasSoulEnergy() {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return false;

        CompoundTag tag = soulSource.getTag();

        // 检查是否为链接宝石（Soul Transfer）
        if (soulSource.getItem() == SOUL_TRANSFER.get()) {
            if (tag != null && tag.contains("owner")) {
                UUID ownerUuid = tag.getUUID("owner");
                Player owner = this.level != null ? this.level.getPlayerByUUID(ownerUuid) : null;
                return owner != null && SEHelper.getSEActive(owner) && SEHelper.getSESouls(owner) > 0;
            }
        }
        // 检查是否为图腾（ITotem实现）
        else if (soulSource.getItem() instanceof ITotem) {
            if (tag != null && tag.contains("Souls")) {
                return tag.getInt("Souls") > 0;
            }
        }

        return false;
    }

    // 暂存餐 / 输出是否已被注入灵魂
    private boolean isSoulInfused(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("SoulInfused");
    }

    // 【优化】一次调用同时判断"灵魂源是否可用"与"剩余数量"，避免每 tick 反复查 NBT
    private int getSoulEnergyAvailable() {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return 0;

        CompoundTag tag = soulSource.getTag();

        if (soulSource.getItem() == SOUL_TRANSFER.get()) {
            if (tag != null && tag.contains("owner")) {
                UUID ownerUuid = tag.getUUID("owner");
                Player owner = this.level != null ? this.level.getPlayerByUUID(ownerUuid) : null;
                if (owner != null && SEHelper.getSEActive(owner)) {
                    return (int)SEHelper.getSESouls(owner);
                }
            }
        } else if (soulSource.getItem() instanceof ITotem) {
            if (tag != null && tag.contains("Souls")) {
                return tag.getInt("Souls");
            }
        }
        return 0;
    }

    // 从灵魂源消耗灵魂能量
    private boolean consumeSoulEnergy(int amount) {
        if (amount <= 0) return false;

        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty() || this.level == null) return false;

        CompoundTag tag = soulSource.getTag();

        // 检查是否为链接宝石（Soul Transfer）
        if (soulSource.getItem() == SOUL_TRANSFER.get()) {
            if (tag != null && tag.contains("owner")) {
                UUID ownerUuid = tag.getUUID("owner");
                Player owner = this.level.getPlayerByUUID(ownerUuid);

                if (owner != null && SEHelper.getSEActive(owner) && SEHelper.getSESouls(owner) >= amount) {
                    // 从玩家灵魂能量中消耗
                    SEHelper.decreaseSESouls(owner, amount);
                    SEHelper.sendSEUpdatePacket(owner);
                    this.spawnSoulParticles();
                    return true;
                }
            }
        }
        // 检查是否为图腾（ITotem实现）
        else if (soulSource.getItem() instanceof ITotem) {
            if (tag != null && tag.contains("Souls")) {
                int souls = tag.getInt("Souls");
                if (souls >= amount) {
                    // 从图腾中消耗（直接改原物品的 NBT，再置脏保证存档）
                    tag.putInt("Souls", souls - amount);
                    this.spawnSoulParticles();
                    this.setChanged();
                    return true;
                }
            }
        }

        return false;
    }

    private void spawnSoulParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            BlockPos pos = this.getBlockPos();
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    5, 0.2, 0.0, 0.2, 0.05);
        }
    }

    // ======================== 核心Tick逻辑 ========================
    public static void cookingTick(Level level, BlockPos pos, BlockState state, CursedIngotPotBlockEntity cookingPot) {
        boolean isHeated = cookingPot.isHeated(level, pos);
        boolean didInventoryChange = false;

        ItemStack currentMeal = cookingPot.getMeal();
        ItemStack outputStack = cookingPot.inventory.getStackInSlot(OUTPUT_SLOT);
        boolean anyHasMark = cookingPot.isSoulInfused(currentMeal) || cookingPot.isSoulInfused(outputStack);

        boolean canCook = false;
        float speedMultiplier = 1.0f;
        CookingPotRecipe currentRecipe = cookingPot.cachedRecipe;

        // 【优化】配方缓存自愈：每 5 秒重新匹配一次（仅在有原料时执行），避免数据包重载后缓存过期
        if (level.getGameTime() % 100 == 0) {
            cookingPot.refreshCurrentRecipe();
            currentRecipe = cookingPot.cachedRecipe;
        }

        if (cookingPot.hasInput() && currentRecipe != null && cookingPot.canCook(currentRecipe)) {
            // 【优化】灵魂消耗与结果栈均取缓存值，不再每 tick 拷贝结果栈 / 查食物属性
            int estimatedCost = cookingPot.cachedSoulCost;
            int remainingSoul = cookingPot.getSoulEnergyAvailable();
            boolean hasSoulEnergy = remainingSoul > 0;

            boolean needSoul;
            if (currentMeal.isEmpty() && outputStack.isEmpty()) {
                needSoul = hasSoulEnergy && (remainingSoul >= estimatedCost);
            } else {
                // 已有灵魂注入的成品，必须继续供能，否则混合产出
                needSoul = anyHasMark;
            }

            boolean soulAvailable = hasSoulEnergy && (remainingSoul >= estimatedCost);

            if (anyHasMark && !soulAvailable) {
                canCook = false;
                cookingPot.cookTime = 0;
            } else {
                if (isHeated && needSoul && soulAvailable) {
                    canCook = true;
                    speedMultiplier = 2.0f;   // 有灵魂 + 热源：2 倍速
                } else if (isHeated && !needSoul) {
                    canCook = true;
                    speedMultiplier = 1.0f;   // 仅热源：1 倍速
                } else if (needSoul && soulAvailable) {
                    canCook = true;
                    speedMultiplier = 1.2f;   // 仅灵魂：1.2 倍速
                } else {
                    canCook = false;
                    speedMultiplier = 0f;
                }
            }
        }

        if (canCook && cookingPot.hasInput() && currentRecipe != null) {
            cookingPot.cookTime += 1.0f * speedMultiplier;
            if (cookingPot.cookTime >= cookingPot.cookTimeTotal) {
                boolean success = cookingPot.processCooking(currentRecipe);
                if (!success) {
                    cookingPot.cookTime = 0;
                }
            }
        } else if (cookingPot.cookTime > 0) {
            // 【优化】未烹饪时降温每 tick 恰好一次（修复此前空锅/无配方时的双倍衰减）
            cookingPot.cookTime = Mth.clamp(cookingPot.cookTime - 1.0f, 0.0f, cookingPot.cookTimeTotal);
        }

        ItemStack mealStack = cookingPot.getMeal();
        if (!mealStack.isEmpty()) {
            // 【优化】批量取餐：挂起逐槽事件，结束时统一刷新一次
            cookingPot.deferInventoryEvents = true;
            try {
                if (!cookingPot.doesMealHaveContainer(mealStack)) {
                    // 【修复】仅在真正移动了物品时才标记变更：
                    // 输出槽满 / 物品不同时 moveMealToOutput 是空操作，此前每 tick 都会触发完整配方扫描 + 置脏
                    didInventoryChange = cookingPot.moveMealToOutput();
                } else if (!cookingPot.inventory.getStackInSlot(CONTAINER_SLOT).isEmpty()) {
                    didInventoryChange = cookingPot.useStoredContainersOnMeal();
                }
            } finally {
                cookingPot.deferInventoryEvents = false;
            }
        }

        if (didInventoryChange) {
            cookingPot.inventoryChanged();
        }

        // 1.20.1 本地特效：有灵魂能量且有原料时偶尔冒出灵魂火
        // （相对旧写法调整了判定顺序：先掷随机再查灵魂，省掉每 tick 的 NBT 读取）
        if (level instanceof ServerLevel serverLevel && level.random.nextFloat() < 0.1F
                && cookingPot.hasInput() && cookingPot.hasSoulEnergy()) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() * 0.6 - 0.3);
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() * 0.6 - 0.3);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static void animationTick(Level level, BlockPos pos, BlockState state, CursedIngotPotBlockEntity cookingPot) {
        if (cookingPot.isHeated(level, pos)) {
            RandomSource random = level.random;
            double x;
            double y;
            double z;
            if (random.nextFloat() < 0.2F) {
                x = (double)pos.getX() + 0.5 + (random.nextDouble() * 0.6 - 0.3);
                y = (double)pos.getY() + 0.7;
                z = (double)pos.getZ() + 0.5 + (random.nextDouble() * 0.6 - 0.3);
                level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0, 0.0, 0.0);
            }

            if (random.nextFloat() < 0.05F) {
                x = (double)pos.getX() + 0.5 + (random.nextDouble() * 0.4 - 0.2);
                y = (double)pos.getY() + 0.5;
                z = (double)pos.getZ() + 0.5 + (random.nextDouble() * 0.4 - 0.2);
                double motionY = random.nextBoolean() ? 0.015 : 0.005;
                level.addParticle((ParticleOptions)ModParticleTypes.STEAM.get(), x, y, z, 0.0, motionY, 0.0);
            }
        }

    }

    // ======================== 配方、缓存与辅助方法 ========================

    private boolean hasInputInternal() {
        for (int i = 0; i < 6; ++i) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasInput() {
        return this.hasInputCache;
    }

    // 【优化】配方与衍生结果一起刷新/失效；调用点：库存变化、加载完成、每 5 秒自愈
    private void refreshCurrentRecipe() {
        this.cachedRecipe = null;
        this.cachedResultStack = ItemStack.EMPTY;
        this.cachedSoulCost = 0;

        if (this.level == null || this.level.isClientSide) {
            return;
        }

        if (this.hasInputCache) {
            RecipeWrapper wrapper = new RecipeWrapper(this.inventory);
            this.cachedRecipe = this.level.getRecipeManager()
                    .getRecipeFor(ModRecipeTypes.COOKING.get(), wrapper, this.level).orElse(null);

            if (this.cachedRecipe != null) {
                // 同样取副本：cachedResultStack 只读，但不与配方内部栈共享引用
                this.cachedResultStack = this.cachedRecipe.getResultItem(this.level.registryAccess()).copy();
                this.cachedSoulCost = this.calculateSoulCost(this.cachedResultStack);
                this.cookTimeTotal = this.cachedRecipe.getCookTime();
                if (this.cookTime > this.cookTimeTotal) {
                    this.cookTime = this.cookTimeTotal;
                }
            }
        }

        if (this.cachedRecipe == null && this.cookTime > 0) {
            this.cookTime = 0;
        }
    }

    protected boolean canCook(CookingPotRecipe recipe) {
        if (!this.hasInput() || this.level == null) {
            return false;
        }

        // 【优化】用缓存的配方结果栈，避免每 tick 拷贝
        ItemStack resultStack = this.cachedResultStack;
        if (resultStack.isEmpty()) {
            return false;
        }

        ItemStack storedMealStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        if (storedMealStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItem(storedMealStack, resultStack)) {
            return false;
        }

        int total = storedMealStack.getCount() + resultStack.getCount();
        return total <= storedMealStack.getMaxStackSize() && total <= 64;
    }

    private int calculateSoulCost(ItemStack resultStack) {
        int baseCost = 50;
        int nutritionCost = 0;

        // 检查物品是否有食物属性
        FoodProperties foodProperties = resultStack.getFoodProperties(null);
        if (foodProperties != null) {
            nutritionCost = 10 * foodProperties.getNutrition();
        }

        return Math.min(baseCost + nutritionCost, 2000);
    }

    private boolean processCooking(CookingPotRecipe recipe) {
        if (this.level == null) {
            return false;
        }

        this.cookTimeTotal = recipe.getCookTime();
        if (this.cookTime < this.cookTimeTotal) {
            return false;
        }

        ItemStack currentMeal = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
        boolean anyHasMark = this.isSoulInfused(currentMeal) || this.isSoulInfused(outputStack);
        // 【修复】农夫乐事 1.20.1 的 getResultItem() 直接返回配方内部的 output 栈（没有 copy），
        // 往它身上写 NBT 会永久污染这条配方：之后即使没有灵魂源，该菜谱的产物依旧带着 SoulInfused。
        ItemStack resultStack = recipe.getResultItem(this.level.registryAccess()).copy();

        int soulCost = this.calculateSoulCost(resultStack);
        // 【优化】合并灵魂查询：一次调用同时判断"灵魂源是否可用"与"剩余数量"
        int remainingSoul = this.getSoulEnergyAvailable();
        boolean hasSoulSource = remainingSoul > 0;

        // 已有灵魂成品时必须继续供能，避免同一批产出混入未注入灵魂的成品
        boolean shouldUseSoul;
        if (currentMeal.isEmpty() && outputStack.isEmpty()) {
            shouldUseSoul = hasSoulSource && (remainingSoul >= soulCost);
        } else {
            shouldUseSoul = anyHasMark && hasSoulSource && (remainingSoul >= soulCost);
        }

        if (shouldUseSoul) {
            boolean soulInfused = this.consumeSoulEnergy(soulCost);
            if (soulInfused) {
                // 给产物注入灵魂
                CompoundTag tag = resultStack.getOrCreateTag();
                tag.putBoolean("SoulInfused", true);
                resultStack.setTag(tag);
            } else {
                this.cookTime = 0.0f;
                return false;
            }
        }

        // 【优化】批量改库存：挂起逐槽事件，最后统一刷新一次配方缓存并同步
        this.deferInventoryEvents = true;
        try {
            if (currentMeal.isEmpty()) {
                this.inventory.setStackInSlot(MEAL_DISPLAY_SLOT, resultStack.copy());
            } else if (ItemStack.isSameItem(currentMeal, resultStack)) {
                currentMeal.grow(resultStack.getCount());
            } else {
                return false;
            }

            this.cookTime = 0.0f;
            this.mealContainerStack = recipe.getOutputContainer();
            this.setRecipeUsed(recipe);

            for (int i = 0; i < 6; ++i) {
                ItemStack slotStack = this.inventory.getStackInSlot(i);
                if (slotStack.hasCraftingRemainingItem()) {
                    this.ejectIngredientRemainder(slotStack.getCraftingRemainingItem().copy().split(1));
                } else if (INGREDIENT_REMAINDER_OVERRIDES.containsKey(slotStack.getItem())) {
                    this.ejectIngredientRemainder(((Item)INGREDIENT_REMAINDER_OVERRIDES.get(slotStack.getItem())).getDefaultInstance());
                }

                if (!slotStack.isEmpty()) {
                    slotStack.shrink(1);
                }
            }
        } finally {
            this.deferInventoryEvents = false;
        }

        this.inventoryChanged();
        return true;
    }

    protected void ejectIngredientRemainder(ItemStack remainderStack) {
        Direction direction = ((Direction)this.getBlockState().getValue(CookingPotBlock.FACING)).getCounterClockWise();
        double x = (double)this.worldPosition.getX() + 0.5 + (double)direction.getStepX() * 0.25;
        double y = (double)this.worldPosition.getY() + 0.7;
        double z = (double)this.worldPosition.getZ() + 0.5 + (double)direction.getStepZ() * 0.25;
        ItemUtils.spawnItemEntity(this.level, remainderStack, x, y, z, (double)((float)direction.getStepX() * 0.08F), 0.25, (double)((float)direction.getStepZ() * 0.08F));
    }

    public void setRecipeUsed(@Nullable Recipe<?> recipe) {
        if (recipe != null) {
            ResourceLocation recipeID = recipe.getId();
            this.usedRecipeTracker.addTo(recipeID, 1);
        }

    }

    @Nullable
    public Recipe<?> getRecipeUsed() {
        return null;
    }

    public void awardUsedRecipes(Player player, List<ItemStack> items) {
        List<Recipe<?>> usedRecipes = this.getUsedRecipesAndPopExperience(player.level(), player.position());
        player.awardRecipes(usedRecipes);
        this.usedRecipeTracker.clear();
    }

    public List<Recipe<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
        List<Recipe<?>> list = Lists.newArrayList();

        for (Object2IntMap.Entry<ResourceLocation> entry : this.usedRecipeTracker.object2IntEntrySet()) {
            level.getRecipeManager().byKey((ResourceLocation)entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                // 仅烹饪锅配方给经验；避免非烹饪配方被强转
                if (recipe instanceof CookingPotRecipe cookingRecipe) {
                    int expTotal = Mth.floor((float)entry.getIntValue() * cookingRecipe.getExperience());
                    float expFraction = Mth.frac((float)entry.getIntValue() * cookingRecipe.getExperience());
                    if (expFraction != 0.0F && level.random.nextDouble() < (double)expFraction) {
                        ++expTotal;
                    }

                    if (level instanceof ServerLevel serverLevel) {
                        ExperienceOrb.award(serverLevel, pos, expTotal);
                    }
                }
            });
        }

        return list;
    }

    public ItemStack getContainer() {
        ItemStack mealStack = this.getMeal();
        return !mealStack.isEmpty() && !this.mealContainerStack.isEmpty() ? this.mealContainerStack : mealStack.getCraftingRemainingItem();
    }

    public boolean isHeated() {
        return this.level == null ? false : this.isHeated(this.level, this.worldPosition);
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public ItemStack getMeal() {
        return this.inventory.getStackInSlot(6);
    }

    public NonNullList<ItemStack> getDroppableInventory() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < INVENTORY_SIZE; ++i) {
            if (i != MEAL_DISPLAY_SLOT) {
                drops.add(this.inventory.getStackInSlot(i));
            }
        }
        return drops;
    }

    // 【修复】返回是否真的搬运了物品：输出槽满 / 物品不同时是空操作，调用方据此避免无意义的置脏与配方重扫
    private boolean moveMealToOutput() {
        ItemStack mealStack = this.inventory.getStackInSlot(6);
        ItemStack outputStack = this.inventory.getStackInSlot(8);
        int mealCount = Math.min(mealStack.getCount(), mealStack.getMaxStackSize() - outputStack.getCount());
        if (mealCount <= 0) {
            return false;
        }

        if (outputStack.isEmpty()) {
            this.inventory.setStackInSlot(8, mealStack.split(mealCount));
            return true;
        } else if (ItemStack.isSameItem(outputStack, mealStack)) {
            mealStack.shrink(mealCount);
            outputStack.grow(mealCount);
            return true;
        }
        return false;
    }

    private boolean useStoredContainersOnMeal() {
        ItemStack mealStack = this.inventory.getStackInSlot(6);
        ItemStack containerInputStack = this.inventory.getStackInSlot(7);
        ItemStack outputStack = this.inventory.getStackInSlot(8);
        if (this.isContainerValid(containerInputStack) && outputStack.getCount() < outputStack.getMaxStackSize()) {
            int smallerStackCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
            int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize() - outputStack.getCount());
            if (mealCount <= 0) {
                return false;
            }

            if (outputStack.isEmpty()) {
                containerInputStack.shrink(mealCount);
                this.inventory.setStackInSlot(8, mealStack.split(mealCount));
                return true;
            } else if (ItemStack.isSameItem(outputStack, mealStack)) {
                mealStack.shrink(mealCount);
                containerInputStack.shrink(mealCount);
                outputStack.grow(mealCount);
                return true;
            }
        }

        return false;
    }

    public ItemStack useHeldItemOnMeal(ItemStack container) {
        ItemStack mealStack = this.getMeal();
        if (mealStack.isEmpty()) {
            return ItemStack.EMPTY;
        }


        if (!this.doesMealHaveContainer(mealStack)) {

            if (container.isEmpty() ||
                    (ItemStack.isSameItem(mealStack, container) && container.getCount() < container.getMaxStackSize())) {
                ItemStack result = mealStack.split(1);
                this.inventoryChanged();
                return result;
            } else {
                return ItemStack.EMPTY;
            }
        }

        else if (this.isContainerValid(container)) {
            container.shrink(1);
            this.inventoryChanged();
            return mealStack.split(1);
        } else {
            return ItemStack.EMPTY;
        }
    }
    private boolean doesMealHaveContainer(ItemStack meal) {
        return !this.mealContainerStack.isEmpty() || meal.hasCraftingRemainingItem();
    }

    public boolean isContainerValid(ItemStack containerItem) {
        if (containerItem.isEmpty()) {
            return false;
        } else {
            return !this.mealContainerStack.isEmpty() ? ItemStack.isSameItem(this.mealContainerStack, containerItem) : ItemStack.isSameItem(this.getMeal(), containerItem);
        }
    }

    public Component getName() {
        return (Component)(this.customName != null ? this.customName : TextUtils.getTranslation("container.cursed_ingot_pot", new Object[0]));
    }

    public Component getDisplayName() {
        return this.getName();
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
        // 【优化】打开菜单时不再做完整配方匹配：缓存已由 inventoryChanged / 每 5 秒自愈维护
        return new CursedIngotPotMenu(id, player, this, this.cookingPotData);
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(ForgeCapabilities.ITEM_HANDLER)) {
            return side != null && !side.equals(Direction.UP) ? this.outputHandler.cast() : this.inputHandler.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    public void setRemoved() {
        super.setRemoved();
        this.inputHandler.invalidate();
        this.outputHandler.invalidate();
    }

    public CompoundTag getUpdateTag() {
        return this.writeItems(new CompoundTag());
    }

    // 【优化】库存变化的唯一出口：同步 + 重建输入缓存 + 刷新配方缓存
    @Override
    protected void inventoryChanged() {
        super.inventoryChanged();
        this.hasInputCache = this.hasInputInternal();
        this.refreshCurrentRecipe();
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(INVENTORY_SIZE) {
            protected void onContentsChanged(int slot) {
                // 【优化】批量修改库存（加载 / 一次烹饪结算）期间不逐槽刷新，结束后由调用方统一刷新
                if (CursedIngotPotBlockEntity.this.deferInventoryEvents) {
                    CursedIngotPotBlockEntity.this.setChanged();
                    return;
                }

                CursedIngotPotBlockEntity.this.inventoryChanged();
            }
        };
    }

    private ContainerData createIntArray() {
        return new ContainerData() {
            public int get(int index) {
                int var10000;
                switch (index) {
                    case 0 -> var10000 = (int)CursedIngotPotBlockEntity.this.cookTime;
                    case 1 -> var10000 = CursedIngotPotBlockEntity.this.cookTimeTotal;
                    default -> var10000 = 0;
                }

                return var10000;
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CursedIngotPotBlockEntity.this.cookTime = value;
                    case 1 -> CursedIngotPotBlockEntity.this.cookTimeTotal = value;
                }

            }

            public int getCount() {
                return 2;
            }
        };
    }

    static {
        INGREDIENT_REMAINDER_OVERRIDES = Map.ofEntries(Map.entry(Items.POWDER_SNOW_BUCKET, Items.BUCKET), Map.entry(Items.AXOLOTL_BUCKET, Items.BUCKET), Map.entry(Items.COD_BUCKET, Items.BUCKET), Map.entry(Items.PUFFERFISH_BUCKET, Items.BUCKET), Map.entry(Items.SALMON_BUCKET, Items.BUCKET), Map.entry(Items.TROPICAL_FISH_BUCKET, Items.BUCKET), Map.entry(Items.SUSPICIOUS_STEW, Items.BOWL), Map.entry(Items.MUSHROOM_STEW, Items.BOWL), Map.entry(Items.RABBIT_STEW, Items.BOWL), Map.entry(Items.BEETROOT_SOUP, Items.BOWL), Map.entry(Items.POTION, Items.GLASS_BOTTLE), Map.entry(Items.SPLASH_POTION, Items.GLASS_BOTTLE), Map.entry(Items.LINGERING_POTION, Items.GLASS_BOTTLE), Map.entry(Items.EXPERIENCE_BOTTLE, Items.GLASS_BOTTLE));
    }
}
