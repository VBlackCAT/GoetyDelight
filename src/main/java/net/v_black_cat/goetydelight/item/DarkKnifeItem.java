package net.v_black_cat.goetydelight.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import vectorwing.farmersdelight.common.item.KnifeItem;

public class DarkKnifeItem extends KnifeItem {
    public DarkKnifeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        float baseDamage = this.getAttackDamage();
        float boostedDamage = baseDamage * 1.5f;
        target.hurt(target.damageSources().mobAttack(attacker), boostedDamage);
        return super.hurtEnemy(stack, target, attacker);
    }
}