package net.v_black_cat.goetydelight.util;


import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.network.PacketDistributor;

import net.v_black_cat.goetydelight.network.payload.ClientClickAirPayload;


public class PayloadUtil {


    public static void sendClickAir(Item item) {


        Minecraft mc = Minecraft.getInstance();


        if (mc.player == null) {
            return;
        }


        PacketDistributor.sendToServer(ClientClickAirPayload.create(item));


    }

}