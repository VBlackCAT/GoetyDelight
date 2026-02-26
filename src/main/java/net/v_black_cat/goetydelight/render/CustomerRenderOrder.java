package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;

import java.util.List;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CustomerRenderOrder {

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        
        if (event.getEntity() instanceof ICustomerEntity customer) {
            List<ItemStack> order = customer.goetyDelight$getOrder();
            
            
            if (order != null && !order.isEmpty()) {
                renderOrderItems(event, order);
            }
        }
    }

    private static void renderOrderItems(RenderLivingEvent.Post<?, ?> event, List<ItemStack> order) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher renderer = mc.getEntityRenderDispatcher();
        
        if (renderer == null) return;
        
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        
        
        poseStack.pushPose();
        
        try {
            
            poseStack.translate(0.0D, event.getEntity().getBbHeight() + 0.5D, 0.0D);
            
            
            poseStack.mulPose(renderer.cameraOrientation());
            
            
            float scale = 0.5f;
            poseStack.scale(scale, scale, scale);
            
            
            int itemCount = Math.min(order.size(), 4); 
            float spacing = 0.6f; 
            float startX = -(itemCount - 1) * spacing / 2f; 
            
            for (int i = 0; i < itemCount; i++) {
                ItemStack itemStack = order.get(i);
                if (!itemStack.isEmpty()) {
                    
                    poseStack.pushPose();
                    poseStack.translate(startX + i * spacing, 0.0D, 0.0D);
                    
                    
                    mc.getItemRenderer().renderStatic(
                        itemStack,
                        ItemDisplayContext.GROUND,
                        0xF000F0, 
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        buffer,
                        mc.level,
                        0
                    );
                    
                    poseStack.popPose();
                }
            }
        } finally {
            
            poseStack.popPose();
        }
    }
}