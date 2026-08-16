package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.particles.ParticleTypes;
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
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.util.FoodState;

import java.util.List;
import java.util.Map;

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
            creeper.getData(ModAttachments.FOOD_STATE).getBiscatAffectedPlayers()
                    .put(player.getStringUUID(), effectEndTime);
        }

        List<Phantom> phantoms = player.level().getEntitiesOfClass(Phantom.class,
                player.getBoundingBox().inflate(50), entity -> true);
        for (Phantom phantom : phantoms) {
            phantom.getData(ModAttachments.FOOD_STATE).getBiscatAffectedPlayers()
                    .put(player.getStringUUID(), effectEndTime);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;
        FoodState state = player.getData(ModAttachments.FOOD_STATE);
        long effectEndTime = state.getBiscatEffectEndTime();
        if (effectEndTime > 0 && level.getGameTime() >= effectEndTime) {
            state.setBiscatEffectEndTime(0);
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

    // 维护 Creeper 避让 AI（可选）
    @SubscribeEvent
    public static void onCreeperTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        Level level = creeper.level();
        if (level.isClientSide) return;

        FoodState creeperState = creeper.getData(ModAttachments.FOOD_STATE);
        Map<String, Long> affectedPlayers = creeperState.getBiscatAffectedPlayers();
        if (affectedPlayers.isEmpty()) {
            creeper.goalSelector.getAvailableGoals().removeIf(goal ->
                    goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
            return;
        }

        long currentTime = level.getGameTime();
        affectedPlayers.entrySet().removeIf(entry -> currentTime >= entry.getValue());

        if (affectedPlayers.isEmpty()) {
            creeper.goalSelector.getAvailableGoals().removeIf(goal ->
                    goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
            return;
        }

        boolean hasAvoidGoal = creeper.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
        if (!hasAvoidGoal) {
            creeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    creeper,
                    Player.class,
                    8.0F,
                    1.5D,
                    1.5D,
                    entity -> entity != null && entity.isAlive() && affectedPlayers.containsKey(entity.getStringUUID())
            ));
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