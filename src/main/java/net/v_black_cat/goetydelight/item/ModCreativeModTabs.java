package net.v_black_cat.goetydelight.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;

import java.util.HashSet;
import java.util.Set;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GoetyDelight.MODID);

    // 阻止列表 - 包含不应该出现在创造模式标签页的物品
    private static final Set<RegistryObject<?>> BLACKLIST = new HashSet<>();

    static {
         BLACKLIST.add(ModItems.EXAMPLE_ITEM);
         BLACKLIST.add(ModItems.Marble_OP_SWORD);

         BLACKLIST.add(ModItems.APOCALYPTIUM_KNIFE2);
         BLACKLIST.add(ModItems.APOCALYPTIUM_KNIFE1);
         BLACKLIST.add(ModItems.GOETYDELIGHT_ICON);
         BLACKLIST.add(ModItems.ROASTED_CORPSE_MAGGOTS);
         BLACKLIST.add(ModItems.ROTTEN_CORPSE_MAGGOT_FEAST);
         BLACKLIST.add(ModItems.CORPSE_MAGGOT);


         BLACKLIST.add(ModBlocks.NIGHT_STOVE);
         BLACKLIST.add(ModBlocks.APOCALYPTIUM_COOKING_POT_PARTS);
         BLACKLIST.add(ModBlocks.BLACK_IRON_COOKING_POT);
         BLACKLIST.add(ModBlocks.EXAMPLE_BLOCK);
         BLACKLIST.add(ModBlocks.NETHER_MARBLE);
         BLACKLIST.add(ModBlocks.POINTED_DRIPMARBLE);
         BLACKLIST.add(ModBlocks.DRIPMARBLE_BLOCK);
         BLACKLIST.add(ModBlocks.MARBLE_STAIRS);
         BLACKLIST.add(ModBlocks.MARBLE_SLAB);
         BLACKLIST.add(ModBlocks.MARBLE_BUTTON);
         BLACKLIST.add(ModBlocks.MARBLE);
         BLACKLIST.add(ModBlocks.MARBLE_PRESSURE_PLATE);
         BLACKLIST.add(ModBlocks.MARBLE_FENCE);
         BLACKLIST.add(ModBlocks.MARBLE_WALL);
         BLACKLIST.add(ModBlocks.MARBLE_FENCE_GATE);
         BLACKLIST.add(ModBlocks.MARBLE_DOOR);
         BLACKLIST.add(ModBlocks.SILT_MARBLE_HEAVY);
         BLACKLIST.add(ModBlocks.BLUE_MARBLE);
         BLACKLIST.add(ModBlocks.JUNGLE_MARBLE);
         BLACKLIST.add(ModBlocks.MARBLE_TRAPDOOR);

    }

    public static final RegistryObject<CreativeModeTab> GOETYDELIGHT_TAB = CREATIVE_MODE_TABS.register("goetydelight_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.GOETYDELIGHT_ICON.get()))
                    .title(Component.translatable("creativetab.goetydelight_tab"))
                    .displayItems((parameters, output) -> {
                        // 添加所有物品，但排除阻止列表中的物品
                        ModItems.ITEMS.getEntries().forEach(item -> {
                            if (item.isPresent() && !BLACKLIST.contains(item)) {
                                output.accept(item.get());
                            }
                        });

                        // 添加所有方块，但排除阻止列表中的方块
                        ModBlocks.BLOCKS.getEntries().forEach(block -> {
                            if (block.isPresent() && !BLACKLIST.contains(block)) {
                                output.accept(block.get());
                            }
                        });
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    // 辅助方法：添加物品到阻止列表
    public static void addToBlacklist(RegistryObject<?> item) {
        BLACKLIST.add(item);
    }

    // 辅助方法：从阻止列表移除物品
    public static void removeFromBlacklist(RegistryObject<?> item) {
        BLACKLIST.remove(item);
    }
}