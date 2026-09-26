package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModEnchantments;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class EnchantmentEffectHandlers {
    private static final Map<UUID, Map<UUID, Integer>> playerDrainTracker = new ConcurrentHashMap<>();

    // 辅助方法：将 ResourceKey 转为 Holder
    private static net.minecraft.core.Holder<Enchantment> getHolder(RegistryAccess registryAccess, ResourceKey<Enchantment> key) {
        return registryAccess.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }

    // === Frost Aspect ===
    @SubscribeEvent
    public static void onFrostAspect(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        RegistryAccess registryAccess = attacker.level().registryAccess();
        int level = attacker.getMainHandItem().getEnchantmentLevel(getHolder(registryAccess, ModEnchantments.FROST_ASPECT));
        if (level <= 0) return;

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 + (level - 1) * 15, level));

        if (attacker.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    target.getX(), target.getY() + target.getEyeHeight(), target.getZ(),
                    10 + level * 5, 0.5, 0.5, 0.5, 0.1);
        }
    }

    // === Soul Mending ===
    @SubscribeEvent
    public static void onSoulMending(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % 4 != 0) return;

        RegistryAccess registryAccess = player.level().registryAccess();
        net.minecraft.core.Holder<Enchantment> holder = getHolder(registryAccess, ModEnchantments.SOUL_MENDING);

        for (ItemStack stack : player.getAllSlots()) {
            int level = stack.getEnchantmentLevel(holder);
            if (level > 0 && stack.isDamageableItem() && stack.getDamageValue() > 0) {
                repairItemWithSoulEnergy(player, stack, level);
            }
        }
    }

    private static void repairItemWithSoulEnergy(Player player, ItemStack stack, int level) {
        int currentDamage = stack.getDamageValue();
        int actualRepair = Math.min(level, currentDamage);
        int requiredSouls;
        if (level > 9) requiredSouls = 1;
        else if (actualRepair < level) requiredSouls = Math.max(1, 5 - level / 2);
        else requiredSouls = actualRepair * 5;

        if (requiredSouls <= 0 || SEHelper.getSoulsAmount(player, requiredSouls)) {
            if (requiredSouls > 0) SEHelper.decreaseSouls(player, requiredSouls);
            stack.setDamageValue(currentDamage - actualRepair);
        }
    }

    // === Soul Healing ===
    @SubscribeEvent
    public static void onSoulHealing(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % 10 != 0) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        RegistryAccess registryAccess = player.level().registryAccess();
        int level = chest.getEnchantmentLevel(getHolder(registryAccess, ModEnchantments.SOUL_HEALING));
        if (level <= 0 || player.getHealth() >= player.getMaxHealth()) return;

        float maxHealth = player.getMaxHealth();
        float healAmount = (1.0F + 0.01F * maxHealth) * level;
        if (healAmount > 0.5F * maxHealth) healAmount = 0.5F * maxHealth;
        int cost = 5 * level;

        if (SEHelper.getSoulsAmount(player, cost)) {
            player.heal(healAmount);
            SEHelper.decreaseSouls(player, cost);
        }
    }

    // === Soul Affix ===
    @SubscribeEvent
    public static void onSoulAffix(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        RegistryAccess registryAccess = player.level().registryAccess();
        net.minecraft.core.Holder<Enchantment> holder = getHolder(registryAccess, ModEnchantments.SOUL_AFFIX);

        int totalLevel = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            totalLevel += player.getItemBySlot(slot).getEnchantmentLevel(holder);
        }
        if (totalLevel <= 0) return;

        int cost = 5 * totalLevel;
        if (SEHelper.getSoulsAmount(player, cost)) {
            float original = event.getOriginalDamage();
            event.setNewDamage(original + totalLevel);
            SEHelper.decreaseSouls(player, cost);
        }
    }

    // === Soul Drain ===
    @SubscribeEvent
    public static void onSoulDrain(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();

        RegistryAccess registryAccess = player.level().registryAccess();
        int level = weapon.getEnchantmentLevel(getHolder(registryAccess, ModEnchantments.SOUL_DRAIN));
        if (level <= 0) return;

        LivingEntity target = event.getEntity();

        float drainBase = event.getOriginalDamage() * 0.2f;
        float damageBonus = drainBase * level;
        event.setNewDamage(event.getOriginalDamage() + damageBonus);

        UUID playerId = player.getUUID();
        UUID targetId = target.getUUID();
        Map<UUID, Integer> targetMap = playerDrainTracker.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        int count = targetMap.getOrDefault(targetId, 0);
        int maxCount = (int) (0.5 + 1.5 * level);

        if (count < maxCount) {
            int soulDrop = SEHelper.getSoulGiven(target);
            if (soulDrop > 0) {
                int drainAmount = (int) Math.ceil(soulDrop * (0.1 + 0.05 * level));
                SEHelper.increaseSouls(player, drainAmount);
                targetMap.put(targetId, count + 1);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        UUID targetId = event.getEntity().getUUID();
        playerDrainTracker.forEach((playerId, targetMap) -> targetMap.remove(targetId));
    }
}