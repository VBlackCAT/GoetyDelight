package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.entities.boss.Vizier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.config.Config;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static net.v_black_cat.goetydelight.item.ModItems.BUFF_EFFECT_SUPPLIER;
import static net.v_black_cat.goetydelight.util.TimeConverter.minToTick;
import static net.v_black_cat.goetydelight.util.TimeConverter.sToTick;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TenThousandPoisonFeast extends BowlFoodItem {
    private static List<MobEffect> cachedFilteredDebuffs = null;
    public TenThousandPoisonFeast(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player,
                                                           @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!player.level().isClientSide) {
            if (target instanceof Vizier) {
                handleVizierInteraction(player, target);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else {
                applyRandomDebuffs(target, player);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide) {
            applyRandomDebuffs(livingEntity, livingEntity instanceof Player player ? player : null);
        }

        return result;
    }
    private void handleVizierInteraction(Player player, LivingEntity vizier) {
        vizier.addEffect(new MobEffectInstance(
                BUFF_EFFECT_SUPPLIER.get(),
                minToTick(1),
                9,
                false,
                true,
                true
        ));
        vizier.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                sToTick(30),
                2,
                false,
                true,
                true
        ));

        // 提示文本
        player.displayClientMessage(
                Component.translatable("message.goetydelight.ten_thousand_poison_feast.vizier"),
                true
        );
    }
    private static List<MobEffect> getFilteredDebuffEffects() {
        if (cachedFilteredDebuffs == null) {
            cacheFilteredDebuffEffects();
        }
        return cachedFilteredDebuffs != null ? cachedFilteredDebuffs : Collections.emptyList();
    }
    private static void cacheFilteredDebuffEffects() {
        boolean useWhitelist = Config.isTenThousandPoisonFeastUseWhitelist();
        Map<ResourceLocation, int[]> levelConfig = Config.getTenThousandPoisonFeastLevelConfig();
        Map<ResourceLocation, double[]> durationConfig = Config.getTenThousandPoisonFeastDurationConfig();

        cachedFilteredDebuffs = ForgeRegistries.MOB_EFFECTS.getValues().stream()
                .filter(effect -> effect.getCategory() == MobEffectCategory.HARMFUL)
                .filter(effect -> {
                    ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    if (effectId == null) return false;
                    if (levelConfig.containsKey(effectId) || durationConfig.containsKey(effectId)) {
                        return true;
                    }
                    boolean isInFilterList = Config.isEffectInFilterList(effectId);
                    if (useWhitelist) {
                        return isInFilterList;
                    } else {
                        return !isInFilterList;
                    }
                })
                .collect(Collectors.toList());
        GoetyDelight.LOGGER.info("Cached {} filtered harmful effects for Ten Thousand Poison Feast (Mode: {})",
                cachedFilteredDebuffs.size(), useWhitelist ? "Whitelist" : "Blacklist");
    }
    private static void clearDebuffCache() {
        cachedFilteredDebuffs = null;
    }
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        cacheFilteredDebuffEffects();
    }
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        clearDebuffCache();
    }
    private void applyRandomDebuffs(LivingEntity entity, Player player) {
        List<MobEffect> debuffs = getFilteredDebuffEffects();
        if (debuffs.isEmpty()) {
            return;
        }
        int effectCount = Config.getTenThousandPoisonFeastEffectCount();
        Map<ResourceLocation, int[]> levelConfig = Config.getTenThousandPoisonFeastLevelConfig();
        Map<ResourceLocation, double[]> durationConfig = Config.getTenThousandPoisonFeastDurationConfig();

        int defaultMinLevel = Config.getTenThousandPoisonFeastDefaultMinLevel();
        int defaultMaxLevel = Config.getTenThousandPoisonFeastDefaultMaxLevel();
        double defaultMinDuration = Config.getTenThousandPoisonFeastDefaultMinDuration();
        double defaultMaxDuration = Config.getTenThousandPoisonFeastDefaultMaxDuration();

        Random random = new Random();

        List<MobEffect> shuffledDebuffs = new ArrayList<>(debuffs);
        Collections.shuffle(shuffledDebuffs, random);

        int appliedCount = 0;
        for (MobEffect effect : shuffledDebuffs) {
            if (appliedCount >= effectCount) {
                break;
            }
            ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (effectId == null) {
                continue;
            }
            int minLevel, maxLevel;
            if (levelConfig.containsKey(effectId)) {
                int[] range = levelConfig.get(effectId);
                minLevel = range[0];
                maxLevel = range[1];
            } else {
                minLevel = defaultMinLevel;
                maxLevel = defaultMaxLevel;
            }

            double minDurationMin, maxDurationMin;
            if (durationConfig.containsKey(effectId)) {
                double[] range = durationConfig.get(effectId);
                minDurationMin = range[0];
                maxDurationMin = range[1];
            } else {
                minDurationMin = defaultMinDuration;
                maxDurationMin = defaultMaxDuration;
            }

            int level = minLevel + (maxLevel > minLevel ? random.nextInt(maxLevel - minLevel + 1) : 0);

            double randomDurationMin = minDurationMin + (maxDurationMin > minDurationMin ?
                    random.nextDouble() * (maxDurationMin - minDurationMin) : 0);
            int durationTicks = Config.minutesToTicks(randomDurationMin);

            durationTicks = Math.max(1, durationTicks);

            if (entity.addEffect(new MobEffectInstance(
                    effect,
                    durationTicks,
                    level,
                    false,
                    true,
                    true
            ))) {
                appliedCount++;
            }
        }
    }
}