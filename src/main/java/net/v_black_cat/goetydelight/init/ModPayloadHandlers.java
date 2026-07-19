package net.v_black_cat.goetydelight.init;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.v_black_cat.goetydelight.network.handler.ClientClickAirPayloadHandler;
import net.v_black_cat.goetydelight.network.payload.ClientClickAirPayload;
<<<<<<< HEAD
=======
import net.v_black_cat.goetydelight.network.SyncBackModelPacket;
>>>>>>> 7998848 (炉灶、锅、以及没写完的FalseProverbs)

public class ModPayloadHandlers {
    private static final String VERSION = "1";

<<<<<<< HEAD
    public static void register(
            RegisterPayloadHandlersEvent event
    ) {

        PayloadRegistrar registrar =
                event.registrar(VERSION);

=======
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        // 客户端→服务端：点击空气
>>>>>>> 7998848 (炉灶、锅、以及没写完的FalseProverbs)
        registrar.playToServer(
                ClientClickAirPayload.TYPE,
                ClientClickAirPayload.STREAM_CODEC,
                ClientClickAirPayloadHandler::handle
        );

<<<<<<< HEAD

    }
}
=======
        // 服务端→客户端：背部模型同步
        registrar.playToClient(
                SyncBackModelPacket.TYPE,
                SyncBackModelPacket.STREAM_CODEC,
                SyncBackModelPacket::handleClient
        );
    }
}
>>>>>>> 7998848 (炉灶、锅、以及没写完的FalseProverbs)
