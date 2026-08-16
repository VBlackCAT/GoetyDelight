package net.v_black_cat.goetydelight.item.food;

import net.v_black_cat.goetydelight.capability.FoodStateCapability;
import net.v_black_cat.goetydelight.util.FoodState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class SevenLeafPuddingItem extends BowlFoodItem {

    // UUIDs for attribute modifiers
    private static final UUID ATTACK_DAMAGE_BONUS_UUID = UUID.fromString("d3b1a8c2-4e5f-6a7b-8c9d-0e1f2a3b4c5d");
    private static final UUID MOVEMENT_SPEED_BONUS_UUID = UUID.fromString("a5b4c3d2-1f0e-9a8b-7c6d-5e4f3a2b1c0d");

    // Duration in ticks (5 minutes)
    private static final int DURATION_TICKS = 20 * 60 * 5;  // 20 ticks/second * 60 seconds * 5 minutes

    public SevenLeafPuddingItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            FoodState state = FoodStateCapability.get(player);
            if (state != null) {
                // Remove existing bonuses if active
                if (state.isSevenLeafPuddingActive()) {
                    removeBonusAttributes(player);
                }

                // Add new bonuses
                addBonusAttributes(player);

                // Set activation data
                state.setSevenLeafPuddingActive(true);
                state.setSevenLeafPuddingActivationTime(level.getGameTime());
            }

            // Notify player
            //player.displayClientMessage(Component.literal("甜浆果布丁的效果被激活！获得3点攻击力和5%移动速度加成，持续5分钟。"), true);
        }

        return resultStack;
    }

    // Add attribute bonuses
    private void addBonusAttributes(Player player) {
        // Add attack damage bonus
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            AttributeModifier modifier = new AttributeModifier(
                    ATTACK_DAMAGE_BONUS_UUID,
                    "Seven Leaf Pudding Attack Bonus",
                    3.0,
                    AttributeModifier.Operation.ADDITION
            );
            attackDamage.addTransientModifier(modifier);
        }

        // Add movement speed bonus
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            AttributeModifier modifier = new AttributeModifier(
                    MOVEMENT_SPEED_BONUS_UUID,
                    "Seven Leaf Pudding Speed Bonus",
                    0.05,  // 5% increase
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            movementSpeed.addTransientModifier(modifier);
        }
    }

    // Remove attribute bonuses
    private void removeBonusAttributes(Player player) {
        // Remove attack damage bonus
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_BONUS_UUID) != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_BONUS_UUID);
        }

        // Remove movement speed bonus
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null && movementSpeed.getModifier(MOVEMENT_SPEED_BONUS_UUID) != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_BONUS_UUID);
        }

        // Clear state
        FoodState state = FoodStateCapability.get(player);
        if (state != null) {
            state.setSevenLeafPuddingActive(false);
            state.setSevenLeafPuddingActivationTime(0);
        }
    }

    // Check if bonus duration has expired
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        Level level = player.level();

        if (!level.isClientSide) {
            // 过期检查降频为每 20 tick（1秒）一次，避免每 tick 读 NBT
            if (player.tickCount % 20 != 0) return;

            FoodState state = FoodStateCapability.get(player);
            if (state != null && state.isSevenLeafPuddingActive()) {
                long activationTime = state.getSevenLeafPuddingActivationTime();
                long currentTime = level.getGameTime();

                // Check if duration has expired
                if (currentTime - activationTime >= DURATION_TICKS) {
                    // Remove bonuses
                    SevenLeafPuddingItem item = (SevenLeafPuddingItem) net.v_black_cat.goetydelight.item.ModItems.SEVEN_LEAF_PUDDING.get();
                    item.removeBonusAttributes(player);

                    // Notify player
//                    player.displayClientMessage(Component.literal("甜浆果布丁的效果已结束。"), true);
                }
            }
        }
    }

    // Remove bonuses on player death
    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player) {
            SevenLeafPuddingItem item = (SevenLeafPuddingItem) net.v_black_cat.goetydelight.item.ModItems.SEVEN_LEAF_PUDDING.get();
            FoodState state = FoodStateCapability.get(player);
            if (state != null && state.isSevenLeafPuddingActive()) {
                item.removeBonusAttributes(player);
            }
        }
    }

    // Clear data on player respawn
    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        FoodState state = FoodStateCapability.get(player);
        if (state != null) {
            state.setSevenLeafPuddingActive(false);
            state.setSevenLeafPuddingActivationTime(0);
        }
    }
}