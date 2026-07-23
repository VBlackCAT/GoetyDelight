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
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;

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
        // ★ 正常判断条件
        if (!FalseProverbsItem.getPlayerBackModelStatus(player.getUUID())) {
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