package net.v_black_cat.goetydelight.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.util.FoodState;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class ToxicMealItem extends BowlFoodItem {

    private static final int EFFECT_DURATION = 20 * 100;
    private static final int NAUSEA_IMMUNE_THRESHOLD = 5;
    private static final int POISON_IMMUNE_THRESHOLD = 10;
    private static final int WEAKNESS_IMMUNE_THRESHOLD = 15;

    public ToxicMealItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {return (int) (32 * 3);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);
        playerEat(level, entity);
        return resultStack;
    }

    private static void playerEat(@NotNull Level level, @NotNull LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            FoodState state = player.getData(ModAttachments.FOOD_STATE);
            int count = state.getToxicMealCount();
            count++;
            state.setToxicMealCount(count);
        }
    }

    private static void otherEntityEat(@NotNull Level level, @NotNull LivingEntity entity) {
        FoodState state = entity.getData(ModAttachments.FOOD_STATE);
        int count = state.getToxicMealCount();
        count++;
        state.setToxicMealCount(count);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!player.level().isClientSide) {
            if (target instanceof Player) {
                playerEat(target.level(), (Player) target);
            } else {
                otherEntityEat(target.level(), target);
            }

            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EFFECT_DURATION, 0));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION, 9));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION, 4));

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            player.displayClientMessage(Component.literal("已对目标喂了毒物饭！"), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();

        if (effect == null) return;

        int toxicMealCount = entity.getData(ModAttachments.FOOD_STATE).getToxicMealCount();

        boolean immuneNausea = toxicMealCount >= NAUSEA_IMMUNE_THRESHOLD;
        boolean immunePoison = toxicMealCount >= POISON_IMMUNE_THRESHOLD;
        boolean immuneWeakness = toxicMealCount >= WEAKNESS_IMMUNE_THRESHOLD;

        if (effect.getEffect() == MobEffects.CONFUSION && immuneNausea) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        } else if (effect.getEffect() == MobEffects.POISON && immunePoison) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        } else if (effect.getEffect() == MobEffects.WEAKNESS && immuneWeakness) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player) {
            player.getData(ModAttachments.FOOD_STATE).setToxicMealCount(0);
        }
    }

    public static int getToxicMealCount(Player player) {
        return player.getData(ModAttachments.FOOD_STATE).getToxicMealCount();
    }

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