package net.v_black_cat.goetydelight.item.food;

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
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.util.FoodState;

@EventBusSubscriber(modid = "goetydelight")
public class RoastLaowangItem extends Item {

    public RoastLaowangItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType() == EntityType.PIG && !entity.isBaby()) {
            DamageSource source = event.getSource();
            if (source.is(DamageTypes.LIGHTNING_BOLT) || source.getMsgId().contains("lightning")) {
                if (entity.getRandom().nextFloat() < 0.50f) {
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
            FoodState state = entity.getData(ModAttachments.FOOD_STATE);
            state.setRoastLaowangActive(true);
            state.setRoastLaowangStartTime(entity.level().getGameTime());
            state.setRoastLaowangDuration(200);
        }
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (Player player : server.getPlayerList().getPlayers()) {
            FoodState state = player.getData(ModAttachments.FOOD_STATE);
            if (state.isRoastLaowangActive()) {
                if (player.level().getGameTime() - state.getRoastLaowangStartTime() >= state.getRoastLaowangDuration()) {
                    state.setRoastLaowangActive(false);
                }
            }
        }
    }

}