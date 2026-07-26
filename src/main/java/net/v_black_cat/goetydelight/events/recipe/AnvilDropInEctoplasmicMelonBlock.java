package net.v_black_cat.goetydelight.events.recipe;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.v_black_cat.goetydelight.events.AnvilLandInBlockEvent;

@EventBusSubscriber(modid = "goetydelight")   
public class AnvilDropInEctoplasmicMelonBlock {

    private static final ResourceLocation ECTOPLASMIC_MELON_BLOCK_ID =
            ResourceLocation.parse("goetydelight:ectoplasmic_melon_block");
    public static final int DROP_COUNT = 9;

    @SubscribeEvent
    public static void onAnvilLand(AnvilLandInBlockEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState blockBelow = event.getBlockBelow();

        if (!isEctoplasmicMelonBlock(blockBelow)) return;

        convertToEctoplasm((ServerLevel) level, pos, event.getEntity());
        event.setCanceled(true);   // 标记取消（配合 Mixin 后续处理）
    }

    private static boolean isEctoplasmicMelonBlock(BlockState state) {
        // 通过注册表获取 Block 对象进行比较
        return state.is(BuiltInRegistries.BLOCK.get(ECTOPLASMIC_MELON_BLOCK_ID));
    }

    private static void convertToEctoplasm(ServerLevel level, BlockPos pos, FallingBlockEntity entity) {
        level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);

        for (int i = 0; i < DROP_COUNT; i++) {
            ItemStack ectoplasmStack = new ItemStack(ModItems.ECTOPLASM.get(), 1);

            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;

            double x = entity != null ? entity.getX() + offsetX : pos.getX() + 0.5 + offsetX;
            double y = entity != null ? entity.getY() + 0.5 : pos.getY() + 0.5;
            double z = entity != null ? entity.getZ() + offsetZ : pos.getZ() + 0.5 + offsetZ;

            ItemEntity itemEntity = new ItemEntity(level, x, y, z, ectoplasmStack);
            itemEntity.setDeltaMovement(
                    (level.random.nextDouble() - 0.5) * 0.4,
                    0.15 + level.random.nextDouble() * 0.25,
                    (level.random.nextDouble() - 0.5) * 0.4
            );
            itemEntity.setPickUpDelay(10);
            level.addFreshEntity(itemEntity);
        }

        spawnConversionEffects(level, pos);
    }

    private static void spawnConversionEffects(ServerLevel level, BlockPos pos) {
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

        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}