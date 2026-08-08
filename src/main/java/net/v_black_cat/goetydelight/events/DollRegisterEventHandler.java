package net.v_black_cat.goetydelight.events;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.DollBlock;
import net.v_black_cat.goetydelight.item.DollItem;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class DollRegisterEventHandler {
    public static final Map<ResourceLocation, DollBlock> DOLL_BLOCKS = Maps.newLinkedHashMap();
    public static final Map<ResourceLocation, String> VANILLA_TOOLTIPS = Maps.newHashMap();
    public static final Map<ResourceLocation, String> SPECIAL_TOOLTIPS = Maps.newHashMap();
    public static final Set<Item> DOLL_ITEMS = Sets.newLinkedHashSet();
    public static final String[] SPECIAL_DOLL_NAMES = {
            "doll_5152", "doll_p0", "doll_p7", "doll_bai", "doll_baka",
            "doll_fox", "doll_kunzhong", "doll_lamiao", "doll_lll252", "doll_m3",
            "doll_maid1", "doll_maid2", "doll_moon", "doll_skillupper", "doll_vblackcat",
            "doll_windis","doll_zswj", "doll_yushi", "doll_sim", "doll_dwky",
            "doll_sky", "doll_dimspector","doll_haozi","doll_fish","doll_kunkun","doll_djm",
            "doll_htohtosgoy"
    };

    private static void registerAllSpecialTooltips() {
        registerVanillaTooltips("doll_5152", "doll_5152");
        registerVanillaTooltips("doll_p0", "doll_p0");
        registerVanillaTooltips("doll_p7", "doll_p7");
        registerVanillaTooltips("doll_bai", "doll_bai");
        registerVanillaTooltips("doll_baka", "doll_baka");
        registerVanillaTooltips("doll_fox", "doll_fox");
        registerVanillaTooltips("doll_lamiao", "doll_lamiao");
        registerVanillaTooltips("doll_kunzhong", "doll_kunzhong");
        registerVanillaTooltips("doll_lll252", "doll_lll252");
        registerVanillaTooltips("doll_m3", "doll_m3");
        registerVanillaTooltips("doll_maid1", "doll_maid1");
        registerVanillaTooltips("doll_maid2", "doll_maid2");
        registerVanillaTooltips("doll_moon", "doll_moon");
        registerVanillaTooltips("doll_skillupper", "doll_skillupper");
        registerVanillaTooltips("doll_vblackcat", "doll_vblackcat");
        registerVanillaTooltips("doll_windis", "doll_windis");
        registerVanillaTooltips("doll_zswj", "doll_zswj");
        registerVanillaTooltips("doll_yushi", "doll_yushi");
        registerVanillaTooltips("doll_sim", "doll_sim");
        registerVanillaTooltips("doll_dwky", "doll_dwky");
        registerVanillaTooltips("doll_sky", "doll_sky");
        registerVanillaTooltips("doll_dimspector", "doll_dimspector");
        registerVanillaTooltips("doll_haozi", "doll_haozi");
        registerVanillaTooltips("doll_fish", "doll_fish");
        registerVanillaTooltips("doll_kunkun", "doll_kunkun");
        registerVanillaTooltips("doll_djm", "doll_djm");
        registerVanillaTooltips("doll_htohtosgoy", "doll_htohtosgoy");
    }

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event) {
        registerAllSpecialTooltips();

        if (event.getRegistryKey().equals(BuiltInRegistries.BLOCK.key())) {
            for (String dollName : SPECIAL_DOLL_NAMES) {
                ResourceLocation name = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, dollName);
                DollBlock block = new DollBlock();
                DOLL_BLOCKS.put(name, block);
                event.register(BuiltInRegistries.BLOCK.key(), name, () -> block);
            }
        }

        if (event.getRegistryKey().equals(BuiltInRegistries.ITEM.key())) {
            for (String dollName : SPECIAL_DOLL_NAMES) {
                ResourceLocation name = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, dollName);
                DollBlock block = DOLL_BLOCKS.get(name);
                String vanillaDesc = VANILLA_TOOLTIPS.getOrDefault(name, "vanilla");
                String specialDesc = SPECIAL_TOOLTIPS.getOrDefault(name, vanillaDesc);
                Item item = new DollItem(block, specialDesc);
                DOLL_ITEMS.add(item);
                event.register(BuiltInRegistries.ITEM.key(), name, () -> item);
            }
        }
    }

    private static void registerVanillaTooltips(String name, String tooltip) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, name);
        VANILLA_TOOLTIPS.put(id, tooltip);
    }
}