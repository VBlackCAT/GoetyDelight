package net.v_black_cat.goetydelight.events.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.tags.ItemTags;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.init.ModRecipeSerializers;

import java.util.List;
import java.util.stream.Collectors;

public class RecraftBoatPlate extends CustomRecipe {

    public RecraftBoatPlate(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    @Override
    public boolean matches(CraftingInput pInput, Level pLevel) {
        ItemStack boatPlate = ItemStack.EMPTY;
        int itemCount = 0;

        for (int i = 0; i < pInput.size(); i++) {
            ItemStack stack = pInput.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.BOAT_PLATE.get()) {
                    boatPlate = stack;
                } else {
                    return false;
                }
            }
        }

        return itemCount == 1 && !boatPlate.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput pInput, HolderLookup.Provider pRegistries) {
        List<Item> logs = getLogItems();
        if (logs.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 基于时间获取当前显示的原木
        Item currentLog = getCurrentDisplayLog(logs);
        return new ItemStack(currentLog, 5);
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RECRAFT_BOAT_PLATE.get();
    }

    // 获取当前应该显示的原木（基于游戏时间，每5秒更换）
    public static Item getCurrentDisplayLog(List<Item> logs) {
        if (logs.isEmpty()) return net.minecraft.world.item.Items.OAK_LOG;

        // 使用系统时间来计算，每5秒更换一次
        long currentTime = System.currentTimeMillis() / 5000;
        int index = (int) (currentTime % logs.size());
        return logs.get(Math.abs(index));
    }

    // 获取所有原木物品的缓存列表
    private static List<Item> getLogItems() {
        // 每次调用时重新获取，确保与数据包同步
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item.getDefaultInstance().is(ItemTags.LOGS))
                .collect(Collectors.toList());
    }

    // 获取结果物品（用于客户端预览）
    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        List<Item> logs = getLogItems();
        return new ItemStack(getCurrentDisplayLog(logs), 5);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput pInput) {
        return NonNullList.withSize(pInput.size(), ItemStack.EMPTY);
    }

    public static class Serializer implements RecipeSerializer<RecraftBoatPlate> {
        // 1.21 使用 MapCodec
        private static final MapCodec<RecraftBoatPlate> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                                .forGetter(recipe -> recipe.category())
                ).apply(instance, RecraftBoatPlate::new)
        );

        // 1.21 使用 StreamCodec
        private static final StreamCodec<RegistryFriendlyByteBuf, RecraftBoatPlate> STREAM_CODEC =
                StreamCodec.composite(
                        CraftingBookCategory.STREAM_CODEC,
                        recipe -> recipe.category(),
                        RecraftBoatPlate::new
                );

        @Override
        public MapCodec<RecraftBoatPlate> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RecraftBoatPlate> streamCodec() {
            return STREAM_CODEC;
        }
    }
}