package net.v_black_cat.goetydelight.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import net.v_black_cat.goetydelight.util.EntityTagChecker;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class OminousIceCreamItem extends Item {

    public static final String OMINOUS_ACTIVE_TAG = "OminousIceCreamActive";
    public static final String HAS_CONSUMED_TAG = "HasConsumedOminousIceCream";

    public OminousIceCreamItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            if (player.hasEffect(MobEffects.BAD_OMEN)) {
                player.getPersistentData().putBoolean(OMINOUS_ACTIVE_TAG, true);
                player.getPersistentData().putBoolean(HAS_CONSUMED_TAG, true);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.EVOKER_PREPARE_SUMMON,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return resultStack;
            }

            if (resultStack.isEmpty()) {
                return new ItemStack(Items.BOWL);
            } else if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                player.drop(new ItemStack(Items.BOWL), false);
            }
        }

        return resultStack;
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        LivingEntity target = event.getNewTarget();

        
        if (!(attacker instanceof Mob) || !(target instanceof Player)) {
            return;
        }

        Mob mob = (Mob) attacker;
        Player player = (Player) target;

        
        if (!EntityTagChecker.isEntityInTag(mob, "minecraft:raiders") ||
                mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return;
        }

        
        if (player.getPersistentData().getBoolean(OMINOUS_ACTIVE_TAG)) {
            
            if (mob.getLastHurtByMob() != player) {
                if (event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
                    event.setNewTarget(null);
                } else {
                    event.setCanceled(true);
                }
            }
            
        }
    }

    
    @SubscribeEvent
    public static void onLivingAttack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                    (LivingEntity) event.getSource().getEntity() : null;

            if (attacker instanceof Mob mob) {
                
                if (EntityTagChecker.isEntityInTag(mob, "minecraft:raiders") &&
                        !mob.getType().is(Tags.EntityTypes.BOSSES)) {

                    
                    if (player.getPersistentData().getBoolean(OMINOUS_ACTIVE_TAG)) {
                        
                        if (mob.getLastHurtByMob() != player) {
                            event.setCanceled(true);

                            
                            double d0 = mob.getX() - player.getX();
                            double d1 = mob.getZ() - player.getZ();
                            mob.getNavigation().stop();
                            mob.setDeltaMovement(mob.getDeltaMovement().add(
                                    Math.signum(d0) * 0.1, 0, Math.signum(d1) * 0.1
                            ));

                            
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,
                                    net.minecraft.sounds.SoundSource.HOSTILE, 0.5F, 1.5F);
                        }
                        
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();
        if (effect == null) return;

        if (entity instanceof Player player && effect.getEffect() == MobEffects.BAD_OMEN) {
            if (player.getPersistentData().getBoolean(OMINOUS_ACTIVE_TAG)) {
                player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
                
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpire(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();
        if (effect == null) return;

        if (entity instanceof Player player && effect.getEffect() == MobEffects.BAD_OMEN) {
            if (player.getPersistentData().getBoolean(OMINOUS_ACTIVE_TAG)) {
                player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
                
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player) {
            player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
            player.getPersistentData().remove(HAS_CONSUMED_TAG);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
        player.getPersistentData().remove(HAS_CONSUMED_TAG);
    }
}