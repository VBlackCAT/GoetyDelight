package net.v_black_cat.goetydelight.buff.effect.impl;

import com.Polarice3.Goety.utils.ModDamageSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class CrimsonMemoriesBuffEffect implements BuffEffect {
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (BuffUtil.hasBuff(entity, ModBuffTypes.CRIMSON_MEMORIES.getId()) &&
                entity.level().dimension() == Level.NETHER) {
            event.setCanceled(true);
        }
    }

    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player == null || !(event.getTarget() instanceof LivingEntity target)) return;
        if (!BuffUtil.hasBuff(player, ModBuffTypes.CRIMSON_MEMORIES.getId())) return;

        if (player.level().isClientSide) return;


        ResourceLocation targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (targetId != null && (targetId.equals(ResourceLocation.parse("goety:apostle")) ||
                targetId.equals(ResourceLocation.parse("goety:heretic")) ||
                targetId.equals(ResourceLocation.parse("goety:wither_necromancer")) ||
                targetId.equals(ResourceLocation.parse("goety:maverick")))) {
            return;
        }

        Registry<DamageType> damageTypeRegistry = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        DamageSource damageSource = null;
        if (target.level().dimension() == Level.NETHER && ModList.get().isLoaded("goety_revelation")) {
//            damageSource = new DamageSource(damageTypeRegistry.getHolderOrThrow(ExtraDamageTypes.QUIETUS));
        } else {
            damageSource = new DamageSource(damageTypeRegistry.getHolderOrThrow(ModDamageSource.DOOM));
        }
        if (damageSource!=null){
            target.hurt(damageSource, target.getMaxHealth() * 10);
        }

    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {}
    @Override
    public void onApply(LivingEntity entity, int amplifier) {}
    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}