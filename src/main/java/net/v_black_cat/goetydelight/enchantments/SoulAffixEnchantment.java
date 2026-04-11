package net.v_black_cat.goetydelight.enchantments;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import net.v_black_cat.goetydelight.config.Config;

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
        if(Config.isSoulAffixDisabled()){
            return false;
        }

        return true;
    }

    @Override
    public boolean isTradeable() {
        if(Config.isSoulAffixDisabled()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        if(Config.isSoulAffixDisabled()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isAllowedOnBooks() {
        if(Config.isSoulAffixDisabled()){
            return false;
        }
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        if(Config.isSoulAffixDisabled()){
            return false;
        }
        return  stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof net.minecraft.world.item.BowItem ||
                stack.getItem() instanceof net.minecraft.world.item.PickaxeItem ||
                stack.getItem() instanceof net.minecraft.world.item.ShieldItem ||
                stack.getItem() instanceof net.minecraft.world.item.CrossbowItem ||
                stack.getItem() instanceof net.minecraft.world.item.TridentItem ||
                stack.getItem() instanceof net.minecraft.world.item.AxeItem ||
                stack.getItem() instanceof net.minecraft.world.item.ShovelItem ||
                stack.getItem() instanceof net.minecraft.world.item.HoeItem ||
                stack.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem ||
                stack.getItem() instanceof net.minecraft.world.item.ShearsItem ||
                stack.getItem() instanceof vectorwing.farmersdelight.common.item.KnifeItem ||
                stack.getItem() instanceof com.Polarice3.Goety.common.items.magic.DarkWand ||
                stack.getItem() instanceof com.Polarice3.Goety.common.items.magic.DarkStaff;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offhandItem = new PlayerOffhandInvWrapper(player.getInventory()).getStackInSlot(0);
            ItemStack head_armorItem = player.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chest_armorItem = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack legs_armorItem = player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack feet_armorItem = player.getItemBySlot(EquipmentSlot.FEET);
//            Float damage;
//            if(mainHandItem.getAttributeModifiers( EquipmentSlot.MAINHAND).get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).stream().findFirst().isPresent()){
//             damage = (float) mainHandItem.getAttributeModifiers( EquipmentSlot.MAINHAND).get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).stream().findFirst().get().getAmount();
//            }else {damage = 1.0f;}
            if (!mainHandItem.isEmpty() || !offhandItem.isEmpty()  || !head_armorItem.isEmpty() || !chest_armorItem.isEmpty() || !legs_armorItem.isEmpty() || !feet_armorItem.isEmpty()) {
                int enchantmentLevel = mainHandItem.getEnchantmentLevel(ModEnchantments.SOUL_AFFIX.get()) +
                        offhandItem.getEnchantmentLevel(ModEnchantments.SOUL_AFFIX.get()) +
                        head_armorItem.getEnchantmentLevel(ModEnchantments.SOUL_AFFIX.get()) +
                        chest_armorItem.getEnchantmentLevel(ModEnchantments.SOUL_AFFIX.get()) +
                        legs_armorItem.getEnchantmentLevel(ModEnchantments.SOUL_AFFIX.get()) +
                        feet_armorItem.getEnchantmentLevel(ModEnchantments.SOUL_AFFIX.get());
                if (enchantmentLevel > 0) {
                    applySoulAffixDamage(event, player, enchantmentLevel);
                }
            }
        }
    }

    private static void applySoulAffixDamage(LivingHurtEvent event, Player player, int enchantmentLevel) {
        int soulEnergyCost = Config.getSoulAffixSoulCostPerLevel() * enchantmentLevel;
        if (SEHelper.getSoulsAmount(player, soulEnergyCost)) {
            double damagePerLevel = Config.getSoulAffixDamagePerLevel();
            event.setAmount((float) (event.getAmount() + (enchantmentLevel * damagePerLevel)));
            SEHelper.decreaseSouls(player, soulEnergyCost);
        }
    }
    @Override
    public boolean canEnchant(ItemStack stack) {
        if(Config.isSoulAffixDisabled()){
            return false;
        }
        return !Config.getSoulAffixBlacklist().contains(stack.getItem());
    }
}
