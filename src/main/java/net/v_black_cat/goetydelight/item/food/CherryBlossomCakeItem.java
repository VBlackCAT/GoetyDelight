package net.v_black_cat.goetydelight.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.item.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class CherryBlossomCakeItem extends Item {
    // UUID用于攻击力修饰符
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("15f2e8d8-2ff0-4915-8039-a6807c993b51");
    // 用于标识临时攻击力加成的名称
    private static final String ATTACK_DAMAGE_NAME = "Cherry Blossom Cake Attack Boost";

    public CherryBlossomCakeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {
            // 移除任何现有的樱桃蛋糕攻击力加成
            removeAttackDamageBoost(entity);

            // 获取实体的幸运值（如果有）
            AttributeInstance luckAttribute = entity.getAttribute(Attributes.LUCK);
            double luckValue = luckAttribute != null ? luckAttribute.getValue() : 0;

            // 每点幸运值增加0.5点攻击力
            double attackBoost = luckValue * 0.5;

            // 添加攻击力加成
            addAttackDamageBoost(entity, attackBoost);
        }

        return resultStack;
    }

    private void addAttackDamageBoost(LivingEntity entity, double boostAmount) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && boostAmount > 0) {
            AttributeModifier modifier = new AttributeModifier(
                    ATTACK_DAMAGE_UUID,
                    ATTACK_DAMAGE_NAME,
                    boostAmount,
                    AttributeModifier.Operation.ADDITION
            );
            attackDamage.addTransientModifier(modifier);
        }
    }

    private void removeAttackDamageBoost(LivingEntity entity) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_UUID) != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_UUID);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.goetydelight.cherry_blossom_cake"));
    }

    @Mod.EventBusSubscriber
    public static class CherryBlossomCakeEventHandler {
        @SubscribeEvent
        public static void onPlayerAttack(LivingHurtEvent event) {
            // 检查攻击者是否有樱桃蛋糕的攻击力加成
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                AttributeInstance attackDamage = attacker.getAttribute(Attributes.ATTACK_DAMAGE);
                if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_UUID) != null) {
                    // 移除加成（仅对一次攻击生效）
                    attackDamage.removeModifier(ATTACK_DAMAGE_UUID);
                }
            }
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            ItemStack stack = event.getItemStack();
            Level level = player.level();

            // 检查手持物品是否为樱花簇（PINK_PETALS）
            if (stack.getItem() == net.minecraft.world.item.Items.PINK_PETALS &&
                    event.getTarget() instanceof Fox fox) {

                // 仅在服务端处理
                if (!level.isClientSide()) {
                    // 消耗手中的粉红色花簇
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    ItemStack cherryBlossomCake = new ItemStack(ModItems.CHERRY_BLOSSOM_CAKE.get());
                    fox.spawnAtLocation(cherryBlossomCake);

                    // 播放粒子效果
                    for (int i = 0; i < 7; ++i) {
                        double d0 = level.random.nextGaussian() * 0.02D;
                        double d1 = level.random.nextGaussian() * 0.02D;
                        double d2 = level.random.nextGaussian() * 0.02D;
                        level.addParticle(ParticleTypes.HEART,
                                fox.getX() + level.random.nextFloat() * fox.getBbWidth() * 2.0F - fox.getBbWidth(),
                                fox.getY() + 0.5D + level.random.nextFloat() * fox.getBbHeight(),
                                fox.getZ() + level.random.nextFloat() * fox.getBbWidth() * 2.0F - fox.getBbWidth(),
                                d0, d1, d2);
                    }

                    // 播放声音
                    level.playSound(null, fox.getX(), fox.getY(), fox.getZ(),
                            SoundEvents.FOX_EAT, fox.getSoundSource(), 1.0F, 1.0F);
                }

                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                event.setCanceled(true);
            }
        }
    }
}