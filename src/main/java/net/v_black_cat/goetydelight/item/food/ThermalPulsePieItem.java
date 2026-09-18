package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.util.ConvertServantUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class ThermalPulsePieItem extends Item {

    private static final float DURATION_TICKS = 3 * 60 * 20.0f;
    private static final String THERMAL_TAG = "thermal_pulse_tag";

    // 暂存可能致死的攻击者：Apostle UUID -> 攻击者 UUID
    private static final Map<UUID, UUID> LETHAL_ATTACKERS = new ConcurrentHashMap<>();

    public ThermalPulsePieItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            ConvertServantUtil.applyTimedTag(entity, THERMAL_TAG, DURATION_TICKS);
        }
        return result;
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        ConvertServantUtil.tickTimedTag(player, THERMAL_TAG);
    }

    private static boolean isThermalActive(Entity attacker) {
        return attacker != null && attacker.getPersistentData().getFloat(THERMAL_TAG) > 0;
    }

    @SubscribeEvent
    public static void onAttackEvent(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity)) {return;}
        LivingEntity target = event.getEntity();
        if (!(target instanceof LivingEntity)) {return;}
        if (!(target.level() instanceof ServerLevel level)) return;
        if (!isThermalActive(target)) return;

        ResourceLocation targetId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (targetId == null || ConvertServantUtil.isBannedEntity(target)) return;
        if (target instanceof IOwned owned && owned.getTrueOwner() != null) return;

        convertToServant(target, attacker, level);
    }

    private static void convertToServant(LivingEntity target, Entity attacker, ServerLevel level) {
        String[] names = ConvertServantUtil.resolveServantKey(target);
        String entityName = names[0];
        String servantKey = names[1];

        LivingEntity servant = ConvertServantUtil.findServantEntity(
                level, entityName, servantKey, target.getMaxHealth()
        );
        if (servant == null) return;

        ConvertServantUtil.spawnServantFromTarget(
                target, servant, attacker, level,
                target.getHealth(), target.getMaxHealth()
        );
    }

    /**
     * 在伤害阶段记录可能致死的攻击者。
     * 由于 Apostle 覆写了死亡逻辑，LivingDeathEvent 中拿不到 killer，
     * 因此需要在此处提前记录。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Apostle apostle)) return;

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (attacker == null) return;

        float finalDamage = event.getAmount();
        if (apostle.getHealth() - finalDamage <= 0) {
            LETHAL_ATTACKERS.put(apostle.getUUID(), attacker.getUUID());
        }
    }

    @SubscribeEvent
    public static void onApostleKilledByServant(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Apostle apostle)) return;
        if (!(apostle.level() instanceof ServerLevel serverLevel)) return;

        UUID attackerUUID = LETHAL_ATTACKERS.remove(apostle.getUUID());
        if (attackerUUID == null) return;

        Entity attacker = serverLevel.getEntity(attackerUUID);
        if (!(attacker instanceof LivingEntity living)) return;
        if (!(living instanceof IOwned owned)) return;
        if (owned.getTrueOwner() == null) return;

        ItemStack drop = new ItemStack(ModItems.THERMAL_PULSE_PIE.get());
        ItemEntity itemEntity = new ItemEntity(
                serverLevel,
                apostle.getX(),
                apostle.getY() + 0.5,
                apostle.getZ(),
                drop
        );
        serverLevel.addFreshEntity(itemEntity);
    }
}