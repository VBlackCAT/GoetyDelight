package net.v_black_cat.goetydelight.event;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockRightClickEventHandler {


    private static final Supplier<Block> SILT_MARBLE_HEAVY_SUPPLIER = ModBlocks.SILT_MARBLE_HEAVY;

    private static Block getSpecialBlock() {
        return ModBlocks.SILT_MARBLE_HEAVY.get();
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        Block specialBlock = SILT_MARBLE_HEAVY_SUPPLIER.get();

        if (event.getLevel().getBlockState(event.getPos()).getBlock() == specialBlock) {

            Player player = event.getEntity();

            if (!event.getLevel().isClientSide()) {

                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("💦"));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}