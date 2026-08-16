package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.utils.LichdomHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.capability.FoodStateCapability;
import net.v_black_cat.goetydelight.util.FoodState;

public class SundaeOfThePhilosophersPotionItem extends Item {

    
    private static final int MAX_MINING_BOOST_COUNT = 3;
    private static final int MAX_MAGIC_RESISTANCE_COUNT = 1;

    public SundaeOfThePhilosophersPotionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (LichdomHelper.isLich(player)) {
            return super.use(level, player, usedHand);
        } else {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide && livingEntity instanceof Player player) {
            
            applyMiningSpeedBoost(player);

            
            applyMagicResistanceBoost(player);
        }

        if (livingEntity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return result;
            }

            if (result.isEmpty()) {
                return new ItemStack(Items.BOWL);
            } else if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                player.drop(new ItemStack(Items.BOWL), false);
            }
        }

        return result;
    }

    
    private void applyMiningSpeedBoost(Player player) {
        int currentCount = getMiningSpeedBoostCount(player);
        FoodState state = FoodStateCapability.get(player);
        if (state != null && currentCount < MAX_MINING_BOOST_COUNT) {
            state.setPhilosopherMiningBoost(currentCount + 1);
        }
    }

    
    private void applyMagicResistanceBoost(Player player) {
        int currentCount = getMagicResistanceBoostCount(player);
        FoodState state = FoodStateCapability.get(player);
        if (state != null && currentCount < MAX_MAGIC_RESISTANCE_COUNT) {
            state.setPhilosopherMagicResistance(currentCount + 1);
        }
    }

    
    public static int getMiningSpeedBoostCount(Player player) {
        FoodState state = FoodStateCapability.get(player);
        return state == null ? 0 : state.getPhilosopherMiningBoost();
    }

    
    public static int getMagicResistanceBoostCount(Player player) {
        FoodState state = FoodStateCapability.get(player);
        return state == null ? 0 : state.getPhilosopherMagicResistance();
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