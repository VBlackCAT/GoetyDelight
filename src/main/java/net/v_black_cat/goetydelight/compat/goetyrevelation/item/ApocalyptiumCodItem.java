package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.mega.endinglib.api.client.text.TextColorUtils;
import com.mega.revelationfix.common.entity.boss.ApostleServant;
import com.mega.revelationfix.common.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.api.ITimedEntityManager;
import net.v_black_cat.goetydelight.compat.goetyrevelation.ApocalyptiumData;
import net.v_black_cat.goetydelight.util.TimedEntityManager;
import top.theillusivec4.curios.api.CuriosApi;
import z1gned.goetyrevelation.util.ApollyonAbilityHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ApocalyptiumCodItem extends Item {

    private static final String PREVENT_DROPS_TAG = "PreventDrops";
    private static final String HALO_OF_THE_END_ID = "goety_revelation:halo_of_the_end";

    public static int SERVANT_LIFETIME = 30 * 60 * 20;
    public static int APOLLYON_DURATION = 5 * 60 * 20;

    /** 全局计时管理器 */
    private static final TimedEntityManager MANAGER = TimedEntityManager.getInstance();

    public ApocalyptiumCodItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ==================== 使用 / 交互 ====================

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
                    if (convertToApollyon(target, player)) {
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                } else if (entityId.equals("goety:apostle")) {
                    // 使徒需要特殊条件才能转化
                    if (canConvertApostle(player) && convertToApollyon(target, player)) {
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                }
            }
        }

        return super.interactLivingEntity(stack, player, target, hand);
    }

    // ==================== 转化条件检查 ====================

    private boolean canConvertApostle(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
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

    // ==================== 召唤仆从 ====================

    private void summonApostleServant(Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();

        BlockPos spawnPos = player.blockPosition().offset(1, 0, 1);

        ApostleServant servant = new ApostleServant(ModEntities.APOSTLE_SERVANT.get(), serverLevel);
        servant.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        servant.setTrueOwner(player);

        servant.setLimitedLife(SERVANT_LIFETIME);

        servant.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);

        serverLevel.addFreshEntity(servant);

        // ★ 用接口注册计时：到点自动移除（兜底）
        UUID servantUUID = MANAGER.track(
                player,
                servant,
                SERVANT_LIFETIME,
                ITimedEntityManager.Category.SERVANT,
                ForgeRegistries.ENTITY_TYPES.getKey(servant.getType())
        );

        // 缓存到 ApocalyptiumData（保持兼容）
        ApocalyptiumData.get(serverLevel).cacheEntity(servantUUID, servant);

        player.sendSystemMessage(Component.literal("§6使徒仆从已召唤，将持续30分钟"));
    }

    // ==================== 转化为亚形态 ====================

    /**
     * 将目标转化为亚形态使徒。
     *
     * @return 是否转化成功
     */
    private boolean convertToApollyon(LivingEntity target, Player owner) {
        ServerLevel serverLevel = (ServerLevel) target.level();
        long currentTime = serverLevel.getGameTime();

        // ★ 限制：同一玩家已存在转化后的使徒，则不允许再次转化
        if (owner != null) {
            UUID existing = MANAGER.findExistingTrackedFor(
                    owner, ITimedEntityManager.Category.APOLLYON, currentTime);
            if (existing != null && !existing.equals(target.getUUID())) {
                owner.sendSystemMessage(Component.literal("§c你已经有一个转化后的使徒，无法再次转化"));
                return false;
            }
        }

        // 转化逻辑
        if (target instanceof Apostle apostle) {
            ApollyonAbilityHelper helper = (ApollyonAbilityHelper) apostle;
            helper.allTitlesApostle_1_20_1$setApollyon(true);
        }

        // ★ 用接口注册计时：到点执行 restoreFromApollyon
        if (owner != null) {
            MANAGER.track(
                    owner,
                    target,
                    APOLLYON_DURATION,
                    ITimedEntityManager.Category.APOLLYON,
                    ForgeRegistries.ENTITY_TYPES.getKey(target.getType())
            );
        }

        // 缓存到 ApocalyptiumData（保持兼容）
        ApocalyptiumData.get(serverLevel).cacheEntity(target.getUUID(), target);

        // 粒子效果
        serverLevel.sendParticles(
                ParticleTypes.ENCHANTED_HIT,
                target.getX(), target.getY() + 1, target.getZ(),
                50, 0.5, 0.5, 0.5, 0.1
        );

        // 提示
        Player nearestPlayer = serverLevel.getNearestPlayer(target, 10);
        if (nearestPlayer != null) {
            nearestPlayer.sendSystemMessage(Component.literal("§c使徒已暂时转化为亚形态，将持续5分钟"));
        }
        return true;
    }

    // ==================== 恢复形态 ====================

    private void restoreFromApollyon(LivingEntity target, Player owner) {
        ServerLevel serverLevel = (ServerLevel) target.level();
        UUID entityUUID = target.getUUID();

        if (target instanceof Apostle apostle) {
            ApollyonAbilityHelper helper = (ApollyonAbilityHelper) apostle;
            helper.setDoom(false);
            helper.allTitlesApostle_1_20_1$setApollyon(false);
        }

        // 计时清理已由 handleExpiry 内部处理；这里只做形态恢复
        // 保留 PreventDrops（使徒恢复后仍然不掉落），由调用方决定是否 removePreventDrop

        ApocalyptiumData.get(serverLevel).removeCachedEntity(entityUUID);
    }

    // ==================== 掉落阻止 ====================

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        UUID entityUUID = entity.getUUID();

        if (entity.level() instanceof ServerLevel serverLevel) {
            // 1. 反查 owner
            Player owner = TimedEntityManager.findOwnerByPreventDrop(serverLevel, entityUUID);
            if (owner == null) {
                return;
            }

            // 2. 阻止掉落
            if (MANAGER.shouldPreventDrop(owner, entityUUID)) {
                event.setCanceled(true);
            }

            // 3. 补缓存（供其他逻辑使用）
            ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
            if (data.getCachedEntity(entityUUID) == null) {
                Entity worldEntity = serverLevel.getEntity(entityUUID);
                if (worldEntity instanceof LivingEntity le) {
                    data.cacheEntity(entityUUID, le);
                }
            }

            // 4. 清理该玩家身上关于这个实体的所有记录
            MANAGER.untrackAll(owner, entityUUID);
        }
    }

    // ==================== 实体重新加载 ====================

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        UUID uuid = living.getUUID();

        // 用 PreventDrops 反查 owner
        Player owner = TimedEntityManager.findOwnerByPreventDrop(serverLevel, uuid);
        if (owner == null) return;

        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
        data.cacheEntity(uuid, living);

        long currentTime = serverLevel.getGameTime();

        // 亚形态加载时已过期 → 立即恢复
        if (MANAGER.isExpired(owner, uuid, ITimedEntityManager.Category.APOLLYON, currentTime)) {
            restoreFromApollyon(living, owner);
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    living.getX(), living.getY() + 1, living.getZ(),
                    30, 0.5, 0.5, 0.5, 0.05
            );
            MANAGER.untrack(owner, uuid, ITimedEntityManager.Category.APOLLYON);
        }

        // 仆从加载时已过期 → 立即移除
        if (MANAGER.isExpired(owner, uuid, ITimedEntityManager.Category.SERVANT, currentTime)) {
            living.remove(Entity.RemovalReason.DISCARDED);
            MANAGER.untrackAll(owner, uuid);
        }
    }

    // ==================== Tick 检查 ====================

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) event.level;

        for (Player player : serverLevel.getServer().getPlayerList().getPlayers()) {
            if (MANAGER.isEmpty(player)) continue;

            // 仆从过期：直接移除
            for (UUID uuid : MANAGER.getSnapshot(player, ITimedEntityManager.Category.SERVANT).keySet()) {
                MANAGER.handleExpiry(player, uuid, ITimedEntityManager.Category.SERVANT, null);
            }

            // 亚形态过期：恢复形态
            for (UUID uuid : MANAGER.getSnapshot(player, ITimedEntityManager.Category.APOLLYON).keySet()) {
                MANAGER.handleExpiry(player, uuid, ITimedEntityManager.Category.APOLLYON,
                        living -> restoreFromApollyon(living, player));
            }
        }
    }

    // ==================== Tooltip ====================

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