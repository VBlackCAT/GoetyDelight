package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.v_black_cat.goetydelight.init.ModEnchantments;

/**
 * 灵魂修补（Soul Mending）与灵魂治愈（Soul Healing）的实现。
 *
 * <p>旧实现挂在 {@code PlayerTickEvent} 上：每 4 tick 用 {@code player.getAllSlots()} 拉一份 41 格的
 * 快照列表逐个查附魔，每 10 tick 再单独查一次胸甲附魔，仆人装备同样是每 4 tick 遍历一遍
 * —— 无论玩家身上有没有这些附魔，都要付出注册表查询 + 列表分配的代价。
 *
 * <p>现在改成「物品自报」：由 {@code ItemStackMixin} 在 {@code ItemStack#inventoryTick} 处把每格
 * 物品的 tick 转进来（1.21.1 NeoForge 的 {@code Inventory#tick} 会覆盖主背包 0-35、盔甲 36-39、
 * 副手 40），只有真正带附魔的物品才会走到修复/治疗逻辑：
 * <ul>
 *   <li>零列表分配、零玩家级扫描；</li>
 *   <li>用 {@link Holder#is(ResourceKey)} 直接在物品自带的附魔组件里比对，<b>不查注册表</b>，
 *       也不会因为数据包重载后 Holder 实例变化而失效；</li>
 *   <li>节奏与旧实现一致（修补每 4 tick、治愈每 10 tick）。</li>
 * </ul>
 */
public final class SoulEnchantUtil {

    /** 旧实现每 4 tick 修一次；仆人侧改成每 20 tick 检查一次，一次补足 5 次，速率不变。 */
    public static final int REPAIR_INTERVAL_TICKS = 4;
    public static final int HEAL_INTERVAL_TICKS = 10;
    public static final int SERVANT_CHECK_INTERVAL_TICKS = 20;
    private static final int SERVANT_BATCH = SERVANT_CHECK_INTERVAL_TICKS / REPAIR_INTERVAL_TICKS;

    private SoulEnchantUtil() {
    }

    /**
     * 读取物品上指定附魔的等级（无则 0）。
     *
     * <p>直接遍历物品自带的附魔组件，用 {@code Holder#is(ResourceKey)} 比对 —— 避免
     * {@code registryAccess().getHolderOrThrow(...)} 这类每次都要做的注册表查询。
     */
    public static int levelOf(ItemStack stack, ResourceKey<Enchantment> key) {
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            return 0;
        }
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(key)) {
                return enchantments.getLevel(holder);
            }
        }
        return 0;
    }

    /**
     * 物品自身的 tick 入口（由 {@code ItemStackMixin} 调用）。
     *
     * <p>只处理玩家背包里的物品：仆人装备不在 {@code Inventory} 里、不会被 vanilla tick 到，
     * 由 {@link #repairServantEquipment} 单独驱动。
     */
    public static void onInventoryTick(ItemStack stack, LivingEntity owner) {
        if (owner.level().isClientSide || stack.isEmpty() || !(owner instanceof Player player)) {
            return;
        }

        int tickCount = owner.tickCount;

        // 灵魂治愈：只认胸甲槽（用 EquipmentSlot 比对，不依赖槽位数字）
        if (tickCount % HEAL_INTERVAL_TICKS == 0 && owner.getItemBySlot(EquipmentSlot.CHEST) == stack) {
            int level = levelOf(stack, ModEnchantments.SOUL_HEALING);
            if (level > 0) {
                applySoulHealing(player, level);
            }
        }

        // 灵魂修补：任意格子上带附魔且已损坏的物品
        if (tickCount % REPAIR_INTERVAL_TICKS == 0 && stack.isDamageableItem() && stack.getDamageValue() > 0) {
            int level = levelOf(stack, ModEnchantments.SOUL_MENDING);
            if (level > 0) {
                repairItemWithSoulEnergy(player, stack, level);
            }
        }
    }

    public static void applySoulHealing(Player player, int level) {
        if (player.getHealth() >= player.getMaxHealth()) {
            return;
        }
        float maxHealth = player.getMaxHealth();
        float healAmount = (1.0F + 0.01F * maxHealth) * level;
        if (healAmount > 0.5F * maxHealth) {
            healAmount = 0.5F * maxHealth;
        }
        int cost = 5 * level;
        if (SEHelper.getSoulsAmount(player, cost)) {
            player.heal(healAmount);
            SEHelper.decreaseSouls(player, cost);
        }
    }

    public static void repairItemWithSoulEnergy(Player player, ItemStack stack, int level) {
        int currentDamage = stack.getDamageValue();
        int actualRepair = Math.min(level, currentDamage);
        int requiredSouls;
        if (level > 9) {
            requiredSouls = 1;
        } else if (actualRepair < level) {
            requiredSouls = Math.max(1, 5 - level / 2);
        } else {
            requiredSouls = actualRepair * 5;
        }

        if (requiredSouls <= 0 || SEHelper.getSoulsAmount(player, requiredSouls)) {
            if (requiredSouls > 0) {
                SEHelper.decreaseSouls(player, requiredSouls);
            }
            stack.setDamageValue(currentDamage - actualRepair);
        }
    }

    /** 仆人装备的批量修补：一次补足 {@link #SERVANT_BATCH} 次，等价于旧实现每秒的修复总量。 */
    public static void repairServantEquipment(Player owner, LivingEntity servant) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = servant.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem() || stack.getDamageValue() <= 0) {
                continue;
            }
            int level = levelOf(stack, ModEnchantments.SOUL_MENDING);
            if (level <= 0) {
                continue;
            }
            for (int i = 0; i < SERVANT_BATCH; i++) {
                if (stack.getDamageValue() <= 0) {
                    break;
                }
                repairItemWithSoulEnergy(owner, stack, level);
            }
        }
    }
}
