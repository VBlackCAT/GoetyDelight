package net.v_black_cat.goetydelight.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.init.CustomDollLoader;
import net.v_black_cat.goetydelight.init.CustomDollReloadListener;
import net.v_black_cat.goetydelight.item.CustomDollItem;
import org.apache.commons.lang3.StringUtils;

public class CustomDollItemRender extends BlockEntityWithoutLevelRenderer {
    public CustomDollItemRender(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        Model model = CustomDollReloadListener.DFAULT_DOLL_MODEL;
        ResourceLocation texture;

        String modelId = CustomDollItem.getModelId(itemStackIn);
        if (StringUtils.isBlank(modelId)) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStackIn.getItem());
            texture = itemId == null ? CustomDollReloadListener.DEFAULT_TEXTURE_ID : getTextureByName(itemId.getPath());
        } else {
            texture = getTextureByName(modelId);
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, buffer, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    private ResourceLocation getTextureByName(String modelId) {
        ResourceLocation texture = CustomDollLoader.getTexture(extractTextureNameFromModelId(modelId));
        return texture == null ? CustomDollReloadListener.DEFAULT_TEXTURE_ID : texture;
    }

    private String extractTextureNameFromModelId(String modelId) {
        if (modelId == null || !modelId.contains(".")) {
            return modelId;
        }
        String[] parts = modelId.split("\\.");
        return parts[parts.length - 1];
    }
}
