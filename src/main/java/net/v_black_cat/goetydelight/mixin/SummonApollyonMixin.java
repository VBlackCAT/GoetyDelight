package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.util.SummonApostle;
import com.Polarice3.Goety.common.ritual.SummonRitual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.v_black_cat.goetydelight.compat.goetyrevelation.item.AtonementVoucherWrapedCodItem;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.DoomCookieItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import z1gned.goetyrevelation.entitiy.SummonApollyon;

@Mixin(value = SummonRitual.class, priority = 900, remap = false)
public abstract class SummonApollyonMixin {

    @Unique
    private Player goetyDelight$castingPlayer;

    @Unique
    private Level goetyDelight$level;

    @Unique
    private BlockPos goetyDelight$blockPos;

    @Inject(
            method = "finish",
            at = @At("HEAD"),
            remap = false
    )
    private void goetyDelight$captureRitualData(
            Level world,
            BlockPos blockPos,
            DarkAltarBlockEntity tileEntity,
            Player castingPlayer,
            ItemStack activationItem,
            CallbackInfo ci
    ) {
        this.goetyDelight$castingPlayer = castingPlayer;
        this.goetyDelight$level = world;
        this.goetyDelight$blockPos = blockPos;
    }

    @ModifyArg(
            method = "finish",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lcom/Polarice3/Goety/common/ritual/SummonRitual;"
                                    + "spawnEntity("
                                    + "Lnet/minecraft/world/entity/player/Player;"
                                    + "Lnet/minecraft/world/entity/Entity;"
                                    + "Lnet/minecraft/world/level/Level;"
                                    + ")V",
                    ordinal = 1,
                    remap = false
            ),
            index = 1,
            remap = false
    )
    private Entity goetyDelight$modifySummonResult(Entity entity) {
        Player player = this.goetyDelight$castingPlayer;
        Level level = this.goetyDelight$level;
        BlockPos pos = this.goetyDelight$blockPos;

        if (player == null || !(level instanceof ServerLevel) || pos == null) {
            return entity;
        }

        // 赎罪券鱼：取消本次 Apollyon 替换。
        if (entity instanceof SummonApollyon) {
            int count = AtonementVoucherWrapedCodItem.getUsageCount(player);

            if (count > 0) {
                Entity apostle = goetyDelight$createApostle(level, pos);

                if (apostle != null) {
                    AtonementVoucherWrapedCodItem.setUsageCount(player, count - 1);
                    return apostle;
                }
            }

            return entity;
        }

        // Doom Cookie：将普通 SummonApostle 强制替换为 Apollyon。
        if (entity instanceof SummonApostle) {
            int count = DoomCookieItem.getUsageCount(player);

            if (count > 0) {
                Entity apollyon = goetyDelight$createApollyon(level, pos);

                if (apollyon != null) {
                    DoomCookieItem.setUsageCount(player, count - 1);
                    return apollyon;
                }
            }
        }

        return entity;
    }

    @Unique
    private static Entity goetyDelight$createApostle(Level level, BlockPos pos) {
        Entity entity = ModEntityType.SUMMON_APOSTLE.get().create(level);

        if (entity != null) {
            goetyDelight$setPosition(entity, level, pos);
        }

        return entity;
    }

    @Unique
    private static Entity goetyDelight$createApollyon(Level level, BlockPos pos) {
        EntityType<?> type = z1gned.goetyrevelation.entitiy.ModEntityType.SUMMON_APOLLYON.get();
        Entity entity = type.create(level);

        if (entity != null) {
            goetyDelight$setPosition(entity, level, pos);
        }

        return entity;
    }

    @Unique
    private static void goetyDelight$setPosition(
            Entity entity,
            Level level,
            BlockPos pos
    ) {
        entity.absMoveTo(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                level.random.nextInt(360),
                0.0F
        );
    }
}

