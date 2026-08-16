package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EnchantedGoldenAppleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.capability.FoodStateCapability;
import net.v_black_cat.goetydelight.util.FoodState;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class AncientEnchantedGoldenAppleItem extends EnchantedGoldenAppleItem {
    public AncientEnchantedGoldenAppleItem(Properties properties) {
        super(properties);
    }
    @Override
    public boolean isFoil(ItemStack pStack) {return true;}

    private static final int ANCIENT_COUNT = 60;

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof AncientEnchantedGoldenAppleItem) {
            LivingEntity entity = event.getEntity();
            FoodState state = FoodStateCapability.get(entity);
            if (state != null) {
                state.setAncientGoldenAppleCount(ANCIENT_COUNT);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        FoodState state = FoodStateCapability.get(entity);
        if (state == null) return;
        int storedAncientCount = state.getAncientGoldenAppleCount();
        if (storedAncientCount > 0) {
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * 0.8f;
            event.setAmount(reducedDamage);
            state.setAncientGoldenAppleCount(storedAncientCount - 1);
        }
    }
}
