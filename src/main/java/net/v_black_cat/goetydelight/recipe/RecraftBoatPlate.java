package net.v_black_cat.goetydelight.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RecraftBoatPlate extends CustomRecipe {
    private static final Random RANDOM = new Random();
    private static List<Item> LOG_ITEMS = null;

    public RecraftBoatPlate(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }

    @Override
    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        ItemStack boatPlate = ItemStack.EMPTY;
        int itemCount = 0;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.BOAT_PLATE.get()) { // 替换为你的船盘物品
                    boatPlate = stack;
                } else {
                    return false;
                }
            }
        }

        return itemCount == 1 && !boatPlate.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
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
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.getDefaultInstance().is(ItemTags.LOGS))
                .collect(Collectors.toList());
    }

    // 获取结果物品（用于客户端预览）
    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        List<Item> logs = getLogItems();
        return new ItemStack(getCurrentDisplayLog(logs), 5);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer pContainer) {
        return NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);
    }

    public static class Serializer implements RecipeSerializer<RecraftBoatPlate> {
        @Override
        public RecraftBoatPlate fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(pSerializedRecipe, "category", null),
                    CraftingBookCategory.MISC);
            return new RecraftBoatPlate(pRecipeId, category);
        }

        @Override
        public RecraftBoatPlate fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CraftingBookCategory category = pBuffer.readEnum(CraftingBookCategory.class);
            return new RecraftBoatPlate(pRecipeId, category);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, RecraftBoatPlate pRecipe) {
            pBuffer.writeEnum(pRecipe.category());
        }
    }
}