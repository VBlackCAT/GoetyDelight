package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.v_black_cat.goetydelight.item.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class BiscatItem extends Item {

    private static final String BISCUIT_EFFECT_TAG = "BiscatEffectTime";
    private static final String BISCUIT_AFFECTED_PLAYERS_TAG = "BiscatAffectedPlayers";
    private static final int EFFECT_DURATION_TICKS = 20 * 60 * 5;

    public BiscatItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();

            persistentData.putLong(BISCUIT_EFFECT_TAG, level.getGameTime() + EFFECT_DURATION_TICKS);

            markCreeperAsAffected(player, persistentData);
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
        CompoundTag playerData = player.getPersistentData();

        long effectEndTime = playerData.getLong(BISCUIT_EFFECT_TAG);
        long currentTime = level.getGameTime();

        if (effectEndTime > 0 && currentTime >= effectEndTime) {
            playerData.remove(BISCUIT_EFFECT_TAG);
        }
    }

    private void markCreeperAsAffected(Player player, CompoundTag playerData) {
        List<Creeper> creepers = player.level().getEntitiesOfClass(Creeper.class,
                player.getBoundingBox().inflate(50),
                entity -> true);

        for (Creeper creeper : creepers) {
            CompoundTag creeperData = creeper.getPersistentData();
            CompoundTag affectedPlayers = creeperData.contains(BISCUIT_AFFECTED_PLAYERS_TAG)
                    ? creeperData.getCompound(BISCUIT_AFFECTED_PLAYERS_TAG)
                    : new CompoundTag();

            affectedPlayers.putLong(player.getStringUUID(), playerData.getLong(BISCUIT_EFFECT_TAG));
            creeperData.put(BISCUIT_AFFECTED_PLAYERS_TAG, affectedPlayers);
        }

        List<Phantom> phantoms = player.level().getEntitiesOfClass(Phantom.class,
                player.getBoundingBox().inflate(50),
                entity -> true);

        for (Phantom phantom : phantoms) {
            CompoundTag phantomData = phantom.getPersistentData();
            CompoundTag affectedPlayers = phantomData.contains(BISCUIT_AFFECTED_PLAYERS_TAG)
                    ? phantomData.getCompound(BISCUIT_AFFECTED_PLAYERS_TAG)
                    : new CompoundTag();

            affectedPlayers.putLong(player.getStringUUID(), playerData.getLong(BISCUIT_EFFECT_TAG));
            phantomData.put(BISCUIT_AFFECTED_PLAYERS_TAG, affectedPlayers);
        }
    }

    @SubscribeEvent
    public static void onCreeperTarget(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) {
            return;
        }

        if (event.getNewTarget() instanceof Player player) {
            CompoundTag playerData = player.getPersistentData();
            long effectEndTime = playerData.getLong(BISCUIT_EFFECT_TAG);
            long currentTime = player.level().getGameTime();

            if (effectEndTime > 0 && currentTime < effectEndTime) {
                event.setNewTarget(null);

                CompoundTag creeperData = creeper.getPersistentData();
                CompoundTag affectedPlayers = creeperData.contains(BISCUIT_AFFECTED_PLAYERS_TAG)
                        ? creeperData.getCompound(BISCUIT_AFFECTED_PLAYERS_TAG)
                        : new CompoundTag();

                if (!affectedPlayers.contains(player.getStringUUID())) {
                    affectedPlayers.putLong(player.getStringUUID(), effectEndTime);
                    creeperData.put(BISCUIT_AFFECTED_PLAYERS_TAG, affectedPlayers);
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
                                CompoundTag creeperTag = creeper.getPersistentData();
                                if (!creeperTag.contains(BISCUIT_AFFECTED_PLAYERS_TAG)) {
                                    return false;
                                }
                                return creeperTag.getCompound(BISCUIT_AFFECTED_PLAYERS_TAG).contains(entity.getStringUUID());
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
            CompoundTag playerData = player.getPersistentData();
            long effectEndTime = playerData.getLong(BISCUIT_EFFECT_TAG);
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

        CompoundTag creeperData = creeper.getPersistentData();
        if (!creeperData.contains(BISCUIT_AFFECTED_PLAYERS_TAG)) {
            return;
        }

        CompoundTag affectedPlayers = creeperData.getCompound(BISCUIT_AFFECTED_PLAYERS_TAG);
        long currentTime = level.getGameTime();

        affectedPlayers.getAllKeys().removeIf(playerUuid -> {
            long effectEndTime = affectedPlayers.getLong(playerUuid);
            return currentTime >= effectEndTime;
        });

        if (affectedPlayers.isEmpty()) {
            creeperData.remove(BISCUIT_AFFECTED_PLAYERS_TAG);

            creeper.goalSelector.getAvailableGoals().removeIf(goal ->
                    goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>
            );
            return;
        }

        creeperData.put(BISCUIT_AFFECTED_PLAYERS_TAG, affectedPlayers);

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
                        return affectedPlayers.contains(entity.getStringUUID());
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
