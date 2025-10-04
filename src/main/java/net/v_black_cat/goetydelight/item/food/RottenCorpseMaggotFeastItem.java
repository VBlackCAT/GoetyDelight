package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.events.WightSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RottenCorpseMaggotFeastItem extends Item {
    public RottenCorpseMaggotFeastItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack consumedStack = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide && livingEntity instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;

            if (serverLevel.getRandom().nextFloat() < 0.25F) {

                int sePercent = 10;
                WightSpawner.summonWight(serverLevel, serverPlayer, sePercent);

            }
        }

        return consumedStack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return (int) (32 * 2);
    }
}
