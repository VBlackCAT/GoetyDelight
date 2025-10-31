package net.v_black_cat.goetydelight.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.entities.ModEntityType;

public class ModSpawnEggItem extends ForgeSpawnEggItem {
    public ModSpawnEggItem(final RegistryObject<? extends EntityType<? extends Mob>> entityTypeSupplier, int primaryColorIn, int secondaryColorIn, Properties builder) {
        super(Lazy.of(entityTypeSupplier), primaryColorIn, secondaryColorIn, builder);
    }
}
