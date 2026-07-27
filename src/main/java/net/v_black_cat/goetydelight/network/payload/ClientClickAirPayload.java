package net.v_black_cat.goetydelight.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.v_black_cat.goetydelight.item.food.EternalRefusalOfBlackMeatSoupItem;
import net.v_black_cat.goetydelight.item.food.RejectedDarkMeatSoupItem;

public record ClientClickAirPayload(int itemId) implements CustomPacketPayload {

    public static final Type<
            ClientClickAirPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("goetydelight", "click_air"));

    public static final StreamCodec<
            ByteBuf, ClientClickAirPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientClickAirPayload::itemId,
            ClientClickAirPayload::new
    );

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

    public static void handle(ClientClickAirPayload payload, IPayloadContext context) {
        // 确保在服务端线程执行
        context.enqueueWork(() -> {
            var player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            // 获取玩家主手物品
            ItemStack stack = serverPlayer.getMainHandItem();
            Item item = stack.getItem();

            // 根据物品类型执行投掷逻辑
            if (item instanceof EternalRefusalOfBlackMeatSoupItem eternalSoup) {
                // 服务端二次校验冷却
                if (!eternalSoup.isOnCooldown(stack, serverPlayer.level())) {
                    eternalSoup.throwSoup(stack, serverPlayer);
                }
            } else if (item instanceof RejectedDarkMeatSoupItem rejectedSoup) {
                // 普通版投掷（无冷却）
                rejectedSoup.throwSoup(stack, serverPlayer);
            }
            // 其他物品忽略
        });
    }
}