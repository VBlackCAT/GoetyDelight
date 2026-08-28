package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModEnchantments;
import net.v_black_cat.goetydelight.util.SearchServant;

import java.util.Map;
import java.util.Optional;
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
    public static void onFrostAspect(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;
        LivingEntity target = event.getEntity();

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
            // 【优化】先做廉价的耐久检查，再查附魔等级（避免对每格物品都做注册表/组件查询）
            if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                int level = stack.getEnchantmentLevel(holder);
                if (level > 0) {
                    repairItemWithSoulEnergy(player, stack, level);
                }
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

    @SubscribeEvent
    public static void onSoulMendingServantTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % 4 != 0) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 处理仆人装备
        Optional<SearchServant.ServantData> servantDataOpt = SearchServant.getServantData(serverPlayer);
        if (servantDataOpt.isPresent()) {
            SearchServant.ServantData servantData = servantDataOpt.get();
            ServerLevel level = (ServerLevel) serverPlayer.level();
            RegistryAccess registryAccess = level.registryAccess();
            Holder<Enchantment> holder = getHolder(registryAccess, ModEnchantments.SOUL_MENDING);

            for (UUID servantUUID : servantData.servantUUIDs) {
                Entity entity = level.getEntity(servantUUID);
                if (entity instanceof LivingEntity servant) {
                    // 处理不同类型的所有者
                    Player owner = null;
                    if (servant instanceof IOwned owned && owned.getTrueOwner() instanceof Player p) {
                        owner = p;
                    } else if (servant instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player p) {
                        owner = p;
                    }

                    if (owner != null) {
                        for (ItemStack stack : servant.getAllSlots()) {
                            // 【优化】廉价耐久检查前置，且只查一次附魔等级
                            if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                                int enchantLevel = stack.getEnchantmentLevel(holder);
                                if (enchantLevel > 0) {
                                    repairItemWithSoulEnergy(owner, stack, enchantLevel);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // === Soul Affix ===
    @SubscribeEvent
    public static void onSoulAffix(LivingIncomingDamageEvent event) {
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
            float original = event.getAmount();
            event.setAmount(original + totalLevel);
            SEHelper.decreaseSouls(player, cost);
        }
    }

    // === Soul Drain ===
    @SubscribeEvent
    public static void onSoulDrain(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack weapon = player.getMainHandItem();
        RegistryAccess registryAccess = player.level().registryAccess();
        Holder<Enchantment> holder = getHolder(registryAccess, ModEnchantments.SOUL_DRAIN);
        int enchantmentLevel = weapon.getEnchantmentLevel(holder);

        if (enchantmentLevel <= 0) return;

        LivingEntity target = event.getEntity();

        // 初始化追踪器
        UUID playerId = player.getUUID();
        playerDrainTracker.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());

        // 计算基础伤害（与1.20.1逻辑一致，基于武器的基础攻击力）
        float baseDamage = calculateBaseWeaponDamage(weapon);
        if (baseDamage <= 0) {
            baseDamage = 1.0f;
        }

        // 计算伤害加成
        int targetSouls = SEHelper.getSoulGiven(target);
        float maxBonusDamage = baseDamage * enchantmentLevel;
        float damageBonus = Math.min((float) targetSouls, maxBonusDamage);

        event.setAmount(event.getAmount() + damageBonus);

        // 处理灵魂吸取逻辑
        UUID targetId = target.getUUID();
        Map<UUID, Integer> targetMap = playerDrainTracker.get(playerId);
        if (targetMap == null) return;

        int currentDrainCount = targetMap.getOrDefault(targetId, 0);
        int maxDrainCount = (int) (0.5 + 1.5 * enchantmentLevel);

        if (currentDrainCount < maxDrainCount && targetSouls > 0) {
            double soulDrainPercent = 0.10 + 0.05 * enchantmentLevel;
            int drainAmount = (int) Math.ceil(targetSouls * soulDrainPercent);
            SEHelper.increaseSouls(player, drainAmount);

            targetMap.put(targetId, currentDrainCount + 1);
        }
    }

    // 清理死亡实体的追踪数据
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        UUID targetId = event.getEntity().getUUID();
        playerDrainTracker.forEach((playerId, targetMap) -> targetMap.remove(targetId));
    }

    private static float calculateBaseWeaponDamage(ItemStack weapon) {
        if (weapon.isEmpty()) return 0;
        return (float) weapon.getAttributeModifiers()
                .modifiers()
                .stream()
                .filter(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE))
                .mapToDouble(entry -> entry.modifier().amount())
                .sum();
    }
}