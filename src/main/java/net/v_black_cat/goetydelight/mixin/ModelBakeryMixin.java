package net.v_black_cat.goetydelight.mixin;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.GoetyDelight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow @Final
    private Map<ResourceLocation, UnbakedModel> unbakedCache;

    /**
     * 在 loadModel 方法中，为 doll_ 方块提供默认模型并跳过警告
     */
    @Inject(
            method = "loadModel(Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipDollBlockStateLoading(ResourceLocation location, CallbackInfo ci) {
        if (location instanceof ModelResourceLocation modelLocation) {
            String namespace = modelLocation.getNamespace();
            String variant = modelLocation.getVariant();
            String path = modelLocation.getPath();

            // 检查是否是 goetydelight 的 doll_ 方块
            if (namespace.equals(GoetyDelight.MODID)
                    && path.startsWith("doll_")
                    && !variant.isEmpty()
                    && !variant.equals("inventory")) {

                // 获取 missing model 作为占位符
                UnbakedModel missingModel = unbakedCache.get(ModelBakery.MISSING_MODEL_LOCATION);
                if (missingModel != null) {
                    // 为所有 doll_ 变体使用 missing model
                    unbakedCache.put(modelLocation, missingModel);
                }

                // 跳过原版加载逻辑
                ci.cancel();
            }
        }
    }
}