package net.v_black_cat.goetydelight.util;

import net.minecraft.nbt.CompoundTag;

/**
 * 玩家/实体食物相关状态的统一存储，替代原先散落在 {@code getPersistentData()} 中的标签。
 * Forge 侧通过 capability（FoodStateCapability）附加，NeoForge 侧通过 attachment（ModAttachments.FOOD_STATE）附加。
 */
public class FoodState {

    // 七叶布丁
    private boolean sevenLeafPuddingActive;
    private long sevenLeafPuddingActivationTime;
    // 贤者圣代
    private int philosopherMiningBoost;
    private int philosopherMagicResistance;

    public boolean isSevenLeafPuddingActive() {
        return sevenLeafPuddingActive;
    }

    public void setSevenLeafPuddingActive(boolean sevenLeafPuddingActive) {
        this.sevenLeafPuddingActive = sevenLeafPuddingActive;
    }

    public long getSevenLeafPuddingActivationTime() {
        return sevenLeafPuddingActivationTime;
    }

    public void setSevenLeafPuddingActivationTime(long sevenLeafPuddingActivationTime) {
        this.sevenLeafPuddingActivationTime = sevenLeafPuddingActivationTime;
    }

    public int getPhilosopherMiningBoost() {
        return philosopherMiningBoost;
    }

    public void setPhilosopherMiningBoost(int philosopherMiningBoost) {
        this.philosopherMiningBoost = philosopherMiningBoost;
    }

    public int getPhilosopherMagicResistance() {
        return philosopherMagicResistance;
    }

    public void setPhilosopherMagicResistance(int philosopherMagicResistance) {
        this.philosopherMagicResistance = philosopherMagicResistance;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("sevenLeafPuddingActive", sevenLeafPuddingActive);
        tag.putLong("sevenLeafPuddingActivationTime", sevenLeafPuddingActivationTime);
        tag.putInt("philosopherMiningBoost", philosopherMiningBoost);
        tag.putInt("philosopherMagicResistance", philosopherMagicResistance);
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        sevenLeafPuddingActive = tag.getBoolean("sevenLeafPuddingActive");
        sevenLeafPuddingActivationTime = tag.getLong("sevenLeafPuddingActivationTime");
        philosopherMiningBoost = tag.getInt("philosopherMiningBoost");
        philosopherMagicResistance = tag.getInt("philosopherMagicResistance");
    }
}
