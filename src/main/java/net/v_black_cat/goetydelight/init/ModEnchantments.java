package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENCHANTMENT, GoetyDelight.MODID);

    // 示例附魔（需替换为实际附魔类）
    // public static final DeferredHolder<Enchantment, Enchantment> EXAMPLE_ENCHANT =
    //         ENCHANTMENTS.register("example_enchant", () -> new YourEnchantment());

    public static void register(IEventBus modEventBus) {
        ENCHANTMENTS.register(modEventBus);
    }
}