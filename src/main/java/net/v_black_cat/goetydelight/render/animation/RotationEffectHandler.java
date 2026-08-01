package net.v_black_cat.goetydelight.render.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.util.BuffUtil;
import org.joml.Quaternionf;

/**
 * 1.21.1 移植版：拥有糖权杖免疫 Buff 的实体，头顶环绕 4 颗白鲨糖果（对应 1.20.1 RotationEffectHandler）。
 * Buff 状态通过 ModAttachments.ACTIVE_BUFFS 的附件同步机制从服务端同步到客户端。
 */
@EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
public class RotationEffectHandler {
    private static float rotationAngle = 0;
    private static final int ITEM_COUNT = 4;
    private static final double RADIUS = 0.8;
    private static final double HEIGHT_OFFSET = 0.3;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        rotationAngle += 1f;
        if (rotationAngle >= 360) rotationAngle = 0;
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean hasImmunity = BuffUtil.hasBuff(event.getEntity(), ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId());
        if (!hasImmunity) return;

        float partialTicks = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(0, event.getEntity().getBbHeight() / 2, 0);

        Quaternionf rotation = Axis.YP.rotationDegrees(rotationAngle);
        poseStack.mulPose(rotation);

        for (int i = 0; i < ITEM_COUNT; i++) {
            poseStack.pushPose();

            double angle = i * (360.0 / ITEM_COUNT);
            double x = RADIUS * Math.cos(Math.toRadians(angle));
            double z = RADIUS * Math.sin(Math.toRadians(angle));

            poseStack.translate(x, HEIGHT_OFFSET, z);
            poseStack.scale(1f, 1f, 1f);

            Quaternionf itemRotation = Axis.YP.rotationDegrees(-rotationAngle * 8);
            poseStack.mulPose(itemRotation);

            ItemStack itemStack = new ItemStack(ModItems.WHITE_SHARK_CANDY.get());
            renderItem(itemStack, poseStack, mc, partialTicks);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderItem(ItemStack stack, PoseStack poseStack, Minecraft mc, float partialTicks) {
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, null, null, 0);
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        itemRenderer.render(
                stack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                buffer,
                15728880,
                OverlayTexture.NO_OVERLAY,
                model
        );

        buffer.endBatch();
    }
}
