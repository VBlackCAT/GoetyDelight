package net.v_black_cat.goetydelight.entities.ai.customer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import java.util.function.ToDoubleFunction;

public class AABBRandomPos {

    
    @Nullable
    public static Vec3 getPos(PathfinderMob mob, AABB searchBox, int verticalRange) {
        return getPos(mob, searchBox, verticalRange, mob::getWalkTargetValue);
    }

    
    @Nullable
    public static Vec3 getPos(PathfinderMob mob, AABB searchBox, int verticalRange, ToDoubleFunction<BlockPos> scoringFunction) {
        
        int minY = (int) Math.max(searchBox.minY, mob.getY() - verticalRange);
        int maxY = (int) Math.min(searchBox.maxY, mob.getY() + verticalRange);
        
        
        
        

        return RandomPos.generateRandomPos(() -> {
            
            double randomX = searchBox.minX + mob.getRandom().nextDouble() * (searchBox.maxX - searchBox.minX);
            double randomZ = searchBox.minZ + mob.getRandom().nextDouble() * (searchBox.maxZ - searchBox.minZ);
            
            int randomY = minY + mob.getRandom().nextInt(maxY - minY + 1);

            BlockPos candidatePos = new BlockPos((int) randomX, randomY, (int) randomZ);

            
            candidatePos = movePosUpOutOfSolid(mob, candidatePos);
            
            return candidatePos;
        }, scoringFunction);
    }

    
    @Nullable
    public static BlockPos movePosUpOutOfSolid(PathfinderMob mob, BlockPos pos) {
        
        pos = RandomPos.moveUpOutOfSolid(pos, mob.level().getMaxBuildHeight(), (blockPos) -> GoalUtils.isSolid(mob, blockPos));
        
        return (!GoalUtils.isWater(mob, pos) && !GoalUtils.hasMalus(mob, pos)) ? pos : null;
    }

    @Nullable
    public static Vec3 getPosAwayFromAABB(PathfinderMob mob,
                                          AABB avoidBox,
                                          double desiredDistance,
                                          int horizontalRange,
                                          int verticalRange) {

        Vec3 mobPos = mob.position();

        AABB forbiddenZone = avoidBox.inflate(desiredDistance);
        Vec3 direction;
        if (avoidBox.contains(mobPos)) {
            Vec3 center = avoidBox.getCenter();
            direction = mobPos.subtract(center);

            if (direction.lengthSqr() < 0.0001) {
                double angle = mob.getRandom().nextDouble() * Math.PI * 2;
                direction = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            }
        } else {
            direction = mobPos.subtract(avoidBox.getCenter());
        }

        direction = direction.normalize();

        for (int i = 0; i < 15; ++i) {

            double distance = desiredDistance +
                    mob.getRandom().nextDouble() * horizontalRange;

            Vec3 target = mobPos.add(direction.scale(distance));

            target = target.add(
                    (mob.getRandom().nextDouble() - 0.5) * 2,
                    mob.getRandom().nextInt(verticalRange * 2 + 1) - verticalRange,
                    (mob.getRandom().nextDouble() - 0.5) * 2
            );

            BlockPos pos = new BlockPos(
                    (int) target.x,
                    (int) target.y,
                    (int) target.z
            );

            pos = AABBRandomPos.movePosUpOutOfSolid(mob, pos);

            if (pos != null) {
                Vec3 finalPos = Vec3.atBottomCenterOf(pos);
                if (!forbiddenZone.contains(finalPos)) {
                    return finalPos;
                }
            }
        }

        return null;
    }
    
    
}