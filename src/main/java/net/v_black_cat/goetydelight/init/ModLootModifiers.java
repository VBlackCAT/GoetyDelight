package net.v_black_cat.goetydelight.init;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.loot.ModLootModifier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    GoetyDelight.MODID
            );

    /**
     * 与 1.20.1 相同的序列化器 ID，供 data/goetydelight/loot_modifiers/*.json 引用。
     */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ModLootModifier>> LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("goetydelight_loot_modifier", () -> ModLootModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}
