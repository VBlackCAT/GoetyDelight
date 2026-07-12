package net.v_black_cat.goetydelight.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, GoetyDelight.MODID);

    // 示例方块实体（需替换为实际类）
    // public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<YourBlockEntity>> YOUR_BE =
    //         BLOCK_ENTITIES.register("your_be", () -> BlockEntityType.Builder.of(
    //                 YourBlockEntity::new, ModBlocks.YOUR_BLOCK.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}