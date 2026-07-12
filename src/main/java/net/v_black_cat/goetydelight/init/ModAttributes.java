package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, GoetyDelight.MODID);

    // 示例属性（需替换为实际属性类）
    // public static final DeferredHolder<Attribute, Attribute> EXAMPLE_ATTRIBUTE =
    //         ATTRIBUTES.register("example_attribute", () -> new RangedAttribute("attribute.goetydelight.example", 0.0D, 0.0D, 1024.0D).setSyncable(true));

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }
}