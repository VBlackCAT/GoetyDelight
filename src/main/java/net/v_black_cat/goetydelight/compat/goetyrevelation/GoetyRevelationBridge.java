package net.v_black_cat.goetydelight.compat.goetyrevelation;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.compat.goetyrevelation.block.ApollyonCakeBlock;
import net.v_black_cat.goetydelight.compat.goetyrevelation.block.ApollyonCakeData;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class GoetyRevelationBridge {

    private GoetyRevelationBridge() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation THE_END_RITUAL =
            new ResourceLocation("goety_revelation", "the_end_ritual");

    private static final int CAKE_RADIUS = 10;

    /**
     * 由 Mixin 在 {@code DarkAltarBlockEntity#stopRitual(Z)V} 的 HEAD 处调用。
     * 此时 {@code castingPlayer} / {@code castingPlayerId} 尚未被 clearRitual 清空。
     */
    public static void onRitualStop(DarkAltarBlockEntity self) {
        if (!GoetyRevelationCompat.IS_LOADED) {
            return;
        }
        try {
            handleRitualStop(self);
        } catch (Throwable t) {
            LOGGER.error("[goetydelight] GoetyRevelationBridge.onRitualStop failed", t);
        }
    }

    @Nullable
    private static UUID resolveRitualOwner(DarkAltarBlockEntity self) {
        if (self.castingPlayer != null) {
            return self.castingPlayer.getUUID();
        }
        return self.castingPlayerId;
    }

    private static void handleRitualStop(DarkAltarBlockEntity self) {
        RitualRecipe recipe = self.getCurrentRitualRecipe();
        if (recipe == null) {
            return;
        }

        ResourceLocation recipeId = recipe.getId();
        if (recipeId == null || !THE_END_RITUAL.equals(recipeId)) {
            return;
        }

        if (!(self.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        @Nullable UUID ownerUuid = resolveRitualOwner(self);
        if (ownerUuid == null) {
            LOGGER.warn("[goetydelight] The End ritual finished without a resolvable owner UUID; skip activation.");
            return;
        }

        ApollyonCakeData data = ApollyonCakeData.get(serverLevel);
        if (data == null) {
            return;
        }

        List<BlockPos> nearbyCakes = data.getCakesNear(
                serverLevel.dimension(),
                self.getBlockPos(),
                CAKE_RADIUS
        );
        if (nearbyCakes.isEmpty()) {
            return;
        }

        for (BlockPos cakePos : nearbyCakes) {
            BlockState state = serverLevel.getBlockState(cakePos);
            if (!(state.getBlock() instanceof ApollyonCakeBlock)) {
                continue;
            }
            if (!state.hasProperty(ApollyonCakeBlock.IS_THE_END)) {
                continue;
            }
            if (state.getValue(ApollyonCakeBlock.IS_THE_END)) {
                continue;
            }

            ApollyonCakeBlock.setCakeOwner(serverLevel, cakePos, ownerUuid);
            serverLevel.setBlock(
                    cakePos,
                    state.setValue(ApollyonCakeBlock.IS_THE_END, true),
                    3
            );
        }
    }
}