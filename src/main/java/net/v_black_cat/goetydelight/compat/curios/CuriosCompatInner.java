package net.v_black_cat.goetydelight.compat.curios;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import net.v_black_cat.goetydelight.item.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CuriosCompatInner {
    @OnlyIn(Dist.CLIENT)
    static void registerRenderer(EntityRenderersEvent.AddLayers event) {
        if (event.getSkin("default") instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new DollItemRenderer<>(playerRenderer, event.getContext().getItemInHandRenderer()));
        }
        if (event.getSkin("slim") instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new DollItemRenderer<>(playerRenderer, event.getContext().getItemInHandRenderer()));
        }
    }

    static void registerDollItemPredicate() {
        CuriosApi.registerCurioPredicate(new ResourceLocation(GoetyDelight.MODID, "doll_item"), slotResult -> {
            ItemStack item = slotResult.stack();
            if (item.getItem() instanceof DollEntityItem) {
                return true;
            }
            return item.is(ModItems.DOLL_ITEM.get());
        });
    }
}
