package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ally.spider.AbstractSpiderServant;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SiblingSundaeItem extends Item {
    public SiblingSundaeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {
            // 定义6个tag分别对应6个等级卡池
            TagKey<EntityType<?>>[] servantTags = new TagKey[]{
                    TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("goetydelight", "servants_pool/servants_pool_1")), // 1级
                    TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("goetydelight", "servants_pool/servants_pool_2")), // 2级
                    TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("goetydelight", "servants_pool/servants_pool_3")), // 3级
                    TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("goetydelight", "servants_pool/servants_pool_4")), // 4级
                    TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("goetydelight", "servants_pool/servants_pool_5")), // 5级
                    TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("goetydelight", "servants_pool/servants_pool_6"))  // 6级
            };

            // 各等级卡池权重
            double[] weights = new double[]{50, 35, 12.5, 2.0, 0.45,0.05};
            int[] summonCounts = new int[]{5, 4, 2, 1, 1};
            int[] lifeTimes = new int[]{2400, 6000, 12000, 36000, 72000, 144000}; // tick: 2,5,10,30,60,120分钟
            double[] immortalBaseChance = new double[]{0.5, 0.5, 0.5, 0.5, 0.2, 0.1}; // 5级仅20%,6级仅10%

            // 幸运影响
            int luck = 0;
            if (entity instanceof Player player) {
                luck = (int)player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.LUCK).getValue();
            }

            // roll等级：每3点幸运多1次roll，最多9次，取10次最高
            int rollTimes = 1 + Math.min(luck / 3, 9);
            int bestLevel = 0;
            Random random = new Random();
            for (int i = 0; i < rollTimes; i++) {
                int level1 = rollLevel(weights, random);
                if (level1 > bestLevel) bestLevel = level1;
            }

            // roll仆从
            List<EntityType<?>> servantTypes = new ArrayList<>();
            BuiltInRegistries.ENTITY_TYPE.getTag(servantTags[bestLevel]).ifPresent(tag -> {
                for (Holder<EntityType<?>> holder : tag) {
                    servantTypes.add(holder.value());
                }
            });

            if (!servantTypes.isEmpty()) {
                // 召唤数量
                int count = summonCounts[bestLevel];
                // 永生概率
                double immortalChance = immortalBaseChance[bestLevel] + Math.min(luck, 50) * 0.01;
                if (bestLevel == 4) immortalChance = Math.min(immortalChance, 0.2); // 5级上限20%
                else immortalChance = Math.min(immortalChance, 1.0);

                for (int i = 0; i < count; i++) {
                    EntityType<?> randomType = servantTypes.get(random.nextInt(servantTypes.size()));
                    boolean immortal = random.nextDouble() < immortalChance;
                    int life = immortal ? -1 : lifeTimes[bestLevel];
                    createAndSetupServant((EntityType<? extends Entity>) randomType, level, entity, life);
                }
            }
        }
        return resultStack;
    }

    // 权重抽卡池等级
    private int rollLevel(double[] weights, Random random) {
        double total = 0;
        for (double w : weights) total += w;
        double r = random.nextDouble() * total;
        double acc = 0;
        for (int i = 0; i < weights.length; i++) {
            acc += weights[i];
            if (r < acc) return i;
        }
        return weights.length - 1;
    }

    private void createAndSetupServant(EntityType<? extends Entity> servantType, Level level, LivingEntity owner, int lifeTicks) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = servantType.create(level);

            if (entity instanceof AbstractSpiderServant servant){
                servant.setPos(owner.getX(), owner.getY(), owner.getZ());
                servant.setTrueOwner(owner);
                if (lifeTicks > 0) servant.setLimitedLife(lifeTicks);
                // finalizeSpawn
                servant.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(servant.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
                processHealthAndEffects(servant);
                level.addFreshEntity(servant);
            } else if (entity instanceof Summoned servant) {
                servant.setPos(owner.getX(), owner.getY(), owner.getZ());
                servant.setTrueOwner(owner);
                if (lifeTicks > 0) servant.setLimitedLife(lifeTicks);
                // finalizeSpawn
                servant.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(servant.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, (SpawnGroupData) null, (CompoundTag) null);
                processHealthAndEffects(servant);
                level.addFreshEntity(servant);
            }
        }
    }

    private void processHealthAndEffects(Mob servant) {
        float maxHealth = servant.getMaxHealth();
        if (maxHealth < 60.0f) {
            int healthDeficit = (int) (60.0f - maxHealth);
            int healthBoostLevel = healthDeficit / 3;

            if (healthBoostLevel > 0) {
                servant.addEffect(new MobEffectInstance(
                        GoetyEffects.BUFF.get(),
                        1200,
                        healthBoostLevel - 1,
                        false,
                        true
                ));
            }
        }
    }
}