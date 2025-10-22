package net.v_black_cat.goetydelight.research;

import com.Polarice3.Goety.common.research.Research;

public class ModResearchList {
    public static Research VIZIER_RECIPE= new Research("vizier_recipe");
    static {
        com.Polarice3.Goety.common.research.ResearchList.registerResearch("vizier_recipe", ModResearchList.VIZIER_RECIPE);
    }
}