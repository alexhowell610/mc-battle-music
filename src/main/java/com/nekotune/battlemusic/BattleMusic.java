package com.nekotune.battlemusic;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BattleMusic.MOD_ID)
public class BattleMusic
{
    public static final String MOD_ID = "battlemusic";
    public static final Logger LOGGER = LogManager.getLogger();

    public BattleMusic()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSounds.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModConfigs.SPEC, MOD_ID + "-client.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }
}
