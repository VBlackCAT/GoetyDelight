package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import com.mega.revelationfix.common.data.TimeStopSavedData;
import com.mega.revelationfix.common.init.ModAttributes;
import com.mega.revelationfix.common.item.curios.TheNeedleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class RoastNeedleItem extends Item {

    public RoastNeedleItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 32;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level,
                                              @NotNull LivingEntity living) {
        // 先走父类（处理堆叠减少、碗等）
        ItemStack result = super.finishUsingItem(stack, level, living);

        if (level instanceof ServerLevel serverLevel && living instanceof ServerPlayer player) {
            clearNeedles(serverLevel, player);

            DamageSource source = randomDamageSource(serverLevel, player);
            player.hurt(source, 5.0F);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 1.0F);
        }

        return result;
    }

    private static void clearNeedles(ServerLevel level, ServerPlayer player) {
        TimeStopSavedData data = TimeStopSavedData.readOrCreate(level.getServer());
        Map<java.util.UUID, TimeStopSavedData.SimplePlayerData> map = data.playerDataMap;

        if (map.containsKey(player.getUUID())) {
            map.get(player.getUUID()).setNeedleTimes(0);
        }

        if (player.getAttribute(Attributes.MAX_HEALTH) != null)
            player.getAttribute(Attributes.MAX_HEALTH).removeModifier(TheNeedleItem.HEALTH);
        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null)
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(TheNeedleItem.ATTACK_DAMAGE);
        if (player.getAttribute(Attributes.ATTACK_SPEED) != null)
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(TheNeedleItem.ATTACK_SPEED);
        if (ModAttributes.DAMAGE_RESISTANCE.get() != null && player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()) != null)
            player.getAttribute(ModAttributes.DAMAGE_RESISTANCE.get()).removeModifier(TheNeedleItem.RESISTANCE);

        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null)
            player.getAttributes().getSyncableAttributes().add(player.getAttribute(Attributes.ATTACK_DAMAGE));

        data.setDirty();

         if (player instanceof com.mega.revelationfix.safe.entity.PlayerInterface pi)
             pi.revelationfix$odamaneHaloExpandedContext().setBlasphemous(false);
    }


    private static DamageSource randomDamageSource(ServerLevel level, LivingEntity entity) {
        List<ResourceKey<DamageType>> keys = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .registryKeySet()
                .stream()
                .toList();

        RandomSource random = level.getRandom();
        ResourceKey<DamageType> key = keys.get(random.nextInt(keys.size()));

        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);

        return new DamageSource(holder, entity, entity);  // (type, directEntity, causingEntity)
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.goetydelight.roast_needle.tooltip.1")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.tooltip.shift"));
        }
        tooltip.add(Component.literal(""));
        // 经文（风味文本）
        tooltip.add(Component.translatable("item.goetydelight.roast_needle.tooltip.2")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.goetydelight.roast_needle.tooltip.3")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(ChatFormatting.ITALIC));
    }
}