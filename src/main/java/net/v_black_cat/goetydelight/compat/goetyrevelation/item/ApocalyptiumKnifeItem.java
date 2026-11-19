package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.compat.CompatManager;
import net.v_black_cat.goetydelight.compat.ftbultimine.FtbUltimineCompat;
import net.v_black_cat.goetydelight.compat.goetyrevelation.GoetyRevelationCompat;
import vectorwing.farmersdelight.common.item.KnifeItem;
import net.v_black_cat.goetydelight.util.DelightLootTableCache;
import com.Polarice3.Goety.common.blocks.SnapWartsBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApocalyptiumKnifeItem extends KnifeItem {
    public ApocalyptiumKnifeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof ApocalyptiumKnifeItem)) {
            return;
        }

        DamageSource source = event.getSource();
        Holder<DamageType> damageTypeHolder = source.typeHolder();

        if (damageTypeHolder instanceof Holder.Reference<DamageType> reference) {
            if (reference.is(DamageTypeTags.BYPASSES_ARMOR)
                    && reference.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)
                    && reference.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
                return;
            }

            Set<TagKey<DamageType>> merged = new HashSet<>(reference.tags().toList());
            merged.add(DamageTypeTags.BYPASSES_ARMOR);
            merged.add(DamageTypeTags.BYPASSES_ENCHANTMENTS);
            merged.add(DamageTypeTags.BYPASSES_RESISTANCE);

            reference.bindTags(merged);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (!(heldItem.getItem() instanceof ApocalyptiumKnifeItem)) {
            return;
        }

        // FTB Ultimine 激活时交给连锁逻辑处理，避免这里 cancel 掉连锁事件。
        if (CompatManager.isFtbUltimineLoaded() && FtbUltimineCompat.isActive(player)) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState blockState = level.getBlockState(pos);
        if (!isHarvestable(blockState)) {
            return;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            harvestKnife(serverLevel, pos, blockState, heldItem);
        }
        event.setCanceled(true);
    }

    /** 判断方块是否是本刀可收割的成熟作物。 */
    public static boolean isHarvestable(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        if (block instanceof SnapWartsBlock) {
            return state.getValue(SnapWartsBlock.AGE) >= 2;
        }
        return false;
    }

    private static void harvestKnife(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool) {
        spawnDropsInWorld(level, pos, collectKnifeDrops(level, pos, state, tool));
        shrinkHarvestState(level, pos, state);
    }

    /** 供 FTB Ultimine 连锁逻辑取用：不直接在世界中生成掉落物。 */
    public static List<ItemStack> collectKnifeDrops(ServerLevel level, BlockPos pos,
                                                    BlockState state, ItemStack tool) {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            drops.addAll(getBlockDrops(level, pos, state, tool));
        }
        return drops;
    }

    /** 收割后只降低一级年龄，和右键单块收割行为保持一致。 */
    public static void shrinkHarvestState(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        BlockState newState;
        if (block instanceof CropBlock cropBlock) {
            newState = cropBlock.getStateForAge(Math.max(0, cropBlock.getAge(state) - 1));
        } else if (block instanceof NetherWartBlock) {
            newState = state.setValue(NetherWartBlock.AGE,
                    Math.max(0, state.getValue(NetherWartBlock.AGE) - 1));
        } else if (block instanceof SnapWartsBlock) {
            newState = state.setValue(SnapWartsBlock.AGE,
                    Math.max(0, state.getValue(SnapWartsBlock.AGE) - 1));
        } else {
            return;
        }
        level.setBlock(pos, newState, 3);
        level.levelEvent(2001, pos, Block.getId(state));
    }

    @SubscribeEvent
    public static void onLivingDropsEvent(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof ApocalyptiumKnifeItem) {
                LivingEntity target = event.getEntity();
                ServerLevel level = (ServerLevel) target.level();
                EntityType<?> entityType = target.getType();
                List<ItemStack> delightDrops = DelightLootTableCache.getDelightDropsForMob(entityType);
                int lootingLevel = EnchantmentHelper.getTagEnchantmentLevel(
                        Enchantments.MOB_LOOTING,
                        heldItem
                );
                if (!delightDrops.isEmpty()) {
                    for (int extraDrop = 0; extraDrop < 2; extraDrop++) {
                        for (ItemStack dropStack : delightDrops) {
                            ItemStack droppedItem = dropStack.copy();
                            if (!droppedItem.isEmpty()) {
                                spawnDropItem(level, target, droppedItem);
                            }
                        }
                    }
                }
                if (lootingLevel > 0) {
                    int enhancedLooting = lootingLevel * 2;
                    for (ItemStack dropStack : delightDrops) {
                        int bonus = level.random.nextInt(enhancedLooting + 1);
                        if (bonus > 0) {
                            ItemStack bonusStack = dropStack.copy();
                            bonusStack.setCount(Math.min(bonus, bonusStack.getMaxStackSize()));

                            if (!bonusStack.isEmpty()) {
                                spawnDropItem(level, target, bonusStack);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 模拟普通作物掉落
     */
    private static void simulateCropDrops(ServerLevel level, BlockPos pos, BlockState state, CropBlock cropBlock, ItemStack tool) {
        // 获取作物的掉落物
        List<ItemStack> drops = getBlockDrops(level, pos, state, tool);

        // 生成掉落物实体
        spawnDropsInWorld(level, pos, drops);
    }

    /**
     * 模拟地狱疣掉落（重载方法）
     */
    private static void simulateCropDrops(ServerLevel level, BlockPos pos, BlockState state, NetherWartBlock netherWartBlock, ItemStack tool) {
        List<ItemStack> drops = getBlockDrops(level, pos, state, tool);
        spawnDropsInWorld(level, pos, drops);
    }

    /**
     * 模拟 SnapWarts 掉落（重载方法）
     */
    private static void simulateSnapWartsDrops(ServerLevel level, BlockPos pos, BlockState state, SnapWartsBlock snapWartsBlock, ItemStack tool) {
        List<ItemStack> drops = getBlockDrops(level, pos, state, tool);
        spawnDropsInWorld(level, pos, drops);
    }

    /**
     * 获取方块掉落物列表（通用方法）
     */
    public static List<ItemStack> getBlockDrops(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool) {
        List<ItemStack> drops = new ArrayList<>();

        try {
            drops.addAll(Block.getDrops(state, level, pos, null, null, tool));
        } catch (Exception e) {
            drops.clear();
        }

        return drops;
    }

    /**
     * 在指定位置生成掉落物实体
     */
    private static void spawnDropsInWorld(ServerLevel level, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        drop
                );
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
    }

    /**
     * 生成掉落物实体的辅助方法
     */
    private static void spawnDropItem(ServerLevel level, LivingEntity target, ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(
                level,
                target.getX(),
                target.getY(),
                target.getZ(),
                itemStack
        );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_knife.tooltip.1")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_knife.tooltip.2")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_knife.tooltip.3")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.tooltip.shift"));
        }
    }
}