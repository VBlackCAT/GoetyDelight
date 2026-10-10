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
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.v_black_cat.goetydelight.init.ModServerConfig;
import net.v_black_cat.goetydelight.init.ModEffects;   // 确保存在
import net.v_black_cat.goetydelight.util.TickConverterUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static net.v_black_cat.goetydelight.util.TickConverterUtil.minToTick;
import static net.v_black_cat.goetydelight.util.TickConverterUtil.sToTick;

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
        List<Holder<MobEffect>> cached = cachedFilteredDebuffs;
        if (cached == null) {
            // 兜底：进存档前被调用（正常不会发生，onServerStarted 已建好缓存）
            cached = buildFilteredDebuffEffects();
            cachedFilteredDebuffs = cached;
        }
        return cached;
    }

    /** 真正的读取点：扫一遍效果注册表 + 读当前配置。只在上面三个时机被调用，本身不打日志 */
    private static List<Holder<MobEffect>> buildFilteredDebuffEffects() {
        boolean useWhitelist = ModServerConfig.isTenThousandPoisonFeastUseWhitelist();
        Map<ResourceLocation, int[]> levelConfig = ModServerConfig.getTenThousandPoisonFeastLevelConfig();
        Map<ResourceLocation, double[]> durationConfig = ModServerConfig.getTenThousandPoisonFeastDurationConfig();

        return BuiltInRegistries.MOB_EFFECT.holders()
                .filter(holder -> holder.value().getCategory() == MobEffectCategory.HARMFUL)
                .filter(holder -> {
                    ResourceLocation effectId = holder.unwrapKey().map(ResourceKey::location).orElse(null);
                    if (effectId == null) return false;
                    // 显式配了等级/时长的效果无条件放行，其余按白/黑名单过滤
                    if (levelConfig.containsKey(effectId) || durationConfig.containsKey(effectId)) {
                        return true;
                    }
                    boolean isInFilterList = ModServerConfig.isEffectInFilterList(effectId);
                    return useWhitelist == isInFilterList;
                })
                .collect(Collectors.toList());
    }

    /** ① 进入存档 / 服务器启动时读取一次（游戏总线） */
    public static void onServerStarted(ServerStartedEvent event) {
        cachedFilteredDebuffs = buildFilteredDebuffEffects();
    }

    /** 退出存档时丢弃（游戏总线） */
    public static void onServerStopped(ServerStoppedEvent event) {
        cachedFilteredDebuffs = null;
    }

    /** ② 配置发生变化后丢弃缓存，下次使用时重建（MOD 总线；只处理本模组自己的配置） */
    public static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != ModServerConfig.SPEC) {
            return;
        }
        cachedFilteredDebuffs = null;
    }

    private void applyRandomDebuffs(LivingEntity entity, Player player) {
        List<Holder<MobEffect>> debuffHolders = getFilteredDebuffEffects();
        if (debuffHolders.isEmpty()) {
            return;
        }

        int effectCount = ModServerConfig.getTenThousandPoisonFeastEffectCount();
        Map<ResourceLocation, int[]> levelConfig = ModServerConfig.getTenThousandPoisonFeastLevelConfig();
        Map<ResourceLocation, double[]> durationConfig = ModServerConfig.getTenThousandPoisonFeastDurationConfig();

        int defaultMinLevel = ModServerConfig.getTenThousandPoisonFeastDefaultMinLevel();
        int defaultMaxLevel = ModServerConfig.getTenThousandPoisonFeastDefaultMaxLevel();
        double defaultMinDuration = ModServerConfig.getTenThousandPoisonFeastDefaultMinDuration();
        double defaultMaxDuration = ModServerConfig.getTenThousandPoisonFeastDefaultMaxDuration();

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
            int durationTicks = ModServerConfig.minutesToTicks(randomDuration);
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