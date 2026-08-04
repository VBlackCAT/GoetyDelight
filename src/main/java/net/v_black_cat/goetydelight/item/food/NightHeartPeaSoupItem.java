package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.TickTask;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import net.v_black_cat.goetydelight.ability.MinionBoost;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;
import vectorwing.farmersdelight.common.item.DrinkableItem;

import javax.annotation.Nullable;
import java.util.List;

public class NightHeartPeaSoupItem extends DrinkableItem {

    private static final int SOUP_BOOST_DURATION = -1;

    public NightHeartPeaSoupItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                serverLevel.setDayTime(18000 + 24000 * (serverLevel.getDayTime() / 24000 + 1));

                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        net.minecraft.sounds.SoundEvents.AMBIENT_CAVE.get(),
                        net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 0.8F);

                for (int i = 0; i < 20; i++) {
                    double x = entity.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                    double y = entity.getY() + level.random.nextDouble() * 2.0;
                    double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 2.0;

                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                            x, y, z, 1, 0, 0, 0, 0.1);
                }
            }

            if (entity instanceof Player player) {
                SpellStat spellStat = new SpellStat(0,0,16,2.0,0,0);
                for(int i = 0; i < 16; ++i) {
                    double d0 = MathHelper.rgbToSpeed(96.0);
                    double d1 = MathHelper.rgbToSpeed(62.0);
                    double d2 = MathHelper.rgbToSpeed(92.0);

                    serverLevel.sendParticles((SimpleParticleType)ModParticleTypes.CULT_SPELL.get(),
                            player.getRandomX(1.0), player.getRandomY(), player.getRandomZ(1.0),
                            0, d0, d1, d2, 0.5);
                }

                SEHelper.setEndWalk(player, player.blockPosition(), player.level().dimension());
                ModNetwork.sendTo(player, new SPlayPlayerSoundPacket((SoundEvent)ModSounds.END_WALK.get(), 1.0F, 1.0F));

                MinionBoost.increaseSoupBoostCount(player);
                MinionBoost.applyMinionBoosts(player);

                int currentAmplifier = BuffUtil.getBuffAmplifier(player, ModBuffTypes.MINION_BOOST.getId());
                BuffUtil.applyBuff(player, ModBuffTypes.MINION_BOOST.getId(), SOUP_BOOST_DURATION, currentAmplifier + 1);
            }

            // 融身入影：Goety 的 finishItemEvents 会在使用完非 IWand 物品时立即移除 SHADOW_WALK，
            // 因此把该效果延迟到使用完成事件之后再附加，保证持续满 60 秒
            // execute() 在服务端线程上会同步执行，必须在下一 tick 才真正附加
            MinecraftServer server = serverLevel.getServer();
            server.tell(new TickTask(server.getTickCount() + 1, () -> {
                if (entity.isAlive()) {
                    entity.addEffect(new MobEffectInstance(GoetyEffects.SHADOW_WALK.get(), 1200, 2));
                }
            }));
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // 融身入影改为延迟附加后不在食物属性里，这里手动补回 tooltip 说明
        tooltip.add(Component.translatable("tooltip.goetydelight.night_heart_pea_soup.shadow_walk",
                GoetyEffects.SHADOW_WALK.get().getDisplayName()));
    }
}