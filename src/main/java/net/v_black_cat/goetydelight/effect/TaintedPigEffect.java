package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.data.models.model.TextureMapping.getItemTexture;

public class TaintedPigEffect extends MobEffect {

    public TaintedPigEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 这个效果主要通过事件处理器触发，这里不需要每tick执行操作
        super.applyEffectTick(entity, amplifier);
    }



    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 不需要每tick执行，返回false
        return false;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
        // 效果移除时的逻辑（如果有需要）
    }

    public static void createZombifiedPiglinMinion(Level level, LivingEntity owner) {

        EntityType<?> entityType = ModEntityType.ZPIGLIN_BRUTE_SERVANT.get();
        Summoned servant = (Summoned) entityType.create(level);

        if (servant != null) {
            servant.setPos(owner.getX(), owner.getY(), owner.getZ());
            servant.setTrueOwner(owner);
            servant.setLimitedLife(3600); // 3分钟寿命

            // 1%概率生成时自带合金套
            if (owner.getRandom().nextFloat() < 0.01f) {
                servant.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
                servant.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
                servant.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
                servant.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
            }

            // 调用finalizeSpawn
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                servant.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(servant.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            }

            level.addFreshEntity(servant);
        }
    }

    // 更新事件处理器
    @Mod.EventBusSubscriber(modid = "goetydelight")
    public static class TaintedPigEffectEventHandler {

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity entity = event.getEntity();

            // 处理SummonZombifiedPiglin效果的召唤功能
            if (entity.hasEffect(ModEffects.TAINTED_PIG.get())) {
                // 50%概率生成僵尸猪灵蛮兵仆从
                if (entity.getRandom().nextFloat() < 0.5f) {
                    Level level = entity.level();

                    // 创建僵尸猪灵蛮兵仆从
                    createZombifiedPiglinMinion(level, entity);

                    // 每召唤一只仆从，缩减效果持续时间1分钟（1200 ticks）
                    MobEffectInstance effectInstance = entity.getEffect(ModEffects.TAINTED_PIG.get());
                    if (effectInstance != null) {
                        int newDuration = effectInstance.getDuration() - 1200; // 减少1分钟
                        if (newDuration <= 0) {
                            entity.removeEffect(ModEffects.TAINTED_PIG.get());
                        } else {
                            entity.addEffect(new MobEffectInstance(
                                    ModEffects.TAINTED_PIG.get(),
                                    newDuration,
                                    effectInstance.getAmplifier(),
                                    effectInstance.isAmbient(),
                                    effectInstance.isVisible()
                            ));
                        }
                    }
                }
            }
        }
    }
}

