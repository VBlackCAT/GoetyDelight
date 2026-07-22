package net.v_black_cat.goetydelight.visual.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientEntityVisualEffectPackets {
    private static final Map<Integer, CompoundTag> PENDING_SYNCS = new HashMap<>();

    private ClientEntityVisualEffectPackets() {
    }

    public static void sync(int entityId, CompoundTag effectsTag) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            PENDING_SYNCS.clear();
            return;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) {
            PENDING_SYNCS.put(entityId, effectsTag.copy());
        } else {
            apply(entity, effectsTag);
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (PENDING_SYNCS.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            PENDING_SYNCS.clear();
            return;
        }
        Iterator<Map.Entry<Integer, CompoundTag>> iterator = PENDING_SYNCS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, CompoundTag> entry = iterator.next();
            Entity entity = minecraft.level.getEntity(entry.getKey());
            if (entity != null) {
                apply(entity, entry.getValue());
                iterator.remove();
            }
        }
    }

    private static void apply(Entity entity, CompoundTag tag) {
        var effects = EntityVisualEffectSystem.getEffects(entity);
        if (effects != null) effects.deserializeNBT(tag);
    }
}
