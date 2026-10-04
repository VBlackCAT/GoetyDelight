package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RoastHaoZiItem extends Item {
    public RoastHaoZiItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.goetydelight.roast_hao_zi"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}