package net.v_black_cat.goetydelight.mixin;

import com.mega.revelationfix.common.init.ModAttributes;
import com.mega.revelationfix.common.item.curios.TheNeedleItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.CrystalCandySavedData;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.NeedleAttributeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TheNeedleItem.class, remap = false)
public class TheNeedleItemMixin {

    @Unique
    private static boolean goetydelight$hadModifierBefore = false;

    @Inject(method = "releaseUsing", at = @At("HEAD"), remap = false)
    private void goetydelight$checkBeforeNeedle(ItemStack stack, Level level, LivingEntity living, int left, CallbackInfo ci) {
        if (living instanceof ServerPlayer player) {
            goetydelight$hadModifierBefore = player.getAttribute(Attributes.MAX_HEALTH).getModifier(NeedleAttributeUtil.NEEDLE_HP_UUID) != null;
        }
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"), remap = false)
    private void goetydelight$reapplyCandyAfterNeedle(ItemStack itemStack, Level level, LivingEntity living, int left, CallbackInfo ci) {
        if (!(living instanceof ServerPlayer player)) return;

        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);
        if (data.isRefreshingSet.contains(player.getUUID())) return;

        double[] c = data.getCandyBonus(player.getUUID());

        // 1. 剥离糖（如果原版针吃到了糖，必须剥出来，否则下面加糖会翻倍）
        if (goetydelight$hadModifierBefore) {
            double curHp = NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.MAX_HEALTH), NeedleAttributeUtil.NEEDLE_HP_UUID);
            double curAtk = NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.ATTACK_DAMAGE), NeedleAttributeUtil.NEEDLE_ATK_UUID);
            double curSpd = NeedleAttributeUtil.amountOf(player.getAttribute(Attributes.ATTACK_SPEED), NeedleAttributeUtil.NEEDLE_SPD_UUID);
            double curRes = NeedleAttributeUtil.amountOf(player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()), NeedleAttributeUtil.NEEDLE_RES_UUID);

            NeedleAttributeUtil.setModifier(player, Attributes.MAX_HEALTH, NeedleAttributeUtil.NEEDLE_HP_UUID, "Needle modifier", curHp - c[0]);
            NeedleAttributeUtil.setModifier(player, Attributes.ATTACK_DAMAGE, NeedleAttributeUtil.NEEDLE_ATK_UUID, "Needle modifier", curAtk - c[1]);
            NeedleAttributeUtil.setModifier(player, Attributes.ATTACK_SPEED, NeedleAttributeUtil.NEEDLE_SPD_UUID, "Needle modifier", curSpd - c[2]);
            NeedleAttributeUtil.setModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NeedleAttributeUtil.NEEDLE_RES_UUID, "Needle modifier", curRes - c[3]);
        }

        // 2. 砍掉原版针超标的抗性（如果超了，强行砍回 85%，溢出转三维）
        NeedleAttributeUtil.capNeedleResistance(player);

        // 3. 最后再把糖加回去（此步骤内部包含糖的封顶）
        NeedleAttributeUtil.addCandyBonus(player);
    }
}