package net.v_black_cat.goetydelight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModItems;

public class FalseProverbsBackLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public FalseProverbsBackLayer(PlayerRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        // 附件由服务端同步（槽位 >= 0 表示背上背着剑）；没同步过就不画
        Integer backSlot = player.getExistingDataOrNull(ModAttachments.FALSE_PROVERBS_BACK_SLOT.get());
        if (backSlot == null || backSlot < 0) {
            return;
        }

        poseStack.pushPose();

        getParentModel().body.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.55f, 0.55f, 0.55f);
        poseStack.translate(0.0D, 0.2D, -0.2D);

        ItemStack stack = new ItemStack(ModItems.FALSE_PROVERBS.get());

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.HEAD,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                player.level(),
                player.getId()
        );

        poseStack.popPose();
    }
}