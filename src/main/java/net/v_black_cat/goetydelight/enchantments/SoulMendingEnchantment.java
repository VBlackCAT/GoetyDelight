package net.v_black_cat.goetydelight.enchantments;
import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.config.Config;
import net.v_black_cat.goetydelight.util.SearchServant;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class SoulMendingEnchantment extends Enchantment {

    private static final int REPAIR_INTERVAL_TICKS = 4;
    private static final PriorityQueue<ScheduledRepair> REPAIR_QUEUE = new PriorityQueue<>(
            Comparator.comparingLong(ScheduledRepair::tick)
                    .thenComparingLong(ScheduledRepair::sequence)
    );
    private static final Map<UUID, Long> SCHEDULED_PLAYERS = new HashMap<>();
    private static long repairSequence;

    public SoulMendingEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 15 + (enchantmentLevel - 1) * 9;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return this.getMinCost(enchantmentLevel) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 3; 
    }

    @Override
    public boolean isTreasureOnly() {
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        return false; 
    }

    @Override
    public boolean isTradeable() {
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        return true; 
    }

    @Override
    public boolean isDiscoverable() {
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        return true; 
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.isDamageableItem(); 
    }

    @Override
    public Rarity getRarity() {
       return Rarity.VERY_RARE;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if(Config.isSoulMendingDisabled()){
            return false;
        }
        if (Config.getSoulMendingBlacklist().contains(stack.getItem())){
            return false;
        }

        return stack.isDamageableItem();
    }


    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        long gameTime = event.getServer().overworld().getGameTime();
        while (!REPAIR_QUEUE.isEmpty() && REPAIR_QUEUE.peek().tick() <= gameTime) {
            ScheduledRepair scheduled = REPAIR_QUEUE.poll();
            if (!Long.valueOf(scheduled.sequence()).equals(SCHEDULED_PLAYERS.get(scheduled.playerId()))) {
                continue;
            }
            SCHEDULED_PLAYERS.remove(scheduled.playerId());

            ServerPlayer player = event.getServer().getPlayerList().getPlayer(scheduled.playerId());
            if (player == null || player.isRemoved()) {
                continue;
            }

            repairPlayer(player);
            schedule(player, gameTime + REPAIR_INTERVAL_TICKS);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            schedule(player, player.level().getGameTime() + REPAIR_INTERVAL_TICKS);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SCHEDULED_PLAYERS.remove(event.getEntity().getUUID());
    }

    private static void schedule(ServerPlayer player, long tick) {
        if (SCHEDULED_PLAYERS.containsKey(player.getUUID())) {
            return;
        }
        long sequence = repairSequence++;
        SCHEDULED_PLAYERS.put(player.getUUID(), sequence);
        REPAIR_QUEUE.add(new ScheduledRepair(player.getUUID(), tick, sequence));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        REPAIR_QUEUE.clear();
        SCHEDULED_PLAYERS.clear();
        repairSequence = 0L;
    }

    private static void repairPlayer(ServerPlayer serverPlayer) {
        java.util.Optional<SearchServant.ServantData> servantDataOpt = SearchServant.getServantData(serverPlayer);
        for (ItemStack stack : serverPlayer.getAllSlots()) {
            int enchantmentLevel = stack.getEnchantmentLevel(ModEnchantments.SOUL_MENDING.get());
            if (enchantmentLevel > 0) {
                repairItemWithSoulEnergy(serverPlayer, stack, enchantmentLevel);
            }
        }

        if (servantDataOpt.isEmpty()) {
            return;
        }

        SearchServant.ServantData servantData = servantDataOpt.get();
        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) serverPlayer.level();
        for (UUID servantUUID : servantData.servantUUIDs) {
            Entity entity = level.getEntity(servantUUID);
            if (!(entity instanceof LivingEntity servant)) {
                continue;
            }

            if (servant instanceof IOwned owned && owned.getTrueOwner() instanceof Player owner) {
                repairServantSlots(owner, servant);
            } else if (servant instanceof OwnableEntity ownableEntity
                    && ownableEntity.getOwner() instanceof Player owner) {
                repairServantSlots(owner, servant);
            }
        }
    }

    private static void repairServantSlots(Player owner, LivingEntity servant) {
        for (ItemStack stack : servant.getAllSlots()) {
            int enchantmentLevel = stack.getEnchantmentLevel(ModEnchantments.SOUL_MENDING.get());
            if (enchantmentLevel > 0) {
                repairItemWithSoulEnergy(owner, stack, enchantmentLevel);
            }
        }
    }

    private static void repairItemWithSoulEnergy(Player player, ItemStack stack, int enchantmentLevel) {
        
        int currentDamage = stack.getDamageValue();

        if (currentDamage <= 0) return;

        int actualRepair = Math.min(enchantmentLevel, currentDamage);

        int requiredSouls;
        if (enchantmentLevel > 9) {
            requiredSouls = 1;
        } else if (actualRepair < enchantmentLevel) {
            requiredSouls = Math.max(1, 5 - enchantmentLevel / 2);
        } else {
            requiredSouls = actualRepair * 5;
        }
        if (requiredSouls <= 0 || SEHelper.getSoulsAmount(player, requiredSouls)) {
            if (requiredSouls > 0) {
                SEHelper.decreaseSouls(player, requiredSouls);
            }
            int newDamage = currentDamage - actualRepair;
            stack.setDamageValue(newDamage);
        }
    }

    private record ScheduledRepair(UUID playerId, long tick, long sequence) {
    }
}
