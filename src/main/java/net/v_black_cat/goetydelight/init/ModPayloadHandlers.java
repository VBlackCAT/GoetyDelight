package net.v_black_cat.goetydelight.init;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.v_black_cat.goetydelight.network.handler.ClientClickAirPayloadHandler;
import net.v_black_cat.goetydelight.network.payload.ClientClickAirPayload;

public class ModPayloadHandlers {
    private static final String VERSION = "1";

    public static void register(
            RegisterPayloadHandlersEvent event
    ) {

        PayloadRegistrar registrar =
                event.registrar(VERSION);

        registrar.playToServer(
                ClientClickAirPayload.TYPE,
                ClientClickAirPayload.STREAM_CODEC,
                ClientClickAirPayloadHandler::handle
        );


    }
}
