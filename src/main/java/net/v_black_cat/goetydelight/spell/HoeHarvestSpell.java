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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HoeHarvestSpell extends Spell {

    private static final double BASE_RADIUS = 2.0D;
    private static final double MAX_RADIUS = 12.0D;
    private static final int COOLDOWN_TICKS = 25 * 20;

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
        if (focus.isEmpty()) {
            focus = caster.getMainHandItem(); // 兜底
        }

        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE);

        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency());
        radius += 2.0D * rangeLevel;
        radius = Math.min(radius, MAX_RADIUS);
        int r = (int) Math.floor(radius);

        BlockPos center = caster.blockPosition();
        int harvested = 0;
        int tilled = 0;

        // ===== 核心修改：只有按住 Shift 才允许耕地 =====
        boolean shouldTill = caster.isShiftKeyDown();

        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    // 先尝试收割（传入 focus 使附魔生效）
                    if (harvestCrop(worldIn, pos, caster, focus)) {
                        harvested++;
                    } 
                    // 仅当按住 Shift 且当前方块不可收割时才尝试耕地
                    else if (shouldTill && tillBlock(worldIn, pos, caster)) {
                        tilled++;
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

    /**
     * 获取附魔等级 - 使用 getHolder 以兼容 1.21.1 的 Holder 系统
     */
    private static int getEnchantLevel(ItemStack stack, LivingEntity caster, ResourceKey<Enchantment> enchantmentKey) {
        if (stack.isEmpty()) {
            return 0;
        }
        return caster.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(enchantmentKey)           // 返回 Optional<Holder<Enchantment>>
                .map(stack::getEnchantmentLevel)     // 方法引用接受 Holder，完美匹配
                .orElse(0);
    }

    /**
     * 收割单个作物，自动补种
     * @param tool 传入法杖/焦点物品，用于计算时运/精准采集
     */
    private boolean harvestCrop(ServerLevel world, BlockPos pos, LivingEntity caster, ItemStack tool) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof StemBlock || block instanceof AttachedStemBlock) {
            return false;
        }
        if (state.hasProperty(BlockStateProperties.HALF)) {
            return false;
        }
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            world.destroyBlock(pos, true);
            return true;
        }

        if (block instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) {
                return false;
            }
            IntegerProperty ageProperty = findAgeProperty(state);
            if (ageProperty == null) {
                return false;
            }
            return harvestAndRegrow(world, pos, state, caster, ageProperty, 0, tool);
        }
        if (block instanceof SweetBerryBushBlock) {
            if (state.getValue(SweetBerryBushBlock.AGE) < 3) {
                return false;
            }
            return harvestAndRegrow(world, pos, state, caster, SweetBerryBushBlock.AGE, 1, tool);
        }
        if (block instanceof CocoaBlock) {
            if (state.getValue(CocoaBlock.AGE) < 2) {
                return false;
            }
            return harvestAndRegrow(world, pos, state, caster, CocoaBlock.AGE, 0, tool);
        }
        if (block instanceof BushBlock && !(block instanceof SugarCaneBlock)) {
            IntegerProperty ageProperty = findAgeProperty(state);
            if (ageProperty != null
                    && state.getValue(ageProperty) >= Collections.max(ageProperty.getPossibleValues())) {
                return harvestAndRegrow(world, pos, state, caster, ageProperty, 0, tool);
            }
        }
        return false;
    }

    private boolean harvestAndRegrow(ServerLevel world, BlockPos pos, BlockState state, LivingEntity caster,
                                     IntegerProperty ageProperty, int newAge, ItemStack tool) {
        // 传入 tool 使附魔生效
        List<ItemStack> drops = Block.getDrops(state, world, pos, null, caster, tool);
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.popResource(world, pos, drop);
            }
        }
        world.setBlock(pos, state.setValue(ageProperty, newAge), 3);
        return true;
    }

    private static IntegerProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty
                    && integerProperty.getName().equals("age")) {
                return integerProperty;
            }
        }
        return null;
    }

    /**
     * 耕地逻辑 - 完全依赖 getToolModifiedState，无硬编码列表
     */
    private boolean tillBlock(ServerLevel world, BlockPos pos, LivingEntity caster) {
        BlockState state = world.getBlockState(pos);
        if (!world.getBlockState(pos.above()).isAir()) {
            return false;
        }

        UseOnContext context = new UseOnContext(world,
                caster instanceof Player player ? player : null,
                InteractionHand.MAIN_HAND,
                ItemStack.EMPTY,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        BlockState tilled = state.getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
        if (tilled != null && tilled != state) {
            world.setBlock(pos, tilled, 3);
            return true;
        }
        return false;
    }
}