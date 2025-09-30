package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.events.spell.CastMagicEvent;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpellCastEventHandler {

    @SubscribeEvent
    public static void onCastMagic(CastMagicEvent event) {

        LivingEntity caster = event.getEntity();
        ISpell spell = event.getSpell();

        MobEffectInstance effectInstance = caster.getEffect(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT.get()); // 请根据实际注册类调整
        if (effectInstance != null) {
            if (spell instanceof SummonSpell summonSpell) {

                summonSpell.SpellResult((ServerLevel) caster.level(), caster, event.getEntity().getMainHandItem(), event.getSpell().defaultStats());
                caster.getEffect(GoetyEffects.SUMMON_DOWN.get());
                caster.removeEffect(GoetyEffects.SUMMON_DOWN.get());
                SEHelper.increaseSouls((Player) caster, spell.soulCost(caster, event.getEntity().getMainHandItem()));
                SEHelper.sendSEUpdatePacket((Player) caster);
            }


        }





    }
}