package net.v_black_cat.goetydelight.item.food;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.ability.AbilityRegistry;
import net.v_black_cat.goetydelight.ability.TimedAbilitySystem;
import net.v_black_cat.goetydelight.api.GetSpellAttributeFactory;

import java.util.UUID;

public class RubyHardCandyItem extends Item {
    // 免伤持续时间（10分钟，以tick为单位）
    private static final int DAMAGE_REDUCTION_DURATION = 20 * 60 * 10;
    // 最大强效等级
    private static final int MAX_POTENCY_LEVEL = 3;
    // 强效等级NBT标签
    private static final String POTENCY_LEVEL_TAG = "RubyCandyPotencyLevel";
    // 法术强度属性修改器的UUID
    private static final UUID SPELL_POTENCY_UUID = UUID.fromString("8b4513a0-4e2a-11ee-be56-0242ac120004");
    private static Attribute ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getSpellPotencyAttributeModifier();

    public RubyHardCandyItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // 获取当前强效等级
            int currentLevel = getPotencyLevel(player);

            // 如果未达到最大等级，增加强效等级
            if (currentLevel < MAX_POTENCY_LEVEL) {
                increasePotencyLevel(player);

                // 添加50%免伤能力
                boolean success = TimedAbilitySystem.addAbilityToEntity(
                        entity,
                        AbilityRegistry.RUBY_HARD_CANDY_DAMAGE_REDUCTION,
                        DAMAGE_REDUCTION_DURATION
                );

                if (success) {
                    // 消耗物品（如果不是创造模式）
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
            } else {
                // 已达到最大等级，只添加免伤效果
                TimedAbilitySystem.addAbilityToEntity(
                        entity,
                        AbilityRegistry.RUBY_HARD_CANDY_DAMAGE_REDUCTION,
                        DAMAGE_REDUCTION_DURATION
                );

                // 消耗物品（如果不是创造模式）
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }

    // 获取玩家的强效等级
    private int getPotencyLevel(Player player) {
        return player.getPersistentData().getInt(POTENCY_LEVEL_TAG);
    }

    // 增加玩家的强效等级
    private void increasePotencyLevel(Player player) {
        int currentLevel = getPotencyLevel(player);
        if (currentLevel < MAX_POTENCY_LEVEL) {
            player.getPersistentData().putInt(POTENCY_LEVEL_TAG, currentLevel + 1);

            // 应用强效等级效果（每级增加10点法术强度）
            applyPotencyEffect(player, currentLevel + 1);
        }
    }

    // 应用强效等级效果
    private void applyPotencyEffect(Player player, int level) {
        // 移除可能存在的旧属性修改器
        removePotencyEffect(player);

        // 只有当ATTRIBUTE不为null时才应用效果
        if (ATTRIBUTE != null) {
            // 计算法术强度加成值（每级10点）
            double potencyBonus = 2 * level;

            // 创建属性修改器
            AttributeModifier modifier = new AttributeModifier(
                    SPELL_POTENCY_UUID,
                    "Ruby Hard Candy Potency Bonus",
                    potencyBonus,
                    AttributeModifier.Operation.ADDITION
            );

            // 应用属性修改器
            if (player.getAttribute(ATTRIBUTE) != null) {
                player.getAttribute(ATTRIBUTE).addPermanentModifier(modifier);
            }

            // 存储当前修饰器信息以便后续移除
            player.getPersistentData().putDouble("RubyCandyPotencyValue", potencyBonus);
        }
    }

    // 移除强效等级效果
    private void removePotencyEffect(Player player) {
        // 只有当ATTRIBUTE不为null时才尝试移除效果
        if (ATTRIBUTE != null && player.getAttribute(ATTRIBUTE) != null) {
            // 移除所有同UUID的修改器
            player.getAttribute(ATTRIBUTE).removeModifier(SPELL_POTENCY_UUID);
        }
    }

    // 获取当前法术强度加成值
    public static double getCurrentPotencyBonus(Player player) {
        // 只有当ATTRIBUTE不为null时才尝试获取加成值
        if (ATTRIBUTE != null && player.getAttribute(ATTRIBUTE) != null) {
            // 检查是否有修改器
            AttributeModifier modifier = player.getAttribute(ATTRIBUTE).getModifier(SPELL_POTENCY_UUID);
            if (modifier != null) {
                return modifier.getAmount();
            }
        }
        return 0;
    }

    // 伤害减免事件处理器
    @Mod.EventBusSubscriber
    public static class DamageReductionHandler {

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity entity = event.getEntity();

            // 只在服务端处理
            if (entity.level().isClientSide) return;

            // 检查实体是否有免伤能力
            boolean hasDamageReduction = TimedAbilitySystem.hasAbility(
                    entity,
                    AbilityRegistry.RUBY_HARD_CANDY_DAMAGE_REDUCTION
            );

            // 如果有免伤能力，减少50%伤害
            if (hasDamageReduction) {
                float reducedDamage = event.getAmount() * 0.5f;
                event.setAmount(reducedDamage);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // 重新应用红宝石硬糖的强效等级效果
        int potencyLevel = player.getPersistentData().getInt("RubyCandyPotencyLevel");
        if (potencyLevel > 0 && ATTRIBUTE != null) {
            // 重新应用效果
            double potencyBonus = 2 * potencyLevel;
            AttributeModifier modifier = new AttributeModifier(
                    UUID.fromString("8b4513a0-4e2a-11ee-be56-0242ac120004"),
                    "Ruby Hard Candy Potency Bonus",
                    potencyBonus,
                    AttributeModifier.Operation.ADDITION
            );
            Attribute attribute = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getSpellPotencyAttributeModifier();

            if (attribute != null && player.getAttribute(attribute) != null) {
                player.getAttribute(attribute).addPermanentModifier(modifier);
            }
        }
    }
}