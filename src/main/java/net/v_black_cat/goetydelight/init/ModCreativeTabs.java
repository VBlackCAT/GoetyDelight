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
                        // 图标
                        output.accept(ModItems.GOETYDELIGHT_ICON.get());

                        // 饮品
                        output.accept(ModItems.TAINTED_DRINK.get());
                        output.accept(ModItems.PURE_DRINK.get());
                        output.accept(ModItems.SPIDER_EGG_BUBBLE_TEA.get());
                        output.accept(ModItems.BOILING_BLOOD_BREW.get());
                        output.accept(ModItems.SKULL_SHOT.get());
                        output.accept(ModItems.GRAPE_SLUSH.get());
                        output.accept(ModItems.LIQUID_VOID_TEA_DRINK.get());
                        output.accept(ModItems.OMINOUS_RAMUNE.get());
                        output.accept(ModItems.RAKI.get());
                        output.accept(ModItems.RUBY_SYRUP.get());

                        // 普通食物
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
                        output.accept(ModItems.CHERRY_BLOSSOM_CAKE.get());
                        output.accept(ModItems.NETHER_WART_OMELETTE.get());
                        output.accept(ModItems.WARPED_WART_OMELETTE.get());
                        output.accept(ModItems.FULL_SPIDER_FEAST.get());
                        output.accept(ModItems.METAMORPHIC_SCENT_FRUIT.get());
                        output.accept(ModItems.SHAWARMA.get());
                        output.accept(ModItems.MENEMEN_WITH_BREAD.get());
                        output.accept(ModItems.BAKLAVA.get());
                        output.accept(ModItems.BISCAT.get());
                        output.accept(ModItems.CAKE.get());

                        // 特殊食物（有特殊效果的）
                        output.accept(ModItems.REJECTED_DARK_MEAT_SOUP.get());
                        output.accept(ModItems.TOXIC_MEAL.get());
                        output.accept(ModItems.SAUCE_GRILLED_CANDY_FISH.get());
                        output.accept(ModItems.FRENZIED_FUNGUS_POP_ROCKS.get());
                        output.accept(ModItems.BONE_LORD_ASH_RICE.get());
                        output.accept(ModItems.CRISP_BISCUIT.get());
                        output.accept(ModItems.ROTTEN_CORPSE_MAGGOT_FEAST.get());
                        output.accept(ModItems.CORPSE_MAGGOT.get());
                        output.accept(ModItems.LICHS_CHAOS_STEW.get());
                        output.accept(ModItems.SNAP_UNHOLY_TRIPE.get());
                        output.accept(ModItems.THE_BOX_OF_THE_DEAD.get());
                        output.accept(ModItems.STUFFED_TALL_SKULL_RICE.get());
                        output.accept(ModItems.NIGHT_HEART_PEA_SOUP.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_HEAD.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_MEET.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_FLANK.get());
                        output.accept(ModItems.FORBIDDDEN_SOUP_BUN.get());
                    })
                    .withSearchBar()
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}