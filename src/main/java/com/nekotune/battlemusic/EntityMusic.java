package com.nekotune.battlemusic;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;

@OnlyIn(Dist.CLIENT)
public abstract class EntityMusic
{
    public record SoundData(SoundEvent soundEvent, int priority){}
    public static final double MAX_SONG_RANGE = 256D;
    private static float masterVolume = ModConfigs.VOLUME.get().floatValue();

    // Hashmap of all currently running entity music instances
    private static final HashMap<SoundEvent, EntityMusicInstance> INSTANCES = new HashMap<>();
    public static boolean isPlaying(SoundEvent soundEvent) {
        return (INSTANCES.get(soundEvent) != null);
    }
    public static HashMap<SoundEvent, EntityMusicInstance> getInstances() {
        return cloneHashMap(INSTANCES);
    }

    // Static hashmap of what entities play what sounds
    private static final HashMap<EntityType<?>, SoundData> ENTITY_SOUND_DATA = new HashMap<>();
    public static void updateEntitySoundData() {
        ENTITY_SOUND_DATA.clear();
        List<? extends String> entityDataStrings = ModConfigs.ENTITIES_SONGS.get();
        for (String entityDataString : entityDataStrings) {
            try {
                ResourceLocation entityResource = ResourceLocation.read(entityDataString.substring(0, entityDataString.indexOf(';')))
                        .getOrThrow(false, Util.prefix("Encountered error while loading entity music data (entity)", LOGGER::warn));
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityResource);
                if (entityType == EntityType.PIG) { continue; }
                ResourceLocation soundResource = ResourceLocation.read(entityDataString.substring(entityDataString.indexOf(';') + 1, entityDataString.lastIndexOf(';')))
                        .getOrThrow(false, Util.prefix("Encountered error while loading entity music data (sound)", LOGGER::warn));
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundResource);
                int priority = Integer.parseInt(entityDataString.substring(entityDataString.lastIndexOf(';')+1));
                ENTITY_SOUND_DATA.put(entityType, new SoundData(soundEvent, priority));
            } catch (Exception e) {
                LOGGER.warn("Error loading entity music data from battlemusic config: " + e.getMessage());
            }
        }
        for (EntityMusic.EntityMusicInstance instance : INSTANCES.values()) {
            instance.destroy();
        }
    }
    static {
        updateEntitySoundData();
    }

    public static <T, K> HashMap<T, K> cloneHashMap(HashMap<T, K> hashMap) {
        HashMap<T, K> clone = new HashMap<>();
        for (T key : hashMap.keySet()) {
            clone.put(key, hashMap.get(key));
        }
        return clone;
    }
    public static HashMap<EntityType<?>, SoundData> getEntitySoundData() {
        return cloneHashMap(ENTITY_SOUND_DATA);
    }

    public static void setMasterVolume(float newVolume) {
        if (ModConfigs.LINKED_TO_MUSIC.get()) {
            newVolume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC);
        }
        for(EntityMusicInstance instance : getInstances().values()) {
            instance.setVolume(instance.getVolume() * (newVolume / masterVolume));
            masterVolume = newVolume;
            if (instance.getVolume() > masterVolume) {
                instance.setVolume(masterVolume);
            }
        }
    }

    public static boolean isValidEntity(Mob mob) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        if (ENTITY_SOUND_DATA.get(mob.getType()) != null && mob.level().equals(player.level()) &&
                !mob.isDeadOrDying() && !mob.isSleeping() && !mob.isAlliedTo(player.self()) && !mob.isNoAi()
                && !(mob instanceof NeutralMob && !((NeutralMob) mob).isAngryAt(player))) {
            if (mob.getTarget() instanceof Player) { return true; }
            AttributeInstance frAttribute = mob.getAttribute(Attributes.FOLLOW_RANGE);
            double followRange = (frAttribute != null) ? frAttribute.getValue() : MAX_SONG_RANGE;
            if (mob instanceof EnderDragon) {
                followRange = 300; // Because the ender dragon is special
            }
            return mob.canAttack(player, TargetingConditions.forCombat().range(followRange).ignoreLineOfSight().ignoreInvisibilityTesting());
        }
        return false;
    }

    public static void spawnInstance(SoundData soundData, LocalPlayer player, Mob entity, @Nullable Float fadeInSeconds) {
        if (fadeInSeconds == null) {
            fadeInSeconds = ModConfigs.FADE_TIME.get().floatValue();
        }
        EntityMusicInstance entityMusicInstance = new EntityMusicInstance(soundData, player, entity, fadeInSeconds);
        INSTANCES.put(soundData.soundEvent, entityMusicInstance);
        Minecraft.getInstance().getSoundManager().queueTickingSound(entityMusicInstance);
    }

    public static class EntityMusicInstance extends AbstractTickableSoundInstance
    {
        // Fields
        public final SoundData SOUND_DATA;
        public final LocalPlayer PLAYER;
        public final Mob ENTITY;
        private float fadeSeconds;
        private boolean fadingIn;

        // Constructors
        public EntityMusicInstance(@NotNull SoundData soundData, LocalPlayer player, Mob entity, float fadeInSeconds) {
            super(soundData.soundEvent, SoundSource.NEUTRAL, RandomSource.create());
            this.looping = true;
            this.relative = true;
            this.SOUND_DATA = soundData;
            this.PLAYER = player;
            this.ENTITY = entity;
            this.volume = (fadeInSeconds == 0) ? EntityMusic.masterVolume : 0f;
            this.fadeSeconds = fadeInSeconds;
            this.fadingIn = (fadeInSeconds > 0);
        }

        // Methods
        @Override
        public void tick() {
            if (this.isStopped()) return;
            // Mute all other music
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            soundManager.stop(null, SoundSource.MUSIC);
            // Fade
            if (this.fadeSeconds > 0f) {
                if (this.fadingIn) {
                    this.volume += EntityMusic.masterVolume /(this.fadeSeconds*20);
                    if (this.volume >= EntityMusic.masterVolume) {
                        this.volume = EntityMusic.masterVolume;
                        this.fadeSeconds = 0f;
                        this.fadingIn = false;
                    }
                } else {
                    this.volume -= EntityMusic.masterVolume /(this.fadeSeconds*20);
                    if (this.volume <= 0f) {
                        this.destroy();
                    }
                }
            } else {
                // If entity is no longer valid for playing music to the player, fade out the sound
                if (!isValidEntity(this.ENTITY)) {
                    fadeOut(2);
                }
            }
            float pitchMod = 1f;
            // Pitch up music when at low health (unless fighting the warden)
            if (this.PLAYER.getHealth() <= ModConfigs.HEALTH_PITCH_THRESH.get() && !(this.ENTITY instanceof Warden)) {
                pitchMod += ModConfigs.HEALTH_PITCH_AMOUNT.get();
            }
            // Pitch up music during second phase of dragon fight
            if (this.ENTITY instanceof EnderDragon && ((EnderDragon)this.ENTITY).nearestCrystal == null) {
                pitchMod += ModConfigs.DRAGON_PITCH_AMOUNT.get();
            }
            this.pitch = pitchMod;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        public void setVolume(float volume) {
            this.volume = volume;
        }
        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public void destroy() {
            this.stop();
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            soundManager.stop(this);
            INSTANCES.remove(this.SOUND_DATA.soundEvent);
        }

        public void fadeOut(float seconds) {
            this.fadingIn = false;
            this.fadeSeconds = seconds;
            if (seconds == 0) {
                this.destroy();
            }
        }
    }
}
