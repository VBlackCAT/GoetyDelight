package net.v_black_cat.goetydelight.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.v_black_cat.goetydelight.item.MetamorphicScentGrassItem.MetamorphicScentGrassRenderItem;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    // 修改渲染方法中的物品堆栈
    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "HEAD"
            ),
            name = "itemStack",
            remap = false
    )
    private ItemStack modifyRenderedItemStack(ItemStack original) {
        return MetamorphicScentGrassRenderItem(original);
    }

    // 修改模型获取中的物品堆栈
    @ModifyVariable(
            method = "getModel",
            at = @At(
                    value = "HEAD"
            ),
            name = "stack",
            remap = false
    )
    private ItemStack modifyModelItemStack(ItemStack original) {
        return MetamorphicScentGrassRenderItem(original);
    }

    // 修改静态渲染方法中的物品堆栈
    @ModifyVariable(
            method = "renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V",
            at = @At(
                    value = "HEAD"
            ),
            name = "stack",
            remap = false
    )
    private ItemStack modifyStaticRenderItemStack(ItemStack original) {
        return MetamorphicScentGrassRenderItem(original);
    }

    // 修改带实体的静态渲染方法中的物品堆栈
    @ModifyVariable(
            method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At(
                    value = "HEAD"
            ),
            name = "itemStack",
            remap = false
    )
    private ItemStack modifyEntityStaticRenderItemStack(ItemStack original) {
        return MetamorphicScentGrassRenderItem(original);
    }
}