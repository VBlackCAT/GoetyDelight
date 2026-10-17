package net.v_black_cat.goetydelight.entities.display_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.util.ModSounds;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class DisplayEntityMusicManager {

    /** 与 DisplayEntity.TIME_STOP_IMMUNE_LIFETIME 保持一致（200 tick × 50ms = 10 秒） */
    private static final int TIME_STOP_IMMUNE_LIFETIME = 200;
    private static final long TIME_STOP_DURATION_MS = TIME_STOP_IMMUNE_LIFETIME * 50L;

    private static final Map<String, RegistryObject<SoundEvent>> ANIMATION_MUSIC = Map.of(
            "kwil", ModSounds.MUSIC_KWIL
    );

    /** 正常时间：leader 实体实例 */
    private static final Map<DisplayEntity, DisplayEntityMusicInstance> PLAYING =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    /** group -> leader */
    private static final Map<String, DisplayEntity> GROUP_LEADER =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    /** 实体 -> 所属 group */
    private static final Map<DisplayEntity, String> ENTITY_GROUP =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    /** 时停期间：每个 group/key 一个玩家实例 */
    private static final Map<String, TimeStopMusicInstance> TIME_STOP_PLAYING =
            new ConcurrentHashMap<>();

    /** group/key -> 应播放的音源 */
    private static final Map<String, SoundEvent> GROUP_SOUND =
            new ConcurrentHashMap<>();

    /** 本次时停的结束时间戳（毫秒），onTimeStopStart 时算一次 */
    private static volatile long timeStopEndAtMs = 0L;

    private static volatile boolean inTimeStop = false;

    private DisplayEntityMusicManager() {
    }

    public static boolean isInTimeStop() {
        return inTimeStop;
    }

    // ============================================================
    // 原有事件
    // ============================================================

    public static void onAnimationChanged(DisplayEntity entity, @Nullable String animation) {
        handleMusicRequest(entity, animation, "onAnimationChanged");
    }

    public static void onMusicGroupChanged(DisplayEntity entity, @Nullable String group) {
        String oldGroup = ENTITY_GROUP.remove(entity);
        if (oldGroup != null && oldGroup.equals(GROUP_LEADER.get(oldGroup))) {
            GROUP_LEADER.remove(oldGroup);
        }
        handleMusicRequest(entity, entity.getAnimation(), "onMusicGroupChanged");
    }

    public static void onEntityRemoved(DisplayEntity entity) {
        String group = ENTITY_GROUP.remove(entity);
        stop(entity);
        if (group == null) return;

        DisplayEntity leader = GROUP_LEADER.get(group);
        if (leader == entity) {
            GROUP_LEADER.remove(group);
            if (!inTimeStop) {
                GROUP_SOUND.remove(group);
            }
            tryPromoteNextLeader(group);
        }
    }

    // ============================================================
    // 免疫实体出现：触发一次时停开始（内部有 inTimeStop 守卫）
    // ============================================================

    public static void onImmuneEntitySpawned(DisplayEntity entity) {
        if (entity == null) return;
        onTimeStopStart();
    }

    // ============================================================
    // 时停开始：只做一次"实体实例 → 玩家实例"的切换
    // ============================================================

    public static void onTimeStopStart() {
        if (inTimeStop) return;
        inTimeStop = true;
        timeStopEndAtMs = System.currentTimeMillis() + TIME_STOP_DURATION_MS;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            inTimeStop = false;
            return;
        }

        // 1) 停掉所有实体实例（时停期间不再有实体实例在跑）
        for (DisplayEntityMusicInstance inst : PLAYING.values()) {
            if (inst != null) mc.getSoundManager().stop(inst);
        }
        PLAYING.clear();

        // 2) 对每个已知 group，创建唯一的玩家实例
        //    GROUP_SOUND 可能尚未填充（同步顺序不保证），此时回退到 leader.getAnimation()
        for (Map.Entry<String, DisplayEntity> e : GROUP_LEADER.entrySet()) {
            String key = e.getKey();
            DisplayEntity leader = e.getValue();
            if (leader == null) continue;

            SoundEvent sound = GROUP_SOUND.get(key);
            if (sound == null) {
                RegistryObject<SoundEvent> ref = ANIMATION_MUSIC.get(leader.getAnimation());
                sound = ref != null ? ref.get() : null;
                if (sound != null) GROUP_SOUND.put(key, sound);
            }
            if (sound == null) continue;

            if (TIME_STOP_PLAYING.containsKey(key)) continue;

            final String groupKey = key;
            TimeStopMusicInstance tsInst = new TimeStopMusicInstance(
                    sound, player, timeStopEndAtMs,
                    () -> onOneTimeStopInstanceFinished(groupKey));
            TIME_STOP_PLAYING.put(key, tsInst);
            mc.getSoundManager().play(tsInst);
        }
    }

    /**
     * 某个玩家实例到点/玩家死亡结束。
     * 所有实例都结束 → 真正结束时停。
     */
    private static void onOneTimeStopInstanceFinished(String key) {
        TIME_STOP_PLAYING.remove(key);
        if (TIME_STOP_PLAYING.isEmpty() && inTimeStop) {
            onTimeStopEnd();
        }
    }

    // ============================================================
    // 时停结束：停玩家实例，恢复仍存活的 leader 实体实例
    // ============================================================

    public static void onTimeStopEnd() {
        if (!inTimeStop) return;
        inTimeStop = false;
        timeStopEndAtMs = 0L;

        Minecraft mc = Minecraft.getInstance();

        for (TimeStopMusicInstance inst : TIME_STOP_PLAYING.values()) {
            if (inst != null) mc.getSoundManager().stop(inst);
        }
        TIME_STOP_PLAYING.clear();

        // 恢复：只有 leader 还活着才恢复；否则清掉该 group 的音源记录
        java.util.Iterator<Map.Entry<String, SoundEvent>> it = GROUP_SOUND.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SoundEvent> e = it.next();
            String key = e.getKey();
            SoundEvent sound = e.getValue();

            DisplayEntity leader = GROUP_LEADER.get(key);
            if (leader == null || !leader.isAlive() || leader.isRemoved()) {
                it.remove();
                continue;
            }
            if (sound == null) continue;
            if (PLAYING.containsKey(leader)) continue;

            DisplayEntityMusicInstance inst = new DisplayEntityMusicInstance(sound, leader);
            PLAYING.put(leader, inst);
            mc.getSoundManager().play(inst);
        }
    }

    // ============================================================
    // 核心逻辑
    // ============================================================

    private static void handleMusicRequest(DisplayEntity entity, @Nullable String animation, String from) {
        String group = entity.getMusicGroup();
        boolean grouped = group != null && !group.isEmpty();

        if (!grouped) {
            ENTITY_GROUP.remove(entity);
            stop(entity);
            if (!inTimeStop) {
                playFor(entity, animation, from + "/independent");
            }
            return;
        }

        ENTITY_GROUP.put(entity, group);

        DisplayEntity leader = GROUP_LEADER.get(group);

        if (leader != null && leader.isAlive() && !leader.isRemoved()) {
            if (leader == entity && !PLAYING.containsKey(entity) && !inTimeStop) {
                stop(entity);
                playFor(entity, animation, from + "/replaySelfLeader");
            }
            return;
        }

        GROUP_LEADER.put(group, entity);
        stop(entity);
        if (!inTimeStop) {
            playFor(entity, animation, from + "/becomeLeader");
        }
    }

    private static void tryPromoteNextLeader(String group) {
        for (Map.Entry<DisplayEntity, String> e : ENTITY_GROUP.entrySet()) {
            DisplayEntity candidate = e.getKey();
            if (group.equals(e.getValue())
                    && candidate != null
                    && candidate.isAlive()
                    && !candidate.isRemoved()) {
                GROUP_LEADER.put(group, candidate);
                stop(candidate);
                if (!inTimeStop) {
                    playFor(candidate, candidate.getAnimation(), "promoteNextLeader");
                }
                return;
            }
        }
    }

    /**
     * 只在非时停时创建实体实例。
     * 时停期间不在这里创建任何玩家实例——玩家实例统一由 onTimeStopStart 创建。
     */
    private static void playFor(DisplayEntity entity, @Nullable String animation, String from) {
        if (animation == null || animation.isEmpty()) return;
        if (inTimeStop) return;

        RegistryObject<SoundEvent> musicRef = ANIMATION_MUSIC.get(animation);
        if (musicRef == null) return;

        SoundEvent music = musicRef.get();
        if (music == null) return;

        String group = entity.getMusicGroup();
        String key = (group != null && !group.isEmpty())
                ? group
                : "entity@" + System.identityHashCode(entity);
        GROUP_SOUND.put(key, music);

        if (PLAYING.containsKey(entity)) return;

        DisplayEntityMusicInstance instance = new DisplayEntityMusicInstance(music, entity);
        PLAYING.put(entity, instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    // ============================================================
    // 停止
    // ============================================================

    public static void stop(DisplayEntity entity) {
        DisplayEntityMusicInstance instance = PLAYING.remove(entity);
        if (instance != null) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
        String group = ENTITY_GROUP.get(entity);
        if (group != null && !inTimeStop) {
            GROUP_SOUND.remove(group);
        }
    }

    public static void stopAll() {
        for (DisplayEntityMusicInstance instance : PLAYING.values()) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
        for (TimeStopMusicInstance instance : TIME_STOP_PLAYING.values()) {
            if (instance != null) Minecraft.getInstance().getSoundManager().stop(instance);
        }
        PLAYING.clear();
        TIME_STOP_PLAYING.clear();
        GROUP_LEADER.clear();
        ENTITY_GROUP.clear();
        GROUP_SOUND.clear();
        inTimeStop = false;
        timeStopEndAtMs = 0L;
    }

    public static void forceLeader(DisplayEntity entity, String group, String animation) {
        entity.setMusicGroup(group);
        DisplayEntity old = GROUP_LEADER.remove(group);
        if (old != null && old != entity) stop(old);
        GROUP_LEADER.put(group, entity);
        ENTITY_GROUP.put(entity, group);
        stop(entity);
        if (!inTimeStop) {
            playFor(entity, animation, "forceLeader");
        }
    }
}