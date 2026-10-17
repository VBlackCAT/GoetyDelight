package net.v_black_cat.goetydelight.entities.display_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;

/**
 * 跟随 DisplayEntity 的音乐 SoundInstance。
 * 循环播放，位置随实体更新，实体消失时自动停止。
 *
 * 注意：tick() 内不能直接 stop()，必须 Minecraft.execute 延迟，
 * 否则 SoundEngine.tickNonPaused 遍历期间修改列表会抛 ConcurrentModificationException。
 */
@OnlyIn(Dist.CLIENT)
public class DisplayEntityMusicInstance extends AbstractTickableSoundInstance {

    private final WeakReference<DisplayEntity> entityRef;

    /** 防止 stop 被重复投递 */
    private boolean finished = false;

    public DisplayEntityMusicInstance(SoundEvent sound, DisplayEntity entity) {
        super(sound, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.entityRef = new WeakReference<>(entity);
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;

        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        if (finished) return;

        DisplayEntity entity = entityRef.get();
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            scheduleStop();
            return;
        }

        // 时停期间不跟随坐标（时停中实体 tick 被冻结，坐标更新会引发 SoundEngine 重算抖动）
        if (DisplayEntityMusicManager.isInTimeStop()) {
            return;
        }

        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    /** 延迟到 SoundEngine 遍历结束后再 stop，避免 CME */
    private void scheduleStop() {
        if (finished) return;
        finished = true;
        Minecraft.getInstance().execute(this::stopSafely);
    }

    /** 再次延迟一层，确保一定不在 SoundEngine 遍历期间执行 */
    private void stopSafely() {
        Minecraft.getInstance().execute(super::stop);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}