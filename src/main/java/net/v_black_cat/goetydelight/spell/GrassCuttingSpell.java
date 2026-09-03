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
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

public class GrassCuttingSpell extends Spell {

    private static final double BASE_RADIUS = 2.0D;   // 5×5
    private static final double MAX_RADIUS = 12.0D;   // 25×25
    private static final int COOLDOWN_TICKS = 25 * 20; // 25 秒

   
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
        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE);

        // 范围 = 基础半径 + 强效(potency) + 半径(radius) 属性加成 + 范围附魔(每级+2)，上限 25×25
        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency());
        radius += 2.0D * rangeLevel;
        radius = Math.min(radius, MAX_RADIUS);
        int r = (int) Math.floor(radius);

        BlockPos center = caster.blockPosition();
        int harvested = 0;
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    if (harvestPlant(worldIn, pos, caster, silkTouch, fortune)) {
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

    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, ResourceKey<Enchantment> enchantment) {
        if (stack.isEmpty()) {
            return 0;
        }
        return caster.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(enchantment)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    /**
     * 收割单个位置。
     * 精准采集 = 用剪刀模拟该方块自己的战利品表（双格植物在结构完整时先结算，高草→2 短草、
     * Goety 高 sienna → 2 sienna_grass、FD 沙灌走它的 shears_harvest 能力条件）；
     * 无精准的草类 → 草秆(自实现时运：数量 = 1 + 随机(0..时运))，花 → 本体，其余剪刀类 → 无掉落。
     */
    private boolean harvestPlant(ServerLevel world, BlockPos pos, LivingEntity caster, boolean silkTouch, int fortune) {
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
                dropAll(world, pos, shearsDrops);
            } else {
                int strawCount = 1 + world.random.nextInt(fortune + 1);
                Block.popResource(world, pos, new ItemStack(ModItems.STRAW.get(), strawCount));
            }
        } else if (flower) {
            Block.popResource(world, pos, new ItemStack(block.asItem()));
        } else if (silkTouch) {
            dropAll(world, pos, shearsDrops);
        }
        return true;
    }

    private static void dropAll(ServerLevel world, BlockPos pos, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                Block.popResource(world, pos, stack);
            }
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
