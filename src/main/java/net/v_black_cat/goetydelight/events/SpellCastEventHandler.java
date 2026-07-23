package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.events.spell.CastMagicEvent;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModEffects;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class SpellCastEventHandler {

    @SubscribeEvent
    public static void onCastMagic(CastMagicEvent event) {
        LivingEntity caster = event.getEntity();
        if (caster.level().isClientSide()) {
            return;
        }

        ISpell spell = event.getSpell();

        MobEffectInstance effectInstance = caster.getEffect(ModEffects.SERVANT_REINFORCEMENT);

        if (effectInstance != null && spell instanceof SummonSpell summonSpell) {
            ServerLevel serverLevel = (ServerLevel) caster.level();

            summonSpell.SpellResult(serverLevel, caster,
                    event.getEntity().getMainHandItem(),
                    event.getSpell().defaultStats());

            caster.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(GoetyEffects.SUMMON_DOWN.get()));

            if (caster instanceof Player player) {
                SEHelper.increaseSouls(player, spell.soulCost(caster, event.getEntity().getMainHandItem()));
                SEHelper.sendSEUpdatePacket(player);
            }
        }
    }
}
