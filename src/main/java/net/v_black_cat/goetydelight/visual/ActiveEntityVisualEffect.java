package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ActiveEntityVisualEffect {
    static final long NO_GAME_TIME = Long.MIN_VALUE;
    private static final String EXPIRES_AT_GAME_TIME = "ExpiresAtGameTime";
    private static final String REMAINING_TICKS = "RemainingTicks";

    private final ResourceLocation id;
    private final int initialDuration;
    private long expiresAtGameTime;
    private int remainingTicks;
    private CompoundTag data;

    public ActiveEntityVisualEffect(ResourceLocation id, int durationTicks, CompoundTag data, long gameTime) {
        this(
                id,
                durationTicks,
                durationTicks == EntityVisualEffects.INFINITE
                        ? Long.MAX_VALUE
                        : gameTime + Math.max(0, durationTicks),
                durationTicks,
                data
        );
    }

    private ActiveEntityVisualEffect(
            ResourceLocation id,
            int initialDuration,
            long expiresAtGameTime,
            int remainingTicks,
            CompoundTag data
    ) {
        this.id = id;
        this.initialDuration = initialDuration;
        this.expiresAtGameTime = expiresAtGameTime;
        this.remainingTicks = remainingTicks;
        this.data = data.copy();
    }

    public ResourceLocation id() {
        return id;
    }

    public int initialDuration() {
        return initialDuration;
    }

    public long expiresAtGameTime() {
        return expiresAtGameTime;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public boolean isExpired(long gameTime) {
        return expiresAtGameTime != Long.MAX_VALUE
                && expiresAtGameTime != NO_GAME_TIME
                && gameTime >= expiresAtGameTime;
    }

    public CompoundTag data() {
        return data;
    }

    public void setData(CompoundTag data) {
        this.data = data.copy();
    }

    private void refreshRemainingTicks(long gameTime) {
        if (initialDuration == EntityVisualEffects.INFINITE) {
            expiresAtGameTime = Long.MAX_VALUE;
            remainingTicks = EntityVisualEffects.INFINITE;
            return;
        }

        if (expiresAtGameTime == NO_GAME_TIME) {
            expiresAtGameTime = gameTime + Math.max(0, remainingTicks);
        }

        long remaining = Math.max(0L, expiresAtGameTime - gameTime);
        remainingTicks = remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    CompoundTag serializeNBT(long gameTime) {
        refreshRemainingTicks(gameTime);
        return serializeNBTInternal();
    }

    /**
     * Serializes without recalculating the deadline. This is used by generic
     * INBTSerializable paths which do not have a level game time available.
     */
    CompoundTag serializeNBTStored() {
        return serializeNBTInternal();
    }

    private CompoundTag serializeNBTInternal() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id.toString());
        tag.putInt("InitialDuration", initialDuration);
        tag.putInt(REMAINING_TICKS, remainingTicks);
        tag.putLong(EXPIRES_AT_GAME_TIME, expiresAtGameTime);
        tag.put("Data", data.copy());
        return tag;
    }

    static ActiveEntityVisualEffect deserializeNBT(CompoundTag tag, long gameTime) {
        ResourceLocation id = new ResourceLocation(tag.getString("Id"));
        int initialDuration = tag.getInt("InitialDuration");
        int storedRemaining = tag.contains(REMAINING_TICKS) ? tag.getInt(REMAINING_TICKS) : initialDuration;

        long expiresAtGameTime;
        if (initialDuration == EntityVisualEffects.INFINITE) {
            expiresAtGameTime = Long.MAX_VALUE;
        } else if (gameTime != NO_GAME_TIME && tag.contains(REMAINING_TICKS)) {
            // Relative time is authoritative: a visual effect pauses while its level is unloaded
            // and resumes with the same remaining duration instead of expiring during load.
            expiresAtGameTime = gameTime + Math.max(0, storedRemaining);
        } else if (tag.contains(EXPIRES_AT_GAME_TIME)) {
            expiresAtGameTime = tag.getLong(EXPIRES_AT_GAME_TIME);
        } else if (gameTime != NO_GAME_TIME) {
            // Older saves stored only a relative duration. Convert it once when loading.
            expiresAtGameTime = gameTime + Math.max(0, storedRemaining);
        } else {
            // Preserve the effect data and relative time until a level game time is available.
            expiresAtGameTime = NO_GAME_TIME;
        }

        return new ActiveEntityVisualEffect(
                id,
                initialDuration,
                expiresAtGameTime,
                storedRemaining,
                tag.getCompound("Data")
        );
    }
}