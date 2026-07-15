package net.v_black_cat.goetydelight.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelShapeUtil {
    public static VoxelShape rotateAABB90Clockwise(AABB aabb) {
        double newMinX = 1 - aabb.maxZ;
        double newMinZ = aabb.minX;
        double newMaxX = 1 - aabb.minZ;
        double newMaxZ = aabb.maxX;
        double newMinY = aabb.minY;
        double newMaxY = aabb.maxY;
        return Shapes.create(new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ));
    }
    public static VoxelShape rotateVoxelShape90Clockwise(VoxelShape originalShape) {
        return originalShape.toAabbs().stream()
                .map(aabb -> rotateAABB90Clockwise(aabb))
                .reduce(Shapes.empty(), Shapes::or);
    }
}
