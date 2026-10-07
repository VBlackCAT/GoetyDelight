package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.UUID;

public class ApollyonCakeItem extends BlockItem {

    public static final String TAG_SERVINGS = "CakeServings";
    public static final String TAG_IS_THE_END = "CakeIsTheEnd";
    public static final String TAG_OWNER = "CakeOwner";

    public ApollyonCakeItem(Block block, Properties properties) {
        super(block, properties);
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
}