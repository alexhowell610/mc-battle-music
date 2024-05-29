package com.nekotune.battlemusic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public abstract class ModEvents
{
    @Mod.EventBusSubscriber(modid = BattleMusic.MOD_ID)
    public static class ForgeEvents
    {
        // Register commands
        @SubscribeEvent
        public static void onCommandRegister(RegisterCommandsEvent event) {
            ModCommands.register(event.getDispatcher());
        }

        // Update battle music
        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity entity = event.getEntity();
            Level level = entity.level;
            LocalPlayer player = Minecraft.getInstance().player;
            if (level.isClientSide() && player != null && entity instanceof Mob && EntityMusic.isValidEntity((Mob)entity)) {
                // Check if music bound to current entity type is already playing and if entity can see the player
                EntityMusic.SoundData soundData = EntityMusic.getEntitySoundData().get(entity.getType());
                if (soundData != null && !EntityMusic.isPlaying(soundData.soundEvent())) {
                    // Check if another entity with higher or equal priority is already playing battle music
                    boolean fail = false;
                    ArrayList<EntityMusic.EntityMusicInstance> lowerPriorityInstances = new ArrayList<>();
                    for (EntityMusic.EntityMusicInstance entityMusicInstance : EntityMusic.getInstances().values()) {
                        // Ignore and remove invalid music instances
                        if (!EntityMusic.isValidEntity(entityMusicInstance.ENTITY)) {
                            entityMusicInstance.destroy();
                            continue;
                        }
                        if (entityMusicInstance.SOUND_DATA.priority() >= soundData.priority()) {
                            fail = true;
                            break;
                        } else {
                            lowerPriorityInstances.add(entityMusicInstance);
                        }
                    }
                    if (!fail) {
                        // Fade out all lower priorities
                        lowerPriorityInstances.forEach(instance -> instance.fadeOut(ModConfigs.FADE_TIME.get().floatValue()));
                        // Play battle music
                        EntityMusic.spawnInstance(soundData, player, (Mob)entity, ModConfigs.FADE_TIME.get().floatValue());
                    }
                }
            }
        }
    }
}