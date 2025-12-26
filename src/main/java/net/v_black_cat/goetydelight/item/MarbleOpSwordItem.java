package net.v_black_cat.goetydelight.item;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.v_black_cat.goetydelight.mixin.LivingEntityAccessor;

import static net.minecraft.world.damagesource.DamageTypes.PLAYER_EXPLOSION;

public class MarbleOpSwordItem extends SwordItem {
    boolean YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS =true;
    public MarbleOpSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 正确的Accessor使用方式
        ((LivingEntityAccessor) target).setDead(true);
        target.deathTime = 0;
        target.setHealth(0);
        target.die(new DamageSource((attacker.damageSources().explosion( attacker,  attacker)).typeHolder(), attacker));
        return YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS;
    }
}
