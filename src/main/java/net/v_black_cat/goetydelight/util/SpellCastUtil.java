package net.v_black_cat.goetydelight.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.jetbrains.annotations.Nullable;

public final class SpellCastUtil {

    private SpellCastUtil() {
    }

    /** 视线所指的方块；对着空气时返回 null */
    @Nullable
    public static BlockPos aimedBlock(LivingEntity caster) {
        double reach = caster.getAttributeValue(ForgeMod.BLOCK_REACH.get());
        HitResult hit = caster.pick(reach, 1.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        }
        return null;
    }

    /** 施法中心 = 右击的方块；没指到方块时退回施法者脚下 */
    public static BlockPos castCenter(LivingEntity caster) {
        BlockPos aimed = aimedBlock(caster);
        return aimed != null ? aimed : caster.blockPosition();
    }

    /**
     * 施法中心 = 右击的方块<b>或实体</b>（谁更近算谁）；都没有时退回施法者脚下。
     * 实体拾取沿用 Goety 自己的写法（Spell#entityCollideResult）。
     */
    public static BlockPos castCenterOrEntity(LivingEntity caster) {
        BlockPos aimed = aimedBlock(caster);
        EntityHitResult entityHit = aimedEntity(caster);
        if (entityHit != null) {
            Vec3 eye = caster.getEyePosition(1.0F);
            // 方块比实体更近就用方块
            if (aimed == null || eye.distanceToSqr(Vec3.atCenterOf(aimed)) > eye.distanceToSqr(entityHit.getLocation())) {
                return entityHit.getEntity().blockPosition();
            }
        }
        return aimed != null ? aimed : caster.blockPosition();
    }

    /** 视线所指的生物；没指到时返回 null */
    @Nullable
    public static EntityHitResult aimedEntity(LivingEntity caster) {
        double reach = caster.getAttributeValue(ForgeMod.ENTITY_REACH.get());
        Vec3 srcVec = caster.getEyePosition(1.0F);
        Vec3 lookVec = caster.getViewVector(1.0F);
        Vec3 destVec = srcVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        AABB box = caster.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(reach, reach, reach);
        return ProjectileUtil.getEntityHitResult(caster.level(), caster, srcVec, destVec, box,
                entity -> entity instanceof LivingEntity
                        && caster.hasLineOfSight(entity)
                        && !entity.isSpectator()
                        && entity.isPickable());
    }

    /** 视线所指实体本身（供需要"直接对那个实体下手"的法术使用） */
    @Nullable
    public static LivingEntity aimedLivingEntity(LivingEntity caster) {
        EntityHitResult hit = aimedEntity(caster);
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    /** 该实体是否是施法者的视线目标（对"点谁就以谁为中心"的判定有用） */
    public static boolean isAimedAt(LivingEntity caster, Entity target) {
        EntityHitResult hit = aimedEntity(caster);
        return hit != null && hit.getEntity() == target;
    }
}
