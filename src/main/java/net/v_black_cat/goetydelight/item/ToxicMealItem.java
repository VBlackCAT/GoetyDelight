package net.v_black_cat.goetydelight.item;

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
public class ToxicMealItem extends Item {

    // 永久免疫标签
    private static final String PERMANENT_IMMUNE_NAUSEA = "PermanentImmuneNausea";
    private static final String PERMANENT_IMMUNE_POISON = "PermanentImmunePoison";
    private static final String PERMANENT_IMMUNE_WEAKNESS = "PermanentImmuneWeakness";

    // 临时免疫标签
    private static final String TOXIC_MEAL_COUNT = "ToxicMealCount";
    private static final String IMMUNE_NAUSEA = "ImmuneNausea";
    private static final String IMMUNE_POISON = "ImmunePoison";
    private static final String IMMUNE_WEAKNESS = "ImmuneWeakness";

    // 效果持续时间
    private static final int EFFECT_DURATION = 20 * 100;

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

        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();

            // 检查是否已经获得永久免疫
            boolean hasPermanentNausea = persistentData.getBoolean(PERMANENT_IMMUNE_NAUSEA);
            boolean hasPermanentPoison = persistentData.getBoolean(PERMANENT_IMMUNE_POISON);
            boolean hasPermanentWeakness = persistentData.getBoolean(PERMANENT_IMMUNE_WEAKNESS);

            // 如果还没有永久免疫，则增加食用次数
            if (!hasPermanentNausea || !hasPermanentPoison || !hasPermanentWeakness) {
                int count = persistentData.getInt(TOXIC_MEAL_COUNT);
                count++;
                persistentData.putInt(TOXIC_MEAL_COUNT, count);

                // 根据食用次数授予永久免疫
                if (count == 5 && !hasPermanentNausea) {
                    persistentData.putBoolean(PERMANENT_IMMUNE_NAUSEA, true);
                    player.displayClientMessage(Component.literal("你获得了对反胃效果的永久免疫！"), true);
                } else if (count == 10 && !hasPermanentPoison) {
                    persistentData.putBoolean(PERMANENT_IMMUNE_POISON, true);
                    player.displayClientMessage(Component.literal("你获得了对中毒效果的永久免疫！"), true);
                } else if (count == 15 && !hasPermanentWeakness) {
                    persistentData.putBoolean(PERMANENT_IMMUNE_WEAKNESS, true);
                    player.displayClientMessage(Component.literal("你获得了对虚弱效果的永久免疫！"), true);
                }

                // 显示当前食用次数
                player.displayClientMessage(Component.literal("你已食用毒物饭 " + count + " 次。"), true);
            } else {
                // 如果已经获得所有永久免疫
                player.displayClientMessage(Component.literal("你已经获得了所有永久免疫效果！"), true);
            }
        }

        return resultStack;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!player.level().isClientSide) {
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

    // 处理效果应用事件
    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            MobEffectInstance effect = event.getEffectInstance();

            // 检查永久免疫
            boolean immuneNausea = persistentData.getBoolean(PERMANENT_IMMUNE_NAUSEA);
            boolean immunePoison = persistentData.getBoolean(PERMANENT_IMMUNE_POISON);
            boolean immuneWeakness = persistentData.getBoolean(PERMANENT_IMMUNE_WEAKNESS);

            // 检查临时免疫（如果永久免疫未激活）
            if (!immuneNausea) immuneNausea = persistentData.getBoolean(IMMUNE_NAUSEA);
            if (!immunePoison) immunePoison = persistentData.getBoolean(IMMUNE_POISON);
            if (!immuneWeakness) immuneWeakness = persistentData.getBoolean(IMMUNE_WEAKNESS);

            // 根据免疫状态阻止效果
            if (effect.getEffect() == MobEffects.CONFUSION && immuneNausea) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            } else if (effect.getEffect() == MobEffects.POISON && immunePoison) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            } else if (effect.getEffect() == MobEffects.WEAKNESS && immuneWeakness) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            }
        }
    }

    // 玩家死亡事件处理
    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();

            // 清除临时免疫数据
            persistentData.remove(TOXIC_MEAL_COUNT);
            persistentData.remove(IMMUNE_NAUSEA);
            persistentData.remove(IMMUNE_POISON);
            persistentData.remove(IMMUNE_WEAKNESS);

            // 保留永久免疫数据
        }
    }

    // 玩家重生事件处理
    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        CompoundTag persistentData = player.getPersistentData();

        // 应用永久免疫（如果已获得）
        if (persistentData.getBoolean(PERMANENT_IMMUNE_NAUSEA)) {
            persistentData.putBoolean(IMMUNE_NAUSEA, true);
        }
        if (persistentData.getBoolean(PERMANENT_IMMUNE_POISON)) {
            persistentData.putBoolean(IMMUNE_POISON, true);
        }
        if (persistentData.getBoolean(PERMANENT_IMMUNE_WEAKNESS)) {
            persistentData.putBoolean(IMMUNE_WEAKNESS, true);
        }
    }

    // 获取食用次数
    public static int getToxicMealCount(Player player) {
        return player.getPersistentData().getInt(TOXIC_MEAL_COUNT);
    }

    // 检查永久免疫状态
    public static boolean hasPermanentImmuneToNausea(Player player) {
        return player.getPersistentData().getBoolean(PERMANENT_IMMUNE_NAUSEA);
    }

    public static boolean hasPermanentImmuneToPoison(Player player) {
        return player.getPersistentData().getBoolean(PERMANENT_IMMUNE_POISON);
    }

    public static boolean hasPermanentImmuneToWeakness(Player player) {
        return player.getPersistentData().getBoolean(PERMANENT_IMMUNE_WEAKNESS);
    }

    // 检查当前免疫状态（包括永久和临时）
    public static boolean isImmuneToNausea(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(PERMANENT_IMMUNE_NAUSEA) || data.getBoolean(IMMUNE_NAUSEA);
    }

    public static boolean isImmuneToPoison(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(PERMANENT_IMMUNE_POISON) || data.getBoolean(IMMUNE_POISON);
    }

    public static boolean isImmuneToWeakness(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(PERMANENT_IMMUNE_WEAKNESS) || data.getBoolean(IMMUNE_WEAKNESS);
    }
}