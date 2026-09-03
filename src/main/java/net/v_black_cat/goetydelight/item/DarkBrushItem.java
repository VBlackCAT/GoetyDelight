package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.api.items.ISoulRepair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import com.Polarice3.Goety.utils.ItemHelper;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public class DarkBrushItem extends BrushItem implements ISoulRepair {

    public int ACCELERATION_FACTOR = 10;
    public static final String ACCUMULATED_OFFSET_TAG = "AccumulatedOffset";

    public DarkBrushItem(Properties pProperties, int acceleration_factor) {
        super(pProperties);
        this.ACCELERATION_FACTOR = acceleration_factor;
    }

    public static final double MAX_BRUSH_DISTANCE;
    static {
        MAX_BRUSH_DISTANCE = Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0;
    }

@Override
public void repairTick(ItemStack stack, Entity entityIn, boolean isSelected) {
    if (stack.getItem() == ModItems.DARK_BRUSH.get()) {
        ISoulRepair.super.repairTick(stack, entityIn, isSelected);
    }
}

    public HitResult calculateHitResult(LivingEntity entity) {
        return ProjectileUtil.getHitResultOnViewVector(entity, (p_281111_) -> {
            return !p_281111_.isSpectator() && p_281111_.isPickable();
        }, MAX_BRUSH_DISTANCE);
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (remainingUseDuration >= 0 && livingEntity instanceof Player player) {
            HitResult hitresult = this.calculateHitResult(livingEntity);
            if (hitresult instanceof BlockHitResult blockhitresult) {
                if (hitresult.getType() == HitResult.Type.BLOCK) {
                    int i = this.getUseDuration(stack) - remainingUseDuration + 1;
                    boolean flag = i % 5/ACCELERATION_FACTOR == 0;

                    if (flag) {
                        BlockPos blockpos = blockhitresult.getBlockPos();
                        BlockState blockstate = level.getBlockState(blockpos);
                        HumanoidArm humanoidarm = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                        this.spawnDustParticles(level, blockhitresult, blockstate, livingEntity.getViewVector(0.0F), humanoidarm);

                        Block block = blockstate.getBlock();
                        SoundEvent soundevent;
                        if (block instanceof BrushableBlock) {
                            BrushableBlock brushableblock = (BrushableBlock)block;
                            soundevent = brushableblock.getBrushSound();
                        } else {
                            soundevent = SoundEvents.BRUSH_GENERIC;
                        }

                        level.playSound(player, blockpos, soundevent, SoundSource.BLOCKS);

                        if (!level.isClientSide()) {
                            BlockEntity blockentity = level.getBlockEntity(blockpos);
                            if (blockentity instanceof BrushableBlockEntity) {
                                BrushableBlockEntity brushableblockentity = (BrushableBlockEntity)blockentity;

                                // 获取或初始化累计偏移量
                                CompoundTag tag = stack.getOrCreateTag();
                                int accumulatedOffset = tag.getInt(ACCUMULATED_OFFSET_TAG);

                                boolean flag1 = false;
                                long baseTime = level.getGameTime();

                                // 使用累计偏移量进行加速
                                for (int j = 0; j < ACCELERATION_FACTOR; j++) {
                                    long fakeTime = baseTime + accumulatedOffset + j;
                                    boolean result = brushableblockentity.brush(fakeTime, player, blockhitresult.getDirection());

                                    if (result) {
                                        flag1 = true;
                                        break;
                                    }
                                }

                                // 更新累计偏移量
                                accumulatedOffset += ACCELERATION_FACTOR;
                                tag.putInt(ACCUMULATED_OFFSET_TAG, accumulatedOffset);
                                stack.setTag(tag);

                                if (flag1) {
                                    EquipmentSlot equipmentslot = stack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                                    stack.hurtAndBreak(1, livingEntity, (p_279044_) -> {
                                        p_279044_.broadcastBreakEvent(equipmentslot);
                                    });

                                    // 刷扫完成后重置累计偏移量
                                    tag.remove(ACCUMULATED_OFFSET_TAG);
                                }
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

    // 当玩家停止使用刷子时重置累计偏移量
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(ACCUMULATED_OFFSET_TAG);
        }
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }
}