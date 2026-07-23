package net.v_black_cat.goetydelight.item.food;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.util.PayloadUtil;

import java.util.List;

import static com.Polarice3.Goety.utils.ModPotionUtil.setCustomEffects;
import static net.v_black_cat.goetydelight.util.TickConverterUtil.sToTick;


public class RejectedDarkMeatSoupItem extends Item {


    public RejectedDarkMeatSoupItem(Properties properties) {
        super(properties);
    }


    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 80;
    }

    /**
     * 使用动画
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }


    /**
     * 喝声音效
     */
    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }


    /**
     * 攻击实体时投掷黑暗肉汤
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {


        if (attacker instanceof Player player) {


            throwSoup(stack, player);


            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }


            return true;
        }


        return false;
    }


    /**
     * 投掷汤主体逻辑
     */
    private static void throwSoup(ItemStack stack, Player player) {


        Level level = player.level();


        if (level.isClientSide) {
            return;
        }




        /*
         * 不复制原ItemStack
         *
         * 防止:
         * - NBT污染
         * - 自定义数据复制
         * - Curios数据复制
         */
        ItemStack potionStack = new ItemStack(stack.getItem());


        setCustomEffects(potionStack, List.of(

                /*
                 * 迷乱
                 */
                new MobEffectInstance(MobEffects.CONFUSION, sToTick(30)),



                /*
                 * 随机等级毒
                 */
                new MobEffectInstance(MobEffects.POISON, sToTick(30), level.random.nextInt(5)),



                /*
                 * 虚弱
                 */
                new MobEffectInstance(MobEffects.WEAKNESS, sToTick(30), 1)

        ));


        ThrownPotion potion = new ThrownPotion(level, player);


        potion.setItem(potionStack);


        potion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);


        level.addFreshEntity(potion);


        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

    }


    /**
     * 给外部调用
     */
    public static void throwSoup(ItemStack stack, LivingEntity entity) {


        if (entity instanceof Player player) {


            throwSoup(stack, player);


            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

        }

    }

    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        var player = event.getEntity();
        var mainHandItem = player.getMainHandItem();
        if (mainHandItem.is(ModItems.REJECTED_DARK_MEAT_SOUP.get())) {
            PayloadUtil.sendClickAir(mainHandItem.getItem());
        }
    }


}