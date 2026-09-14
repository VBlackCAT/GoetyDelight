package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import com.mega.revelationfix.Revelationfix;
import com.mega.revelationfix.common.entity.boss.ApostleServant;
import com.mega.revelationfix.common.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.compat.goety_revelation.ApocalyptiumData;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApocalyptiumCodItem extends Item {

    private static final String IS_APOLLYON_TAG = "isApollyon";
    private static final String PREVENT_DROPS_TAG = "PreventDrops";
    private static final String HALO_OF_THE_END_ID = "goety_revelation:halo_of_the_end";

    private static final int SERVANT_LIFETIME = 30 * 60 * 20;
    private static final int APOLLYON_DURATION = 5 * 60 * 20;

    private static final Set<WeakReference<LivingEntity>> TRACKED_ENTITIES = ConcurrentHashMap.newKeySet();

    private static boolean isTrackingActive = false;

    public ApocalyptiumCodItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            summonApostleServant(player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        if (!level.isClientSide && target != null) {
            ResourceLocation targetType = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

            if (targetType != null) {
                String entityId = targetType.toString();

                if (entityId.equals("revelationfix:apostle_servant")) {
                    // 仆从可以直接转化
                    convertToApollyon(target);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                } else if (entityId.equals("goety:apostle")) {
                    // 使徒需要特殊条件才能转化
                    if (canConvertApostle(player)) {
                        convertToApollyon(target);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    } else {
                        return InteractionResult.FAIL;
                    }
                }
            }
        }

        return super.interactLivingEntity(stack, player, target, hand);
    }

    private boolean canConvertApostle(Player player) {
        // 创造模式直接允许
        if (player.getAbilities().instabuild) {
            return true;
        }

        // 检查curios饰品栏
        return hasHaloOfTheEndInSpecificSlots(player);
    }

    private boolean hasHaloOfTheEndInSpecificSlots(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> {
                    Set<String> targetSlots = Set.of("head", "curio");

                    for (Map.Entry<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                        if (targetSlots.contains(entry.getKey())) {
                            top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler stacksHandler = entry.getValue();

                            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (!stack.isEmpty()) {
                                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                                    if (itemId != null && itemId.toString().equals(HALO_OF_THE_END_ID)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    private void summonApostleServant(Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);

        BlockPos spawnPos = player.blockPosition().offset(1, 0, 1);

        ApostleServant servant = new ApostleServant(ModEntities.APOSTLE_SERVANT.get(), serverLevel);
        servant.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        servant.setTrueOwner(player);

        UUID servantUUID = servant.getUUID();
        long expiryTime = serverLevel.getGameTime() + SERVANT_LIFETIME;

        data.addServantExpiry(servantUUID, expiryTime);
        data.addPreventDrop(servantUUID);

        servant.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);

        serverLevel.addFreshEntity(servant);
        TRACKED_ENTITIES.add(new WeakReference<>(servant));
        activateTracking();

        player.sendSystemMessage(Component.literal("§6使徒仆从已召唤，将持续30分钟"));
    }

    private void convertToApollyon(LivingEntity target) {
        ServerLevel serverLevel = (ServerLevel) target.level();
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);

        UUID entityUUID = target.getUUID();
        long expiryTime = target.level().getGameTime() + APOLLYON_DURATION;

        data.addApollyonExpiry(entityUUID, expiryTime);

        CompoundTag entityNbt = target.saveWithoutId(new CompoundTag());
        entityNbt.putByte(IS_APOLLYON_TAG, (byte) 1);
        target.load(entityNbt);

        if (data.shouldPreventDrop(entityUUID)) {
            target.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);
        }

        TRACKED_ENTITIES.add(new WeakReference<>(target));
        activateTracking();

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                    target.getX(), target.getY() + 1, target.getZ(),
                    50, 0.5, 0.5, 0.5, 0.1
            );
        }

        if (!target.level().isClientSide) {
            Player nearestPlayer = target.level().getNearestPlayer(target, 10);
            if (nearestPlayer != null) {
                nearestPlayer.sendSystemMessage(Component.literal("§c使徒已暂时转化为亚形态，将持续5分钟"));
            }
        }
    }

    private void restoreFromApollyon(LivingEntity target) {
        ServerLevel serverLevel = (ServerLevel) target.level();
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);

        UUID entityUUID = target.getUUID();
        data.removeApollyonExpiry(entityUUID);

        CompoundTag entityNbt = target.saveWithoutId(new CompoundTag());
        entityNbt.putByte(IS_APOLLYON_TAG, (byte) 0);
        target.load(entityNbt);

        if (data.shouldPreventDrop(entityUUID)) {
            target.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);
        }

        if (!target.level().isClientSide) {
            Player nearestPlayer = target.level().getNearestPlayer(target, 10);
            if (nearestPlayer != null) {
                nearestPlayer.sendSystemMessage(Component.literal("§a使徒已恢复原形态"));
            }
        }
    }

    private void activateTracking() {
        if (!isTrackingActive) {
            isTrackingActive = true;
        }
    }

    private void deactivateTracking() {
        if (isTrackingActive) {
            isTrackingActive = false;
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        UUID entityUUID = entity.getUUID();

        if (entity.level() instanceof ServerLevel serverLevel) {
            ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
            if (data.shouldPreventDrop(entityUUID)) {
                event.setCanceled(true);
            }
            data.cleanupEntity(entityUUID);
        }
        cleanupDeadReferences();
    }

    /**
     * 玩家登录时检查是否需要重新激活追踪
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level() instanceof ServerLevel) {
            cleanupDeadReferences();
            checkAndReactivateTracking((ServerLevel) event.getEntity().level());
        }
    }

    /**
     * 清理无效的实体引用
     */
    private void cleanupDeadReferences() {
        Iterator<WeakReference<LivingEntity>> iterator = TRACKED_ENTITIES.iterator();
        while (iterator.hasNext()) {
            WeakReference<LivingEntity> ref = iterator.next();
            LivingEntity entity = ref.get();
            if (entity == null || entity.isRemoved() || !entity.isAlive()) {
                UUID entityUUID = null;
                if (entity != null) {
                    entityUUID = entity.getUUID();
                }
                iterator.remove();
                if (entityUUID != null && entity.level() instanceof ServerLevel serverLevel) {
                    ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
                    data.cleanupEntity(entityUUID);
                }
            }
        }

        if (TRACKED_ENTITIES.isEmpty()) {
            deactivateTracking();
        }
    }

    /**
     * 检查并重新激活追踪
     */
    private void checkAndReactivateTracking(ServerLevel serverLevel) {
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
        if (!data.isEmpty()) {
            activateTracking();

            // 重新添加已存在的实体到追踪列表
            for (UUID uuid : data.getServantExpirySnapshot().keySet()) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity instanceof LivingEntity livingEntity) {
                    TRACKED_ENTITIES.add(new WeakReference<>(livingEntity));
                }
            }

            for (UUID uuid : data.getApollyonExpirySnapshot().keySet()) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity instanceof LivingEntity livingEntity) {
                    TRACKED_ENTITIES.add(new WeakReference<>(livingEntity));
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (!isTrackingActive) {
            return;
        }

        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) event.level;
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
        long currentTime = serverLevel.getGameTime();

        // 处理仆从过期
        for (Map.Entry<UUID, Long> entry : data.getServantExpirySnapshot().entrySet()) {
            if (currentTime >= entry.getValue()) {
                UUID entityUUID = entry.getKey();
                Entity entity = serverLevel.getEntity(entityUUID);
                if (entity != null) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                data.cleanupEntity(entityUUID);
            }
        }

        // 处理亚形态过期
        for (Map.Entry<UUID, Long> entry : data.getApollyonExpirySnapshot().entrySet()) {
            if (currentTime >= entry.getValue()) {
                UUID entityUUID = entry.getKey();
                Entity entity = serverLevel.getEntity(entityUUID);
                if (entity instanceof LivingEntity livingEntity) {
                    restoreFromApollyon(livingEntity);
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.SMOKE,
                            livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(),
                            30, 0.5, 0.5, 0.5, 0.05
                    );
                }
                data.removeApollyonExpiry(entityUUID);
            }
        }

        // 清理无效引用
        Iterator<WeakReference<LivingEntity>> iterator = TRACKED_ENTITIES.iterator();
        while (iterator.hasNext()) {
            WeakReference<LivingEntity> ref = iterator.next();
            LivingEntity livingEntity = ref.get();

            if (livingEntity == null || livingEntity.isRemoved() || !livingEntity.isAlive()) {
                UUID entityUUID = null;
                if (livingEntity != null) {
                    entityUUID = livingEntity.getUUID();
                }
                iterator.remove();
                if (entityUUID != null) {
                    data.cleanupEntity(entityUUID);
                }
            }
        }

        // 检查是否需要停用追踪
        if (TRACKED_ENTITIES.isEmpty() && data.isEmpty()) {
            deactivateTracking();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.1")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.2")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.3")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.tooltip.shift"));
        }
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.4")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.5")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.6")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.7")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}