package net.v_black_cat.goetydelight.init;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
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

    // ============ 玩偶实体数据组件 ============
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> DOLL_ENTITY =
            DATA_COMPONENTS.registerComponentType(
                    "doll_entity",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            );

    // ============ 增益效果数据组件 ============
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

    /**
     * 注册到 Mod 事件总线
     */
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}