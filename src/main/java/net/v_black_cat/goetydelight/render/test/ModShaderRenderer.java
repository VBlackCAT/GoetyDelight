package net.v_black_cat.goetydelight.render.test;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
public class ModShaderRenderer {

    private static float time = 0;

    @SubscribeEvent
    public static void onRenderLivingEvent(RenderLivingEvent event) {
        if(false){
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;
            time = (player.level().getGameTime() + mc.getFrameTime()) / 10.0f;
            // 使用新着色器
            ModShaderInstance shader = (ModShaderInstance) ModShaderReg.getFloridShader();
            shader.setTime(time);

            time += 0.05f;
            if (time > 100) time = 0;

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(0, 2, 3); // 调整渲染位置

            RenderSystem.setShader(() -> shader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            float size = 2f;
            buffer.vertex(poseStack.last().pose(), -size, -size, 0).uv(0, 0).endVertex();
            buffer.vertex(poseStack.last().pose(), -size, size, 0).uv(0, 1).endVertex();
            buffer.vertex(poseStack.last().pose(), size, size, 0).uv(1, 1).endVertex();
            buffer.vertex(poseStack.last().pose(), size, -size, 0).uv(1, 0).endVertex();

            tesselator.end();
            poseStack.popPose();
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
        }

    }
}