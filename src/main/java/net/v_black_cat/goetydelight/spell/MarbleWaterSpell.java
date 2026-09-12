package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.magic.BlockSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class MarbleWaterSpell extends BlockSpell {

    /** 可被替换为粉砂质大理石的方块（默认 #c:stones，数据包可扩展） */
    private static final TagKey<Block> SILTIFIABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("goetydelight", "marble_focus/siltifiable"));

    @Override
    public int defaultSoulCost() {
        return 51; // 灵魂能量
    }

    @Override
    public int defaultSpellCooldown() {
        return 10; // 冷却
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.GEOMANCY;
    }

    /** 施法前判定：返回 false 时既不耗灵魂也不进冷却 */
    @Override
    public boolean rightBlock(ServerLevel worldIn, LivingEntity caster, BlockPos target, Direction direction, SpellStat spellStat) {
        BlockState state = worldIn.getBlockState(target);
        if (caster.isShiftKeyDown()) {
            // 替换模式：只有石头类方块才有效
            return isSiltifiable(state);
        }
        // 放水模式：目标处或点击面的相邻处有一处能放水才有效
        return canPlaceWater(worldIn.getBlockState(placeTarget(state, target, direction)));
    }

    @Override
    public void blockResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff,
                            BlockPos target, Direction direction, SpellStat spellStat) {
        BlockState state = worldIn.getBlockState(target);

        if (caster.isShiftKeyDown()) {
            if (!isSiltifiable(state)) {
                return;
            }
            worldIn.setBlockAndUpdate(target, ModBlocks.SILT_MARBLE_HEAVY_BLOCK.get().defaultBlockState());
            worldIn.playSound(null, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
                    SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            return;
        }

        BlockPos placePos = placeTarget(state, target, direction);
        BlockState placeState = worldIn.getBlockState(placePos);
        if (canPlaceWater(placeState)) {
            worldIn.setBlockAndUpdate(placePos, Blocks.WATER.defaultBlockState());
            worldIn.playSound(null, placePos.getX() + 0.5D, placePos.getY() + 0.5D, placePos.getZ() + 0.5D,
                    SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /** 放水位置：目标可放水就放目标处，否则放在点击面的相邻处 */
    private static BlockPos placeTarget(BlockState state, BlockPos target, Direction direction) {
        return canPlaceWater(state) ? target : target.relative(direction);
    }

    /** 石头类（可替换为粉砂质大理石）判定；已经是目标方块则视为无效 */
    private static boolean isSiltifiable(BlockState state) {
        return state.is(SILTIFIABLE) && state.getBlock() != ModBlocks.SILT_MARBLE_HEAVY_BLOCK.get();
    }

    private static boolean canPlaceWater(BlockState state) {
        return state.isAir()
                || state.canBeReplaced()
                || state.getFluidState().is(Fluids.WATER);
    }
}
