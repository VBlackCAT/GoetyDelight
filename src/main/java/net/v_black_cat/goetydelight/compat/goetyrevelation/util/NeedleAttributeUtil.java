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
     * 刷新糖的加成（包含抗性 85% 封顶）
     */
    public static void modifyCandyBonus(ServerPlayer player) {
        if (player.server == null) return;
        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);
        UUID id = player.getUUID();
        double[] c = data.getCandyBonus(id);

        // 如果糖是 0，不用折腾
        if (c[0] == 0 && c[1] == 0 && c[2] == 0 && c[3] == 0) return;

        data.isRefreshingSet.add(id); // 开启防叠加标记

        AttributeInstance hpInst = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance atkInst = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance spdInst = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance resInst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());

        double curHp = amountOf(hpInst, NEEDLE_HP_UUID);
        double curAtk = amountOf(atkInst, NEEDLE_ATK_UUID);
        double curSpd = amountOf(spdInst, NEEDLE_SPD_UUID);
        double curRes = amountOf(resInst, NEEDLE_RES_UUID);

        double finalRes = curRes + c[3];
        double finalHp = curHp + c[0];
        double finalAtk = curAtk + c[1];
        double finalSpd = curSpd + c[2];

        // 抗性 85% 封顶判断
        double totalRes = getTotalResistance(player);
        if (totalRes + c[3] > 0.85) {
            double allowed = Math.max(0, 0.85 - (totalRes - curRes));
            double overflow = c[3] - allowed;
            finalRes = curRes + allowed;
            finalHp += overflow / 3.0;
            finalAtk += overflow / 3.0;
            finalSpd += overflow / 3.0;
        }

        setModifier(player, Attributes.MAX_HEALTH,                 NEEDLE_HP_UUID,  "Needle modifier", finalHp);
        setModifier(player, Attributes.ATTACK_DAMAGE,              NEEDLE_ATK_UUID, "Needle modifier", finalAtk);
        setModifier(player, Attributes.ATTACK_SPEED,               NEEDLE_SPD_UUID, "Needle modifier", finalSpd);
        setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NEEDLE_RES_UUID, "Needle modifier", finalRes);

        data.isRefreshingSet.remove(id);
    }
}