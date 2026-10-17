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

@OnlyIn(Dist.CLIENT)
public final class DisplayEntityMusicManager {
    private static final Map<String, RegistryObject<SoundEvent>> ANIMATION_MUSIC = Map.of(
            "kwil", ModSounds.MUSIC_KWIL
    );

    private static final Map<DisplayEntity, DisplayEntityMusicInstance> PLAYING =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<String, DisplayEntity> GROUP_LEADER =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<DisplayEntity, String> ENTITY_GROUP =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    private DisplayEntityMusicManager() {
    }


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
        if (group == null) {
            stop(entity);
            return;
        }

        DisplayEntity leader = GROUP_LEADER.get(group);
        if (leader == entity) {
            GROUP_LEADER.remove(group);
            stop(entity);
            tryPromoteNextLeader(group);
        } else {
            stop(entity);
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
            playFor(entity, animation, from + "/independent");
            return;
        }

        ENTITY_GROUP.put(entity, group);

        DisplayEntity leader = GROUP_LEADER.get(group);

        if (leader != null && leader.isAlive() && !leader.isRemoved()) {
            if (leader == entity && !PLAYING.containsKey(entity)) {
                stop(entity);
                playFor(entity, animation, from + "/replaySelfLeader");
            }
            return;
        }

        GROUP_LEADER.put(group, entity);
        stop(entity);
        playFor(entity, animation, from + "/becomeLeader");
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
                playFor(candidate, candidate.getAnimation(), "promoteNextLeader");
                return;
            }
        }
    }

    private static void playFor(DisplayEntity entity, @Nullable String animation, String from) {
        if (animation == null || animation.isEmpty()) {
            return;
        }

        RegistryObject<SoundEvent> musicRef = ANIMATION_MUSIC.get(animation);
        if (musicRef == null) {
            return;
        }

        SoundEvent music = musicRef.get();
        if (music == null) {
            return;
        }

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

    public static void forceLeader(DisplayEntity entity, String group, String animation) {
        entity.setMusicGroup(group);
        DisplayEntity old = GROUP_LEADER.remove(group);
        if (old != null && old != entity) {
            stop(old);
        }
        GROUP_LEADER.put(group, entity);
        ENTITY_GROUP.put(entity, group);
        stop(entity);
        playFor(entity, animation, "forceLeader");
    }
}