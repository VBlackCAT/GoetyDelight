package net.v_black_cat.goetydelight.compat.goetyrevelation.util;

import com.mega.revelationfix.common.init.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class NeedleAttributeUtil {

    public static final UUID NEEDLE_HP_UUID  = UUID.fromString("1b242614-c859-4088-bff7-b2809f9bbe18");
    public static final UUID NEEDLE_ATK_UUID = UUID.fromString("9f2e7534-e108-4c75-b71c-64c3b407f39b");
    public static final UUID NEEDLE_SPD_UUID = UUID.fromString("44a1da2a-8219-49cf-a523-8b710f0464a1");
    public static final UUID NEEDLE_RES_UUID = UUID.fromString("af236f3f-f82a-4008-97f9-5af0173b2ec9");

    public static final double RES_CAP = 0.85;
    public static final double CANDY_ATTR_CAP = 1.0; // 糖的属性加成上限 (100%)

    public static double amountOf(AttributeInstance inst, UUID id) {
        if (inst == null) return 0;
        AttributeModifier m = inst.getModifier(id);
        return m == null ? 0 : m.getAmount();
    }

    public static void setModifier(ServerPlayer player, Attribute attr, UUID uuid, String name, double amount) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier old = inst.getModifier(uuid);
        if (old != null) inst.removeModifier(old);
        if (amount != 0) {
            inst.addPermanentModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    public static double getTotalResistance(ServerPlayer player) {
        AttributeInstance inst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());
        if (inst == null) return 0;
        double total = 0;
        for (AttributeModifier m : inst.getModifiers()) {
            if (m.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || m.getOperation() == AttributeModifier.Operation.ADDITION) {
                total += m.getAmount();
            }
        }
        return total;
    }

    /**
     * 应用糖加成：
     *   1. 从 consumedMap 现算糖原始总量 raw
     *   2. 剥离"上次写入量" applied，得到针裸值 base
     *   3. 对 raw 做单项 100% cap
     *   4. 抗性 85% cap（针+糖合计），溢出转三维且过 100% cap
     *   5. 覆盖写入 base + capped 糖，并回写 applied
     */
    public static void addCandyBonus(ServerPlayer player) {
        if (player.server == null) return;
        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);
        UUID id = player.getUUID();

        double[] raw = data.computeCandyFromConsumed(id);
        if (raw[0] == 0 && raw[1] == 0 && raw[2] == 0 && raw[3] == 0) {
            // 无糖：也要把上次写入量剥离掉（防止残留）
            double[] applied = data.getCandyBonus(id);
            if (applied[0] != 0 || applied[1] != 0 || applied[2] != 0 || applied[3] != 0) {
                data.isRefreshingSet.add(id);
                try {
                    stripApplied(player, applied);
                    data.setCandyBonus(id, 0, 0, 0, 0);
                } finally {
                    data.isRefreshingSet.remove(id);
                }
            }
            return;
        }

        data.isRefreshingSet.add(id);
        try {
            // 1. 剥离上次写入的糖，得到"针裸值"
            double[] applied = data.getCandyBonus(id);
            double baseHp  = amountOf(player.getAttribute(Attributes.MAX_HEALTH),  NEEDLE_HP_UUID)  - applied[0];
            double baseAtk = amountOf(player.getAttribute(Attributes.ATTACK_DAMAGE), NEEDLE_ATK_UUID) - applied[1];
            double baseSpd = amountOf(player.getAttribute(Attributes.ATTACK_SPEED),  NEEDLE_SPD_UUID) - applied[2];
            double baseRes = amountOf(player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()), NEEDLE_RES_UUID) - applied[3];

            // 2. 对糖原始总量做单项 100% cap
            double addHp  = Math.min(raw[0], CANDY_ATTR_CAP);
            double addAtk = Math.min(raw[1], CANDY_ATTR_CAP);
            double addSpd = Math.min(raw[2], CANDY_ATTR_CAP);
            double addRes = raw[3];

            // 3. 抗性 85% cap：otherRes 是"非针非糖"来源
            double otherRes = getTotalResistance(player) - baseRes - applied[3];
            if (otherRes + baseRes + addRes > RES_CAP) {
                double overflow = otherRes + baseRes + addRes - RES_CAP;
                addRes = Math.max(0, RES_CAP - otherRes - baseRes);
                addHp  = Math.min(addHp  + overflow / 3.0, CANDY_ATTR_CAP);
                addAtk = Math.min(addAtk + overflow / 3.0, CANDY_ATTR_CAP);
                addSpd = Math.min(addSpd + overflow / 3.0, CANDY_ATTR_CAP);
            }

            // 4. 覆盖写入
            setModifier(player, Attributes.MAX_HEALTH,  NEEDLE_HP_UUID,  "Needle modifier", baseHp  + addHp);
            setModifier(player, Attributes.ATTACK_DAMAGE, NEEDLE_ATK_UUID, "Needle modifier", baseAtk + addAtk);
            setModifier(player, Attributes.ATTACK_SPEED,  NEEDLE_SPD_UUID, "Needle modifier", baseSpd + addSpd);
            setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID, "Needle modifier", baseRes + addRes);

            // 5. 回写"本次实际写入的糖量"
            data.setCandyBonus(id, addHp, addAtk, addSpd, addRes);
        } finally {
            data.isRefreshingSet.remove(id);
        }
    }

    /** 把 applied 从当前 Modifier 里剥离掉（用于无糖或 Mixin 剥离）。 */
    public static void stripApplied(ServerPlayer player, double[] applied) {
        setModifier(player, Attributes.MAX_HEALTH,
                NEEDLE_HP_UUID, "Needle modifier",
                amountOf(player.getAttribute(Attributes.MAX_HEALTH), NEEDLE_HP_UUID) - applied[0]);
        setModifier(player, Attributes.ATTACK_DAMAGE,
                NEEDLE_ATK_UUID, "Needle modifier",
                amountOf(player.getAttribute(Attributes.ATTACK_DAMAGE), NEEDLE_ATK_UUID) - applied[1]);
        setModifier(player, Attributes.ATTACK_SPEED,
                NEEDLE_SPD_UUID, "Needle modifier",
                amountOf(player.getAttribute(Attributes.ATTACK_SPEED), NEEDLE_SPD_UUID) - applied[2]);
        setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(),
                NEEDLE_RES_UUID, "Needle modifier",
                amountOf(player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()), NEEDLE_RES_UUID) - applied[3]);
    }

    /**
     * 扎针后，如果原版针把总抗性搞超了 85%，就强行砍掉针的抗性，溢出转三维。
     * 调用前应已剥离糖（applied 已清零），此时 NEEDLE_RES_UUID 的值 = 针裸值 + 本次随机增量。
     */
    public static void capNeedleResistance(ServerPlayer player) {
        AttributeInstance resInst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());
        if (resInst == null) return;

        double totalRes = getTotalResistance(player);
        if (totalRes > RES_CAP) {
            double overflow = totalRes - RES_CAP;
            double curRes = amountOf(resInst, NEEDLE_RES_UUID);
            double newRes = Math.max(0, curRes - overflow);

            setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID, "Needle modifier", newRes);

            AttributeInstance hpInst  = player.getAttribute(Attributes.MAX_HEALTH);
            AttributeInstance atkInst = player.getAttribute(Attributes.ATTACK_DAMAGE);
            AttributeInstance spdInst = player.getAttribute(Attributes.ATTACK_SPEED);

            setModifier(player, Attributes.MAX_HEALTH,  NEEDLE_HP_UUID,  "Needle modifier",
                    amountOf(hpInst,  NEEDLE_HP_UUID)  + overflow / 3.0);
            setModifier(player, Attributes.ATTACK_DAMAGE, NEEDLE_ATK_UUID, "Needle modifier",
                    amountOf(atkInst, NEEDLE_ATK_UUID) + overflow / 3.0);
            setModifier(player, Attributes.ATTACK_SPEED,  NEEDLE_SPD_UUID, "Needle modifier",
                    amountOf(spdInst, NEEDLE_SPD_UUID) + overflow / 3.0);
        }
    }

    /** 兼容旧名字。 */
    public static void modifyCandyBonus(ServerPlayer player) {
        addCandyBonus(player);
    }
}