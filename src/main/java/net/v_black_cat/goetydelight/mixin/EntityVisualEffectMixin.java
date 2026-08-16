package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.Entity;
import net.v_black_cat.goetydelight.visual.EntityVisualEffects;
import net.v_black_cat.goetydelight.visual.IVisualEffectHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.1 移植版：与 1.20.1 一致，通过 mixin 接口（{@link IVisualEffectHolder}）直接在 Entity 上
 * 缓存 {@link EntityVisualEffects}，避免每 tick 走附件查找。附件（{@code ModAttachments.VISUAL_EFFECTS}）
 * 仍负责持久化、死亡复制与客户端同步，字段只是服务端的快速访问缓存。
 */
@Mixin(Entity.class)
public abstract class EntityVisualEffectMixin implements IVisualEffectHolder {

    @Unique
    private EntityVisualEffects goetydelight$effects;

    @Override
    public EntityVisualEffects goetydelight$getVisualEffects() {
        return goetydelight$effects;
    }

    @Override
    public void goetydelight$setVisualEffects(EntityVisualEffects effects) {
        this.goetydelight$effects = effects;
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void goetydelight$onRemove(CallbackInfo ci) {
        if (this.goetydelight$effects != null) {
            this.goetydelight$effects.clear();
            this.goetydelight$effects = null;
        }
    }
}
