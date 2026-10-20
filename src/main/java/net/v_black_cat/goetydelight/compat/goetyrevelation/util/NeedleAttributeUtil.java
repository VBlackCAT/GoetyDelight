package net.v_black_cat.goetydelight.compat.goetyrevelation.util;

import com.mega.revelationfix.common.init.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

/** 抗性读取 + 属性 modifier 增删工具。Mixin 与水晶糖共用。 */
public final class NeedleAttributeUtil {

    // 扎针的 UUID (原版 TheNeedleItem 中的静态 UUID)
    public static final UUID NEEDLE_HP_UUID  = UUID.fromString("1b242614-c859-4088-bff7-b2809f9bbe18");
    public static final UUID NEEDLE_ATK_UUID = UUID.fromString("9f2e7534-e108-4c75-b71c-64c3b407f39b");
    public static final UUID NEEDLE_SPD_UUID = UUID.fromString("44a1da2a-8219-49cf-a523-8b710f0464a1");
    public static final UUID NEEDLE_RES_UUID = UUID.fromString("af236f3f-f82a-4008-97f9-5af0173b2ec9");

    // 水晶糖的 UUID (独立存储，清针不丢)
    public static final UUID CANDY_HP_UUID  = UUID.fromString("a1c0c0de-0001-4a00-8000-000000000001");
    public static final UUID CANDY_ATK_UUID = UUID.fromString("a1c0c0de-0002-4a00-8000-000000000002");
    public static final UUID CANDY_SPD_UUID = UUID.fromString("a1c0c0de-0003-4a00-8000-000000000003");
    public static final UUID CANDY_RES_UUID = UUID.fromString("a1c0c0de-0004-4a00-8000-000000000004");

    public static final double RES_CAP = 0.95;

    private NeedleAttributeUtil() {}

    public static double amountOf(AttributeInstance inst, UUID id) {
        if (inst == null) return 0;
        AttributeModifier m = inst.getModifier(id);
        return m == null ? 0 : m.getAmount();
    }

    public static double getTotalResistance(ServerPlayer player) {
        AttributeInstance inst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());
        if (inst == null) return 0;
        double total = 0;
        for (AttributeModifier m : inst.getModifiers()) {
            if (m.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
                    || m.getOperation() == AttributeModifier.Operation.ADDITION) {
                total += m.getAmount();
            }
        }
        return total;
    }

    public static void applyModifier(ServerPlayer player, Attribute attr, UUID uuid, String name, double amount) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst != null) {
            AttributeModifier old = inst.getModifier(uuid);
            if (old != null) inst.removeModifier(old);
            if (amount != 0) {
                inst.addPermanentModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }
}