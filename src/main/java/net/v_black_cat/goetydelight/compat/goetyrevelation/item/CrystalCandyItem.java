package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.revelationfix.common.init.ModAttributes;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.ConsumedCurio;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.CrystalCandySavedData;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.NeedleAttributeUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

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
            // 不再做任何查重拦截，随便塞
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
                // 不再做任何查重拦截，随便塞
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

        // 用于结算属性的合法物品列表
        ListTag validStored = new ListTag();
        // 用于塞回糖里的重复物品列表
        ListTag duplicateStored = new ListTag();

        for (int i = 0; i < stored.size(); i++) {
            CompoundTag itemTag = stored.getCompound(i);
            ItemStack s = ItemStack.of(itemTag);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(s.getItem());
            String targetId = id != null ? id.toString() : "null";

            // 如果已经吃过，或者在这一颗糖里重复了，就放入保留区
            boolean isDuplicate = data.hasConsumedItem(player.getUUID(), targetId);
            if (!isDuplicate) {
                for(int j=0; j<validStored.size(); j++) {
                    ItemStack v = ItemStack.of(validStored.getCompound(j));
                    ResourceLocation vid = ForgeRegistries.ITEMS.getKey(v.getItem());
                    if (vid != null && vid.toString().equals(targetId)) {
                        isDuplicate = true;
                        break;
                    }
                }
            }

            if (isDuplicate) {
                duplicateStored.add(itemTag); // 保留重复的，放回糖里
            } else {
                data.addConsumed(player.getUUID(), ConsumedCurio.fromStack(s));
                validStored.add(itemTag); // 合法的，去结算
            }
        }

        // 结算合法的物品
        if (!validStored.isEmpty()) {
            applyCrystalCandyBonuses(player, validStored);
        }

        // 如果还有剩下的重复物品，就重新写回 NBT
        if (!duplicateStored.isEmpty()) {
            tag.put(STORED, duplicateStored);
            player.displayClientMessage(Component.literal("部分饰品已吃过，保留在水晶糖中未消耗").withStyle(ChatFormatting.GRAY), true);
        } else {
            tag.remove(STORED); // 全都合法吃完了，清空糖
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getCooldowns().addCooldown(this, 20);

        player.getInventory().setChanged();
        if (player.containerMenu != null) player.containerMenu.broadcastChanges();

        return stack;
    }

    private static void applyCrystalCandyBonuses(ServerPlayer player, ListTag stored) {
        double hp = 0, atk = 0, spd = 0, res = 0;

        for (int i = 0; i < stored.size(); i++) {
            ItemStack s = ItemStack.of(stored.getCompound(i));
            double hpAdd, atkAdd, spdAdd, resAdd;
            switch (s.getRarity()) {
                case COMMON   -> { hpAdd = 0.01; atkAdd = 0.01; spdAdd = 0;    resAdd = 0; }
                case UNCOMMON -> { hpAdd = 0.02; atkAdd = 0.02; spdAdd = 0.02; resAdd = 0; }
                case RARE     -> { hpAdd = 0.04; atkAdd = 0.04; spdAdd = 0.04; resAdd = 0.04; }
                case EPIC     -> { hpAdd = 0.24; atkAdd = 0.24; spdAdd = 0.24; resAdd = 0.24; }
                default       -> { hpAdd = 0;    atkAdd = 0;    spdAdd = 0;    resAdd = 0; }
            }
            hp += hpAdd; atk += atkAdd; spd += spdAdd; res += resAdd;
        }

        AttributeInstance hpInst = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance atkInst = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance spdInst = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance resInst = player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get());

        double oldCandyHp = NeedleAttributeUtil.amountOf(hpInst, NeedleAttributeUtil.CANDY_HP_UUID);
        double oldCandyAtk = NeedleAttributeUtil.amountOf(atkInst, NeedleAttributeUtil.CANDY_ATK_UUID);
        double oldCandySpd = NeedleAttributeUtil.amountOf(spdInst, NeedleAttributeUtil.CANDY_SPD_UUID);
        double oldCandyRes = NeedleAttributeUtil.amountOf(resInst, NeedleAttributeUtil.CANDY_RES_UUID);

        double currentRes = NeedleAttributeUtil.getTotalResistance(player);
        if (currentRes + res > NeedleAttributeUtil.RES_CAP) {
            double allowed = Math.max(0, NeedleAttributeUtil.RES_CAP - currentRes);
            double overflow = res - allowed;
            res = allowed;
            hp += overflow / 3.0;
            atk += overflow / 3.0;
            spd += overflow / 3.0;
        }

        NeedleAttributeUtil.applyModifier(player, Attributes.MAX_HEALTH, NeedleAttributeUtil.CANDY_HP_UUID, "Crystal Candy", oldCandyHp + hp);
        NeedleAttributeUtil.applyModifier(player, Attributes.ATTACK_DAMAGE, NeedleAttributeUtil.CANDY_ATK_UUID, "Crystal Candy", oldCandyAtk + atk);
        NeedleAttributeUtil.applyModifier(player, Attributes.ATTACK_SPEED, NeedleAttributeUtil.CANDY_SPD_UUID, "Crystal Candy", oldCandySpd + spd);
        NeedleAttributeUtil.applyModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NeedleAttributeUtil.CANDY_RES_UUID, "Crystal Candy", oldCandyRes + res);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag stored = tag.getList(STORED, 10);
        if (stored.isEmpty()) {
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.empty").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.how_to_use").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.contains", stored.size()).withStyle(ChatFormatting.GOLD));
            for (int i = 0; i < stored.size(); i++) {
                ItemStack s = ItemStack.of(stored.getCompound(i));
                tooltip.add(Component.literal(" - ").append(s.getHoverName()).withStyle(s.getRarity().color));
            }
            tooltip.add(Component.translatable("item.goetydelight.crystal_candy.eat_hint").withStyle(ChatFormatting.GRAY));
        }
    }
}