package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.endinglib.api.time.TimeStopAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.entities.display_entity.DisplayEntity;
import net.v_black_cat.goetydelight.util.TimeConverter;

import java.util.ArrayList;
import java.util.List;

public class ApollyonCakeSliceItem extends Item {

    // ------------------------------------------------------------------
    // 常量
    // ------------------------------------------------------------------

    public static final String TAG_IS_THE_END = "SliceIsTheEnd";

    public static final String THE_END_NAME_KEY = "item.goetydelight.apollyon_cake_slice.the_end";

    private static final int TIME_STOP_DURATION = 200;

    private static final boolean TIME_STOP_FORCE = false;

    private static final boolean TIME_STOP_PLAY_SOUND = true;

    /** 玩家持久化数据：已食用「终末切片」次数 */
    public static final String TAG_SLICE_COUNT = "GoetyDelightApollyonSliceCount";

    /** 触发召唤所需的次数 */
    private static final int TRIGGER_COUNT = 7;

    /** 前中距玩家的距离（格）—— 最近 */
    private static final double FRONT_DISTANCE = 2.0D;

    /** 左后/右后距玩家的距离（格）—— 稍远 */
    private static final double BACK_DISTANCE = 3.0D;

    /** 品字形左右偏移（格） */
    private static final double SUMMON_SIDE_OFFSET = 1.0D;

    /** 时停免疫标记 */
    private static final String TAG_TIME_STOP_IMMUNE = "TimeStopImmune";

    /** 待清除的实体（时停结束时统一清除） */
    private static final List<PendingCleanup> PENDING_CLEANUPS = new ArrayList<>();

    public ApollyonCakeSliceItem(Properties properties) {
        super(properties);
    }

    // ------------------------------------------------------------------
    // 原有：is_the_end 判断、名字键、创建堆叠
    // ------------------------------------------------------------------

    public static boolean isTheEnd(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_IS_THE_END);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return isTheEnd(stack) ? THE_END_NAME_KEY : super.getDescriptionId(stack);
    }

    public static ItemStack createStack(Item item, boolean isTheEnd) {
        ItemStack stack = new ItemStack(item);
        if (isTheEnd) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(TAG_IS_THE_END, true);
        }
        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isTheEnd(stack);
    }

    // ------------------------------------------------------------------
    // 食用逻辑
    // ------------------------------------------------------------------

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        boolean theEnd = isTheEnd(stack);

        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && theEnd) {
            // 1. 伤害抗性
            entity.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, TimeConverter.sToTick(66), 5));

            // 2. 时停
            if (TIME_STOP_PLAY_SOUND) {
                TimeStopAPI.use(true, entity, TIME_STOP_FORCE, TIME_STOP_DURATION);
            } else {
                TimeStopAPI.useWithoutSoundEffect(true, entity, TIME_STOP_FORCE, TIME_STOP_DURATION);
            }

            // 3. 只有玩家才统计
            if (entity instanceof Player player) {
                CompoundTag data = player.getPersistentData();
                int count = data.getInt(TAG_SLICE_COUNT) + 1;
                data.putInt(TAG_SLICE_COUNT, count);

                // 4. 达到 7 次触发召唤
                if (count >= TRIGGER_COUNT) {
                    data.putInt(TAG_SLICE_COUNT, 0);
                    summonDisplayTrio(player, (ServerLevel) level);
                }
            }
        }

        return result;
    }

    // ------------------------------------------------------------------
    // 品字形召唤 3 个 DisplayEntity
    // ------------------------------------------------------------------

    private static void summonDisplayTrio(Player player, ServerLevel level) {
        double yawRad = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = -forwardZ;
        double rightZ = forwardX;

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        // 品字形：前中最近（顶点朝玩家），左后/右后稍远
        double[][] offsets = new double[][]{
                { forwardX * FRONT_DISTANCE, forwardZ * FRONT_DISTANCE },                    // 前中
                { forwardX * BACK_DISTANCE - rightX * SUMMON_SIDE_OFFSET,
                        forwardZ * BACK_DISTANCE - rightZ * SUMMON_SIDE_OFFSET },                  // 左后
                { forwardX * BACK_DISTANCE + rightX * SUMMON_SIDE_OFFSET,
                        forwardZ * BACK_DISTANCE + rightZ * SUMMON_SIDE_OFFSET }                   // 右后
        };

        // 与 offsets 一一对应
        String[] names = new String[]{ "player__7", "mega32k", "Kun_Zhong" };

        List<DisplayEntity> spawned = new ArrayList<>(3);

        for (int i = 0; i < offsets.length; i++) {
            double x = px + offsets[i][0];
            double z = pz + offsets[i][1];

            DisplayEntity display = ModEntities.DISPLAY_ENTITY.get().create(level);
            if (display == null) continue;

            display.moveTo(x, py, z, 0f, 0f);

            // 面向玩家
            double dx = px - x;
            double dz = pz - z;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            display.setYRot(yaw);
            display.setYHeadRot(yaw);

            // 名字 + 动画
            display.setCustomName(Component.literal(names[i]));
            display.setCustomNameVisible(false);
            display.setAnimation("kwil");
            display.setInvulnerable(true);

            // 时停免疫标记
            display.getPersistentData().putBoolean(TAG_TIME_STOP_IMMUNE, true);

            level.addFreshEntity(display);
            spawned.add(display);
        }

        // 5. 时停结束时统一清除
        scheduleClearOnTimeStopEnd(level, spawned);
    }

    // ------------------------------------------------------------------
    // 时停结束时清除（由 ServerExpandedContextMixin 调用）
    // ------------------------------------------------------------------

    private static void scheduleClearOnTimeStopEnd(ServerLevel level, List<DisplayEntity> entities) {
        PENDING_CLEANUPS.add(new PendingCleanup(level, entities));
    }

    /**
     * 时停结束时由 ServerExpandedContextMixin 调用。
     * 清空所有待清除的实体。
     */
    public static void onTimeStopEnd() {
        if (PENDING_CLEANUPS.isEmpty()) return;

        for (PendingCleanup task : PENDING_CLEANUPS) {
            for (DisplayEntity e : task.entities) {
                if (e.isAlive()) {
                    e.discard();
                }
            }
        }
        PENDING_CLEANUPS.clear();
    }

    private static class PendingCleanup {
        final ServerLevel level;
        final List<DisplayEntity> entities;

        PendingCleanup(ServerLevel level, List<DisplayEntity> entities) {
            this.level = level;
            this.entities = entities;
        }
    }
}