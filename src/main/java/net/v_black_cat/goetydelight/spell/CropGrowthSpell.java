package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.util.SpellCastUtil;

import java.util.ArrayList;
import java.util.List;

public class CropGrowthSpell extends Spell {

    private static final double BASE_RADIUS = 2.0D;    // 5×5
    private static final double MAX_RADIUS = 7.0D;     // 15×15（范围附魔每级 +2，III 级封顶）
    private static final int COOLDOWN_TICKS = 10 * 20;  // 10 秒
    private static final int MAX_BONE_MEAL_PER_BLOCK = 8; // 小麦 0→7 每次 +2~5，8 次足够

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 16, BASE_RADIUS, 0, 0.0F);
    }

    @Override
    public int defaultSoulCost() {
        return 50;
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
        return SpellType.WILD;
    }

    @Override
    public List<ResourceKey<Enchantment>> acceptedEnchantments() {
        List<ResourceKey<Enchantment>> list = new ArrayList<>();
        list.add(ModEnchantments.RANGE);
        return list;
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster, SpellStat spellStat) {
        int r = spellRadius(WandUtil.findFocus(caster), caster, spellStat);
        BlockPos center = SpellCastUtil.castCenter(caster); // 与 SpellResult 同一中心
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    if (isGrowable(worldIn, center.offset(dx, y, dz))) {
                        return true; // 有一格可催熟就放行，边扫边退
                    }
                }
            }
        }
        return false;
    }

    /** 范围 = 基础半径 + 强效(potency) + 半径属性加成 + 范围附魔(每级 +2)，上限 15×15（同锄头聚晶） */
    private static int spellRadius(ItemStack focus, LivingEntity caster, SpellStat spellStat) {
        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE);
        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency());
        radius += 1.0D * rangeLevel;
        radius = Math.min(radius, MAX_RADIUS);
        return (int) Math.floor(radius);
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        ItemStack focus = IWand.getFocus(staff);
        if (focus.isEmpty()) focus = WandUtil.findFocus(caster);
        if (focus.isEmpty()) focus = caster.getMainHandItem(); // 兜底

        int r = spellRadius(focus, caster, spellStat);
        BlockPos center = SpellCastUtil.castCenter(caster); // 以右击的方块为中心
        int grown = 0;
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    if (growBlock(worldIn, center.offset(dx, y, dz))) {
                        grown++;
                    }
                }
            }
        }

        if (grown > 0) {
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /** 该方块是否可催熟：是 Bonemealable、可被骨粉催、且不是树苗 */
    private static boolean isGrowable(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof SaplingBlock) return false; // 树苗归树木催生聚晶
        return block instanceof BonemealableBlock bonemealable
                && bonemealable.isValidBonemealTarget(world, pos, state);
    }

    /** 对一格方块反复催熟直到成熟（或达到次数上限）；返回是否真的催动过 */
    private static boolean growBlock(ServerLevel world, BlockPos pos) {
        boolean any = false;
        for (int i = 0; i < MAX_BONE_MEAL_PER_BLOCK; ++i) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof SaplingBlock) break;
            if (!(block instanceof BonemealableBlock bonemealable)
                    || !bonemealable.isValidBonemealTarget(world, pos, state)) {
                break; // 已成熟 / 不可催 → 停
            }
            bonemealable.performBonemeal(world, world.random, pos, state);
            any = true;
        }
        return any;
    }

    /** 直接从物品栈读取附魔等级；注册表/物品缺失时返回 0（不抛异常） */
    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, ResourceKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        return caster.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(enchantment)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }
}
