package com.nekotune.battlemusic;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.ArrayList;

import static com.nekotune.battlemusic.EntityMusic.*;

@Mod(BattleMusic.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class BattleMusic
{
    public static final String MOD_ID = "battlemusic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BattleMusic()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSounds.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModConfigs.SPEC, MOD_ID + "-client.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static final ArrayList<Mob> validEntities = new ArrayList<>();

    public static void reload() {
        updateEntitySoundData();
        validEntities.clear();
    }

    @Mod.EventBusSubscriber(modid = BattleMusic.MOD_ID)
    public static abstract class ForgeEvents
    {
        // Register commands
        @SubscribeEvent
        public static void onCommandRegister(RegisterCommandsEvent event) {
            ModCommands.register(event.getDispatcher());
        }

        // Update valid entities
        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity entity = event.getEntity();
            if (entity instanceof Mob) {
                if (isValidEntity((Mob)entity)) {
                    validEntities.add((Mob) entity);
                } else validEntities.remove(entity);
            }
        }

        // Update battle music
        @SubscribeEvent
        public static void onTick(TickEvent.LevelTickEvent event) {
            if (event.phase == TickEvent.Phase.START || event.side != LogicalSide.CLIENT) return;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            SoundData f_soundData = null;
            Mob f_entity = null;

            for (Mob entity : validEntities) {
                // Ensure entity is still valid
                if (!isValidEntity(entity)) {
                    validEntities.remove(entity);
                    continue;
                }

                SoundData soundData = getEntitySoundData().get(entity.getType());
                if (f_soundData != null) {
                    // Only overwrite final variables if priority is higher
                    if (f_soundData.priority() >= soundData.priority()) {
                        continue;
                    }
                    // Skip if the music is the same
                    if (f_soundData.soundEvent().getLocation().equals(soundData.soundEvent().getLocation())) {
                        continue;
                    }
                }

                if (playing != null) {
                    // Check if this entity's music is already playing
                    if (playing.ENTITY.getType().equals(entity.getType())) {
                        continue;
                    }
                    // Ensure this sound has higher priority than what's currently playing
                    if (playing.SOUND_DATA.priority() >= soundData.priority()) {
                        continue;
                    }
                }

                f_soundData = soundData;
                f_entity = entity;
            }

            // Play battle music
            if (f_soundData != null) {
                if (playing != null) {
                    playing.destroy();
                }
                playing = spawnInstance(f_soundData, f_entity);
            }

            if (playing != null) {
                updateMusic();
            }
        }
    }
}
