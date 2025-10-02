package net.v_black_cat.goetydelight.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import com.Polarice3.Goety.utils.ItemHelper;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public class DarkBrushItem extends BrushItem {

    public DarkBrushItem(Properties pProperties) {
        super(pProperties);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {

            Player player = event.player;
            if (player.tickCount % 20 == 0) {
            if (!player.level().isClientSide()) {

                ItemStack mainHandStack = player.getMainHandItem();

                if (mainHandStack.getItem() instanceof DarkBrushItem) {

                        ItemHelper.repairTick(mainHandStack, player, true);
                    }
                }
            }
        }
    }
}