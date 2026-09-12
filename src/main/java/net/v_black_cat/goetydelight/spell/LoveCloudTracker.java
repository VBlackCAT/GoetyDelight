package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.util.BrewGas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LoveCloudTracker {

    /** 检查"是否被提前判废"的间隔（tick） */
    private static final int CHECK_INTERVAL_TICKS = 10;
    /** 同一片云最多补回的格数（按格累计，不是按轮），防止异常时无限刷实体 */
    private static final int MAX_REVIVES = 2;

    /** 药云里每格交给 Goety 的内部 duration：远大于实际时长，避免它的随机判废抢先生效 */
    public static final int GAS_INTERNAL_DURATION_TICKS = 20 * 60 * 10; // 10 分钟保险

    private static final List<Cloud> CLOUDS = new ArrayList<>();

    /**
     * 下一次需要被唤醒的服务器 tick。没有云、或距离下次检查/到期还早时，整个 tick 处理
     * 只花一次 long 比较（此前每 tick 都要对每片云做 getLevel 查找 + 递减计数）。
     */
    private static long nextWakeTick = Long.MAX_VALUE;
    private static long lastWakeTick = 0L;

    private LoveCloudTracker() {
    }

    /**
     * 在指定格生成一团药云（法术铺云与补回都用它，保证参数一致）。
     *
     * @return 成功加入且未立刻被移除的 gas；否则 null
     */
    public static BrewGas spawnGas(ServerLevel level, BlockPos pos, LivingEntity owner,
                                   List<BrewEffectInstance> effects) {
        BrewGas gas = new BrewGas(ModEntityType.BREW_EFFECT_GAS.get(), level);
        gas.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        // area = 0：关掉 Goety 的随机自扩散，形状完全由铺云决定
        gas.setGas(List.of(), effects, GAS_INTERNAL_DURATION_TICKS, 0, owner);
        if (!level.addFreshEntity(gas)) {
            return null;
        }
        // addFreshEntity 返回 true 只代表"没被取消加入"，实体仍可能已被立刻 discard
        return gas.isRemoved() ? null : gas;
    }

    /** 登记一片药云，由 ticker 负责按时回收 */
    public static void track(ServerLevel level, LivingEntity owner, Map<BlockPos, UUID> cells,
                             List<BrewEffectInstance> effects, int durationTicks) {
        CLOUDS.add(new Cloud(level.dimension(), owner.getUUID(), List.copyOf(effects),
                new LinkedHashMap<>(cells), durationTicks));
 /*       GoetyDelight.LOGGER.info("[爱与丰饶] 铺开药云 {} 格，时长 {} tick（{} 秒）",
                cells.size(), durationTicks, durationTicks / 20);   */
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (CLOUDS.isEmpty()) {
            nextWakeTick = Long.MAX_VALUE;
            return;
        }

        MinecraftServer server = event.getServer();
        long tick = server.getTickCount();
        if (tick < nextWakeTick) {
            return; // 还没到任何一片云的检查/到期时刻：零成本
        }

        long elapsed = Math.max(1L, tick - lastWakeTick);
        lastWakeTick = tick;
        nextWakeTick = Long.MAX_VALUE;

        Iterator<Cloud> it = CLOUDS.iterator();
        while (it.hasNext()) {
            Cloud cloud = it.next();
            ServerLevel level = server.getLevel(cloud.dimension);
            if (level == null) {
                it.remove();
                continue;
            }

            cloud.ticksUntilEnd -= (int) elapsed;
            if (cloud.ticksUntilEnd <= 0) {
                int removed = 0;
                for (UUID id : cloud.cells.values()) {
                    if (level.getEntity(id) instanceof BrewGas gas) {
                        gas.discard();
                        removed++;
                    }
                }
                GoetyDelight.LOGGER.info("[爱与丰饶] 药云到期回收 {} 团（其中曾提前消失 {} 团，补回 {} 格）",
                        removed, cloud.cells.size() - removed, cloud.revives);
                it.remove();
                continue;
            }

            cloud.ticksUntilCheck -= (int) elapsed;
            if (cloud.ticksUntilCheck <= 0) {
                cloud.ticksUntilCheck = CHECK_INTERVAL_TICKS;
                reviveMissing(level, cloud);
            }

            nextWakeTick = Math.min(nextWakeTick, tick + Math.min(cloud.ticksUntilEnd, cloud.ticksUntilCheck));
        }
    }

    /** 把被 Goety 自身逻辑提前判废的格子补回来（按格累计上限） */
    private static void reviveMissing(ServerLevel level, Cloud cloud) {
        if (cloud.revives >= MAX_REVIVES) {
            return;
        }
        LivingEntity owner = level.getEntity(cloud.ownerId) instanceof LivingEntity living ? living : null;
        int revived = 0;
        for (Map.Entry<BlockPos, UUID> entry : cloud.cells.entrySet()) {
            if (cloud.revives + revived >= MAX_REVIVES) {
                break;
            }
            if (level.getEntity(entry.getValue()) != null) {
                continue;
            }
            BrewGas gas = spawnGas(level, entry.getKey(), owner, cloud.effects);
            if (gas != null) {
                entry.setValue(gas.getUUID());
                revived++;
            }
        }
        if (revived > 0) {
            cloud.revives += revived;
 /*           GoetyDelight.LOGGER.warn("[爱与丰饶] 有 {} 格药云被提前判废并已补回（累计 {}/{} 格）",
                    revived, cloud.revives, MAX_REVIVES);   */
        }
    }

    /** 一片药云：维度 + 归属者 + 效果 + 每格实体 UUID + 倒计时（自己数，不依赖任何时钟） */
    private static final class Cloud {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final List<BrewEffectInstance> effects;
        private final Map<BlockPos, UUID> cells;
        private int ticksUntilEnd;
        private int ticksUntilCheck;
        private int revives;

        private Cloud(ResourceKey<Level> dimension, UUID ownerId, List<BrewEffectInstance> effects,
                      Map<BlockPos, UUID> cells, int durationTicks) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.effects = effects;
            this.cells = cells;
            this.ticksUntilEnd = durationTicks;
            this.ticksUntilCheck = CHECK_INTERVAL_TICKS;
        }
    }
}