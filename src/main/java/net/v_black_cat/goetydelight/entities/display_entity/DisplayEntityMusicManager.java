package net.v_black_cat.goetydelight.entities.display_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.util.ModSounds;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DisplayEntity 的动画音乐管理器。
 * <p>
 * 支持音乐分组：同一 {@code MusicGroup} 的多个实体共享同一份 SoundInstance。
 * 组内第一个注册的实体作为「组长」负责播放，其余实体不播。
 * 组长消失时，组内下一个实体自动接管。
 */
@OnlyIn(Dist.CLIENT)
public final class DisplayEntityMusicManager {

    /** 动画名 → 音乐 */
    private static final Map<String, RegistryObject<SoundEvent>> ANIMATION_MUSIC = Map.of(
            "kwil", ModSounds.MUSIC_KWIL
    );

    /** 组长实体 → 正在播的 SoundInstance */
    private static final Map<DisplayEntity, DisplayEntityMusicInstance> PLAYING =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    /** MusicGroup → 当前组长实体（强引用实体？用 WeakHashMap 更安全） */
    private static final Map<String, DisplayEntity> GROUP_LEADER =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    /** 实体 → 它所属的 MusicGroup（仅用于移除时反查） */
    private static final Map<DisplayEntity, String> ENTITY_GROUP =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    private DisplayEntityMusicManager() {
    }

    // ============================================================
    // 事件入口
    // ============================================================

    /** 动画名变化 */
    public static void onAnimationChanged(DisplayEntity entity, @Nullable String animation) {
        handleMusicRequest(entity, animation);
    }

    /** MusicGroup 变化 */
    public static void onMusicGroupChanged(DisplayEntity entity, @Nullable String group) {
        // 先把旧的组关系解绑
        String oldGroup = ENTITY_GROUP.remove(entity);
        if (oldGroup != null && oldGroup.equals(GROUP_LEADER.get(oldGroup))) {
            // 自己是旧组的组长 → 释放组长位置
            GROUP_LEADER.remove(oldGroup);
        }
        // 重新处理
        handleMusicRequest(entity, entity.getAnimation());
    }

    /** 实体被移除 */
    public static void onEntityRemoved(DisplayEntity entity) {
        String group = ENTITY_GROUP.remove(entity);
        if (group == null) {
            // 独立播放
            stop(entity);
            return;
        }

        // 是组长吗？
        DisplayEntity leader = GROUP_LEADER.get(group);
        if (leader == entity) {
            GROUP_LEADER.remove(group);
            stop(entity);
            // 组内其他实体接管
            tryPromoteNextLeader(group);
        } else {
            // 只是普通成员，无需处理
            stop(entity);
        }
    }

    // ============================================================
    // 核心逻辑
    // ============================================================

    /**
     * 处理一次"播放请求"。会根据 MusicGroup 决定是自己播、还是让组长播、还是什么都不做。
     */
    private static void handleMusicRequest(DisplayEntity entity, @Nullable String animation) {
        String group = entity.getMusicGroup();
        boolean grouped = group != null && !group.isEmpty();

        if (!grouped) {
            // 独立播放：直接播
            ENTITY_GROUP.remove(entity);
            stop(entity);
            playFor(entity, animation);
            return;
        }

        // 记录组关系
        ENTITY_GROUP.put(entity, group);

        // 组里已经有组长吗？
        DisplayEntity leader = GROUP_LEADER.get(group);
        if (leader != null && leader.isAlive() && !leader.isRemoved()) {
            // 有组长 → 自己什么都不做
            return;
        }

        // 没有组长（或组长已死）→ 自己当组长
        GROUP_LEADER.put(group, entity);
        stop(entity);
        playFor(entity, animation);
    }

    /** 把组里还活着的下一个实体推为组长 */
    private static void tryPromoteNextLeader(String group) {
        for (Map.Entry<DisplayEntity, String> e : ENTITY_GROUP.entrySet()) {
            DisplayEntity candidate = e.getKey();
            if (group.equals(e.getValue())
                    && candidate != null
                    && candidate.isAlive()
                    && !candidate.isRemoved()) {
                GROUP_LEADER.put(group, candidate);
                stop(candidate);
                playFor(candidate, candidate.getAnimation());
                return;
            }
        }
    }

    /** 真正的播放逻辑 */
    private static void playFor(DisplayEntity entity, @Nullable String animation) {
        if (animation == null || animation.isEmpty()) return;

        RegistryObject<SoundEvent> musicRef = ANIMATION_MUSIC.get(animation);
        if (musicRef == null) return;

        SoundEvent music = musicRef.get();
        DisplayEntityMusicInstance instance =
                new DisplayEntityMusicInstance(music, entity);
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
    }

    public static void stopAll() {
        for (DisplayEntityMusicInstance instance : PLAYING.values()) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
        PLAYING.clear();
        GROUP_LEADER.clear();
        ENTITY_GROUP.clear();
    }

    // ============================================================
    // 外部 API
    // ============================================================

    /**
     * 手动指定一个实体为某组的组长并播放动画音乐。
     * 如果该组已有组长，会先把旧组长停掉，再让新组长接管。
     */
    public static void forceLeader(DisplayEntity entity, String group, String animation) {
        entity.setMusicGroup(group);
        DisplayEntity old = GROUP_LEADER.remove(group);
        if (old != null && old != entity) {
            stop(old);
        }
        GROUP_LEADER.put(group, entity);
        ENTITY_GROUP.put(entity, group);
        stop(entity);
        playFor(entity, animation);
    }
}