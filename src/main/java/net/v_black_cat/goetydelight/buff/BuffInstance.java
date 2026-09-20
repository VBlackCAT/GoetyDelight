package net.v_black_cat.goetydelight.buff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;


public class BuffInstance {
    private final ResourceLocation typeId;
    private final int duration;
    private final int amplifier;
    private final boolean infinite;
    private final long expiresAtGameTime;

    public BuffInstance(ResourceLocation typeId, int duration, int amplifier, long gameTime) {
        this(
                typeId,
                duration,
                amplifier,
                duration == -1 ? Long.MAX_VALUE : gameTime + Math.max(0, duration),
                false
        );
    }

    private BuffInstance(
            ResourceLocation typeId,
            int duration,
            int amplifier,
            long expiresAtGameTime,
            boolean absoluteExpiration
    ) {
        this.typeId = typeId;
        this.duration = duration;
        this.amplifier = amplifier;
        this.infinite = duration == -1;
        this.expiresAtGameTime = expiresAtGameTime;
    }

    static BuffInstance deserialize(ResourceLocation typeId, int duration, int amplifier, long gameTime, CompoundTag tag) {
        long expiresAtGameTime = tag.contains("expiresAtGameTime")
                ? tag.getLong("expiresAtGameTime")
                : gameTime + Math.max(0, duration);
        return new BuffInstance(typeId, duration, amplifier, expiresAtGameTime, true);
    }

    public ResourceLocation getTypeId() {
        return typeId;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public long getExpiresAtGameTime() {
        return expiresAtGameTime;
    }

    public int getRemainingTicks(long gameTime) {
        if (infinite) {
            return -1;
        }
        long remaining = Math.max(0L, expiresAtGameTime - gameTime);
        return remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    public boolean isExpired(long gameTime) {
        return !infinite && gameTime >= expiresAtGameTime;
    }
}
