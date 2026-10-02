package net.v_black_cat.goetydelight.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.entities.ghostfarmer.GhostFarmerEntity;
import net.v_black_cat.goetydelight.entities.soul_lich.SoulLichEntity;
import net.v_black_cat.goetydelight.entities.spell.GrassCuttingSlashEntity;
import net.v_black_cat.goetydelight.entities.spell.RichSoilSpellEntity;
import net.v_black_cat.goetydelight.entities.spell.TreeGrowthSpellEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, GoetyDelight.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GhostFarmerEntity>> GHOST_FARMER =
            ENTITIES.register("ghost_farmer", () -> EntityType.Builder.of(GhostFarmerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.99F).clientTrackingRange(8).build("ghost_farmer"));

    public static final DeferredHolder<EntityType<?>, EntityType<SoulLichEntity>> SOUL_LICH =
            ENTITIES.register("soul_lich", () -> EntityType.Builder.of(SoulLichEntity::new, MobCategory.MONSTER)
                    .sized(0.4F, 0.99F).clientTrackingRange(8).build("soul_lich"));
    public static final DeferredHolder<EntityType<?>,EntityType<DollEntity>> DOLL_ENTITY =
            ENTITIES.register("doll_entity",  () ->
                    EntityType.Builder.<DollEntity>of(DollEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.85f)
                            .clientTrackingRange(10)
                            .build("doll_entity"));

    /** 光柱本体：催熟/锄头/沃土/爱与丰饶/大理石水都靠它延迟执行 */
    public static final DeferredHolder<EntityType<?>, EntityType<RichSoilSpellEntity>> RICH_SOIL_SPELL =
            ENTITIES.register("rich_soil_spell", () ->
                    EntityType.Builder.<RichSoilSpellEntity>of(RichSoilSpellEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSummon()
                            .build("rich_soil_spell"));

    public static final DeferredHolder<EntityType<?>, EntityType<TreeGrowthSpellEntity>> TREE_GROWTH_SPELL =
            ENTITIES.register("tree_growth_spell", () ->
                    EntityType.Builder.<TreeGrowthSpellEntity>of(TreeGrowthSpellEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSummon()
                            .build("tree_growth_spell"));

    public static final DeferredHolder<EntityType<?>, EntityType<GrassCuttingSlashEntity>> GRASS_CUTTING_SLASH =
            ENTITIES.register("grass_cutting_slash", () ->
                    EntityType.Builder.<GrassCuttingSlashEntity>of(GrassCuttingSlashEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSummon()
                            .build("grass_cutting_slash"));

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}