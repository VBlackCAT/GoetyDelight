package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import vectorwing.farmersdelight.common.item.DrinkableItem;

import java.util.UUID;

public class NightHeartPeaSoupItem extends DrinkableItem implements IWand {
    // 最大加成次数
    private static final int MAX_BOOST_COUNT = 5;
    // 每次加成的百分比
    private static final double BOOST_PERCENTAGE = 0.1;
    // 玩家NBT标签键
    private static final String SOUP_BOOST_COUNT_TAG = "NightPeaSoupBoostCount";
    // 仆从NBT标签键
    private static final String MINION_BOOST_APPLIED_TAG = "NightPeaSoupBoostApplied";

    // 属性修改器UUIDs
    private static final UUID ATTACK_DAMAGE_BOOST_UUID = UUID.fromString("d03da095-ff85-4c52-a404-51566bdff53e");
    private static final UUID MAX_HEALTH_BOOST_UUID = UUID.fromString("12f30d3e-d988-4b93-af83-8f25dc50f14c");
    private static final UUID ARMOR_BOOST_UUID = UUID.fromString("0cb7577e-778e-4872-9c54-5345668e468f");
    private static final UUID MOVEMENT_SPEED_BOOST_UUID = UUID.fromString("c5c7d0c5-d5c7-4c5d-b5c5-d5c5d5c5d5c6");
    private static final UUID ARMOR_TOUGHNESS_BOOST_UUID = UUID.fromString("c5c7d0c5-d5c7-4c5d-b5c5-d5c5d5c5d5c7");

    public NightHeartPeaSoupItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 设置时间为黑夜
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {


                serverLevel.setDayTime(18000 + 24000 * (serverLevel.getDayTime() / 24000 + 1));

                // 播放音效和粒子效果
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        net.minecraft.sounds.SoundEvents.AMBIENT_CAVE.get(),
                        net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 0.8F);

                for (int i = 0; i < 20; i++) {
                    double x = entity.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                    double y = entity.getY() + level.random.nextDouble() * 2.0;
                    double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 2.0;

                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                            x, y, z, 1, 0, 0, 0, 0.1);
                }
            }

            // 添加EndWalk效果（SHADOW_WALK）
            if (entity instanceof Player player) {
                SpellStat spellStat = new SpellStat(0,0,16,2.0,0,0);
                for(int i = 0; i < 16; ++i) {
                    double d0 = MathHelper.rgbToSpeed(96.0);
                    double d1 = MathHelper.rgbToSpeed(62.0);
                    double d2 = MathHelper.rgbToSpeed(92.0);

                    serverLevel.sendParticles((SimpleParticleType)ModParticleTypes.CULT_SPELL.get(),
                            player.getRandomX(1.0), player.getRandomY(), player.getRandomZ(1.0),
                            0, d0, d1, d2, 0.5);
                }

                SEHelper.setEndWalk(player, player.blockPosition(), player.level().dimension());
                ModNetwork.sendTo(player, new SPlayPlayerSoundPacket((SoundEvent)ModSounds.END_WALK.get(), 1.0F, 1.0F));

                // 增加玩家的豌豆汤加成计数
                increaseSoupBoostCount(player);

                // 给所有现有仆从应用加成
                applyMinionBoosts(player, getSoupBoostCount(player));
            }
        }

        // 处理玻璃瓶的返还逻辑
        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return result; // 创造模式不消耗物品
            }

            // 尝试将玻璃瓶添加到玩家物品栏
            if (result.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE); // 如果原物品已消耗完，直接返还碗
            } else if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false); // 背包满时掉落碗
            }
        }

        return result;
    }

    @Override
    public SpellType getSpellType() {
        return null;
    }

    // 获取玩家的豌豆汤加成计数
    public static int getSoupBoostCount(Player player) {
        return player.getPersistentData().getInt(SOUP_BOOST_COUNT_TAG);
    }

    // 增加玩家的豌豆汤加成计数（最多5次）
    private void increaseSoupBoostCount(Player player) {
        int currentCount = getSoupBoostCount(player);
        if (currentCount < MAX_BOOST_COUNT) {
            player.getPersistentData().putInt(SOUP_BOOST_COUNT_TAG, currentCount + 1);
        }
    }

    // 给所有仆从应用加成
    private void applyMinionBoosts(Player player, int boostCount) {
        if (boostCount <= 0) return;

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(64.0D))) {
            if (isPlayerMinion(entity, player)) {
                applyMinionBoost(entity, boostCount);
            }
        }
    }

    // 检查实体是否是玩家的仆从
    private boolean isPlayerMinion(LivingEntity entity, Player player) {
        if (entity instanceof IOwned ownedEntity) {
            LivingEntity owner = ownedEntity.getTrueOwner();
            return owner == player;
        }
        return false;
    }

    public static void applyMinionBoost(LivingEntity minion, int boostCount) {
        if (minion.level().isClientSide) return;

        CompoundTag persistentData = minion.getPersistentData();
        
        // 记录已应用的加成等级
        persistentData.putInt(MINION_BOOST_APPLIED_TAG, boostCount);

        // 计算加成值
        double boostMultiplier = BOOST_PERCENTAGE * boostCount;

        // 应用攻击力加成
        AttributeInstance attackDamage = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            AttributeModifier modifier = attackDamage.getModifier(ATTACK_DAMAGE_BOOST_UUID);
            if (modifier != null) {
                attackDamage.removeModifier(ATTACK_DAMAGE_BOOST_UUID);
            }
            
            attackDamage.addPermanentModifier(new AttributeModifier(
                    ATTACK_DAMAGE_BOOST_UUID,
                    "Night Pea Soup Attack Boost",
                    boostMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用生命上限加成
        AttributeInstance maxHealth = minion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            AttributeModifier modifier = maxHealth.getModifier(MAX_HEALTH_BOOST_UUID);
            if (modifier != null) {
                maxHealth.removeModifier(MAX_HEALTH_BOOST_UUID);
            }
            
            maxHealth.addPermanentModifier(new AttributeModifier(
                    MAX_HEALTH_BOOST_UUID,
                    "Night Pea Soup Health Boost",
                    boostMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));

            // 同时增加当前生命值以匹配上限增加
            minion.setHealth(minion.getHealth() * (1.0f + (float)boostMultiplier));
        }

        // 应用护甲加成
        AttributeInstance armor = minion.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            AttributeModifier modifier = armor.getModifier(ARMOR_BOOST_UUID);
            if (modifier != null) {
                armor.removeModifier(ARMOR_BOOST_UUID);
            }
            
            armor.addPermanentModifier(new AttributeModifier(
                    ARMOR_BOOST_UUID,
                    "Night Pea Soup Armor Boost",
                    boostMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用护甲韧性加成
        AttributeInstance armorToughness = minion.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            AttributeModifier modifier = armorToughness.getModifier(ARMOR_TOUGHNESS_BOOST_UUID);
            if (modifier != null) {
                armorToughness.removeModifier(ARMOR_TOUGHNESS_BOOST_UUID);
            }
            
            armorToughness.addPermanentModifier(new AttributeModifier(
                    ARMOR_TOUGHNESS_BOOST_UUID,
                    "Night Pea Soup Armor Toughness Boost",
                    boostMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用移动速度加成
        AttributeInstance movementSpeed = minion.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            AttributeModifier modifier = movementSpeed.getModifier(MOVEMENT_SPEED_BOOST_UUID);
            if (modifier != null) {
                movementSpeed.removeModifier(MOVEMENT_SPEED_BOOST_UUID);
            }
            
            movementSpeed.addPermanentModifier(new AttributeModifier(
                    MOVEMENT_SPEED_BOOST_UUID,
                    "Night Pea Soup Movement Speed Boost",
                    boostMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    // 移除仆从的加成
    public static void removeMinionBoost(LivingEntity minion) {
        AttributeInstance attackDamage = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_BOOST_UUID);
        }

        AttributeInstance maxHealth = minion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(MAX_HEALTH_BOOST_UUID);
        }

        AttributeInstance armor = minion.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_BOOST_UUID);
        }

        AttributeInstance armorToughness = minion.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            armorToughness.removeModifier(ARMOR_TOUGHNESS_BOOST_UUID);
        }

        AttributeInstance movementSpeed = minion.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_BOOST_UUID);
        }
    }

    // 事件处理器
    @Mod.EventBusSubscriber()
    public static class BoostHandler {
        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity) {
                // 检查是否是仆从
                if (entity instanceof IOwned ownedEntity) {
                    LivingEntity owner = ownedEntity.getTrueOwner();

                    // 检查主人是否是玩家且拥有豌豆汤加成
                    if (owner instanceof Player player) {
                        int boostCount = getSoupBoostCount(player);
                        if (boostCount > 0) {
                            applyMinionBoost(entity, boostCount);
                        }
                    }
                }
            }
        }
    }
}