package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.Polarice3.Goety.api.ritual.RitualType.getRitualType;
import static net.v_black_cat.goetydelight.GoetyDelight.MODID;
import static net.v_black_cat.goetydelight.config.Config.*;
import static net.v_black_cat.goetydelight.util.RitualUtil.getItemsOnPedestals;
@Mod.EventBusSubscriber(modid = MODID)
public class MetamorphicScentGrassItem extends Item {
    private static final String TAG_STORED_ITEM = "MetamorphicItem";

    public MetamorphicScentGrassItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack storedItem = getMetamorphicItem(stack);
        if (!storedItem.isEmpty()) {
            return storedItem.getItem().getName(stack);
        }
        return super.getName( stack);
    }

    @Override
    public @org.jetbrains.annotations.Nullable FoodProperties getFoodProperties(ItemStack stack, @org.jetbrains.annotations.Nullable LivingEntity entity) {
        ItemStack metamorphicItem = getMetamorphicItem(stack);
        return metamorphicItem.isEmpty() ? super.getFoodProperties(stack, entity) : metamorphicItem.getFoodProperties(entity);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        livingEntity.eat(level, new ItemStack(stack.getItem(),1));

        ItemStack metamorphicItem = getMetamorphicItem(stack);
        if (!metamorphicItem.isEmpty()){


            FoodProperties foodProperties = metamorphicItem.getFoodProperties(livingEntity);
            if (foodProperties != null) {
                List<Pair<MobEffectInstance, Float>> effects = foodProperties.getEffects();
                if (!effects.isEmpty()){
                    Iterator var5 = effects.iterator();
                    double metamorphicScentGrassDurationMultiplier = getMetamorphicScentGrassDurationMultiplier();
                    double metamorphicScentGrassAmplifierMultiplier = getMetamorphicScentGrassAmplifierMultiplier();
                    if (metamorphicScentGrassDurationMultiplier > 0.0D && metamorphicScentGrassAmplifierMultiplier > 0.0D) {

                        while(var5.hasNext()) {
                            Pair<MobEffectInstance, Float> pair = (Pair)var5.next();
                            if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < (Float)pair.getSecond()) {
                                MobEffectInstance originalEffect = pair.getFirst();

                                int newDuration = (int) (originalEffect.getDuration()*metamorphicScentGrassDurationMultiplier);

                                int newAmplifier = Math.max(0, (int) (((originalEffect.getAmplifier()+1)*metamorphicScentGrassAmplifierMultiplier)-1));

                                livingEntity.addEffect(new MobEffectInstance(
                                        originalEffect.getEffect(),
                                        newDuration,
                                        newAmplifier
                                ));
                            }
                        }
                    }
                }

            }

        }
        stack.shrink(1);
        return stack;
    }

    public static ItemStack getMetamorphicItem(ItemStack grassStack) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            CompoundTag tag = grassStack.getTag();
            if (tag != null && tag.contains(TAG_STORED_ITEM, 10)) {
                ItemStack itemStack = ItemStack.of(tag.getCompound(TAG_STORED_ITEM));
                if (itemStack.getItem() instanceof MetamorphicScentGrassItem){
                    return ItemStack.EMPTY;
                }
                return ItemStack.of(tag.getCompound(TAG_STORED_ITEM));
            }
        }
        return ItemStack.EMPTY;
    }

    public static void setMetamorphicItem(ItemStack grassStack, ItemStack itemToStore) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            CompoundTag tag = grassStack.getOrCreateTag();
            if (itemToStore.isEmpty()) {
                tag.remove(TAG_STORED_ITEM);
                if (tag.isEmpty()) {
                    grassStack.setTag(null);
                }
            } else {
                CompoundTag storedTag = new CompoundTag();
                itemToStore.save(storedTag);
                tag.put(TAG_STORED_ITEM, storedTag);
            }
        }
    }

    // 检查是否已存储物品
    public static boolean hasMetamorphicItem(ItemStack grassStack) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            CompoundTag tag = grassStack.getTag();
            return tag != null && tag.contains(TAG_STORED_ITEM, 10);
        }
        return false;
    }

    // 清除存储的物品
    public static void clearMetamorphicItem(ItemStack grassStack) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            CompoundTag tag = grassStack.getTag();
            if (tag != null) {
                tag.remove(TAG_STORED_ITEM);
                if (tag.isEmpty()) {
                    grassStack.setTag(null);
                }
            }
        }
    }


    // 渲染时返回存储的物品
    public static ItemStack MetamorphicScentGrassRenderItem(ItemStack grassStack) {
        if (grassStack.getItem() instanceof MetamorphicScentGrassItem) {
            ItemStack storedItem = getMetamorphicItem(grassStack);
            if (!storedItem.isEmpty()) {
                return storedItem;
            }
        }
        return grassStack;
    }


    // 添加悬停文本显示存储的物品信息
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (hasMetamorphicItem(stack)) {
            ItemStack storedItem = getMetamorphicItem(stack);
            tooltip.add(Component.translatable("item.goetydelight.metamorphic_scent_grass.metamorphic_item")
                    .append(": ")
                    .append(storedItem.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    // 在物品栏中显示存储物品的图标（可选功能）
    @Override
    public boolean isFoil(ItemStack stack) {
        return hasMetamorphicItem(stack) || super.isFoil(stack);
    }
    public static RitualRecipe metamorphicScentGrassAndFruitReciper(Level world, BlockPos pos,
                                                                    Player player,
                                                                    ItemStack activationItem,
                                                                    RitualRecipe ritualRecipe){

        if (ritualRecipe!=null) {
            return ritualRecipe;
        }
        if (activationItem.getFoodProperties(player)==null){
            return ritualRecipe;
        }
        //验证幻味草配方
        List<ItemStack> itemsOnPedestals = getItemsOnPedestals(world, pos);
        if (itemsOnPedestals.size()!=12){
            return ritualRecipe;
        }

        boolean isGrassRecipe = itemsOnPedestals.stream()
                .allMatch(itemStack -> itemStack.getItem() instanceof MetamorphicScentGrassItem);
        
        boolean isFruitRecipe = !isGrassRecipe && itemsOnPedestals.stream()
                .allMatch(itemStack -> itemStack.getItem().equals(ModItems.METAMORPHIC_SCENT_FRUIT.get()));

        if (!isGrassRecipe && !isFruitRecipe){
            return ritualRecipe;
        }


        String craftType = "culinary";
        if(!(getRitualType(craftType)
                .getRequirement((RitualBlockEntity) world.getBlockEntity( pos), player, pos,world))){
            return ritualRecipe;
        }

        // 创建配方ID
        ResourceLocation recipeId = new ResourceLocation("goetydelight", "metamorphic_scent_grass_ritual");

        NonNullList<Ingredient> ingredients = NonNullList.create();

        for (int i = 0; i < 12; i++) {
            ingredients.add(Ingredient.of(itemsOnPedestals.get(i)));
        }

        // 创建激活物品
        Ingredient activitem = Ingredient.of(new ItemStack(activationItem.getItem()));

        ItemStack stack;
        if (isGrassRecipe){
            Set<Item> metamorphicScentGrassCopyBlacklist = getMetamorphicScentGrassCopyBlacklist();
            if (metamorphicScentGrassCopyBlacklist.contains(activationItem.getItem())) {
                return ritualRecipe;
            }

            stack = new ItemStack(ModItems.METAMORPHIC_SCENT_GRASS.get(),getMetamorphicScentGrassCopyCount());
            setMetamorphicItem(stack, activationItem.copyWithCount(1));
        }else {
            Set<Item> metamorphicScentFruitCopyBlacklist = getMetamorphicScentFruitCopyBlacklist();
            if (metamorphicScentFruitCopyBlacklist.contains(activationItem.getItem())) {
                return ritualRecipe;
            }
            stack = activationItem.copyWithCount(getMetamorphicScentFruitCopyCount());
        }
        ItemStack resultItem = stack;

        // 创建配方参数
        String group = "goetydelight";

        ResourceLocation ritualType = new ResourceLocation("goety", "craft");
        int duration = 3; // 20秒（20 ticks/秒）
        int summonLife = -1; // 永久
        int soulCost = 0;
        if (activationItem.getFoodProperties(player) != null) {
            FoodProperties foodProperties = activationItem.getFoodProperties(player);
            soulCost = 2 * (foodProperties.getNutrition() + (int)(foodProperties.getSaturationModifier() * foodProperties.getNutrition()));
        }
        String research = "floral_scroll";

        // 创建仪式配方
        RitualRecipe ritualRecipew = new RitualRecipe(
                recipeId,           // 配方ID
                group,              // 组名
                craftType,          // 合成类型
                ritualType,         // 仪式类型
                resultItem,         // 结果物品
                null,               // 召唤实体（不需要）
                null,               // 转换实体（不需要）
                activitem,          // 激活物品
                ingredients,        // 成分列表
                duration,           // 持续时间
                summonLife,         // 召唤生命
                soulCost,           // 灵魂消耗
                null,               // 牺牲实体（不需要）
                "",                 // 牺牲实体显示名
                null,               // 转换实体标签
                "",                 // 转换实体显示名
                null,               // 附魔（不需要）
                0,                  // 经验等级消耗
                research            // 研究要求
        );
        return ritualRecipew;

    }




}
