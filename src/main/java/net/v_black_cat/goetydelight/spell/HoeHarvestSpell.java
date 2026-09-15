package net.v_black_cat.goetydelight.spell;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.v_black_cat.goetydelight.util.SpellCastUtil;
import net.v_black_cat.goetydelight.util.SpellLootUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HoeHarvestSpell extends Spell {

    private static final double BASE_RADIUS = 2.0D;
    private static final double MAX_RADIUS = 7.0D;
    private static final int COOLDOWN_TICKS = 10 * 20;

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
        list.add(ModEnchantments.MAGNET); // 磁引：收割掉落直接进背包（参考 Goety 的 burrowing_focus）
        return list;
    }

    @Override
    public boolean conditionsMet(ServerLevel worldIn, LivingEntity caster, SpellStat spellStat) {
        int r = spellRadius(WandUtil.findFocus(caster), caster, spellStat);
        BlockPos center = SpellCastUtil.castCenter(caster); // 与 SpellResult 同一中心
        boolean shouldTill = caster.isShiftKeyDown();
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    boolean valid = shouldTill ? isTillable(worldIn, pos) : isHarvestable(worldIn, pos);
                    if (valid) {
                        return true; // 有一个有效目标就放行，边扫边退
                    }
                }
            }
        }
        return false;
    }

    /** 范围 = 基础半径 + 强效(potency) + 半径属性加成 + 范围附魔(每级 +2)，上限 15×15 */
    private static int spellRadius(ItemStack focus, LivingEntity caster, SpellStat spellStat) {
        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE);
        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency());
        radius += 1.0D * rangeLevel;
        radius = Math.min(radius, MAX_RADIUS);
        return (int) Math.floor(radius);
    }

    // ========== 施法主逻辑：范围操作 ==========
    @Override
    public void SpellResult(ServerLevel worldIn, LivingEntity caster, ItemStack staff, SpellStat spellStat) {
        ItemStack focus = IWand.getFocus(staff);
        if (focus.isEmpty()) focus = WandUtil.findFocus(caster);
        if (focus.isEmpty()) focus = caster.getMainHandItem();

        int r = spellRadius(focus, caster, spellStat);
        BlockPos center = SpellCastUtil.castCenter(caster); // 以右击的方块为中心
        boolean magnet = getEnchantLevel(focus, caster, ModEnchantments.MAGNET) > 0;
        int harvested = 0;
        int tilled = 0;
        boolean shouldTill = caster.isShiftKeyDown();

        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    if (shouldTill) {
                        if (tillBlock(worldIn, pos, caster)) tilled++;
                    } else {
                        if (harvestCrop(worldIn, pos, caster, focus, magnet)) harvested++;
                    }
                }
            }
        }

        if (harvested > 0) {
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (tilled > 0) {
            worldIn.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    // ========== 检测方法（只读） ==========
    private boolean isHarvestable(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof StemBlock || block instanceof AttachedStemBlock) return false;
        if (state.hasProperty(BlockStateProperties.HALF)) return false;
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) return true;

        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        }
        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= 2;
        }
        if (block instanceof BushBlock && !(block instanceof SugarCaneBlock)) {
            IntegerProperty ageProperty = findAgeProperty(state);
            return ageProperty != null &&
                    state.getValue(ageProperty) >= Collections.max(ageProperty.getPossibleValues());
        }
        return false;
    }

    /** 判断方块是否可被锄头耕为农田（关键修复：使用真实锄头物品构造 context） */
    private boolean isTillable(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!world.getBlockState(pos.above()).isAir()) return false;
        BlockState tilled = getTilledState(world, pos, state);
        return tilled != null && tilled != state;
    }

    /** 构建锄地上下文并调用 getToolModifiedState；返回 null 表示不可耕 */
    @Nullable
    private static BlockState getTilledState(ServerLevel world, BlockPos pos, BlockState state) {
        UseOnContext context = new UseOnContext(world,
                null,
                InteractionHand.MAIN_HAND,
                new ItemStack(Items.IRON_HOE), // 关键：必须传入真实锄头，getToolModifiedState 内部会检查 canPerformAction
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        return state.getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
    }

    // ========== 实际操作 ==========
    private boolean harvestCrop(ServerLevel world, BlockPos pos, LivingEntity caster, ItemStack tool, boolean magnet) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof StemBlock || block instanceof AttachedStemBlock) return false;
        if (state.hasProperty(BlockStateProperties.HALF)) return false;
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            world.destroyBlock(pos, true);
            return true;
        }

        if (block instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) return false;
            IntegerProperty ageProperty = findAgeProperty(state);
            if (ageProperty == null) return false;
            harvestAndRegrow(world, pos, state, caster, ageProperty, 0, tool, magnet);
            return true;
        }
        if (block instanceof SweetBerryBushBlock) {
            if (state.getValue(SweetBerryBushBlock.AGE) < 3) return false;
            harvestAndRegrow(world, pos, state, caster, SweetBerryBushBlock.AGE, 1, tool, magnet);
            return true;
        }
        if (block instanceof CocoaBlock) {
            if (state.getValue(CocoaBlock.AGE) < 2) return false;
            harvestAndRegrow(world, pos, state, caster, CocoaBlock.AGE, 0, tool, magnet);
            return true;
        }
        if (block instanceof BushBlock && !(block instanceof SugarCaneBlock)) {
            IntegerProperty ageProperty = findAgeProperty(state);
            if (ageProperty != null &&
                    state.getValue(ageProperty) >= Collections.max(ageProperty.getPossibleValues())) {
                harvestAndRegrow(world, pos, state, caster, ageProperty, 0, tool, magnet);
                return true;
            }
        }
        return false;
    }

    private void harvestAndRegrow(ServerLevel world, BlockPos pos, BlockState state, LivingEntity caster,
                                  IntegerProperty ageProperty, int newAge, ItemStack tool, boolean magnet) {
        List<ItemStack> drops = Block.getDrops(state, world, pos, null, caster, tool);
        for (ItemStack drop : drops) {
            SpellLootUtil.giveOrDrop(world, pos, caster, drop, magnet);
        }
        world.setBlock(pos, state.setValue(ageProperty, newAge), 3);
    }

    private boolean tillBlock(ServerLevel world, BlockPos pos, LivingEntity caster) {
        BlockState state = world.getBlockState(pos);
        if (!world.getBlockState(pos.above()).isAir()) return false;

        // 使用与 isTillable 相同的逻辑获取耕地后状态
        BlockState tilled = getTilledState(world, pos, state);
        if (tilled != null && tilled != state) {
            world.setBlock(pos, tilled, 3);
            return true;
        }
        return false;
    }

    // ========== 工具方法 ==========
    private static IntegerProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && integerProperty.getName().equals("age")) {
                return integerProperty;
            }
        }
        return null;
    }

    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, ResourceKey<Enchantment> enchantmentKey) {
        if (stack.isEmpty()) return 0;
        return caster.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(enchantmentKey)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }
}