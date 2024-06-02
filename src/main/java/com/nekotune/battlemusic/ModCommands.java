package com.nekotune.battlemusic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;

public abstract class ModCommands
{
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal(BattleMusic.MOD_ID)
                .then(Commands.literal("volume")
                        .then(Commands.literal("set")
                                .then(Commands.argument("volume", FloatArgumentType.floatArg(0f, 5f))
                                        .executes(ModCommands::setVolume))
                        ).then(Commands.literal("get")
                                .executes(ModCommands::getVolume)
                        ).then(Commands.literal("separate")
                                .then(Commands.argument("separate", BoolArgumentType.bool())
                                        .executes(ModCommands::separateVolume)))
                ).then(Commands.literal("reload")
                        .executes(ModCommands::reload));
        dispatcher.register(builder);
    }

    private static int setVolume(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        float volume = context.getArgument("volume", float.class);
        ModConfigs.VOLUME.set((double) volume);
        EntityMusic.setMasterVolume(volume);
        source.sendSuccess(ITextComponent.nullToEmpty("Set battle music volume to " + volume), true);
        return 1;
    }

    private static int getVolume(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        source.sendSuccess(ITextComponent.nullToEmpty("Battle music volume is currently set to " + ModConfigs.VOLUME.get()), true);
        return 1;
    }

    private static int separateVolume(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        boolean separate = context.getArgument("separate", boolean.class);
        ModConfigs.LINKED_TO_MUSIC.set(separate);
        source.sendSuccess(ITextComponent.nullToEmpty((separate) ? "Battle music volume is now separate from music volume" : "Battle music volume is now linked to music volume"), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        source.sendSuccess(ITextComponent.nullToEmpty("Reloaded battle music"), true);
        EntityMusic.updateEntitySoundData();
        EntityMusic.setMasterVolume(ModConfigs.VOLUME.get().floatValue());
        return 1;
    }
}
