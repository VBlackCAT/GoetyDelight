package net.v_black_cat.goetydelight.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
    @Accessor
    GameEventDispatcher getGameEventDispatcher();
}
