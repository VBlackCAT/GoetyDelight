package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SauceGrilledCandyFishItem extends Item {

    public SauceGrilledCandyFishItem(Properties pProperties) {
        super(pProperties);
    }


    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        // 只在服务端处理
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        // 检查实体是否是猫
        if (entity instanceof Apostle cat ) {
            // 计算10%的最大生命值
            float maxHealth = cat.getMaxHealth();
            float healAmount = maxHealth * 0.15f;

            cat.setHealth(Math.min(cat.getHealth() + healAmount, maxHealth));

            cat.level().addParticle(ParticleTypes.HEART, cat.getX(), cat.getY() + 0.5, cat.getZ(), 0, 0, 0);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }else if (entity.getEncodeId().equals("revelationfix:apostle_servant")) {
            LivingEntity cat = (LivingEntity) entity;
            // 计算10%的最大生命值
            float maxHealth = cat.getMaxHealth();
            float healAmount = maxHealth * 0.15f;

            cat.setHealth(Math.min(cat.getHealth() + healAmount, maxHealth));

            cat.level().addParticle(ParticleTypes.HEART, cat.getX(), cat.getY() + 0.5, cat.getZ(), 0, 0, 0);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}