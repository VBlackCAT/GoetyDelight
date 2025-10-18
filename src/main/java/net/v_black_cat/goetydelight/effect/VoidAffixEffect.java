package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VoidAffixEffect extends MobEffect {
    public VoidAffixEffect() { super(MobEffectCategory.HARMFUL, 0x000000);}
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        Entity target = event.getTarget();
        if (target instanceof LivingEntity livingTarget) {
            if (attacker.hasEffect(ModEffects.VOID_AFFIX.get())) {
                // 获取效果等级
                int amplifier = attacker.getEffect(ModEffects.VOID_AFFIX.get()).getAmplifier();
                float damage = 1.0F * (amplifier + 1);
                livingTarget.hurt(livingTarget.damageSources().fellOutOfWorld(), damage);
            }
        }
    }
}
