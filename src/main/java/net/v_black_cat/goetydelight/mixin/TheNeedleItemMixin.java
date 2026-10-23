package net.v_black_cat.goetydelight.mixin;

import com.mega.revelationfix.common.data.TimeStopSavedData;
import com.mega.revelationfix.common.init.ModAttributes;
import com.mega.revelationfix.common.item.curios.TheNeedleItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.CrystalCandySavedData;
import net.v_black_cat.goetydelight.compat.goetyrevelation.util.NeedleAttributeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = TheNeedleItem.class, remap = false)
public class TheNeedleItemMixin {

    /** 扎针前快照：needleTimes + 四个属性是否已有针 modifier */
    @Unique
    private static final Map<UUID, int[]> goetydelight$timesBefore = new ConcurrentHashMap<>();
    /** 扎针前各属性是否已有针 modifier [hp, atk, spd, res]，1=有，0=无 */
    @Unique
    private static final Map<UUID, boolean[]> goetydelight$hadModBefore = new ConcurrentHashMap<>();

    @Inject(method = "releaseUsing", at = @At("HEAD"), remap = false)
    private void goetydelight$beforeNeedle(ItemStack stack, Level level, LivingEntity living, int left, CallbackInfo ci) {
        if (!(living instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();

        TimeStopSavedData tsd = TimeStopSavedData.readOrCreate(player.server);
        goetydelight$timesBefore.put(id, new int[]{ tsd.getPlayerNeedleTimes(player) });

        // 记录扎针前每个属性是否已有针的 modifier
        goetydelight$hadModBefore.put(id, new boolean[]{
                NeedleAttributeUtil.hasNeedleModifier(player, Attributes.MAX_HEALTH,                 NeedleAttributeUtil.NEEDLE_HP_UUID),
                NeedleAttributeUtil.hasNeedleModifier(player, Attributes.ATTACK_DAMAGE,              NeedleAttributeUtil.NEEDLE_ATK_UUID),
                NeedleAttributeUtil.hasNeedleModifier(player, Attributes.ATTACK_SPEED,               NeedleAttributeUtil.NEEDLE_SPD_UUID),
                NeedleAttributeUtil.hasNeedleModifier(player, ModAttributes.DAMAGE_RESISTANCE.get(), NeedleAttributeUtil.NEEDLE_RES_UUID)
        });
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"), remap = false)
    private void goetydelight$reapplyCandyAfterNeedle(ItemStack itemStack, Level level, LivingEntity living, int left, CallbackInfo ci) {
        if (!(living instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();

        int[] timesBefore = goetydelight$timesBefore.remove(id);
        boolean[] hadModBefore = goetydelight$hadModBefore.remove(id);
        if (timesBefore == null || hadModBefore == null) return;

        TimeStopSavedData tsd = TimeStopSavedData.readOrCreate(player.server);
        if (tsd.getPlayerNeedleTimes(player) == timesBefore[0]) return; // 未真正扎针

        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);
        if (data.isRefreshingSet.contains(id)) return;

        double[] applied = data.getCandyBonus(id);

        // 1. 只对"扎针前已有 modifier"的属性剥离糖
        //    扎针前没有 modifier 的属性，原版针新建的是纯增量（old == null 分支），不含糖，不能剥
        if (applied[0] != 0 || applied[1] != 0 || applied[2] != 0 || applied[3] != 0) {
            stripIfHadBefore(player, applied, hadModBefore);
            // 剥离后当前 modifier 已不含糖，清零 applied
            data.setCandyBonus(id, 0, 0, 0, 0);
        }

        // 2. 砍掉原版针超标的抗性
        NeedleAttributeUtil.capNeedleResistance(player);

        // 3. 重新应用糖（内部 hasXxxMod 判断 + 100% cap + 85% cap）
        NeedleAttributeUtil.addCandyBonus(player);
    }

    @Unique
    private static void stripIfHadBefore(ServerPlayer player, double[] applied, boolean[] hadModBefore) {
        if (hadModBefore[0]) NeedleAttributeUtil.stripOne(player, Attributes.MAX_HEALTH,                 NeedleAttributeUtil.NEEDLE_HP_UUID,  applied[0]);
        if (hadModBefore[1]) NeedleAttributeUtil.stripOne(player, Attributes.ATTACK_DAMAGE,              NeedleAttributeUtil.NEEDLE_ATK_UUID, applied[1]);
        if (hadModBefore[2]) NeedleAttributeUtil.stripOne(player, Attributes.ATTACK_SPEED,               NeedleAttributeUtil.NEEDLE_SPD_UUID, applied[2]);
        if (hadModBefore[3]) NeedleAttributeUtil.stripOne(player, ModAttributes.DAMAGE_RESISTANCE.get(), NeedleAttributeUtil.NEEDLE_RES_UUID, applied[3]);
    }
}