package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.magic.BlockSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TreeGrowthSpell extends BlockSpell {

    private static final int SOUL_COST = 60;
    private static final int COOLDOWN_TICKS = 20 * 20; // 20 秒
    private static final int MAX_GROW_ATTEMPTS = 3;    // advanceTree 失败（如空间不足）时最多重试次数

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 16, 0, 0, 0.0F);
    }

    @Override
    public int defaultSoulCost() {
        return SOUL_COST;
    }

    @Override
    public int defaultSpellCooldown() {
        return COOLDOWN_TICKS;
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.WILD;
    }

    @Override
    public List<ResourceKey<Enchantment>> acceptedEnchantments() {
        return List.of(); // 不吃任何附魔
    }

    /** 施法门控：只有目标是树苗（含 Goety 的 EndSaplingBlock / RedMossSaplingBlock 等子类）才放行。 */
    @Override
    public boolean rightBlock(ServerLevel worldIn, LivingEntity caster, BlockPos target,
                              Direction direction, SpellStat spellStat) {
        return worldIn.getBlockState(target).getBlock() instanceof SaplingBlock;
    }

    @Override
    public void blockResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff,
                            BlockPos target, Direction direction, SpellStat spellStat) {
        BlockState state = worldIn.getBlockState(target);
        if (!(state.getBlock() instanceof SaplingBlock sapling)) {
            return;
        }

        // 刚种下的树苗 stage=0：直接推到 1，让本次施法必然触发 growTree（跳过原版随机 tick 的中间一步）
        if (state.getValue(SaplingBlock.STAGE) == 0) {
            worldIn.setBlock(target, state.setValue(SaplingBlock.STAGE, 1), 2);
        }

        // advanceTree(stage=1) == treeGrower.growTree(...)；MegaTreeGrower 自动处理 2×2 大树。
        // 空间不足等导致生成失败时树苗保留，这里小循环重试几次。
        for (int i = 0; i < MAX_GROW_ATTEMPTS
                && worldIn.getBlockState(target).getBlock() instanceof SaplingBlock; ++i) {
            BlockState current = worldIn.getBlockState(target);
            if (current.getBlock() instanceof SaplingBlock sapling2) {
                sapling2.advanceTree(worldIn, target, current, worldIn.random);
            }
        }

        worldIn.playSound(null, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
                SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
