package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.api.ritual.IRitualType;
import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.PedestalBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.RitualBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.ritual.Ritual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.Polarice3.Goety.api.ritual.RitualType.getRitualType;
import static com.Polarice3.Goety.common.ritual.Ritual.PEDESTAL_RANGE;
import static net.v_black_cat.goetydelight.util.RitualUtil.getItemsOnPedestals;
import static net.v_black_cat.goetydelight.util.RitualUtil.getPedestals;

public class MetamorphicScentGrassItem extends Item {
    public MetamorphicScentGrassItem(Properties properties) {
        super(properties);
    }

    public static void metamorphicScentGrassReciper(Level world, BlockPos pos, Player player,
                                                    InteractionHand hand, Direction face,
                                                    ItemStack activationItem,
                                                    RitualRecipe ritualRecipe){

        if (ritualRecipe!=null) {
            return;
        }
        if (activationItem.getFoodProperties(player)==null){
            return;
        }
        //验证幻味草配方
        List<ItemStack> itemsOnPedestals = getItemsOnPedestals(world, pos);
        if (itemsOnPedestals.size()!=12){
            return;
        }

        for(ItemStack itemStack: itemsOnPedestals){
            if (!(itemStack.getItem() instanceof MetamorphicScentGrassItem)){
                return;
            }
        }


        if(getRitualType("culinary")
                .getRequirement((RitualBlockEntity) world.getBlockEntity( pos), pos, world)){
            return;
        }




    }

}
