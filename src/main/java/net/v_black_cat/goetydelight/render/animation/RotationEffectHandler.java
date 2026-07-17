package net.v_black_cat.goetydelight.render.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;
import org.joml.Quaternionf;

public class RotationEffectHandler {
    private static float rotationAngle = 0;
    private static final int ITEM_COUNT = 4;
    private static final double RADIUS = 0.8;
    private static final double HEIGHT_OFFSET = 0.3;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == Phase.START) {
            rotationAngle += 1f;
            if (rotationAngle >= 360) rotationAngle = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderLivingEvent(RenderLivingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // 检查被渲染的实体是否有免疫能力
//        boolean hasImmunity = TimedAbilitySystem.hasAbility(
//                event.getEntity(),
//                AbilityRegistry.SUGAR_SCEPTER_IMMUNITY
//        );


        boolean hasImmunity;
        // 客户端直接通过 Capability 检查（由网络同步包维护）
        hasImmunity = BuffUtil.hasBuff(event.getEntity(), ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId());

//        hasImmunity =true;
        // 只对拥有免疫能力的实体显示特效
        if (!hasImmunity) return;

        float partialTicks = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();

        // 将特效定位到实体上方
        poseStack.translate(0, event.getEntity().getBbHeight()/2, 0);

        Quaternionf rotation = Axis.YP.rotationDegrees(rotationAngle);
        poseStack.mulPose(rotation);

        for (int i = 0; i < ITEM_COUNT; i++) {
            poseStack.pushPose();

            double angle = i * (360.0 / ITEM_COUNT);
            double x = RADIUS * Math.cos(Math.toRadians(angle));
            double z = RADIUS * Math.sin(Math.toRadians(angle));

            poseStack.translate(x, HEIGHT_OFFSET, z);

            poseStack.scale(1f, 1f, 1f); // 缩小物品尺寸

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