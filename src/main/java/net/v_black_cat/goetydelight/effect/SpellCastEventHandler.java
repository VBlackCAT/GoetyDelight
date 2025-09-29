package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.events.spell.CastMagicEvent;
import com.Polarice3.Goety.common.magic.SummonSpell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpellCastEventHandler {

    @SubscribeEvent
    public static void onCastMagic(CastMagicEvent event) {
        LivingEntity caster = event.getEntity();
        ISpell spell = event.getSpell();

        // 1. 检查施法者是否拥有 NIGHT_HEART_PEA_SOUP 效果
        MobEffectInstance effectInstance = caster.getEffect(ModEffects.SERVANT_REINFORCEMENT.get()); // 请根据实际注册类调整
        if (effectInstance != null) {
            if (spell instanceof SummonSpell summonSpell) {

               summonSpell.SpellResult((ServerLevel) caster.level(),caster,event.getEntity().getMainHandItem(), event.getSpell().defaultStats());
                caster.getEffect((MobEffect) GoetyEffects.SUMMON_DOWN.get());

            }


        }

        // 2. 获取效果信息（例如：放大器等级、剩余持续时间等）
        int amplifier = effectInstance.getAmplifier(); // 效果等级
        int duration = effectInstance.getDuration(); // 剩余时间（刻）

        // 3. 执行“复读”逻辑
        // 此处是核心操作区域，以下是几个可能的“复读”方向：

        // 示例1: 记录法术信息（基础版“复读”）
//        System.out.println("[法术复读] 施法者: " + caster.getName().getString() +
//                ", 法术: " + spell.getClass().getSimpleName() +
//                ", 夜心豌豆汤效果等级: " + amplifier +
//                ", 效果剩余时间: " + duration + " ticks");

        // 示例2: 增强当前法术（例如，基于效果等级增加威力）
        // 如果spell对象有设置威力的方法，可以这样操作：
        // if (spell instanceof YourCustomSpellClass yourSpell) {
        //     int bonusPotency = amplifier + 1; // 例如，每级效果等级增加1点威力
        //     yourSpell.setPotency(yourSpell.getPotency() + bonusPotency);
        //     System.out.println("法术威力因夜心豌豆汤效果提升至: " + yourSpell.getPotency());
        // }

        // 示例3: 模拟再次施法（高级“复读”，例如有概率额外释放一次）
        // if (!event.isCancelable() && !event.isCanceled()) { // 注意检查事件是否可取消以及是否已被取消
        //     if (caster.level().random.nextDouble() < 0.5) { // 50%概率触发
        //         // 注意：直接再次触发CastMagicEvent可能导致递归事件，需谨慎处理并添加防止无限循环的逻辑
        //         // 一种更安全的做法是直接调用法术的某个执行方法，而非通过事件系统
        //         System.out.println("夜心豌豆汤效果触发了法术复读!");
        //     }
        // }

        // 示例4: 取消原法术并替换为自定义效果（例如，改变法术行为）
        // if (spell.isSomeSpecificSpell()) {
        //     event.setCanceled(true); // 取消原法术施放
        //     // 然后执行自定义逻辑，例如施放一个不同的法术
        //     performCustomSpellEffect(caster);
        // }

        // 您可以根据需要选择、组合或修改上述示例，也可以在此处添加任何其他自定义逻辑。
    }

    // 如果选择示例4的复杂逻辑，可能需要一个辅助方法
    // private static void performCustomSpellEffect(LivingEntity caster) {
    //     // 实现您的自定义法术效果
    // }
}