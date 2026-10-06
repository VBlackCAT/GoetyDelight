package net.v_black_cat.goetydelight.compat.goetyrevelation;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.compat.goetyrevelation.block.ApollyonCakeBlock;
import net.v_black_cat.goetydelight.compat.goetyrevelation.block.ApollyonCakeData;

import java.util.List;

/**
 * 桥接类：所有对 goetyrevelation 模组的跨模组引用集中在这里。
 * Mixin 类只调用本类的静态方法，不直接 import goetyrevelation 的类。
 */
public final class GoetyRevelationBridge {

    private GoetyRevelationBridge() {}

    /** 目标仪式配方 ID */
    private static final ResourceLocation GOETYDELIGHT_THE_END_RITUAL =
            new ResourceLocation("goety_revelation", "the_end_ritual");

    /** 搜索半径（方块距离） */
    private static final int GOETYDELIGHT_CAKE_RADIUS = 10;

    /**
     * 仪式成功完成时调用。
     * 内部再做模组加载判断和异常保护。
     */
    public static void onRitualStop(DarkAltarBlockEntity self) {
        if (!GoetyRevelationCompat.IS_LOADED) {
            return;
        }
        try {
            handleRitualStop(self);
        } catch (Throwable t) {
            // 防止任何意外异常导致 Mixin 注入失败
            // 可换成你自己的 logger
            System.err.println("[goetydelight] GoetyRevelationBridge.onRitualStop failed: " + t);
        }
    }

    private static void handleRitualStop(DarkAltarBlockEntity self) {
        // 获取当前配方
        RitualRecipe recipe = self.getCurrentRitualRecipe();
        if (recipe == null) {
            return;
        }

        ResourceLocation recipeId = recipe.getId();
        if (recipeId == null || !GOETYDELIGHT_THE_END_RITUAL.equals(recipeId)) {
            return;
        }

        // 仅在服务端执行
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos centerPos = self.getBlockPos();

        ApollyonCakeData data = ApollyonCakeData.get(serverLevel);
        if (data == null) {
            return;
        }

        List<BlockPos> nearbyCakes = data.getCakesNear(
                serverLevel.dimension(),
                centerPos,
                GOETYDELIGHT_CAKE_RADIUS
        );

        if (nearbyCakes.isEmpty()) {
            return;
        }

        for (BlockPos cakePos : nearbyCakes) {
            BlockState state = serverLevel.getBlockState(cakePos);
            if (state.getBlock() instanceof ApollyonCakeBlock
                    && state.hasProperty(ApollyonCakeBlock.IS_THE_END)
                    && !state.getValue(ApollyonCakeBlock.IS_THE_END)) {
                serverLevel.setBlock(
                        cakePos,
                        state.setValue(ApollyonCakeBlock.IS_THE_END, true),
                        3
                );
            }
        }
    }
}