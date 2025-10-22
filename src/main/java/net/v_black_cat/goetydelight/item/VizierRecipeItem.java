package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.research.ResearchScroll;
import com.Polarice3.Goety.common.research.Research;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.v_black_cat.goetydelight.research.ModResearchList;

public class VizierRecipeItem extends ResearchScroll {

    public VizierRecipeItem(Research research) {
        super(scrollProperties(), research);
    }

    public VizierRecipeItem() {
        this(ModResearchList.VIZIER_RECIPE);
    }

    @Override
    public Component researchGet() {
        return Component.translatable("info.goetydelight.research.vizier_recipe_get");
    }

    public static Item.Properties fireResistantProperties() {
        return fireResistant();
    }
}