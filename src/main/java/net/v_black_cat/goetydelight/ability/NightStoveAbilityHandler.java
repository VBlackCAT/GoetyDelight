package net.v_black_cat.goetydelight.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class NightStoveAbilityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        // 检查实体是否有 NightStove 能力
        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.NIGHT_STOVE)) {
            // 减少25%受到的伤害
            float reducedDamage = event.getAmount() * 0.75f; // 只承受75%的伤害，即减少25%
            event.setAmount(reducedDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                (LivingEntity) event.getSource().getEntity() : null;

        // 检查攻击者是否有 NightStove 能力
        if (attacker != null && TimedAbilitySystem.hasAbility(attacker, AbilityRegistry.NIGHT_STOVE)) {
            // 增加25%造成的伤害
            float increasedDamage = event.getAmount() * 1.25f; // 增加25%伤害
            event.setAmount(increasedDamage);
        }
    }
}