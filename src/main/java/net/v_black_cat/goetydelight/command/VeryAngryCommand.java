package net.v_black_cat.goetydelight.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.util.EntityUtil;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class VeryAngryCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("very")
                .then(Commands.literal("angry")
                        .then(Commands.literal("so")
                                .then(Commands.literal("to")
                                        .then(Commands.literal("attack")
                                                .then(Commands.argument("target", StringArgumentType.word())
                                                        .then(Commands.argument("damage", DoubleArgumentType.doubleArg(0.0))
                                                                .executes(VeryAngryCommand::executeCommand)
                                                        )
                                                )
                                        )
                                )
                        )
                );

        dispatcher.register(command);
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String targetStr = StringArgumentType.getString(context, "target");
        double damage = DoubleArgumentType.getDouble(context, "damage");

        ServerLevel level = source.getLevel();
        Entity target = null;

        ServerPlayer player = level.getServer().getPlayerList().getPlayerByName(targetStr);
        if (player != null) {
            target = player;
        }

        if (target == null) {
            try {
                UUID uuid = UUID.fromString(targetStr);
                target = level.getEntity(uuid);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (target == null) {
            source.sendFailure(Component.literal("§c找不到目标实体: " + targetStr));
            return 0;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            source.sendFailure(Component.literal("§c目标必须是生物实体！"));
            return 0;
        }

        float currentHealth = EntityUtil.DsGetHealth(livingTarget);
        float newHealth = Math.max(0.0f, currentHealth - (float) damage);

        EntityUtil.DsSetHealth(livingTarget, newHealth);

        if (source.getEntity() instanceof ServerPlayer executor) {
            executor.sendSystemMessage(Component.literal(
                    String.format("§7已对 §e%s §7造成 §c%.1f §7点伤害 §8(§7%.1f §8-> §7%.1f§8)",
                            target.getName().getString(),
                            damage,
                            currentHealth,
                            newHealth
                    )
            ));
        }

        return 1;
    }
}