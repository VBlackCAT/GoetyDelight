package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.BlockFinder;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class LaowangSpell extends Spell {

    private static final String PIG_NAME = "laowang237";
    private static final int BASE_MIN = 3;      // 基础召唤下限
    private static final int BASE_MAX = 5;      // 基础召唤上限
    private static final int PER_LEVEL_MIN = 1; // 每级强效额外下限
    private static final int PER_LEVEL_MAX = 3; // 每级强效额外上限
    private static final int COOLDOWN_TICKS = 30 * 20; // 30 秒

    @Override
    public int defaultSoulCost() {
        return 100;
    }

    @Override
    public int defaultCastDuration() {
        return 0;
    }

    @Override
    public int defaultSpellCooldown() {
        return COOLDOWN_TICKS;
    }

    @Override
    public SpellType getSpellType() {
        return SpellType.NONE;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.POTENCY.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        // 用 Goety 自己的 WandUtil.findFocus 取聚晶：它只对 instanceof IWand 的栈调用 IWand.getFocus，
        // 所以卷轴等非法杖的栈不会再抛出 "ItemStack is missing item capability"
        ItemStack focus = WandUtil.findFocus(caster);
        int potency = getEnchantLevel(focus, caster, ModEnchantments.POTENCY.get());
        performDeferredEffect(worldIn, caster, potency);
    }

    /** 实际召唤逻辑：由 {@link #SpellResult} 直接调用（与光柱解耦后不再走实体延迟） */
    public static boolean performDeferredEffect(ServerLevel worldIn, LivingEntity caster, int potency) {
        if (caster == null) {
            return false;
        }

        int count = randomBetween(worldIn, BASE_MIN, BASE_MAX);
        for (int level = 0; level < potency; ++level) {
            count += randomBetween(worldIn, PER_LEVEL_MIN, PER_LEVEL_MAX);
        }

        int summoned = 0;
        for (int i = 0; i < count; ++i) {
            if (summonPig(worldIn, caster)) {
                summoned++;
            }
        }

        if (summoned > 0) {
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    ModSounds.SUMMON_SPELL.get(), caster.getSoundSource(), 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    /** 在施法者附近召唤一只名为 laowang237 的成年猪 */
    private static boolean summonPig(ServerLevel worldIn, LivingEntity caster) {
        Pig pig = EntityType.PIG.create(worldIn);
        if (pig == null) {
            return false;
        }
        pig.setBaby(false);

        BlockPos spawnPos = BlockFinder.SummonRadius(caster.blockPosition(), pig, worldIn);
        pig.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                worldIn.random.nextFloat() * 360.0F, 0.0F);
        MobUtil.moveDownToGround(pig);

        pig.setCustomName(Component.literal(PIG_NAME));
        pig.setCustomNameVisible(true);
        pig.setPersistenceRequired();

        if (worldIn.addFreshEntity(pig)) {
            worldIn.sendParticles(ParticleTypes.LARGE_SMOKE,
                    pig.getX(), pig.getY() + pig.getBbHeight() * 0.5D, pig.getZ(),
                    8, 0.3D, 0.3D, 0.3D, 0.02D);
            return true;
        }
        return false;
    }

    private static int randomBetween(ServerLevel worldIn, int min, int max) {
        return min + worldIn.random.nextInt(max - min + 1);
    }

    /** 直接从物品栈读取附魔等级；注册表/物品缺失时返回 0（不抛异常） */
    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, Enchantment enchantment) {
        return stack.isEmpty() ? 0 : stack.getEnchantmentLevel(enchantment);
    }
}
