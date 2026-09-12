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
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModEnchantments;
import net.v_black_cat.goetydelight.util.SearchServant;
import net.v_black_cat.goetydelight.util.SoulEnchantUtil;

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

    // === Soul Mending / Soul Healing ===
    // 玩家自身：已改为「物品自报」（见 ItemStackMixin + SoulEnchantUtil#onInventoryTick），
    // 不再每 4/10 tick 扫描 getAllSlots() 或查询胸甲附魔。

    // 仆人装备：不在 Inventory 里、不会被 vanilla tick，保留周期驱动；
    // 频率由每 4 tick 降到每 20 tick（一次补足 5 次修复，速率不变），并去掉 getAllSlots() 的列表分配。
    @SubscribeEvent
    public static void onSoulMendingServantTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % SoulEnchantUtil.SERVANT_CHECK_INTERVAL_TICKS != 0) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Optional<SearchServant.ServantData> servantDataOpt = SearchServant.getServantData(serverPlayer);
        if (servantDataOpt.isEmpty()) return;

        ServerLevel level = (ServerLevel) serverPlayer.level();
        for (UUID servantUUID : servantDataOpt.get().servantUUIDs) {
            Entity entity = level.getEntity(servantUUID);
            if (!(entity instanceof LivingEntity servant)) continue;

            // 处理不同类型的所有者
            Player owner = null;
            if (servant instanceof IOwned owned && owned.getTrueOwner() instanceof Player p) {
                owner = p;
            } else if (servant instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player p) {
                owner = p;
            }

            if (owner != null) {
                SoulEnchantUtil.repairServantEquipment(owner, servant);
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