package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ActiveEntityVisualEffect {
    private final ResourceLocation id;
    private final int initialDuration;
    private int remainingTicks;
    private CompoundTag data;

    public ActiveEntityVisualEffect(ResourceLocation id, int durationTicks, CompoundTag data) {
        this.id = id;
        this.initialDuration = durationTicks;
        this.remainingTicks = durationTicks;
        this.data = data.copy();
    }

    public ResourceLocation id() {
        return id;
    }

    public int initialDuration() {
        return initialDuration;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public CompoundTag data() {
        return data;
    }

    public void setData(CompoundTag data) {
        this.data = data.copy();
    }

    boolean tick() {
        if (initialDuration == EntityVisualEffects.INFINITE) {
            return false;
        }

        remainingTicks--;
        return remainingTicks <= 0;
    }

    CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id.toString());
        tag.putInt("InitialDuration", initialDuration);
        tag.putInt("RemainingTicks", remainingTicks);
        tag.put("Data", data.copy());
        return tag;
    }

    static ActiveEntityVisualEffect deserializeNBT(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("Id"));
        ActiveEntityVisualEffect effect = new ActiveEntityVisualEffect(
                id,
                tag.getInt("InitialDuration"),
                tag.getCompound("Data")
        );
        effect.remainingTicks = tag.getInt("RemainingTicks");
        return effect;
    }
}
