package net.v_black_cat.goetydelight.mixin;

import com.mega.revelationfix.common.init.ModAttributes;
import com.mega.revelationfix.common.item.curios.TheNeedleItem;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.NeedleAttributeUtil;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = TheNeedleItem.class, remap = false)
public class TheNeedleItemMixin {

    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    // 直接在方法执行完毕后拦截，清点总抗性
    @Inject(
            method = "m_5551_",
            at = @At("RETURN"),
            remap = false
    )
    @Unique
    private void goetydelight$capTotalResistance(ItemStack itemStack, Level level, LivingEntity living, int left, CallbackInfo ci) {
        if (!(living instanceof ServerPlayer player)) return;

        AttributeInstance resInst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());
        if (resInst != null) {
            // 获取包含了装备、针、糖的所有抗性总和
            double currentRes = NeedleAttributeUtil.getTotalResistance(player);
            double cap = NeedleAttributeUtil.RES_CAP; // 0.95

            // 如果总抗性超标了
            if (currentRes > cap) {
                LOGGER.info("[TheNeedleMixin] 发现总抗性溢出! 当前: {}, 目标: 0.95", currentRes);

                // 找到扎针产生的抗性 Modifier
                AttributeModifier needleRes = resInst.getModifier(NeedleAttributeUtil.NEEDLE_RES_UUID);
                if (needleRes != null) {
                    double overflow = currentRes - cap; // 溢出的部分
                    double newAmount = Math.max(0, needleRes.getAmount() - overflow); // 把针的抗性减去溢出部分

                    // 把削过的抗性塞回去
                    resInst.removeModifier(needleRes);
                    if (newAmount > 0) {
                        resInst.addPermanentModifier(new AttributeModifier(
                                NeedleAttributeUtil.NEEDLE_RES_UUID,
                                "Needle modifier",
                                newAmount,
                                AttributeModifier.Operation.MULTIPLY_BASE
                        ));
                    }

                    // 溢出部分转化为生命值
                    AttributeInstance hpInst = player.getAttribute(Attributes.MAX_HEALTH);
                    if (hpInst != null) {
                        AttributeModifier oldHp = hpInst.getModifier(NeedleAttributeUtil.NEEDLE_HP_UUID);
                        double baseHp = oldHp == null ? 0 : oldHp.getAmount();
                        if (oldHp != null) hpInst.removeModifier(oldHp);

                        hpInst.addPermanentModifier(new AttributeModifier(
                                NeedleAttributeUtil.NEEDLE_HP_UUID,
                                "Needle modifier",
                                baseHp + overflow,
                                AttributeModifier.Operation.MULTIPLY_BASE
                        ));
                    }
                    LOGGER.info("[TheNeedleMixin] 已强制削减抗性，溢出 {} 转为生命值。", overflow);
                }
            }
        }
    }

    // 客户端 UI 显示保持不变
    @Inject(
            method = "appendShiftShowMore",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    @Unique
    private void goetydelight$overrideShiftShowMore(ItemStack stack, List<Component> components, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        components.clear();

        String gold = ChatFormatting.GOLD.toString();
        String lightPurple = ChatFormatting.LIGHT_PURPLE.toString();
        String reset = ChatFormatting.RESET.toString();

        double hpAmt = NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.MAX_HEALTH), NeedleAttributeUtil.NEEDLE_HP_UUID)
                + NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.MAX_HEALTH), NeedleAttributeUtil.CANDY_HP_UUID);

        double atkAmt = NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.ATTACK_DAMAGE), NeedleAttributeUtil.NEEDLE_ATK_UUID)
                + NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.ATTACK_DAMAGE), NeedleAttributeUtil.CANDY_ATK_UUID);

        double spdAmt = NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.ATTACK_SPEED), NeedleAttributeUtil.NEEDLE_SPD_UUID)
                + NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.ATTACK_SPEED), NeedleAttributeUtil.CANDY_SPD_UUID);

        double resAmt = NeedleAttributeUtil.amountOf(player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()), NeedleAttributeUtil.NEEDLE_RES_UUID)
                + NeedleAttributeUtil.amountOf(player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()), NeedleAttributeUtil.CANDY_RES_UUID);

        components.add(Component.literal(String.format("%s+%.2f%% %s%s", gold, hpAmt * 100.0F, lightPurple, "最大生命值" + reset)));
        components.add(Component.literal(String.format("%s+%.2f%% %s%s", gold, atkAmt * 100.0F, lightPurple, "攻击伤害" + reset)));
        components.add(Component.literal(String.format("%s+%.2f%% %s%s", gold, spdAmt * 100.0F, lightPurple, "攻击速度" + reset)));
        components.add(Component.literal(String.format("%s+%.2f%% %s%s", gold, resAmt * 100.0F, lightPurple, "伤害抗性" + reset)));

        ci.cancel();
    }
}