package net.v_black_cat.goetydelight.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.block.ModBlocks;

import java.util.function.Consumer;

public class ModAdvancementGenerator implements ForgeAdvancementProvider.AdvancementGenerator {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<Advancement> consumer,
                         ExistingFileHelper existingFileHelper) {

        // ========== 根成就 ==========
/*
        Builder.advancement()- 创建成就构建器
                .display()- 设置成就显示属性
                .addCriterion()- 添加触发条件
                .save()- 保存成就到指定路径
*/
        Advancement root = Builder.advancement()
                .display(ModItems.GOETYDELIGHT_ICON.get(),
                        net.minecraft.network.chat.Component.translatable("advancement.goetydelight.root"),
                        net.minecraft.network.chat.Component.translatable("advancement.goetydelight.root.desc"),
                        new ResourceLocation("textures/block/blackstone.png"),
                        FrameType.TASK, false, false, false)
                .addCriterion("has_any_item", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[0]))
                .save(consumer, GoetyDelight.MODID + ":main/root");//对应生成的json文件名(root.json)

        // ========== 第一层：基础成就 ==========
/*
        FrameType 类型：
        TASK- 普通任务（绿色边框）
        GOAL- 重要目标（黄色边框）
        CHALLENGE- 挑战（紫色边框）
*/

        // 1.1 获得物品
        //getAdvancement()自定义方法:第一个参数为父级进度,具体逻辑自行查看
        Advancement firstItem = getAdvancement(root, ModItems.CORPSE_MAGGOT.get(),
                "get_corpse_maggot", FrameType.TASK, true, true, false)

                .addCriterion("has_corpse_maggot",
                        //物品获取触发
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CORPSE_MAGGOT.get()))
                .save(consumer, GoetyDelight.MODID + ":main/get_corpse_maggot");

        // 1.2 制作刀具
        Advancement firstKnife = getAdvancement(root, ModItems.DARK_KNIFE.get(),
                "craft_first_knife", FrameType.TASK, true, true, false)
                .addCriterion("craft_knife",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.DARK_KNIFE.get()))
                .save(consumer, GoetyDelight.MODID + ":main/craft_first_knife");

        // 1.3 放置工炉灶
        Advancement placeStove = getAdvancement(root, ModBlocks.NIGHT_STOVE.get(),
                "place_night_stove", FrameType.TASK, true, false, false)
                .addCriterion("place_stove",
                        //方块放置触发
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.SHADE_STOVE.get()))
                .save(consumer, GoetyDelight.MODID + ":main/place_shade_stove");

        // ========== 第二层：进阶成就（需要第一层成就） ==========

        // 2.1 刀具收集者（需要第一把刀）
        Advancement knifeCollector = getAdvancement(firstKnife, ModItems.APOCALYPTIUM_KNIFE.get(),
                "knife_collector", FrameType.GOAL, true, true, false)
                .addCriterion("apocalyptium_knife",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.APOCALYPTIUM_KNIFE.get()))
                .addCriterion("venomous_spider_knife",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.VENOMOUS_SPIDER_KNIFE.get()))
                .addCriterion("spectre_knife",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SPECTRE_KNIFE.get()))
                .requirements(RequirementsStrategy.AND) // 需要所有刀具
                .save(consumer, GoetyDelight.MODID + ":main/knife_collector");

        // 2.2 美食家（需要获得第一个物品）
        Advancement foodLover = getAdvancement(firstItem, ModItems.ECTOPLASMIC_MELON.get(),
                "food_lover", FrameType.TASK, true, true, false)
                .addCriterion("eat_melon",
                        //物品使用触发
                        ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.ECTOPLASMIC_MELON.get()))
                .addCriterion("eat_pudding",
                        ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.SEVEN_LEAF_PUDDING.get()))
                .addCriterion("eat_ice_cream",
                        ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.OMINOUS_ICE_CREAM.get()))
                .requirements(RequirementsStrategy.OR) // 任意一种食物即可
                .save(consumer, GoetyDelight.MODID + ":main/food_lover");

        // 2.3 厨房大师（需要放置炉子）
        Advancement kitchenMaster = getAdvancement(placeStove, ModBlocks.SHADE_STOVE.get(),
                "kitchen_master", FrameType.TASK, true, false, false)
                .addCriterion("place_night_stove",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.NIGHT_STOVE.get()))
                .addCriterion("place_cursed_pot",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.CURSED_INGOT_POT.get()))
                .requirements(RequirementsStrategy.AND) // 需要所有厨具
                .save(consumer, GoetyDelight.MODID + ":main/kitchen_master");

        // ========== 第三层：专家成就（需要第二层成就） ==========

        // 3.1 刀具大师（需要刀具收集者）
        Advancement knifeMaster = getAdvancement(knifeCollector, ModItems.APOCALYPTIUM_KNIFE.get(),
                "knife_master", FrameType.CHALLENGE, true, true, false)
                .addCriterion("use_knife_100_times",
                        // 物品耐久度触发
                        ItemDurabilityTrigger.TriggerInstance.changedDurability(
                                ItemPredicate.Builder.item()
                                        .of(ModItems.APOCALYPTIUM_KNIFE.get(),
                                                ModItems.VENOMOUS_SPIDER_KNIFE.get(),
                                                ModItems.SPECTRE_KNIFE.get(),
                                                ModItems.DARK_KNIFE.get()).build(),
                                MinMaxBounds.Ints.atLeast(100)
                        ))
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(500))
                .save(consumer, GoetyDelight.MODID + ":main/knife_master");

        // 3.2 黑暗美食家（需要美食家）
        Advancement darkGourmet = getAdvancement(foodLover, ModItems.TOXIC_MEAL.get(),
                "dark_gourmet", FrameType.GOAL, true, true, false)
                // 为每种食物创建单独的食用条件
                .addCriterion("eat_melon", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.ECTOPLASMIC_MELON.get()))
                .addCriterion("eat_pudding", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.SEVEN_LEAF_PUDDING.get()))
                .addCriterion("eat_ice_cream", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.OMINOUS_ICE_CREAM.get()))
                .addCriterion("eat_toxic_meal", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.TOXIC_MEAL.get()))
                .addCriterion("eat_roasted_maggots", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.ROASTED_CORPSE_MAGGOTS.get()))
                .addCriterion("eat_frog_leg", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.GRILL_FROG_LEG.get()))
                .addCriterion("eat_ash_rice", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.BONE_LORD_ASH_RICE.get()))
                .addCriterion("eat_villagers_feast", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.VILLAGERS_FEAST.get()))
                .addCriterion("eat_chaos_stew", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.LICHS_CHAOS_STEW.get()))
                .addCriterion("eat_philosophers_sundae", ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.SUNDAE_OF_THE_PHILOSOPHERS_POTION.get()))
                .requirements(RequirementsStrategy.AND) // 需要吃所有食物
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(300))
                .save(consumer, GoetyDelight.MODID + ":main/dark_gourmet");

        // 3.3 完美厨师（需要厨房大师)
        Advancement perfectChef = getAdvancement(kitchenMaster, ModItems.ROTTEN_CORPSE_MAGGOT_FEAST.get(),
                "perfect_chef", FrameType.GOAL, true, true, false)
                .addCriterion("cook_50_items",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                ItemPredicate.Builder.item().of(ModItems.ROASTED_CORPSE_MAGGOTS.get()).withCount(MinMaxBounds.Ints.atLeast(50)).build()
                        ))
                .save(consumer, GoetyDelight.MODID + ":main/perfect_chef");

        // ========== 第四层：终极成就（需要所有第三层成就） ==========

        // 4.1 黑暗料理之神
        Advancement darkCookingGod = getAdvancement(root, ModItems.ROTTEN_CORPSE_MAGGOT_FEAST.get(),
                "dark_cooking_god", FrameType.CHALLENGE, true, true, true) // 隐藏成就
                .addCriterion("knife_master_complete",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.APOCALYPTIUM_KNIFE.get()))
                .addCriterion("dark_gourmet_complete",
                        ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.TOXIC_MEAL.get()))
                .addCriterion("perfect_chef_complete",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ROTTEN_CORPSE_MAGGOT_FEAST.get()))
                .requirements(RequirementsStrategy.AND)
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(1000)
                        .addLootTable(new ResourceLocation(GoetyDelight.MODID, "rewards/dark_cooking_god")))
                .save(consumer, GoetyDelight.MODID + ":main/dark_cooking_god");

        // ========== 特殊分支：建筑材料收集 ==========

        // 大理石收集者（独立分支）
        Advancement marbleCollector = getAdvancement(root, ModBlocks.MARBLE.get(),
                "marble_collector", FrameType.TASK, true, false, false)
                .addCriterion("get_marble",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.MARBLE.get()))
                .addCriterion("get_silt_marble",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.SILT_MARBLE_HEAVY.get()))
                .addCriterion("get_blue_marble",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.BLUE_MARBLE.get()))
                .requirements(RequirementsStrategy.OR)
                .save(consumer, GoetyDelight.MODID + ":main/marble_collector");

        // 大理石建筑师（需要大理石收集者）
        Advancement marbleArchitect = getAdvancement(marbleCollector, ModBlocks.MARBLE_STAIRS.get(),
                "marble_architect", FrameType.GOAL, true, true, false)
                .addCriterion("build_with_marble",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.MARBLE_STAIRS.get()))
                .addCriterion("build_slab",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.MARBLE_SLAB.get()))
                .addCriterion("build_fence",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.MARBLE_FENCE.get()))
                .addCriterion("build_gate",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.MARBLE_FENCE_GATE.get()))
                .requirements(RequirementsStrategy.AND)
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(200))
                .save(consumer, GoetyDelight.MODID + ":main/marble_architect");
    }

    private static Builder getAdvancement(Advancement parent, ItemLike display, String name,
                                          FrameType frame, boolean showToast, boolean announce, boolean hidden) {
        return Builder.advancement()
                .parent(parent)
                .display(display,
                        net.minecraft.network.chat.Component.translatable("advancement.goetydelight." + name),
                        net.minecraft.network.chat.Component.translatable("advancement.goetydelight." + name + ".desc"),
                        null, frame, showToast, announce, hidden);
    }
}