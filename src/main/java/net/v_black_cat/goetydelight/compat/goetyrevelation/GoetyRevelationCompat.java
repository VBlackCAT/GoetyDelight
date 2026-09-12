package net.v_black_cat.goetydelight.compat.goetyrevelation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public final class GoetyRevelationCompat {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoetyDelight.MODID);

    private GoetyRevelationCompat() {
    }

    /** 模组构造期：把本模块的 DeferredRegister 挂到 mod 事件总线 */
    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        GoetyDelight.LOGGER.info("[compat] goety_revelation 联动已挂载");
    }

    /** 注册表冻结后（FMLCommonSetupEvent）：需要读取注册表内容的初始化写这里 */
    public static void commonSetup() {
        // 例：给某个物品注册燃料、给村民注册交易、注册自定义配方类型的使用等
    }

    /** 客户端初始化（Dist.CLIENT 下调用）：渲染器/模型层等 */
    public static void clientSetup() {
        // 例：EntityRenderersEvent.RegisterRenderers 里注册本模块实体的渲染器
    }
}
