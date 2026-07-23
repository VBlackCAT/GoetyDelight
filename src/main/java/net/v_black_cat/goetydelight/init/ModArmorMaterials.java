package net.v_black_cat.goetydelight.init;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.EnumMap;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ARMOR_MATERIAL, GoetyDelight.MODID);

    // 示例材料（需替换为实际属性）
    // public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EXAMPLE_MATERIAL =
    //         ARMOR_MATERIALS.register("example_material", () -> new ArmorMaterial(
    //                 Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
    //                     map.put(ArmorItem.Type.BOOTS, 2);
    //                     map.put(ArmorItem.Type.LEGGINGS, 5);
    //                     map.put(ArmorItem.Type.CHESTPLATE, 6);
    //                     map.put(ArmorItem.Type.HELMET, 2);
    //                 }),
    //                 15,
    //                 SoundEvents.ARMOR_EQUIP_CHAIN,
    //                 () -> Ingredient.of(ModItems.EXAMPLE_ITEM.get()),
    //                 0.0F,
    //                 0.0F,
    //                 net.minecraft.resources.ResourceLocation.withDefaultNamespace("trims/items/leggings_trim")
    //         ));

    public static void register(IEventBus modEventBus) {
        ARMOR_MATERIALS.register(modEventBus);
    }
}