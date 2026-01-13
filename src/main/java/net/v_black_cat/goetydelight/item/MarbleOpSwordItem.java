package net.v_black_cat.goetydelight.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.gameevent.GameEvent;

public class MarbleOpSwordItem extends SwordItem {
    boolean YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS =true;
    public MarbleOpSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        DamageSource source = new DamageSource(attacker.damageSources().genericKill().typeHolder(), attacker);
        target.getCombatTracker().recordDamage(source, Float.POSITIVE_INFINITY);
        target.setHealth(0);
        target.gameEvent(GameEvent.ENTITY_DAMAGE);
        target.setLastHurtByMob(attacker);
        target.die(new DamageSource(target.damageSources().genericKill().typeHolder(), attacker));
        return YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS;
    }
}
