package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;
import net.v_black_cat.goetydelight.compat.goetyrevelation.RevelationCompatRegistry;
import net.v_black_cat.goetydelight.recipe.WorkstationCookingRecipe;
import net.v_black_cat.goetydelight.util.TextUtils;

/**
 * 天启烹饪锅：
 *  - 不消耗灵魂（灵魂源槽位被完全忽略）
 *  - 不再给产物附加 SoulInfused 能量
 *  - 烹饪时间 ×0.1（等效速度 ×10）
 *  - 自热（永远视为有热源）
 *  - 不需要灵魂源
 * 其余逻辑复用 CursedIngotPotBlockEntity。
 * 注意：不再覆写 processCooking。父类在 soulCost <= 0 时短路视为成功。
 */
public class ApocalyptiumCookingPotBlockEntity extends CursedIngotPotBlockEntity {

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

    /** 不消耗灵魂：恒为 0。父类 processCooking 在 soulCost <= 0 时短路成功。 */
    @Override
    protected int modifySoulCost(int original) {
        return 0;
    }

    /** 天启锅不再给产物写入 SoulInfused 能量。 */
    // applySoulInfusion 继承 CursedIngotPotBlockEntity 的空实现。

    // ===================== 不需要灵魂 + 自热 =====================

    /** 不需要灵魂源：返回极大值，使 cookingTick 的灵魂可用判定恒成立。 */
    @Override
    protected int getSoulEnergyAvailable() {
        return Integer.MAX_VALUE / 2;
    }

    /** 不消耗灵魂：始终返回 false（不真正扣灵魂）。 */
    @Override
    protected boolean consumeSoulEnergy(int amount) {
        return false;
    }

    /** 不需要灵魂：hasSoulEnergy 恒为 true，用于粒子特效判定。 */
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

    /** 天启锅不需要灵魂源，保持父类默认 false，无需覆写。 */

    @Override
    protected boolean canUseWorkstation(WorkstationCookingRecipe recipe) {
        // 天启锅是诅咒金属锅的升级版本：诅咒金属锅能做的特殊配方，天启锅也能做。
        return super.canUseWorkstation(recipe) || recipe.isCursedIngotPot();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
        return new ApocalyptiumCookingPotMenu(id, player, this, this.cookingPotData);
    }
}