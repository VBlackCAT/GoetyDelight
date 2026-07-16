package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class AncientEnchantedGoldenAppleItem extends Item {



    public AncientEnchantedGoldenAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            BuffUtil.applyBuff(entity, ModBuffTypes.ANCIENT_ENCHANTED_GOLDEN_APPLE,-1,30);
        }
        return result;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();

        if (BuffUtil.hasBuff(entity,ModBuffTypes.ANCIENT_ENCHANTED_GOLDEN_APPLE) ) {
            int totalAmplifier = BuffUtil.getTotalAmplifier(entity, ModBuffTypes.ANCIENT_ENCHANTED_GOLDEN_APPLE);
            if (totalAmplifier > 0){
                float originalDamage = event.getOriginalDamage();
                float reducedDamage = originalDamage * 0.8f;
                event.setNewDamage(reducedDamage);
                BuffUtil.applyBuff(entity, ModBuffTypes.ANCIENT_ENCHANTED_GOLDEN_APPLE,-1,totalAmplifier-1);
            }

        }
    }
}