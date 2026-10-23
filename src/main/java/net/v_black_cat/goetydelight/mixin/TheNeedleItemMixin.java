package net.v_black_cat.goetydelight.mixin;

import com.mega.revelationfix.common.data.TimeStopSavedData;
import com.mega.revelationfix.common.item.curios.TheNeedleItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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

    @Unique
    private static final Map<UUID, Integer> goetydelight$timesBefore = new ConcurrentHashMap<>();

    @Inject(method = "releaseUsing", at = @At("HEAD"), remap = false)
    private void goetydelight$beforeNeedle(ItemStack stack, Level level, LivingEntity living, int left, CallbackInfo ci) {
        if (!(living instanceof ServerPlayer player)) return;
        TimeStopSavedData tsd = TimeStopSavedData.readOrCreate(player.server);
        goetydelight$timesBefore.put(player.getUUID(), tsd.getPlayerNeedleTimes(player));
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"), remap = false)
    private void goetydelight$reapplyCandyAfterNeedle(ItemStack itemStack, Level level, LivingEntity living, int left, CallbackInfo ci) {
        if (!(living instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();

        Integer before = goetydelight$timesBefore.remove(id);
        if (before == null) return;

        TimeStopSavedData tsd = TimeStopSavedData.readOrCreate(player.server);
        if (tsd.getPlayerNeedleTimes(player) == before) return; // 未真正扎针

        CrystalCandySavedData data = CrystalCandySavedData.readOrCreate(player.server);
        if (data.isRefreshingSet.contains(id)) return;

        // 1. 剥离糖（用 bonusMap 的"上次写入量"），得到针裸值 + 本次随机增量
        double[] applied = data.getCandyBonus(id);
        if (applied[0] != 0 || applied[1] != 0 || applied[2] != 0 || applied[3] != 0) {
            NeedleAttributeUtil.stripApplied(player, applied);
            data.setCandyBonus(id, 0, 0, 0, 0); // 当前 Modifier 已无糖
        }

        // 2. 砍掉原版针超标的抗性（此时 NEEDLE_RES_UUID = 针裸值 + 本次随机增量）
        NeedleAttributeUtil.capNeedleResistance(player);

        // 3. 重新应用糖（内部现算 + min(100%) + 85% cap + 覆盖写入 + 回写 applied）
        NeedleAttributeUtil.addCandyBonus(player);
    }
}