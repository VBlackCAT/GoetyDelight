package net.v_black_cat.goetydelight.entities.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.entities.ModEntities;

public class TreeGrowthSpellEntity extends RichSoilSpellEntity {
    private static final int MAX_GROW_ATTEMPTS = 3;

    public TreeGrowthSpellEntity(EntityType<? extends RichSoilSpellEntity> entityType, Level level) {
        super(entityType, level);
    }

    public TreeGrowthSpellEntity(Level level, LivingEntity owner, BlockPos target, ItemStack staff) {
        this(ModEntities.TREE_GROWTH_SPELL.get(), level);
        this.setOwner(owner);
        this.setCastingStaff(staff);
        this.setRadius(1);
        this.setPos(target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D);
    }

    @Override
    protected boolean performImpact(ServerLevel serverLevel, LivingEntity owner, BlockPos center, int radius) {
        BlockState state = serverLevel.getBlockState(center);
        if (!(state.getBlock() instanceof SaplingBlock sapling)) {
            return false;
        }

        if (state.getValue(SaplingBlock.STAGE) == 0) {
            serverLevel.setBlock(center, state.setValue(SaplingBlock.STAGE, 1), 2);
        }

        boolean grew = false;
        for (int i = 0; i < MAX_GROW_ATTEMPTS; ++i) {
            BlockState current = serverLevel.getBlockState(center);
            if (!(current.getBlock() instanceof SaplingBlock currentSapling)) {
                break;
            }
            currentSapling.advanceTree(serverLevel, center, current, serverLevel.random);
            grew = true;
        }
        return grew;
    }
}
