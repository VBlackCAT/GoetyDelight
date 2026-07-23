package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.v_black_cat.goetydelight.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.Polarice3.Goety.api.ritual.RitualType.getRitualType;
import static net.v_black_cat.goetydelight.GoetyDelight.MODID;
import static net.v_black_cat.goetydelight.init.ModConfig.*;
import static net.v_black_cat.goetydelight.util.RitualUtil.getItemsOnPedestals;

public class MetamorphicScentGrassItem extends Item {
    private static final String TAG_STORED_ID = "StoredId";
    private static final String TAG_STORED_COUNT = "StoredCount";

    public MetamorphicScentGrassItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack stored = getMetamorphicItem(stack, RegistryAccess.EMPTY);
        return stored.isEmpty() ? super.getName(stack) : stored.getHoverName();
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        ItemStack stored = getMetamorphicItem(stack, entity != null ? entity.level().registryAccess() : RegistryAccess.EMPTY);
        return stored.isEmpty() ? super.getFoodProperties(stack, entity) : stored.getFoodProperties(entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (hasMetamorphicItem(stack)) {
            ItemStack stored = getMetamorphicItem(stack, context.registries());
            tooltip.add(Component.translatable("item.goetydelight.metamorphic_scent_grass.metamorphic_item")
                    .append(": ")
                    .append(stored.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasMetamorphicItem(stack) || super.isFoil(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack stored = getMetamorphicItem(stack, level.registryAccess());
        if (!stored.isEmpty()) {
            FoodProperties food = stored.getFoodProperties(livingEntity);
            if (food != null) {
                for (FoodProperties.PossibleEffect possible : food.effects()) {
                    MobEffectInstance original = possible.effect();
                    if (original == null) continue;
                    if (!level.isClientSide && level.random.nextFloat() < possible.probability()) {
                        double durationMul = getMetamorphicScentGrassDurationMultiplier();
                        double amplifierMul = getMetamorphicScentGrassAmplifierMultiplier();
                        int newDuration = (int) (original.getDuration() * durationMul);
                        int newAmplifier = Math.max(0, (int) ((original.getAmplifier() + 1) * amplifierMul) - 1);
                        if (newDuration > 0) {
                            livingEntity.addEffect(new MobEffectInstance(original.getEffect(), newDuration, newAmplifier));
                        }
                    }
                }
            }
        }
        stack.shrink(1);
        return stack;
    }

    //存储/读取
    public static ItemStack getMetamorphicItem(ItemStack grassStack, HolderLookup.Provider provider) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            CustomData data = grassStack.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                CompoundTag tag = data.getUnsafe();
                if (tag.contains(TAG_STORED_ID)) {
                    String id = tag.getString(TAG_STORED_ID);
                    int count = tag.getInt(TAG_STORED_COUNT);
                    ResourceLocation loc = ResourceLocation.parse(id);
                    Item item = BuiltInRegistries.ITEM.getOptional(loc).orElse(null);
                    if (item != null && count > 0) {
                        return new ItemStack(item, count);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Deprecated
    public static ItemStack getMetamorphicItem(ItemStack grassStack) {
        return getMetamorphicItem(grassStack, RegistryAccess.EMPTY);
    }

    public static void setMetamorphicItem(ItemStack grassStack, ItemStack itemToStore, HolderLookup.Provider provider) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            if (itemToStore.isEmpty()) {
                grassStack.remove(DataComponents.CUSTOM_DATA);
            } else {
                CompoundTag tag = new CompoundTag();
                ResourceLocation regName = BuiltInRegistries.ITEM.getKey(itemToStore.getItem());
                tag.putString(TAG_STORED_ID, regName.toString());
                tag.putInt(TAG_STORED_COUNT, itemToStore.getCount());
                grassStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }

    @Deprecated
    public static void setMetamorphicItem(ItemStack grassStack, ItemStack itemToStore) {
        setMetamorphicItem(grassStack, itemToStore, RegistryAccess.EMPTY);
    }

    public static boolean hasMetamorphicItem(ItemStack grassStack) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            CustomData data = grassStack.get(DataComponents.CUSTOM_DATA);
            return data != null && data.getUnsafe().contains(TAG_STORED_ID);
        }
        return false;
    }

    public static void clearMetamorphicItem(ItemStack grassStack) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            grassStack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    public static ItemStack MetamorphicScentGrassRenderItem(ItemStack grassStack) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            ItemStack stored = getMetamorphicItem(grassStack);
            if (!stored.isEmpty()) return stored;
        }
        return grassStack;
    }

    //仪式配方
    public static RitualRecipe metamorphicScentGrassAndFruitReciper(Level world, BlockPos pos,
                                                                    Player player,
                                                                    ItemStack activationItem,
                                                                    RitualRecipe ritualRecipe) {
        if (ritualRecipe != null) return ritualRecipe;
        if (activationItem.getFoodProperties(player) == null) return null;

        List<ItemStack> itemsOnPedestals = getItemsOnPedestals(world, pos);
        if (itemsOnPedestals.size() != 12) return null;

        boolean isGrassRecipe = itemsOnPedestals.stream()
                .allMatch(itemStack -> itemStack.getItem() instanceof MetamorphicScentGrassItem);
        boolean isFruitRecipe = !isGrassRecipe && itemsOnPedestals.stream()
                .allMatch(itemStack -> itemStack.getItem().equals(ModItems.METAMORPHIC_SCENT_FRUIT.get()));

        if (!isGrassRecipe && !isFruitRecipe) return null;

        String craftType = "culinary";
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof RitualBlockEntity ritualBE)) return null;
        if (!getRitualType(craftType).getRequirement(ritualBE, player, pos, world)) return null;

        ResourceLocation recipeId = ResourceLocation.parse("goetydelight:metamorphic_scent_grass_ritual");
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (int i = 0; i < 12; i++) {
            ingredients.add(Ingredient.of(itemsOnPedestals.get(i)));
        }
        Ingredient activItem = Ingredient.of(new ItemStack(activationItem.getItem()));

        ItemStack resultStack;
        if (isGrassRecipe) {
            Set<Item> blacklist = getMetamorphicScentGrassCopyBlacklist();
            if (blacklist.contains(activationItem.getItem())) return null;
            int count = Math.max(1, getMetamorphicScentGrassCopyCount());
            resultStack = new ItemStack(ModItems.METAMORPHIC_SCENT_GRASS.get(), count);
            setMetamorphicItem(resultStack, activationItem.copyWithCount(1), world.registryAccess());
        } else {
            Set<Item> blacklist = getMetamorphicScentFruitCopyBlacklist();
            if (blacklist.contains(activationItem.getItem())) return null;
            resultStack = activationItem.copyWithCount(getMetamorphicScentFruitCopyCount());
        }

        int soulCost = 0;
        FoodProperties food = activationItem.getFoodProperties(player);
        if (food != null) {
            soulCost = 2 * (food.nutrition() + (int) (food.saturation() * food.nutrition()));
        }

        return new RitualRecipe(
                recipeId,
                "goetydelight",
                craftType,
                ResourceLocation.parse("goety:craft"),
                resultStack,
                null, null,
                activItem,
                ingredients,
                3, -1, soulCost,
                null, "", null, "", null, "", null,
                0,
                "floral_scroll"//疑似不生效
        );
    }
}
