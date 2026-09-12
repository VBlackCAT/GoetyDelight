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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.util.SpellCastUtil;
import net.v_black_cat.goetydelight.util.SpellLootUtil;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

public class GrassCuttingSpell extends Spell {

    private static final double BASE_RADIUS = 2.0D;   // 5×5
    private static final double MAX_RADIUS = 7.0D;    // 15×15（范围附魔每级 +2，III 级到顶）
    private static final int COOLDOWN_TICKS = 10 * 20; // 10 秒

   
    private static final TagKey<Block> GRASS_LIKE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("goetydelight", "grass_focus/grass_like"));
    private static final TagKey<Block> SHEARS_ONLY = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("goetydelight", "grass_focus/shears_only"));

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
        return SpellType.GEOMANCY;
    }

    @Override
    public List<ResourceKey<Enchantment>> acceptedEnchantments() {
        List<ResourceKey<Enchantment>> list = new ArrayList<>();
        list.add(Enchantments.SILK_TOUCH);
        list.add(Enchantments.FORTUNE);
        list.add(ModEnchantments.RANGE);
        list.add(ModEnchantments.MAGNET); // 磁引：掉落直接进背包（参考 Goety 的 burrowing_focus）
        return list;
    }

    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        ItemStack focus = IWand.getFocus(staff);
        if (focus.isEmpty()) {
            focus = WandUtil.findFocus(caster);
        }

        // 直接从聚晶栈读取附魔等级（绕过 enchantedFocus/isEnchanted 等间接门禁）
        boolean silkTouch = getEnchantLevel(focus, caster, Enchantments.SILK_TOUCH) > 0;
        int fortune = getEnchantLevel(focus, caster, Enchantments.FORTUNE);
        boolean magnet = getEnchantLevel(focus, caster, ModEnchantments.MAGNET) > 0;

        int r = spellRadius(focus, caster, spellStat);
        BlockPos center = SpellCastUtil.castCenter(caster); // 以右击的方块为中心
        int harvested = 0;
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    if (harvestPlant(worldIn, pos, caster, silkTouch, fortune, magnet)) {
                        harvested++;
                    }
                }
            }
        }

        if (harvested > 0) {
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster, SpellStat spellStat) {
        int r = spellRadius(WandUtil.findFocus(caster), caster, spellStat);
        BlockPos center = SpellCastUtil.castCenter(caster); // 与 SpellResult 同一中心
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    if (isHarvestable(worldIn.getBlockState(center.offset(dx, y, dz)))) {
                        return true; // 只要有一株可割就放行，边扫边退
                    }
                }
            }
        }
        return false;
    }

    /** 范围 = 基础半径 + 强效(potency) + 半径(radius) 属性加成 + 范围附魔(每级 +2)，上限 15×15 */
    private static int spellRadius(ItemStack focus, LivingEntity caster, SpellStat spellStat) {
        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE);
        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency());
        radius += 2.0D * rangeLevel;
        radius = Math.min(radius, MAX_RADIUS);
        return (int) Math.floor(radius);
    }

    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, ResourceKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        return caster.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(enchantment)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    private boolean harvestPlant(ServerLevel world, BlockPos pos, LivingEntity caster, boolean silkTouch,
                                 int fortune, boolean magnet) {
        BlockState state = world.getBlockState(pos);
        if (!isHarvestable(state)) {
            return false;
        }
        // 双格植物的上半段跳过（由下半段统一处理，保证战利品结算时结构完整）
        BlockState below = world.getBlockState(pos.below());
        if (isHarvestable(below) && below.getBlock() == state.getBlock()) {
            return false;
        }
        Block block = state.getBlock();
        boolean grassLike = state.is(GRASS_LIKE);
        boolean flower = state.is(BlockTags.FLOWERS) || state.is(BlockTags.TALL_FLOWERS);

        List<ItemStack> shearsDrops = (silkTouch && (grassLike || state.is(SHEARS_ONLY)))
                ? Block.getDrops(state, world, pos, null, caster, new ItemStack(Items.SHEARS))
                : List.of();

        BlockState above = world.getBlockState(pos.above());
        if (above.getBlock() == state.getBlock()) {
            world.destroyBlock(pos.above(), false);
        }
        world.destroyBlock(pos, false);

        if (grassLike) {
            if (silkTouch) {
                dropAll(world, pos, caster, shearsDrops, magnet);
            } else {
                int strawCount = 1 + world.random.nextInt(fortune + 1);
                SpellLootUtil.giveOrDrop(world, pos, caster, new ItemStack(ModItems.STRAW.get(), strawCount), magnet);
            }
        } else if (flower) {
            SpellLootUtil.giveOrDrop(world, pos, caster, new ItemStack(block.asItem()), magnet);
        } else if (silkTouch) {
            dropAll(world, pos, caster, shearsDrops, magnet);
        }
        return true;
    }

    private static void dropAll(ServerLevel world, BlockPos pos, LivingEntity caster,
                                List<ItemStack> stacks, boolean magnet) {
        for (ItemStack stack : stacks) {
            SpellLootUtil.giveOrDrop(world, pos, caster, stack, magnet);
        }
    }

    /** 可收割目标 = 标签化分类（grass_like / shears_only）+ 原版花标签（不吃作物/瓜类） */
    private static boolean isHarvestable(BlockState state) {
        return state.is(GRASS_LIKE)
                || state.is(SHEARS_ONLY)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.TALL_FLOWERS);
    }
}
