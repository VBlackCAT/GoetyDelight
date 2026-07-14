package net.v_black_cat.goetydelight.events;


import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.v_black_cat.goetydelight.effect.TaintedDrinkEffect;
import net.v_black_cat.goetydelight.effect.VoidAffixEffect;

public class AttackEntityEventHandler {
    public static void onAttackEntity(AttackEntityEvent event) {
        TaintedDrinkEffect.onAttackEntity(event);
        VoidAffixEffect.onAttackEntity(event);
    }

}
