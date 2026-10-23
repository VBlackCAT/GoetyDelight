package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.ConsumedCurio;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.CrystalCandySavedData;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.NeedleAttributeUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class CrystalCandyItem extends Item {

    public static final String STORED = "StoredCurios";
    public static final int MAX_STORAGE = 64;

    public CrystalCandyItem(Properties properties) {
        super(properties);
    }

    public static boolean canStore(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof top.theillusivec4.curios.api.type.capability.ICurioItem)) return false;
        Rarity r = stack.getRarity();
        return r == Rarity.COMMON || r == Rarity.UNCOMMON || r == Rarity.RARE || r == Rarity.EPIC;
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack candy, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess carriedItem) {
        if (action != ClickAction.SECONDARY || candy.getCount() != 1) return false;

        CompoundTag tag = candy.getOrCreateTag();
        ListTag stored = tag.getList(STORED, 10);
        if (stored.size() >= MAX_STORAGE) return false;

        if (canStore(other)) {
            CompoundTag s = new CompoundTag();
            other.save(s);
            stored.add(s);
            tag.put(STORED, stored);

            other.shrink(1);
            carriedItem.set(other.isEmpty() ? ItemStack.EMPTY : other);

            player.playNotifySound(SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
            slot.setChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack candy, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (action != ClickAction.SECONDARY || candy.getCount() != 1) return false;

        CompoundTag tag = candy.getOrCreateTag();
        ListTag stored = tag.getList(STORED, 10);
        ItemStack other = slot.getItem();

        if (!other.isEmpty()) {
            if (stored.size() >= MAX_STORAGE) return false;
            if (canStore(other)) {
                CompoundTag s = new CompoundTag();
                other.save(s);
                stored.add(s);
                tag.put(STORED, stored);
                other.shrink(1);
                if (other.isEmpty()) slot.set(ItemStack.EMPTY);
                player.playNotifySound(SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
                slot.setChanged();
                return true;
            }
        } else {
            if (stored.isEmpty()) return false;
            CompoundTag s = stored.getCompound(stored.size() - 1);
            ItemStack out = ItemStack.of(s);
            stored.remove(stored.size() - 1);
            tag.put(STORED, stored);
            slot.set(out);
            player.playNotifySound(SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 1.0F, 1.0F);
            slot.setChanged();
            return true;
        }
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack candy = player.getItemInHand(hand);
        CompoundTag tag = candy.getOrCreateTag();
        ListTag stored = tag.getList(STORED, 10);

        if (stored.isEmpty()) return InteractionResultHolder.pass(candy);
        if (level.isClientSide) return InteractionResultHolder.consume(candy);

        if (player.isShiftKeyDown()) {
            CompoundTag s = stored.getCompound(stored.size() - 1);
            ItemStack out = ItemStack.of(s);
            stored.remove(stored.size() - 1);
            tag.put(STORED, stored);
            if (!player.getInventory().add(out)) player.drop(out, false);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResultHolder.sidedSuccess(candy, false);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(candy);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 20;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) return stack;

        CompoundTag tag = stack.getOrCreateTag();
        ListTag stored = tag.getList(STORED, 10);
        if (stored.isEmpty()) return stack;

        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);

        ListTag validStored = new ListTag();
        ListTag duplicateStored = new ListTag();

        for (int i = 0; i < stored.size(); i++) {
            CompoundTag itemTag = stored.getCompound(i);
            ItemStack s = ItemStack.of(itemTag);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(s.getItem());
            String targetId = id != null ? id.toString() : "null";

            boolean isDuplicate = data.hasConsumedItem(player.getUUID(), targetId);
            if (!isDuplicate) {
                for (int j = 0; j < validStored.size(); j++) {
                    ItemStack v = ItemStack.of(validStored.getCompound(j));
                    ResourceLocation vid = ForgeRegistries.ITEMS.getKey(v.getItem());
                    if (vid != null && vid.toString().equals(targetId)) {
                        isDuplicate = true;
                        break;
                    }
                }
            }

            if (isDuplicate) {
                duplicateStored.add(itemTag);
            } else {
                data.addConsumed(player.getUUID(), ConsumedCurio.fromStack(s));
                validStored.add(itemTag);
            }
        }

        if (!validStored.isEmpty()) {
            applyCrystalCandyBonuses(player, validStored);
        }

        if (!duplicateStored.isEmpty()) {
            tag.put(STORED, duplicateStored);
        } else {
            tag.remove(STORED);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getCooldowns().addCooldown(this, 20);

        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();

        return stack;
    }

    /**
     * 饰品已经通过 addConsumed 写入 consumedMap。
     * 糖总量由 computeCandyFromConsumed 现算，这里只需触发一次 addCandyBonus。
     */
    private static void applyCrystalCandyBonuses(ServerPlayer player, ListTag stored) {
        NeedleAttributeUtil.addCandyBonus(player);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag stored = tag.getList(STORED, 10);
        if (stored.isEmpty()) {
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.empty")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.contains", stored.size())
                    .withStyle(ChatFormatting.GOLD));
            for (int i = 0; i < stored.size(); i++) {
                ItemStack s = ItemStack.of(stored.getCompound(i));
                tooltip.add(Component.literal(" - ").append(s.getHoverName())
                        .withStyle(s.getRarity().color));
            }
        }
        tooltip.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.tooltip.1")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.tooltip.2")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.tooltip.3")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.tooltip.4")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.tooltip.shift"));
        }
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.goetydelight.crystal_candy.tooltip.5")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.goetydelight.crystal_candy.tooltip.6")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(ChatFormatting.ITALIC));
    }

    public static class CrystalCandyEvents {
        @SubscribeEvent
        public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                NeedleAttributeUtil.addCandyBonus(player);
            }
        }
    }
}