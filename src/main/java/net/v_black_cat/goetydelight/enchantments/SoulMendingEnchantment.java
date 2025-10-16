package net.v_black_cat.goetydelight.enchantments;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class SoulMendingEnchantment extends Enchantment {

    public SoulMendingEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 15 + (enchantmentLevel - 1) * 9;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return this.getMinCost(enchantmentLevel) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 3; 
    }

    @Override
    public boolean isTreasureOnly() {
        return false; 
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.isDamageableItem(); 
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        
        return stack.isDamageableItem();
    }

    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.tickCount % 4 == 0) {
            Player player = event.player;

            
            for (ItemStack stack : player.getAllSlots()) {
                int enchantmentLevel = stack.getEnchantmentLevel(ModEnchantments.SOUL_MENDING.get());
                if (stack.isEnchanted() && enchantmentLevel > 0) {
                    repairItemWithSoulEnergy(player, stack, enchantmentLevel);
                }
            }
        }
    }

    private static void repairItemWithSoulEnergy(Player player, ItemStack stack, int enchantmentLevel) {
        
        int currentDamage = stack.getDamageValue();

        if (currentDamage <= 0) return;

        int repairAmount = enchantmentLevel;

        
        int actualRepair = Math.min(repairAmount, currentDamage);

        if (actualRepair<enchantmentLevel) return;
        
        int soulCostPerPoint = 5;
        int requiredSouls = actualRepair * soulCostPerPoint;

        
        if (SEHelper.getSoulsAmount(player, requiredSouls)) {
            SEHelper.decreaseSouls(player, requiredSouls);

            
            int newDamage = currentDamage - actualRepair;
            stack.setDamageValue(newDamage);
        }
    }
}