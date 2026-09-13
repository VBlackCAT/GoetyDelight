package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.util.SummonApostle;
import com.Polarice3.Goety.common.ritual.SummonRitual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.v_black_cat.goetydelight.compat.goety_revelation.item.AtonementVoucherWrapedCodItem;
import net.v_black_cat.goetydelight.compat.goety_revelation.item.DoomCookieItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = SummonRitual.class, priority = 900, remap = false)
public abstract class SummonApollyonMixin {
    @Unique
    private static final ResourceLocation APOLLYON_ID = new ResourceLocation("goety_revelation", "summon_apollyon");
    @Unique
    private Player goetyDelight$castingPlayer;
    @Unique
    private Level goetyDelight$level;
    @Unique
    private BlockPos goetyDelight$blockPos;

    @Inject(method = "finish", at = @At("HEAD"), remap = false)
    private void goetyDelight$captureRitualData(Level world, BlockPos blockPos, DarkAltarBlockEntity tileEntity, Player castingPlayer, ItemStack activationItem, CallbackInfo ci) {
        this.goetyDelight$castingPlayer = castingPlayer;
        this.goetyDelight$level = world;
        this.goetyDelight$blockPos = blockPos;
    }

    @ModifyArg(method = "finish", at = @At(
                    value = "INVOKE",
                    target =
                            "Lcom/Polarice3/Goety/common/ritual/SummonRitual;"
                                    + "spawnEntity("
                                    + "Lnet/minecraft/world/entity/player/Player;"
                                    + "Lnet/minecraft/world/entity/Entity;"
                                    + "Lnet/minecraft/world/level/Level;"
                                    + ")V",
                    ordinal = 1
            ),
            index = 1,
            remap = false
    )
    private Entity goetyDelight$modifyApollyonResult(Entity entity) {
        Player player = this.goetyDelight$castingPlayer;
        Level level = this.goetyDelight$level;
        BlockPos blockPos = this.goetyDelight$blockPos;
        if (player == null || level == null || blockPos == null) {
            return entity;
        }
        if (level.isClientSide) {
            return entity;
        }
        if (goetyDelight$isApollyon(entity)) {
            int voucherCount = AtonementVoucherWrapedCodItem.getUsageCount(player);
            if (voucherCount > 0) {
                AtonementVoucherWrapedCodItem.setUsageCount(player, voucherCount - 1);
                Entity normalApostle = goetyDelight$createSummonApostle(level, blockPos);
                if (normalApostle != null) {
                    return normalApostle;
                }
            }
            return entity;
        }

        if (entity instanceof SummonApostle) {
            int doomCookieCount = DoomCookieItem.getUsageCount(player);
            if (doomCookieCount > 0) {
                DoomCookieItem.setUsageCount(player, doomCookieCount - 1);
                Entity apollyon = goetyDelight$createApollyon(level, blockPos);
                if (apollyon != null) {
                    return apollyon;
                }
            }
        }
        return entity;
    }

    @Unique
    private static boolean goetyDelight$isApollyon(Entity entity) {
        if (entity == null) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return APOLLYON_ID.equals(id);
    }

    @Unique
    private static Entity goetyDelight$createSummonApostle(Level level, BlockPos blockPos) {
        Entity entity = ModEntityType.SUMMON_APOSTLE.get().create(level);
        if (entity == null) {
            return null;
        }
        entity.absMoveTo(
                blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D,
                level.random.nextInt(360),
                0.0F
        );
        return entity;
    }

    @Unique
    private static Entity goetyDelight$createApollyon(Level level, BlockPos blockPos) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(APOLLYON_ID);
        if (entityType == null) {
            return null;
        }
        Entity entity = entityType.create(level);
        if (entity == null) {
            return null;
        }
        entity.absMoveTo(
                blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D,
                level.random.nextInt(360),
                0.0F
        );
        return entity;
    }
}