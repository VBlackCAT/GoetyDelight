package net.v_black_cat.goetydelight.compat.goetyrevelation;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.IEventBus;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.CompatManager;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApocalyptiumKnifeItem;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.AscensionMooncakeItem;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.PiPieItem;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.QuietusMarrowItem;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.StoneSwordSkewerItem;

/**
 * goety_revelation 联动模块入口（对齐 1.21.1 优化版的三段式）。
 * 原先挂在 @Mod.EventBusSubscriber 上的 FMLCommonSetupEvent 逻辑，统一收进 commonSetup()，
 * 由 CompatRegistry 在注册表冻结后调用。
 */
public final class GoetyRevelationCompat {

    public static final String ID = "goety_revelation";

    /** 模组是否存在（启动配置开关是否放行由 CompatManager.isGoetyRevelationCompatEnabled() 判断） */
    public static final boolean IS_LOADED = CompatManager.isGoetyRevelationLoaded();

    /** 本模块的内容注册器（定义在 RevelationCompatRegistry，这里作为模块统一入口） */
    public static final DeferredRegister<Item> ITEMS = RevelationCompatRegistry.ITEMS;
    public static final DeferredRegister<Block> BLOCKS = RevelationCompatRegistry.BLOCKS;

    private GoetyRevelationCompat() {
    }

    /** 模组构造期：注册本模块的内容 */
    public static void register(IEventBus modBus) {
        RevelationCompatRegistry.register(modBus);
        GoetyDelight.LOGGER.info("[compat] goety_revelation 联动已挂载");
    }

    /** 注册表冻结后：把本模块的事件监听器挂上游戏总线 */
    public static void commonSetup() {
        MinecraftForge.EVENT_BUS.register(ApocalyptiumKnifeItem.class);
        MinecraftForge.EVENT_BUS.register(AscensionMooncakeItem.class);
        MinecraftForge.EVENT_BUS.register(PiPieItem.class);
        MinecraftForge.EVENT_BUS.register(QuietusMarrowItem.class);
        MinecraftForge.EVENT_BUS.register(StoneSwordSkewerItem.class);
    }

    /** 客户端初始化（Dist.CLIENT 下调用）：渲染器/模型层等 */
    public static void clientSetup() {
    }
}
