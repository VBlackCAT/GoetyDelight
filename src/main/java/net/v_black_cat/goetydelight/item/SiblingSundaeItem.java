package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.ally.spider.AbstractSpiderServant;
import com.Polarice3.Goety.common.entities.ally.spider.CaveSpiderServant;
import com.Polarice3.Goety.utils.ServantUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
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
            TagKey<EntityType<?>> servantsTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("goety", "servants"));
            List<EntityType<?>> servantTypes = new ArrayList<>();

            BuiltInRegistries.ENTITY_TYPE.getTag(servantsTag).ifPresent(tag -> {
                for (Holder<EntityType<?>> holder : tag) {
                    EntityType<?> type = holder.value();

                        servantTypes.add(type);

                }
            });

            if (!servantTypes.isEmpty()) {
                Random random = new Random();
                for (int i = 0; i < 2; i++) {
                    EntityType<?> randomType = servantTypes.get(random.nextInt(servantTypes.size()));
                    createAndSetupServant((EntityType<? extends Entity>) randomType, level, entity);
                }
            }
        }
        return resultStack;
    }

    private void createAndSetupServant(EntityType<? extends Entity> servantType, Level level, LivingEntity owner) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = servantType.create(level);


            if (entity instanceof AbstractSpiderServant servant){


                servant.setPos(owner.getX(), owner.getY(), owner.getZ());
                servant.setTrueOwner(owner);
                servant.setLimitedLife(1200); // 60秒寿命（60 * 20=1200 ticks）

                // 调用finalizeSpawn
                servant.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(servant.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);

                // 处理生命值和强健效果
                processHealthAndEffects(servant);

                level.addFreshEntity(servant);

            }else {
                Summoned servant =(Summoned) entity;
                if (servant != null) {
                    // 设置仆从位置和主人
                    servant.setPos(owner.getX(), owner.getY(), owner.getZ());
                    servant.setTrueOwner(owner);
                    servant.setLimitedLife(1200); // 60秒寿命（60 * 20=1200 ticks）

                    // 调用finalizeSpawn
                    servant.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(servant.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, (SpawnGroupData) null, (CompoundTag) null);

                    // 处理生命值和强健效果
                    processHealthAndEffects(servant);

                    level.addFreshEntity(servant);
                }

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