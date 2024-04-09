package com.nekotune.battlemusic;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

public abstract class ModConfigs
{
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> VOLUME;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LINKED_TO_MUSIC;
    public static final ForgeConfigSpec.ConfigValue<Double> HEALTH_PITCH_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEALTH_PITCH_THRESH;
    public static final ForgeConfigSpec.ConfigValue<Double> FADE_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITIES_SONGS;

    static {
        BUILDER.push(BattleMusic.MOD_ID + " configs");

        VOLUME = BUILDER.comment("\nVolume of battle music, set to zero to mute")
                        .defineInRange("volume", 1D, 0D, 5D);
        LINKED_TO_MUSIC = BUILDER.comment("\nWhether battle music's volume is affected by the music slider")
                        .define("linked_to_music_volume", true);
        HEALTH_PITCH_AMOUNT = BUILDER.comment("\nHow much the battle music changes in pitch when at low health\nValues below zero shift the pitch down, above zero shift the pitch up\nSet to zero to disable")
                        .defineInRange("health_pitch_amount", 0.05D, -0.5D, 1D);
        HEALTH_PITCH_THRESH = BUILDER.comment("\nAt what HP should the battle music shift in pitch")
                        .defineInRange("health_pitch_thresh", 6, 1, Integer.MAX_VALUE);
        FADE_TIME = BUILDER.comment("\nHow many seconds songs take to fade in and out")
                        .defineInRange("fade_time", 1D, 0D, 10D);
        ENTITIES_SONGS = BUILDER.comment("\nEntites and their respective songs, write in entity;song;priority format")
                        .defineListAllowEmpty(List.of("entities_songs"), () -> List.of(
                                "minecraft:elder_guardian;battlemusic:mini;0",
                                "minecraft:evoker;battlemusic:mini;0",
                                "minecraft:ravager;battlemusic:mini;0",
                                "minecraft:warden;battlemusic:warden;1",
                                "minecraft:wither;battlemusic:wither;2",
                                "minecraft:ender_dragon;battlemusic:ender_dragon;5",
                                "endergetic:brood_eetle;battlemusic:brood_eetle;3",
                                "alexsmobs:void_worm;battlemusic:void_worm;3",
                                "alexsmobs:warped_mosco;battlemusic:mini;0",
                                "aether:slider;battlemusic:slider;2",
                                "aether:valkyrie_queen;battlemusic:slider;2",
                                "aether:sun_god;battlemusic:sun_god;3",
                                "cataclysm:ender_golem;battlemusic:mini;0",
                                "cataclysm:ignited_revenant;battlemusic:mini;0",
                                "cataclysm:ender_guardian;battlemusic:ender_guardian;4",
                                "cataclysm:netherite_monstrosity;battlemusic:netherite_monstrosity;4",
                                "cataclysm:ignis;battlemusic:ignis;4",
                                "cataclysm:harbinger;battlemusic:harbinger;4",
                                "cataclysm:the_leviathan;battlemusic:leviathan;4",
                                "cataclysm:the_prowler;battlemusic:mini;0",
                                "cataclysm:coralssus;battlemusic:mini;0",
                                "cataclysm:modern_remnant;battlemusic:snow_queen;3",
                                "cataclysm:ancient_remnant;battlemusic:snow_queen;3",
                                "twilightforest:naga;battlemusic:naga;2",
                                "twilightforest:lich;battlemusic:lich;3",
                                "twilightforest:hydra;battlemusic:hydra;3",
                                "twilightforest:minoshroom;battlemusic:mini;0",
                                "twilightforest:alpha_yeti;battlemusic:mini;0",
                                "twilightforest:phantom_knight;battlemusic:mini;0",
                                "twilightforest:ur_ghast;battlemusic:ur_ghast;3",
                                "twilightforest:snow_queen;battlemusic:snow_queen;3",
                                "mutant_mobs:mutant_zombie;battlemusic:mini;0",
                                "mutant_mobs:mutant_skeleton;battlemusic:mini;0",
                                "mutant_mobs:mutant_creeper;battlemusic:mini;0",
                                "mutant_mobs:mutant_enderman;battlemusic:mutant_enderman;1",
                                "mutant_more:mutant_blaze;battlemusic:mini;0",
                                "mutant_more:mutant_shulker;battlemusic:mini;0",
                                "mutant_more:mutant_wither_skeleton;battlemusic:mini;0",
                                "rottencreatures:dead_beard;battlemusic:mini;0",
                                "rottencreatures:immortal;battlemusic:mini;0",
                                "irons_spellbooks:citadel_keeper;battlemusic:mini;0",
                                "irons_spellbooks:archevoker;battlemusic:mini;0",
                                "bosses_of_mass_destruction:void_blossom;battlemusic:naga;3",
                                "bosses_of_mass_destruction:lich;battlemusic:lich;3",
                                "bosses_of_mass_destruction:gauntlet;battlemusic:slider;3",
                                "bosses_of_mass_destruction:obsidilith;battlemusic:ur_ghast;3"
                                ), a -> true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
