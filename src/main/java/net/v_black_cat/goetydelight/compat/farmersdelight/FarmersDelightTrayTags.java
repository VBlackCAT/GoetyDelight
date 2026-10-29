package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.world.level.block.state.BlockState;

/**
 * 统一入口：判断方块状态是否属于 Farmer's Delight 的“托盘热源”标签。
 * <p>
 * 内部走反射“先试 1.3.0+ 的 {@code ModTags$Blocks.TRAY_HEAT_SOURCES}，
 * 失败再试 &lt;1.3.0 的 {@code ModTags.TRAY_HEAT_SOURCES}”，
 * 不依赖版本号，也不会因 import/类引用导致 NoClassDefFoundError。
 */
public final class FarmersDelightTrayTags {

    private FarmersDelightTrayTags() {
    }

    public static boolean isTrayHeatSource(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        return FarmersDelightTrayTagsLegacy.isTrayHeatSource(state);
    }
}