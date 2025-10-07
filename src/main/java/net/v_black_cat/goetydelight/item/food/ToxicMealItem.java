package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class ToxicMealItem extends BowlFoodItem {

    // 只保留食用次数标签
    private static final String TOXIC_MEAL_COUNT = "ToxicMealCount";

    // 效果持续时间
    private static final int EFFECT_DURATION = 20 * 100;

    // 免疫阈值
    private static final int NAUSEA_IMMUNE_THRESHOLD = 5;
    private static final int POISON_IMMUNE_THRESHOLD = 10;
    private static final int WEAKNESS_IMMUNE_THRESHOLD = 15;

    public ToxicMealItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return (int) (32 * 3);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);
        playerEat(level, entity);
        return resultStack;
    }

    private static void playerEat(@NotNull Level level, @NotNull LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            int count = persistentData.getInt(TOXIC_MEAL_COUNT);
            count++;
            persistentData.putInt(TOXIC_MEAL_COUNT, count);

//            // 根据食用次数判断是否获得免疫
//            if (count == NAUSEA_IMMUNE_THRESHOLD) {
//                player.displayClientMessage(Component.literal("你获得了对反胃效果的免疫！"), true);
//            } else if (count == POISON_IMMUNE_THRESHOLD) {
//                player.displayClientMessage(Component.literal("你获得了对中毒效果的免疫！"), true);
//            } else if (count == WEAKNESS_IMMUNE_THRESHOLD) {
//                player.displayClientMessage(Component.literal("你获得了对虚弱效果的免疫！"), true);
//            }
//            if (count <=15){
//                player.displayClientMessage(Component.literal("你已食用毒物饭 " + count + " 次。"), true);
//            }

        }
    }

    private static void otherEntityEat(@NotNull Level level, @NotNull LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        int count = persistentData.getInt(TOXIC_MEAL_COUNT);
        count++;
        persistentData.putInt(TOXIC_MEAL_COUNT, count);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!player.level().isClientSide) {
            if (target instanceof Player) {
                playerEat(target.level(), (Player) target);
            } else {
                otherEntityEat(target.level(), target);
            }

            // 添加效果
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EFFECT_DURATION, 0));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION, 9));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION, 4));

            // 消耗物品（如果不是创造模式）
            if (!player.isCreative()) {
                stack.shrink(1);
            }

            player.displayClientMessage(Component.literal("已对目标喂了毒物饭！"), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // 处理效果应用事件 - 根据食用次数判断免疫
    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        CompoundTag persistentData = entity.getPersistentData();
        MobEffectInstance effect = event.getEffectInstance();

        int toxicMealCount = persistentData.getInt(TOXIC_MEAL_COUNT);

        // 根据食用次数判断免疫状态
        boolean immuneNausea = toxicMealCount >= NAUSEA_IMMUNE_THRESHOLD;
        boolean immunePoison = toxicMealCount >= POISON_IMMUNE_THRESHOLD;
        boolean immuneWeakness = toxicMealCount >= WEAKNESS_IMMUNE_THRESHOLD;

        // 根据免疫状态阻止效果
        if (effect.getEffect() == MobEffects.CONFUSION && immuneNausea) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        } else if (effect.getEffect() == MobEffects.POISON && immunePoison) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        } else if (effect.getEffect() == MobEffects.WEAKNESS && immuneWeakness) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    // 玩家死亡事件处理 - 重置食用次数
    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            // 清除食用次数
            persistentData.remove(TOXIC_MEAL_COUNT);
        }
    }


    // 获取食用次数
    public static int getToxicMealCount(Player player) {
        return player.getPersistentData().getInt(TOXIC_MEAL_COUNT);
    }

    // 检查免疫状态（根据食用次数动态判断）
    public static boolean isImmuneToNausea(Player player) {
        return getToxicMealCount(player) >= NAUSEA_IMMUNE_THRESHOLD;
    }

    public static boolean isImmuneToPoison(Player player) {
        return getToxicMealCount(player) >= POISON_IMMUNE_THRESHOLD;
    }

    public static boolean isImmuneToWeakness(Player player) {
        return getToxicMealCount(player) >= WEAKNESS_IMMUNE_THRESHOLD;
    }
}