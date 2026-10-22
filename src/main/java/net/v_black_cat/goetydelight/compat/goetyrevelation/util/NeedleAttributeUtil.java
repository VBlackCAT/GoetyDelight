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
     * 把糖的加成强行写上去（附带抗性封顶）
     */
    public static void addCandyBonus(ServerPlayer player) {
        if (player.server == null) return;
        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);
        UUID id = player.getUUID();
        double[] c = data.getCandyBonus(id);

        if (c[0] == 0 && c[1] == 0 && c[2] == 0 && c[3] == 0) return;

        data.isRefreshingSet.add(id); // 防叠加标记

        AttributeInstance hpInst = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance atkInst = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance spdInst = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance resInst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());

        double curHp = amountOf(hpInst, NEEDLE_HP_UUID);
        double curAtk = amountOf(atkInst, NEEDLE_ATK_UUID);
        double curSpd = amountOf(spdInst, NEEDLE_SPD_UUID);
        double curRes = amountOf(resInst, NEEDLE_RES_UUID);

        double addRes = c[3];
        double addHp = c[0];
        double addAtk = c[1];
        double addSpd = c[2];

        // 糖的抗性封顶检查
        double otherRes = getTotalResistance(player) - curRes;
        if (otherRes + curRes + addRes > RES_CAP) {
            double allowed = Math.max(0, RES_CAP - otherRes - curRes);
            double overflow = addRes - allowed;
            addRes = allowed;
            addHp += overflow / 3.0;
            addAtk += overflow / 3.0;
            addSpd += overflow / 3.0;
        }

        setModifier(player, Attributes.MAX_HEALTH,                 NEEDLE_HP_UUID,  "Needle modifier", curHp + addHp);
        setModifier(player, Attributes.ATTACK_DAMAGE,              NEEDLE_ATK_UUID, "Needle modifier", curAtk + addAtk);
        setModifier(player, Attributes.ATTACK_SPEED,               NEEDLE_SPD_UUID, "Needle modifier", curSpd + addSpd);
        setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID, "Needle modifier", curRes + addRes);

        data.isRefreshingSet.remove(id);
    }

    /**
     * 扎针后，如果原版针把总抗性搞超了 85%，就强行砍掉针的抗性。
     */
    public static void capNeedleResistance(ServerPlayer player) {
        AttributeInstance resInst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());
        if (resInst == null) return;

        double totalRes = getTotalResistance(player);
        if (totalRes > RES_CAP) {
            double overflow = totalRes - RES_CAP;
            double curRes = amountOf(resInst, NEEDLE_RES_UUID);
            double newRes = Math.max(0, curRes - overflow);

            // 把针的抗性砍到上限
            setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID, "Needle modifier", newRes);

            // 溢出部分转给三维（除以3）
            AttributeInstance hpInst = player.getAttribute(Attributes.MAX_HEALTH);
            AttributeInstance atkInst = player.getAttribute(Attributes.ATTACK_DAMAGE);
            AttributeInstance spdInst = player.getAttribute(Attributes.ATTACK_SPEED);

            setModifier(player, Attributes.MAX_HEALTH, NEEDLE_HP_UUID, "Needle modifier", amountOf(hpInst, NEEDLE_HP_UUID) + overflow / 3.0);
            setModifier(player, Attributes.ATTACK_DAMAGE, NEEDLE_ATK_UUID, "Needle modifier", amountOf(atkInst, NEEDLE_ATK_UUID) + overflow / 3.0);
            setModifier(player, Attributes.ATTACK_SPEED, NEEDLE_SPD_UUID, "Needle modifier", amountOf(spdInst, NEEDLE_SPD_UUID) + overflow / 3.0);
        }
    }
}