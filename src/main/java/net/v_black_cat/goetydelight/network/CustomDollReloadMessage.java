package net.v_black_cat.goetydelight.network;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.CustomDollLoader;
import net.v_black_cat.goetydelight.init.ServerCustomDollLoader;
import vectorwing.farmersdelight.common.registry.ModCreativeTabs;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class CustomDollReloadMessage {
    private final Set<String> modelIds;

    public CustomDollReloadMessage(Set<String> modelIds) {
        this.modelIds = modelIds;
    }

    public static void encode(CustomDollReloadMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.modelIds.size());
        for (String modelId : message.modelIds) {
            buf.writeUtf(modelId);
        }
    }

    public static CustomDollReloadMessage decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<String> modelIds = Sets.newHashSet();
        for (int i = 0; i < size; i++) {
            modelIds.add(buf.readUtf());
        }
        return new CustomDollReloadMessage(modelIds);
    }

    public static void handle(CustomDollReloadMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> onReload(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onReload(CustomDollReloadMessage message) {
        CustomDollLoader.init();

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isLocalServer()) {
            ServerCustomDollLoader.getModels().clear();
            ServerCustomDollLoader.getModels().addAll(message.modelIds);
        }

        Minecraft minecraft = Minecraft.getInstance();
        FeatureFlagSet features = Optional.ofNullable(minecraft.player)
                .map(p -> p.connection)
                .map(ClientPacketListener::enabledFeatures)
                .orElse(FeatureFlagSet.of());

        final boolean hasOperatorItemsTabPermissions =
                minecraft.options.operatorItemsTab().get() ||
                        Optional.of(minecraft)
                                .map(m -> m.player)
                                .map(Player::canUseGameMasterBlocks)
                                .orElse(false);

        ClientLevel level = minecraft.level;
        if (level != null) {
            RegistryAccess registryAccess = level.registryAccess();
            final CreativeModeTab.ItemDisplayParameters parameters =
                    new CreativeModeTab.ItemDisplayParameters(features, hasOperatorItemsTabPermissions, registryAccess);
        }
    }
}
