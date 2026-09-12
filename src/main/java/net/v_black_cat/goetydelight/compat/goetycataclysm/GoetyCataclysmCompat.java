package net.v_black_cat.goetydelight.compat.goetycataclysm;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public final class GoetyCataclysmCompat {

    /** 本模块专属的注册器：物品、方块、效果等按需新增，全部挂这里 */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoetyDelight.MODID);

    private GoetyCataclysmCompat() {
    }

    /** 模组构造期：把本模块的 DeferredRegister 挂到 mod 事件总线 */
    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        GoetyDelight.LOGGER.info("[compat] goety_cataclysm 联动已挂载");
    }

    /** 注册表冻结后（FMLCommonSetupEvent）：需要读取注册表内容的初始化写这里 */
    public static void commonSetup() {
    }

    /** 客户端初始化（Dist.CLIENT 下调用）：渲染器/模型层等 */
    public static void clientSetup() {
    }
}
