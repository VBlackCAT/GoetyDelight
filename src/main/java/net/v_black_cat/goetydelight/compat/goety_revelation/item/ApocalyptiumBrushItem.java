package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.item.DarkBrushItem;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class ApocalyptiumBrushItem extends DarkBrushItem {

    private static final String EXTRA_DROPS_TAG = "ExtraDrops";
    private static final String EXTRA_LOOT_TABLE_TAG = "ExtraLootTable";
    private static final String EXTRA_LOOT_SEED_TAG = "ExtraLootSeed";
    private static final int EXTRA_DROP_COUNT = 2;

    public ApocalyptiumBrushItem(Properties pProperties) {
        super(pProperties, 20); // 2倍加速（原来是10，现在是20）
    }

    @Override
    public void repairTick(ItemStack stack, Entity entityIn, boolean isSelected) {
        if (stack.getItem() == ModItems.APOCALYPTIUM_INGOT_BRUSH.get()) {
            super.repairTick(stack, entityIn, isSelected);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (remainingUseDuration >= 0 && livingEntity instanceof Player player) {
            HitResult hitresult = this.calculateHitResult(livingEntity);
            if (hitresult instanceof BlockHitResult blockhitresult) {
                if (hitresult.getType() == HitResult.Type.BLOCK) {
                    int i = this.getUseDuration(stack) - remainingUseDuration + 1;
                    boolean flag = i % 5 / ACCELERATION_FACTOR == 0;

                    if (flag) {
                        BlockPos blockpos = blockhitresult.getBlockPos();
                        BlockState blockstate = level.getBlockState(blockpos);
                        HumanoidArm humanoidarm = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                        this.spawnDustParticles(level, blockhitresult, blockstate, livingEntity.getViewVector(0.0F), humanoidarm);

                        Block block = blockstate.getBlock();
                        SoundEvent soundevent;
                        if (block instanceof BrushableBlock brushableBlock) {
                            soundevent = brushableBlock.getBrushSound();
                        } else {
                            soundevent = SoundEvents.BRUSH_GENERIC;
                        }

                        level.playSound(player, blockpos, soundevent, SoundSource.BLOCKS);

                        if (!level.isClientSide()) {
                            BlockEntity blockentity = level.getBlockEntity(blockpos);
                            if (blockentity instanceof BrushableBlockEntity brushableblockentity) {

                                // 获取NBT数据
                                CompoundTag tag = stack.getOrCreateTag();
                                int accumulatedOffset = tag.getInt(ACCUMULATED_OFFSET_TAG);

                                // 如果是第一次刷扫这个方块，预先抽取额外掉落
                                if (accumulatedOffset == 0 && !tag.contains(EXTRA_LOOT_TABLE_TAG)) {
                                    preExtractExtraLoot(level, player, blockpos, blockhitresult.getDirection(),
                                            brushableblockentity, tag);
                                }

                                boolean brushingCompleted = false;
                                long baseTime = level.getGameTime();

                                // 使用累计偏移量进行加速（2倍速度）
                                for (int j = 0; j < ACCELERATION_FACTOR; j++) {
                                    long fakeTime = baseTime + accumulatedOffset + j;
                                    boolean result = brushableblockentity.brush(fakeTime, player, blockhitresult.getDirection());

                                    if (result) {
                                        brushingCompleted = true;
                                        break;
                                    }
                                }

                                // 更新累计偏移量
                                accumulatedOffset += ACCELERATION_FACTOR;
                                tag.putInt(ACCUMULATED_OFFSET_TAG, accumulatedOffset);

                                if (brushingCompleted) {
                                    // 刷扫完成，掉落预先抽取的物品
                                    dropPreExtractedLoot(level, player, blockpos, blockhitresult.getDirection(), tag);

                                    EquipmentSlot equipmentslot = stack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND)) ?
                                            EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                                    stack.hurtAndBreak(1, livingEntity, (p_279044_) -> {
                                        p_279044_.broadcastBreakEvent(equipmentslot);
                                    });

                                    // 重置累计偏移量
                                    tag.remove(ACCUMULATED_OFFSET_TAG);
                                    // 清理额外掉落数据
                                    tag.remove(EXTRA_DROPS_TAG);
                                    tag.remove(EXTRA_LOOT_TABLE_TAG);
                                    tag.remove(EXTRA_LOOT_SEED_TAG);
                                }

                                stack.setTag(tag);
                            }
                        }
                    }

                    return;
                }
            }

            livingEntity.releaseUsingItem();
        } else {
            livingEntity.releaseUsingItem();
        }
    }

    /**
     * 预先抽取额外掉落物品
     */
    private void preExtractExtraLoot(Level level, Player player, BlockPos blockpos, Direction hitDirection,
                                     BrushableBlockEntity brushableblockentity, CompoundTag tag) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 获取战利品表信息
        ResourceLocation lootTableLocation = brushableblockentity.lootTable;
        long lootTableSeed = brushableblockentity.lootTableSeed;

        if (lootTableLocation == null) {
            return;
        }

        try {
            // 获取战利品表
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableLocation);

            // 创建战利品参数
            LootParams lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos))
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.BLOCK_STATE, serverLevel.getBlockState(blockpos))
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.CHEST);

            // 预先抽取额外掉落物品
            List<ItemStack> extraDrops = new ArrayList<>();
            for (int i = 0; i < EXTRA_DROP_COUNT; i++) {
                List<ItemStack> drops = lootTable.getRandomItems(lootParams, lootTableSeed + i * 1000L);
                for (ItemStack drop : drops) {
                    if (drop != null && !drop.isEmpty()) {
                        extraDrops.add(drop.copy());
                    }
                }
            }

            // 保存到NBT
            ListTag dropsList = new ListTag();
            for (ItemStack drop : extraDrops) {
                CompoundTag itemTag = new CompoundTag();
                drop.save(itemTag);
                dropsList.add(itemTag);
            }

            tag.put(EXTRA_DROPS_TAG, dropsList);
            tag.putString(EXTRA_LOOT_TABLE_TAG, lootTableLocation.toString());
            tag.putLong(EXTRA_LOOT_SEED_TAG, lootTableSeed);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 掉落预先抽取的物品
     */
    private void dropPreExtractedLoot(Level level, Player player, BlockPos blockpos, Direction hitDirection,
                                      CompoundTag tag) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!tag.contains(EXTRA_DROPS_TAG)) {
            return;
        }

        ListTag dropsList = tag.getList(EXTRA_DROPS_TAG, 10); // 10 = CompoundTag type
        for (int i = 0; i < dropsList.size(); i++) {
            CompoundTag itemTag = dropsList.getCompound(i);
            ItemStack drop = ItemStack.of(itemTag);
            if (!drop.isEmpty()) {
                spawnItemEntity(serverLevel, blockpos, hitDirection, drop);
            }
        }
    }

    /**
     * 在指定位置生成物品实体
     */
    private void spawnItemEntity(ServerLevel level, BlockPos blockpos, Direction hitDirection, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        double d0 = 0.5;
        double d1 = 1.0 - d0;
        double d2 = d0 / 2.0;

        Direction direction = hitDirection != null ? hitDirection : Direction.UP;
        BlockPos spawnPos = blockpos.relative(direction, 1);

        double d3 = spawnPos.getX() + 0.5 * d1 + d2;
        double d4 = spawnPos.getY() + 0.5;
        double d5 = spawnPos.getZ() + 0.5 * d1 + d2;

        ItemEntity itemEntity = new ItemEntity(level, d3, d4, d5, itemStack);
        itemEntity.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(itemEntity);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // 玩家中断刷扫，清空所有预先抽取的数据
            tag.remove(ACCUMULATED_OFFSET_TAG);
            tag.remove(EXTRA_DROPS_TAG);
            tag.remove(EXTRA_LOOT_TABLE_TAG);
            tag.remove(EXTRA_LOOT_SEED_TAG);
        }
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }
}