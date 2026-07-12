package net.v_black_cat.goetydelight.init;

import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModGameEvents {
    public static final DeferredRegister<GameEvent> GAME_EVENTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.GAME_EVENT, GoetyDelight.MODID);

    // 示例游戏事件
    // public static final DeferredHolder<GameEvent, GameEvent> EXAMPLE_EVENT =
    //         GAME_EVENTS.register("example_event", () -> new GameEvent(16));

    public static void register(IEventBus modEventBus) {
        GAME_EVENTS.register(modEventBus);
    }
}