package net.v_black_cat.goetydelight.item.food;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.v_black_cat.goetydelight.util.PayloadUtil;

import static net.v_black_cat.goetydelight.util.TickConverterUtil.sToTick;

public class EternalRefusalOfBlackMeatSoupItem extends RejectedDarkMeatSoupItem {

    // 冷却常量
    private static final long CONSUME_COOLDOWN = 25 * 20; // 25秒
    private static final long THROW_COOLDOWN = 10 * 20; // 10秒

    // NBT 键
    private static final String COOLDOWN_TAG = "EternalCooldown";
    private static final String LAST_USED_TAG = "EternalLastUsed";

    public EternalRefusalOfBlackMeatSoupItem(Properties properties) {
        super(properties);
    }

    // ----- 重写投掷方法，加入冷却 -----
    @Override
    public void throwSoup(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        Level level = player.level();
        if (isOnCooldown(stack, level)) return;

        // 调用父类投掷逻辑（父类已改为实例方法）
        super.throwSoup(stack, entity);

        // 设置投掷冷却
        setCooldown(stack, level, THROW_COOLDOWN);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (isOnCooldown(stack, level)) {
                return stack;
            }

            // 应用负面效果
            applyConsumeEffects(player);
            // 设置食用冷却
            setCooldown(stack, level, CONSUME_COOLDOWN);
            // 消耗物品
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }
        return stack;
    }

    private void applyConsumeEffects(Player player) {
        int randomAmplifier = player.getRandom().nextInt(6);
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, sToTick(15)));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, sToTick(15), randomAmplifier));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, sToTick(15), 1));
    }

    public boolean isOnCooldown(ItemStack stack, Level level) {
        CompoundTag tag = getCustomTag(stack);
        if (tag.contains(COOLDOWN_TAG) && tag.contains(LAST_USED_TAG)) {
            long lastUsed = tag.getLong(LAST_USED_TAG);
            long cooldown = tag.getLong(COOLDOWN_TAG);
            return level.getGameTime() - lastUsed < cooldown;
        }
        return false;
    }

    private void setCooldown(ItemStack stack, Level level, long cooldownTicks) {
        CompoundTag tag = getCustomTag(stack);
        tag.putLong(COOLDOWN_TAG, cooldownTicks);
        tag.putLong(LAST_USED_TAG, level.getGameTime());
        setCustomTag(stack, tag);
    }

    private long getRemainingCooldown(ItemStack stack, Level level) {
        CompoundTag tag = getCustomTag(stack);
        if (tag.contains(COOLDOWN_TAG) && tag.contains(LAST_USED_TAG)) {
            long lastUsed = tag.getLong(LAST_USED_TAG);
            long cooldown = tag.getLong(COOLDOWN_TAG);
            return Math.max(0, cooldown - (level.getGameTime() - lastUsed));
        }
        return 0;
    }

    private long getTotalCooldown(ItemStack stack) {
        CompoundTag tag = getCustomTag(stack);
        if (tag.contains(COOLDOWN_TAG)) {
            return tag.getLong(COOLDOWN_TAG);
        }
        return 0;
    }

    private CompoundTag getCustomTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private void setCustomTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        return level != null && isOnCooldown(stack, level);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return 0;
        long remaining = getRemainingCooldown(stack, level);
        long total = getTotalCooldown(stack);
        if (total == 0) return 0;
        return (int) (13.0 * (remaining / (double) total));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF0000;
    }

    @EventBusSubscriber(modid = "goetydelight", value = Dist.CLIENT)
    public static class ClientLeftClickHandler {
        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            Player player = event.getEntity();
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof EternalRefusalOfBlackMeatSoupItem) {
                Level level = player.level();
                EternalRefusalOfBlackMeatSoupItem item = (EternalRefusalOfBlackMeatSoupItem) stack.getItem();
                if (!item.isOnCooldown(stack, level)) {
                    PayloadUtil.sendClickAir(stack.getItem());
                }
            }
        }
    }
}