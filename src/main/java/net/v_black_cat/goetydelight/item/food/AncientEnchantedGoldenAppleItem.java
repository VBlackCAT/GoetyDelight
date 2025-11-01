package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EnchantedGoldenAppleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class AncientEnchantedGoldenAppleItem extends EnchantedGoldenAppleItem {
    public AncientEnchantedGoldenAppleItem(Properties properties) {
        super(properties);
    }
    @Override
    public boolean isFoil(ItemStack pStack) {return true;}

    private static final String ANCIENT_ENCHANTED_GOLDEN_APPLE_TAG = "AncientEnchantedGoldenAppleActive";
    private static int ancientcount = 60;
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof AncientEnchantedGoldenAppleItem) {
            ancientcount = 60;
            LivingEntity entity = event.getEntity();
            CompoundTag tag = entity.getPersistentData();
            tag.putInt(ANCIENT_ENCHANTED_GOLDEN_APPLE_TAG, ancientcount);
        }
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        int storedAncientCount = tag.getInt(ANCIENT_ENCHANTED_GOLDEN_APPLE_TAG);
        if (storedAncientCount > 0) {
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * 0.8f;
            event.setAmount(reducedDamage);
            tag.putInt(ANCIENT_ENCHANTED_GOLDEN_APPLE_TAG, storedAncientCount - 1);
        }
    }
}
