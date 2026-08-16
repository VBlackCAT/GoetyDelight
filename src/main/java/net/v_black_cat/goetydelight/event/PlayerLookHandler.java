package net.v_black_cat.goetydelight.event;


import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.util.ToolUtils;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLookHandler {


    private static final Supplier<Block> MARBLE_BLOCK_SUPPLIER = ModBlocks.SILT_MARBLE_HEAVY;

    private static final List<Supplier<Block>> MARBLE_BLOCKS = Collections.singletonList(
            MARBLE_BLOCK_SUPPLIER
    );

    private static final long COOLDOWN_TICKS = 100; // 冷却

    // 仅客户端本地玩家会访问此字段（服务端已在 onPlayerTick 中提前返回），
    // 因此 static 即可，不存在跨玩家/跨端共享问题。
    private static long lastSendTime=0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;

        // 粒子与提示均为纯客户端表现，服务端直接跳过，避免每 tick 无谓的射线检测
        if (!player.level().isClientSide()) return;

        long currentTime = player.level().getGameTime();


        ItemStack mainHandItem = player.getMainHandItem();
        boolean isHoldingPickaxe = ToolUtils.isPickaxe(mainHandItem);
        if (isHoldingPickaxe) {
                Level level = player.level();
                HitResult hitResult = player.pick(5.0D, 0.0F, false);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos pos = blockHitResult.getBlockPos();
                    Block block = level.getBlockState(pos).getBlock();

                    boolean isMarble = false;
                    for (Supplier<Block> marbleBlock : MARBLE_BLOCKS) {
                        Block marbleBlockInstance = marbleBlock.get();

                        if (block == marbleBlockInstance) {
                            isMarble = true;
                            break;
                        }
                    }

                    if(isMarble){
                        highlightMarbleBlock(level, pos);

                        if (currentTime-lastSendTime > COOLDOWN_TICKS) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("💦"));
                            lastSendTime=currentTime;
                        }
                    }


                }


        }
    }



    private static void highlightMarbleBlock(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            BlockState p_221055_ = level.getBlockState(pos);
            RandomSource p_221058_ = level.random;
            if (p_221058_.nextInt(10) == 0) {
                Direction direction = Direction.getRandom(p_221058_);
                if (direction != Direction.UP) {
                    BlockPos blockpos = pos.relative(direction);
                    BlockState blockstate = level.getBlockState(blockpos);
                    if (!p_221055_.canOcclude() || !blockstate.isFaceSturdy(level, blockpos, direction.getOpposite())) {
                        double d0 = direction.getStepX() == 0 ? p_221058_.nextDouble() : 0.5D + (double) direction.getStepX() * 0.6D;
                        double d1 = direction.getStepY() == 0 ? p_221058_.nextDouble() : 0.5D + (double) direction.getStepY() * 0.6D;
                        double d2 = direction.getStepZ() == 0 ? p_221058_.nextDouble() : 0.5D + (double) direction.getStepZ() * 0.6D;
                        level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, 0.2D, 0.2D, 0.2D);
                    }
                }
            }
        }
    }
}