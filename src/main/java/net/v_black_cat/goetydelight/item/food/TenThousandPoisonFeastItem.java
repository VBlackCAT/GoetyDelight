package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.entities.boss.Vizier;
import net.minecraft.core.Holder;          // ★ 添加导入
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModConfig;
import net.v_black_cat.goetydelight.init.ModEffects;   // 确保存在
import net.v_black_cat.goetydelight.util.TickConverterUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static net.v_black_cat.goetydelight.util.TickConverterUtil.minToTick;
import static net.v_black_cat.goetydelight.util.TickConverterUtil.sToTick;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class TenThousandPoisonFeastItem extends BowlFoodItem {

    private static List<Holder<MobEffect>> cachedFilteredDebuffs = null;

    public TenThousandPoisonFeastItem(Properties properties) {
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
        // ★ 如果 ModEffects.BUFF 已注册为 DeferredHolder<MobEffect>，直接使用
        // ★ 若未注册，使用临时获取方式（示例使用 "goety:buff"）
        Holder<MobEffect> buffHolder = BuiltInRegistries.MOB_EFFECT
                .getHolder(ResourceLocation.parse("goety:buff"))
                .orElseThrow(() -> new IllegalStateException("Buff effect not registered"));
        vizier.addEffect(new MobEffectInstance(
                buffHolder,          // 传入 Holder
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
        player.displayClientMessage(
                Component.translatable("message.goetydelight.ten_thousand_poison_feast.vizier"),
                true
        );
    }

    private static List<Holder<MobEffect>> getFilteredDebuffEffects() {
        if (cachedFilteredDebuffs == null) {
            cacheFilteredDebuffEffects();
        }
        return cachedFilteredDebuffs != null ? cachedFilteredDebuffs : Collections.emptyList();
    }

    private static void cacheFilteredDebuffEffects() {
        boolean useWhitelist = ModConfig.isTenThousandPoisonFeastUseWhitelist();
        Map<ResourceLocation, int[]> levelConfig = ModConfig.getTenThousandPoisonFeastLevelConfig();
        Map<ResourceLocation, double[]> durationConfig = ModConfig.getTenThousandPoisonFeastDurationConfig();

        cachedFilteredDebuffs = BuiltInRegistries.MOB_EFFECT.holders()
                .filter(holder -> holder.value().getCategory() == MobEffectCategory.HARMFUL)
                .filter(holder -> {
                    ResourceLocation effectId = holder.unwrapKey()
                            .map(ResourceKey::location)
                            .orElse(null);
                    if (effectId == null) return false;
                    if (levelConfig.containsKey(effectId) || durationConfig.containsKey(effectId)) {
                        return true;
                    }
                    boolean isInFilterList = ModConfig.isEffectInFilterList(effectId);
                    return useWhitelist == isInFilterList;
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
        List<Holder<MobEffect>> debuffHolders = getFilteredDebuffEffects();
        if (debuffHolders.isEmpty()) {
            return;
        }

        int effectCount = ModConfig.getTenThousandPoisonFeastEffectCount();
        Map<ResourceLocation, int[]> levelConfig = ModConfig.getTenThousandPoisonFeastLevelConfig();
        Map<ResourceLocation, double[]> durationConfig = ModConfig.getTenThousandPoisonFeastDurationConfig();

        int defaultMinLevel = ModConfig.getTenThousandPoisonFeastDefaultMinLevel();
        int defaultMaxLevel = ModConfig.getTenThousandPoisonFeastDefaultMaxLevel();
        double defaultMinDuration = ModConfig.getTenThousandPoisonFeastDefaultMinDuration();
        double defaultMaxDuration = ModConfig.getTenThousandPoisonFeastDefaultMaxDuration();

        RandomSource random = entity.getRandom();

        List<Holder<MobEffect>> shuffled = new ArrayList<>(debuffHolders);
        for (int i = shuffled.size() - 1; i > 0; i--) {
            Collections.swap(shuffled, i, random.nextInt(i + 1));
        }

        int appliedCount = 0;
        for (Holder<MobEffect> holder : shuffled) {
            if (appliedCount >= effectCount) {
                break;
            }
            ResourceLocation effectId = holder.unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(null);
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

            double minDuration, maxDuration;
            if (durationConfig.containsKey(effectId)) {
                double[] range = durationConfig.get(effectId);
                minDuration = range[0];
                maxDuration = range[1];
            } else {
                minDuration = defaultMinDuration;
                maxDuration = defaultMaxDuration;
            }

            int level = minLevel + (maxLevel > minLevel ? random.nextInt(maxLevel - minLevel + 1) : 0);
            double randomDuration = minDuration + (maxDuration > minDuration ?
                    random.nextDouble() * (maxDuration - minDuration) : 0);
            int durationTicks = ModConfig.minutesToTicks(randomDuration);
            durationTicks = Math.max(1, durationTicks);

            if (entity.addEffect(new MobEffectInstance(
                    holder,
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