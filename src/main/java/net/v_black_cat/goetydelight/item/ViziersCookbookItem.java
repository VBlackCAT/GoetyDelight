package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.research.Scroll;
import net.minecraft.network.chat.Component;
import net.v_black_cat.goetydelight.research.ResearchList;

public class ViziersCookbookItem extends Scroll {
    public ViziersCookbookItem() {
        super(ResearchList.VIZIER_COOKBOOK);
    }
    @Override
    public Component researchGet() {
        return Component.translatable("info.goetydelight.research.vizier_cookbook");
    }
}
