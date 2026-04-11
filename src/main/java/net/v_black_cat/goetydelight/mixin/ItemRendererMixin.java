package net.v_black_cat.goetydelight.mixin;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static net.v_black_cat.goetydelight.item.MetamorphicScentGrassItem.MetamorphicScentGrassRenderItem;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    // 修改模型获取中的物品堆栈
    @ModifyVariable(
            method = "getModel",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1
    )
    private ItemStack modifyModelItemStack(ItemStack original) {
        return MetamorphicScentGrassRenderItem(original);
    }

    @ModifyVariable(
            method = "render",
            at = @At("HEAD"),
            index = 6,
            argsOnly = true
    )
    private int makeFullBright(int value, ItemStack stack) {
        return stack.is(ModItems.FALSE_PROVERBS.get()) ? LightTexture.FULL_BRIGHT : value;
    }


}