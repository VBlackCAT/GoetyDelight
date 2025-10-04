package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.magic.SummonSpell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

import static net.v_black_cat.goetydelight.item.food.NightHeartPeaSoupItem.getSoupBoostCount;

@Mixin(SummonSpell.class)
public abstract class SummonSpellMixin {

    @Redirect(
            method = "conditionsMet",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/magic/SummonSpell;summonLimit()I"
            ),
            remap = false
    )
    private int modifySummonLimit(SummonSpell instance, ServerLevel worldIn, LivingEntity caster) {
        int originalLimit = instance.summonLimit();
        int boostCount = getSoupBoostCount((Player) caster);

        return boostCount>0
                ? originalLimit * 2
                : originalLimit;
    }
}