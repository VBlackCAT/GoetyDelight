package net.v_black_cat.goetydelight.entities;

import com.Polarice3.Goety.common.entities.neutral.AbstractWraith;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModEntityType {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GoetyDelight.MODID);

    public static final RegistryObject<EntityType<AbstractWraith>> ABSTRACTWRAITH =
            ENTITY_TYPE.register("abstractwraith",
                    () -> EntityType.Builder.of(AbstractWraith::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build(new net.minecraft.resources.ResourceLocation(GoetyDelight.MODID, "abstractwraith").toString())
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPE.register(eventBus);
    }
}
