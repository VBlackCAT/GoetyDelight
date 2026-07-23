package net.v_black_cat.goetydelight.network.handler;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.v_black_cat.goetydelight.item.food.RejectedDarkMeatSoupItem;
import net.v_black_cat.goetydelight.network.payload.ClientClickAirPayload;


public class ClientClickAirPayloadHandler {


    public static void handle(ClientClickAirPayload payload, IPayloadContext context) {


        context.enqueueWork(() -> {


            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }


            Item item = payload.getItem();


            if (item == null) {
                return;
            }


            handleClickAir(player, item);


        });


    }


    private static void handleClickAir(ServerPlayer player, Item item) {


        ItemStack stack = player.getMainHandItem();

        //拒绝黑肉汤
        if (item instanceof RejectedDarkMeatSoupItem soup) {
            soup.throwSoup(stack, player);
        }



    }

}