package net.v_black_cat.goetydelight.visual;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.v_black_cat.goetydelight.mixin.EntityVisualEffectMixin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityVisualEffectsProvider implements ICapabilitySerializable<CompoundTag> {
    private final EntityVisualEffects effects = new EntityVisualEffects();
    private final Entity owner;
    /** 见 {@link #getCapability}：可能被 invalidateCaps() 永久置空，因此需要按需重建，不能是 final。 */
    private LazyOptional<EntityVisualEffects> optional = LazyOptional.of(() -> effects);

    public EntityVisualEffectsProvider(Entity owner) {
        this.owner = owner;
        if (owner instanceof IVisualEffectHolder holder) {
            holder.goetydelight$setVisualEffects(effects);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (owner instanceof IVisualEffectHolder holder && holder.goetydelight$getVisualEffects() == null) {
            holder.goetydelight$setVisualEffects(effects);
        }
        // Forge 的 invalidateCaps() 会把 LazyOptional 永久置为无效，而 reviveCaps() 只恢复 CapabilityProvider
        // 自己的 valid 标记，不会复活 provider 的 LazyOptional。结果是：实体经历过一次
        // invalidateCaps()/reviveCaps()（换维度、重生、区块重载等）之后，entity.getCapability() 永远返回空 ——
        // 服务端 addEffect/removeEffect 静默失败（表现就是「指令没反应、特效不显示」），客户端渲染也会直接跳过。
        // 容器实例本身一直有效，所以这里重建一个 LazyOptional 即可自愈。
        if (!optional.isPresent()) {
            optional = LazyOptional.of(() -> effects);
        }
        return EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS.orEmpty(cap, optional.cast());
    }

    @Override
    public CompoundTag serializeNBT() {
        return effects.serializeNBT(currentGameTime());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        effects.deserializeNBT(nbt, currentGameTime());
        if (owner instanceof IVisualEffectHolder holder) {
            holder.goetydelight$setVisualEffects(effects);
        }
        EntityVisualEffectSystem.schedule(owner, effects);
    }

    private long currentGameTime() {
        return owner.level() == null
                ? ActiveEntityVisualEffect.NO_GAME_TIME
                : owner.level().getGameTime();
    }

    public void invalidate() {
        optional.invalidate();
    }
}
