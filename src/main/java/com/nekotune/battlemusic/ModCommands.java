package com.nekotune.battlemusic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class ModCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(BattleMusic.MOD_ID)
            .then(Commands.literal("volume")
                .then(Commands.literal("set")
                    .then(Commands.argument("volume", FloatArgumentType.floatArg(0f, 5f))
                        .executes(ModCommands::setVolume)))
                .then(Commands.literal("get")
                    .executes(ModCommands::getVolume))
                .then(Commands.literal("separate")
                    .then(Commands.argument("separate", BoolArgumentType.bool())
                        .executes(ModCommands::separateVolume))))
            .then(Commands.literal("reload")
                .executes(ModCommands::reload))
            .then(Commands.literal("setMusic")
                .then(Commands.argument("entity", ResourceLocationArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                    .executes((ctx) -> ModCommands.setMusic(ctx, false, false))
                    .then(Commands.argument("song", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                        .executes((ctx) -> ModCommands.setMusic(ctx, true, false))
                    .then(Commands.argument("priority", IntegerArgumentType.integer())
                        .executes((ctx) -> ModCommands.setMusic(ctx, true, true))))));
        dispatcher.register(builder);
    }

    private static int setVolume(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        float volume = context.getArgument("volume", float.class);
        ModConfigs.VOLUME.set((double) volume);
        EntityMusic.setMasterVolume(volume);
        source.sendSuccess(() -> Component.literal("Set battle music volume to " + volume), true);
        return 1;
    }

    private static int getVolume(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Battle music volume is currently set to " + ModConfigs.VOLUME.get()), true);
        return 1;
    }

    private static int separateVolume(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean separate = context.getArgument("separate", boolean.class);
        ModConfigs.LINKED_TO_MUSIC.set(separate);
        source.sendSuccess(() -> Component.literal((separate) ? "Battle music volume is now separate from music volume" : "Battle music volume is now linked to music volume"), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Reloaded battle music"), true);
        return ModCommands.reload();
    }

    private static int reload() {
        EntityMusic.updateEntitySoundData();
        EntityMusic.setMasterVolume(ModConfigs.VOLUME.get().floatValue());
        return 1;
    }

    private static int setMusic(CommandContext<CommandSourceStack> context, boolean songDef, boolean priorityDef) {
        CommandSourceStack source = context.getSource();
        ResourceLocation entity = context.getArgument("entity", ResourceLocation.class);
        List<? extends String> defined = ModConfigs.ENTITIES_SONGS.get();
        if (!songDef) {
            List<Integer> indices = new ArrayList<>(List.of());
            for (int i = 0; i < defined.size(); i++) {
                String item = defined.get(i);
                if (item.substring(0, 2).equalsIgnoreCase(entity.toString())) {
                    indices.add(0, i);
                }
            }
            for (int i : indices) {
                defined.remove(i);
            }
            ModConfigs.ENTITIES_SONGS.set(defined);
            ModConfigs.ENTITIES_SONGS.save();
            source.sendSuccess(() -> Component.literal("Removed battle music from entity " + entity.getPath()), true);
        } else {
            ResourceLocation song = context.getArgument("song", ResourceLocation.class);
            int priority = 0;
            if (priorityDef) {
                priority = context.getArgument("priority", int.class);
            }
            List<String> data = new ArrayList<>(List.of());
            data.addAll(defined);
            data.add(entity.toString() + ';' + song.toString() + ';' + priority);
            defined = ModConfigs.BUILDER.defineListAllowEmpty(List.of("entities_songs"), () -> data, a -> true).get();
            ModConfigs.ENTITIES_SONGS.set(defined);
            ModConfigs.ENTITIES_SONGS.save();
            final int p = priority;
            source.sendSuccess(() -> Component.literal("Set battle music for entity " + entity.getPath() + " to sound " + song + " with priority " + p), true);
        }
        return ModCommands.reload();
    }
}
