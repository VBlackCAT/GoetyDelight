package net.v_black_cat.goetydelight.item.food;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.ThrowSoupPacket;

import java.util.Arrays;
import java.util.Random;

import static net.v_black_cat.goetydelight.util.TimeConverter.sToTick;

public class EternalRefusalOfBlackMeatSoupItem extends RejectedDarkMeatSoupItem {

    private static final String COOLDOWN_TAG = "Cooldown";
    private static final String LAST_USED_TAG = "LastUsed";

    public EternalRefusalOfBlackMeatSoupItem(Properties properties) {
        super(properties);
    }

    // 检查物品是否在冷却中
    public boolean isOnCooldown(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(COOLDOWN_TAG) && tag.contains(LAST_USED_TAG)) {
            long lastUsed = tag.getLong(LAST_USED_TAG);
            long cooldown = tag.getLong(COOLDOWN_TAG);
            return level.getGameTime() - lastUsed < cooldown;
        }
        return false;
    }

    // 设置物品冷却
    public void setCooldown(ItemStack stack, Level level, long cooldownTicks) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(COOLDOWN_TAG, cooldownTicks);
        tag.putLong(LAST_USED_TAG, level.getGameTime());
    }
    // 获取剩余冷却时间（ ticks）
    public long getRemainingCooldown(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(COOLDOWN_TAG) && tag.contains(LAST_USED_TAG)) {
            long lastUsed = tag.getLong(LAST_USED_TAG);
            long cooldown = tag.getLong(COOLDOWN_TAG);
            long elapsed = level.getGameTime() - lastUsed;
            return Math.max(0, cooldown - elapsed);
        }
        return 0;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            // 检查冷却
            if (isOnCooldown(stack, level)) {
                return stack;
            }

            applyConsumeEffects(player);

            // 设置物品冷却
            setCooldown(stack, level, 25 * 20);

            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return stack;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 检查冷却
            if (isOnCooldown(stack, player.level())) {
                return false;
            }

            throwPotion(stack, player.level(), player);

            // 设置物品冷却
            setCooldown(stack, player.level(), 10 * 20);
            return true;
        }
        return false;
    }

    @Override
    public void throwSoup(ItemStack stack, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 检查冷却
            if (isOnCooldown(stack, player.level())) {
                return;
            }

            throwPotion(stack, player.level(), player);

            // 设置物品冷却
            setCooldown(stack, player.level(), 10 * 20);
        }
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isOnCooldown(stack, world)) {
            // 如果在冷却中，不能开始吃
            return InteractionResultHolder.fail(stack);
        }else {
           return super.use(world, player, hand);
        }


    }

    private void applyConsumeEffects(Player player) {
        Random random = new Random();
        int randomAmplifier = Math.min(random.nextInt(6), 5);


        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, sToTick(15)));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, sToTick(15), randomAmplifier));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, sToTick(15), 1));
    }


    private void throwPotion(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {

            ItemStack throwStack = stack.copy();
            throwStack.setCount(1);


            int randomAmplifier = Math.min(level.random.nextInt(6), 5);
            PotionUtils.setCustomEffects(throwStack, Arrays.asList(
                    new MobEffectInstance(MobEffects.CONFUSION, sToTick(30)),
                    new MobEffectInstance(MobEffects.POISON, sToTick(30), randomAmplifier),
                    new MobEffectInstance(MobEffects.WEAKNESS, sToTick(30), 1)
            ));


            ThrownPotion thrownPotion = new ThrownPotion(level, player);
            thrownPotion.setItem(throwStack);
            thrownPotion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            level.addFreshEntity(thrownPotion);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Mod.EventBusSubscriber(modid = "goetydelight", value = Dist.CLIENT)
    public static class PlayerLeftClickHandler {

        @SubscribeEvent
        public static void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
            Player player = event.getEntity();
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof EternalRefusalOfBlackMeatSoupItem soupItem) {
                // 检查冷却
                if (soupItem.isOnCooldown(stack, player.level())) {
                    return;
                }

                NetworkHandler.sendToServer(new ThrowSoupPacket(player.getUUID()));

                // 设置物品冷却
                soupItem.setCooldown(stack, player.level(), 10 * 20);
            }else if (event instanceof PlayerInteractEvent.LeftClickEmpty) {

                if (stack.getItem() instanceof RejectedDarkMeatSoupItem) {
                    // 发送网络包通知服务器
                    NetworkHandler.sendToServer(new ThrowSoupPacket(player.getUUID()));
                }
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return isBarVisibleClient(stack);
        }
        return false;
    }
    @OnlyIn(Dist.CLIENT)
    private boolean isBarVisibleClient(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            return isOnCooldown(stack, level);
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return getBarWidthClient(stack);
        }
        return 0;
    }
    @OnlyIn(Dist.CLIENT)
    private int getBarWidthClient(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            long remaining = getRemainingCooldown(stack, level);
            long total = 60 * 20; // 25秒冷却时间
            return (int) (13.0 * (remaining / (double) total));
        }
        return 0;
    }


    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF0000;
    }
}