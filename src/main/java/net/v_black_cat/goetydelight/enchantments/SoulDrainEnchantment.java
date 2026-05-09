package net.v_black_cat.goetydelight.enchantments;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.config.Config;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class SoulDrainEnchantment extends Enchantment {
    private static final Map<UUID, Map<UUID, Integer>> playerDrainTracker = new ConcurrentHashMap<>();

    public SoulDrainEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
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
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.SOUL_AFFIX.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
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
            int enchantmentLevel = mainHandItem.getEnchantmentLevel(ModEnchantments.SOUL_DRAIN.get());

            if (enchantmentLevel > 0) {
                LivingEntity target = event.getEntity();
                applySoulDrain(event, player, target, mainHandItem, enchantmentLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            int enchantmentLevel = mainHandItem.getEnchantmentLevel(ModEnchantments.SOUL_DRAIN.get());

            if (enchantmentLevel > 0) {
                UUID playerId = player.getUUID();

                playerDrainTracker.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        UUID targetId = target.getUUID();

        playerDrainTracker.forEach((playerId, targetMap) -> {
            targetMap.remove(targetId);
        });
    }

    private static void applySoulDrain(LivingHurtEvent event, Player player, LivingEntity target, ItemStack weapon, int enchantmentLevel) {
        float baseDamage = (float) weapon.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                .stream()
                .mapToDouble(modifier -> modifier.getAmount())
                .sum();

        if (baseDamage <= 0) {
            baseDamage = 1.0f;
        }

        int targetSouls = SEHelper.getSoulGiven(target);
        float maxBonusDamage = baseDamage * enchantmentLevel;
        float damageBonus = Math.min((float) targetSouls, maxBonusDamage);

        event.setAmount(event.getAmount() + damageBonus);

        UUID playerId = player.getUUID();
        UUID targetId = target.getUUID();

        Map<UUID, Integer> targetMap = playerDrainTracker.get(playerId);
        if (targetMap == null) {
            return;
        }

        int currentDrainCount = targetMap.getOrDefault(targetId, 0);
        int maxDrainCount = (int) (0.5 + 1.5 * enchantmentLevel);

        if (currentDrainCount < maxDrainCount && targetSouls > 0) {
            double soulDrainPercent = 0.10 + 0.05 * enchantmentLevel;
            int drainAmount = (int) Math.ceil(targetSouls * soulDrainPercent);
            SEHelper.increaseSouls(player, drainAmount);

            targetMap.put(targetId, currentDrainCount + 1);
        }
    }
}
