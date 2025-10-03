package net.v_black_cat.goetydelight.ritual;

import com.Polarice3.Goety.api.ritual.IRitualType;
import com.Polarice3.Goety.api.ritual.RitualType;
import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.HashMap;
import java.util.Map;

public class DelightRitualType implements IRitualType {
    @Override
    public String getName() {
        // 唯一标识符，用于后续注册
        return "culinary";
    }

    @Override
    public ItemStack getJeiIcon() {
        // JEI显示的图标
        return new ItemStack(ModBlocks.CURSED_INGOT_POT.get());
    }

    @Override
    public boolean getRequirement(RitualBlockEntity tileEntity, BlockPos pos, Level level) {
        // 自定义仪式条件检查逻辑
        return checkDelightRequirements(pos, level);
    }

    private boolean checkDelightRequirements(BlockPos pos, Level level) {
        // 使用与原版相同的检测范围
        final int RANGE = 5;

        // 创建方块要求映射：方块类型 -> 需要数量
        Map<Block, Integer> blockRequirements = new HashMap<>();
        blockRequirements.put(Blocks.HONEY_BLOCK, 5);      // 需要5个蜂蜜块
        blockRequirements.put(ModBlocks.NIGHT_STOVE.get(), 1);      // 需要3个种植小麦
        blockRequirements.put(ModBlocks.CURSED_INGOT_POT.get(), 1);      // 需要腐尸蛆虫盛宴方块

        // 创建计数器映射：方块类型 -> 当前计数
        Map<Block, Integer> blockCounts = new HashMap<>();
        for (Block block : blockRequirements.keySet()) {
            blockCounts.put(block, 0);
        }

        // 遍历检测范围内的所有方块
        for (int i = -RANGE; i <= RANGE; ++i) {
            for (int j = -RANGE; j <= RANGE; ++j) {
                for (int k = -RANGE; k <= RANGE; ++k) {
                    BlockPos checkPos = pos.offset(i, j, k);
                    BlockState state = level.getBlockState(checkPos);
                    Block block = state.getBlock();

                    // 如果这个方块在我们的要求列表中，增加计数
                    if (blockRequirements.containsKey(block)) {
                        blockCounts.put(block, blockCounts.get(block) + 1);
                    }
                }
            }
        }

        // 检查所有要求是否满足
        for (Map.Entry<Block, Integer> entry : blockRequirements.entrySet()) {
            Block block = entry.getKey();
            int requiredCount = entry.getValue();
            int actualCount = blockCounts.get(block);

            if (actualCount < requiredCount) {
                return false; // 有一个要求不满足就返回false
            }
        }

        return true; // 所有要求都满足
    }

    //仪式完成后发生啥事
    @Override
    public void onFinishRitual(Level world, BlockPos darkAltarPos,
                               DarkAltarBlockEntity tileEntity,
                               Player castingPlayer,
                               ItemStack activationItem) {
        // 生成特殊实体
        Entity delightEntity = EntityType.BEE.create(world);
        delightEntity.moveTo(darkAltarPos.getX(), darkAltarPos.getY() + 1, darkAltarPos.getZ());
        world.addFreshEntity(delightEntity);

        // 给予玩家效果
        castingPlayer.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                600,  // 30秒
                1
        ));

        // 播放音效和粒子
        world.playSound(null, darkAltarPos, SoundEvents.BELL_RESONATE,
                SoundSource.BLOCKS, 1.0F, 0.5F);

        for (int i = 0; i < 20; i++) {
            world.addParticle(ParticleTypes.HEART,
                    darkAltarPos.getX() + 0.5 + world.random.nextGaussian() * 0.5,
                    darkAltarPos.getY() + 1.5,
                    darkAltarPos.getZ() + 0.5 + world.random.nextGaussian() * 0.5,
                    0, 0.1, 0);
        }
    }

//在模组主类调用这个方法注册仪式
    public static void registerRitualType() {
        DelightRitualType delightRitualType = new DelightRitualType();
        RitualType.create(
                "DELIGHT",
                delightRitualType
        );
    }


}