package net.v_black_cat.goetydelight.buff;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

/**
 * 一个激活的效果实例，通过 ResourceLocation 引用 BuffType。
 */
public class BuffInstance {
    private final ResourceLocation typeId;  // 指向注册表中的 BuffType
    private int duration;
    private int amplifier;

    public BuffInstance(ResourceLocation typeId, int duration, int amplifier) {
        this.typeId = typeId;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public ResourceLocation getTypeId() { return typeId; }
    public int getDuration() { return duration; }
    public int getAmplifier() { return amplifier; }

    public void tick() {
        if (duration > 0) duration--;
    }

    public boolean isExpired() { return duration <= 0; }

    // 通过注册表获取 BuffType（需要注入注册表）
    public BuffType getType() {
        return ModBuffTypes.BUFF_REGISTRY.get(typeId);
    }

    // === 序列化 ===
    public static final Codec<BuffInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("type_id").forGetter(BuffInstance::getTypeId),
                    Codec.INT.fieldOf("duration").forGetter(BuffInstance::getDuration),
                    Codec.INT.fieldOf("amplifier").forGetter(BuffInstance::getAmplifier)
            ).apply(instance, BuffInstance::new)
    );

    public static final StreamCodec<ByteBuf, BuffInstance> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BuffInstance::getTypeId,
            ByteBufCodecs.INT, BuffInstance::getDuration,
            ByteBufCodecs.INT, BuffInstance::getAmplifier,
            BuffInstance::new
    );
}