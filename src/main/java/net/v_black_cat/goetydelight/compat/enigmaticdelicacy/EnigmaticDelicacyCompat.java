package net.v_black_cat.goetydelight.compat.enigmaticdelicacy;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;

/**
 * enigmaticdelicacy 联动模块（对齐 1.21.1 优化版：register / commonSetup / clientSetup 三段式）。
 */
public final class EnigmaticDelicacyCompat {

    /** 本模块专属的注册器：物品、方块、效果等按需新增，全部挂这里 */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GoetyDelight.MODID);

    /** 本模块专属的方块注册器 */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GoetyDelight.MODID);

    private EnigmaticDelicacyCompat() {
    }

    /** 模组构造期：把本模块的 DeferredRegister 挂到 mod 事件总线 */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        GoetyDelight.LOGGER.info("[compat] enigmaticdelicacy 联动已挂载");
    }

    /** 注册表冻结后（FMLCommonSetupEvent）：需要读取注册表内容的初始化写这里 */
    public static void commonSetup() {
    }

    /** 客户端初始化（Dist.CLIENT 下调用）：渲染器/模型层等 */
    public static void clientSetup() {
    }
}
