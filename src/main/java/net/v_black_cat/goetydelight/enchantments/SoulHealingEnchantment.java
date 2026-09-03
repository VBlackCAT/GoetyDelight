package net.v_black_cat.goetydelight.enchantments;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.config.Config;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class SoulHealingEnchantment extends Enchantment {

    public static final EnchantmentCategory CHESTPLATE = EnchantmentCategory.create("CHESTPLATE",
            item -> item instanceof ArmorItem && ((ArmorItem) item).getEquipmentSlot() == EquipmentSlot.CHEST);

    public SoulHealingEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, CHESTPLATE, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 30 + (enchantmentLevel - 1) * 9;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return this.getMinCost(enchantmentLevel) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasureOnly() {
        if(Config.isSoulHealingDisabled()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isTradeable() {
        if(Config.isSoulHealingDisabled()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        if(Config.isSoulHealingDisabled()){
            return false;
        }
        return true;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if(Config.isSoulHealingDisabled()){
            return false;
        }
        if (Config.getSoulHealingBlacklist().contains(stack.getItem())){
            return false;
        }

        return stack.getItem() instanceof ArmorItem &&
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }


    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    public @NotNull Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench) && ench != Enchantments.MENDING;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.tickCount % 10 == 0) {
            if(event.side.isServer()){
                Player player = event.player;
                ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
                if (!chestArmor.isEmpty()) {
                    int enchantmentLevel = chestArmor.getEnchantmentLevel(ModEnchantments.SOUL_HEALING.get());
                    if (enchantmentLevel > 0) {
                        healPlayerWithSoulEnergy(player, enchantmentLevel);
                    }
                }
            }
        }
    }

    private static void healPlayerWithSoulEnergy(Player player, int enchantmentLevel) {
        if (player.getHealth() < player.getMaxHealth()) {
            float maxHealth = player.getMaxHealth();
            float healAmount = (0.5F+0.005F*maxHealth) * enchantmentLevel;
            int soulEnergyCost = 5 * enchantmentLevel;

            if (healAmount > 0.5F*maxHealth){
                healAmount = 0.5F*maxHealth;
            }

            if (SEHelper.getSoulsAmount(player,soulEnergyCost)) {
                player.heal(healAmount);
                SEHelper.decreaseSouls(player, soulEnergyCost);
            }
        }
    }

}




