package net.v_black_cat.goetydelight.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.v_black_cat.goetydelight.GoetyDelight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.client.book.BookContentResourceListenerLoader;
import net.v_black_cat.goetydelight.item.ItemBlackList;

import java.util.Iterator;
import java.util.Map;

@Mixin(BookContentResourceListenerLoader.class)
public class BookContentResourceListenerLoaderMixin {

    @Inject(method = "apply", at = @At("HEAD"), remap = false)
    private void onApplyHead(Map<ResourceLocation, JsonElement> map,
                             ResourceManager manager,
                             ProfilerFiller profiler,
                             CallbackInfo ci) {

        Iterator<Map.Entry<ResourceLocation, JsonElement>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = iterator.next();
            JsonElement jsonElement = entry.getValue();

            // 检查是否是JSON对象且包含icon字段
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                // 检查icon字段是否存在
                if (jsonObject.has("icon") && jsonObject.get("icon").isJsonPrimitive()) {
                    String iconValue = jsonObject.get("icon").getAsString();
                    if (ItemBlackList.isIconBlackListed(iconValue)) {
                        // 从map中移除这个条目
                        iterator.remove();
                         GoetyDelight.LOGGER.info("Removed patchouli entry with blacklisted icon: {}", iconValue);
                    }
                }
            }
        }


    }
}