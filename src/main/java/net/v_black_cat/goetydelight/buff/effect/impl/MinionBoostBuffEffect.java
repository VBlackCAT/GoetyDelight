package net.v_black_cat.goetydelight.buff.effect.impl;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.ability.MinionBoost;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MinionBoostBuffEffect implements BuffEffect {

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide) {
            MinionBoost.applyMinionBoosts(player);
        }
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {}

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity) {
            if (entity instanceof IOwned ownedEntity) {
                LivingEntity owner = ownedEntity.getTrueOwner();
                if (owner instanceof Player player) {
                    int totalAmplifier = BuffUtil.getTotalAmplifier(player, ModBuffTypes.MINION_BOOST.getId());
                    if (totalAmplifier > 0) {
                        MinionBoost.applyMinionBoosts(player);
                    }
                }
            }
        }
    }
}