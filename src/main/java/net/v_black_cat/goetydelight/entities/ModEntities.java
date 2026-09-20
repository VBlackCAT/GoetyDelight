package net.v_black_cat.goetydelight.entities;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.ghostfarmer.GhostFarmerEntity;
import net.v_black_cat.goetydelight.entities.soul_lich.SoulLichEntity;
import net.v_black_cat.goetydelight.entities.spell.GrassCuttingSlashEntity;
import net.v_black_cat.goetydelight.entities.spell.RichSoilSpellEntity;
import net.v_black_cat.goetydelight.entities.spell.TreeGrowthSpellEntity;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GoetyDelight.MODID);

    public static final RegistryObject<EntityType<GhostFarmerEntity>> GHOST_FARMER =
            ENTITY_TYPES.register("ghost_farmer", () -> EntityType.Builder.of(GhostFarmerEntity::new, MobCategory.CREATURE).sized(0.6F, 1.99F).clientTrackingRange(8).build("ghost_farmer"));
    public static final RegistryObject<EntityType<SoulLichEntity>> SOUL_LICH =
            ENTITY_TYPES.register("soul_lich", () -> EntityType.Builder.of(SoulLichEntity::new, MobCategory.MONSTER).sized(0.4F, 0.99F).clientTrackingRange(8).build("soul_lich"));
    public static final RegistryObject<EntityType<RichSoilSpellEntity>> RICH_SOIL_SPELL =
            ENTITY_TYPES.register("rich_soil_spell", () ->
                    EntityType.Builder.<RichSoilSpellEntity>of(RichSoilSpellEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSummon()
                            .build("rich_soil_spell"));
    public static final RegistryObject<EntityType<TreeGrowthSpellEntity>> TREE_GROWTH_SPELL =
            ENTITY_TYPES.register("tree_growth_spell", () ->
                    EntityType.Builder.<TreeGrowthSpellEntity>of(TreeGrowthSpellEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSummon()
                            .build("tree_growth_spell"));

    public static final RegistryObject<EntityType<GrassCuttingSlashEntity>> GRASS_CUTTING_SLASH =
            ENTITY_TYPES.register("grass_cutting_slash", () ->
                    EntityType.Builder.<GrassCuttingSlashEntity>of(GrassCuttingSlashEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSummon()
                            .build("grass_cutting_slash"));
    public static final RegistryObject<EntityType<DollEntity>> DOLL_ENTITY =
            ENTITY_TYPES.register("doll_entity",  () ->
                    EntityType.Builder.<DollEntity>of(DollEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.85f)
                            .clientTrackingRange(10)
                            .build("doll_entity"));



    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
