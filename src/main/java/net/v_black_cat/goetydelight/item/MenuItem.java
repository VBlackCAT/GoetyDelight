package net.v_black_cat.goetydelight.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;

public class MenuItem extends Item {
    public MenuItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {


        InteractionResult interactionResult = super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        if (interactionTarget instanceof ICustomerEntity customer){
            if (!(customer.goetyDelight$isCustomerMode())&&customer.goetyDelight$canEnterCustomerMode()){
                double v = player.getRandom().nextDouble();
                if (player.isCreative()) {
                    customer.goetyDelight$setCustomerMode( true);
                    interactionResult = InteractionResult.SUCCESS;
                } else if (v < 0.5) {
                    customer.goetyDelight$enterCustomerModeAndCheckCoolDown();
                    stack.shrink(1);
                    interactionResult = InteractionResult.SUCCESS;
                } else {
                    customer.goetyDelight$setEnterCustomerModeCooldown(10 * 20);
                }
            }

        }



        return interactionResult;
    }
}
