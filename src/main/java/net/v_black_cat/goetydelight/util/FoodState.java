package net.v_black_cat.goetydelight.util;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

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
    // 极地冰剩余时间（旧存档字段，仅在没有 polariceEndTime 时作为回退）
    private float polariceTime;
    // 极地冰到期游戏时间（>0 表示用绝对时间判定，无需任何周期递减）
    private long polariceEndTime;
    // 饼干猫（Biscat）
    private long biscatEffectEndTime;
    private final Map<String, Long> biscatAffectedPlayers = new HashMap<>();
    // 烤老王（RoastLaowang）
    private boolean roastLaowangActive;
    private long roastLaowangStartTime;
    private long roastLaowangDuration;

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

    /**
     * 是否仍处于极地冰时效内。
     *
     * <p>食用时记录的是绝对到期游戏时间，因此不需要「每 20 tick 遍历所有玩家递减一次」的清理循环。
     * 旧存档只有 polariceTime（剩余 tick）而没有到期时间，这里做一次惰性迁移把它折算成绝对时间
     * ——不能直接沿用旧字段判定，否则没人递减它会导致永久免疫。
     */
    public boolean hasActivePolarice(long gameTime) {
        if (polariceEndTime <= 0L && polariceTime > 0.0F) {
            polariceEndTime = gameTime + (long) polariceTime;
            polariceTime = 0.0F;
        }
        return polariceEndTime > gameTime;
    }

    public void setPolariceEndTime(long polariceEndTime) {
        this.polariceEndTime = polariceEndTime;
        this.polariceTime = 0.0F;
    }

    public long getBiscatEffectEndTime() {
        return biscatEffectEndTime;
    }

    public void setBiscatEffectEndTime(long biscatEffectEndTime) {
        this.biscatEffectEndTime = biscatEffectEndTime;
    }

    public Map<String, Long> getBiscatAffectedPlayers() {
        return biscatAffectedPlayers;
    }

    /**
     * 是否仍处于烤老王时效内。
     *
     * <p>用「开始时间 + 时长」现场判定，替代原来每 20 tick 遍历所有玩家清理标记的循环。
     */
    public boolean isRoastLaowangActive(long gameTime) {
        return roastLaowangActive && gameTime - roastLaowangStartTime < roastLaowangDuration;
    }

    public void setRoastLaowangActive(boolean roastLaowangActive) {
        this.roastLaowangActive = roastLaowangActive;
    }

    public long getRoastLaowangStartTime() {
        return roastLaowangStartTime;
    }

    public void setRoastLaowangStartTime(long roastLaowangStartTime) {
        this.roastLaowangStartTime = roastLaowangStartTime;
    }

    public long getRoastLaowangDuration() {
        return roastLaowangDuration;
    }

    public void setRoastLaowangDuration(long roastLaowangDuration) {
        this.roastLaowangDuration = roastLaowangDuration;
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
        tag.putLong("polariceEndTime", polariceEndTime);
        tag.putLong("biscatEffectEndTime", biscatEffectEndTime);
        CompoundTag affectedPlayers = new CompoundTag();
        for (Map.Entry<String, Long> e : biscatAffectedPlayers.entrySet()) {
            affectedPlayers.putLong(e.getKey(), e.getValue());
        }
        tag.put("biscatAffectedPlayers", affectedPlayers);
        tag.putBoolean("roastLaowangActive", roastLaowangActive);
        tag.putLong("roastLaowangStartTime", roastLaowangStartTime);
        tag.putLong("roastLaowangDuration", roastLaowangDuration);
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
        polariceEndTime = tag.getLong("polariceEndTime");
        biscatEffectEndTime = tag.getLong("biscatEffectEndTime");
        biscatAffectedPlayers.clear();
        CompoundTag affectedPlayers = tag.getCompound("biscatAffectedPlayers");
        for (String key : affectedPlayers.getAllKeys()) {
            biscatAffectedPlayers.put(key, affectedPlayers.getLong(key));
        }
        roastLaowangActive = tag.getBoolean("roastLaowangActive");
        roastLaowangStartTime = tag.getLong("roastLaowangStartTime");
        roastLaowangDuration = tag.getLong("roastLaowangDuration");
    }
}
