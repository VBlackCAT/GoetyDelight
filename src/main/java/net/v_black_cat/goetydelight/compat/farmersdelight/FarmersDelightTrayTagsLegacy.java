package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 托盘热源标签判定。
 * <p>
 * 直接使用 Farmer's Delight 的数据包标签 {@code farmersdelight:tray_heat_sources}，
 * 不再反射 Minecraft 的 {@code BlockState#is}（生产环境方法名会被重映射为 SRG 名）。
 */
final class FarmersDelightTrayTagsLegacy {

    private static final TagKey<Block> TRAY_HEAT_SOURCES = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("farmersdelight", "tray_heat_sources"));

    private FarmersDelightTrayTagsLegacy() {
    }

    static boolean isTrayHeatSource(BlockState state) {
        return state != null && state.is(TRAY_HEAT_SOURCES);
    }
}
