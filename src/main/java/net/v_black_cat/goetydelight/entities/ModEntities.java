package net.v_black_cat.goetydelight.entities;


import com.Polarice3.Goety.common.entities.hostile.Wraith;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GoetyDelight.MODID);

    public static final RegistryObject<EntityType<GhostFarmerEntity>> GHOST_FARMER =
            ENTITY_TYPES.register("ghost_farmer", () -> EntityType.Builder.of(GhostFarmerEntity::new, MobCategory.CREATURE).sized(0.6F, 1.99F).clientTrackingRange(8).build("ghost_farmer"));



    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
