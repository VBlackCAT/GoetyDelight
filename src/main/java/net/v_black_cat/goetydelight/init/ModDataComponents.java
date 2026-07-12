package net.v_black_cat.goetydelight.init;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 自定义数据组件（Data Components）注册类
 * 使用 DeferredRegister.DataComponents 简化注册流程
 */
public class ModDataComponents {
    // 使用专门的 DataComponents 注册器
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, GoetyDelight.MODID);


    public record BuffData(ResourceLocation buffTypeId, int duration, int amplifier) {
        public static final Codec<BuffData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("buff_type").forGetter(BuffData::buffTypeId),
                        Codec.INT.fieldOf("duration").forGetter(BuffData::duration),
                        Codec.INT.fieldOf("amplifier").forGetter(BuffData::amplifier)
                ).apply(instance, BuffData::new)
        );

        public static final StreamCodec<ByteBuf, BuffData> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, BuffData::buffTypeId,
                ByteBufCodecs.INT, BuffData::duration,
                ByteBufCodecs.INT, BuffData::amplifier,
                BuffData::new
        );
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BuffData>> ITEM_BUFF =
            DATA_COMPONENTS.registerComponentType(
                    "item_buff",
                    builder -> builder
                            .persistent(BuffData.CODEC)
                            .networkSynchronized(BuffData.STREAM_CODEC)
            );
/*
    // ============ 示例组件：魔力值 ============
    // 组件值的记录类（必须实现 hashCode 和 equals，推荐使用 record）
    public record ManaData(int mana, int maxMana) {
        // 默认值
        public static final ManaData DEFAULT = new ManaData(0, 100);
    }

    // 持久化和网络同步的 Codec 与 StreamCodec
    public static final Codec<ManaData> MANA_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("mana").forGetter(ManaData::mana),
                    Codec.INT.fieldOf("maxMana").forGetter(ManaData::maxMana)
            ).apply(instance, ManaData::new)
    );

    public static final StreamCodec<ByteBuf, ManaData> MANA_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ManaData::mana,
            ByteBufCodecs.INT, ManaData::maxMana,
            ManaData::new
    );

    // 注册魔力值组件（持久化+网络同步）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ManaData>> MANA =
            DATA_COMPONENTS.registerComponentType(
                    "mana",
                    builder -> builder
                            .persistent(MANA_CODEC)
                            .networkSynchronized(MANA_STREAM_CODEC)
            );

    // ============ 示例组件：临时标记（不持久化，仅网络同步） ============
    // 使用简单的布尔值作为组件值
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> MARKER =
            DATA_COMPONENTS.registerComponentType(
                    "marker",
                    builder -> builder
                            // 不提供 persistent，表示不会保存到磁盘
                            .networkSynchronized(ByteBufCodecs.BOOL)
            );

    // ============ 示例组件：仅本地（既不持久化也不网络同步） ============
    // 使用 StreamCodec.unit 提供默认值，不传输任何数据
    public static final StreamCodec<ByteBuf, Integer> UNIT_INT = StreamCodec.unit(0);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LOCAL_ONLY =
            DATA_COMPONENTS.registerComponentType(
                    "local_only",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(UNIT_INT) // 使用 unit 表示不发送数据
            );


    */

    /**
     * 注册到 Mod 事件总线
     */
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}