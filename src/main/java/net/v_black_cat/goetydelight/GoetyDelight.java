package net.v_black_cat.goetydelight;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.v_black_cat.goetydelight.events.*;
import net.v_black_cat.goetydelight.init.*;
import net.v_black_cat.goetydelight.visual.GDVisualEffects;
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
//        ModEnchantments.register(modEventBus);
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
        ModBuffTypes.register(modEventBus);
        ModRituals.register(modEventBus);
        GDVisualEffects.register(modEventBus);
    }

    /**
     * 集中注册所有事件监听器
     */
    private void registerEvents(IEventBus modEventBus) {
        // Mod 总线事件
        modEventBus.addListener(CommonSetupHandler::onCommonSetup);
        modEventBus.addListener(AddCreativeHandler::onAddCreative);
        modEventBus.addListener(RegisterPayloadHandlersEventHandler::register);
        modEventBus.addListener(ModEntityAttributesHandler::onEntityAttributeCreation);

        // 游戏总线事件
        NeoForge.EVENT_BUS.addListener(ServerStartingHandler::onServerStarting);
        NeoForge.EVENT_BUS.addListener(ServerTickEventHandler::onServerTick);
        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEventHandler::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(EntityTickEventHandler::onEntityTick);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEventHandler::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(AttackEntityEventHandler::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(LivingDamageEventHandler::onLivingHurtPre);
        NeoForge.EVENT_BUS.addListener(LivingDamageEventHandler::onLivingHurtPost);
        NeoForge.EVENT_BUS.addListener(MobEffectEventHandler::onEffectApplicable);
        NeoForge.EVENT_BUS.addListener(MobEffectEventHandler::onEffectAdded);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEventHandler::onLeftClickEmpty);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEventHandler::onLivingIncomingDamage);
        NeoForge.EVENT_BUS.addListener(LivingChangeTargetEventHandler::onLivingChangeTarget);
        NeoForge.EVENT_BUS.addListener(PlayerTickEventHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(PlayerTickEventHandler::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(PlayerTickEventHandler::onPlayerDeath);
        NeoForge.EVENT_BUS.addListener(PlayerTickEventHandler::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(VisualEffectEventHandler::onLevelTick);
        NeoForge.EVENT_BUS.addListener(VisualEffectEventHandler::onStartTracking);
        NeoForge.EVENT_BUS.addListener(VisualEffectEventHandler::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(VisualEffectEventHandler::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(GhostFarmerBlockBreakHandler::onBlockBreak);

    }
}
