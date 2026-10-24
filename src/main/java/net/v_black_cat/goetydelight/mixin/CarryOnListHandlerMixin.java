package net.v_black_cat.goetydelight.mixin;

import net.v_black_cat.goetydelight.GoetyDelight;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Set;

@Pseudo
@Mixin(targets = "tschipp.carryon.common.config.ListHandler")
public class CarryOnListHandlerMixin {

    @Inject(method = "initConfigLists", at = @At("TAIL"), remap = false)
    private static void goetydelight$addCustomBlacklists(CallbackInfo ci) {
        // 防呆设计：确保模组存在
        if (!ModList.get().isLoaded("carryon")) {
            return;
        }

        try {
            // 反射获取 FORBIDDEN_TILES (方块黑名单)
            Field forbiddenTilesField = Class.forName("tschipp.carryon.common.config.ListHandler")
                    .getDeclaredField("FORBIDDEN_TILES");
            forbiddenTilesField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Set<String> forbiddenTiles = (Set<String>) forbiddenTilesField.get(null);

            // 添加你的方块
            forbiddenTiles.add("goetydelight:apollyon_cake");

            // 如果需要加实体黑名单，顺便加上：
            // Field forbiddenEntitiesField = ...getDeclaredField("FORBIDDEN_ENTITIES");
            // Set<String> forbiddenEntities = (Set<String>) forbiddenEntitiesField.get(null);
            // forbiddenEntities.add("goetydelight:some_entity");

            GoetyDelight.LOGGER.debug("Successfully injected goetydelight:apollyon_cake into CarryOn FORBIDDEN_TILES");

        } catch (Exception e) {
            GoetyDelight.LOGGER.error("Failed to inject into CarryOn blacklist: {}", e.getMessage());
        }
    }
}