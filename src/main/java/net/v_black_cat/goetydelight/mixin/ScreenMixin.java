package net.v_black_cat.goetydelight.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static net.v_black_cat.goetydelight.item.MetamorphicScentGrassItem.MetamorphicScentGrassRenderItem;

@Mixin(value = Screen.class,remap = false)
public class ScreenMixin {
    @ModifyVariable(
            method = "getTooltipFromItem(Lnet/minecraft/client/Minecraft;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD"),
            name = "item",
            argsOnly = true,
            ordinal = 0
    )
    private static ItemStack modifyMainTooltipStack(ItemStack originalStack) {
        return MetamorphicScentGrassRenderItem(originalStack);
    }
}
