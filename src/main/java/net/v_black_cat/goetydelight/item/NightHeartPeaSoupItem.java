package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.effect.ModEffects;
import com.Polarice3.Goety.common.effects.GoetyEffects; // 导入Goety的效果类
import com.Polarice3.Goety.config.SpellConfig; // 导入配置类
import com.Polarice3.Goety.utils.MathHelper; // 导入数学工具类
import com.Polarice3.Goety.client.particles.ModParticleTypes; // 导入粒子效果
import com.Polarice3.Goety.common.network.ModNetwork; // 导入网络包
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket; // 导入声音包
import com.Polarice3.Goety.init.ModSounds; // 导入声音
import vectorwing.farmersdelight.common.item.DrinkableItem;

public class NightHeartPeaSoupItem extends DrinkableItem implements IWand {
    public NightHeartPeaSoupItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 设置时间为黑夜
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                serverLevel.setDayTime(18000);

                // 播放音效和粒子效果
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

            // 添加EndWalk效果（SHADOW_WALK）
            if (entity instanceof Player player) {

                SpellStat spellStat=new SpellStat(0,0,16,2.0,0,0);
                Player caster=player;
                    for(int i = 0; i < 16; ++i) {
                        double d0 = MathHelper.rgbToSpeed(96.0);
                        double d1 = MathHelper.rgbToSpeed(62.0);
                        double d2 = MathHelper.rgbToSpeed(92.0);

                        serverLevel.sendParticles((SimpleParticleType)ModParticleTypes.CULT_SPELL.get(), caster.getRandomX(1.0), caster.getRandomY(), caster.getRandomZ(1.0), 0, d0, d1, d2, 0.5);

                    }

                    SEHelper.setEndWalk(player, player.blockPosition(), player.level().dimension());
                    ModNetwork.sendTo(player, new SPlayPlayerSoundPacket((SoundEvent)ModSounds.END_WALK.get(), 1.0F, 1.0F));

            }
        }

        return result;
    }

    @Override
    public SpellType getSpellType() {
        return null;
    }
}