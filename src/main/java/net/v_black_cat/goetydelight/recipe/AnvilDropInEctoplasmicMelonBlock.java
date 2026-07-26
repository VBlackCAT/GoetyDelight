package net.v_black_cat.goetydelight.recipe;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.event.AnvilLandInBlockEvent;

/**
 * 处理铁砧坠落到灵质西瓜块上的逻辑
 * 当铁砧落在灵质西瓜块上时，将其转换为9个灵质
 */
@Mod.EventBusSubscriber(modid = "goetydelight", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnvilDropInEctoplasmicMelonBlock {

    private static final ResourceLocation ECTOPLASMIC_MELON_BLOCK_ID =
            new ResourceLocation("goetydelight", "ectoplasmic_melon_block");
    public static int DROP_COUNT = 9;

    @SubscribeEvent
    public static void onAnvilLand(AnvilLandInBlockEvent event) {
        Level level = event.getLevel();

        // 只在服务器端处理
        if (level.isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState blockBelow = event.getBlockBelow();

        // 检查是否是灵质西瓜块
        if (!isEctoplasmicMelonBlock(blockBelow)) {
            return;
        }

        // 执行转换
        convertToEctoplasm((ServerLevel) level, pos, event.getEntity());

        // 取消铁砧放置
        event.setCanceled(true);
    }

    /**
     * 检查方块是否是灵质西瓜块
     */
    private static boolean isEctoplasmicMelonBlock(BlockState state) {
        return state.getBlockHolder().is(ECTOPLASMIC_MELON_BLOCK_ID);
    }

    /**
     * 将灵质西瓜块转换为9个灵质
     */
    private static void convertToEctoplasm(ServerLevel level, BlockPos pos, FallingBlockEntity entity) {
        // 移除灵质西瓜块
        level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);

        // 生成9个灵质，每个单独掉落
        for (int i = 0; i < DROP_COUNT; i++) {
            ItemStack ectoplasmStack = new ItemStack(ModItems.ECTOPLASM.get(), 1);

            // 在铁砧位置周围随机散布
            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;

            double x = entity != null ? entity.getX() + offsetX : pos.getX() + 0.5 + offsetX;
            double y = entity != null ? entity.getY() + 0.5 : pos.getY() + 0.5;
            double z = entity != null ? entity.getZ() + offsetZ : pos.getZ() + 0.5 + offsetZ;

            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level, x, y, z, ectoplasmStack
            );

            // 每个灵质有不同的随机速度
            itemEntity.setDeltaMovement(
                    (level.random.nextDouble() - 0.5) * 0.4,
                    0.15 + level.random.nextDouble() * 0.25,
                    (level.random.nextDouble() - 0.5) * 0.4
            );

            // 设置拾取延迟
            itemEntity.setPickUpDelay(10);

            level.addFreshEntity(itemEntity);
        }

        // 播放特效
        spawnConversionEffects(level, pos);
    }

    /**
     * 生成转换特效
     */
    private static void spawnConversionEffects(ServerLevel level, BlockPos pos) {
        // 生成粒子效果
        for (int i = 0; i < 30; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;
            double y = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.PORTAL,
                    x, y, z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.5,
                    (level.random.nextDouble() - 0.5) * 0.5,
                    (level.random.nextDouble() - 0.5) * 0.5,
                    0.1
            );
        }

        // 播放音效
        level.playSound(
                null,
                pos,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }
}