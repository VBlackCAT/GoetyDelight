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
    private static final String REMAINING_USES_TAG = "remaining_uses";
    private static final int MAX_USES = 8;

    public CakeItem(Properties properties) {
        super(properties.stacksTo(1)); 
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; 
    }



    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);
        
        if (entity instanceof Player player) {
            player.getFoodData().eat(this, stack);
        } else {
            entity.eat(level, stack);
        }

        
        if (!level.isClientSide && entity instanceof Player player) {
            
            CompoundTag nbt = stack.getOrCreateTag();
            int remainingUses = nbt.getInt(REMAINING_USES_TAG);

            
            if (remainingUses == 0) {
                remainingUses = MAX_USES;
            }

            
            remainingUses--;
            nbt.putInt(REMAINING_USES_TAG, remainingUses);

            
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
            if (kills > 0) {
                //player.displayClientMessage(Component.literal("超度了 " + kills + " 个灵魂！"), true);
            }

            
            if (remainingUses <= 0) {
                stack.shrink(1); 
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY; 
                }
            }
        }

        return stack;
    }


    @Override
    public boolean isBarVisible(ItemStack stack) {
        
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(REMAINING_USES_TAG)) {
            return nbt.getInt(REMAINING_USES_TAG) < MAX_USES;
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        
        CompoundTag nbt = stack.getOrCreateTag();
        int remainingUses = nbt.getInt(REMAINING_USES_TAG);
        if (remainingUses == 0) {
            remainingUses = MAX_USES;
        }
        return Math.round(13.0F * remainingUses / MAX_USES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        
        CompoundTag nbt = stack.getOrCreateTag();
        int remainingUses = nbt.getInt(REMAINING_USES_TAG);
        if (remainingUses == 0) {
            remainingUses = MAX_USES;
        }

        float ratio = (float) remainingUses / MAX_USES;
        
        int r = (int) (255 * (1.0F - ratio));
        int g = (int) (255 * ratio);
        int b = 0;

        return (r << 16) | (g << 8) | b;
    }


    private boolean isTargetEntity(Mob entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) {
            return false;
        }
        // 直接检查实体类型ID是否在目标列表中
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