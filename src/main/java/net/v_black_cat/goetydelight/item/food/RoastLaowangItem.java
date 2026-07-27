package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.v_black_cat.goetydelight.init.ModItems;

import java.util.Random;

@EventBusSubscriber(modid = "goetydelight")
public class RoastLaowangItem extends Item {

    public RoastLaowangItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    private static final String BAKATAG = "bakatag";
    private static final String BAKA_TIME_TAG = "bakatime";
    private static final String BAKA_START_TAG = "bakatime_start";
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.PIG && !entity.isBaby()) {
            DamageSource source = event.getSource();
            if (source.is(DamageTypes.LIGHTNING_BOLT) || source.getMsgId().contains("lightning")) {
                if (random.nextFloat() < 0.50f) {
                    entity.spawnAtLocation(new ItemStack(ModItems.ROAST_LAOWANG.get()));
                }
            }
        }
        if (entity instanceof Player player && "laowang237".equals(player.getName().getString())) {
            player.spawnAtLocation(new ItemStack(ModItems.ROAST_LAOWANG.get()));
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof RoastLaowangItem) {
            LivingEntity entity = event.getEntity();
            CompoundTag tag = entity.getPersistentData();
            tag.putBoolean(BAKATAG, true);
            tag.putLong(BAKA_TIME_TAG, 200);
            tag.putLong(BAKA_START_TAG, entity.level().getGameTime());
        }
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (Player player : server.getPlayerList().getPlayers()) {
            CompoundTag tag = player.getPersistentData();
            if (tag.getBoolean(BAKATAG)) {
                long start = tag.getLong(BAKA_START_TAG);
                long duration = tag.getLong(BAKA_TIME_TAG);
                if (player.level().getGameTime() - start >= duration) {
                    tag.remove(BAKATAG);
                    tag.remove(BAKA_TIME_TAG);
                    tag.remove(BAKA_START_TAG);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        boolean flag = false; // 原逻辑中始终 false，保留
        if (!flag) return;

        LivingEntity entity = event.getEntity();
        if (entity.getPersistentData().getBoolean(BAKATAG)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.getPersistentData().getBoolean(BAKATAG)) {
                event.setCanceled(true);
            }
        }
    }
}