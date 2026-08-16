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
    // 不详冰淇淋
    private boolean ominousIceCreamActive;
    private boolean ominousIceCreamConsumed;
    // 骨领主骨灰拌饭
    private boolean boneLordAshRiceActive;
    private long boneLordAshRiceActivationTime;
    // 红宝石硬糖法强
    private int candyPotencyLevel;
    private double effectBonus;
    // 巴克拉瓦冷却
    private long baklavaCooldown;
    // 毒物饭食用次数
    private int toxicMealCount;
    // 远古附魔金苹果剩余次数
    private int ancientGoldenAppleCount;
    // 深红记忆标记
    private boolean crimsonMemories;
    // 虚假箴言下蹲状态
    private boolean falseProverbsShift;
    // 极地冰剩余时间
    private float polariceTime;

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

    public boolean isOminousIceCreamActive() {
        return ominousIceCreamActive;
    }

    public void setOminousIceCreamActive(boolean ominousIceCreamActive) {
        this.ominousIceCreamActive = ominousIceCreamActive;
    }

    public boolean isOminousIceCreamConsumed() {
        return ominousIceCreamConsumed;
    }

    public void setOminousIceCreamConsumed(boolean ominousIceCreamConsumed) {
        this.ominousIceCreamConsumed = ominousIceCreamConsumed;
    }

    public boolean isBoneLordAshRiceActive() {
        return boneLordAshRiceActive;
    }

    public void setBoneLordAshRiceActive(boolean boneLordAshRiceActive) {
        this.boneLordAshRiceActive = boneLordAshRiceActive;
    }

    public long getBoneLordAshRiceActivationTime() {
        return boneLordAshRiceActivationTime;
    }

    public void setBoneLordAshRiceActivationTime(long boneLordAshRiceActivationTime) {
        this.boneLordAshRiceActivationTime = boneLordAshRiceActivationTime;
    }

    public int getCandyPotencyLevel() {
        return candyPotencyLevel;
    }

    public void setCandyPotencyLevel(int candyPotencyLevel) {
        this.candyPotencyLevel = candyPotencyLevel;
    }

    public double getEffectBonus() {
        return effectBonus;
    }

    public void setEffectBonus(double effectBonus) {
        this.effectBonus = effectBonus;
    }

    public long getBaklavaCooldown() {
        return baklavaCooldown;
    }

    public void setBaklavaCooldown(long baklavaCooldown) {
        this.baklavaCooldown = baklavaCooldown;
    }

    public int getToxicMealCount() {
        return toxicMealCount;
    }

    public void setToxicMealCount(int toxicMealCount) {
        this.toxicMealCount = toxicMealCount;
    }

    public int getAncientGoldenAppleCount() {
        return ancientGoldenAppleCount;
    }

    public void setAncientGoldenAppleCount(int ancientGoldenAppleCount) {
        this.ancientGoldenAppleCount = ancientGoldenAppleCount;
    }

    public boolean isCrimsonMemories() {
        return crimsonMemories;
    }

    public void setCrimsonMemories(boolean crimsonMemories) {
        this.crimsonMemories = crimsonMemories;
    }

    public boolean isFalseProverbsShift() {
        return falseProverbsShift;
    }

    public void setFalseProverbsShift(boolean falseProverbsShift) {
        this.falseProverbsShift = falseProverbsShift;
    }

    public float getPolariceTime() {
        return polariceTime;
    }

    public void setPolariceTime(float polariceTime) {
        this.polariceTime = polariceTime;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("sevenLeafPuddingActive", sevenLeafPuddingActive);
        tag.putLong("sevenLeafPuddingActivationTime", sevenLeafPuddingActivationTime);
        tag.putInt("philosopherMiningBoost", philosopherMiningBoost);
        tag.putInt("philosopherMagicResistance", philosopherMagicResistance);
        tag.putBoolean("ominousIceCreamActive", ominousIceCreamActive);
        tag.putBoolean("ominousIceCreamConsumed", ominousIceCreamConsumed);
        tag.putBoolean("boneLordAshRiceActive", boneLordAshRiceActive);
        tag.putLong("boneLordAshRiceActivationTime", boneLordAshRiceActivationTime);
        tag.putInt("candyPotencyLevel", candyPotencyLevel);
        tag.putDouble("effectBonus", effectBonus);
        tag.putLong("baklavaCooldown", baklavaCooldown);
        tag.putInt("toxicMealCount", toxicMealCount);
        tag.putInt("ancientGoldenAppleCount", ancientGoldenAppleCount);
        tag.putBoolean("crimsonMemories", crimsonMemories);
        tag.putBoolean("falseProverbsShift", falseProverbsShift);
        tag.putFloat("polariceTime", polariceTime);
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        sevenLeafPuddingActive = tag.getBoolean("sevenLeafPuddingActive");
        sevenLeafPuddingActivationTime = tag.getLong("sevenLeafPuddingActivationTime");
        philosopherMiningBoost = tag.getInt("philosopherMiningBoost");
        philosopherMagicResistance = tag.getInt("philosopherMagicResistance");
        ominousIceCreamActive = tag.getBoolean("ominousIceCreamActive");
        ominousIceCreamConsumed = tag.getBoolean("ominousIceCreamConsumed");
        boneLordAshRiceActive = tag.getBoolean("boneLordAshRiceActive");
        boneLordAshRiceActivationTime = tag.getLong("boneLordAshRiceActivationTime");
        candyPotencyLevel = tag.getInt("candyPotencyLevel");
        effectBonus = tag.getDouble("effectBonus");
        baklavaCooldown = tag.getLong("baklavaCooldown");
        toxicMealCount = tag.getInt("toxicMealCount");
        ancientGoldenAppleCount = tag.getInt("ancientGoldenAppleCount");
        crimsonMemories = tag.getBoolean("crimsonMemories");
        falseProverbsShift = tag.getBoolean("falseProverbsShift");
        polariceTime = tag.getFloat("polariceTime");
    }
}
