package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.effects.brew.BrewEffect;
import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.common.effects.brew.BrewEffects;
import com.Polarice3.Goety.common.entities.ally.AnimalSummon;
import com.Polarice3.Goety.common.entities.ally.illager.AbstractIllagerServant;
import com.Polarice3.Goety.common.entities.util.BrewGas;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.entities.spell.RichSoilSpellEntity;
import net.v_black_cat.goetydelight.util.SpellCastUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoveAndFertilitySpell extends Spell {

    private static final int DEFAULT_SIDE = 9;
    private static final int SIDE_PER_LEVEL = 1;
    private static final int MAX_SIDE = 15;

    private static final int COOLDOWN_TICKS = 240 * 20;
    private static final int BASE_DURATION_SECONDS = 3;
    private static final int DURATION_PER_LEVEL_SECONDS = 2;

    /** 默认平铺两层：脚下那一层 + 它上面一层 */
    private static final int[] LAYER_OFFSETS = {0, 1};
    private static final int MAX_CLOUD_BLOCKS = 256;

    /** Goety 自带的酿造效果 id（BrewEffect.getEffectID() = "effect.goety." + 名称） */
    private static final String LOVE_EFFECT_ID = "effect.goety.love";
    private static final String FERTILITY_EFFECT_ID = "effect.goety.fertility";

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 16, DEFAULT_SIDE / 2.0D, 0, 0.0F);
    }

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
        return SpellType.GEOMANCY;
    }

    @Override
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.RANGE.get());     // 每级长宽各 +1，最大 15×15
        list.add(ModEnchantments.RADIUS.get());
        list.add(ModEnchantments.DURATION.get());  // 持续时间
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        ItemStack focus = IWand.getFocus(staff);
        if (focus.isEmpty()) {
            focus = WandUtil.findFocus(caster);
        }
        if (focus.isEmpty()) {
            focus = caster.getMainHandItem(); // 兜底
        }

        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE.get());
        int radiusLevel = getEnchantLevel(focus, caster, ModEnchantments.RADIUS.get());
        int durationLevel = getEnchantLevel(focus, caster, ModEnchantments.DURATION.get());
        int durationTicks = (BASE_DURATION_SECONDS + DURATION_PER_LEVEL_SECONDS * durationLevel) * 20;
        int side = cloudSide(rangeLevel + radiusLevel);
        BlockPos center = SpellCastUtil.castCenterOrEntity(caster);
        worldIn.addFreshEntity(new RichSoilSpellEntity(worldIn, caster, center,
                Math.max(1, side / 2), RichSoilSpellEntity.EffectType.LOVE_AND_FERTILITY)
                .setStaff(staff)
                .setCloudSide(side)
                .setDurationTicks(durationTicks));
    }

    public static boolean performDeferredEffect(ServerLevel worldIn, LivingEntity caster, BlockPos center,
                                                int side, int durationTicks) {
        if (caster == null) {
            return false;
        }

        List<BrewEffectInstance> brewEffects = new ArrayList<>();
        addBrewEffect(brewEffects, LOVE_EFFECT_ID, durationTicks);
        addBrewEffect(brewEffects, FERTILITY_EFFECT_ID, durationTicks);
        if (brewEffects.isEmpty()) {
            return false;
        }

        Map<BlockPos, UUID> cloud = fillCloud(worldIn, caster, center, side, brewEffects);
        if (cloud.isEmpty()) {
            return false;
        }
        LoveCloudTracker.track(worldIn, caster, cloud, brewEffects, durationTicks);

        worldIn.sendParticles(ParticleTypes.HEART,
                center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D,
                8, 0.4D, 0.4D, 0.4D, 0.0D);
        worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }

    /** 边长 = 默认 8 + 每级范围 +1，上限 15 */
    private static int cloudSide(int rangeLevel) {
        return Math.min(DEFAULT_SIDE + SIDE_PER_LEVEL * rangeLevel, MAX_SIDE);
    }


    private static Map<BlockPos, UUID> fillCloud(ServerLevel worldIn, LivingEntity caster, BlockPos center, int side,
                                                 List<BrewEffectInstance> brewEffects) {
        // 偶数边长时向正方向少铺一格，保证正好 side × side
        int half = side / 2;
        int minOffset = -half;
        int maxOffset = side - 1 - half;

        Map<BlockPos, UUID> spawned = new LinkedHashMap<>();
        for (int dy : LAYER_OFFSETS) {
            for (int dx = minOffset; dx <= maxOffset && spawned.size() < MAX_CLOUD_BLOCKS; ++dx) {
                for (int dz = minOffset; dz <= maxOffset && spawned.size() < MAX_CLOUD_BLOCKS; ++dz) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!canHoldGas(worldIn.getBlockState(pos))) {
                        continue; // 只往空气/可替换的草木里铺（带流体或火的格子会被 Goety 当帧判废）
                    }
                    if (!worldIn.getEntitiesOfClass(BrewGas.class, new AABB(pos)).isEmpty()) {
                        continue;
                    }
                    BrewGas gas = LoveCloudTracker.spawnGas(worldIn, pos, caster, brewEffects);
                    if (gas != null) {
                        spawned.put(pos, gas.getUUID());
                    }
                }
            }
        }
        return spawned;
    }
    private static boolean canHoldGas(BlockState state) {
        if (state.is(BlockTags.FIRE) || !state.getFluidState().isEmpty()) {
            return false;
        }
        return state.isAir() || state.canBeReplaced();
    }

    /** 把 Goety 的酿造效果装进药云；BrewGas 只对 canLinger() 的效果生效，取不到就跳过 */
    private static void addBrewEffect(List<BrewEffectInstance> list, String effectId, int durationTicks) {
        BrewEffect effect = BrewEffects.INSTANCE.getBrewEffect(effectId);
        if (effect != null && effect.canLinger()) {
            list.add(new BrewEffectInstance(effect, durationTicks, 0));
        }
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster, SpellStat spellStat) {
        ItemStack focus = WandUtil.findFocus(caster);
        int side = cloudSide(getEnchantLevel(focus, caster, ModEnchantments.RANGE.get())
                + getEnchantLevel(focus, caster, ModEnchantments.RADIUS.get()));
        int half = side / 2;
        AABB area = areaOf(SpellCastUtil.castCenterOrEntity(caster), -half, side - 1 - half);
        for (LivingEntity target : worldIn.getEntitiesOfClass(LivingEntity.class, area)) {
            if (isAffectable(target)) {
                return true;
            }
        }
        return false;
    }

    /** 中心 ± 偏移构成的方形区域（垂直再取 ±2），与平铺范围一致 */
    private static AABB areaOf(BlockPos center, int minOffset, int maxOffset) {
        return new AABB(
                center.getX() + minOffset, center.getY() - 2.0D, center.getZ() + minOffset,
                center.getX() + maxOffset + 1.0D, center.getY() + 3.0D, center.getZ() + maxOffset + 1.0D);
    }

    /** 爱/丰饶实际能作用到的生物：原版动物、Goety 动物仆从、Goety 灾厄仆从（与两个 BrewEffect 的判定一致） */
    private static boolean isAffectable(LivingEntity entity) {
        return entity instanceof Animal
                || entity instanceof AnimalSummon
                || entity instanceof AbstractIllagerServant;
    }

    /** 直接从物品栈读取附魔等级；注册表/物品缺失时返回 0（不抛异常） */
    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, Enchantment enchantment) {
        return stack.isEmpty() ? 0 : stack.getEnchantmentLevel(enchantment);
    }
}
