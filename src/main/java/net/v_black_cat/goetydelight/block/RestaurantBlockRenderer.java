package net.v_black_cat.goetydelight.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RestaurantBlockRenderer implements BlockEntityRenderer<RestaurantBlockEntity> {
    
    public RestaurantBlockRenderer(BlockEntityRendererProvider.Context context) {
        // 构造函数可以留空或用于初始化
    }
    
    @Override
    public void render(RestaurantBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        
        // 只有当 shouldRenderArea 为 true 时才渲染区域
        if (blockEntity.shouldRenderArea) {
            poseStack.pushPose();
            
            // 获取各个区域的范围
            BlockPos[] rangeMarker = blockEntity.getRangeMarker();
            BlockPos[] diningArea = blockEntity.getDiningAreaRange();
            BlockPos[] pickupArea = blockEntity.getPickupAreaRange();
            BlockPos[] entranceArea = blockEntity.getEntranceAreaRange();
            BlockPos[] exitArea = blockEntity.getExitAreaRange();
            //渲染标记区域 - 橙色
            if (rangeMarker[0] != null && rangeMarker[1] != null) {
                renderAreaBox(poseStack, bufferSource, rangeMarker[0], rangeMarker[1],
                             1.0F, 0.5F, 0.0F,
                             0.4F, blockEntity.getBlockPos());
            }

            // 渲染用餐区域 - 红色
            if (diningArea[0] != null && diningArea[1] != null) {
                renderAreaBox(poseStack, bufferSource, diningArea[0], diningArea[1], 
                             1.0F, 0.0F, 0.0F, 0.4F, blockEntity.getBlockPos());
            }
            
            // 渲染取餐区域 - 绿色
            if (pickupArea[0] != null && pickupArea[1] != null) {
                renderAreaBox(poseStack, bufferSource, pickupArea[0], pickupArea[1], 
                             0.0F, 1.0F, 0.0F, 0.4F, blockEntity.getBlockPos());
            }
            
            // 渲染入口区域 - 蓝色
            if (entranceArea[0] != null && entranceArea[1] != null) {
                renderAreaBox(poseStack, bufferSource, entranceArea[0], entranceArea[1], 
                             0.0F, 0.0F, 1.0F, 0.4F, blockEntity.getBlockPos());
            }
            
            // 渲染出口区域 - 黄色
            if (exitArea[0] != null && exitArea[1] != null) {
                renderAreaBox(poseStack, bufferSource, exitArea[0], exitArea[1], 
                             1.0F, 1.0F, 0.0F, 0.4F, blockEntity.getBlockPos());
            }
            
            poseStack.popPose();
        }
    }
    
    /**
     * 渲染区域边框盒子
     * @param poseStack PoseStack
     * @param bufferSource MultiBufferSource
     * @param startPos 起始位置
     * @param endPos 结束位置
     * @param r 红色分量
     * @param g 绿色分量
     * @param b 蓝色分量
     * @param alpha 透明度
     * @param blockPos 方块位置（用于坐标转换）
     */
    private void renderAreaBox(PoseStack poseStack, MultiBufferSource bufferSource, 
                              net.minecraft.core.BlockPos startPos, net.minecraft.core.BlockPos endPos,
                              float r, float g, float b, float alpha, net.minecraft.core.BlockPos blockPos) {
        
        // 计算相对于方块位置的相对坐标
        double minX = Math.min(startPos.getX(), endPos.getX()) - blockPos.getX();
        double minY = Math.min(startPos.getY(), endPos.getY()) - blockPos.getY();
        double minZ = Math.min(startPos.getZ(), endPos.getZ()) - blockPos.getZ();
        double maxX = Math.max(startPos.getX(), endPos.getX()) - blockPos.getX() + 1;
        double maxY = Math.max(startPos.getY(), endPos.getY()) - blockPos.getY() + 1;
        double maxZ = Math.max(startPos.getZ(), endPos.getZ()) - blockPos.getZ() + 1;
        
        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        
        // 渲染边框
        LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), 
                                   box, r, g, b, alpha);
        
        // 渲染半透明填充
        LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), 
                                   box.inflate(0.001), r, g, b, alpha * 0.2F);
    }
    
    @Override
    public boolean shouldRenderOffScreen(RestaurantBlockEntity blockEntity) {
        return true;
    }
    
    @Override
    public int getViewDistance() {
        return 64; // 设置较大的视距以便远距离也能看到区域渲染
    }
    
    @Override
    public boolean shouldRender(RestaurantBlockEntity blockEntity, Vec3 cameraPos) {
        // 基于方块位置和摄像机位置判断是否应该渲染
        return Vec3.atCenterOf(blockEntity.getBlockPos())
                .closerThan(cameraPos, this.getViewDistance());
    }
}