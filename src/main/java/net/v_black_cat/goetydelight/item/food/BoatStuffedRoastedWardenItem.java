package net.v_black_cat.goetydelight.item.food;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.v_black_cat.goetydelight.util.ParticlesUtil;

import java.util.function.Consumer;

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
    public int getUseDuration(ItemStack stack) {
        return 32; 
    }

@Override
public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {

    if (entity instanceof Player player) {
        ParticlesUtil.spawnItemParticles(stack, 20, entity);
        player.playSound(getEatingSound(), 0.5F, 1.0F);
    }

    // Apply food effects for each serving consumed


    ItemStack resultStack = stack.copy();
    int servings = getServings(resultStack);
    if (servings > 1) {
        super.finishUsingItem(stack, level, entity);
        setServings(resultStack, servings - 1);
        return resultStack;
    } else {
        return new ItemStack(Items.DARK_OAK_BOAT);
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

        int totalUseTime = this.getUseDuration(stack);
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
        CompoundTag tag = stack.getTagElement(SERVINGS_TAG);
        if (tag != null && tag.contains("Count")) {
            return tag.getInt("Count");
        }
        
        return 4;
    }

    
    public static void setServings(ItemStack stack, int servings) {
        if (servings <= 0) {
            stack.removeTagKey(SERVINGS_TAG);
            return;
        }
        
        CompoundTag tag = stack.getOrCreateTagElement(SERVINGS_TAG);
        tag.putInt("Count", servings);
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

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new BoatStuffedRoastedWardenClient());
    }

    public static class BoatStuffedRoastedWardenClient implements IClientItemExtensions {
        
        private static final HumanoidModel.ArmPose HOLDING_POSE = HumanoidModel.ArmPose.create("GOETYDELIGHT_HOLD", false, (model, entity, arm) -> {
            model.rightArm.xRot = Mth.DEG_TO_RAD * -90.0F;
            model.rightArm.yRot = Mth.DEG_TO_RAD * 0F;
            model.rightArm.zRot = Mth.DEG_TO_RAD * 10.0F;

            model.leftArm.xRot = Mth.DEG_TO_RAD * -90.0F;
            model.leftArm.yRot = Mth.DEG_TO_RAD * 0F;
            model.leftArm.zRot = Mth.DEG_TO_RAD * -10.0F;
        });

        

        private static final HumanoidModel.ArmPose USING_POSE = HumanoidModel.ArmPose.create("GOETYDELIGHT_USE", false, (model, entity, arm) -> {
            
            float useProgress = 0.0f;
            if (entity.getUseItemRemainingTicks() > 0) {
                useProgress = 1.0f - (float)entity.getUseItemRemainingTicks() / (float)entity.getUseItem().getUseDuration();
            }

            
            float rotateEffect = Mth.sin(useProgress * 50.0f);
                model.rightArm.xRot = Mth.DEG_TO_RAD * (-90f + rotateEffect*15f);
                model.rightArm.zRot = Mth.DEG_TO_RAD * (10f + rotateEffect*10f);
                
                model.leftArm.xRot = Mth.DEG_TO_RAD * (-90f + rotateEffect*15f);
                model.leftArm.zRot = Mth.DEG_TO_RAD * (-10f - rotateEffect*10f);

        });

        @Override
        public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
            ItemStack mainHandItem = entityLiving.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandItem = entityLiving.getItemInHand(InteractionHand.OFF_HAND);

            boolean isHoldingInMainHand = mainHandItem.getItem() instanceof BoatStuffedRoastedWardenItem;
            boolean isHoldingInOffHand = offHandItem.getItem() instanceof BoatStuffedRoastedWardenItem;

            
            boolean isUsing = entityLiving.getUseItem().getItem() instanceof BoatStuffedRoastedWardenItem;

            if (isUsing) {
                
                return USING_POSE;
            } else if (isHoldingInMainHand || isHoldingInOffHand) {
                
                return HOLDING_POSE;
            }

            return HumanoidModel.ArmPose.EMPTY;
        }

        @Override
        public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                               ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
            if (itemInHand.getItem() instanceof BoatStuffedRoastedWardenItem) {
                int i = arm == HumanoidArm.RIGHT ? 1 : -1;

                if (player.isUsingItem() && player.getUseItem() == itemInHand) {
                    float useProgress = (float)(player.getUseItemRemainingTicks() - partialTick) / itemInHand.getUseDuration();

                    float bounceEffect = Mth.sin(useProgress * 20.0f) * 0.1f;
                    float forwardMove = useProgress * 0.5f + bounceEffect;
                    float upMove = useProgress * 0.3f + Mth.cos(useProgress * 50.0f) * 0.05f;
                    float rotateEffect = Mth.sin(useProgress * 50.0f) * 5.0f;

                    poseStack.translate(i * 1f, -0.9f, -0.72f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(15f + rotateEffect));
                    poseStack.mulPose(Axis.YP.rotationDegrees(i * 0f + rotateEffect));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(i * 0f));

                    float scale = 1.0f + useProgress * 0.8f;
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
            return false;
        }
    }
}