package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;
import net.v_black_cat.goetydelight.item.ModItems;

public class FalseProverbsRanderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final Item item;

    public FalseProverbsRanderLayer(PlayerRenderer renderer) {
        super(renderer);
        this.item = ModItems.FALSE_PROVERBS.get();
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
        boolean shouldRender = FalseProverbsItem.getPlayerBackModelStatus(player.getUUID());

        if (shouldRender) {
            poseStack.pushPose();

            try {
                // 绑定到身体位置而不是头部
                getParentModel().body.translateAndRotate(poseStack);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.scale(0.5f, 0.5f, 0.5f);
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

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                poseStack.popPose();
            }
        }
    }
}