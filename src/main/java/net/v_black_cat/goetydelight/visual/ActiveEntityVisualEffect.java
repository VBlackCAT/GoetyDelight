package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ActiveEntityVisualEffect {
    static final long NO_GAME_TIME = Long.MIN_VALUE;
    /** 效果开始时的游戏时间，由 {@code EntityVisualEffectSystem#addEffect} 写入 data。 */
    public static final String START_GAME_TIME = "StartGameTime";
    /** 入场展开时长（tick，默认 {@link #DEFAULT_GROW_TICKS}，<=0 关闭）。 */
    public static final String GROW_TICKS = "GrowTicks";
    /** 入场展开的起始比例（默认 {@link #DEFAULT_GROW_FROM}，0~1）。 */
    public static final String GROW_FROM = "GrowFrom";
    private static final int DEFAULT_GROW_TICKS = 12;
    private static final float DEFAULT_GROW_FROM = 0.15F;
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

    /** 效果开始时的游戏时间；未记录时返回 -1。 */
    public long startGameTime() {
        return data.contains(START_GAME_TIME) ? data.getLong(START_GAME_TIME) : -1L;
    }

    /**
     * 播放进度（0 = 刚开始，1 = 结束）。
     *
     * <p>用「开始时间 + 当前游戏时间」推算：服务端只在增删/到期时同步，进度完全由客户端自行计算，
     * 因此不需要任何一侧每 tick 递减计数。
     *
     * <p>修复：原实现按 {@code remainingTicks} 反向推算进度，但客户端从不递减该值
     * （服务端也只在同步时刷新它），于是有限时长的特效进度恒为 0 —— 领域雾/斩击的
     * {@code intensity = base * clamp(progress * 1.6)} 被锁死在 0，表现就是「时间到了以后再 add 不显示」。
     * 没有记录开始时间时（旧存档）回退到旧的剩余 tick 计数。
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

    /**
     * 入场展开系数：特效刚 add 时是 {@code GrowFrom} 的比例（默认 15%），在 {@code GrowTicks}
     * （默认 12 tick）内 ease-out 平滑长到 1.0 —— 也就是 data 里设定的 Radius / Height / Scale 范围。
     *
     * <ul>
     *     <li>{@code GrowTicks:0} 或负数 = 关闭，保持瞬间成型；</li>
     *     <li>有限时长的特效最多用生命的前 1/3 做展开，短命特效不会整段都在长大；</li>
     *     <li>没有 StartGameTime（旧存档 / 旧同步）时返回 1.0，不做展开。</li>
     * </ul>
     */
    public float growthScale(long gameTime, float partialTick) {
        long start = startGameTime();
        if (start < 0) {
            return 1.0F;
        }

        int growTicks = data.contains(GROW_TICKS) ? data.getInt(GROW_TICKS) : DEFAULT_GROW_TICKS;
        if (growTicks <= 0) {
            return 1.0F;
        }

        if (initialDuration > 0) {
            growTicks = Math.min(growTicks, Math.max(1, initialDuration / 3));
        }

        float from = data.contains(GROW_FROM)
                ? Mth.clamp(data.getFloat(GROW_FROM), 0.0F, 1.0F)
                : DEFAULT_GROW_FROM;

        float elapsed = (float) (gameTime - start) + partialTick;
        float t = Mth.clamp(elapsed / (float) growTicks, 0.0F, 1.0F);
        float eased = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
        return from + (1.0F - from) * eased;
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