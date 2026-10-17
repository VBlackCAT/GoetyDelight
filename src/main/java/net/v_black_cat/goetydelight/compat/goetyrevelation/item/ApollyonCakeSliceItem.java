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
import net.v_black_cat.goetydelight.compat.EndingLibrary.EndingLibraryCompat;
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

    public static final String TAG_SLICE_COUNT = "GoetyDelightApollyonSliceCount";
    private static final int TRIGGER_COUNT = 7;

    private static final double FRONT_DISTANCE = 2.0D;
    private static final double BACK_DISTANCE = 3.0D;
    private static final double SUMMON_SIDE_OFFSET = 1.0D;

    private static final String TAG_TIME_STOP_IMMUNE = "TimeStopImmune";

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
            // 1. 伤害抗性（原版效果，不依赖 EL）
            entity.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, TimeConverter.sToTick(66), 5));

            // 2. 时停 —— 只有 EL 加载且版本 >= 2.2.2 时才调用
            //    注意：TimeStopAPI 是 EL 的类，EL 未加载/版本过低时调用会 NoClassDefFoundError
            if (EndingLibraryCompat.isAtLeastMinVersion()) {
                if (TIME_STOP_PLAY_SOUND) {
                    TimeStopAPI.use(true, entity, TIME_STOP_FORCE, TIME_STOP_DURATION);
                } else {
                    TimeStopAPI.useWithoutSoundEffect(true, entity, TIME_STOP_FORCE, TIME_STOP_DURATION);
                }
            }

            // 3. 只有玩家才统计
            if (entity instanceof Player player) {
                CompoundTag data = player.getPersistentData();
                int count = data.getInt(TAG_SLICE_COUNT) + 1;
                data.putInt(TAG_SLICE_COUNT, count);

                // 4. 达到 7 次触发召唤（summonDisplayTrio 内部还会再判断版本）
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
        if (!EndingLibraryCompat.isAtLeastMinVersion()) {
            return;
        }

        double yawRad = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = -forwardZ;
        double rightZ = forwardX;

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        double[][] offsets = new double[][]{
                { forwardX * FRONT_DISTANCE, forwardZ * FRONT_DISTANCE },
                { forwardX * BACK_DISTANCE - rightX * SUMMON_SIDE_OFFSET,
                        forwardZ * BACK_DISTANCE - rightZ * SUMMON_SIDE_OFFSET },
                { forwardX * BACK_DISTANCE + rightX * SUMMON_SIDE_OFFSET,
                        forwardZ * BACK_DISTANCE + rightZ * SUMMON_SIDE_OFFSET }
        };

        String[] names = new String[]{ "player__7", "mega32k", "Kun_Zhong" };
        String musicGroup = "apollyon_slice_" + player.getUUID();

        List<DisplayEntity> spawned = new ArrayList<>(3);

        for (int i = 0; i < offsets.length; i++) {
            double x = px + offsets[i][0];
            double z = pz + offsets[i][1];

            DisplayEntity display = ModEntities.DISPLAY_ENTITY.get().create(level);
            if (display == null) continue;

            display.moveTo(x, py, z, 0f, 0f);

            double dx = px - x;
            double dz = pz - z;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            display.setYRot(yaw);
            display.setYHeadRot(yaw);

            display.setCustomName(Component.literal(names[i]));
            display.setCustomNameVisible(false);
            display.setAnimation("kwil");
            display.setMusicGroup(musicGroup);

            display.setInvulnerable(true);

            display.setTimeStopImmune(true);

            level.addFreshEntity(display);
            spawned.add(display);
        }

        scheduleClearOnTimeStopEnd(level, spawned);
    }

    // ------------------------------------------------------------------
    // 时停结束时清除
    // ------------------------------------------------------------------

    private static void scheduleClearOnTimeStopEnd(ServerLevel level, List<DisplayEntity> entities) {
        PENDING_CLEANUPS.add(new PendingCleanup(level, entities));
    }

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