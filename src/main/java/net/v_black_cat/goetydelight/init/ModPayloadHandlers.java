package net.v_black_cat.goetydelight.init;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.v_black_cat.goetydelight.network.handler.ClientClickAirPayloadHandler;
import net.v_black_cat.goetydelight.network.payload.ClientClickAirPayload;
import net.v_black_cat.goetydelight.network.SyncBackModelPacket;

public class ModPayloadHandlers {
    private static final String VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        // 客户端→服务端：点击空气
        registrar.playToServer(
                ClientClickAirPayload.TYPE,
                ClientClickAirPayload.STREAM_CODEC,
                ClientClickAirPayloadHandler::handle
        );

        // 服务端→客户端：背部模型同步
        registrar.playToClient(
                SyncBackModelPacket.TYPE,
                SyncBackModelPacket.STREAM_CODEC,
                SyncBackModelPacket::handleClient
        );
    }
}
