package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.events.spell.CastMagicEvent;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModEffects;

public class NightHeartPeaSoupEffect extends MobEffect {
    public NightHeartPeaSoupEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x98D982);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide) {
            RandomSource random = level.random;
            for (int i = 0; i < 2; i++) {
                double x = entity.getX() + (random.nextDouble() - 0.5) * 2.0;
                double y = entity.getY() + random.nextDouble() * 1.5;
                double z = entity.getZ() + (random.nextDouble() - 0.5) * 2.0;
                level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0, 0.1, 0);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    public static void onCastMagic(CastMagicEvent event) {
        LivingEntity caster = event.getEntity();
        if (caster.level().isClientSide()) return;

        ISpell spell = event.getSpell();
        MobEffectInstance effectInstance = caster.getEffect(ModEffects.SERVANT_REINFORCEMENT);

        if (effectInstance != null && spell instanceof SummonSpell summonSpell) {
            ServerLevel serverLevel = (ServerLevel) caster.level();
            summonSpell.SpellResult(serverLevel, caster,
                    event.getEntity().getMainHandItem(),
                    event.getSpell().defaultStats());

            caster.removeEffect(GoetyEffects.SUMMON_DOWN);

            if (caster instanceof Player player) {
                SEHelper.increaseSouls(player, spell.soulCost(caster, event.getEntity().getMainHandItem()));
                SEHelper.sendSEUpdatePacket(player);
            }
        }
    }
}