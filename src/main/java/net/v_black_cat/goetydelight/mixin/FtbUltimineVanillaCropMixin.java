package net.v_black_cat.goetydelight.mixin;

import dev.ftb.mods.ftbultimine.ItemCollection;
import dev.ftb.mods.ftbultimine.crops.VanillaCropLikeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApocalyptiumKnifeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FTB Ultimine 自带的 CropBlock 处理器不认识天启镰刀的“双倍掉落 + 只降一级”。
 * 在连锁收割每个 CropBlock 时，若玩家手持天启镰刀，改用镰刀自己的收割逻辑。
 */
@Mixin(value = VanillaCropLikeHandler.class, remap = false)
public class FtbUltimineVanillaCropMixin {

    @Inject(method = "doHarvesting", at = @At("HEAD"), cancellable = true, remap = false)
    private void goetydelight$knifeHarvest(Player player, BlockPos pos, BlockState state,
                                           ItemCollection collection, CallbackInfo ci) {
        if (!(player.getMainHandItem().getItem() instanceof ApocalyptiumKnifeItem)
                || !ApocalyptiumKnifeItem.isHarvestable(state)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        for (ItemStack drop : ApocalyptiumKnifeItem.collectKnifeDrops(
                level, pos, state, player.getMainHandItem())) {
            collection.add(drop);
        }
        ApocalyptiumKnifeItem.shrinkHarvestState(level, pos, state);
        ci.cancel();
    }
}
