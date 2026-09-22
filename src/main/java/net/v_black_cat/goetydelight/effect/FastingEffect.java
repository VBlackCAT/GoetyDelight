package net.v_black_cat.goetydelight.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.QuietusMarrowItem;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FastingEffect extends MobEffect {
    public static final TagKey<Item> ALLOWED_DRINKS =
            ItemTags.create(new ResourceLocation(GoetyDelight.MODID, "allowed_drinks"));

    public FastingEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B7355);
    }

    public static boolean isAllowedDrink(ItemStack stack) {
        return stack.is(ALLOWED_DRINKS);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(this);
        if (instance != null && instance.getDuration() > 0) {
            instance.duration = -1;
        }
    }

    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();

        if (!entity.hasEffect(ModEffects.FASTING.get())) return;
        if (!stack.isEdible()) return;
        if (isAllowedDrink(stack)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();

        if (!isAllowedDrink(stack)) return;
        if (!entity.hasEffect(ModEffects.FASTING.get())) return;
        QuietusMarrowItem.removeQuietusMarrowEffects(entity);
    }
}