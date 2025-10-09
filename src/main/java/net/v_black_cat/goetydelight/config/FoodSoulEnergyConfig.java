package net.v_black_cat.goetydelight.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class FoodSoulEnergyConfig {
     
    private static final Map<String, Integer> FOOD_ENERGY_MAP = new HashMap<>();

     
    static {

        FOOD_ENERGY_MAP.put("goetydelight:ectoplasmic_melon", 50);
        FOOD_ENERGY_MAP.put("goetydelight:blue_ectoplasmic_sundae", 300);
        FOOD_ENERGY_MAP.put("goetydelight:ectoplasm_jelly", 100);
        FOOD_ENERGY_MAP.put("goetydelight:gathering_soul_embryos", 10000);
        FOOD_ENERGY_MAP.put("goetydelight:soul_convergence_room", 30000);
        FOOD_ENERGY_MAP.put("goetydelight:villagers_feast", 100);

    }

     
    public static int getSoulEnergyForItem(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId != null) {
            return FOOD_ENERGY_MAP.getOrDefault(itemId.toString(), 0);
        }
        return 0;
    }

     
    public static void addFoodEnergy(String itemId, int energy) {
        FOOD_ENERGY_MAP.put(itemId, energy);
    }

     
    public static Map<String, Integer> getAllFoodEnergyConfig() {
        return new HashMap<>(FOOD_ENERGY_MAP);
    }
}