package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import vectorwing.farmersdelight.common.item.KnifeItem;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class DarkKnifeItem extends KnifeItem {

    private static final float ADDED_DAMAGE = 2.0f;

    public DarkKnifeItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(
                ItemAttributeModifiers.builder()
                        // ★ 基础攻击力 = tier.getAttackDamageBonus() （从 ModTiers.DARK 读取）
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
                        // 额外主手加成
                        /*          .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("dark_knife_boost"),
                                ADDED_DAMAGE,
                                AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        // 副手加成
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("dark_knife_boost_offhand"),
                                ADDED_DAMAGE,
                                AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.OFFHAND)     */
                        .build()
        )
        );
    }

    /**
     * 1.20.1 行为：手持暗刀攻击时造成 1.5 倍伤害。
     * 1.20.1 的实现检查的是受伤实体的主手，这里按意图改为检查攻击者主手。
     */
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker.getMainHandItem().getItem() instanceof DarkKnifeItem) {
            event.setAmount(event.getAmount() * 1.5f);
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        System.out.println("Repair item: " + repair.getItem());
        System.out.println("DARK_ALLOY_INGOT: " + ModItems.DARK_ALLOY_INGOT.get());
        System.out.println("Are they the same? " + (repair.getItem() == ModItems.DARK_ALLOY_INGOT.get()));
        return repair.is(ModItems.DARK_ALLOY_INGOT.get());
    }
}
