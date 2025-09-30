package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;


@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpellDurationEffect extends MobEffect {


    public SpellDurationEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }



}