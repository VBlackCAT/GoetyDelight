package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static vectorwing.farmersdelight.common.registry.ModItems.COOKING_POT;

public class PotFoodItem extends Item {
    public PotFoodItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
         
        ItemStack result = super.finishUsingItem(stack, level, entity);

         
        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return result;  
            }

             
            if (result.isEmpty()) {
                return new ItemStack(COOKING_POT.get());  
            } else if (!player.getInventory().add(new ItemStack(COOKING_POT.get()))) {
                player.drop(new ItemStack(COOKING_POT.get()), false);  
            }
        }

        return result;
    }

}
