package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.world.level.block.state.BlockState;

/**
 * 统一入口：判断方块状态是否属于 Farmer's Delight 的“托盘热源”标签。
 * <p>
 * 直接使用数据包标签 {@code farmersdelight:tray_heat_sources}，不依赖版本号，
 * 也不会在生产环境因为反射 Minecraft 方法名（SRG 重映射）而失效。
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