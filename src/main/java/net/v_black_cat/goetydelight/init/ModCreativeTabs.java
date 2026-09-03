package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.CustomDollItem;
import net.v_black_cat.goetydelight.init.ModBlocks;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoetyDelight.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("goetydelight_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.goetydelight_tab"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
.icon(() -> new ItemStack(ModBlocks.NIGHT_STOVE.get()))
                    .displayItems((parameters, output) -> {
                        // ==================== 物品 ====================
                        // 武器
                        output.accept(ModItems.CURSED_INGOT_KNIFE.get());
                        output.accept(ModItems.DARK_KNIFE.get());
//                        output.accept(ModItems.APOCALYPTIUM_KNIFE.get());
//                        output.accept(ModItems.VENOMOUS_SPIDER_KNIFE.get());
//                        output.accept(ModItems.SPECTRE_KNIFE.get());
//                        output.accept(ModItems.MARBLE_OP_SWORD.get());
                        output.accept(ModItems.FALSE_PROVERBS.get());
                        output.accept(ModItems.CURSED_METAL_BRUSH.get());
                        output.accept(ModItems.DARK_BRUSH.get());
//                        output.accept(ModItems.APOCALYPTIUM_INGOT_BRUSH.get());

                        // 图标
                      //  output.accept(ModItems.GOETYDELIGHT_ICON.get());

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
//                        output.accept(ModItems.PROMOTION_HARD_CANDY.get());
                        output.accept(ModItems.PARASITIZED_WARDEN.get());
                        output.accept(ModItems.ECTOPLASM_JELLY.get());
                        output.accept(ModItems.WHITE_SHARK_SUGAR_PACK.get());
                        output.accept(ModItems.SUNSHINE_SUGAR_BUN.get());
                        output.accept(ModItems.CANDY_FISH.get());
                        output.accept(ModItems.FROG_LEG_SANDWICH.get());
                        output.accept(ModItems.CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE.get());
//                        output.accept(ModItems.SPIDER_EGG_BUBBLE_TEA_2.get());
                        output.accept(ModItems.CRYING_SHARK_SUGAR_PACK.get());
                        output.accept(ModItems.SIBLING_SUNDAE.get());
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
//                        output.accept(ModItems.ASCENSION_MOONCAKE.get());
                        output.accept(ModItems.VILLAGERS_FEAST.get());
                        output.accept(ModItems.NETHER_WART_OMELETTE.get());
                        output.accept(ModItems.WARPED_WART_OMELETTE.get());
                        output.accept(ModItems.FULL_SPIDER_FEAST.get());
//                        output.accept(ModItems.METAMORPHIC_SCENT_FRUIT.get());
                        output.accept(ModItems.METAMORPHIC_SCENT_GRASS.get());
                        output.accept(ModItems.SHAWARMA.get());
                        output.accept(ModItems.MENEMEN_WITH_BREAD.get());
                        output.accept(ModItems.BAKLAVA.get());
                        output.accept(ModItems.BISCAT.get());
                        output.accept(ModItems.CAKE.get());
                        output.accept(ModItems.JUNGLE_SALAD.get());

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
                        output.accept(ModItems.UNHOLY_SAUCE.get());
                        output.accept(ModItems.THE_BOX_OF_THE_DEAD.get());
                        output.accept(ModItems.STUFFED_TALL_SKULL_RICE.get());
                        output.accept(ModItems.NIGHT_HEART_PEA_SOUP.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_HEAD.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_HAND.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_BODY.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_LEG.get());
                        output.accept(ModItems.BOAT_STUFFED_ROASTED_WARDEN_SOUP.get());
                        output.accept(ModItems.FORBIDDDEN_SOUP_BUN.get());
                        output.accept(ModItems.CUP.get());
                        output.accept(ModItems.OMINOUS_ICE_CREAM.get());
                        output.accept(ModItems.MAGIC_QUARTZ_COOKIE.get());
                        output.accept(ModItems.ROASTED_CORPSE_MAGGOTS.get());
//                        output.accept(ModItems.ROAST_LAOWANG.get());
                        output.accept(ModItems.ANCIENT_ENCHANTED_GOLDEN_APPLE.get());
                        output.accept(ModItems.HIDDEN_PANCAKE.get());
                        output.accept(ModItems.POLARICE.get());
                        output.accept(ModItems.TEN_THOUSAND_POISON_FEAST.get());

                        // 盛宴食物（烤肉相关）
                        output.accept(ModItems.RING_PACKED_VOID_GEL_JELLY.get());
                        output.accept(ModItems.ROAST_SPIDER_EGG.get());
                        output.accept(ModItems.ROAST_LAOWANG_EAR.get());
                        output.accept(ModItems.ROAST_LAOWANG_FEET.get());
                        output.accept(ModItems.ROAST_LAOWANG_HEAD.get());
                        output.accept(ModItems.ROAST_LAOWANG_LEG.get());
                        output.accept(ModItems.ONION_PORK_CHOP_RICE.get());
                        output.accept(ModItems.ECTOPLASMIC_MELON_SALAD.get());
                        //刷怪蛋
                        output.accept(ModItems.GHOST_FARMER_SPAWN_EGG.get());
                        //卷轴
                        output.accept(ModItems.VIZIERS_COOKBOOK.get());
                        // 种子
                        output.accept(ModItems.ECTOPLASMIC_MELON_SEEDS.get());
                        output.accept(ModItems.METAMORPHIC_SCENT_GRASS_SEEDS.get());

                        // 聚晶（法杖 Focus）
                        output.accept(ModItems.GRASS_CUTTING_FOCUS.get());
                        output.accept(ModItems.HOE_FOCUS.get());
                        output.accept(ModItems.MARBLE_FOCUS.get());

                        // 人偶
                        CustomDollItem.addCreativeTab(output);

                        // ==================== 方块 ====================
                        // 建筑方块 - 大理石系列
                     /*   output.accept(ModBlocks.MARBLE.get());
                        output.accept(ModBlocks.SILT_MARBLE_HEAVY.get());
                        output.accept(ModBlocks.BLUE_MARBLE.get());
                        output.accept(ModBlocks.JUNGLE_MARBLE.get());
                        output.accept(ModBlocks.NETHER_MARBLE.get());
                        output.accept(ModBlocks.DRIPMARBLE_BLOCK.get());
                        output.accept(ModBlocks.POINTED_DRIPMARBLE.get());
                        output.accept(ModBlocks.MARBLE_STAIRS.get());
                        output.accept(ModBlocks.MARBLE_SLAB.get());
                        output.accept(ModBlocks.MARBLE_BUTTON.get());
                        output.accept(ModBlocks.MARBLE_PRESSURE_PLATE.get());
                        output.accept(ModBlocks.MARBLE_FENCE.get());
                        output.accept(ModBlocks.MARBLE_FENCE_GATE.get());
                        output.accept(ModBlocks.MARBLE_WALL.get());
                        output.accept(ModBlocks.MARBLE_DOOR.get());
                        output.accept(ModBlocks.MARBLE_TRAPDOOR.get());    */

                        // 功能方块 - 炉灶与设备
                        output.accept(ModBlocks.NIGHT_STOVE.get());
                        output.accept(ModBlocks.SHADE_STOVE.get());
                        output.accept(ModBlocks.CURSED_INGOT_POT.get());
                     //   output.accept(ModBlocks.APOCALYPTIUM_POT.get());

                        // 作物
                        output.accept(ModBlocks.ECTOPLASMIC_MELON_BLOCK.get());

                        // 食物方块 - Feast 类
                        output.accept(ModBlocks.ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK.get());
                        output.accept(ModBlocks.VOID_GEL_JELLY_BLOCK.get());
                        output.accept(ModBlocks.STUFFED_TALL_SKULL_RICE_BLOCK.get());
                        output.accept(ModBlocks.LICHS_CHAOS_STEW_BLOCK.get());
                        output.accept(ModBlocks.NIGHT_HEART_PEA_SOUP_BLOCK.get());
                        output.accept(ModBlocks.BONE_LORD_ASH_RICE_BLOCK.get());
                        output.accept(ModBlocks.MENEMEN_BLOCK.get());
                        output.accept(ModBlocks.BOAT_STUFFED_ROASTED_WARDEN_BlOCK.get());
                        output.accept(ModBlocks.SNAP_UNHOLY_TRIPE_BLOCK.get());
                        output.accept(ModBlocks.ROAST_LAOWANG_BLOCK.get());
                        output.accept(ModBlocks.ROYAL_CAKE_BLOCK.get());
                    })
                    .withSearchBar()
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
