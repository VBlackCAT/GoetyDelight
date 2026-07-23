package net.v_black_cat.goetydelight.init;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.neoforged.bus.api.IEventBus;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModEnchantments {
    // 附魔的 ResourceKey（用于代码引用）
    public static final ResourceKey<Enchantment> FROST_ASPECT = key("frost_aspect");
    public static final ResourceKey<Enchantment> SOUL_MENDING = key("soul_mending");
    public static final ResourceKey<Enchantment> SOUL_HEALING = key("soul_healing");
    public static final ResourceKey<Enchantment> SOUL_AFFIX = key("soul_affix");
    public static final ResourceKey<Enchantment> SOUL_DRAIN = key("soul_drain");

    // Bootstrap 方法，在数据生成阶段注册附魔定义
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> itemGetter = context.lookup(Registries.ITEM);

        // 1. Frost Aspect (冰霜)
        register(context, FROST_ASPECT,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemGetter.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                itemGetter.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                5, // 权重
                                3, // 最大等级
                                Enchantment.dynamicCost(10, 5),
                                Enchantment.dynamicCost(20, 10),
                                2, // 铁砧花费
                                EquipmentSlotGroup.MAINHAND
                        )
                )
                // 效果：伤害额外增加 (可选)
                .withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(0.5F)))
        );

        register(context, SOUL_MENDING,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemGetter.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                                itemGetter.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                                2, // 权重较低
                                3, // 最大等级
                                Enchantment.dynamicCost(15, 9),
                                Enchantment.dynamicCost(30, 15),
                                4,
                                EquipmentSlotGroup.ANY
                        )
                )
        );

        register(context, SOUL_HEALING,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemGetter.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                                itemGetter.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                                1,
                                1,
                                Enchantment.constantCost(30),
                                Enchantment.constantCost(60),
                                4,
                                EquipmentSlotGroup.CHEST
                        )
                )
        );
        
        register(context, SOUL_AFFIX,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemGetter.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                itemGetter.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                2,
                                5,
                                Enchantment.dynamicCost(30, 9),
                                Enchantment.dynamicCost(60, 30),
                                6,
                                EquipmentSlotGroup.ANY
                        )
                )
        );

        register(context, SOUL_DRAIN,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemGetter.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                itemGetter.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                3,
                                3,
                                Enchantment.dynamicCost(30, 9),
                                Enchantment.dynamicCost(60, 30),
                                4,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, name));
    }
}
