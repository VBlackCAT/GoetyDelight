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
    public static final double CANDY_ATTR_CAP = 1.0;

    public static double amountOf(AttributeInstance inst, UUID id) {
        if (inst == null) return 0;
        AttributeModifier m = inst.getModifier(id);
        return m == null ? 0 : m.getAmount();
    }

    public static boolean hasNeedleModifier(ServerPlayer player, Attribute attr, UUID uuid) {
        AttributeInstance inst = player.getAttribute(attr);
        return inst != null && inst.getModifier(uuid) != null;
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
     * applied 语义：当前 modifier 里含有的糖量。
     * - modifier 存在：base = cur - applied
     * - modifier 不存在：base = 0
     */
    public static void addCandyBonus(ServerPlayer player) {
        if (player.server == null) return;
        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);
        UUID id = player.getUUID();

        double[] raw = data.computeCandyFromConsumed(id);
        boolean hasCandy = raw[0] != 0 || raw[1] != 0 || raw[2] != 0 || raw[3] != 0;

        data.isRefreshingSet.add(id);
        try {
            double[] applied = data.getCandyBonus(id);

            boolean hasHp  = hasNeedleModifier(player, Attributes.MAX_HEALTH,  NEEDLE_HP_UUID);
            boolean hasAtk = hasNeedleModifier(player, Attributes.ATTACK_DAMAGE, NEEDLE_ATK_UUID);
            boolean hasSpd = hasNeedleModifier(player, Attributes.ATTACK_SPEED,  NEEDLE_SPD_UUID);
            boolean hasRes = hasNeedleModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID);

            double baseHp  = hasHp  ? amountOf(player.getAttribute(Attributes.MAX_HEALTH),  NEEDLE_HP_UUID)  - applied[0] : 0;
            double baseAtk = hasAtk ? amountOf(player.getAttribute(Attributes.ATTACK_DAMAGE), NEEDLE_ATK_UUID) - applied[1] : 0;
            double baseSpd = hasSpd ? amountOf(player.getAttribute(Attributes.ATTACK_SPEED),  NEEDLE_SPD_UUID) - applied[2] : 0;
            double baseRes = hasRes ? amountOf(player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()), NEEDLE_RES_UUID) - applied[3] : 0;

            if (!hasCandy) {
                if (hasHp || hasAtk || hasSpd || hasRes) {
                    setModifier(player, Attributes.MAX_HEALTH,  NEEDLE_HP_UUID,  "Needle modifier", baseHp);
                    setModifier(player, Attributes.ATTACK_DAMAGE, NEEDLE_ATK_UUID, "Needle modifier", baseAtk);
                    setModifier(player, Attributes.ATTACK_SPEED,  NEEDLE_SPD_UUID, "Needle modifier", baseSpd);
                    setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID, "Needle modifier", baseRes);
                    data.setCandyBonus(id, 0, 0, 0, 0);
                }
                return;
            }

            double addHp  = Math.min(raw[0], CANDY_ATTR_CAP);
            double addAtk = Math.min(raw[1], CANDY_ATTR_CAP);
            double addSpd = Math.min(raw[2], CANDY_ATTR_CAP);
            double addRes = raw[3];

            double otherRes = getTotalResistance(player) - baseRes - applied[3];
            if (otherRes + baseRes + addRes > RES_CAP) {
                double overflow = otherRes + baseRes + addRes - RES_CAP;
                addRes = Math.max(0, RES_CAP - otherRes - baseRes);
                addHp  = Math.min(addHp  + overflow / 3.0, CANDY_ATTR_CAP);
                addAtk = Math.min(addAtk + overflow / 3.0, CANDY_ATTR_CAP);
                addSpd = Math.min(addSpd + overflow / 3.0, CANDY_ATTR_CAP);
            }

            setModifier(player, Attributes.MAX_HEALTH,  NEEDLE_HP_UUID,  "Needle modifier", baseHp  + addHp);
            setModifier(player, Attributes.ATTACK_DAMAGE, NEEDLE_ATK_UUID, "Needle modifier", baseAtk + addAtk);
            setModifier(player, Attributes.ATTACK_SPEED,  NEEDLE_SPD_UUID, "Needle modifier", baseSpd + addSpd);
            setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID, "Needle modifier", baseRes + addRes);

            data.setCandyBonus(id, addHp, addAtk, addSpd, addRes);
        } finally {
            data.isRefreshingSet.remove(id);
        }
    }

    /** 剥离单项（供 Mixin 精确控制用）。modifier 不存在则跳过。 */
    public static void stripOne(ServerPlayer player, Attribute attr, UUID uuid, double appliedAmount) {
        if (appliedAmount <= 0) return;
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null || inst.getModifier(uuid) == null) return;
        setModifier(player, attr, uuid, "Needle modifier", amountOf(inst, uuid) - appliedAmount);
    }

    /** 兼容旧调用：剥离四项，返回实际剥离量。 */
    public static double[] stripApplied(ServerPlayer player, double[] applied) {
        double[] stripped = new double[4];
        for (int i = 0; i < 4; i++) {
            Attribute attr = switch (i) {
                case 0 -> Attributes.MAX_HEALTH;
                case 1 -> Attributes.ATTACK_DAMAGE;
                case 2 -> Attributes.ATTACK_SPEED;
                default -> ModAttributes.DAMAGE_RESISTANCE.get();
            };
            UUID uuid = switch (i) {
                case 0 -> NEEDLE_HP_UUID;
                case 1 -> NEEDLE_ATK_UUID;
                case 2 -> NEEDLE_SPD_UUID;
                default -> NEEDLE_RES_UUID;
            };
            AttributeInstance inst = player.getAttribute(attr);
            if (inst != null && inst.getModifier(uuid) != null && applied[i] > 0) {
                stripOne(player, attr, uuid, applied[i]);
                stripped[i] = applied[i];
            }
        }
        return stripped;
    }

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

    public static void modifyCandyBonus(ServerPlayer player) {
        addCandyBonus(player);
    }
}