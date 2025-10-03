package net.v_black_cat.goetydelight.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GoetyDelight.MODID);

//    public static final RegistryObject<BlockEntityType<GemEmpoweringStationBlockEntity>> GEM_EMPOWERING_STATION_BE =
//            BLOCK_ENTITIES.register("gem_empowering_station_block_entity", () ->
//                    BlockEntityType.Builder.of(GemEmpoweringStationBlockEntity::new,
//                            ModBlocks.GEM_EMPOWERING_STATION.get()).build(null));


    public static final RegistryObject<BlockEntityType<CursedIngotPotBlockEntity>> CURSED_INGOT_POT_BE =
            BLOCK_ENTITIES.register("cursed_ingot_pot", () ->
                    BlockEntityType.Builder.of(CursedIngotPotBlockEntity::new, ModBlocks.CURSED_INGOT_POT.get()).build(null));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}