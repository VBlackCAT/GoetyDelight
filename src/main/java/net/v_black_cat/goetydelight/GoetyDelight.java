package net.v_black_cat.goetydelight;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ComposterBlock;
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
import net.v_black_cat.goetydelight.ability.AbilityRegistry;
import net.v_black_cat.goetydelight.bedrock.BedrockModel;
import net.v_black_cat.goetydelight.block.ModBlockEntities;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.block.RenderBlockRenderer;
import net.v_black_cat.goetydelight.block.RestaurantBlockRenderer;
import net.v_black_cat.goetydelight.config.Config;
import net.v_black_cat.goetydelight.effect.ModEffects;
import net.v_black_cat.goetydelight.enchantments.ModEnchantments;
import net.v_black_cat.goetydelight.entities.GhostFarmerRenderer;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.entities.ModEntityDataSerializers;
import net.v_black_cat.goetydelight.entities.ai.ModActivity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.entities.ai.ModSensor;
import net.v_black_cat.goetydelight.item.ModCreativeModTabs;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.loot.RegHelper;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.recipe.ModRecipeSerializers;
import net.v_black_cat.goetydelight.render.DollEntityRender;
import net.v_black_cat.goetydelight.render.animation.RotationEffectHandler;

import net.v_black_cat.goetydelight.ritual.DelightRitualType;
import net.v_black_cat.goetydelight.screen.*;
import net.v_black_cat.goetydelight.structures.ModStructurePieceTypes;
import net.v_black_cat.goetydelight.structures.ModStructureProcessorTypes;
import net.v_black_cat.goetydelight.structures.ModStructures;
import net.v_black_cat.goetydelight.util.ModSounds;
import org.slf4j.Logger;

import java.io.InputStream;

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

        ModSounds.SOUND_EVENTS.register(modEventBus);

        GLOBAL_LOOT_MODIFIER_CODECS.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        RegHelper.LOOT_CONDITIONS.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        ModEffects.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModEntities.register(modEventBus);
        AbilityRegistry.registerAbilities();
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModActivity.register(modEventBus);
        ModSensor.register(modEventBus);
        ModMemory.register(modEventBus);
        ModEntityDataSerializers.register(modEventBus);

        // 注册结构和结构片段、结构处理器
        ModStructures.register(modEventBus);
        ModStructurePieceTypes.register(modEventBus);
        ModStructureProcessorTypes.register(modEventBus);
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

        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(ModItems.METAMORPHIC_SCENT_GRASS.get(), 0.2F);
            ComposterBlock.COMPOSTABLES.put(ModItems.METAMORPHIC_SCENT_FRUIT.get(), 0.75F);
            ComposterBlock.COMPOSTABLES.put(ModItems.METAMORPHIC_SCENT_GRASS_SEEDS.get(), 0.05F);
            ComposterBlock.COMPOSTABLES.put(ModItems.ECTOPLASMIC_MELON.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModItems.ECTOPLASMIC_MELON_SEEDS.get(), 0.09F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.ECTOPLASMIC_MELON_BLOCK.get().asItem(), 0.95F);
            DelightRitualType.registerRitualType();
        });

        
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
            MinecraftForge.EVENT_BUS.addListener(RotationEffectHandler::onRenderTick);
            MinecraftForge.EVENT_BUS.addListener(RotationEffectHandler::onRenderLivingEvent);
            MenuScreens.register(ModMenuTypes.CURSED_INGOT_POT.get(), CursedIngotPotScreen::new);
            MenuScreens.register(ModMenuTypes.SHADE_STOVE.get(), ShadeStoveScreen::new);
            MenuScreens.register(ModMenuTypes.NIGHT_STOVE.get(), NightStoveScreen::new);
            MenuScreens.register(ModMenuTypes.RESTAURANT.get(), RestaurantScreen::new);
            BlockEntityRenderers.register(ModBlockEntities.RENDER_BLOCK.get(), RenderBlockRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.RESTAURANT_BE.get(), RestaurantBlockRenderer::new);
            EntityRenderers.register(ModEntities.GHOST_FARMER.get(), GhostFarmerRenderer::new);
            EntityRenderers.register(ModEntities.DOLL_ENTITY.get(),DollEntityRender::new);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ECTOPLASMIC_MELON_STEM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ATTACHED_ECTOPLASMIC_MELON_STEM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DRIPMARBLE_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ROYAL_CAKE_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ECTOPLASMIC_MELON_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BOAT_STUFFED_ROASTED_WARDEN_BlOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.METAMORPHIC_SCENT_GRASS.get(), RenderType.cutout());
        }
    }
}