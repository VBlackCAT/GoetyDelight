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

    private static final double BASE_RADIUS = 2.0D;   // 5×5
    private static final double MAX_RADIUS = 12.0D;   // 25×25
    private static final int COOLDOWN_TICKS = 25 * 20; // 25 秒

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
        int rangeLevel = getEnchantLevel(focus, caster, ModEnchantments.RANGE);

        double radius = Math.max(BASE_RADIUS, spellStat.getRadius() + spellStat.getPotency());
        radius += 2.0D * rangeLevel;
        radius = Math.min(radius, MAX_RADIUS);
        int r = (int) Math.floor(radius);

        BlockPos center = caster.blockPosition();
        int harvested = 0;
        int tilled = 0;
        for (int y = -2; y <= 2; ++y) {
            for (int dx = -r; dx <= r; ++dx) {
                for (int dz = -r; dz <= r; ++dz) {
                    BlockPos pos = center.offset(dx, y, dz);
                    if (harvestCrop(worldIn, pos, caster)) {
                        harvested++;
                    } else if (tillBlock(worldIn, pos, caster)) {
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

    /**
     * 收割单个位置（参考 FTB Ultimine 的 VanillaCropLikeHandler：不枚举方块，按类判定成熟）。
     * 只摘果不破坏植株：原地把 age 退回幼苗阶段实现自动补种；仅成熟作物可收；瓜茎永不触碰。
     */
    private boolean harvestCrop(ServerLevel world, BlockPos pos, LivingEntity caster) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // 瓜茎（原版 + 继承 StemBlock/AttachedStemBlock 的模组瓜茎，含本模组灵质瓜茎）永不收割
        if (block instanceof StemBlock || block instanceof AttachedStemBlock) {
            return false;
        }
        // 双格作物（瓶子草作物等）整株破坏式收割会破坏结构，跳过
        if (state.hasProperty(BlockStateProperties.HALF)) {
            return false;
        }
        // 瓜果实体（无 age，非植株）：整体敲掉采收
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            world.destroyBlock(pos, true);
            return true;
        }

        // 1) 原版 CropBlock 及其所有子类（含其它模组继承 CropBlock 的作物，如 FD 卷心菜/洋葱/番茄）
        if (block instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) {
                return false;
            }
            // getAgeProperty() 是 protected（FTBU 能调是它自己的依赖情况），复位阶段统一走通用
            // "age" 属性查找；CropBlock 体系约定 age 属性名即为 "age"，效果等同
            IntegerProperty ageProperty = findAgeProperty(state);
            if (ageProperty == null) {
                return false;
            }
            return harvestAndRegrow(world, pos, state, caster, ageProperty, 0);
        }
        // 2) 甜浆果：成熟(age>=3)后采果，保留 age=1 以便继续挂果
        if (block instanceof SweetBerryBushBlock) {
            if (state.getValue(SweetBerryBushBlock.AGE) < 3) {
                return false;
            }
            return harvestAndRegrow(world, pos, state, caster, SweetBerryBushBlock.AGE, 1);
        }
        // 3) 可可豆：成熟(age>=2)后采收，age 归 0
        if (block instanceof CocoaBlock) {
            if (state.getValue(CocoaBlock.AGE) < 2) {
                return false;
            }
            return harvestAndRegrow(world, pos, state, caster, CocoaBlock.AGE, 0);
        }
        // 4) 通用回退：非 CropBlock 但带 "age" 属性的植株类作物（如下界疣及同类模组作物）
        //    甘蔗除外——它的 age 只是生长阶段，不是"成熟可摘"语义（FTBU 亦不将其视为可摘作物）
        if (block instanceof BushBlock && !(block instanceof SugarCaneBlock)) {
            IntegerProperty ageProperty = findAgeProperty(state);
            if (ageProperty != null
                    && state.getValue(ageProperty) >= Collections.max(ageProperty.getPossibleValues())) {
                return harvestAndRegrow(world, pos, state, caster, ageProperty, 0);
            }
        }
        return false;
    }

    /** 掉落原版收获物并原地把 age 退回幼苗阶段（植株不破坏，自动补种） */
    private boolean harvestAndRegrow(ServerLevel world, BlockPos pos, BlockState state, LivingEntity caster,
                                     IntegerProperty ageProperty, int newAge) {
        List<ItemStack> drops = Block.getDrops(state, world, pos, null, caster, ItemStack.EMPTY);
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.popResource(world, pos, drop);
            }
        }
        world.setBlock(pos, state.setValue(ageProperty, newAge), 3);
        return true;
    }

    /** 在方块状态里找名为 "age" 的整数属性（模组作物通用识别） */
    private static IntegerProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty
                    && integerProperty.getName().equals("age")) {
                return integerProperty;
            }
        }
        return null;
    }

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
        if (tilled == null || tilled == state) {
            if (!isHoeTillable(state.getBlock())) {
                return false;
            }
            tilled = Blocks.FARMLAND.defaultBlockState();
        }
        world.setBlock(pos, tilled, 3);
        return true;
    }

    private static boolean isHoeTillable(Block block) {
        return block == Blocks.DIRT
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.COARSE_DIRT
                || block == Blocks.PODZOL
                || block == Blocks.MYCELIUM
                || block == Blocks.DIRT_PATH;
    }
}
