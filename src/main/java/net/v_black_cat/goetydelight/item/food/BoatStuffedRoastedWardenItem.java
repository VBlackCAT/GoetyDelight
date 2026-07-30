package net.v_black_cat.goetydelight.item.food;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.util.ParticlesUtil;

public class BoatStuffedRoastedWardenItem extends Item {

    public static final String SERVINGS_TAG = "Servings";

    public BoatStuffedRoastedWardenItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return super.canContinueUsing(oldStack, newStack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            FoodProperties foodProperties = stack.getFoodProperties(livingEntity);
            if (foodProperties != null) {
                player.getFoodData().eat(foodProperties.nutrition(), foodProperties.saturation());
                foodProperties.effects().forEach(pair -> {
                    if (player.getRandom().nextFloat() < pair.probability()) {
                        player.addEffect(new MobEffectInstance(pair.effect()));
                    }
                });
            }
            ParticlesUtil.spawnItemParticles(stack, 20, livingEntity);
            player.playSound(this.getEatingSound(), 0.5F, 1.0F);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        int servings = getServings(stack);
        if (servings > 1) {
            // 直接设置 DAMAGE 组件
            stack.set(DataComponents.DAMAGE, 4 - (servings - 1));
            return stack;
        } else {
            return new ItemStack(ModItems.BOAT_PLATE.get());
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        if (getServings(stack) <= 0) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        super.onUseTick(level, entity, stack, remainingUseTicks);

        int totalUseTime = this.getUseDuration(stack, entity);
        int usedTicks = totalUseTime - remainingUseTicks;

        if (usedTicks % 5 == 0) {
            ParticlesUtil.spawnItemParticles(stack, 20, entity);
            if (entity instanceof Player player) {
                float pitch = 1.0F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F;
                player.playSound(getEatingSound(), 0.5F, pitch);
            }
        }
    }

    public static int getServings(ItemStack stack) {
        int damage = stack.getDamageValue();
        if (damage < 0) damage = 0;
        if (damage > 4) damage = 4;
        return 4 - damage;
    }

    public static void setServings(ItemStack stack, int servings) {
        if (servings < 0) servings = 0;
        if (servings > 4) servings = 4;
        stack.setDamageValue(4 - servings);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getServings(stack) < 4;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int servings = getServings(stack);
        return Math.round((float) servings / 4.0F * 13.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00;
    }



    public static class ClientExtensions implements IClientItemExtensions {
        @Override
        public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                               ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
            if (!(itemInHand.getItem() instanceof BoatStuffedRoastedWardenItem)) return false;

            int i = arm == HumanoidArm.RIGHT ? 1 : -1;

            if (player.isUsingItem() && player.getUseItem() == itemInHand) {
                int total = itemInHand.getUseDuration(player);
                int remaining = player.getUseItemRemainingTicks();
                float useProgress = (float) (remaining - partialTick) / total;

                float bounceEffect = net.minecraft.util.Mth.sin(useProgress * 20.0f) * 0.1f;
                float forwardMove = useProgress * 0.5f + bounceEffect;
                float upMove = useProgress * 0.3f + net.minecraft.util.Mth.cos(useProgress * 50.0f) * 0.05f;
                float rotateEffect = net.minecraft.util.Mth.sin(useProgress * 50.0f) * 5.0f;

                poseStack.translate(i * 1f, -0.9f, -0.72f);
                poseStack.mulPose(Axis.XP.rotationDegrees(15f + rotateEffect));
                poseStack.mulPose(Axis.YP.rotationDegrees(i * 0f + rotateEffect));
                poseStack.mulPose(Axis.ZP.rotationDegrees(i * 0f));
                poseStack.scale(1.8f, 1.8f, 1.8f);
            } else {
                poseStack.translate(i * 1f, -0.9f, -0.72f);
                poseStack.mulPose(Axis.XP.rotationDegrees(0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(i * 0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(i * 0f));
                poseStack.scale(1.8f, 1.8f, 1.8f);
            }
            return true;
        }
    }

    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                new BoatStuffedRoastedWardenItem.ClientExtensions(),
                ModItems.BOAT_STUFFED_ROASTED_WARDEN_BODY.get()
        );
        event.registerItem(
                new BoatStuffedRoastedWardenItem.ClientExtensions(),
                ModItems.BOAT_STUFFED_ROASTED_WARDEN_HEAD.get()
        );
        event.registerItem(
                new BoatStuffedRoastedWardenItem.ClientExtensions(),
                ModItems.BOAT_STUFFED_ROASTED_WARDEN_HAND.get()
        );
        event.registerItem(
                new BoatStuffedRoastedWardenItem.ClientExtensions(),
                ModItems.BOAT_STUFFED_ROASTED_WARDEN_LEG.get()
        );
        event.registerItem(
                new BoatStuffedRoastedWardenItem.ClientExtensions(),
                ModItems.BOAT_STUFFED_ROASTED_WARDEN_SOUP.get()
        );
    }
}