package net.v_black_cat.goetydelight.compat.goetyrevelation;

import com.Polarice3.Goety.api.ritual.IRitualType;
import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.Polarice3.Goety.common.items.ModItems.*;

/**
 * 大师烹饪仪式：
 * 在普通烹饪仪式（culinary）的方块要求基础上，额外需要
 * 1 个 APOCALYPTIUM_POT 与 1 个 NIGHT_STOVE。
 */
public class MasterDelightRitualType implements IRitualType {

    @Override
    public String getName() {
        return "master_culinary";
    }

    @Override
    public ItemStack getJeiIcon() {
        return new ItemStack(RevelationCompatRegistry.APOCALYPTIUM_POT.get());
    }

    @Override
    public boolean getRequirement(RitualBlockEntity tileEntity, BlockPos pos, Level level) {
        return checkMasterRequirements(pos, level);
    }

    @Override
    public boolean getRequirement(RitualBlockEntity pTileEntity, @Nullable Player pPlayer, BlockPos pPos, Level pLevel) {
        return checkMasterRequirements(pPos, pLevel);
    }

    /**
     * 大师烹饪仪式方块要求：
     *   - 2 个烟熏炉（SMOKER）
     *   - 1 个阴影炉灶（SHADE_STOVE）
     *   - 1 个诅咒金属锅（CURSED_INGOT_POT）
     *   - 1 个 APOCALYPTIUM_POT
     *   - 1 个 NIGHT_STOVE
     */
    private boolean checkMasterRequirements(BlockPos pos, Level level) {
        if (!GoetyRevelationCompat.IS_LOADED || RevelationCompatRegistry.APOCALYPTIUM_POT == null || ModBlocks.NIGHT_STOVE == null || ModBlocks.NIGHT_STOVE.get() == null) {
            return false;
        }

        final int RANGE = 8;

        Map<Block, Integer> blockRequirements = new HashMap<>();
        blockRequirements.put(Blocks.SMOKER, 2);
        blockRequirements.put(ModBlocks.SHADE_STOVE.get(), 1);
        blockRequirements.put(ModBlocks.CURSED_INGOT_POT.get(), 1);
        blockRequirements.put(RevelationCompatRegistry.APOCALYPTIUM_POT.get(), 1);
        blockRequirements.put(ModBlocks.NIGHT_STOVE.get(), 1);

        Map<Block, Integer> blockCounts = new HashMap<>();
        for (Block block : blockRequirements.keySet()) {
            blockCounts.put(block, 0);
        }

        for (int i = -RANGE; i <= RANGE; ++i) {
            for (int j = -RANGE; j <= RANGE; ++j) {
                for (int k = -RANGE; k <= RANGE; ++k) {
                    BlockPos checkPos = pos.offset(i, j, k);
                    BlockState state = level.getBlockState(checkPos);
                    Block block = state.getBlock();
                    if (blockRequirements.containsKey(block)) {
                        blockCounts.put(block, blockCounts.get(block) + 1);
                    }
                }
            }
        }

        for (Map.Entry<Block, Integer> entry : blockRequirements.entrySet()) {
            if (blockCounts.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onFinishRitual(Level world, BlockPos darkAltarPos,
                               DarkAltarBlockEntity tileEntity,
                               Player castingPlayer,
                               ItemStack activationItem) {

        RitualRecipe recipe = tileEntity.getCurrentRitualRecipe();
        if (recipe != null) {
            String id = recipe.getId().toString();

            if (id.equals("goetydelight:ritual/apollyon_cake")) {
                returnSpecialItems(world, darkAltarPos, castingPlayer, activationItem.copy());
            } else if (id.equals("goetydelight:ritual/dimensional_jell")) {
                returnSpecialItems(world, darkAltarPos, castingPlayer, activationItem.copy());
            }
        }

        world.playSound(null, darkAltarPos, SoundEvents.BELL_RESONATE,
                SoundSource.BLOCKS, 1.0F, 0.5F);

        for (int i = 0; i < 20; i++) {
            world.addParticle(ParticleTypes.HEART,
                    darkAltarPos.getX() + 0.5 + world.random.nextGaussian() * 0.5,
                    darkAltarPos.getY() + 1.5,
                    darkAltarPos.getZ() + 0.5 + world.random.nextGaussian() * 0.5,
                    0, 0.1, 0);
        }
    }

    private void returnSpecialItems(Level world, BlockPos darkAltarPos, Player player, ItemStack returnItem) {
        if (player != null) {
            if (!player.getInventory().add(returnItem)) {
                player.drop(returnItem, false);
            }
            world.playSound(null, darkAltarPos, SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            Block.popResource(world, darkAltarPos, returnItem);
            world.playSound(null, darkAltarPos, SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}