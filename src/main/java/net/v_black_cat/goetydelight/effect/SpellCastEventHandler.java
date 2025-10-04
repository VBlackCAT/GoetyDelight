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

        
        if (caster.level().isClientSide()) {
            return; 
        }

        ISpell spell = event.getSpell();
        MobEffectInstance effectInstance = caster.getEffect(ModEffects.SERVANT_REINFORCEMENT.get());

        if (effectInstance != null && spell instanceof SummonSpell summonSpell) {
            
            ServerLevel serverLevel = (ServerLevel) caster.level();

            summonSpell.SpellResult(serverLevel, caster,
                    event.getEntity().getMainHandItem(),
                    event.getSpell().defaultStats());

            
            caster.removeEffect(GoetyEffects.SUMMON_DOWN.get());

            
            if (caster instanceof Player player) {
                SEHelper.increaseSouls(player, spell.soulCost(caster, event.getEntity().getMainHandItem()));
                SEHelper.sendSEUpdatePacket(player);
            }
        }

    }
}