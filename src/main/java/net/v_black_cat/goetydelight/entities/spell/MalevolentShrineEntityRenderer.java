package net.v_black_cat.goetydelight.entities.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 领域本体不绘制任何模型（只负责斩击逻辑）。
 */
public class MalevolentShrineEntityRenderer extends EntityRenderer<MalevolentShrineEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/misc/white.png");

    public MalevolentShrineEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MalevolentShrineEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(MalevolentShrineEntity entity) {
        return TEXTURE;
    }
}