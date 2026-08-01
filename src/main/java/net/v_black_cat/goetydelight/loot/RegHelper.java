package net.v_black_cat.goetydelight.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

/**
 * 1.21.1 移植版：注册自定义战利品条件类型（对应 1.20.1 loot/RegHelper）。
 */
public class RegHelper {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GoetyDelight.MODID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> ENTITY_TAG_CONDITION =
            LOOT_CONDITIONS.register("entity_tag",
                    () -> new LootItemConditionType(ModLootConditions.EntityTagCondition.CODEC));

    public static void register(IEventBus modEventBus) {
        LOOT_CONDITIONS.register(modEventBus);
    }
}
