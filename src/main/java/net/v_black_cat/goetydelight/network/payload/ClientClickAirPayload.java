package net.v_black_cat.goetydelight.network.payload;


import io.netty.buffer.ByteBuf;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;


public record ClientClickAirPayload(int itemId) implements CustomPacketPayload {


    public static final Type<ClientClickAirPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("goetydelight", "click_air"));


    public static final StreamCodec<ByteBuf, ClientClickAirPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientClickAirPayload::itemId, ClientClickAirPayload::new);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public Item getItem() {

        return BuiltInRegistries.ITEM.byId(itemId);

    }


    public static ClientClickAirPayload create(Item item) {

        return new ClientClickAirPayload(BuiltInRegistries.ITEM.getId(item));

    }

}