package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.magic.BlockSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class MarbleWaterSpell extends BlockSpell {

//wu1wu2 is huge baka
    @Override
    public int defaultSoulCost() {
        return 51; // 灵魂能量
    }

    @Override
    public int defaultSpellCooldown() {
        return 10;//冷却
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.GEOMANCY;
    }

    @Override
    public void blockResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff,
                            BlockPos target, Direction direction, SpellStat spellStat) {
        BlockState state = worldIn.getBlockState(target);
        BlockPos placePos = canPlaceWater(state)
                ? target
                : target.relative(direction);

        BlockState placeState = worldIn.getBlockState(placePos);
        if (canPlaceWater(placeState)) {
            worldIn.setBlockAndUpdate(placePos, Blocks.WATER.defaultBlockState());
            worldIn.playSound(null, placePos.getX() + 0.5D, placePos.getY() + 0.5D, placePos.getZ() + 0.5D,
                    SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static boolean canPlaceWater(BlockState state) {
        return state.isAir()
                || state.canBeReplaced()
                || state.getFluidState().is(Fluids.WATER);
    }
}
