package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Supplier;

/**
 * 提供延迟加载的效果（MobEffect）供应商。
 * 用于避免在物品注册时因效果未加载而导致的错误。
 */
public final class ModEffectSuppliers {

    private ModEffectSuppliers() {} // 禁止实例化

    public static final Supplier<MobEffect> COMFORT = farmersDelightBuff("comfort");
    public static final Supplier<MobEffect> NOURISHMENT = farmersDelightBuff("nourishment");
    public static final Supplier<MobEffect> WILD_RAGE = goetyBuff("wild_rage");
    public static final Supplier<MobEffect> RAMPAGE = goetyBuff("rampage");
    public static final Supplier<MobEffect> FORTUNATE = goetyBuff("fortunate");
    public static final Supplier<MobEffect> CHILL_HIDE = goetyBuff("chill_hide");
    public static final Supplier<MobEffect> CORPSE_EATER = goetyBuff("corpse_eater");
    public static final Supplier<MobEffect> SHADOW_WALK = goetyBuff("shadow_walk");
    public static final Supplier<MobEffect> CLIMBING = goetyBuff("climbing");
    public static final Supplier<MobEffect> FROG_LEG = goetyBuff("frog_leg");
    public static final Supplier<MobEffect> CHARGED = goetyBuff("charged");
    public static final Supplier<MobEffect> SOUL_ARMOR = goetyBuff("soul_armor");
    public static final Supplier<MobEffect> BUFF = goetyBuff("buff");
    public static final Supplier<MobEffect> SAVE_EFFECTS = goetyBuff("save_effects");
    public static final Supplier<MobEffect> PHOTOSYNTHESIS = goetyBuff("photosynthesis");
    public static final Supplier<MobEffect> FROSTY_AURA = goetyBuff("frosty_aura");
    public static final Supplier<MobEffect> FIERY_AURA = goetyBuff("fiery_aura");
    public static final Supplier<MobEffect> ILLAGUE = goetyBuff("illague");
    public static final Supplier<MobEffect> VENOMOUS_HANDS = goetyBuff("venomous_hands");

    private static Supplier<MobEffect> goetyBuff(String effectId) {
        return () -> BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse("goety:" + effectId));
    }

    private static Supplier<MobEffect> farmersDelightBuff(String effectId) {
        return () -> BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse("farmersdelight:" + effectId));
    }
}