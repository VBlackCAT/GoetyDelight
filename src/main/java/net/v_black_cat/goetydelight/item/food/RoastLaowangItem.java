package net.v_black_cat.goetydelight.item.food;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.capability.FoodStateCapability;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.util.FoodState;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class RoastLaowangItem extends Item{
    public RoastLaowangItem(Item.Properties properties) {
        super(properties);
    }
    @Override
    public boolean isFoil(ItemStack pStack) {return true;}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().getType() == EntityType.PIG && !event.getEntity().isBaby()) {
            if (event.getSource().typeHolder().unwrapKey().isPresent()) {
                String damageTypeName = event.getSource().typeHolder().unwrapKey().get().location().toString();
                if (damageTypeName.contains("lightning") || damageTypeName.contains("shock")) {
                    if (event.getEntity().getRandom().nextFloat() < 0.50f) {
                        ItemStack roastLaowang = new ItemStack(ModItems.ROAST_LAOWANG.get());
                        event.getEntity().spawnAtLocation(roastLaowang);
                    }
                }
            }
        }
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            if(player.getName().getString().equals("laowang237")){
                if (!event.getSource().is(DamageTypeTags.IS_FIRE)) {
                ItemStack roastLaowang = new ItemStack(ModItems.ROAST_LAOWANG.get());
                player.spawnAtLocation(roastLaowang);}
                else {
                    ItemStack roastLaowangblock = new ItemStack(ModBlocks.ROAST_LAOWANG_BLOCK.get());
                    player.spawnAtLocation(roastLaowangblock);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof RoastLaowangItem) {
            LivingEntity entity = event.getEntity();
            FoodState state = FoodStateCapability.get(entity);
            if (state != null) {
                state.setRoastLaowangActive(true);
                state.setRoastLaowangStartTime(entity.level().getGameTime());
                state.setRoastLaowangDuration(200);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Level level = player.level();
        if (!level.isClientSide) {
            FoodState state = FoodStateCapability.get(player);
            if (state != null && state.isRoastLaowangActive()) {
                long currentTime = level.getGameTime();
                if (currentTime - state.getRoastLaowangStartTime() >= state.getRoastLaowangDuration()) {
                    state.setRoastLaowangActive(false);
                }
            }
        }
    }
}