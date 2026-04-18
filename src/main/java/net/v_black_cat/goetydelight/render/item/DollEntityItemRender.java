package net.v_black_cat.goetydelight.render.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.item.CustomDollItem;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import net.v_black_cat.goetydelight.item.ModItems;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class DollEntityItemRender extends BlockEntityWithoutLevelRenderer {
    private final Cache<ItemStack, ItemStack> dollCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    public DollEntityItemRender(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack,
                             MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        // 从物品获取玩偶实体
        ItemStack dollShowItem = dollCache.getIfPresent(itemStackIn);
        if (dollShowItem == null) {
            DollEntity entity = DollEntityItem.getDollEntity(world, itemStackIn);

            // 先检查是否是自定义玩偶
            String dollId = entity.getCustomDollId();
            if (StringUtils.isNotBlank(dollId)) {
                dollShowItem = new ItemStack(ModItems.CUSTOM_DOLL.get());
                CustomDollItem.setModelId(dollShowItem, dollId);
            } else {
                Block displayBlock = entity.getDisplayBlockState().getBlock();
                if (displayBlock == Blocks.AIR) {
                    dollShowItem = new ItemStack(ModItems.DOLL_ITEM.get());
                } else {
                    dollShowItem = new ItemStack(displayBlock);
                }
            }

            dollCache.put(itemStackIn, dollShowItem);
        }
        // 渲染物品
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        poseStack.pushPose();
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.scale(0.75f, 0.75f, 0.75f);
        }
        poseStack.translate(0.5, 0.5, 0.5);
        itemRenderer.renderStatic(dollShowItem, transformType, combinedLight, combinedOverlay, poseStack, bufferSource, world, 0);
        poseStack.popPose();
    }
}
