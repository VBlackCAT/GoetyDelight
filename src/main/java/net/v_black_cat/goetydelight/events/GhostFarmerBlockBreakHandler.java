package net.v_black_cat.goetydelight.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.v_black_cat.goetydelight.entities.ghostfarmer.GhostFarmerEntity;
import net.v_black_cat.goetydelight.init.ModBlocks;

import java.util.List;

public class GhostFarmerBlockBreakHandler {
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() == ModBlocks.ECTOPLASMIC_MELON_BLOCK.get()) {
            Player player = event.getPlayer();
            Level level = player.level();

            BlockPos breakPos = event.getPos();
            if (level instanceof ServerLevel serverLevel) {
                Structure structure = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .get(ResourceKey.create(Registries.STRUCTURE,
                                ResourceLocation.fromNamespaceAndPath("goetydelight", "ectoplasmic_melon_field")));
                if (structure == null) return;
                StructureStart structureStart = serverLevel.structureManager()
                        .getStructureWithPieceAt(breakPos, structure);
                if (structureStart == null || !structureStart.isValid()) {
                    return;
                }
            }

            AABB searchArea = new AABB(event.getPos()).inflate(16);
            List<GhostFarmerEntity> ghostFarmers = level.getEntitiesOfClass(
                    GhostFarmerEntity.class, searchArea);

            for (GhostFarmerEntity ghostFarmer : ghostFarmers) {
                ghostFarmer.onEctoplasmicMelonBreak(player);
            }
        }
    }
}
