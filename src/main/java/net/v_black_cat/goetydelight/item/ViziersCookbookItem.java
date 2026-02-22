package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.research.Scroll;
import net.minecraft.network.chat.Component;
import net.v_black_cat.goetydelight.research.ModResearchList;


public class ViziersCookbookItem extends Scroll {

    public ViziersCookbookItem(Properties properties) {
        super( properties, ModResearchList.VIZIER_COOKBOOK);
    }

    @Override
    public net.minecraft.network.chat.Component researchGet() {
        return Component.translatable("info.goetydelight.research.vizier_cookbook_get");
    }

}
