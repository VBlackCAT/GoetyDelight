package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static net.v_black_cat.goetydelight.item.food.MetamorphicScentGrassItem.metamorphicScentGrassAndFruitReciper;

@Mixin(DarkAltarBlockEntity.class)
public class DarkAltarBlockEntityMixin {

    @Inject(
            method = "activate(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/core/Direction;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/blocks/entities/DarkAltarBlockEntity;logRitualMatchFailure(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true,
            remap = false
    )
    private void onRitualMatchFailure(Level world,
                                      BlockPos pos,
                                      Player player,
                                      InteractionHand hand,
                                      Direction face,
                                      CallbackInfoReturnable<Boolean> cir,
                                      ItemStack activationItem,
                                      List<RecipeHolder<RitualRecipe>> ritualRecipes,
                                      RecipeHolder<RitualRecipe> ritualHolder,
                                      RitualRecipe ritualRecipe) {
        if (ritualRecipe != null) {
            return;
        }

        RitualRecipe custom = metamorphicScentGrassAndFruitReciper(
                world, pos, player,
                activationItem,
                null
        );

        if (custom == null) {
            return;
        }

        DarkAltarBlockEntity self = (DarkAltarBlockEntity) (Object) this;
        self.startRitual(player, activationItem, custom, custom.getId());
        cir.setReturnValue(true);
    }
}