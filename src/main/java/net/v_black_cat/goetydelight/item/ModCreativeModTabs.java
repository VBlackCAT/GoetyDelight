package net.v_black_cat.goetydelight.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.*;

import java.util.Arrays;
import java.util.List;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoetyDelight.MODID);

    public static final RegistryObject<CreativeModeTab> GOETYDELIGHT_TAB = CREATIVE_MODE_TABS.register("goetydelight_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GOETYDELIGHT_ICON.get()))
                    .title(Component.translatable("creativetab.goetydelight_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        List<RegistryObject<? extends ItemLike>> allItems = Arrays.asList(



                                // 物品
                                //ModItems.EXAMPLE_ITEM,
                                //ModItems.Marble_OP_SWORD,
                                ModItems.CAKE,
                                ModItems.SEVEN_LEAF_PUDDING,
                                ModItems.TAINTED_DRINK,
                                ModItems.OMINOUS_ICE_CREAM,
                                ModItems.ECTOPLASMIC_MELON,
                                ModItems.CRYING_SHARK_SUGAR_PACK,
                                ModItems.SKULL_SHOT,
                                ModItems.REJECTED_DARK_MEAT_SOUP,
                                ModItems.SIBLING_SUNDAE,
                                ModItems.PROMOTION_HARD_CANDY,
                                ModItems.NIGHT_HEART_PEA_SOUP,
                                ModItems.CUP,
                                ModItems.BLUE_ECTOPLASMIC_SUNDAE,
                                ModItems.TOXIC_MEAL,
                                ModItems.POACHED_NETHER_WART_EGG,
                                ModItems.POACHED_SPIDER_EGG,
                                ModItems.ECTOPLASM_JELLY,
                                ModItems.ROASTED_CORPSE_MAGGOTS,
                                ModItems.GRILL_FROG_LEG,
                                ModItems.BEAR_PAW,
                                ModItems.FRENZIED_FUNGUS_POP_ROCKS,
                                ModItems.WHITE_SHARK_CANDY,
                                ModItems.WHITE_SHARK_SUGAR_PACK,
                                ModItems.CANDY_FISH,
                                ModItems.SOUL_CONVERGENCE_ROOM,
                                ModItems.SOUL_CONVERGENCE_ROOM_2,
                                ModItems.GRAPE_SLUSH,
                                ModItems.FROG_LEG_SANDWICH,
                                //ModItems.SPIDER_EGG_BUBBLE_TEA,
                                //ModItems.SPIDER_EGG_BUBBLE_TEA_2,
                                ModItems.SAUCE_GRILLED_CANDY_FISH,
                                ModItems.BONE_LORD_ASH_RICE,
                                ModItems.RUBY_HARD_CANDY,
                                ModItems.CRISP_BISCUIT,
                                ModItems.ROTTEN_CORPSE_MAGGOT_FEAST,
                                ModItems.CORPSE_MAGGOT,

                                //工具
                                ModItems.APOCALYPTIUM_KNIFE0,
                                ModItems.VENOMOUS_SPIDER_KNIFE,
                                ModItems.SPECTRE_KNIFE,
//                                ModItems.APOCALYPTIUM_KNIFE2,
                                //ModItems.APOCALYPTIUM_KNIFE1,
                                ModItems.BLACK_IRON_KNIFE,
                                ModItems.DARK_KNIFE,

                                ModItems.BRUSH

                                // 方块
//                                ModBlocks.NIGHT_STOVE
//                                ModBlocks.APOCALYPTIUM_COOKING_POT_PARTS,
//                                ModBlocks.BLACK_IRON_COOKING_POT



                                //ModBlocks.EXAMPLE_BLOCK,
                                //ModBlocks.MARBLE,
//                                ModBlocks.JUNGLE_MARBLE,
//                                ModBlocks.BLUE_MARBLE,
//                                ModBlocks.NETHER_MARBLE,
//                                ModBlocks.DRIPMARBLE_BLOCK,
//                                ModBlocks.POINTED_DRIPMARBLE,
//                                ModBlocks.MARBLE_STAIRS,
//                                ModBlocks.MARBLE_SLAB,
                                //ModBlocks.MARBLE_BUTTON,
//                                ModBlocks.MARBLE_PRESSURE_PLATE,
                                //ModBlocks.MARBLE_FENCE,
//                                ModBlocks.MARBLE_FENCE_GATE,
                                //ModBlocks.MARBLE_WALL,
//                                ModBlocks.MARBLE_DOOR,
//                                ModBlocks.MARBLE_TRAPDOOR
                        );


                        for (RegistryObject<? extends ItemLike> item : allItems) {
                            pOutput.accept(stackOf(item));
                        }

                    })
                    .build());


    private static ItemStack stackOf(RegistryObject<? extends ItemLike> registryObject) {
        return new ItemStack(registryObject.get(), 1);
    }


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
