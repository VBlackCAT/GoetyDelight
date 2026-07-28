package net.v_black_cat.goetydelight.init;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.effect.*;

public class ModEffects {
    public static final DeferredRegister<
            MobEffect> EFFECTS = DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT, GoetyDelight.MODID);

    public static final DeferredHolder<
            MobEffect,
            MobEffect> THE_PALE_MESSENGER = EFFECTS.register("the_pale_messenger", TaintedDrinkEffect
            ::new);

    public static final DeferredHolder<
            MobEffect,
            MobEffect> ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT = EFFECTS.register("zombified_piglin_brute_servant_support", TaintedPigEffect
            ::new);

    public static final DeferredHolder<
            MobEffect,
            MobEffect> SPELL_MASTERY = EFFECTS.register("spell_mastery", SpellMasteryEffect::new);

    public static final DeferredHolder<
            MobEffect,
            MobEffect> SPELL_DURATION = EFFECTS.register("spell_duration", SpellDurationEffect
            ::new);

    public static final DeferredHolder<
            MobEffect,
            MobEffect> SERVANT_REINFORCEMENT = EFFECTS.register("servant_reinforcement", NightHeartPeaSoupEffect
            ::new);

    public static final DeferredHolder<
            MobEffect,
            MobEffect> HUNTING_DENIAL = EFFECTS.register("hunting_denial", SoulConvergenceRoomEffect
            ::new);

    public static final DeferredHolder<
            MobEffect, MobEffect> HYDRATION = EFFECTS.register("hydration", HydrationEffect::new);

    public static final DeferredHolder<
            MobEffect,
            MobEffect> CRIMSON_MEMORIES = EFFECTS.register("crimson_memories", CrimsonMemoriesEffect
            ::new);

    public static final DeferredHolder<
            MobEffect, MobEffect> WIGHT_DENIAL = EFFECTS.register("wight_denial", WightDenialEffect
            ::new);

    public static final DeferredHolder<
            MobEffect, MobEffect> VOID_AFFIX = EFFECTS.register("void_affix", VoidAffixEffect::new);

    public static final DeferredHolder<
            MobEffect, MobEffect> TINGLING = EFFECTS.register("tingling", TinglingEffect::new);

 //   public static final DeferredHolder<
        //    MobEffect, MobEffect> WARDEN = EFFECTS.register("warden", WardenEffect::new);

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
