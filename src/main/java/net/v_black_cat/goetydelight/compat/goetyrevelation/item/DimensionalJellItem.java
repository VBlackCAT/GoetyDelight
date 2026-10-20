package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.google.common.collect.ImmutableMultimap;
import com.mega.endinglib.api.client.text.TextColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DimensionalJellItem extends Item {
    private static final UUID CHEST_SLOT_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-345678901234");
    private static final String IMMUNE_COUNT_TAG = "DimensionalJellImmuneCount";
    private static final String REVIVE_COUNT_TAG = "DimensionalJellReviveCount";
    private static final int DEFAULT_IMMUNE_COUNT = 10;
    private static final int DEFAULT_REVIVE_COUNT = 5;

    public DimensionalJellItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            applyEffects(player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    private void applyEffects(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(IMMUNE_COUNT_TAG, DEFAULT_IMMUNE_COUNT);
        persistentData.putInt(REVIVE_COUNT_TAG, DEFAULT_REVIVE_COUNT);

        CuriosApi.getCuriosInventory(player).ifPresent(inventory ->
                inventory.addPermanentSlotModifiers(ImmutableMultimap.of(
                        "back",
                        new AttributeModifier(
                                CHEST_SLOT_UUID,
                                "goetydelight:dimensional_jell_boost_back",
                                1.0,
                                AttributeModifier.Operation.ADDITION
                        )
                ))
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.contains(IMMUNE_COUNT_TAG)) return;
        int count = data.getInt(IMMUNE_COUNT_TAG);
        if (count > 0 && event.getAmount() >= player.getMaxHealth()) {
            data.putInt(IMMUNE_COUNT_TAG, count - 1);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.contains(IMMUNE_COUNT_TAG)) return;
        int count = data.getInt(IMMUNE_COUNT_TAG);
        if (count > 0 && event.getAmount() >= player.getMaxHealth()) {
            data.putInt(IMMUNE_COUNT_TAG, count - 1);
            event.setCanceled(true);
        }else if (event.getAmount() > player.getMaxHealth() * 0.5f) {
            event.setAmount(event.getAmount() * 0.5f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.contains(IMMUNE_COUNT_TAG)) return;
        int count = data.getInt(IMMUNE_COUNT_TAG);
        if (count > 0 && event.getAmount() >= player.getMaxHealth()) {
            event.setCanceled(true);
            data.putInt(IMMUNE_COUNT_TAG, count - 1);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(REVIVE_COUNT_TAG)) return;

        int reviveCount = persistentData.getInt(REVIVE_COUNT_TAG);
        if (reviveCount > 0) {
            event.setCanceled(true);
            persistentData.putInt(REVIVE_COUNT_TAG, reviveCount - 1);
            player.setHealth(player.getMaxHealth());
        }
    }

        @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.goetydelight.dimensional_jell.tooltip.1")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.dimensional_jell.tooltip.2")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}