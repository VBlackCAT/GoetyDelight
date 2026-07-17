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
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;
import net.v_black_cat.goetydelight.api.GetSpellAttributeFactory;

import java.util.HashMap;
import java.util.Map;
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

    // 为各种法术强度属性创建UUID映射
    private static final Map<String, UUID> ATTRIBUTE_UUIDS = new HashMap<>();
    private static final Map<Attribute, String> ATTRIBUTE_NAMES = new HashMap<>();

    static {
        ATTRIBUTE_UUIDS.put("spell_potency", UUID.fromString("8b4513a0-4e2a-11ee-be56-0242ac120004"));
        ATTRIBUTE_UUIDS.put("abyss_potency", UUID.fromString("294201d2-5085-48ea-89eb-351ffa677ea0"));
        ATTRIBUTE_UUIDS.put("frost_potency", UUID.fromString("083c98be-adba-43a1-9d70-689e8adda03f"));
        ATTRIBUTE_UUIDS.put("geomancy_potency", UUID.fromString("cdda777a-a9e7-4bf7-b13e-04f8c235034f"));
        ATTRIBUTE_UUIDS.put("necromancy_potency", UUID.fromString("1ad3e5bd-5aaa-44d0-8655-5bcf01c3f0fd"));
        ATTRIBUTE_UUIDS.put("nether_potency", UUID.fromString("a9a1c501-d310-4e2f-93db-cfaf40c2a2b7"));
        ATTRIBUTE_UUIDS.put("storm_potency", UUID.fromString("056efc77-e10c-4f5a-b766-74243a4c9e6c"));
        ATTRIBUTE_UUIDS.put("void_potency", UUID.fromString("a628709f-075f-4a66-8bc6-8b16e6f4c822"));
        ATTRIBUTE_UUIDS.put("wild_potency", UUID.fromString("59d8c77b-2acb-499c-8178-31eea06c4e2b"));

        // 初始化属性名称映射
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getSpellPotencyAttributeModifier(), "spell_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getAbyssPotencyAttributeModifier(), "abyss_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getFrostPotencyAttributeModifier(), "frost_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getGeomancyPotencyAttributeModifier(), "geomancy_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getNecromancyPotencyAttributeModifier(), "necromancy_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getNetherPotencyAttributeModifier(), "nether_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getStormPotencyAttributeModifier(), "storm_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getVoidPotencyAttributeModifier(), "void_potency");
        ATTRIBUTE_NAMES.put(GetSpellAttributeFactory.createGetSpellAttributeImplementation().getWildPotencyAttributeModifier(), "wild_potency");
    }

    private static final Attribute ABYSS_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getAbyssPotencyAttributeModifier();
    private static final Attribute FROST_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getFrostPotencyAttributeModifier();
    private static final Attribute GEOMANCY_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getGeomancyPotencyAttributeModifier();
    private static final Attribute NECROMANCY_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getNecromancyPotencyAttributeModifier();
    private static final Attribute NETHER_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getNetherPotencyAttributeModifier();
    private static final Attribute STORM_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getStormPotencyAttributeModifier();
    private static final Attribute VOID_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getVoidPotencyAttributeModifier();
    private static final Attribute WILD_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getWildPotencyAttributeModifier();

    public RubyHardCandyItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        
        stack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            
            int currentLevel = getPotencyLevel(player);

            
            if (currentLevel < MAX_POTENCY_LEVEL) {
                increasePotencyLevel(player);

                
                BuffUtil.applyBuff(
                        entity,
                        ModBuffTypes.RUBY_HARD_CANDY_DAMAGE_REDUCTION.getId(),
                        DAMAGE_REDUCTION_DURATION,
                        0
                );
            } else {
                
                BuffUtil.applyBuff(
                        entity,
                        ModBuffTypes.RUBY_HARD_CANDY_DAMAGE_REDUCTION.getId(),
                        DAMAGE_REDUCTION_DURATION,
                        0
                );
            }

            
        }

        return stack;
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

            // 应用强效等级效果（每级增加1点法术强度）
            applyPotencyEffect(player, currentLevel + 1);
        }
    }

    // 应用强效等级效果
    public void applyPotencyEffect(Player player, int level) {
        // 移除可能存在的旧属性修改器
        removePotencyEffect(player);

        // 计算法术强度加成值（每级1点）
        double potencyBonus = 1.0 * level;

        // 为所有非空属性应用效果
        applyAttributeModifier(player, ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, ABYSS_ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, FROST_ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, GEOMANCY_ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, NECROMANCY_ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, NETHER_ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, STORM_ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, VOID_ATTRIBUTE, potencyBonus);
        applyAttributeModifier(player, WILD_ATTRIBUTE, potencyBonus);

        // 存储当前修饰器信息以便后续移除
        player.getPersistentData().putDouble("RubyCandyPotencyValue", potencyBonus);
    }

    // 应用单个属性修改器
    private void applyAttributeModifier(Player player, Attribute attribute, double bonus) {
        if (attribute != null) {
            String attributeName = ATTRIBUTE_NAMES.get(attribute);
            if (attributeName != null) {
                UUID uuid = ATTRIBUTE_UUIDS.get(attributeName);
                if (uuid != null) {
                    AttributeModifier modifier = new AttributeModifier(
                            uuid,
                            "Ruby Hard Candy Potency Bonus",
                            bonus,
                            AttributeModifier.Operation.ADDITION
                    );

                    if (player.getAttribute(attribute) != null) {
                        player.getAttribute(attribute).addPermanentModifier(modifier);
                    }
                }
            }
        }
    }

    // 移除强效等级效果
    private void removePotencyEffect(Player player) {
        removeAttributeModifier(player, ATTRIBUTE);
        removeAttributeModifier(player, ABYSS_ATTRIBUTE);
        removeAttributeModifier(player, FROST_ATTRIBUTE);
        removeAttributeModifier(player, GEOMANCY_ATTRIBUTE);
        removeAttributeModifier(player, NECROMANCY_ATTRIBUTE);
        removeAttributeModifier(player, NETHER_ATTRIBUTE);
        removeAttributeModifier(player, STORM_ATTRIBUTE);
        removeAttributeModifier(player, VOID_ATTRIBUTE);
        removeAttributeModifier(player, WILD_ATTRIBUTE);
    }

    // 移除单个属性修改器
    private void removeAttributeModifier(Player player, Attribute attribute) {
        if (attribute != null && player.getAttribute(attribute) != null) {
            String attributeName = ATTRIBUTE_NAMES.get(attribute);
            if (attributeName != null) {
                UUID uuid = ATTRIBUTE_UUIDS.get(attributeName);
                if (uuid != null) {
                    player.getAttribute(attribute).removeModifier(uuid);
                }
            }
        }
    }

    // 获取当前法术强度加成值
    public static double getCurrentPotencyBonus(Player player, Attribute attribute) {
        if (attribute != null && player.getAttribute(attribute) != null) {
            String attributeName = ATTRIBUTE_NAMES.get(attribute);
            if (attributeName != null) {
                UUID uuid = ATTRIBUTE_UUIDS.get(attributeName);
                if (uuid != null) {
                    AttributeModifier modifier = player.getAttribute(attribute).getModifier(uuid);
                    if (modifier != null) {
                        return modifier.getAmount();
                    }
                }
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
            boolean hasDamageReduction = BuffUtil.hasBuff(
                    entity,
                    ModBuffTypes.RUBY_HARD_CANDY_DAMAGE_REDUCTION.getId()
            );

            // 如果有免伤能力，减少50%伤害
            if (hasDamageReduction) {
                float reducedDamage = event.getAmount() * 0.75f;
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
        if (potencyLevel > 0) {
            // 重新应用效果
            double potencyBonus = 1.0 * potencyLevel;
            
            // 为所有属性重新应用效果
            reapplyAttributeModifier(player, ATTRIBUTE, potencyBonus, "spell_potency");
            reapplyAttributeModifier(player, ABYSS_ATTRIBUTE, potencyBonus, "abyss_potency");
            reapplyAttributeModifier(player, FROST_ATTRIBUTE, potencyBonus, "frost_potency");
            reapplyAttributeModifier(player, GEOMANCY_ATTRIBUTE, potencyBonus, "geomancy_potency");
            reapplyAttributeModifier(player, NECROMANCY_ATTRIBUTE, potencyBonus, "necromancy_potency");
            reapplyAttributeModifier(player, NETHER_ATTRIBUTE, potencyBonus, "nether_potency");
            reapplyAttributeModifier(player, STORM_ATTRIBUTE, potencyBonus, "storm_potency");
            reapplyAttributeModifier(player, VOID_ATTRIBUTE, potencyBonus, "void_potency");
            reapplyAttributeModifier(player, WILD_ATTRIBUTE, potencyBonus, "wild_potency");
        }
    }
    
    // 重新应用单个属性修改器
    private static void reapplyAttributeModifier(Player player, Attribute attribute, double bonus, String attributeName) {
        if (attribute != null) {
            UUID uuid = ATTRIBUTE_UUIDS.get(attributeName);
            if (uuid != null) {
                AttributeModifier modifier = new AttributeModifier(
                        uuid,
                        "Ruby Hard Candy Potency Bonus",
                        bonus,
                        AttributeModifier.Operation.ADDITION
                );

                if (player.getAttribute(attribute) != null) {
                    // 先移除再添加，防止重复
                    player.getAttribute(attribute).removeModifier(uuid);
                    player.getAttribute(attribute).addPermanentModifier(modifier);
                }
            }
        }
    }
}