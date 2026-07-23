package net.v_black_cat.goetydelight.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

import java.util.Collection;
import java.util.Set;

public class BuffCommand {

    private static final SuggestionProvider<CommandSourceStack> EFFECT_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(ModBuffTypes.getRegisteredIds(), builder);

    private static final DynamicCommandExceptionType UNKNOWN_EFFECT =
            new DynamicCommandExceptionType(id -> Component.literal("Unknown buff type: " + id));

    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED =
            new SimpleCommandExceptionType(Component.translatable("commands.goetydelight.buff.give.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED =
            new SimpleCommandExceptionType(Component.translatable("commands.goetydelight.buff.remove.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> buffCommand = Commands.literal("buff")
                .requires(source -> source.hasPermission(2))
                // --- give 子命令 ---
                .then(Commands.literal("give")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("buff_type", ResourceLocationArgument.id())
                                        .suggests(EFFECT_SUGGESTIONS)
                                        .executes(ctx -> giveBuff(
                                                ctx,
                                                EntityArgument.getPlayers(ctx, "targets"),
                                                ResourceLocationArgument.getId(ctx, "buff_type"),
                                                200, 1))
                                        .then(Commands.argument("duration", IntegerArgumentType.integer(1, 1000000))
                                                .executes(ctx -> giveBuff(
                                                        ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        ResourceLocationArgument.getId(ctx, "buff_type"),
                                                        IntegerArgumentType.getInteger(ctx, "duration"),
                                                        1))
                                                .then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                                        .executes(ctx -> giveBuff(
                                                                ctx,
                                                                EntityArgument.getPlayers(ctx, "targets"),
                                                                ResourceLocationArgument.getId(ctx, "buff_type"),
                                                                IntegerArgumentType.getInteger(ctx, "duration"),
                                                                IntegerArgumentType.getInteger(ctx, "amplifier"))))
                                        )
                                )
                        )
                )
                // --- remove 子命令 ---
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("buff_type", ResourceLocationArgument.id())
                                        .suggests(EFFECT_SUGGESTIONS)
                                        .executes(ctx -> removeBuff(
                                                ctx,
                                                EntityArgument.getPlayers(ctx, "targets"),
                                                ResourceLocationArgument.getId(ctx, "buff_type")))
                                )
                        )
                )
                // --- list 子命令 ---
                .then(Commands.literal("list")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(BuffCommand::listBuffs))
                );

        dispatcher.register(Commands.literal("goetydelight").then(buffCommand));
    }

    private static int giveBuff(CommandContext<CommandSourceStack> ctx,
                                Collection<ServerPlayer> targets,
                                ResourceLocation typeId,
                                int duration,
                                int amplifier) throws CommandSyntaxException {
        checkRegistered(typeId);
        int successCount = 0;

        for (ServerPlayer player : targets) {
            BuffUtil.applyBuff(player, typeId, duration, amplifier);
            successCount++;
        }

        if (successCount == 0) {
            throw ERROR_GIVE_FAILED.create();
        }

        if (targets.size() == 1) {
            ctx.getSource().sendSuccess(() ->
                    Component.translatable("commands.goetydelight.buff.give.success.single",
                            typeId.toString(),
                            targets.iterator().next().getDisplayName(),
                            duration / 20,
                            amplifier), true);
        } else {
            ctx.getSource().sendSuccess(() ->
                    Component.translatable("commands.goetydelight.buff.give.success.multiple",
                            typeId.toString(),
                            targets.size(),
                            duration / 20,
                            amplifier), true);
        }

        return successCount;
    }

    private static int removeBuff(CommandContext<CommandSourceStack> ctx,
                                  Collection<ServerPlayer> targets,
                                  ResourceLocation typeId) throws CommandSyntaxException {
        checkRegistered(typeId);
        int successCount = 0;

        for (ServerPlayer player : targets) {
            if (BuffUtil.hasBuff(player, typeId)) {
                BuffUtil.removeBuff(player, typeId);
                successCount++;
            }
        }

        if (successCount == 0) {
            throw ERROR_REMOVE_FAILED.create();
        }

        if (targets.size() == 1) {
            ctx.getSource().sendSuccess(() ->
                    Component.translatable("commands.goetydelight.buff.remove.success.single",
                            typeId.toString(),
                            targets.iterator().next().getDisplayName()), true);
        } else {
            ctx.getSource().sendSuccess(() ->
                    Component.translatable("commands.goetydelight.buff.remove.success.multiple",
                            typeId.toString(),
                            targets.size()), true);
        }

        return successCount;
    }

    private static int listBuffs(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        ActiveBuffs activeBuffs = target.getData(ModAttachments.ACTIVE_BUFFS);

        if (activeBuffs == null || activeBuffs.getActiveTypes().isEmpty()) {
            context.getSource().sendSuccess(() ->
                    Component.translatable("commands.goetydelight.buff.list.empty", target.getName()), false);
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        Set<ResourceLocation> types = activeBuffs.getActiveTypes();
        for (ResourceLocation typeId : types) {
            int totalAmp = activeBuffs.getTotalAmplifier(typeId);
            sb.append(typeId.toString()).append(" (amp: ").append(totalAmp).append("), ");
        }
        String result = sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";

        context.getSource().sendSuccess(() ->
                Component.translatable("commands.goetydelight.buff.list.success", target.getName(), result), false);
        return types.size();
    }

    private static void checkRegistered(ResourceLocation effectId) throws CommandSyntaxException {
        if (!ModBuffTypes.getRegisteredIds().contains(effectId)) {
            throw UNKNOWN_EFFECT.create(effectId);
        }
    }
}