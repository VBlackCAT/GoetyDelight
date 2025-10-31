package net.v_black_cat.goetydelight.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.ModEntityType;

public class ModSpawnEggs {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GoetyDelight.MODID);

    public static void init(){
        ModSpawnEggs.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

        public static final RegistryObject<ModSpawnEggItem> GHOST_FARMER_SPAWN_EGG = ITEMS.register("ghost_farmer_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityType.ABSTRACTWRAITH, 0x05071624, 0xf5da2a, egg()));

    public static Item.Properties egg(){
        return new Item.Properties();
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
