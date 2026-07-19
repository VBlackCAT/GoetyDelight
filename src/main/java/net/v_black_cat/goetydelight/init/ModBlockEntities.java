package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.NightStoveBlockEntity;
import net.v_black_cat.goetydelight.block.ShadeStoveBlockEntity;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<
            BlockEntityType<
                    ?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoetyDelight.MODID);

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    NightStoveBlockEntity>> NIGHT_STOVE_BE = BLOCK_ENTITIES.register("night_stove", () -> BlockEntityType.Builder.of(
            NightStoveBlockEntity::new,
            ModBlocks.NIGHT_STOVE.get()
    ).build(null));

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    ShadeStoveBlockEntity>> SHADE_STOVE_BE = BLOCK_ENTITIES.register("shade_stove", () -> BlockEntityType.Builder.of(
            ShadeStoveBlockEntity::new,
            ModBlocks.SHADE_STOVE.get()
    ).build(null));
    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    CursedIngotPotBlockEntity>> CURSED_INGOT_POT_BE = BLOCK_ENTITIES.register("cursed_ingot_pot", () -> BlockEntityType.Builder.of(
            CursedIngotPotBlockEntity::new,
            ModBlocks.CURSED_INGOT_POT.get() // 确保方块已注册
    ).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}