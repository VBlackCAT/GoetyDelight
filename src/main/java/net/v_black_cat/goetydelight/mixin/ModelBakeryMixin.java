package net.v_black_cat.goetydelight.mixin;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.GoetyDelight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(
            method = "loadModel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipDollBlockStateLoading(ResourceLocation location, CallbackInfo ci) {
        if (location instanceof ModelResourceLocation) {
            String path = location.getPath();
            String namespace = location.getNamespace();
            if (namespace.equals(GoetyDelight.MODID) && path.startsWith("doll_")) {
                ci.cancel();
            }
        }
    }
}