package net.v_black_cat.goetydelight.entities;


import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.Hellfire;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class OwnedHellfire extends Hellfire {

    public OwnedHellfire(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    public OwnedHellfire(Level world, double pPosX, double pPosY, double pPosZ, @Nullable LivingEntity owner) {
        super(world, pPosX, pPosY, pPosZ, owner);
    }

    public OwnedHellfire(Level world, BlockPos blockPos, @Nullable LivingEntity owner) {
        super(world, blockPos, owner);
    }

    public OwnedHellfire(Level world, Vec3 vector3d, @Nullable LivingEntity owner) {
        super(world, vector3d, owner);
    }

    @Override
    public void dealDamageTo(LivingEntity target) {
        LivingEntity owner = this.getOwner();

        // 检查目标是否应该免疫伤害
        if (shouldBeImmune(target, owner)) {
            return;
        }

        super.dealDamageTo(target);
    }

    private boolean shouldBeImmune(LivingEntity target, LivingEntity owner) {
        // 1. 目标就是主人
        if (target == owner) {
            return true;
        }

        // 2. 目标是主人的仆从
        if (target instanceof IOwned ownedTarget) {
            LivingEntity targetOwner = ownedTarget.getTrueOwner();
            if (targetOwner == owner) {
                return true;
            }

            // 检查主人链（如果仆从的主人也是仆从）
            if (targetOwner instanceof IOwned ownedOwner) {
                if (ownedOwner.getTrueOwner() == owner) {
                    return true;
                }
            }
        }

        // 3. 目标是主人的盟友
        return MobUtil.areAllies(owner, target);
    }
}