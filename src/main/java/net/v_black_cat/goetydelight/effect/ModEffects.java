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

    public static final RegistryObject<MobEffect> IT_STINKS =
            EFFECTS.register("it_stinks", ItStinksEffect::new);
    public static final RegistryObject<MobEffect> TAINTED_DRINK =
            EFFECTS.register("tainted_drink", TaintedDrinkEffect::new);

    public static final RegistryObject<MobEffect> TAINTED_PIG =
            EFFECTS.register("tainted_pig", TaintedPigEffect::new);

    public static final RegistryObject<MobEffect> SPELL_MASTERY =
            EFFECTS.register("spell_mastery", SpellMasteryEffect::new);
    public static final RegistryObject<MobEffect> SPELL_DURATION =
            EFFECTS.register("spell_duration", SpellDurationEffect::new);

    public static final RegistryObject<MobEffect> NIGHT_HEART_PEA_SOUP =
            EFFECTS.register("night_heart_pea_soup", NightHeartPeaSoupEffect::new);
    public static final RegistryObject<MobEffect> SOUL_CONVERGENCE_ROOM =
            EFFECTS.register("soul_convergence_room", SoulConvergenceRoomEffect::new);

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}