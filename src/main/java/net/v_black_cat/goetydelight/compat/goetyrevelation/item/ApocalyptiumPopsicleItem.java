package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApocalyptiumPopsicleItem extends SwordItem {

    public static final String PERSISTENT_TAG = "goetydelight:apocalyptium_blessing";
    private static final Set<UUID> CACHED_PLAYERS = ConcurrentHashMap.newKeySet();

    public ApocalyptiumPopsicleItem(Tiers tiers, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tiers, attackDamageModifier, attackSpeedModifier, properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            CompoundTag forgeData = persistentData.getCompound(Player.PERSISTED_NBT_TAG);
            forgeData.putBoolean(PERSISTENT_TAG, true);
            persistentData.put(Player.PERSISTED_NBT_TAG, forgeData);

            CACHED_PLAYERS.add(player.getUUID());
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (!CACHED_PLAYERS.contains(player.getUUID())) {
            return;
        }

        DamageSource source = event.getSource();
        Holder<DamageType> damageTypeHolder = source.typeHolder();
        if (damageTypeHolder instanceof Holder.Reference<DamageType> reference) {
            Set<net.minecraft.tags.TagKey<DamageType>> tags =
                    new HashSet<>(reference.tags().toList());

            tags.add(DamageTypeTags.BYPASSES_INVULNERABILITY);
            tags.add(DamageTypeTags.BYPASSES_COOLDOWN);

            reference.bindTags(tags);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (hasPersistentTag(player)) {
            CACHED_PLAYERS.add(player.getUUID());
        } else {
            CACHED_PLAYERS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CACHED_PLAYERS.remove(event.getEntity().getUUID());
    }

    private static boolean hasPersistentTag(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(Player.PERSISTED_NBT_TAG)) {
            return false;
        }
        return persistentData.getCompound(Player.PERSISTED_NBT_TAG).getBoolean(PERSISTENT_TAG);
    }
}