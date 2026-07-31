package net.v_black_cat.goetydelight.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.v_black_cat.goetydelight.GoetyDelight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockStateModelLoader.class)
public class BlockStateModelLoaderMixin {

    @WrapOperation(
            method = "loadAllBlockStates",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/BlockStateModelLoader;loadBlockStateDefinitions(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/level/block/state/StateDefinition;)V"
            )
    )
    private void skipDollBlockStateLoading(BlockStateModelLoader instance, ResourceLocation blockStateId,
                                           StateDefinition<Block, ?> stateDefinition,
                                           Operation<Void> original) {
        // 跳过 GoetyDelight 的 doll 方块的 blockstate 加载
        if (blockStateId.getNamespace().equals(GoetyDelight.MODID) &&
                blockStateId.getPath().startsWith("doll_")) {
            return; // 直接返回，不加载 blockstate
        }

        // 其他方块正常加载
        original.call(instance, blockStateId, stateDefinition);
    }
}