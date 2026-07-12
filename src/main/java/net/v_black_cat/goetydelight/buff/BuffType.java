package net.v_black_cat.goetydelight.buff;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 自定义效果类型
 */
public record BuffType(
        int defaultDuration,
        int defaultAmplifier,
        boolean stackable
) {
    public static final Codec<BuffType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("defaultDuration").forGetter(BuffType::defaultDuration),
                    Codec.INT.fieldOf("defaultAmplifier").forGetter(BuffType::defaultAmplifier),
                    Codec.BOOL.fieldOf("stackable").forGetter(BuffType::stackable)
            ).apply(instance, BuffType::new)
    );
}