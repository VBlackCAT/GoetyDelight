package net.v_black_cat.goetydelight.render.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.init.CustomDollReloadListener;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class DollEntityItemRender extends BlockEntityWithoutLevelRenderer {
    private final Cache<ItemStack, ItemStack> dollCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public DollEntityItemRender(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.blockEntityRenderDispatcher = dispatcher;
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        DollEntity entity = DollEntityItem.getDollEntity(world, itemStackIn);
        String dollId = entity.getCustomDollId();

        poseStack.pushPose();
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.scale(0.75f, 0.75f, 0.75f);
        }

        if (StringUtils.isNotBlank(dollId)) {
            Model model = CustomDollReloadListener.DFAULT_DOLL_MODEL;
            ResourceLocation texture = getTextureByName(dollId);

            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
            model.renderToBuffer(poseStack, buffer, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            Block displayBlock = entity.getDisplayBlockState().getBlock();
            if (displayBlock == Blocks.AIR) {
                displayBlock = ModBlocks.CUSTOM_DOLL.get();
            }

            ItemStack dollShowItem = new ItemStack(displayBlock);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderStatic(dollShowItem, transformType, combinedLight, combinedOverlay,
                    poseStack, bufferSource, world, 0);
        }

        poseStack.popPose();
    }

    private ResourceLocation getTextureByName(String modelId) {
        String textureName = extractTextureNameFromModelId(modelId);
        return new ResourceLocation("goetydelight", "textures/block/doll/" + textureName + ".png");
    }

    private String extractTextureNameFromModelId(String modelId) {
        if (modelId == null || !modelId.contains(".")) {
            return modelId;
        }
        String[] parts = modelId.split("\\.");
        return parts[parts.length - 1];
    }
}
