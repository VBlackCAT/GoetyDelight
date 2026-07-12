package net.v_black_cat.goetydelight.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, GoetyDelight.MODID);

    // 示例实体（需替换为实际实体类）
    // public static final DeferredHolder<EntityType<?>, EntityType<YourEntity>> YOUR_ENTITY =
    //         ENTITIES.register("your_entity", () -> EntityType.Builder.of(YourEntity::new, MobCategory.CREATURE)
    //                 .sized(0.6f, 1.8f).build("your_entity"));

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}