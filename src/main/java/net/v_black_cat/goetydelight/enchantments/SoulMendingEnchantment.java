package net.v_black_cat.goetydelight.enchantments;
import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.config.Config;
import net.v_black_cat.goetydelight.util.SearchServant;

import java.util.List;

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
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        return false; 
    }

    @Override
    public boolean isTradeable() {
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        return true; 
    }

    @Override
    public boolean isDiscoverable() {
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        return true; 
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.isDamageableItem(); 
    }

    @Override
    public Rarity getRarity() {
       return Rarity.VERY_RARE;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        if (Config.getSoulMendingBlacklist().contains(stack.getItem())){
            return false;
        }

        return stack.isDamageableItem();
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.tickCount % 4 == 0 && !event.player.level().isClientSide()) {
            if (event.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) serverPlayer.level();

                SearchServant.scanServantsForPlayer(level, serverPlayer);

                java.util.Optional<SearchServant.ServantData> servantDataOpt = SearchServant.getServantData(serverPlayer);
                if (servantDataOpt.isPresent()) {
                    SearchServant.ServantData servantData = servantDataOpt.get();

                    for (ItemStack stack : serverPlayer.getAllSlots()) {
                        int enchantmentLevel = stack.getEnchantmentLevel(ModEnchantments.SOUL_MENDING.get());
                        if (enchantmentLevel > 0) {
                            repairItemWithSoulEnergy(serverPlayer, stack, enchantmentLevel);
                        }
                    }

                    for (java.util.UUID servantUUID : servantData.servantUUIDs) {
                        Entity entity = level.getEntity(servantUUID);
                        if (entity instanceof LivingEntity servant) {
                            if (servant instanceof IOwned owned && owned.getTrueOwner() instanceof Player owner) {
                                for (ItemStack stack : servant.getAllSlots()) {
                                    int enchantmentLevel = stack.getEnchantmentLevel(ModEnchantments.SOUL_MENDING.get());
                                    if (enchantmentLevel > 0) {
                                        repairItemWithSoulEnergy(owner, stack, enchantmentLevel);
                                    }
                                }
                            }else if (servant instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() instanceof Player owner) {
                                for (ItemStack stack : servant.getAllSlots()) {
                                    int enchantmentLevel = stack.getEnchantmentLevel(ModEnchantments.SOUL_MENDING.get());
                                    if (enchantmentLevel > 0) {
                                        repairItemWithSoulEnergy(owner, stack, enchantmentLevel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void repairItemWithSoulEnergy(Player player, ItemStack stack, int enchantmentLevel) {
        
        int currentDamage = stack.getDamageValue();

        if (currentDamage <= 0) return;

        int actualRepair = Math.min(enchantmentLevel, currentDamage);

        int requiredSouls;
        if (enchantmentLevel > 9) {
            requiredSouls = 1;
        } else if (actualRepair < enchantmentLevel) {
            requiredSouls = Math.max(1, 5 - enchantmentLevel / 2);
        } else {
            requiredSouls = actualRepair * 5;
        }
        if (requiredSouls <= 0 || SEHelper.getSoulsAmount(player, requiredSouls)) {
            if (requiredSouls > 0) {
                SEHelper.decreaseSouls(player, requiredSouls);
            }
            int newDamage = currentDamage - actualRepair;
            stack.setDamageValue(newDamage);
        }
    }
}