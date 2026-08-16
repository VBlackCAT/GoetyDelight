package net.v_black_cat.goetydelight.ability;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.utils.LichdomHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModConfig;

import java.util.List;
import java.util.UUID;

public class MinionBoost {

    private static final ResourceLocation ATTACK_DAMAGE_BOOST_ID = rl("attack");
    private static final ResourceLocation MAX_HEALTH_BOOST_ID = rl("health");
    private static final ResourceLocation ARMOR_BOOST_ID = rl("armor");
    private static final ResourceLocation MOVEMENT_SPEED_BOOST_ID = rl("speed");
    private static final ResourceLocation ARMOR_TOUGHNESS_BOOST_ID = rl("toughness");

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "minion_" + path);
    }

    // ==================== 数据读写 ====================
    public static int getStewBoostCount(Player player) {
        return player.getData(ModAttachments.MINION_STEW_BOOST_COUNT);
    }

    public static int getSoupBoostCount(Player player) {
        return player.getData(ModAttachments.MINION_SOUP_BOOST_COUNT);
    }

    public static void increaseStewBoostCount(Player player) {
        int cur = getStewBoostCount(player);
        if (cur < ModConfig.getLichStewMaxCount()) {
            player.setData(ModAttachments.MINION_STEW_BOOST_COUNT, cur + 1);
            applyMinionBoosts(player);
        }
    }

    public static void increaseSoupBoostCount(Player player) {
        int cur = getSoupBoostCount(player);
        if (cur < ModConfig.getNightPeaSoupMaxCount()) {
            player.setData(ModAttachments.MINION_SOUP_BOOST_COUNT, cur + 1);
            applyMinionBoosts(player);
        }
    }

    public static void increaseStew(Player player) {
        increaseStewBoostCount(player);
    }

    public static void increaseSoup(Player player) {
        increaseSoupBoostCount(player);
    }

    // ==================== 刷新所有仆从 ====================
    public static void applyMinionBoosts(Player player) {
        if (player.level().isClientSide()) return;
        int stew = getStewBoostCount(player);
        int soup = getSoupBoostCount(player);
        if (stew <= 0 && soup <= 0) return;

        double radius = 128.0;
        List<LivingEntity> minions = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e instanceof IOwned && ((IOwned) e).getTrueOwner() == player
        );
        for (LivingEntity minion : minions) {
            applyMinionBoost(minion, player, stew, soup);
        }
    }

    // ==================== 应用单个仆从加成 ====================
    public static void applyMinionBoost(LivingEntity minion, Player owner, int stewCount, int soupCount) {
        if (minion.level().isClientSide()) return;

        removeMinionBoost(minion);

        double stewPct = LichdomHelper.isLich(owner)
                ? ModConfig.getLichChaosStewBoostPercentage() * stewCount
                : 0;
        double soupPct = ModConfig.getNightHeartPeaSoupBoostPercentage() * soupCount;
        double total = stewPct + soupPct;
        if (total <= 0) return;

        addModifier(minion, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_BOOST_ID, total);
        addModifier(minion, Attributes.MAX_HEALTH, MAX_HEALTH_BOOST_ID, total);
        addModifier(minion, Attributes.ARMOR, ARMOR_BOOST_ID, total);
        addModifier(minion, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_BOOST_ID, total);
        addModifier(minion, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_BOOST_ID, soupPct);

        if (minion.getHealth() > minion.getMaxHealth()) {
            minion.setHealth(minion.getMaxHealth());
        }
    }

    private static void addModifier(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id, double multiplier) {
        if (multiplier <= 0) return;
        AttributeInstance ai = entity.getAttribute(attr);
        if (ai == null) return;
        ai.removeModifier(id);
        double value = ai.getBaseValue() * multiplier;
        ai.addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
    }

    public static void removeMinionBoost(LivingEntity minion) {
        removeModifier(minion, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_BOOST_ID);
        removeModifier(minion, Attributes.MAX_HEALTH, MAX_HEALTH_BOOST_ID);
        removeModifier(minion, Attributes.ARMOR, ARMOR_BOOST_ID);
        removeModifier(minion, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_BOOST_ID);
        removeModifier(minion, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_BOOST_ID);
    }

    private static void removeModifier(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id) {
        AttributeInstance ai = entity.getAttribute(attr);
        if (ai != null) {
            ai.removeModifier(id);
        }
    }

    // ==================== 事件监听 ====================
    @EventBusSubscriber(modid = GoetyDelight.MODID)
    public static class MinionBoostHandler {
        // 附件 MINION_STEW_BOOST_COUNT / MINION_SOUP_BOOST_COUNT 已配置 copyOnDeath，
        // 死亡复活时会自动复制层数，无需手动 onPlayerClone 拷贝。

        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide()) return;
            if (!(event.getEntity() instanceof LivingEntity entity)) return;
            if (!(entity instanceof IOwned owned)) return;

            tryApplyBoost(entity, owned, 0);
        }

        private static void tryApplyBoost(LivingEntity entity, IOwned owned, int attempt) {
            if (attempt > 20) return;

            Player player = null;
            UUID ownerId = owned.getOwnerId();
            if (ownerId != null && entity.level() instanceof ServerLevel serverLevel) {
                MinecraftServer server = serverLevel.getServer();
                player = server.getPlayerList().getPlayer(ownerId);
                if (player == null) {
                    for (Player p : server.getPlayerList().getPlayers()) {
                        if (p.getUUID().equals(ownerId)) {
                            player = p;
                            break;
                        }
                    }
                }
            }

            if (player == null) {
                LivingEntity owner = owned.getTrueOwner();
                if (owner instanceof Player) {
                    player = (Player) owner;
                }
            }

            if (player != null) {
                int stew = getStewBoostCount(player);
                int soup = getSoupBoostCount(player);
                if (stew > 0 || soup > 0) {
                    applyMinionBoost(entity, player, stew, soup);
                }
                return;
            }

            if (entity.level() instanceof ServerLevel serverLevel) {
                MinecraftServer server = serverLevel.getServer();
                server.tell(new TickTask(server.getTickCount() + 5, () -> {
                    if (entity.isAlive()) {
                        tryApplyBoost(entity, owned, attempt + 1);
                    }
                }));
            }
        }
    }
}