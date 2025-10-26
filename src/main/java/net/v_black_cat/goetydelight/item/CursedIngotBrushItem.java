package net.v_black_cat.goetydelight.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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
import net.v_black_cat.goetydelight.mixin.BrushableBlockEntityAccessor;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public class CursedIngotBrushItem extends BrushItem {
    static {
        MAX_BRUSH_DISTANCE = Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0;
    }
    private static final double MAX_BRUSH_DISTANCE;

    


    public CursedIngotBrushItem(Properties pProperties) {
        super(pProperties);
    }

    private HitResult calculateHitResult(LivingEntity entity) {
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

                    //修改刷扫间隔
                    boolean flag = i % 2 == 0;
                    if (flag) {
                        BlockPos blockpos = blockhitresult.getBlockPos();
                        BlockState blockstate = level.getBlockState(blockpos);
                        HumanoidArm humanoidarm = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                        this.spawnDustParticles(level, blockhitresult, blockstate, livingEntity.getViewVector(0.0F), humanoidarm);
                        Block $$18 = blockstate.getBlock();
                        SoundEvent soundevent;
                        if ($$18 instanceof BrushableBlock) {
                            BrushableBlock brushableblock = (BrushableBlock)$$18;
                            soundevent = brushableblock.getBrushSound();
                        } else {
                            soundevent = SoundEvents.BRUSH_GENERIC;
                        }

                        level.playSound(player, blockpos, soundevent, SoundSource.BLOCKS);

                        if (!level.isClientSide()) {
                            BlockEntity blockentity = level.getBlockEntity(blockpos);




                            if (blockentity instanceof BrushableBlockEntity) {
                                BrushableBlockEntity brushableblockentity = (BrushableBlockEntity)blockentity;
                                boolean flag1 = brushableblockentity.brush(level.getGameTime(), player, blockhitresult.getDirection());
                                if(blockentity instanceof BrushableBlockEntityAccessor accessor){
                                    //减少方块冷却
                                    accessor.setCoolDownEndsAtTick((long) (accessor.getBrushCountResetsAtTick()/1.5));
                                }
                                if (flag1) {
                                    EquipmentSlot equipmentslot = stack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                                    stack.hurtAndBreak(1, livingEntity, (p_279044_) -> {
                                        p_279044_.broadcastBreakEvent(equipmentslot);
                                    });

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
    
}