package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.entities.spell.RichSoilSpellEntity;
import net.v_black_cat.goetydelight.util.SpellCastUtil;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class RichSoilSpell extends Spell {

    private static final double BASE_RADIUS = 3.0D;    // 7×7（默认范围 +2，补上删掉的范围附魔）
    private static final double MAX_RADIUS = 7.0D;     // 15×15
    private static final double RADIUS_PER_LEVEL = 1.5D; // 半径附魔每级 +1.5（向下取整 → 9×9 / 13×13 / 15×15）
    private static final int COOLDOWN_TICKS = 120 * 20; // 120 秒

    private static final TagKey<Block> FARMLAND = TagKey.create(Registries.BLOCK,
            new ResourceLocation("goetydelight", "rich_soil_focus/farmland"));

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 16, BASE_RADIUS, 0, 0.0F);
    }

    @Override
    public int defaultSoulCost() {
        return 500;
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
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(ModEnchantments.RADIUS.get());
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        ItemStack focus = WandUtil.findFocus(caster);
        int r = spellRadius(focus, caster, spellStat);
        BlockPos center = SpellCastUtil.castCenter(caster); // 以右击的方块为中心，实体负责延迟执行
        worldIn.addFreshEntity(new RichSoilSpellEntity(worldIn, caster, center, r,
                RichSoilSpellEntity.EffectType.RICH_SOIL)
                .setStaff(staff)
                .setPenetration(spellPenetration(focus, caster)));
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster, SpellStat spellStat) {
        ItemStack focus = WandUtil.findFocus(caster);
        int r = spellRadius(focus, caster, spellStat);
        int penetration = spellPenetration(focus, caster);
        BlockPos center = SpellCastUtil.castCenter(caster); // 与 SpellResult 同一中心
        for (int y = -penetration; y <= penetration; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    if (convertState(worldIn.getBlockState(center.offset(dx, y, dz))) != null) {
                        return true; // 有一块可转化就放行，边扫边退
                    }
                }
            }
        }
        return false;
    }

    /** 范围 = 基础半径 + 强效(potency) + 半径属性加成 + 半径附魔(每级 +1.5)，上限 15×15 */
    private static int spellRadius(ItemStack focus, LivingEntity caster, SpellStat spellStat) {
        int radiusLevel = getEnchantLevel(focus, caster, ModEnchantments.RADIUS.get());
        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency());
        radius += RADIUS_PER_LEVEL * radiusLevel;
        radius = Math.min(radius, MAX_RADIUS);
        return (int) Math.floor(radius);
    }

    /** 穿透 = 半径附魔等级（每级 ±1 层），III 级共 7 层；没有半径附魔就只转化表层 */
    private static int spellPenetration(ItemStack focus, LivingEntity caster) {
        return getEnchantLevel(focus, caster, ModEnchantments.RADIUS.get());
    }

    /** 返回转换后的方块状态；不满足转换条件时返回 null */
    public static BlockState convertState(BlockState state) {
        // 泥土/草方块/灰化土等原版土 → 沃土
        if (state.is(BlockTags.DIRT)) {
            return ModBlocks.RICH_SOIL.get().defaultBlockState();
        }
        // 耕地 → 沃土耕地，并保留原有湿润度；只有标签收录的耕地才算（默认仅 minecraft:farmland）
        if (state.is(FARMLAND) && state.getBlock() != ModBlocks.RICH_SOIL_FARMLAND.get()) {
            BlockState richSoilFarmland = ModBlocks.RICH_SOIL_FARMLAND.get().defaultBlockState();
            if (state.hasProperty(FarmBlock.MOISTURE) && richSoilFarmland.hasProperty(FarmBlock.MOISTURE)) {
                richSoilFarmland = richSoilFarmland.setValue(FarmBlock.MOISTURE, state.getValue(FarmBlock.MOISTURE));
            }
            return richSoilFarmland;
        }
        return null;
    }

    /** 直接从物品栈读取附魔等级；注册表/物品缺失时返回 0（不抛异常） */
    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, Enchantment enchantment) {
        return stack.isEmpty() ? 0 : stack.getEnchantmentLevel(enchantment);
    }
}
