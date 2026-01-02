package net.v_black_cat.goetydelight.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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


}