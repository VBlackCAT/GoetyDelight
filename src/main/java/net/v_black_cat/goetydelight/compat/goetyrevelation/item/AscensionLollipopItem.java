package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import static org.apache.commons.lang3.math.IEEE754rUtils.max;

public class AscensionLollipopItem extends Item {
    /** 喂食插件冷却：5 秒。 */
    private static final long FEED_COOLDOWN_TICKS = 100L;

    /** 玩家 UUID -> 冷却结束的游戏刻。仅用于精妙背包/旅行者背包的自动喂食。 */
    private static final Map<UUID, Long> FEED_COOLDOWN_UNTIL = new ConcurrentHashMap<>();

    public AscensionLollipopItem(Properties properties) {
        super(properties);
    }

    /** 自动喂食冷却是否还在生效。 */
    public static boolean isFeedOnCooldown(Player player) {
        Long until = FEED_COOLDOWN_UNTIL.get(player.getUUID());
        if (until == null) {
            return false;
        }
        long now = player.level().getGameTime();
        if (now >= until) {
            FEED_COOLDOWN_UNTIL.remove(player.getUUID(), until);
            return false;
        }
        return true;
    }

    /** 自动喂食成功后开始 5 秒冷却。 */
    public static void startFeedCooldown(Player player) {
        FEED_COOLDOWN_UNTIL.put(player.getUUID(),
                player.level().getGameTime() + FEED_COOLDOWN_TICKS);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (stack.isEdible()) {
            level.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    entity.getEatingSound(stack),
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
            );
            FoodProperties food = stack.getFoodProperties(entity);
            if (food != null) {
                for (var pair : food.getEffects()) {
                    MobEffectInstance effect = pair.getFirst();
                    float chance = pair.getSecond();
                    if (!level.isClientSide && effect != null && level.random.nextFloat() < chance) {
                        entity.addEffect(new MobEffectInstance(effect));
                    }
                }
                if (entity instanceof Player player) {
                    FoodData data = player.getFoodData();
                    float current = data.getSaturationLevel();
                    if (Float.isNaN(current) || Float.isInfinite(current) || current < 0) {
                        data.setSaturation(0.0F);
                    }
                    data.setSaturation(max(data.getSaturationLevel(),20.0F));
                }
            }
            entity.gameEvent(GameEvent.EAT);
        }

        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (level.isClientSide) {
            return target instanceof Player ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!(target instanceof Player targetPlayer)) {
            return InteractionResult.PASS;
        }

        FoodData data = targetPlayer.getFoodData();
        data.setFoodLevel(0);
        data.setSaturation(0.0F);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.goetydelight.ascension_lollipop.tooltip.1")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.ascension_lollipop.tooltip.2")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.tooltip.shift"));
        }
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.goetydelight.ascension_lollipop.tooltip.3")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.goetydelight.ascension_lollipop.tooltip.4")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(ChatFormatting.ITALIC));
    }
}
