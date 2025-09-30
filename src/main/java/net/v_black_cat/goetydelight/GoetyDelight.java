package net.v_black_cat.goetydelight;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.v_black_cat.goetydelight.effect.ModEffects;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.item.ModCreativeModTabs;
import net.v_black_cat.goetydelight.loot.RegHelper;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.recipe.ModRecipeSerializers;
import org.slf4j.Logger;

import static net.v_black_cat.goetydelight.loot.ModLootModifier.GLOBAL_LOOT_MODIFIER_CODECS;
import static net.v_black_cat.goetydelight.item.ModItems.ITEMS;
import static net.v_black_cat.goetydelight.block.ModBlocks.BLOCKS;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(GoetyDelight.MODID)
public class GoetyDelight
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "goetydelight";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();




    public GoetyDelight(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModCreativeModTabs.register(modEventBus);
        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        GLOBAL_LOOT_MODIFIER_CODECS.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        RegHelper.LOOT_CONDITIONS.register(modEventBus);
        ModEffects.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModEntities.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        //LOGGER.info("HELLO FROM COMMON SETUP");

        //if (Config.logDirtBlock)
           // LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        //LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        NetworkHandler.register();
        //Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
           // LOGGER.info("HELLO FROM CLIENT SETUP");
           // LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
