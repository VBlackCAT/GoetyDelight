package net.v_black_cat.goetydelight.buff;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.SyncBuffPacket;

import java.util.Set;


@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public final class BuffSystem {

    public static final Capability<ActiveBuffs> ACTIVE_BUFFS_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    private static final ResourceLocation CAPABILITY_ID =
            new ResourceLocation(GoetyDelight.MODID, "active_buffs");



    public static boolean applyBuff(LivingEntity entity, ResourceLocation typeId, int duration, int amplifier) {
        if (entity.level().isClientSide) return false;

        ActiveBuffs buffs = getBuffs(entity);
        if (buffs == null) return false;

        // 先触发旧 Buff 的 onRemove
        if (buffs.hasBuff(typeId)) {
            int oldAmp = buffs.getTotalAmplifier(typeId);
            BuffEffect oldEffect = ModBuffTypes.getEffect(typeId);
            if (oldEffect != null) {
                oldEffect.onRemove(entity, oldAmp);
            }
        }

        buffs.addBuff(typeId, duration, amplifier);

        // 触发新 Buff 的 onApply
        BuffEffect newEffect = ModBuffTypes.getEffect(typeId);
        if (newEffect != null) {
            newEffect.onApply(entity, amplifier);
        }

        syncToClients(entity, typeId, true);
        return true;
    }

    public static boolean removeBuff(LivingEntity entity, ResourceLocation typeId) {
        if (entity.level().isClientSide) return false;

        ActiveBuffs buffs = getBuffs(entity);
        if (buffs == null || !buffs.hasBuff(typeId)) return false;

        int amplifier = buffs.getTotalAmplifier(typeId);
        buffs.removeBuff(typeId);

        BuffEffect effect = ModBuffTypes.getEffect(typeId);
        if (effect != null) {
            effect.onRemove(entity, amplifier);
        }

        syncToClients(entity, typeId, false);
        return true;
    }

    public static boolean hasBuff(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = getBuffs(entity);
        return buffs != null && buffs.hasBuff(typeId);
    }

    public static int getTotalAmplifier(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = getBuffs(entity);
        return buffs == null ? 0 : buffs.getTotalAmplifier(typeId);
    }

    public static int getBuffAmplifier(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = getBuffs(entity);
        if (buffs == null) return 0;
        var instances = buffs.getInstances(typeId);
        return instances.isEmpty() ? 0 : instances.get(instances.size() - 1).getAmplifier();
    }

    // ========== Hold 模式快速访问 ==========

    private static ActiveBuffs getBuffs(LivingEntity entity) {
        if (entity instanceof IBuffHolder holder) {
            return holder.goetydelight$getActiveBuffs();
        }
        return null;
    }

    // ========== 网络同步 ==========

    private static void syncToClients(LivingEntity entity, ResourceLocation typeId, boolean added) {
        if (entity.level().isClientSide) return;
        SyncBuffPacket packet = new SyncBuffPacket(entity.getId(), typeId, added);
        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                packet
        );
    }

    // ========== Tick 事件 ==========

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        ActiveBuffs buffs = getBuffs(entity);
        if (buffs == null || buffs.isEmpty()) return;

        // tick 并自动移除过期的 Buff（触发 onRemove）
        buffs.tickAllAndRemove(entity);

        // 对依然活跃的类型执行每 tick 效果（apply）
        for (ResourceLocation typeId : buffs.getActiveTypes()) {
            int totalAmplifier = buffs.getTotalAmplifier(typeId);
            BuffEffect effect = ModBuffTypes.getEffect(typeId);
            if (effect != null) {
                effect.apply(entity, totalAmplifier);
            }
        }
    }

    // ========== AttachCapabilities ==========

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            BuffCapabilityProvider provider = new BuffCapabilityProvider(living);
            event.addCapability(CAPABILITY_ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    // ========== PlayerClone ==========

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) return;
        ActiveBuffs oldBuffs = getBuffs(event.getOriginal());
        ActiveBuffs newBuffs = getBuffs(event.getEntity());
        if (oldBuffs != null && newBuffs != null) {
            newBuffs.deserializeNBT(oldBuffs.serializeNBT());
        }
    }

    // ========== MOD Bus ==========

    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.register(ActiveBuffs.class);
        }
    }
}
