package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.EverChargeSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.v_black_cat.goetydelight.entities.spell.MalevolentShrineEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

/**
 * 石油雾。
 * <p>
 * 按住右键持续释放：由 Goety 的 EverChargeSpell 机制每秒消耗 2000 灵魂能量，
 * SpellResult 每 tick 刷新一个跟随施法者的领域实体；松手后领域会在数 tick 内消失。
 */
public class MalevolentShrineSpell extends EverChargeSpell {

    public static final int SOUL_COST_PER_SECOND = 2000;
    public static final double BASE_RADIUS = 8.0D;
    public static final float BASE_DAMAGE = 3.0F;
    public static final float DAMAGE_PER_POTENCY = 0.75F;

    /** 每个施法者当前持有的领域，用于保证领域实体位置固定后仍能跨距离刷新。 */
    private static final Map<UUID, MalevolentShrineEntity> ACTIVE_DOMAINS = new HashMap<>();

    @Override
    public int defaultSoulCost() {
        return SOUL_COST_PER_SECOND;
    }

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 0, BASE_RADIUS, 0, 0.0F);
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.VOID;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        list.add(ModEnchantments.RADIUS.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        ItemStack focus = IWand.getFocus(staff);
        if (focus.isEmpty()) {
            focus = WandUtil.findFocus(caster);
        }
        if (focus.isEmpty()) {
            focus = caster.getMainHandItem();
        }

        int potency = getEnchantLevel(focus, caster, ModEnchantments.POTENCY.get());
        int radiusLevel = getEnchantLevel(focus, caster, ModEnchantments.RADIUS.get());
        double radius = Math.max(1.0D, spellStat.getRadius() + spellStat.getPotency() + radiusLevel);
        float damage = BASE_DAMAGE + potency * DAMAGE_PER_POTENCY;

        MalevolentShrineEntity domain = ACTIVE_DOMAINS.get(caster.getUUID());
        if (domain == null || domain.isRemoved() || domain.level() != worldIn) {
            domain = new MalevolentShrineEntity(worldIn, caster, (float) radius, damage);
            worldIn.addFreshEntity(domain);
            registerDomain(caster, domain);
        } else {
            domain.setRadius((float) radius);
            domain.setDamage(damage);
            domain.setOwner(caster);
            domain.refreshDuration();
        }
    }

    public static void registerDomain(LivingEntity caster, MalevolentShrineEntity domain) {
        ACTIVE_DOMAINS.put(caster.getUUID(), domain);
    }

    public static void unregisterDomain(MalevolentShrineEntity domain) {
        ACTIVE_DOMAINS.values().removeIf(existing -> existing == domain);
    }

    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, Enchantment enchantment) {
        return stack.isEmpty() ? 0 : stack.getEnchantmentLevel(enchantment);
    }
}