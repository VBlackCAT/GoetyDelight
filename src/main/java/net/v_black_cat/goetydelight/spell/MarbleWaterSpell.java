package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.BlockSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.v_black_cat.goetydelight.entities.spell.RichSoilSpellEntity;

import java.util.List;

public class MarbleWaterSpell extends BlockSpell {

    private static final TagKey<Block> SILTIFIABLE = TagKey.create(Registries.BLOCK,
            new ResourceLocation("goetydelight", "marble_focus/siltifiable"));

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 8, 1.0D, 0, 0.0F);
    }

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
    public List<Enchantment> acceptedEnchantments() {
        return List.of(ModEnchantments.RADIUS.get());
    }

    @Override
    public void blockResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff,
                            BlockPos target, Direction direction, SpellStat spellStat) {
        ItemStack focus = IWand.getFocus(staff);
        if (focus.isEmpty()) {
            focus = WandUtil.findFocus(caster);
        }
        int radiusLevel = focus.isEmpty() ? 0 : focus.getEnchantmentLevel(ModEnchantments.RADIUS.get());
        int radius = Math.max(1, 1 + radiusLevel);
        worldIn.addFreshEntity(new RichSoilSpellEntity(worldIn, caster, target, radius,
                RichSoilSpellEntity.EffectType.MARBLE_WATER)
                .setStaff(staff)
                .setShiftMode(caster.isShiftKeyDown())
                .setDirection(direction));
    }

    public static boolean performDeferredEffect(ServerLevel worldIn, LivingEntity caster, BlockPos target,
                                                Direction direction, int radius, boolean shiftMode) {
        if (shiftMode) {
            int converted = 0;
            for (int y = -2; y <= 2; ++y) {
                for (int dx = -radius; dx <= radius; ++dx) {
                    for (int dz = -radius; dz <= radius; ++dz) {
                        BlockPos pos = target.offset(dx, y, dz);
                        BlockState state = worldIn.getBlockState(pos);
                        if (isSiltifiable(state)) {
                            worldIn.setBlockAndUpdate(pos, ModBlocks.SILT_MARBLE_HEAVY_BLOCK.get().defaultBlockState());
                            converted++;
                        }
                    }
                }
            }
            if (converted > 0) {
                worldIn.playSound(null, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
                        SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
                return true;
            }
            return false;
        }

        BlockState state = worldIn.getBlockState(target);
        BlockPos placePos = placeTarget(state, target, direction);
        BlockState placeState = worldIn.getBlockState(placePos);
        if (canPlaceWater(placeState)) {
            worldIn.setBlockAndUpdate(placePos, Blocks.WATER.defaultBlockState());
            worldIn.playSound(null, placePos.getX() + 0.5D, placePos.getY() + 0.5D, placePos.getZ() + 0.5D,
                    SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
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
