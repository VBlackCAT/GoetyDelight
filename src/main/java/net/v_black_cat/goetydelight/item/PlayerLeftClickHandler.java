package net.v_black_cat.goetydelight.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.ThrowSoupPacket;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class PlayerLeftClickHandler {

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        if (event instanceof PlayerInteractEvent.LeftClickEmpty) {

            Player player = event.getEntity();
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof RejectedDarkMeatSoupItem) {
                // 发送网络包通知服务器
                NetworkHandler.sendToServer(new ThrowSoupPacket(player.getUUID()));
            }
        }
}}