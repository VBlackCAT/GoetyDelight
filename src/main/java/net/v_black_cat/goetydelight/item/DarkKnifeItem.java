package net.v_black_cat.goetydelight.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Tier;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vectorwing.farmersdelight.common.item.KnifeItem;

@Mod.EventBusSubscriber
public class DarkKnifeItem extends KnifeItem {
    public DarkKnifeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getMainHandItem().getItem() instanceof DarkKnifeItem) {
            event.setAmount(event.getAmount() * 1.5f);
        }
    }
}