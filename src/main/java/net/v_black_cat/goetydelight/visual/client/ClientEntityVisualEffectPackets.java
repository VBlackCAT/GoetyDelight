package net.v_black_cat.goetydelight.visual.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;

public final class ClientEntityVisualEffectPackets {
    private ClientEntityVisualEffectPackets() {
    }

    public static void sync(int entityId, CompoundTag effectsTag) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (entity == null) {
            return;
        }

        entity.getCapability(EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS).ifPresent(effects -> effects.deserializeNBT(effectsTag));
    }
}
