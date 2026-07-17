package net.v_black_cat.goetydelight.buff;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class BuffCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final ActiveBuffs buffs = new ActiveBuffs();
    private final LazyOptional<ActiveBuffs> optional = LazyOptional.of(() -> buffs);
    private final LivingEntity owner;

    public BuffCapabilityProvider(LivingEntity owner) {
        this.owner = owner;
        if (owner instanceof IBuffHolder holder) {
            holder.goetydelight$setActiveBuffs(buffs);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // 如果 Mixin 字段被清除了，重新设置
        if (owner instanceof IBuffHolder holder && holder.goetydelight$getActiveBuffs() == null) {
            holder.goetydelight$setActiveBuffs(buffs);
        }
        return BuffSystem.ACTIVE_BUFFS_CAP.orEmpty(cap, optional.cast());
    }

    @Override
    public CompoundTag serializeNBT() {
        return buffs.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        buffs.deserializeNBT(nbt);
        if (owner instanceof IBuffHolder holder) {
            holder.goetydelight$setActiveBuffs(buffs);
        }
    }

    public void invalidate() {
        optional.invalidate();
    }
}
