package net.v_black_cat.goetydelight.compat.ftbultimine;

import com.Polarice3.Goety.common.blocks.SnapWartsBlock;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.ItemCollection;
import dev.ftb.mods.ftbultimine.crops.CropLikeRegistry;
import dev.ftb.mods.ftbultimine.crops.ICropLikeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApocalyptiumKnifeItem;

/**
 * FTB Ultimine 联动。
 * <p>
 * FTB Ultimine 自带的原版作物处理器只覆盖 CropBlock / SweetBerry / Cocoa；
 * 下界疙瘩和 SnapWarts 需要额外的 {@link ICropLikeHandler} 才能进入连锁。
 * CropBlock 的镰刀逻辑通过 {@code FtbUltimineVanillaCropMixin} 注入原版处理器。
 */
public final class FtbUltimineCompat {

    private FtbUltimineCompat() {
    }

    public static void register() {
        CropLikeRegistry.getInstance().registerHandler(new NetherWartAndSnapWartsHandler());
        GoetyDelight.LOGGER.info("[compat] FTB Ultimine 作物处理器已注册");
    }

    /** FTB Ultimine 连锁键是否按下。 */
    public static boolean isActive(Player player) {
        return FTBUltimine.instance != null
                && FTBUltimine.instance.getOrCreatePlayerData(player).isPressed();
    }

    /**
     * 只负责原版处理器不覆盖的两种作物。
     * 其他作物仍交给 FTB 的原版处理器，避免破坏原有连锁行为。
     */
    private static final class NetherWartAndSnapWartsHandler implements ICropLikeHandler {

        @Override
        public boolean isApplicable(Level level, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof NetherWartBlock) {
                return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
            }
            if (state.getBlock() instanceof SnapWartsBlock) {
                return state.getValue(SnapWartsBlock.AGE) >= 2;
            }
            return false;
        }

        @Override
        public void doHarvesting(Player player, BlockPos pos, BlockState state, ItemCollection collection) {
            if (!(player.level() instanceof ServerLevel level)) {
                return;
            }

            boolean knife = player.getMainHandItem().getItem() instanceof ApocalyptiumKnifeItem;
            if (knife && ApocalyptiumKnifeItem.isHarvestable(state)) {
                for (ItemStack drop : ApocalyptiumKnifeItem.collectKnifeDrops(
                        level, pos, state, player.getMainHandItem())) {
                    collection.add(drop);
                }
                ApocalyptiumKnifeItem.shrinkHarvestState(level, pos, state);
                return;
            }

            // 非镰刀玩家：保持普通连锁收割，收获一次并重置年龄。
            for (ItemStack drop : Block.getDrops(state, level, pos, null, player, ItemStack.EMPTY)) {
                collection.add(drop);
            }
            if (state.getBlock() instanceof NetherWartBlock) {
                level.setBlock(pos, state.setValue(NetherWartBlock.AGE, 0), 3);
            } else if (state.getBlock() instanceof SnapWartsBlock) {
                level.setBlock(pos, state.setValue(SnapWartsBlock.AGE, 0), 3);
            }
            level.levelEvent(2001, pos, Block.getId(state));
        }

        @Override
        public boolean isEquivalent(BlockState first, BlockState second) {
            return first.getBlock() == second.getBlock();
        }
    }
}
