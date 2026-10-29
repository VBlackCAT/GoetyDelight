package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 托盘热源标签的反射实现。
 * <p>
 * 探测顺序：
 * <ol>
 *   <li>1.3.0+：{@code vectorwing.farmersdelight.common.tag.ModTags$Blocks.TRAY_HEAT_SOURCES}</li>
 *   <li>&lt;1.3.0：{@code vectorwing.farmersdelight.common.tag.ModTags.TRAY_HEAT_SOURCES}</li>
 * </ol>
 * 哪个字段先反射到就用哪个；都拿不到则返回 false（功能降级，不崩）。
 */
final class FarmersDelightTrayTagsLegacy {

    private static final String MOD_TAGS_CLASS = "vectorwing.farmersdelight.common.tag.ModTags";
    private static final String MOD_TAGS_BLOCKS_CLASS = MOD_TAGS_CLASS + "$Blocks";
    private static final String FIELD_NAME = "TRAY_HEAT_SOURCES";

    private static TagKey<Block> trayHeatTag;
    private static Method isTagMethod;
    private static boolean resolved = false;

    private FarmersDelightTrayTagsLegacy() {
    }

    @SuppressWarnings("unchecked")
    static boolean isTrayHeatSource(BlockState state) {
        resolve();

        if (trayHeatTag == null || isTagMethod == null) {
            // 解析失败，保守返回 false，避免崩溃
            return false;
        }

        try {
            return (Boolean) isTagMethod.invoke(state, trayHeatTag);
        } catch (ReflectiveOperationException e) {
            // 调用失败，保守返回 false
            return false;
        }
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        // 先试 1.3.0+：ModTags$Blocks.TRAY_HEAT_SOURCES
        trayHeatTag = readTagField(MOD_TAGS_BLOCKS_CLASS);
        // 再试 <1.3.0：ModTags.TRAY_HEAT_SOURCES
        if (trayHeatTag == null) {
            trayHeatTag = readTagField(MOD_TAGS_CLASS);
        }

        try {
            // BlockState.is(TagKey<Block>)
            isTagMethod = BlockState.class.getMethod("is", TagKey.class);
        } catch (NoSuchMethodException e) {
            isTagMethod = null;
        }
    }

    @SuppressWarnings("unchecked")
    private static TagKey<Block> readTagField(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getField(FIELD_NAME);
            return (TagKey<Block>) field.get(null);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
}