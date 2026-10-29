package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Farmer's Delight 音效的反射访问。
 * <p>
 * 1.3.0+ 提供了 {@code ModSounds.BLOCK_FOOD_TAKE_PORTION}，
 * 1.2.x 没有此字段，此时回退到原版的 {@link SoundEvents#ARMOR_EQUIP_GENERIC}。
 */
public final class FarmersDelightSoundsLegacy {

    private static final String MOD_SOUNDS_CLASS = "vectorwing.farmersdelight.common.registry.ModSounds";
    private static final String FOOD_TAKE_FIELD = "BLOCK_FOOD_TAKE_PORTION";

    private static SoundEvent foodTakePortion;
    private static boolean resolved = false;

    private FarmersDelightSoundsLegacy() {
    }

    /**
     * 获取取餐音效。
     * 1.3.0+ 返回 ModSounds.BLOCK_FOOD_TAKE_PORTION.get()
     * 1.2.x 返回 SoundEvents.ARMOR_EQUIP_GENERIC
     */
    public static SoundEvent getFoodTakePortion() {
        resolve();
        return foodTakePortion != null ? foodTakePortion : SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        try {
            Class<?> clazz = Class.forName(MOD_SOUNDS_CLASS);
            // 尝试获取字段，1.2.x 会在这里抛出 NoSuchFieldException
            Field field = clazz.getField(FOOD_TAKE_FIELD);
            Object registryObject = field.get(null);
            if (registryObject != null) {
                // 调用 RegistryObject.get() 获取实际的 SoundEvent
                Method get = registryObject.getClass().getMethod("get");
                Object sound = get.invoke(registryObject);
                if (sound instanceof SoundEvent) {
                    foodTakePortion = (SoundEvent) sound;
                }
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            // 1.2.x 环境或农夫乐事未加载，安全降级，保留 null
        }
    }
}