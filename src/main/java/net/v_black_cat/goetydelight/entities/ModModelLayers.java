package net.v_black_cat.goetydelight.entities;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModModelLayers {

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation GHOST_FARMER_LAYER = new ModelLayerLocation(new ResourceLocation(GoetyDelight.MODID, "ghost_farmer"), "main");

}
