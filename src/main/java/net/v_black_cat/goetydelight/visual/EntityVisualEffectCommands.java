package net.v_black_cat.goetydelight.visual;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public final class EntityVisualEffectCommands {
    private static final SuggestionProvider<CommandSourceStack> EFFECT_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(GDVisualEffects.registeredIds(), builder);
    private static final DynamicCommandExceptionType UNKNOWN_EFFECT =
            new DynamicCommandExceptionType(id -> Component.literal("未知视觉效果 ID: " + id));

    private EntityVisualEffectCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("goetydelightvisual")
                .requires(source -> source.hasPermission(2))
                .then(addCommand())
                .then(removeCommand())
                .then(clearCommand()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addCommand() {
        return Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("effect", ResourceLocationArgument.id())
                                .suggests(EFFECT_SUGGESTIONS)
                                .executes(context -> addEffect(
                                        EntityArgument.getEntities(context, "targets"),
                                        ResourceLocationArgument.getId(context, "effect")
                                ))
                                .then(Commands.argument("data", CompoundTagArgument.compoundTag())
                                        .executes(context -> addEffect(
                                                EntityArgument.getEntities(context, "targets"),
                                                ResourceLocationArgument.getId(context, "effect"),
                                                0,
                                                CompoundTagArgument.getCompoundTag(context, "data")
                                        )))
                                .then(Commands.argument("duration", IntegerArgumentType.integer(EntityVisualEffects.INFINITE))
                                        .executes(context -> addEffect(
                                                EntityArgument.getEntities(context, "targets"),
                                                ResourceLocationArgument.getId(context, "effect"),
                                                IntegerArgumentType.getInteger(context, "duration")
                                        ))
                                        .then(Commands.argument("data", CompoundTagArgument.compoundTag())
                                                .executes(context -> addEffect(
                                                        EntityArgument.getEntities(context, "targets"),
                                                        ResourceLocationArgument.getId(context, "effect"),
                                                        IntegerArgumentType.getInteger(context, "duration"),
                                                        CompoundTagArgument.getCompoundTag(context, "data")
                                                ))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeCommand() {
        return Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("effect", ResourceLocationArgument.id())
                                .suggests(EFFECT_SUGGESTIONS)
                                .executes(context -> removeEffect(
                                        EntityArgument.getEntities(context, "targets"),
                                        ResourceLocationArgument.getId(context, "effect")
                                ))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> clearCommand() {
        return Commands.literal("clear")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(context -> clearEffects(EntityArgument.getEntities(context, "targets"))));
    }

    private static int addEffect(Collection<? extends Entity> entities, ResourceLocation effectId, int duration) throws CommandSyntaxException {
        return addEffect(entities, effectId, duration, new CompoundTag());
    }

    private static int addEffect(Collection<? extends Entity> entities, ResourceLocation effectId, int duration, CompoundTag data) throws CommandSyntaxException {
        checkRegistered(effectId);
        int changed = 0;
        for (Entity entity : entities) {
            if (EntityVisualEffectSystem.addEffect(entity, effectId, duration, data)) {
                changed++;
            }
        }
        return changed;
    }

    private static int addEffect(Collection<? extends Entity> entities, ResourceLocation effectId) throws CommandSyntaxException {
        return addEffect(entities, effectId, 0);
    }

    private static int removeEffect(Collection<? extends Entity> entities, ResourceLocation effectId) throws CommandSyntaxException {
        checkRegistered(effectId);
        int changed = 0;
        for (Entity entity : entities) {
            if (EntityVisualEffectSystem.removeEffect(entity, effectId)) {
                changed++;
            }
        }
        return changed;
    }

    private static int clearEffects(Collection<? extends Entity> entities) {
        int changed = 0;
        for (Entity entity : entities) {
            boolean hadEffects = entity.getCapability(EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS)
                    .map(effects -> !effects.isEmpty())
                    .orElse(false);
            if (hadEffects) {
                entity.getCapability(EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS).ifPresent(effects -> {
                    effects.clear();
                    EntityVisualEffectSystem.sync(entity);
                });
                changed++;
            }
        }
        return changed;
    }

    private static void checkRegistered(ResourceLocation effectId) throws CommandSyntaxException {
        if (!GDVisualEffects.isRegistered(effectId)) {
            throw UNKNOWN_EFFECT.create(effectId);
        }
    }
}
