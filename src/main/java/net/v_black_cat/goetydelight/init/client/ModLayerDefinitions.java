package net.v_black_cat.goetydelight.init.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModLayerDefinitions {
    public static final ModelLayerLocation FALSE_PROVERBS_BACK = new ModelLayerLocation(ResourceLocation.parse(GoetyDelight.MODID + ":false_proverbs_back"), "main");
    public static final ModelLayerLocation GHOST_FARMER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "ghost_farmer"), "main");
    public static final ModelLayerLocation SOUL_LICH = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "soul_lich"), "main");
}