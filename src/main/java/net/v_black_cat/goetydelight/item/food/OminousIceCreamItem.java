package net.v_black_cat.goetydelight.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import net.v_black_cat.goetydelight.util.EntityTagChecker;

import javax.annotation.Nullable;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class OminousIceCreamItem extends Item {

    public static final String OMINOUS_ACTIVE_TAG = "OminousIceCreamActive";
    public static final String HAS_CONSUMED_TAG = "HasConsumedOminousIceCream";

    // 存储已修改AI的生物，避免重复修改
    private static final WeakHashMap<Mob, Boolean> modifiedMobs = new WeakHashMap<>();

    public OminousIceCreamItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            if (player.hasEffect(MobEffects.BAD_OMEN)) {
                player.getPersistentData().putBoolean(OMINOUS_ACTIVE_TAG, true);
                player.getPersistentData().putBoolean(HAS_CONSUMED_TAG, true);

                //player.displayClientMessage(Component.literal("不详之兆的力量被冰激凌激活！"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.EVOKER_PREPARE_SUMMON,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                //player.displayClientMessage(Component.literal("需要不详之兆效果才能激活冰激凌的力量！"), true);
            }
        }

        return resultStack;
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || event.getLevel().isClientSide()) {
            return;
        }

        // 只处理袭击者生物，并且不是Boss
        if (!EntityTagChecker.isEntityInTag(mob, "minecraft:raiders") ||
                mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return;
        }

        if (modifiedMobs.containsKey(mob)) {
            return;
        }


        // 添加新的AI目标：只攻击没有激活状态的玩家
        mob.targetSelector.addGoal(0, new ConditionalPlayerTargetGoal(mob));

        modifiedMobs.put(mob, true);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();
        if (effect == null) return;

        if (entity instanceof Player player && effect.getEffect() == MobEffects.BAD_OMEN) {
            if (player.getPersistentData().getBoolean(OMINOUS_ACTIVE_TAG)) {
                player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
                //player.displayClientMessage(Component.literal("不详之兆的力量消散了！"), true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpire(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();
        if (effect == null) return;

        if (entity instanceof Player player && effect.getEffect() == MobEffects.BAD_OMEN) {
            if (player.getPersistentData().getBoolean(OMINOUS_ACTIVE_TAG)) {
                player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
                //player.displayClientMessage(Component.literal("不详之兆的力量自然消散了！"), true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
            player.getPersistentData().remove(HAS_CONSUMED_TAG);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getPersistentData().remove(OMINOUS_ACTIVE_TAG);
        player.getPersistentData().remove(HAS_CONSUMED_TAG);
    }

    public static class ConditionalPlayerTargetGoal extends NearestAttackableTargetGoal<Player> {
        public ConditionalPlayerTargetGoal(Mob mob) {
            super(mob, Player.class, true);
            this.targetConditions = TargetingConditions.forCombat()
                    .range(this.getFollowDistance())
                    .selector(player -> {
                        // 直接过滤掉有激活状态的玩家
                        return !player.getPersistentData().getBoolean(OminousIceCreamItem.OMINOUS_ACTIVE_TAG);
                    });
        }

        @Override
        public boolean canUse() {
            // 反击逻辑：如果被有激活状态的玩家攻击，选择该玩家为目标
            LivingEntity lastAttacker = this.mob.getLastHurtByMob();
            if (lastAttacker instanceof Player) {
                Player player = (Player) lastAttacker;
                if (player.getPersistentData().getBoolean(OminousIceCreamItem.OMINOUS_ACTIVE_TAG)) {
                    // 检查是否在最近时间内被攻击（避免旧仇恨）
                    if (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp() < 100) { // 5秒内
                        this.target = player;
                        return true;
                    }
                }
            }

            // 已有目标时不再重新搜索
            if (this.mob.getTarget() != null) {
                // 检查当前目标是否有效
                LivingEntity currentTarget = this.mob.getTarget();
                if (currentTarget instanceof Player) {
                    Player player = (Player) currentTarget;
                    // 如果当前目标是无效玩家，清除目标
                    if (player.getPersistentData().getBoolean(OminousIceCreamItem.OMINOUS_ACTIVE_TAG)) {
                        this.mob.setTarget(null);
                    }
                }
                return false;
            }

            // 只在无目标时执行搜索
            this.findTarget();
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.target == null) return false;

            // 如果是反击目标，允许继续攻击一段时间
            if (this.target == this.mob.getLastHurtByMob()) {
                // 允许继续攻击直到仇恨时间结束
                return this.mob.tickCount - this.mob.getLastHurtByMobTimestamp() < 100;
            }

            // 持续验证目标有效性
            return !this.target.isRemoved() &&
                    !this.target.getPersistentData().getBoolean(OminousIceCreamItem.OMINOUS_ACTIVE_TAG) &&
                    super.canContinueToUse(); // 父类验证（如视线检查）
        }

        // 全局拦截无效目标设置
        @Override
        public void setTarget(@Nullable LivingEntity target) {
            if (target instanceof Player) {
                Player player = (Player) target;
                // 阻止设置无效玩家为目标（除非是反击）
                if (player.getPersistentData().getBoolean(OminousIceCreamItem.OMINOUS_ACTIVE_TAG)) {
                    // 只允许在反击情况下设置目标
                    if (this.mob.getLastHurtByMob() != player) {
                        return; // 阻止设置
                    }
                }
            }
            super.setTarget(target);
        }
    }
}