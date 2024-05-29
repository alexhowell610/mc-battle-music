package com.nekotune.battlemusic;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public abstract class ModConfigs
{
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> VOLUME;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LINKED_TO_MUSIC;
    public static final ForgeConfigSpec.ConfigValue<Double> HEALTH_PITCH_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Double> DRAGON_PITCH_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEALTH_PITCH_THRESH;
    public static final ForgeConfigSpec.ConfigValue<Double> FADE_TIME;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITIES_SONGS;

    static {
        BUILDER.push(BattleMusic.MOD_ID + " configs");

        VOLUME = BUILDER.comment("\nVolume of battle music, set to zero to mute")
                        .defineInRange("volume", 1D, 0D, 5D);
        LINKED_TO_MUSIC = BUILDER.comment("\nWhether battle music's volume is affected by the music slider")
                        .define("linked_to_music_volume", true);
        HEALTH_PITCH_AMOUNT = BUILDER.comment("""
                        
                        How much the battle music changes in pitch when at low health   \s
                            > Values below zero shift the pitch down, above zero shift the pitch up   \s
                            > Set to zero to disable  \s""")
                        .defineInRange("health_pitch_amount", 0.05D, -0.5D, 1D);
        DRAGON_PITCH_AMOUNT = BUILDER.comment("\nHow much the battle music changes in pitch during the second phase of the Ender Dragon fight\nValues below zero shift the pitch down, above zero shift the pitch up\nSet to zero to disable")
                .defineInRange("dragon_pitch_amount", 0.05D, -0.5D, 1D);
        HEALTH_PITCH_THRESH = BUILDER.comment("\nAt what HP should the battle music shift in pitch")
                        .defineInRange("health_pitch_thresh", 6, 1, Integer.MAX_VALUE);
        FADE_TIME = BUILDER.comment("\nHow many seconds songs take to fade in and out")
                        .defineInRange("fade_time", 1D, 0D, 10D);
        ENTITIES_SONGS = BUILDER.comment("""

                        Entites and their respective songs, write in entity;song;priority format      \s
                            > entity = the entity's ID, eg. "minecraft:pig"      \s
                            > song = the song's ID, eg. "battlemusic:mini1"      \s
                            > priority = what precedence this song takes over other entity's music, eg. a priority of 1 will fade out and play over a priority of 0.     \s""")
                        .defineListAllowEmpty(List.of("entities_songs"), () -> List.of(
                                "minecraft:elder_guardian;battlemusic:mini1;0\n",
                                "minecraft:warden;battlemusic:necromancer;1\n",
                                "minecraft:wither;battlemusic:wither_storm;2\n",
                                "minecraft:ender_dragon;battlemusic:enderman;5\n",
                                "iter_rpg:hobgoblin;battlemusic:mini1;0\n",
                                "queen_bee:queen_bee;battlemusic:mini1;0\n",
                                "irons_spellbooks:dead_king;battlemusic:boss;3\n",
                                "irons_spellbooks:citadel_keeper;battlemusic:mini1;0\n",
                                "irons_spellbooks:archevoker;battlemusic:mini1;0\n",
                                "dawnera:tyrannosaurus;battlemusic:mini1;0\n",
                                "upgrade_aquatic:great_thrasher;battlemusic:mini1;0\n",
                                "born_in_chaos_v1:dire_hound_leader;battlemusic:mini1;0\n",
                                "born_in_chaos_v1:supreme_bonescaller;battlemusic:mini1;0\n",
                                "alexscaves:luxtructosaurus;battlemusic:menta_mardi;3\n",
                                "alexscaves:tremorzilla;battlemusic:metaluna;3\n",
                                "alexscaves:magnetron;battlemusic:mini2;0\n",
                                "alexscaves:forsaken;battlemusic:mini2;1\n",
                                "alexscaves:brainiac;battlemusic:mini1;0\n",
                                "alexsmobs:void_worm;battlemusic:ghast;4\n",
                                "alexsmobs:warped_mosco;battlemusic:mini1;0\n",
                                "plenty_of_golems:ancient_of_prismarine;battlemusic:mini2;3\n",
                                "born_in_chaos_v1:lord_pumpkinhead;battlemusic:mini2;0\n",
                                "endergetic:brood_eetle;battlemusic:cauldron;2\n",
                                "aether:slider;battlemusic:mini2;2\n",
                                "aether:valkyrie;battlemusic:mini1;0\n",
                                "aether:sun_god;battlemusic:summit;3\n",
                                "aether:valkyrie_queen;battlemusic:mini2;2\n",
                                "cataclysm:ender_golem;battlemusic:mini2;0\n",
                                "cataclysm:ignited_revenant;battlemusic:mini2;0\n",
                                "cataclysm:ender_guardian;battlemusic:porcus_humungous;4\n",
                                "cataclysm:netherite_monstrosity;battlemusic:redstone_monstrosity;4\n",
                                "cataclysm:ignis;battlemusic:wildfire;4\n",
                                "cataclysm:the_harbinger;battlemusic:ancient;4\n",
                                "cataclysm:the_leviathan;battlemusic:ancient_guardian;4\n",
                                "cataclysm:the_prowler;battlemusic:mini1;0\n",
                                "cataclysm:amethyst_crab;battlemusic:mini2;0\n",
                                "cataclysm:coralssus;battlemusic:mini1;0\n",
                                "cataclysm:ancient_remnant;battlemusic:ascension;3\n",
                                "cataclysm:deepling_brute;battlemusic:mini1;0\n",
                                "mowziesmobs:ferrous_wroughtnaut;mowziesmobs:music.ferrous_wroughtnaut_theme;1\n",
                                "mowziesmobs:umvuthi;mowziesmobs:music.umvuthi_theme;2\n",
                                "mowziesmobs:frostmaw;mowziesmobs:music.frostmaw_theme;2\n",
                                "bosses_of_mass_destruction:void_blossom;battlemusic:arena2;1\n",
                                "bosses_of_mass_destruction:lich;battlemusic:boss;2\n",
                                "bosses_of_mass_destruction:gauntlet;battlemusic:kermetic;3\n",
                                "bosses_of_mass_destruction:obsidilith;battlemusic:broken_heart_of_ender;3\n",
                                "twilightforest:naga;battlemusic:arena2;2\n",
                                "twilightforest:lich;battlemusic:boss;3\n",
                                "twilightforest:hydra;battlemusic:arena1;3\n",
                                "twilightforest:minoshroom;battlemusic:mini1;0\n",
                                "twilightforest:alpha_yeti;battlemusic:mini2;1\n",
                                "twilightforest:phantom_knight;battlemusic:mini2;1\n",
                                "twilightforest:ur_ghast;battlemusic:metaluna;3\n",
                                "twilightforest:snow_queen;battlemusic:ascension;3\n",
                                "mutant_mobs:mutant_zombie;battlemusic:mini1;0\n",
                                "mutant_mobs:mutant_skeleton;battlemusic:mini1;0\n",
                                "mutant_mobs:mutant_creeper;battlemusic:mini2;0\n",
                                "mutant_mobs:mutant_enderman;battlemusic:shattered;1\n",
                                "mutant_more:mutant_blaze;battlemusic:mini2;1\n",
                                "mutant_more:mutant_shulker;battlemusic:mini2;1\n",
                                "mutant_more:mutant_wither_skeleton;battlemusic:mini1;0\n",
                                "rottencreatures:dead_beard;battlemusic:mini1;0\n",
                                "rottencreatures:immortal;battlemusic:mini1;0"
                                ), a -> true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
