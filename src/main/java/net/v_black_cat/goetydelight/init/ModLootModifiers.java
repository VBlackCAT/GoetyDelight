package net.v_black_cat.goetydelight.init;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModLootModifiers {
    // 正确类型：MapCodec<? extends IGlobalLootModifier>
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(
                    net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    GoetyDelight.MODID
            );

    // 示例：注册一个战利品修改器的序列化器（MapCodec）
    // 你需要先实现一个具体的 IGlobalLootModifier 子类（如 MyLootModifier），并在其中定义 CODEC
    // public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<MyLootModifier>> EXAMPLE_MODIFIER =
    //         LOOT_MODIFIER_SERIALIZERS.register("example_modifier", () -> MyLootModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}