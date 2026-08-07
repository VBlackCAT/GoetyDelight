package net.v_black_cat.goetydelight.item.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.v_black_cat.goetydelight.init.ModConfig;
import org.jetbrains.annotations.NotNull;

public class CakeItem extends Item {
    public CakeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull
                    Level level, @NotNull LivingEntity entity) {

        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            double effectRadius = ModConfig.getCakeEffectRadius();

            AABB effectArea = new AABB(
            player.position().subtract(effectRadius, effectRadius, effectRadius),
            player.position().add(effectRadius, effectRadius, effectRadius)
            );

            List<Mob> nearbyEntities = level.getEntitiesOfClass(Mob.class, effectArea);
            int kills = 0;

            for (Mob target : nearbyEntities) {
                if (isTargetEntity(target)) {
                    float maxHealth = target.getHealth();

                    player.heal(maxHealth);

                    target.hurt(level.damageSources().genericKill(), Integer.MAX_VALUE);
                    kills++;
                    addDeathEffects(level, target);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PHANTOM_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return resultStack;
    }

    private boolean isTargetEntity(Mob entity) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) return false;

        return entityId.equals(ResourceLocation.parse("minecraft:vex")) ||
                entityId.equals(ResourceLocation.parse("minecraft:allay")) ||
                entityId.equals(ResourceLocation.parse("goety:ally_irk")) ||
                entityId.equals(ResourceLocation.parse("goety:irk_servant")) ||
                entityId.equals(ResourceLocation.parse("goety:ally_vex")) ||
                entityId.equals(ResourceLocation.parse("goety:vex_servant")) ||
                entityId.equals(ResourceLocation.parse("goety:tormentor")) ||
                entityId.equals(ResourceLocation.parse("goety:irk")) ||
                entityId.equals(ResourceLocation.parse("iceandfire:if_pixie")) ||
                entityId.equals(ResourceLocation.parse("alexsmobs:crimson_mosquito")) ||
                entityId.equals(ResourceLocation.parse("alexsmobs:skreecher")) ||
                entityId.equals(ResourceLocation.parse("alexsmobs:murmur_head")) ||
                entityId.equals(ResourceLocation.parse("alexsmobs:centipede_head")) ||
                entityId.equals(ResourceLocation.parse("spectrum:eraser")) ||
                entityId.equals(ResourceLocation.parse("pastel:eraser")) ||
                entityId.equals(ResourceLocation.parse("alexsmobs:seagull"));
                entityId.equals(ResourceLocation.parse("alexsmobs:straddler")) ||
    }

    private void addDeathEffects(Level level, Mob target) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    15, 0.5, 0.5, 0.5, 0.05);

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.8F, 1.0F);
        }
    }
}
