package net.v_black_cat.goetydelight.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModBlocks;

/**
 * 1.21.1 移植版（对应 1.20.1 PlayerLookHandler + BlockRightClickEventHandler）：
 * 手持镐子看向粗沉积大理石时冒泪滴粒子并提示；右键粗沉积大理石时提示。
 */
@EventBusSubscriber(modid = GoetyDelight.MODID)
public class MarbleHighlightHandler {
    private static final long COOLDOWN_TICKS = 100;
    // 仅客户端本地玩家会访问此字段（服务端已在 onPlayerTick 中提前返回），
    // 因此 static 即可，不存在跨玩家/跨端共享问题。
    private static long lastSendTime = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // 粒子与提示均为纯客户端表现，服务端直接跳过，避免每 tick 无谓的射线检测
        if (!player.level().isClientSide()) return;

        long currentTime = player.level().getGameTime();

        ItemStack mainHandItem = player.getMainHandItem();
        if (!mainHandItem.is(ItemTags.PICKAXES)) return;

        Level level = player.level();
        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockPos pos = blockHitResult.getBlockPos();
        Block block = level.getBlockState(pos).getBlock();
        if (block != ModBlocks.SILT_MARBLE_HEAVY.get()) return;

        highlightMarbleBlock(level, pos);

        if (currentTime - lastSendTime > COOLDOWN_TICKS) {
            player.sendSystemMessage(Component.literal("哈！！！！！！！"));
            lastSendTime = currentTime;
        }
    }

    private static void highlightMarbleBlock(Level level, BlockPos pos) {
        if (!level.isClientSide()) return;

        BlockState state = level.getBlockState(pos);
        RandomSource random = level.random;
        if (random.nextInt(10) != 0) return;

        Direction direction = Direction.getRandom(random);
        if (direction == Direction.UP) return;

        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (state.canOcclude() && neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite())) {
            return;
        }

        double d0 = direction.getStepX() == 0 ? random.nextDouble() : 0.5D + direction.getStepX() * 0.6D;
        double d1 = direction.getStepY() == 0 ? random.nextDouble() : 0.5D + direction.getStepY() * 0.6D;
        double d2 = direction.getStepZ() == 0 ? random.nextDouble() : 0.5D + direction.getStepZ() * 0.6D;
        level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2,
                0.2D, 0.2D, 0.2D);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getPos()).getBlock() == ModBlocks.SILT_MARBLE_HEAVY.get()) {
            if (!event.getLevel().isClientSide()) {
                event.getEntity().sendSystemMessage(Component.literal("💦"));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
