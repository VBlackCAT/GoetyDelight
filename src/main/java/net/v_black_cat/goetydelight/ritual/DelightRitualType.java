package net.v_black_cat.goetydelight.ritual;

import com.Polarice3.Goety.api.ritual.IRitualType;
import com.Polarice3.Goety.api.ritual.RitualType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.v_black_cat.goetydelight.init.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static com.Polarice3.Goety.common.items.ModItems.*;

/**
 * 「美食」仪式类型：需求祭坛 8 格范围内有 2 个烟熏炉、1 个幽影炉、1 个诅咒金属锅。
 *
 * <p>性能优化（对照 Goety 原版 {@code RitualRequirements} 的 BlockFinder 风格）：
 * 原实现每次调用都做 17³ = 4913 次 {@code getBlockState} 且每次 {@code pos.offset} 分配一个 BlockPos，
 * spark 采样显示占服务端约 1.1%。现在：
 * 1) 需求表提为 static final，消除每次调用的 Map 分配；
 * 2) 复用 {@link BlockPos.MutableBlockPos} 零分配扫描，并跳过空气方块；
 * 3) 按祭坛实体做 20 tick 结果缓存（结构需求短期内不会变化，最多滞后 1 秒）。
 */
public class DelightRitualType implements IRitualType {

    public static final String CULINARY = "culinary";

    private static final int RANGE = 8;
    private static final int CACHE_TICKS = 20;

    // 需求方块与数量（静态表，避免每次调用构建 HashMap；类在 common setup 时才加载，注册表已就绪）
    private static final Block[] REQUIRED_BLOCKS = {
            Blocks.SMOKER,
            ModBlocks.SHADE_STOVE.get(),
            ModBlocks.CURSED_INGOT_POT.get()
    };
    private static final int[] REQUIRED_COUNTS = {2, 1, 1};

    // 需求结果缓存：WeakHashMap 键随祭坛方块实体生命周期自动回收，避免长期持有
    private static final Map<RitualBlockEntity, RequirementCache> REQUIREMENT_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final class RequirementCache {
        final long lastTick;
        final boolean result;

        RequirementCache(long lastTick, boolean result) {
            this.lastTick = lastTick;
            this.result = result;
        }
    }

    @Override
    public String getName() {
        return CULINARY;
    }

    @Override
    public ItemStack getJeiIcon() {
        return new ItemStack(ModBlocks.CURSED_INGOT_POT.get());
    }

    @Override
    public boolean getRequirement(RitualBlockEntity tileEntity, @Nullable Player player, BlockPos pos, Level level) {
        return checkDelightRequirements(tileEntity, pos, level, player);
    }

    private boolean checkDelightRequirements(@Nullable RitualBlockEntity tileEntity, BlockPos pos, Level level,
                                             @Nullable Player player) {
        long now = level.getGameTime();

        // 缓存命中：20 tick 内直接返回上次结果，避免每 tick 做 4913 次方块扫描
        if (tileEntity != null) {
            RequirementCache cache = REQUIREMENT_CACHE.get(tileEntity);
            if (cache != null && now - cache.lastTick < CACHE_TICKS) {
                return cache.result;
            }
        }

        int[] found = new int[REQUIRED_BLOCKS.length];
        int baseX = pos.getX(), baseY = pos.getY(), baseZ = pos.getZ();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        // 统计范围内方块数量（复用 MutableBlockPos，零分配；空气方块直接跳过）
        for (int i = -RANGE; i <= RANGE; ++i) {
            for (int j = -RANGE; j <= RANGE; ++j) {
                for (int k = -RANGE; k <= RANGE; ++k) {
                    mutable.set(baseX + i, baseY + j, baseZ + k);
                    BlockState state = level.getBlockState(mutable);
                    if (state.isAir()) continue;
                    Block block = state.getBlock();
                    for (int r = 0; r < REQUIRED_BLOCKS.length; r++) {
                        if (block == REQUIRED_BLOCKS[r]) {
                            found[r]++;
                            break;
                        }
                    }
                }
            }
        }

        boolean allMet = true;
        List<Component> missingMessages = new ArrayList<>();

        for (int r = 0; r < REQUIRED_BLOCKS.length; r++) {
            int required = REQUIRED_COUNTS[r];
            int actual = found[r];
            if (actual < required) {
                allMet = false;
                missingMessages.add(Component.translatable(
                        "info.goety.ritual.structure.noBlocks",
                        REQUIRED_BLOCKS[r].getName(), required - actual
                ));
            }
        }

        // 如果有缺失且玩家在线，发送提示消息（随缓存降频，不再每 tick 刷屏）
        if (!allMet && player != null) {
            player.sendSystemMessage(Component.translatable("message.goetydelight.ritual.missing_header"));
            for (Component msg : missingMessages) {
                player.sendSystemMessage(msg);
            }
        }

        if (tileEntity != null) {
            REQUIREMENT_CACHE.put(tileEntity, new RequirementCache(now, allMet));
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
