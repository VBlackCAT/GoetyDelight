package net.v_black_cat.goetydelight.compat.goetyrevelation.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 一条"已食用过的饰品"记录。 */
public class ConsumedCurio {

    public final ResourceLocation itemId;
    public final int rarityOrdinal;    // Rarity.ordinal()
    public final CompoundTag stackTag; // 可为 null

    public ConsumedCurio(ResourceLocation itemId, int rarityOrdinal, CompoundTag stackTag) {
        this.itemId = itemId;
        this.rarityOrdinal = rarityOrdinal;
        this.stackTag = stackTag;
    }

    public static ConsumedCurio fromStack(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        CompoundTag tag = stack.hasTag() ? stack.getTag().copy() : null;
        return new ConsumedCurio(id, stack.getRarity().ordinal(), tag);
    }

    public CompoundTag toTag() {
        CompoundTag c = new CompoundTag();
        c.putString("id", itemId.toString());
        c.putInt("rarity", rarityOrdinal);
        if (stackTag != null) c.put("tag", stackTag.copy());
        return c;
    }

    public static ConsumedCurio fromTag(CompoundTag c) {
        ResourceLocation id = new ResourceLocation(c.getString("id"));
        int r = c.getInt("rarity");
        CompoundTag t = c.contains("tag") ? c.getCompound("tag").copy() : null;
        return new ConsumedCurio(id, r, t);
    }
}