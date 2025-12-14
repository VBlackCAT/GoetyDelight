package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.Polarice3.Goety.api.ritual.RitualType.getRitualType;
import static net.v_black_cat.goetydelight.util.RitualUtil.getItemsOnPedestals;

public class MetamorphicScentGrassItem extends Item {
    public MetamorphicScentGrassItem(Properties properties) {
        super(properties);
    }

    public static RitualRecipe metamorphicScentGrassReciper(Level world, BlockPos pos,
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

        for(ItemStack itemStack: itemsOnPedestals){
            if (!(itemStack.getItem() instanceof MetamorphicScentGrassItem)){
                return ritualRecipe;
            }
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
            ingredients.add(Ingredient.of(new ItemStack(ModItems.METAMORPHIC_SCENT_GRASS.get())));
        }

        // 创建激活物品（幻味草）
        Ingredient activitem = Ingredient.of(new ItemStack(activationItem.getItem()));


        // 创建结果物品（这里需要根据你的需求定义）
        ItemStack resultItem = new ItemStack(activationItem.getItem(), 2);

        // 创建配方参数
        String group = "goetydelight";

        ResourceLocation ritualType = new ResourceLocation("goety", "craft");
        int duration = 3; // 30秒（20 ticks/秒）
        int summonLife = -1; // 永久
        int soulCost = 0; // 不需要灵魂消耗
        String research = ""; // 不需要研究

        // 创建仪式配方
        RitualRecipe ritualRecipew = new RitualRecipe(
                recipeId,           // 配方ID
                group,              // 组名
                craftType,          // 合成类型
                ritualType,         // 仪式类型
                resultItem,         // 结果物品
                null,               // 召唤实体（不需要）
                null,               // 转换实体（不需要）
                activitem,     // 激活物品
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
