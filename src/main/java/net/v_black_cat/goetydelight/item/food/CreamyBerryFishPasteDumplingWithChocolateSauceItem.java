package net.v_black_cat.goetydelight.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.Polarice3.Goety.common.effects.GoetyEffects.SENSE_LOSS;

public class CreamyBerryFishPasteDumplingWithChocolateSauceItem extends Item {
    public CreamyBerryFishPasteDumplingWithChocolateSauceItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return (int) (32 * 4);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        if (!level.isClientSide) {
            // 检查目标是否为 vizier
            if (isVizier(target)) {
                applyVizierEffects(target);
                // 发送提示消息
                sendVizierMessage(player);
            } else {
                // 对其他生物应用负面效果
                applyNegativeEffects(target);
            }

            // 消耗物品
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private boolean isVizier(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null &&
                entityId.getNamespace().equals("goety") &&
                entityId.getPath().equals("vizier");
    }

    private void applyVizierEffects(LivingEntity target) {
        // 反胃1，1分钟
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 60, 0));
        // 力量3，2分钟
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 120, 2));
        // 生命提升5，10分钟
        target.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 600, 4));
    }

    private void applyNegativeEffects(LivingEntity target) {
        // 凋零效果
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 60, 4));
        // 反胃效果
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 120, 9));
        // 感官丧失效果（如果有这个自定义效果）
        target.addEffect(new MobEffectInstance(SENSE_LOSS.get(), 20 * 60, 0));
    }

    private void sendVizierMessage(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                Component.translatable("message.goetydelight.vizier_feed"),
                true
            );
        }
    }
}