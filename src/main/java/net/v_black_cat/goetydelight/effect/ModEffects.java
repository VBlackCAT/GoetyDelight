package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import static net.v_black_cat.goetydelight.GoetyDelight.MODID;


public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    public static final RegistryObject<MobEffect> THE_PALE_MESSRNGER =
            EFFECTS.register("the_pale_messenger", TaintedDrinkEffect::new);

    public static final RegistryObject<MobEffect> ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT =
            EFFECTS.register("zombified_piglin_brute_servant_support", TaintedPigEffect::new);

    public static final RegistryObject<MobEffect> SPELL_MASTERY =
            EFFECTS.register("spell_mastery", SpellMasteryEffect::new);
    public static final RegistryObject<MobEffect> SPELL_DURATION =
            EFFECTS.register("spell_duration", SpellDurationEffect::new);

    public static final RegistryObject<MobEffect> SERVANT_REINFORCEMENT =
            EFFECTS.register("servant_reinforcement", NightHeartPeaSoupEffect::new);
    public static final RegistryObject<MobEffect> HUNTING_DENIAL =
            EFFECTS.register("hunting_denial", SoulConvergenceRoomEffect::new);
    public static final RegistryObject<MobEffect> HYDRATION =
            EFFECTS.register("hydration", HydrationEffect::new);

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}