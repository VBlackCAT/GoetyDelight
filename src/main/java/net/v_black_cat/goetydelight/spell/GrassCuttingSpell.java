package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.entities.spell.GrassCuttingSlashEntity;
import net.v_black_cat.goetydelight.util.SpellLootUtil;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

public class GrassCuttingSpell extends Spell {

    private static final double BASE_RADIUS = 2.0D;   // 5×5
    private static final double MAX_RADIUS = 7.0D;    // 斩击最大半径
    private static final int COOLDOWN_TICKS = 10 * 20; // 10 秒


    private static final TagKey<Block> GRASS_LIKE = TagKey.create(Registries.BLOCK,
            new ResourceLocation("goetydelight", "grass_focus/grass_like"));
    private static final TagKey<Block> SHEARS_ONLY = TagKey.create(Registries.BLOCK,
            new ResourceLocation("goetydelight", "grass_focus/shears_only"));

    @Override
    public SpellStat defaultStats() {
        return new SpellStat(0, 0, 16, BASE_RADIUS, 0, 0.8F);
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
    public List<Enchantment> acceptedEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        list.add(Enchantments.SILK_TOUCH);
        list.add(Enchantments.BLOCK_FORTUNE);
        list.add(ModEnchantments.RANGE.get());
        list.add(ModEnchantments.RADIUS.get());
        list.add(ModEnchantments.VELOCITY.get());
        list.add(ModEnchantments.MAGNET.get()); // 磁引：掉落直接进背包（参考 Goety 的 burrowing_focus）
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
        int fortune = getEnchantLevel(focus, caster, Enchantments.BLOCK_FORTUNE);
        boolean magnet = getEnchantLevel(focus, caster, ModEnchantments.MAGNET.get()) > 0;

        int r = spellRadius(focus, caster, spellStat);
        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE.get());
        int velocityLevel = getEnchantLevel(focus, caster, ModEnchantments.VELOCITY.get());
        float speed = Math.max(0.35F, spellStat.getVelocity() + velocityLevel * 0.5F);
        float range = Math.max(4.0F, spellStat.getRange() + rangeLevel * 2.0F);
        int lifeSpan = Math.max(4, Mth.ceil(range / speed));
        worldIn.addFreshEntity(new GrassCuttingSlashEntity(
                worldIn, caster, speed, r, lifeSpan, silkTouch, fortune, magnet));
    }


    public static int harvestArea(ServerLevel worldIn, LivingEntity caster, BlockPos center,
                                  int radius, boolean silkTouch, int fortune, boolean magnet) {
        int harvested = 0;
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    if (harvestPlant(worldIn, pos, caster, silkTouch, fortune, magnet)) {
                        harvested++;
                    }
                }
            }
        }
        return harvested;
    }
    /** 半径 = 基础半径 + 强效 + 半径属性/附魔加成，上限 7 */
    private static int spellRadius(ItemStack focus, LivingEntity caster, SpellStat spellStat) {
        int radiusLevel = getEnchantLevel(focus, caster, ModEnchantments.RADIUS.get());
        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency() + radiusLevel);
        radius = Math.min(radius, MAX_RADIUS);
        return (int) Math.floor(radius);
    }

    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, Enchantment enchantment) {
        return stack.isEmpty() ? 0 : stack.getEnchantmentLevel(enchantment);
    }

    private static boolean harvestPlant(ServerLevel world, BlockPos pos, LivingEntity caster, boolean silkTouch,
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
