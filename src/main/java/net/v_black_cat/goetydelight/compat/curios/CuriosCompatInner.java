package net.v_black_cat.goetydelight.compat.curios;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosCompatInner {

    @OnlyIn(Dist.CLIENT)
    static void registerRenderer(EntityRenderersEvent.AddLayers event) {
        // 1.21 使用 PlayerSkin.Model 枚举而不是字符串
        if (event.getSkin(PlayerSkin.Model.WIDE) instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new DollItemRenderer<>(playerRenderer, event.getContext().getItemInHandRenderer()));
        }
        if (event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new DollItemRenderer<>(playerRenderer, event.getContext().getItemInHandRenderer()));
        }
    }

    static void registerDollItemPredicate() {
        CuriosApi.registerCurioPredicate(
                ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "doll_item"),
                slotResult -> {
                    ItemStack item = slotResult.stack();
                    if (item.getItem() instanceof DollEntityItem) {
                        return true;
                    }
                    return item.is(ModItems.DOLL_ITEM.get());
                }
        );
    }
}