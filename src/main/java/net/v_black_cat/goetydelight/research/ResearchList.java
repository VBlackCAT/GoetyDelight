package net.v_black_cat.goetydelight.research;

import com.Polarice3.Goety.common.research.Research;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ResearchList {
    public static Map<String, Research> RESEARCH_LIST = Maps.newHashMap();
    public static Research VIZIERS_COOKBOOK = new Research("viziers_cookbook");
    public static void registerResearch(String id, Research research) {
        RESEARCH_LIST.put(id, research);
    }

    static {
        com.Polarice3.Goety.common.research.ResearchList.registerResearch(VIZIERS_COOKBOOK.getId(), VIZIERS_COOKBOOK);
    }
    public static Map<String, Research> getResearchList() {
        Map<String, Research> researches = Maps.newHashMap();
        researches.put(VIZIERS_COOKBOOK.getId(), VIZIERS_COOKBOOK);
        if (!RESEARCH_LIST.isEmpty()) {
            researches.putAll(RESEARCH_LIST);
        }
        return researches;
    }

    public static Map<ResourceLocation, Research> getResearchIdList() {
        Map<ResourceLocation, Research> researches = Maps.newHashMap();
        for (Research research : getResearchList().values()) {
            researches.put(research.getLocation(), research);
        }
        return researches;
    }

    public static Research getResearch(ResourceLocation resourceLocation) {
        if (getResearchIdList().containsKey(resourceLocation)) {
            return getResearchIdList().get(resourceLocation);
        }
        return null;
    }

    public static Research getResearch(String id) {
        if (getResearchList().containsKey(id)) {
            return getResearchList().get(id);
        }
        return null;
    }
}