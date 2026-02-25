package net.v_black_cat.goetydelight.entities.ai.customer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.GoalUtils;
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

    
    
}