package net.v_black_cat.goetydelight.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GoetyDelight.MODID);

    public static final RegistryObject<BlockEntityType<CursedIngotPotBlockEntity>> CURSED_INGOT_POT_BE =
            BLOCK_ENTITIES.register("cursed_ingot_pot", () ->
                    BlockEntityType.Builder.of(CursedIngotPotBlockEntity::new,
                            ModBlocks.CURSED_INGOT_POT.get()).build(null));

    public static final RegistryObject<BlockEntityType<ShadeStoveBlockEntity>> SHADE_STOVE_BE =
            BLOCK_ENTITIES.register("shade_stove", () ->
                    BlockEntityType.Builder.of(ShadeStoveBlockEntity::new,
                            ModBlocks.SHADE_STOVE.get()).build(null));


    public static final RegistryObject<BlockEntityType<RenderBlockEntity>> RENDER_BLOCK =
            BLOCK_ENTITIES.register("render_block", () ->
                    BlockEntityType.Builder.of(RenderBlockEntity::new,
                            ModBlocks.RENDER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<NightStoveBlockEntity>> NIGHT_STOVE_BE = BLOCK_ENTITIES.register("night_stove",
            () -> BlockEntityType.Builder.of(NightStoveBlockEntity::new, ModBlocks.NIGHT_STOVE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}