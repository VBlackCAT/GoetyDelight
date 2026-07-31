package net.v_black_cat.goetydelight.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.function.Supplier;

public class ModAdvancementsTrigger {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, GoetyDelight.MODID);
    public static final Supplier<PlayerBeKilledTrigger> GHOST_FARMER_KILL_PLAYER =
            TRIGGERS.register("ghost_farmer_kill_player",
                    () -> new PlayerBeKilledTrigger(
                            ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "ghost_farmer_kill_player")
                    ));

    public static void init() {
    }
}