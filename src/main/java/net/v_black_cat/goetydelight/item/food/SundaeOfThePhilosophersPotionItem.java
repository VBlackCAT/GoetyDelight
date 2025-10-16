package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class SundaeOfThePhilosophersPotionItem extends Item {

    
    private static final String MINING_SPEED_BOOST_TAG = "PhilosopherSundaeMiningSpeedBoost";
    private static final String MAGIC_RESISTANCE_BOOST_TAG = "PhilosopherSundaeMagicResistanceBoost";

    
    private static final int MAX_MINING_BOOST_COUNT = 3;
    private static final int MAX_MAGIC_RESISTANCE_COUNT = 1;

    public SundaeOfThePhilosophersPotionItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide && livingEntity instanceof Player player) {
            
            applyMiningSpeedBoost(player);

            
            applyMagicResistanceBoost(player);
        }

        return result;
    }

    
    private void applyMiningSpeedBoost(Player player) {
        int currentCount = getMiningSpeedBoostCount(player);
        if (currentCount < MAX_MINING_BOOST_COUNT) {
            
            player.getPersistentData().putInt(MINING_SPEED_BOOST_TAG, currentCount + 1);
        }
    }

    
    private void applyMagicResistanceBoost(Player player) {
        int currentCount = getMagicResistanceBoostCount(player);
        if (currentCount < MAX_MAGIC_RESISTANCE_COUNT) {
            
            player.getPersistentData().putInt(MAGIC_RESISTANCE_BOOST_TAG, currentCount + 1);
        }
    }

    
    public static int getMiningSpeedBoostCount(Player player) {
        return player.getPersistentData().getInt(MINING_SPEED_BOOST_TAG);
    }

    
    public static int getMagicResistanceBoostCount(Player player) {
        return player.getPersistentData().getInt(MAGIC_RESISTANCE_BOOST_TAG);
    }

    
    @Mod.EventBusSubscriber
    public static class MiningSpeedHandler {
        @SubscribeEvent
        public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
            Player player = event.getEntity();
            int boostCount = getMiningSpeedBoostCount(player);

            if (boostCount > 0) {
                
                Block block = event.getState().getBlock();

                
                if (isBreakableBlock(block)) {
                    
                    float originalSpeed = event.getOriginalSpeed();
                    float boostedSpeed = originalSpeed * (1.0f + 0.1f * boostCount);
                    event.setNewSpeed(boostedSpeed);
                }
            }
        }

        
        private static boolean isBreakableBlock(Block block) {
            
            
            
            return true;
        }
    }

    
    @Mod.EventBusSubscriber
    public static class MagicResistanceHandler {
        @SubscribeEvent
        public static void onMagicDamage(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player player) {
                int resistanceCount = getMagicResistanceBoostCount(player);

                
                if (resistanceCount > 0 && isMagicDamage(event.getSource())) {
                    
                    float originalDamage = event.getAmount();
                    float reducedDamage = originalDamage * 0.5f;
                    event.setAmount(reducedDamage);
                }
            }
        }

        
        private static boolean isMagicDamage(net.minecraft.world.damagesource.DamageSource source) {
            return source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC) ||
                    source.is(net.minecraft.world.damagesource.DamageTypes.WITHER) ||
                    source.is(net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH) ||
                    source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC);
        }
    }
}