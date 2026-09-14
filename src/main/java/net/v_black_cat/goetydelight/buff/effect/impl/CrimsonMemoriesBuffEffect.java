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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.minecraft.world.entity.EntityType;
import java.util.HashSet;
import java.util.Set;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.compat.CompatManager;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class CrimsonMemoriesBuffEffect implements BuffEffect {

    // 【优化】原来每次攻击都 ResourceLocation.parse 四个 boss id：改成静态常量 + 懒解析成
    // EntityType，按引用比对，顺带省掉每此攻击一次 BuiltInRegistries 的 getKey 查询。
    private static final Set<ResourceLocation> NETHER_BOSS_IDS = Set.of(
            ResourceLocation.parse("goety:apostle"),
            ResourceLocation.parse("goety:heretic"),
            ResourceLocation.parse("goety:wither_necromancer"),
            ResourceLocation.parse("goety:maverick"));

    private static Set<EntityType<?>> netherBossTypes;

    private static Set<EntityType<?>> netherBossTypes() {
        Set<EntityType<?>> cached = netherBossTypes;
        if (cached == null) {
            Set<EntityType<?>> built = new HashSet<>();
            for (ResourceLocation id : NETHER_BOSS_IDS) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(built::add);
            }
            netherBossTypes = cached = Set.copyOf(built);
        }
        return cached;
    }

    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        // 维度判断（字段比较）先做，buff 查表放后面
        if (entity.level().dimension() == Level.NETHER
                && BuffUtil.hasBuff(entity, ModBuffTypes.CRIMSON_MEMORIES.getId())) {
            event.setCanceled(true);
        }
    }

    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player == null || !(event.getTarget() instanceof LivingEntity target)) return;
        if (player.level().isClientSide) return;
        if (!BuffUtil.hasBuff(player, ModBuffTypes.CRIMSON_MEMORIES.getId())) return;

        if (netherBossTypes().contains(target.getType())) {
            return;
        }

        Registry<DamageType> damageTypeRegistry = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        DamageSource damageSource = null;
        if (target.level().dimension() == Level.NETHER && CompatManager.isGoetyRevelationCompatEnabled()) {
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