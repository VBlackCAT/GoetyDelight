package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ApollyonCakeItem extends BlockItem {

    public static final String TAG_SERVINGS = "CakeServings";
    public static final String TAG_IS_THE_END = "CakeIsTheEnd";
    public static final String TAG_OWNER = "CakeOwner";
    public static final String THE_END_NAME_KEY = "block.goetydelight.apollyon_cake.the_end";

    public ApollyonCakeItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** 该堆叠是否带 is_the_end 标记（破坏掉落、放置时都靠它传递状态） */
    public static boolean isTheEnd(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_IS_THE_END);
    }

    /** is_the_end 时把名字键换成「终末蛋糕」 */
    @Override
    public String getDescriptionId(ItemStack stack) {
        return isTheEnd(stack) ? THE_END_NAME_KEY : super.getDescriptionId(stack);
    }

    /** 由方块破坏逻辑调用，生成带数据的掉落物 */
    public static ItemStack createStack(Block block,
                                        int servings,
                                        boolean isTheEnd,
                                        @Nullable UUID owner) {
        ItemStack stack = new ItemStack(block);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_SERVINGS, servings);
        tag.putBoolean(TAG_IS_THE_END, isTheEnd);
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
        return stack;
    }

    /** 放置时读取 NBT，用于恢复方块状态与 BlockEntity 数据 */
    @Nullable
    public static CompoundTag getCakeTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        if (!tag.contains(TAG_SERVINGS)
                && !tag.contains(TAG_IS_THE_END)
                && !tag.contains(TAG_OWNER)) {
            return null;
        }
        return tag;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.goetydelight.apollyon_cake.tooltip.1")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apollyon_cake.tooltip.2")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apollyon_cake.tooltip.3")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apollyon_cake.tooltip.4")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
