package net.v_black_cat.goetydelight.ritual;

import com.Polarice3.Goety.api.ritual.IRitualType;
import com.Polarice3.Goety.api.ritual.RitualType;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.Polarice3.Goety.common.items.ModItems.*;

public class DelightRitualType implements IRitualType {

    public static final String CULINARY = "culinary";

    @Override
    public String getName() {
        return CULINARY;
    }

    @Override
    public ItemStack getJeiIcon() {
        return new ItemStack(net.v_black_cat.goetydelight.init.ModItems.GOETYDELIGHT_ICON.get());
    }

    @Override
    public boolean getRequirement(RitualBlockEntity tileEntity, @Nullable Player player, BlockPos pos, Level level) {
        return checkDelightRequirements(pos, level, player);
    }

    private boolean checkDelightRequirements(BlockPos pos, Level level, @Nullable Player player) {
        final int RANGE = 8;
        Map<Block, Integer> blockRequirements = new HashMap<>();
        blockRequirements.put(Blocks.SMOKER, 2);
        // blockRequirements.put(ModBlocks.SHADE_STOVE.get(), 1);
        // blockRequirements.put(ModBlocks.CURSED_INGOT_POT.get(), 1);

        Map<Block, Integer> blockCounts = new HashMap<>();
        for (Block block : blockRequirements.keySet()) {
            blockCounts.put(block, 0);
        }

        // 统计范围内方块数量
        for (int i = -RANGE; i <= RANGE; ++i) {
            for (int j = -RANGE; j <= RANGE; ++j) {
                for (int k = -RANGE; k <= RANGE; ++k) {
                    BlockPos checkPos = pos.offset(i, j, k);
                    BlockState state = level.getBlockState(checkPos);
                    Block block = state.getBlock();
                    if (blockRequirements.containsKey(block)) {
                        blockCounts.put(block, blockCounts.get(block) + 1);
                    }
                }
            }
        }

        boolean allMet = true;
        List<Component> missingMessages = new ArrayList<>();

        for (Map.Entry<Block, Integer> entry : blockRequirements.entrySet()) {
            Block block = entry.getKey();
            int required = entry.getValue();
            int actual = blockCounts.get(block);
            if (actual < required) {
                allMet = false;
                int deficit = required - actual;
                missingMessages.add(Component.translatable(
                        "message.goetydelight.ritual.missing_block",
                        block.getName(), deficit
                ));
            }
        }

        // 如果有缺失且玩家在线，发送提示消息
        if (!allMet && player != null) {
            player.sendSystemMessage(Component.translatable("message.goetydelight.ritual.missing_header"));
            for (Component msg : missingMessages) {
                player.sendSystemMessage(msg);
            }
        }

        return allMet;
    }

    @Override
    public void onFinishRitual(Level world, BlockPos darkAltarPos,
                               DarkAltarBlockEntity tileEntity,
                               Player castingPlayer,
                               ItemStack activationItem) {
        RitualRecipe recipe = tileEntity.getCurrentRitualRecipe();

        if (recipe.getId().toString().equals("goetydelight:ritual/ominous_ramune")) {
            returnSpecialItems(world, darkAltarPos, castingPlayer, new ItemStack(OMINOUS_ORB.get()));
            returnSpecialItems(world, darkAltarPos, castingPlayer, new ItemStack(BOUNCY_BUBBLE_FOCUS.get()));
        } else if (recipe.getId().toString().equals("goetydelight:ritual/ominous_ramune_2")) {
            returnSpecialItems(world, darkAltarPos, castingPlayer, new ItemStack(OMINOUS_SHARD.get()));
            returnSpecialItems(world, darkAltarPos, castingPlayer, new ItemStack(BOUNCY_BUBBLE_FOCUS.get()));
        } else if (recipe.getId().toString().equals("goetydelight:metamorphic_scent_grass_ritual")) {
            returnSpecialItems(world, darkAltarPos, castingPlayer, activationItem.copyWithCount(1));
        } else if (recipe.getId().toString().equals("goetydelight:ritual/undeath_potion")) {
            returnSpecialItems(world, darkAltarPos, castingPlayer, new ItemStack(UNDEATH_POTION.get()));
        }

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

    private void returnSpecialItems(Level world, BlockPos darkAltarPos, Player player, ItemStack returnItem) {
        if (player != null) {
            if (!player.getInventory().add(returnItem)) {
                player.drop(returnItem, false);
            }
            world.playSound(null, darkAltarPos, SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            Block.popResource(world, darkAltarPos, returnItem);
            world.playSound(null, darkAltarPos, SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /**
     * 注册仪式类型
     */
    public static void registerRitualType() {
        DelightRitualType delightRitualType = new DelightRitualType();
        RitualType.addRitualType(CULINARY, delightRitualType);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DelightRitualType::registerRitualType);
    }

}