package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.util.FoodState;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class BiscatItem extends Item {

    private static final int EFFECT_DURATION_TICKS = 20 * 60 * 5;

    public BiscatItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            FoodState state = player.getData(ModAttachments.FOOD_STATE);
            state.setBiscatEffectEndTime(level.getGameTime() + EFFECT_DURATION_TICKS);
            markAffectedMobs(player, state.getBiscatEffectEndTime());
        }
        return resultStack;
    }

    private void markAffectedMobs(Player player, long effectEndTime) {
        List<Creeper> creepers = player.level().getEntitiesOfClass(Creeper.class,
                player.getBoundingBox().inflate(50), entity -> true);
        for (Creeper creeper : creepers) {
            Map<String, Long> affectedPlayers = creeper.getData(ModAttachments.FOOD_STATE)
                    .getBiscatAffectedPlayers();
            affectedPlayers.put(player.getStringUUID(), effectEndTime);
            // 【优化】原先靠「每个苦力怕每 tick 自检」来挂/摘避让 AI；现在标记时就挂上，
            // 到期由队列处理 —— 苦力怕不再承担每 tick 的检查开销。
            addAvoidGoal(creeper, affectedPlayers);
            scheduleExpiry(creeper, effectEndTime);
        }

        List<Phantom> phantoms = player.level().getEntitiesOfClass(Phantom.class,
                player.getBoundingBox().inflate(50), entity -> true);
        for (Phantom phantom : phantoms) {
            phantom.getData(ModAttachments.FOOD_STATE).getBiscatAffectedPlayers()
                    .put(player.getStringUUID(), effectEndTime);
        }
    }

    // 使用 LivingChangeTargetEvent 拦截目标变更
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        // 仅处理怪物主动选择目标
        if (event.getTargetType() != LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
            return;
        }

        if (!(event.getEntity() instanceof Creeper) && !(event.getEntity() instanceof Phantom)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (!(newTarget instanceof Player player)) {
            return;
        }

        FoodState playerState = player.getData(ModAttachments.FOOD_STATE);
        long effectEndTime = playerState.getBiscatEffectEndTime();
        long currentTime = player.level().getGameTime();
        if (effectEndTime > 0 && currentTime < effectEndTime) {
            // 取消目标设定
            event.setNewAboutToBeSetTarget(null);
            event.setCanceled(true);
        }
    }

    // ==================== 苦力怕避让 AI：到期驱动 ====================
    // 旧实现订阅 EntityTickEvent，对每个苦力怕每 tick 做一次「map 剪枝 + goal 扫描（stream + removeIf）」，
    // 无论它附近有没有受影响的玩家。现在改成：标记时挂 goal、到期队列到点时摘 goal + 剪枝。

    /** 到期记录（按维度分桶）：只在到期那一刻才需要碰这个苦力怕。 */
    private static final Map<ResourceKey<Level>, PriorityQueue<Expiry>> EXPIRIES = new HashMap<>();

    private record Expiry(long dueTick, int creeperId) {
    }

    private static void addAvoidGoal(Creeper creeper, Map<String, Long> affectedPlayers) {
        boolean hasAvoidGoal = creeper.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
        if (hasAvoidGoal) return;

        creeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                creeper,
                Player.class,
                8.0F,
                1.5D,
                1.5D,
                // 判定带上时效：即使实体在未加载区块里错过了到期处理，goal 也不会一直生效
                entity -> entity != null && entity.isAlive()
                        && affectedPlayers.getOrDefault(entity.getStringUUID(), 0L)
                                > creeper.level().getGameTime()
        ));
    }

    private static void removeAvoidGoal(Creeper creeper) {
        creeper.goalSelector.getAvailableGoals().removeIf(goal ->
                goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
    }

    private static void scheduleExpiry(Creeper creeper, long dueTick) {
        if (!(creeper.level() instanceof ServerLevel serverLevel)) return;
        EXPIRIES.computeIfAbsent(serverLevel.dimension(),
                        key -> new PriorityQueue<>(Comparator.comparingLong(Expiry::dueTick)))
                .add(new Expiry(dueTick, creeper.getId()));
    }

    /** 只处理本 tick 到期的苦力怕：摘掉避让 AI 并清掉过期条目。 */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        PriorityQueue<Expiry> queue = EXPIRIES.get(level.dimension());
        if (queue == null || queue.isEmpty()) {
            return; // 没有任何待到期记录：零成本
        }

        long gameTime = level.getGameTime();
        while (!queue.isEmpty() && queue.peek().dueTick() <= gameTime) {
            Expiry expiry = queue.poll();
            Entity entity = level.getEntity(expiry.creeperId());
            if (!(entity instanceof Creeper creeper)) {
                continue;
            }
            Map<String, Long> affectedPlayers = creeper.getData(ModAttachments.FOOD_STATE)
                    .getBiscatAffectedPlayers();
            affectedPlayers.entrySet().removeIf(entry -> gameTime >= entry.getValue());
            if (affectedPlayers.isEmpty()) {
                removeAvoidGoal(creeper);
            }
        }

        if (queue.isEmpty()) {
            EXPIRIES.remove(level.dimension());
        }
    }

    @SubscribeEvent
    public static void onRightClickCat(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Cat cat && cat.isTame()) {
            Player player = event.getEntity();
            ItemStack heldItem = player.getItemInHand(event.getHand());
            if (heldItem.is(Items.ORANGE_TULIP)) {
                if (!player.level().isClientSide) {
                    cat.level().addParticle(ParticleTypes.HEART, cat.getX(), cat.getY() + 0.5, cat.getZ(), 0, 0, 0);
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                    ItemStack biscatStack = new ItemStack(ModItems.BISCAT.get()); // 确保注册名
                    if (!player.getInventory().add(biscatStack)) {
                        player.drop(biscatStack, false);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }
}