package net.v_black_cat.goetydelight.entities.display_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * 时停期间使用的音乐实例：挂玩家、相对播放、不做 3D 衰减。
 * 到 endAtMs 自动停止并回调 onFinished，不依赖实体移除事件。
 *
 * 注意：SoundEngine.tickNonPaused 会在遍历实例列表时调用 tick()，
 * 因此 tick() 内不能直接 stop() / 回调 SoundManager 操作，
 * 必须通过 Minecraft.execute 延迟到当前遍历结束后执行，否则会 CME。
 */
@OnlyIn(Dist.CLIENT)
public class TimeStopMusicInstance extends AbstractTickableSoundInstance {

    private final WeakReference<Player> playerRef;

    /** 系统毫秒时间戳；<=0 表示不自动结束 */
    private final long endAtMs;

    /** 结束后回调（由 manager 注册），用于恢复实体实例 */
    @Nullable
    private final Runnable onFinished;

    /** 防止 finish 被重复投递 */
    private boolean finishing = false;

    public TimeStopMusicInstance(SoundEvent sound, Player player,
                                 long endAtMs, @Nullable Runnable onFinished) {
        super(sound, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.playerRef = new WeakReference<>(player);
        this.endAtMs = endAtMs;
        this.onFinished = onFinished;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = true;                              // 相对玩家，不参与世界坐标重算
        this.attenuation = SoundInstance.Attenuation.NONE; // 不做 3D 衰减
    }

    @Override
    public void tick() {
        if (finishing) return;

        Player p = playerRef.get();
        if (p == null || !p.isAlive()) {
            scheduleFinish();
            return;
        }
        if (endAtMs > 0 && System.currentTimeMillis() >= endAtMs) {
            scheduleFinish();
        }
        // 不写坐标，避免 SoundEngine 每 tick 做 attenuation 重算
    }

    /** 不能在 tick() 里直接 stop()/回调：SoundEngine 正在遍历实例列表，会 CME */
    private void scheduleFinish() {
        if (finishing) return;
        finishing = true;
        Minecraft.getInstance().execute(this::finishNow);
    }

    private void finishNow() {
        stop();
        Runnable cb = onFinished;
        if (cb != null) cb.run();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}