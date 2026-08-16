package net.v_black_cat.goetydelight.ability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * 玩家仆从加成的持久化数据（炖菜/汤的累计层数）。
 * 替代原先使用 {@code player.getPersistentData()} 存储的方案。
 */
public class MinionBoostData implements INBTSerializable<CompoundTag> {

    private int stewCount;
    private int soupCount;

    public int getStewCount() {
        return stewCount;
    }

    public int getSoupCount() {
        return soupCount;
    }

    public void setStewCount(int stewCount) {
        this.stewCount = stewCount;
    }

    public void setSoupCount(int soupCount) {
        this.soupCount = soupCount;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("stewCount", stewCount);
        tag.putInt("soupCount", soupCount);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.stewCount = nbt.getInt("stewCount");
        this.soupCount = nbt.getInt("soupCount");
    }
}
