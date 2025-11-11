package net.v_black_cat.goetydelight.ability;

import com.mega.revelationfix.common.apollyon.common.ExtraDamageTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import static com.Polarice3.Goety.utils.ModDamageSource.DOOM;

@Mod.EventBusSubscriber
public class CrimsonMemoriesHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();

        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.CRIMSON_MEMORIES) &&
                isInNether(entity)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player != null && event.getTarget() != null) {

            if (TimedAbilitySystem.hasAbility(player, AbilityRegistry.CRIMSON_MEMORIES)) {
                if (!player.level().isClientSide && event.getTarget() instanceof LivingEntity target) {

                    Registry<DamageType> damageTypeRegistry = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
                    DamageSource damageSource;
                    ResourceLocation targetId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
                    if (targetId != null){
                        if (targetId.equals(new ResourceLocation("goety:apostle")) ||
                                targetId.equals(new ResourceLocation("goety:heretic"))||
                                targetId.equals(new ResourceLocation("goety:wither_necromancer")) ||
                                targetId.equals(new ResourceLocation("goety:maverick"))
                                ){
                        }else{
                            if (isInNether(target)) {
                                if (ModList.get().isLoaded("goety_revelation")){
                                    damageSource = new DamageSource(damageTypeRegistry.getHolderOrThrow(ExtraDamageTypes.QUIETUS));
                                    target.hurt(damageSource,target.getMaxHealth()*10);
                                }else {
                                    damageSource = new DamageSource(damageTypeRegistry.getHolderOrThrow(DOOM));
                                    target.hurt(damageSource,target.getMaxHealth()*10);
                                }
                            } else {
                                damageSource = new DamageSource(damageTypeRegistry.getHolderOrThrow(DOOM));
                                target.hurt(damageSource,target.getMaxHealth()*10);
                            }
                        }

                    }


                }
            }
        }
    }



    private static boolean isInNether(LivingEntity entity) {
        return entity.level().dimension() == Level.NETHER;
    }


}