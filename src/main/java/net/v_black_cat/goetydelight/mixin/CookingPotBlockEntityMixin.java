package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Mixin(value = CookingPotBlockEntity.class,remap = false)
public class CookingPotBlockEntityMixin {

    @ModifyVariable(
            method = "ejectIngredientRemainder",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1
    )
    public ItemStack ejectIngredientRemainder(ItemStack itemStack) {
        if (!(itemStack.isEmpty())){
            if (itemStack.getCount() > 1){
                return itemStack.copy().split(1);
            }
        }
        return itemStack;
    }

}
