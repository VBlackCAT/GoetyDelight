package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.common.items.research.ResearchScroll;
import com.Polarice3.Goety.common.research.Research;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ViziersCookbookItem extends ResearchScroll {

    public ViziersCookbookItem(Item.Properties properties, Research research) {
        super(properties, research);
    }

    public ViziersCookbookItem(Research research) {
        super(research);
    }

    public static Item.Properties cookbookProperties() {
        return (new Item.Properties()).rarity(Rarity.RARE).setNoRepair().stacksTo(1);
    }

    @Override
    public Component researchGet() {
        return Component.translatable("info.goetydelight.research." + this.research.getId());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("info.goetydelight.items." + this.research.getId()).withStyle(ChatFormatting.GOLD));
        if (worldIn != null && worldIn.isClientSide) {
            if (SEHelper.hasResearch(Goety.PROXY.getPlayer(), this.research)) {
                tooltip.add(Component.translatable("info.goety.research.learned").withStyle(ChatFormatting.BLUE));
            } else {
                tooltip.add(Component.translatable("info.goetydelight.items.cookbook").withStyle(ChatFormatting.AQUA));
            }
        }
    }
}
