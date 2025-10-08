package net.v_black_cat.goetydelight.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.SmokingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

@OnlyIn(Dist.CLIENT)
public class ShadeStoveScreen extends AbstractFurnaceScreen<ShadeStoveMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/smoker.png");
//    private static final ResourceLocation ANIMATION_TEXTURE = new ResourceLocation(MODID, "textures/item/apocalyptium_knife.png");

//    private int animationTick = 0;
//    // 动画帧数
//    private static final int FRAME_COUNT = 8;
//    // 每帧持续时间 (ticks)
//    private static final int FRAME_DURATION = 5;
//    // 每帧尺寸
//    private static final int FRAME_WIDTH = 16;
//    private static final int FRAME_HEIGHT = 16;
    public ShadeStoveScreen(ShadeStoveMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, new SmokingRecipeBookComponent(), pPlayerInventory, pTitle, TEXTURE);
    }

//    @Override
//    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
//        // 渲染基础的熔炉UI
//        int i = this.leftPos;
//        int j = this.topPos;
//        pGuiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
//
//        // 渲染燃烧进度条（如果适用）
//        if (this.menu.isLit()) {
//            int k = this.menu.getLitProgress();
//            pGuiGraphics.blit(TEXTURE, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
//        }
//
//        // 渲染烹饪进度条
//        int l = this.menu.getBurnProgress();
//        pGuiGraphics.blit(TEXTURE, i + 79, j + 34, 176, 14, l + 1, 16);
//
//        // 更新动画计时器
//        animationTick = (animationTick + 1) % (FRAME_COUNT * FRAME_DURATION);
//
//        // 计算当前帧
//
//        // 绘制动画
//        int x = i + 80; // 动画位置X (根据UI布局调整)
//        int y = j + 35; // 动画位置Y (根据UI布局调整)
//
//        // 使用裁剪方法绘制当前帧
//        pGuiGraphics.blit(ANIMATION_TEXTURE,
//                x, y, // 目标位置
//                0,animationTick * FRAME_WIDTH, // 源坐标: 根据当前帧偏移X坐标，Y坐标为0
//                FRAME_WIDTH, FRAME_HEIGHT, // 源尺寸: 裁剪的宽度和高度
//                16, 96); // 纹理总尺寸: 宽度为总帧数*每帧宽度，高度为帧高度
//    }

}