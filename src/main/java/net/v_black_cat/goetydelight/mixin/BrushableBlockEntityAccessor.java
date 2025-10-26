package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BrushableBlockEntity.class)
public interface BrushableBlockEntityAccessor {

    @Accessor("brushCount")
    int getBrushCount();

    @Accessor("brushCount")
    void setBrushCount(int brushCount);

    @Accessor("coolDownEndsAtTick")
    long getCoolDownEndsAtTick();

    @Accessor("coolDownEndsAtTick")
    void setCoolDownEndsAtTick(long tick);

    @Accessor("brushCountResetsAtTick")
    long getBrushCountResetsAtTick();

    @Accessor("brushCountResetsAtTick")
    void setBrushCountResetsAtTick(long tick);
}