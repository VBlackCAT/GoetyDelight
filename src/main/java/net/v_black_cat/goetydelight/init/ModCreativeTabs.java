package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoetyDelight.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("goetydelight_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.goetydelight_tab"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.GOETYDELIGHT_ICON.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // 示例物品
                        output.accept(ModItems.EXAMPLE_ITEM.get());
                        output.accept(ModItems.EXAMPLE_FOOD.get());

                        // 图标
                        output.accept(ModItems.GOETYDELIGHT_ICON.get());

                        // 饮品
                        output.accept(ModItems.TAINTED_DRINK.get());
                        output.accept(ModItems.PURE_DRINK.get());

                        // 普通食物（按声明顺序）
                        output.accept(ModItems.PROMOTION_HARD_CANDY.get());
                        output.accept(ModItems.ECTOPLASM_JELLY.get());
                        output.accept(ModItems.WHITE_SHARK_SUGAR_PACK.get());
                        output.accept(ModItems.SUNSHINE_SUGAR_BUN.get());
                        output.accept(ModItems.CANDY_FISH.get());
                        output.accept(ModItems.FROG_LEG_SANDWICH.get());
                        output.accept(ModItems.SPIDER_EGG_BUBBLE_TEA_2.get());
                        output.accept(ModItems.CRYING_SHARK_SUGAR_PACK.get());
                        output.accept(ModItems.BEAR_PAW.get());
                        output.accept(ModItems.ECTOPLASMIC_MELON.get());
                        output.accept(ModItems.BLUE_ECTOPLASMIC_SUNDAE.get());
                        output.accept(ModItems.POACHED_SPIDER_EGG.get());
                        output.accept(ModItems.GRILL_FROG_LEG.get());
                        output.accept(ModItems.SOUL_CONVERGENCE_ROOM.get());
                        output.accept(ModItems.SOUL_CONVERGENCE_ROOM_2.get());
                        output.accept(ModItems.QUICK_GROWING_SEED_POPCORN.get());
                        output.accept(ModItems.NETHER_STYLE_FRIED_EGG_SANDWICH.get());
                        output.accept(ModItems.EXOTIC_BREAKFAST.get());
                        output.accept(ModItems.ASCENSION_MOONCAKE.get());
                        output.accept(ModItems.VILLAGERS_FEAST.get());
                        output.accept(ModItems.FULL_SPIDER_FEAST.get());
                        output.accept(ModItems.METAMORPHIC_SCENT_FRUIT.get());
                        output.accept(ModItems.SHAWARMA.get());
                        output.accept(ModItems.MENEMEN_WITH_BREAD.get());
                    })
                    .withSearchBar()
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}