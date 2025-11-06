package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class CakeItem extends Item {

    private static final double EFFECT_RADIUS = 32.0;

    public CakeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {

        ItemStack resultStack = super.finishUsingItem(stack, level, entity);



        
        if (!level.isClientSide && entity instanceof Player player) {
            
            AABB effectArea = new AABB(
                    player.position().subtract(EFFECT_RADIUS, EFFECT_RADIUS, EFFECT_RADIUS),
                    player.position().add(EFFECT_RADIUS, EFFECT_RADIUS, EFFECT_RADIUS)
            );

            
            List<Mob> nearbyEntities = level.getEntitiesOfClass(Mob.class, effectArea);
            int kills = 0;

            
            for (Mob target : nearbyEntities) {
                if (isTargetEntity(target)) {
                    float maxHealth = target.getHealth();

                    
                    player.heal(maxHealth);
                    
                    target.hurt(level.damageSources().magic(), Integer.MAX_VALUE);
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
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) {
            return false;
        }
        
        return entityId.equals(new ResourceLocation("minecraft:vex")) ||
                entityId.equals(new ResourceLocation("minecraft:allay")) ||
                entityId.equals(new ResourceLocation("goety:ally_irk")) ||
                entityId.equals(new ResourceLocation("goety:tormentor"))||
                entityId.equals(new ResourceLocation("goety:irk")) ||
                entityId.equals(new ResourceLocation("iceandfire:if_pixie")) ||
                entityId.equals(new ResourceLocation("alexsmobs:crimson_mosquito"));
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