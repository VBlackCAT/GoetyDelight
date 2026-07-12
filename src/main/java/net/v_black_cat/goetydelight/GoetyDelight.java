package net.v_black_cat.goetydelight;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.v_black_cat.goetydelight.events.AddCreativeHandler;
import net.v_black_cat.goetydelight.events.CommonSetupHandler;
import net.v_black_cat.goetydelight.events.ServerStartingHandler;
import net.v_black_cat.goetydelight.init.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GoetyDelight.MODID)
public class GoetyDelight {
    public static final String MODID = "goetydelight";
    public static final Logger LOGGER = LoggerFactory.getLogger(GoetyDelight.class);

    public GoetyDelight(IEventBus modEventBus, ModContainer modContainer) {
        // 注册所有内容
        registerAllDeferred(modEventBus);

        // 注册所有事件监听器
        registerEvents(modEventBus);

        // 注册配置
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
    }

    private static void registerAllDeferred(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModAttributes.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModEntities.register(modEventBus);
        ModGameEvents.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModParticles.register(modEventBus);
        ModPotions.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModFluids.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModTrimMaterials.register(modEventBus);
        ModTrimPatterns.register(modEventBus);
        ModJukeboxSongs.register(modEventBus);
        ModBiomeModifiers.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModConfiguredFeatures.register(modEventBus);
        ModPlacedFeatures.register(modEventBus);
        ModStructures.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModAttachments.register(modEventBus);
    }

    /**
     * 集中注册所有事件监听器
     */
    private void registerEvents(IEventBus modEventBus) {
        // Mod 总线事件
        modEventBus.addListener(CommonSetupHandler::onCommonSetup);
        modEventBus.addListener(AddCreativeHandler::onAddCreative);

        // 游戏总线事件
        NeoForge.EVENT_BUS.addListener(ServerStartingHandler::onServerStarting);
    }
}