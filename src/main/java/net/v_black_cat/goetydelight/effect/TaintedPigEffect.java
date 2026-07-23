package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.Summoned;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.v_black_cat.goetydelight.init.ModEffects;

public class TaintedPigEffect extends MobEffect {
    public TaintedPigEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }

    public static void createZombifiedPiglinMinion(Level level, LivingEntity owner) {
        EntityType<?> entityType = ModEntityType.ZPIGLIN_BRUTE_SERVANT.get();
        Summoned servant = (Summoned) entityType.create(level);
        if (servant != null) {
            servant.setPos(owner.getX(), owner.getY(), owner.getZ());
            servant.setTrueOwner(owner);
            servant.setLimitedLife(3600);
            if (owner.getRandom().nextFloat() < 0.01f) {
                servant.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
                servant.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
                servant.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
                servant.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
            }
            if (level instanceof ServerLevel serverLevel) {
                servant.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(servant.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            }
            level.addFreshEntity(servant);
        }
    }

    public static void onLivingHurt(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();

        if (entity.hasEffect(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT)) {
            if (entity.getRandom().nextFloat() < 0.5f) {
                Level level = entity.level();
                TaintedPigEffect.createZombifiedPiglinMinion(level, entity);

                MobEffectInstance effectInstance = entity.getEffect(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT);
                if (effectInstance != null) {
                    int newDuration = effectInstance.getDuration() - 1200;
                    if (newDuration <= 0) {
                        entity.removeEffect(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT);
                    } else {
                        entity.addEffect(new MobEffectInstance(
                                ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT,
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