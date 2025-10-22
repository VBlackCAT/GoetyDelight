package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class TinglingEffect extends MobEffect {

    private static final UUID TINGLING_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    public TinglingEffect() {
        super(MobEffectCategory.NEUTRAL, 0x000000);
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                TINGLING_UUID.toString(),
                0.1,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }

    public void applyEffectTick(LivingEntity living, int amplify) {
        if (living.getHealth() > 1.0F) {
            if (living.hurt(ModDamageSource.getDamageSource(living.level(), ModDamageSource.SHOCK), 0.1F)) {
                double x = living.level().getRandom().nextDouble() * Mth.nextInt(living.level().getRandom(), -1, 1);
                double z = living.level().getRandom().nextDouble() * Mth.nextInt(living.level().getRandom(), -1, 1);
                MobUtil.push(living, x, living.level().random.nextDouble() / 2.0D, z);
            }
        }
    }

    public boolean isDurationEffectTick(int tick, int amplify) {
        int j = 40 >> amplify;
        if (j > 0) {
            return tick % j == 0;
        } else {
            return true;
        }
    }
    @Override
    public void removeAttributeModifiers(LivingEntity living, AttributeMap attributeMap, int amplifier) {
        var attackDamageAttribute = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttribute != null && attackDamageAttribute.getModifier(TINGLING_UUID) != null) {
            attackDamageAttribute.removeModifier(TINGLING_UUID);
        }
    }
}
