package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.init.ModAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpellDurationEffect extends MobEffect {

    // 为法术持续时间属性创建唯一的UUID
    private static final UUID SPELL_DURATION_UUID = UUID.fromString("8b4513a0-4e2a-11ee-be56-0242ac120002");

    public SpellDurationEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
        // 使用父类的属性修改器注册机制
        this.addAttributeModifier(
                ModAttributes.SPELL_DURATION.get(),
                SPELL_DURATION_UUID.toString(),
                10.0, // 基础值（会被等级放大）
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        // 每级效果增加10点法术持续时间
        // 公式：基础值 * (等级 + 1)
        return modifier.getAmount() * (amplifier + 1);
    }
}