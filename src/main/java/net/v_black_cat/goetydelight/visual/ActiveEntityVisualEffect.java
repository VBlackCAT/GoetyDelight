package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ActiveEntityVisualEffect {
    /** 效果开始时的游戏时间（由 {@code EntityVisualEffectSystem#addEffect} 写入）。 */
    public static final String START_GAME_TIME = "StartGameTime";

    private final ResourceLocation id;
    private final int initialDuration;
    /**
     * 旧的「剩余 tick」计数。
     * <p>改为到期队列后服务端不再每 tick 递减它，客户端也从未递减过（所以此前客户端的进度恒为 0）。
     * 现在只作为「没有 StartGameTime 时」的回退值保留，写入 NBT 以便旧存档读回。
     */
    private int remainingTicks;
    private CompoundTag data;

    public ActiveEntityVisualEffect(ResourceLocation id, int durationTicks, CompoundTag data) {
        this.id = id;
        this.initialDuration = durationTicks;
        this.remainingTicks = durationTicks;
        this.data = data.copy();
    }

    static ActiveEntityVisualEffect deserializeNBT(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.parse(tag.getString("Id"));
        ActiveEntityVisualEffect effect = new ActiveEntityVisualEffect(
                id,
                tag.getInt("InitialDuration"),
                tag.getCompound("Data")
        );
        effect.remainingTicks = tag.getInt("RemainingTicks");
        return effect;
    }

    public ResourceLocation id() {
        return id;
    }

    public int initialDuration() {
        return initialDuration;
    }

    public CompoundTag data() {
        return data;
    }

    public void setData(CompoundTag data) {
        this.data = data.copy();
    }

    CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id.toString());
        tag.putInt("InitialDuration", initialDuration);
        tag.putInt("RemainingTicks", remainingTicks);
        tag.put("Data", data.copy());
        return tag;
    }

    /** 效果开始时的游戏时间；未记录时返回 -1。 */
    public long startGameTime() {
        return data.contains(START_GAME_TIME, Tag.TAG_LONG) ? data.getLong(START_GAME_TIME) : -1L;
    }

    /**
     * 播放进度（0 = 刚开始，1 = 结束）。
     *
     * <p>用「开始时间 + 当前游戏时间」推算：服务端只在增删/到期时同步，进度完全由客户端自行计算，
     * 所以不需要任何一侧每 tick 递减计数。没有记录开始时间时回退到旧的剩余 tick 计数。
     */
    public float progress(long gameTime, float partialTick) {
        if (initialDuration <= 0) {
            return 0.0F;
        }
        long start = startGameTime();
        float elapsed = start >= 0
                ? (float) (gameTime - start) + partialTick
                : (float) (initialDuration - remainingTicks) + partialTick;
        return Mth.clamp(elapsed / (float) initialDuration, 0.0F, 1.0F);
    }
}
