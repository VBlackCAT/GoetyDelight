package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.common.entities.hostile.Wraith;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.v_black_cat.goetydelight.entities.soul_lich.SoulLichEntity;
import net.v_black_cat.goetydelight.init.ModEntities;

public class ModEntityAttributesHandler {
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.GHOST_FARMER.get(), Wraith.setCustomAttributes().build());
        event.put(ModEntities.SOUL_LICH.get(), SoulLichEntity.createAttributes());
    }
}
