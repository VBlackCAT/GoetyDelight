package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 物品标签的反射实现。
 * <p>
 * 探测顺序：
 * <ol>
 *   <li>1.3.0+：{@code ModTags$Items.SERVING_CONTAINERS}</li>
 *   <li>&lt;1.3.0：{@code ModTags.SERVING_CONTAINERS}</li>
 * </ol>
 */
final class FarmersDelightItemTagsLegacy {

    private static final String MOD_TAGS_CLASS = "vectorwing.farmersdelight.common.tag.ModTags";
    private static final String MOD_TAGS_ITEMS_CLASS = MOD_TAGS_CLASS + "$Items";
    private static final String FIELD_NAME = "SERVING_CONTAINERS";

    private static TagKey<Item> servingContainersTag;
    private static Method isTagMethod;
    private static boolean resolved = false;

    private FarmersDelightItemTagsLegacy() {
    }

    @SuppressWarnings("unchecked")
    static boolean isServingContainer(ItemStack stack) {
        resolve();

        if (servingContainersTag == null || isTagMethod == null) {
            return false;
        }

        try {
            return (Boolean) isTagMethod.invoke(stack, servingContainersTag);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        // 先试 1.3.0+：ModTags$Items.SERVING_CONTAINERS
        servingContainersTag = readTagField(MOD_TAGS_ITEMS_CLASS);
        // 再试 <1.3.0：ModTags.SERVING_CONTAINERS
        if (servingContainersTag == null) {
            servingContainersTag = readTagField(MOD_TAGS_CLASS);
        }

        try {
            // ItemStack.is(TagKey<Item>)
            isTagMethod = ItemStack.class.getMethod("is", TagKey.class);
        } catch (NoSuchMethodException e) {
            isTagMethod = null;
        }
    }

    @SuppressWarnings("unchecked")
    private static TagKey<Item> readTagField(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getField(FIELD_NAME);
            return (TagKey<Item>) field.get(null);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
}