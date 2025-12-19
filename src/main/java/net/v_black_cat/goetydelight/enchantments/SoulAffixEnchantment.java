package net.v_black_cat.goetydelight.enchantments;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class SoulAffixEnchantment extends Enchantment {
    public SoulAffixEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
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
        return 5;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (!mainHandItem.isEmpty()) {
                int enchantmentLevel = mainHandItem.getEnchantmentLevel(ModEnchantments.SOUL_AFFIX.get());
                if (enchantmentLevel > 0) {
                    applySoulAffixDamage(event, player, enchantmentLevel);
                }
            }
        }
    }

    private static void applySoulAffixDamage(LivingHurtEvent event, Player player, int enchantmentLevel) {
        int soulEnergyCost = 50 * enchantmentLevel;
        if (SEHelper.getSoulsAmount(player, soulEnergyCost)) {
            event.setAmount(event.getAmount() + enchantmentLevel);
            SEHelper.decreaseSouls(player, soulEnergyCost);
        }
    }
}
