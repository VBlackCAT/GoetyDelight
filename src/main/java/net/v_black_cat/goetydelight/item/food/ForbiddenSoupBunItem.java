package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ForbiddenSoupBunItem extends Item {
    public ForbiddenSoupBunItem(Properties pProperties) {
        super(pProperties);
    }

    boolean isEaten = false;

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player) {
            ItemStack rewardItem = getRandomReward();
            if (!player.getInventory().add(rewardItem)) {
                player.drop(rewardItem, false);
            }
            isEaten = true;
            if (player.getAbilities().instabuild) {
                return result;
            }
        }
        isEaten = false;
        return result;
    }

    private ItemStack getRandomReward() {
        if (Math.random() < 0.1) {
            return new ItemStack(ModItems.FORBIDDEN_FRAGMENT.get());
        } else {
            return new ItemStack(ModItems.FORBIDDEN_PIECE.get());
        }
    }

}
