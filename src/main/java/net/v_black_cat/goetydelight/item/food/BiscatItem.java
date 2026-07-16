package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.v_black_cat.goetydelight.init.ModItems;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class BiscatItem extends Item {

    private static final String BISCUIT_EFFECT_TAG = "BiscatEffectTime";
    private static final String BISCUIT_AFFECTED_PLAYERS_TAG = "BiscatAffectedPlayers";
    private static final int EFFECT_DURATION_TICKS = 20 * 60 * 5;

    public BiscatItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            persistentData.putLong(BISCUIT_EFFECT_TAG, level.getGameTime() + EFFECT_DURATION_TICKS);
            markAffectedMobs(player, persistentData);
        }
        return resultStack;
    }

    private void markAffectedMobs(Player player, CompoundTag playerData) {
        List<Creeper> creepers = player.level().getEntitiesOfClass(Creeper.class,
                player.getBoundingBox().inflate(50), entity -> true);
        for (Creeper creeper : creepers) {
            CompoundTag creeperData = creeper.getPersistentData();
            CompoundTag affectedPlayers = creeperData.contains(BISCUIT_AFFECTED_PLAYERS_TAG)
                    ? creeperData.getCompound(BISCUIT_AFFECTED_PLAYERS_TAG)
                    : new CompoundTag();
            affectedPlayers.putLong(player.getStringUUID(), playerData.getLong(BISCUIT_EFFECT_TAG));
            creeperData.put(BISCUIT_AFFECTED_PLAYERS_TAG, affectedPlayers);
        }

        List<Phantom> phantoms = player.level().getEntitiesOfClass(Phantom.class,
                player.getBoundingBox().inflate(50), entity -> true);
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
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;
        CompoundTag playerData = player.getPersistentData();
        long effectEndTime = playerData.getLong(BISCUIT_EFFECT_TAG);
        if (effectEndTime > 0 && level.getGameTime() >= effectEndTime) {
            playerData.remove(BISCUIT_EFFECT_TAG);
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

        CompoundTag playerData = player.getPersistentData();
        long effectEndTime = playerData.getLong(BISCUIT_EFFECT_TAG);
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

        CompoundTag creeperData = creeper.getPersistentData();
        if (!creeperData.contains(BISCUIT_AFFECTED_PLAYERS_TAG)) {
            creeper.goalSelector.getAvailableGoals().removeIf(goal ->
                    goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
            return;
        }

        CompoundTag affectedPlayers = creeperData.getCompound(BISCUIT_AFFECTED_PLAYERS_TAG);
        long currentTime = level.getGameTime();

        List<String> toRemove = new ArrayList<>();
        for (String uuid : affectedPlayers.getAllKeys()) {
            if (currentTime >= affectedPlayers.getLong(uuid)) {
                toRemove.add(uuid);
            }
        }
        toRemove.forEach(affectedPlayers::remove);

        if (affectedPlayers.isEmpty()) {
            creeperData.remove(BISCUIT_AFFECTED_PLAYERS_TAG);
            creeper.goalSelector.getAvailableGoals().removeIf(goal ->
                    goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
            return;
        }

        creeperData.put(BISCUIT_AFFECTED_PLAYERS_TAG, affectedPlayers);

        boolean hasAvoidGoal = creeper.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getPriority() == 1 && goal.getGoal() instanceof AvoidEntityGoal<?>);
        if (!hasAvoidGoal) {
            creeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    creeper,
                    Player.class,
                    8.0F,
                    1.5D,
                    1.5D,
                    entity -> entity != null && entity.isAlive() && affectedPlayers.contains(entity.getStringUUID())
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