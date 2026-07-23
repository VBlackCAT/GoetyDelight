package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModJukeboxSongs {
    public static final DeferredRegister<JukeboxSong> JUKEBOX_SONGS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.JUKEBOX_SONG, GoetyDelight.MODID);

    // 示例（需替换为实际声音事件）
    // public static final DeferredHolder<JukeboxSong, JukeboxSong> EXAMPLE_SONG =
    //         JUKEBOX_SONGS.register("example_song", () -> new JukeboxSong(
    //                 ModSounds.EXAMPLE_SOUND.get(),
    //                 net.minecraft.network.chat.Component.translatable("jukebox_song.goetydelight.example_song"),
    //                 120.0F,
    //                 1
    //         ));

    public static void register(IEventBus modEventBus) {
        JUKEBOX_SONGS.register(modEventBus);
    }
}