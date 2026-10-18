package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;
import net.v_black_cat.goetydelight.compat.goetyrevelation.RevelationCompatRegistry;
import net.v_black_cat.goetydelight.util.TextUtils;

/**
 * 天启烹饪锅：
 *  - 不消耗灵魂（灵魂源槽位被完全忽略）
 *  - 产出灵魂强度 ×10（SoulInfusedLevel = 10）
 *  - 烹饪时间 ×0.1（等效速度 ×10）
 *  - 自热（永远视为有热源）
 * 其余逻辑复用 CursedIngotPotBlockEntity。
 */
public class ApocalyptiumCookingPotBlockEntity extends CursedIngotPotBlockEntity {

    /** 产物灵魂强度等级：普通锅为 1，黑铁锅为 2，天启锅为 10。 */
    public static final int SOUL_LEVEL = 10;
    /** 产物灵魂强度 NBT 键。 */
    public static final String SOUL_LEVEL_TAG = "SoulInfusedLevel";

    public ApocalyptiumCookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(RevelationCompatRegistry.APOCALYPTIUM_POT_BE.get(), pos, state);
    }

    @Override
    public Component getName() {
        // 优先使用自定义名称（如铁砧改名），否则使用天启锅专属的 lang key
        // TextUtils.getTranslation 会自动补全 goetydelight. 前缀
        return this.getCustomName() != null
                ? this.getCustomName()
                : TextUtils.getTranslation("container.apocalyptium_cooking_pot");
    }

    // ===================== 倍率钩子 =====================

    /** 烹饪时间 ×0.1：总时长除以 10，等效速度 ×10。 */
    @Override
    protected int modifyCookTimeTotal(int original) {
        return Math.max(1, original / 10);
    }

    /** 不消耗灵魂：恒为 0，即 never 扣费。 */
    @Override
    protected int modifySoulCost(int original) {
        return 0;
    }

    /** 产出灵魂 ×10：写入 SoulInfused + SoulInfusedLevel=10。 */
    @Override
    protected void applySoulInfusion(ItemStack resultStack) {
        CompoundTag tag = resultStack.getOrCreateTag();
        tag.putBoolean("SoulInfused", true);
        tag.putInt(SOUL_LEVEL_TAG, SOUL_LEVEL);
        resultStack.setTag(tag);
    }

    // ===================== 不消耗灵魂 + 自热 =====================

    /** 永远"有灵魂可用"：不读 soul_slot，直接返回极大值，使 cookingTick 的灵魂分支恒成立。 */
    @Override
    protected int getSoulEnergyAvailable() {
        return Integer.MAX_VALUE / 2;
    }

    /** 不消耗灵魂：始终返回 false（即从不真正扣灵魂），但烹饪判定用 getSoulEnergyAvailable。 */
    @Override
    protected boolean consumeSoulEnergy(int amount) {
        return false;
    }

    /** 不消耗灵魂：hasSoulEnergy 恒为 true，用于粒子特效判定。 */
    @Override
    public boolean hasSoulEnergy() {
        return true;
    }

    /** 自热：永远视为有热源。 */
    @Override
    public boolean isHeated() {
        return true;
    }

    /** 自热：覆写 HeatableBlockEntity 的默认实现。 */
    @Override
    public boolean isHeated(Level level, BlockPos pos) {
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
        return new ApocalyptiumCookingPotMenu(id, player, this, this.cookingPotData);
    }

    /**
     * 关键：父类 processCooking 里 "shouldUseSoul = hasSoulSource && remainingSoul >= soulCost"，
     * 我们让 remainingSoul 极大、soulCost=0，所以 shouldUseSoul 恒 true，
     * 随后调用 consumeSoulEnergy(soulCost=0) 返回 false 会导致烹饪失败。
     * 这里覆写 processCooking 的"灵魂注入"环节：直接注入灵魂，不调用 consumeSoulEnergy。
     */
    @Override
    protected boolean processCooking(vectorwing.farmersdelight.common.crafting.CookingPotRecipe recipe) {
        if (this.level == null) {
            return false;
        }
        this.cookTimeTotal = this.modifyCookTimeTotal(recipe.getCookTime());
        if (this.cookTime < this.cookTimeTotal) {
            return false;
        }

        ItemStack currentMeal = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);

        ItemStack resultStack = recipe.getResultItem(this.level.registryAccess()).copy();

        // 不消耗灵魂，直接注入 10 倍灵魂标记
        this.applySoulInfusion(resultStack);

        // 复用父类批量库存逻辑：这里为避免重复代码，直接走父类 processCooking 的库存段。
        // 但父类会再次尝试 consumeSoulEnergy，因此我们改为手动执行库存结算：
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
                    this.ejectIngredientRemainder(INGREDIENT_REMAINDER_OVERRIDES.get(slotStack.getItem()).getDefaultInstance());
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
}