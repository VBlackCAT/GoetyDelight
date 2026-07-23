package net.v_black_cat.goetydelight.render.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 自定义人偶物品渲染器（桩代码，待完整迁移）
 */
public class CustomDollItemRender extends BlockEntityWithoutLevelRenderer {
    public CustomDollItemRender() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context,
                             com.mojang.blaze3d.vertex.PoseStack poseStack,
                             net.minecraft.client.renderer.MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
    }
}
