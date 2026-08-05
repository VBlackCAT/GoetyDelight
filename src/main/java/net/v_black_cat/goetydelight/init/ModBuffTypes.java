package net.v_black_cat.goetydelight.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.buff.BuffType;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.buff.effect.impl.*;

import java.util.*;
import java.util.function.Supplier;


public class ModBuffTypes {

    public static final ResourceKey<Registry<BuffType>> BUFF_REGISTRY_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(GoetyDelight.MODID, "buff_types"));

    public static final DeferredRegister<BuffType> BUFF_TYPES =
            DeferredRegister.create(BUFF_REGISTRY_KEY, GoetyDelight.MODID);

    public static final Supplier<IForgeRegistry<BuffType>> BUFF_REGISTRY =
            BUFF_TYPES.makeRegistry(() -> new RegistryBuilder<BuffType>()
                    .disableSaving()
                    .disableSync()
                    .disableOverrides()
            );

    private static final Map<ResourceLocation, BuffEffect> EFFECTS = new HashMap<>();

    private static RegistryObject<BuffType> registerWithEffect(String name, BuffType type, BuffEffect effect) {
        RegistryObject<BuffType> holder = BUFF_TYPES.register(name, () -> type);
        EFFECTS.put(holder.getId(), effect);
        return holder;
    }

    public static BuffEffect getEffect(ResourceLocation typeId) {
        return EFFECTS.get(typeId);
    }



    public static final RegistryObject<BuffType> FREEZE_IMMUNITY =
            registerWithEffect("freeze_immunity", new BuffType(-1, 0, false), new FreezeImmunityBuffEffect());

    public static final RegistryObject<BuffType> SUGAR_SCEPTER_IMMUNITY =
            registerWithEffect("sugar_scepter_immunity", new BuffType(-1, 0, false), new SugarScepterImmunityBuffEffect());

    public static final RegistryObject<BuffType> RUBY_HARD_CANDY_DAMAGE_REDUCTION =
            registerWithEffect("ruby_hard_candy_damage_reduction", new BuffType(-1, 0, false), new RubyHardCandyDamageReductionBuffEffect());

    public static final RegistryObject<BuffType> NIGHT_STOVE =
            registerWithEffect("night_stove", new BuffType(-1, 0, false), new NightStoveBuffEffect());

    public static final RegistryObject<BuffType> CRIMSON_MEMORIES =
            registerWithEffect("crimson_memories", new BuffType(-1, 0, false), new CrimsonMemoriesBuffEffect());

    public static final RegistryObject<BuffType> PERMANENT_FIRE_RESISTANCE =
            registerWithEffect("permanent_fire_resistance", new BuffType(-1, 0, false), new PermanentFireResistanceBuffEffect());

    public static final RegistryObject<BuffType> PERMANENT_SAVE_EFFECTS =
            registerWithEffect("permanent_save_effects", new BuffType(-1, 0, false), new PermanentSaveEffectsBuffEffect());

    public static final RegistryObject<BuffType> FOR_WARDEN_EFFECT =
            registerWithEffect("for_warden_effect", new BuffType(-1, 0, false), new ForWardenEffectBuffEffect());

    public static final RegistryObject<BuffType> WARDEN_DETECTED =
            registerWithEffect("warden_detected", new BuffType(-1, 0, false), (entity, amp) -> {});

    public static void register(IEventBus modEventBus) {
        BUFF_TYPES.register(modEventBus);
    }
}
