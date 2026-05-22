package net.v_black_cat.goetydelight.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.event.ModRegisterEvent;

import java.util.stream.Stream;

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

    public static final RegistryObject<BlockEntityType<RestaurantBlockEntity>> RESTAURANT_BE = BLOCK_ENTITIES.register("restaurant",
            () -> BlockEntityType.Builder.of(RestaurantBlockEntity::new, ModBlocks.RESTAURANT.get()).build(null));

    public static RegistryObject<BlockEntityType<CustomDollBlockEntity>> CUSTOM_DOLL_BE = BLOCK_ENTITIES.register("custom_doll", () ->
            BlockEntityType.Builder.of(CustomDollBlockEntity::new, getDollBlocks()).build(null));

    private static Block[] getDollBlocks() {
        return Stream.concat(Stream.of(ModBlocks.CUSTOM_DOLL.get()), ModRegisterEvent.DOLL_BLOCKS.values().stream())
                .toArray(Block[]::new);
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}