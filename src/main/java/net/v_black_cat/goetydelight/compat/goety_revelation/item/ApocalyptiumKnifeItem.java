package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
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
import vectorwing.farmersdelight.common.item.KnifeItem;
import net.v_black_cat.goetydelight.util.DelightLootTableCache;
import com.Polarice3.Goety.common.blocks.SnapWartsBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber
public class ApocalyptiumKnifeItem extends KnifeItem {
    public ApocalyptiumKnifeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof ApocalyptiumKnifeItem) {
                DamageSource source = event.getSource();
                Holder<DamageType> damageTypeHolder = source.typeHolder();
                if (damageTypeHolder instanceof Holder.Reference<DamageType> reference) {
                    reference.bindTags(Set.of(
                            DamageTypeTags.BYPASSES_ARMOR,
                            DamageTypeTags.BYPASSES_ENCHANTMENTS,
                            DamageTypeTags.BYPASSES_RESISTANCE
                    ));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof ApocalyptiumKnifeItem) {
            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState blockState = level.getBlockState(pos);
            Block block = blockState.getBlock();
            if (block instanceof CropBlock cropBlock) {
                if (cropBlock.isMaxAge(blockState)) {
                    if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 2; i++) {
                            simulateCropDrops(serverLevel, pos, blockState, cropBlock, heldItem);
                        }
                        int currentAge = cropBlock.getAge(blockState);
                        int newAge = Math.max(0, currentAge - 1);
                        BlockState newState = cropBlock.getStateForAge(newAge);
                        level.setBlock(pos, newState, 3);
                        level.levelEvent(2001, pos, Block.getId(blockState));
                    }
                    event.setCanceled(true);
                }
            }
            else if (block instanceof NetherWartBlock netherWartBlock) {
                int currentAge = blockState.getValue(NetherWartBlock.AGE);
                if (currentAge >= NetherWartBlock.MAX_AGE) {
                    if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 2; i++) {
                            simulateCropDrops(serverLevel, pos, blockState, netherWartBlock, heldItem);
                        }
                        int newAge = Math.max(0, currentAge - 1);
                        BlockState newState = blockState.setValue(NetherWartBlock.AGE, newAge);
                        level.setBlock(pos, newState, 3);

                        level.levelEvent(2001, pos, Block.getId(blockState));
                    }

                    event.setCanceled(true);
                }
            }
            else if (block instanceof SnapWartsBlock snapWartsBlock) {
                int currentAge = blockState.getValue(SnapWartsBlock.AGE);
                if (currentAge >= 2) {
                    if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 2; i++) {
                            simulateSnapWartsDrops(serverLevel, pos, blockState, snapWartsBlock, heldItem);
                        }
                        int newAge = Math.max(0, currentAge - 1);
                        BlockState newState = blockState.setValue(SnapWartsBlock.AGE, newAge);
                        level.setBlock(pos, newState, 3);
                        level.levelEvent(2001, pos, Block.getId(blockState));
                    }

                    event.setCanceled(true);
                }
            }
        }
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
    private static List<ItemStack> getBlockDrops(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool) {
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
}