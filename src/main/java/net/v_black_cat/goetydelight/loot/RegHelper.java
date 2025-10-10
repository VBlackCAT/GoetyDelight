package net.v_black_cat.goetydelight.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class RegHelper {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GoetyDelight.MODID);

    // 注册实体标签条件
    public static final RegistryObject<LootItemConditionType> ENTITY_TAG_CONDITION =
            LOOT_CONDITIONS.register("entity_tag",
                    () -> new LootItemConditionType(new ModLootConditions.EntityTagCondition.Serializer()));
    public static final RegistryObject<LootItemConditionType> KILLED_WITH_EQUIPMENT_CONDITION =
            LOOT_CONDITIONS.register("killed_with_equipment",
                    () -> new LootItemConditionType(new ModLootConditions.KilledWithEquipmentCondition.Serializer()));
}