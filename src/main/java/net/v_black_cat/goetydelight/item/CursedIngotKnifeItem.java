package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import vectorwing.farmersdelight.common.item.KnifeItem;

public class CursedIngotKnifeItem extends KnifeItem {

    private static final float ADDED_DAMAGE = 0.0f; // 不加额外伤害

    public CursedIngotKnifeItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(
                ItemAttributeModifiers.builder()
                        // ★ 基础攻击力 = tier.getAttackDamageBonus() （从 ModTiers.SPECIAL 读取）
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("base_attack_damage"),
                                tier.getAttackDamageBonus(),
                                AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("base_attack_speed"),
                                -2.0,
                                AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        // 额外主手加成（0）
                   /*     .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("cursed_ingot_boost"),
                                ADDED_DAMAGE,
                                AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        // 副手加成（0）
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("cursed_ingot_boost_offhand"),
                                ADDED_DAMAGE,
                                AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.OFFHAND)    */
                        .build()
        )
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.CURSED_METAL_INGOT.get());
    }
}