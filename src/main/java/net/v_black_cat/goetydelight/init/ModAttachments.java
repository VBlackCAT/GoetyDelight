package net.v_black_cat.goetydelight.init;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.function.Supplier;

public class ModAttachments {
    // 创建 DeferredRegister，注册到 NeoForge 的 ATTACHMENT_TYPES 注册表
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GoetyDelight.MODID);
/*
    // 示例1：持久化的整数附件（使用 Codec 序列化）
    public static final Supplier<AttachmentType<Integer>> PLAYER_MANA =
            ATTACHMENT_TYPES.register("player_mana",
                    () -> AttachmentType.builder(() -> 0)
                            .serialize(Codec.INT) // 也可以用 Codec.INT
                            .copyOnDeath() // 玩家死亡时自动复制
                            .build()
            );

    // 示例2：使用 INBTSerializable 的物品栏附件（单个槽位）
    public static final Supplier<AttachmentType<ItemStackHandler>> CHUNK_INVENTORY =
            ATTACHMENT_TYPES.register("chunk_inventory",
                    () -> AttachmentType.serializable(() -> new ItemStackHandler(1)).build()
            );

    // 示例3：无需持久化的缓存附件（仅运行时有效）
    public static final Supplier<AttachmentType<SomeRuntimeCache>> RUNTIME_CACHE =
            ATTACHMENT_TYPES.register("runtime_cache",
                    () -> AttachmentType.builder(() -> new SomeRuntimeCache()).build()
            );


    */

    /**
     * 将 DeferredRegister 注册到 Mod 事件总线上
     */
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    // 内部辅助类（仅为示例，实际使用时请替换为自己的逻辑）
    public static class SomeRuntimeCache {
        // 自定义缓存数据
        public int value = 0;
    }
}