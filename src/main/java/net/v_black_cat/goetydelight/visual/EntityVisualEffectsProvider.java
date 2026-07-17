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
    private final LazyOptional<EntityVisualEffects> optional = LazyOptional.of(() -> effects);
    private final Entity owner;

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
        return EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS.orEmpty(cap, optional.cast());
    }

    @Override
    public CompoundTag serializeNBT() {
        return effects.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        effects.deserializeNBT(nbt);
        if (owner instanceof IVisualEffectHolder holder) {
            holder.goetydelight$setVisualEffects(effects);
        }
    }

    public void invalidate() {
        optional.invalidate();
    }
}
