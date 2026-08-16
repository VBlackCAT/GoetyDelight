package net.v_black_cat.goetydelight.item.food;

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
import net.v_black_cat.goetydelight.capability.FoodStateCapability;
import net.v_black_cat.goetydelight.util.FoodState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class BoneLordAshRiceItem extends Item {

     
    private static final UUID ARMOR_BONUS_UUID = UUID.fromString("a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID ARMOR_TOUGHNESS_BONUS_UUID = UUID.fromString("d4c3b2a1-f6e5-b8a7-d0c9-f2e1b4a3c5d6");

     
    private static final int DURATION_TICKS = 20 * 60 * 5;  

    public BoneLordAshRiceItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
             
            FoodState state = FoodStateCapability.get(player);

             
            if (state.isBoneLordAshRiceActive()) {
                removeBonusAttributes(player);
            }

             
            addBonusAttributes(player);

             
            state.setBoneLordAshRiceActive(true);
            state.setBoneLordAshRiceActivationTime(level.getGameTime());

             
           // player.displayClientMessage(Component.literal("骨头领主骨灰拌饭的力量被激活！获得15点护甲和10点护甲韧性，持续5分钟。"), true);
        }

        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return resultStack;
            }

            if (resultStack.isEmpty()) {
                return new ItemStack(Items.BOWL);
            } else if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                player.drop(new ItemStack(Items.BOWL), false);
            }
        }

        return resultStack;
    }

     
    private void addBonusAttributes(Player player) {
        AttributeInstance armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null) {
            AttributeModifier modifier = new AttributeModifier(
                    ARMOR_BONUS_UUID,
                    "Bone Lord Ash Rice Armor Bonus",
                    15.0,
                    AttributeModifier.Operation.ADDITION
            );
            armorAttribute.addTransientModifier(modifier);
        }

        AttributeInstance toughnessAttribute = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughnessAttribute != null) {
            AttributeModifier modifier = new AttributeModifier(
                    ARMOR_TOUGHNESS_BONUS_UUID,
                    "Bone Lord Ash Rice Toughness Bonus",
                    10.0,
                    AttributeModifier.Operation.ADDITION
            );
            toughnessAttribute.addTransientModifier(modifier);
        }
    }

     
    private void removeBonusAttributes(Player player) {
        AttributeInstance armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null && armorAttribute.getModifier(ARMOR_BONUS_UUID) != null) {
            armorAttribute.removeModifier(ARMOR_BONUS_UUID);
        }

        AttributeInstance toughnessAttribute = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughnessAttribute != null && toughnessAttribute.getModifier(ARMOR_TOUGHNESS_BONUS_UUID) != null) {
            toughnessAttribute.removeModifier(ARMOR_TOUGHNESS_BONUS_UUID);
        }

         
        FoodState state = FoodStateCapability.get(player);
        state.setBoneLordAshRiceActive(false);
        state.setBoneLordAshRiceActivationTime(0);
    }

     
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        Level level = player.level();

        if (!level.isClientSide) {
            FoodState state = FoodStateCapability.get(player);

            if (state.isBoneLordAshRiceActive()) {
                long activationTime = state.getBoneLordAshRiceActivationTime();
                long currentTime = level.getGameTime();

                 
                if (currentTime - activationTime >= DURATION_TICKS) {
                     
                    BoneLordAshRiceItem item = (BoneLordAshRiceItem) net.v_black_cat.goetydelight.item.ModItems.BONE_LORD_ASH_RICE.get();
                    item.removeBonusAttributes(player);

                     
//                    player.displayClientMessage(Component.literal("骨头领主骨灰拌饭的效果已结束。"), true);
                }
            }
        }
    }

     
    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player) {
             
            BoneLordAshRiceItem item = (BoneLordAshRiceItem) net.v_black_cat.goetydelight.item.ModItems.BONE_LORD_ASH_RICE.get();
            if (FoodStateCapability.get(player).isBoneLordAshRiceActive()) {
                item.removeBonusAttributes(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
         
        FoodState state = FoodStateCapability.get(player);
        state.setBoneLordAshRiceActive(false);
        state.setBoneLordAshRiceActivationTime(0);
    }
}