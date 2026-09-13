package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import com.mega.revelationfix.common.init.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuietusMarrowItem extends Item {

    private static final String NBT_KEY = "GoetyDelight:QuietusProtected";
    private static final String MARROW_TAG = "GoetyDelight:QuietusMarrowActive";
    private static final String MARROW_DAMAGE_WINDOW = "GoetyDelight:QuietusMarrowDamageWindow";
    private static final String MARROW_NO_HEAL_UNTIL = "GoetyDelight:QuietusMarrowNoHealUntil";

    private static final int DAMAGE_INTERVAL = 10;
    private static final float STARVATION_DAMAGE = 2.0F;
    private static final int JUDGE_WINDOW = 15;
    private static final float JUDGE_THRESHOLD = 4.0F;
    private static final int NO_HEAL_DURATION = 20 * 60 * 5;

    private static boolean allowRemoval = false;

    public QuietusMarrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            applyQuietusMarrowEffects(entity);
        }
        return result;
    }

    @SubscribeEvent
    public static void onInteractLivingEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof QuietusMarrowItem)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;

        Player player = event.getEntity();
        if (target instanceof Player) return;

        applyQuietusMarrowEffects(target);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        event.setCanceled(true);
    }

    private static void applyQuietusMarrowEffects(LivingEntity entity) {
        MobEffect quietusEffect = ModEffects.QUIETUS.get();
        MobEffect fastingEffect = net.v_black_cat.goetydelight.effect.ModEffects.FASTING.get();

        MobEffectInstance quietus = new MobEffectInstance(quietusEffect, -1, 1, false, false, true);
        MobEffectInstance fasting = new MobEffectInstance(fastingEffect, -1, 0, false, false, true);

        Map<MobEffect, MobEffectInstance> activeEffects = entity.getActiveEffectsMap();
        activeEffects.put(quietusEffect, quietus);
        activeEffects.put(fastingEffect, fasting);

        syncEffect(entity, quietus);
        syncEffect(entity, fasting);

        addProtected(entity, quietusEffect);
        addProtected(entity, fastingEffect);
        CompoundTag data = entity.getPersistentData();
        data.putBoolean(MARROW_TAG, true);
        data.putInt(MARROW_DAMAGE_WINDOW, 0);
        data.putInt(MARROW_NO_HEAL_UNTIL, 0);
    }

    private static void syncEffect(LivingEntity entity, MobEffectInstance instance) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcastAndSend(
                    entity,
                    new ClientboundUpdateMobEffectPacket(entity.getId(), instance)
            );
        }
    }

    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(MARROW_TAG)) return;

        if (data.getInt(MARROW_NO_HEAL_UNTIL) > 0) {
            data.putInt(MARROW_NO_HEAL_UNTIL, data.getInt(MARROW_NO_HEAL_UNTIL) - 1);
            if (data.getInt(MARROW_NO_HEAL_UNTIL) <= 0) {
                data.remove(MARROW_NO_HEAL_UNTIL);
                data.remove(MARROW_TAG);
                data.remove(MARROW_DAMAGE_WINDOW);
            }
            return;
        }

        int window = data.getInt(MARROW_DAMAGE_WINDOW) + 1;
        data.putInt(MARROW_DAMAGE_WINDOW, window);

        if (window % DAMAGE_INTERVAL == 0) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                entity.hurt(serverLevel.damageSources().starve(), STARVATION_DAMAGE);
            }
        }

        if (window >= JUDGE_WINDOW) {
            int dealt = countDamageInWindow(entity);
            if (dealt < JUDGE_THRESHOLD) {
                data.putInt(MARROW_NO_HEAL_UNTIL, NO_HEAL_DURATION);
                data.putInt(MARROW_DAMAGE_WINDOW, 0);
            } else {
                data.putInt(MARROW_DAMAGE_WINDOW, 0);
            }
        }

        if (entity.isDeadOrDying()) {
            clearMarrow(entity);
        }
    }

    private static int countDamageInWindow(LivingEntity entity) {
        int window = entity.getPersistentData().getInt(MARROW_DAMAGE_WINDOW);
        int hits = window / DAMAGE_INTERVAL;
        return hits * (int) STARVATION_DAMAGE;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag data = entity.getPersistentData();

        if (data.getInt(MARROW_NO_HEAL_UNTIL) > 0) {
            event.setCanceled(true);
            return;
        }

        if (data.getBoolean(MARROW_TAG)) {
            event.setAmount(event.getAmount() * 0.1F);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        removeQuietusMarrowEffects(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        CompoundTag oldData = original.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        if (oldData.getBoolean(MARROW_TAG)) {
            newData.putBoolean(MARROW_TAG, true);
            newData.putInt(MARROW_DAMAGE_WINDOW, oldData.getInt(MARROW_DAMAGE_WINDOW));
            newData.putInt(MARROW_NO_HEAL_UNTIL, oldData.getInt(MARROW_NO_HEAL_UNTIL));
            if (oldData.contains(NBT_KEY, Tag.TAG_LIST)) {
                newData.put(NBT_KEY, oldData.getList(NBT_KEY, Tag.TAG_STRING).copy());
            }

            MobEffect quietusEffect = ModEffects.QUIETUS.get();
            MobEffect fastingEffect = net.v_black_cat.goetydelight.effect.ModEffects.FASTING.get();

            MobEffectInstance quietus = new MobEffectInstance(quietusEffect, -1, 1, false, false, true);
            MobEffectInstance fasting = new MobEffectInstance(fastingEffect, -1, 0, false, false, true);

            newPlayer.getActiveEffectsMap().put(quietusEffect, quietus);
            newPlayer.getActiveEffectsMap().put(fastingEffect, fasting);

            syncEffect(newPlayer, quietus);
            syncEffect(newPlayer, fasting);
        }
    }

    private static void clearMarrow(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.remove(MARROW_TAG);
        data.remove(MARROW_DAMAGE_WINDOW);
        data.remove(MARROW_NO_HEAL_UNTIL);
    }

    private static Set<MobEffect> getProtected(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(NBT_KEY, Tag.TAG_LIST)) {
            return new HashSet<>();
        }
        ListTag list = data.getList(NBT_KEY, Tag.TAG_STRING);
        Set<MobEffect> set = new HashSet<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id == null) continue;
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
            if (effect != null) set.add(effect);
        }
        return set;
    }

    private static void setProtected(LivingEntity entity, Set<MobEffect> effects) {
        CompoundTag data = entity.getPersistentData();
        if (effects.isEmpty()) {
            data.remove(NBT_KEY);
            return;
        }
        ListTag list = new ListTag();
        for (MobEffect effect : effects) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (id != null) list.add(StringTag.valueOf(id.toString()));
        }
        data.put(NBT_KEY, list);
    }

    private static void addProtected(LivingEntity entity, MobEffect effect) {
        Set<MobEffect> set = getProtected(entity);
        if (set.add(effect)) {
            setProtected(entity, set);
        }
    }

    private static void removeProtected(LivingEntity entity, MobEffect effect) {
        Set<MobEffect> set = getProtected(entity);
        if (set.remove(effect)) {
            setProtected(entity, set);
        }
    }

    private static boolean isProtected(LivingEntity entity, MobEffect effect) {
        return getProtected(entity).contains(effect);
    }

    private static void clearProtected(LivingEntity entity) {
        entity.getPersistentData().remove(NBT_KEY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEffectRemoveHighest(MobEffectEvent.Remove event) {
        if (allowRemoval) return;

        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;

        if (isProtected(entity, instance.getEffect())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onEffectRemoveLowest(MobEffectEvent.Remove event) {
        if (allowRemoval) return;

        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;

        if (isProtected(entity, instance.getEffect())) {
            event.setCanceled(true);
        }
    }

    public static void removeQuietusMarrowEffects(LivingEntity entity) {
        allowRemoval = true;
        try {
            entity.removeEffect(ModEffects.QUIETUS.get());
            entity.removeEffect(net.v_black_cat.goetydelight.effect.ModEffects.FASTING.get());
            clearProtected(entity);
            clearMarrow(entity);
        } finally {
            allowRemoval = false;
        }
    }

    public static void removeFastingOnly(LivingEntity entity) {
        allowRemoval = true;
        try {
            MobEffect fastingEffect = net.v_black_cat.goetydelight.effect.ModEffects.FASTING.get();
            entity.removeEffect(fastingEffect);
            removeProtected(entity, fastingEffect);
        } finally {
            allowRemoval = false;
        }
    }
}