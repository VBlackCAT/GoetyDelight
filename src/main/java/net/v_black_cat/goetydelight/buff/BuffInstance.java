package net.v_black_cat.goetydelight.buff;

import net.minecraft.resources.ResourceLocation;


public class BuffInstance {
    private final ResourceLocation typeId;
    private int duration;
    private int amplifier;
    private boolean infinite;

    public BuffInstance(ResourceLocation typeId, int duration, int amplifier) {
        this.typeId = typeId;
        this.duration = duration;
        this.amplifier = amplifier;
        this.infinite = (duration == -1);
    }

    public ResourceLocation getTypeId() { return typeId; }
    public int getDuration() { return duration; }
    public int getAmplifier() { return amplifier; }

    public void tick() {
        if (!infinite && duration > 0) duration--;
    }

    public boolean isExpired() {
        return !infinite && duration <= 0;
    }
}
