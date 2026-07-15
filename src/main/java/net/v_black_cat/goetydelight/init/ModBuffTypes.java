package net.v_black_cat.goetydelight.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.buff.BuffType;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.buff.effect.impl.*;

import java.util.*;

public class ModBuffTypes {
    public static final ResourceKey<Registry<BuffType>> BUFF_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "buff_types"));

    public static final DeferredRegister<BuffType> BUFF_TYPES =
            DeferredRegister.create(BUFF_REGISTRY_KEY, GoetyDelight.MODID);

    public static final Registry<BuffType> BUFF_REGISTRY = BUFF_TYPES.makeRegistry(builder -> {
        builder.sync(true);
        builder.defaultKey(ResourceLocation.withDefaultNamespace("empty"));
    });

    private static final Map<ResourceLocation, BuffEffect> EFFECTS = new HashMap<>();

    private static <T extends BuffEffect> DeferredHolder<BuffType, BuffType> registerWithEffect(
            String name, BuffType type, T effect) {
        DeferredHolder<BuffType, BuffType> holder = BUFF_TYPES.register(name, () -> type);
        EFFECTS.put(holder.getId(), effect);
        return holder;
    }

    public static BuffEffect getEffect(ResourceLocation typeId) {
        return EFFECTS.get(typeId);
    }

    // ========== 原有 Buff ==========
    public static final DeferredHolder<BuffType, BuffType> EMPTY =
            registerWithEffect("empty", new BuffType(0, 0, false), (entity, amp) -> {});
    public static final DeferredHolder<BuffType, BuffType> BURNING =
            registerWithEffect("burning", new BuffType(200, 1, false), new BurningEffect());
    public static final DeferredHolder<BuffType, BuffType> VIGOR =
            registerWithEffect("vigor", new BuffType(600, 1, true), new VigorEffect());

    // ========== 新能力 Buff ==========
    // 注意：所有持续时间设为 -1 表示无限（除非食物本身有有限时长）
    public static final DeferredHolder<BuffType, BuffType> FREEZE_IMMUNITY =
            registerWithEffect("freeze_immunity", new BuffType(-1, 0, false), new FreezeImmunityBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> SUGAR_SCEPTER_IMMUNITY =
            registerWithEffect("sugar_scepter_immunity", new BuffType(-1, 0, false), new SugarScepterImmunityBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> RUBY_HARD_CANDY_DAMAGE_REDUCTION =
            registerWithEffect("ruby_hard_candy_damage_reduction", new BuffType(-1, 0, false), new RubyHardCandyDamageReductionBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> NIGHT_STOVE =
            registerWithEffect("night_stove", new BuffType(-1, 0, false), new NightStoveBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> CRIMSON_MEMORIES =
            registerWithEffect("crimson_memories", new BuffType(-1, 0, false), new CrimsonMemoriesBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> PERMANENT_FIRE_RESISTANCE =
            registerWithEffect("permanent_fire_resistance", new BuffType(-1, 0, false), new PermanentFireResistanceBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> PERMANENT_SAVE_EFFECTS =
            registerWithEffect("permanent_save_effects", new BuffType(-1, 0, false), new PermanentSaveEffectsBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> FOR_WARDEN_EFFECT =
            registerWithEffect("for_warden_effect", new BuffType(-1, 0, false), new ForWardenEffectBuffEffect());
    public static final DeferredHolder<BuffType, BuffType> WARDEN_DETECTED =
            registerWithEffect("warden_detected", new BuffType(-1, 0, false), new WardenDetectedBuffEffect());

    // MinionBoost 比较特殊，使用 stackable = true，amplifier 代表累计层数
    public static final DeferredHolder<BuffType, BuffType> MINION_BOOST =
            registerWithEffect("minion_boost", new BuffType(-1, 1, true), new MinionBoostBuffEffect());

    public static void register(IEventBus modEventBus) {
        BUFF_TYPES.register(modEventBus);
    }
}