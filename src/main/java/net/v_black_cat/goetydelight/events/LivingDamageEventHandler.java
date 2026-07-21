package net.v_black_cat.goetydelight.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.v_black_cat.goetydelight.effect.TaintedDrinkEffect;
import net.v_black_cat.goetydelight.effect.TaintedPigEffect;
import net.v_black_cat.goetydelight.item.food.SundaeOfThePhilosophersPotionItem;

public class LivingDamageEventHandler {
    public static void onLivingHurtPre(LivingDamageEvent.Pre event) {
        TaintedDrinkEffect.onLivingHurt(event);

        // 贤者圣代魔法伤害抵抗
        if (event.getEntity() instanceof Player player) {
            float reduced = SundaeOfThePhilosophersPotionItem.applyMagicResistance(
                    player, event.getNewDamage(), event.getSource());
            if (reduced != event.getNewDamage()) {
                event.setNewDamage(reduced);
            }
        }
    }
    public static void onLivingHurtPost(LivingDamageEvent.Post event) {
        TaintedPigEffect.onLivingHurt(event);
    }

}
