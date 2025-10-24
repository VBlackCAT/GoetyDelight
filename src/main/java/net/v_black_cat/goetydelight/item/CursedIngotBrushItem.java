package net.v_black_cat.goetydelight.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
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

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            super.onUseTick(level, livingEntity, stack, remainingUseDuration);
            return;
        }

        
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);

        if (!level.isClientSide) {
            int i = this.getUseDuration(stack) - remainingUseDuration + 1;

            
            boolean isTriggerTick = (i % 23 == 23 / 2);

            if (isTriggerTick) {
                HitResult hitResult = this.calculateHitResult(player);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos pos = blockHitResult.getBlockPos();
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    if (blockEntity instanceof BrushableBlockEntity brushableEntity) {
                        BrushableBlockEntityAccessor accessor = (BrushableBlockEntityAccessor) brushableEntity;
                        int currentBrushCount = accessor.getBrushCount();

                        
                        int newBrushCount = Math.min(10, currentBrushCount + 1);
                        accessor.setBrushCount(newBrushCount);

                        if (newBrushCount >= 10) {
                            level.scheduleTick(pos, brushableEntity.getBlockState().getBlock(), 1);
                        }
                    }
                }
            }
        }
    }
    
}