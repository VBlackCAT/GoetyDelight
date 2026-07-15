package net.v_black_cat.goetydelight.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Mod 命令注册入口。
 */

public class ModCommands {
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        BuffCommand.register(dispatcher);
    }
}