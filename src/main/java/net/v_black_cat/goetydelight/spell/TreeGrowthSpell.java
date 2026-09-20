package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.magic.BlockSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.SaplingBlock;
import net.v_black_cat.goetydelight.entities.spell.TreeGrowthSpellEntity;

import java.util.List;

public class TreeGrowthSpell extends BlockSpell {

    private static final int SOUL_COST = 60;
    private static final int COOLDOWN_TICKS = 20 * 20; // 20 秒

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
    public List<Enchantment> acceptedEnchantments() {
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
        if (worldIn.getBlockState(target).getBlock() instanceof SaplingBlock) {
            worldIn.addFreshEntity(new TreeGrowthSpellEntity(worldIn, target));
        }
    }
}
