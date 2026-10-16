package net.v_black_cat.goetydelight.entities.display_entity;

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
 */
@OnlyIn(Dist.CLIENT)
public class DisplayEntityMusicInstance extends AbstractTickableSoundInstance {

    private final WeakReference<DisplayEntity> entityRef;

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
        DisplayEntity entity = entityRef.get();
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            stop();
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}