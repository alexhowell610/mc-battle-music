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

import static com.nekotune.battlemusic.EntityMusic.playing;

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
            .then(Commands.literal("debug")
                .executes(ModCommands::debug))
            .then(Commands.literal("set")
                .then(Commands.argument("entity", ResourceLocationArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                    .executes((ctx) -> ModCommands.set(ctx, false, false))
                    .then(Commands.argument("song", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                        .executes((ctx) -> ModCommands.set(ctx, true, false))
                    .then(Commands.argument("priority", IntegerArgumentType.integer())
                        .executes((ctx) -> ModCommands.set(ctx, true, true))))));
        dispatcher.register(builder);
    }

    private static int setVolume(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        float volume = context.getArgument("volume", float.class);
        ModConfigs.VOLUME.set((double) volume);
        ModConfigs.VOLUME.save();
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
        ModConfigs.LINKED_TO_MUSIC.save();
        source.sendSuccess(() -> Component.literal((separate) ? "Battle music volume is now separate from music volume" : "Battle music volume is now linked to music volume"), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Reloaded battle music"), true);
        BattleMusic.reload();
        return 1;
    }

    private static int debug(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BattleMusic.LOGGER.debug("BATTLE MUSIC DEBUG INFO\n"
                +"  - Entity playing music: " + ((playing != null) ? playing.ENTITY.getName().getString() : "NULL") + "\n"
                +"  - Sound being played: " + ((playing != null) ? playing.SOUND_DATA.soundEvent().getLocation() : "NULL") + "\n"
                +"  - Volume: " + ((playing != null) ? playing.getVolume() : "0") + "\n"
                +"  - Pitch: " + ((playing != null) ? playing.getPitch() : "1") + "\n"
                +"  - Fading out? " + (playing != null && playing.fadeOut) + "\n"
                +"  - Priority: " + ((playing != null) ? playing.SOUND_DATA.priority() : "0")
            );
        source.sendSuccess(() -> Component.literal("Logged debug info to logs/debug.log"), true);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, boolean songDef, boolean priorityDef) {
        CommandSourceStack source = context.getSource();
        ResourceLocation entity = context.getArgument("entity", ResourceLocation.class);
        List<String> data = new ArrayList<>(ModConfigs.ENTITIES_SONGS.get());
        int priority = 0;
        boolean changes = false;

        // Remove old data
        for (int i = 0; i < data.size(); i++) {
            String item = data.get(i);
            if (item.substring(0, item.indexOf(';')).equalsIgnoreCase(entity.toString())) {
                priority = Integer.parseInt(item.substring(item.lastIndexOf(';')+1));
                data.remove(i);
                changes = true;
                i--;
            }
        }

        // Set new data
        String success;
        if (songDef) {
            ResourceLocation song = context.getArgument("song", ResourceLocation.class);
            if (priorityDef) {
                priority = context.getArgument("priority", int.class);
            }
            data.add(entity.toString() + ';' + song.toString() + ';' + priority);
            ModConfigs.ENTITIES_SONGS.set(data);
            ModConfigs.ENTITIES_SONGS.save();
            success = "Set battle music for entity " + entity.getPath() + " to sound " + song + " with priority " + priority;
        } else if (changes) {
            ModConfigs.ENTITIES_SONGS.set(data);
            ModConfigs.ENTITIES_SONGS.save();
            success = "Removed battle music from entity " + entity.getPath();
        } else {
            source.sendFailure(Component.literal("No battle music set for entity " + entity.getPath()));
            return 1;
        }
        source.sendSuccess(() -> Component.literal(success), true);
        BattleMusic.reload();
        return 1;
    }
}
