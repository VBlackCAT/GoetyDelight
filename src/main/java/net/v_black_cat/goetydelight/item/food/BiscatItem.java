package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.capability.FoodStateCapability;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.util.FoodState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class BiscatItem extends Item {

    private static final int EFFECT_DURATION_TICKS = 20 * 60 * 5;

    public BiscatItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            FoodState state = FoodStateCapability.get(player);
            if (state != null) {
                state.setBiscatEffectEndTime(level.getGameTime() + EFFECT_DURATION_TICKS);
                markCreeperAsAffected(player, state.getBiscatEffectEndTime());
            }
        }

        return resultStack;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        Level level = player.level();
        FoodState state = FoodStateCapability.get(player);
        if (state == null) return;

        long effectEndTime = state.getBiscatEffectEndTime();
        long currentTime = level.getGameTime();

        if (effectEndTime > 0 && currentTime >= effectEndTime) {
            state.setBiscatEffectEndTime(0);
        }
    }

    private void markCreeperAsAffected(Player player, long effectEndTime) {
        List<Creeper> creepers = player.level().getEntitiesOfClass(Creeper.class,
                player.getBoundingBox().inflate(50),
                entity -> true);

        for (Creeper creeper : creepers) {
            FoodState state = FoodStateCapability.get(creeper);
            if (state != null) {
                state.getBiscatAffectedPlayers().put(player.getStringUUID(), effectEndTime);
            }
        }

        List<Phantom> phantoms = player.level().getEntitiesOfClass(Phantom.class,
                player.getBoundingBox().inflate(50),
                entity -> true);

        for (Phantom phantom : phantoms) {
            FoodState state = FoodStateCapability.get(phantom);
            if (state != null) {
                state.getBiscatAffectedPlayers().put(player.getStringUUID(), effectEndTime);
            }
        }
    }

    @SubscribeEvent
    public static void onCreeperTarget(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) {
            return;
        }

        if (event.getNewTarget() instanceof Player player) {
            FoodState playerState = FoodStateCapability.get(player);
            if (playerState == null) return;
            long effectEndTime = playerState.getBiscatEffectEndTime();
            long currentTime = player.level().getGameTime();

            if (effectEndTime > 0 && currentTime < effectEndTime) {
                event.setNewTarget(null);

                FoodState creeperState = FoodStateCapability.get(creeper);
                if (creeperState != null && !creeperState.getBiscatAffectedPlayers().containsKey(player.getStringUUID())) {
                    creeperState.getBiscatAffectedPlayers().put(player.getStringUUID(), effectEndTime);
                }

                boolean hasAvoidGoal = creeper.goalSelector.getAvailableGoals().stream()
                        .anyMatch(goal ->
                                goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>
                        );

                if (!hasAvoidGoal) {
                    creeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                            creeper,
                            Player.class,
                            8.0F,
                            1.5D,
                            1.5D,
                            entity -> {
                                if (entity == null || !entity.isAlive()) {
                                    return false;
                                }
                                FoodState cs = FoodStateCapability.get(creeper);
                                return cs != null && cs.getBiscatAffectedPlayers().containsKey(entity.getStringUUID());
                            }
                    ));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPhantomTarget(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) {
            return;
        }

        if (event.getNewTarget() instanceof Player player) {
            FoodState playerState = FoodStateCapability.get(player);
            if (playerState == null) return;
            long effectEndTime = playerState.getBiscatEffectEndTime();
            long currentTime = player.level().getGameTime();

            if (effectEndTime > 0 && currentTime < effectEndTime) {
                event.setNewTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onCreeperTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) {
            return;
        }

        Level level = creeper.level();
        if (level.isClientSide) {
            return;
        }

        FoodState creeperState = FoodStateCapability.get(creeper);
        if (creeperState == null || creeperState.getBiscatAffectedPlayers().isEmpty()) {
            return;
        }

        Map<String, Long> affectedPlayers = creeperState.getBiscatAffectedPlayers();
        long currentTime = level.getGameTime();

        affectedPlayers.entrySet().removeIf(entry -> currentTime >= entry.getValue());

        if (affectedPlayers.isEmpty()) {
            creeper.goalSelector.getAvailableGoals().removeIf(goal ->
                    goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>
            );
            return;
        }

        boolean hasAvoidGoal = creeper.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal ->
                        goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>
                );

        if (!hasAvoidGoal) {
            creeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    creeper,
                    Player.class,
                    8.0F,
                    1.5D,
                    1.5D,
                    entity -> {
                        if (entity == null || !entity.isAlive()) {
                            return false;
                        }
                        return affectedPlayers.containsKey(entity.getStringUUID());
                    }
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

                    ItemStack biscatStack = new ItemStack(ModItems.BISCAT.get());
                    if (!player.getInventory().add(biscatStack)) {
                        player.drop(biscatStack, false);
                    }

                    event.setCanceled(true);
                }
            }
        }
    }
}
