package net.v_black_cat.goetydelight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.ability.TimedAbilitySystem;

import java.util.function.Supplier;

public class SyncAbilityPacket {
    private final int entityId;
    private final String abilityId;
    private final boolean add; // true表示添加，false表示移除

    public SyncAbilityPacket(int entityId, String abilityId, boolean add) {
        this.entityId = entityId;
        this.abilityId = abilityId;
        this.add = add;
    }

    public static void encode(SyncAbilityPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeUtf(msg.abilityId);
        buffer.writeBoolean(msg.add);
    }

    public static SyncAbilityPacket decode(FriendlyByteBuf buffer) {
        return new SyncAbilityPacket(buffer.readInt(), buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(SyncAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // 在客户端线程上执行
        ctx.get().enqueueWork(() -> {
            // 获取客户端世界和实体
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            Entity entity = mc.level.getEntity(msg.entityId);

            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                if (msg.add) {
                    // 在客户端临时模拟添加能力（仅用于渲染等视觉效果）
                    // 这里我们直接操作客户端的能力系统，或者设置一个仅供客户端使用的标记
                    // 例如，我们可以将能力ID存储到实体的Tag中，客户端检查这个Tag
                    livingEntity.getPersistentData().putBoolean("ClientSide_" + msg.abilityId, true);
                } else {
                    // 移除客户端的模拟能力
                    livingEntity.getPersistentData().remove("ClientSide_" + msg.abilityId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}