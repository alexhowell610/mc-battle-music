package com.nekotune.battlemusic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

import static com.nekotune.battlemusic.BattleMusic.LOGGER;

@OnlyIn(Dist.CLIENT)
public abstract class EntityMusic
{
    public static class SoundData {
        private final SoundEvent soundEvent;
        private final int priority;

        public SoundData(SoundEvent soundEvent, int priority) {
            this.soundEvent = soundEvent;
            this.priority = priority;
        }
        
        public SoundEvent soundEvent() {
            return this.soundEvent;
        }
        public int priority() {
            return this.priority;
        }
    }
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
        final String ERROR_MSG = "Error loading entity music data from battlemusic config: ";
        for (String entityDataString : entityDataStrings) {
            EntityType<?> entityType = null;
            net.minecraft.util.SoundEvent soundEvent = null;

            String entityString = entityDataString.substring(0, entityDataString.indexOf(';'));
            if (ResourceLocation.isValidResourceLocation(entityString)) {
                ResourceLocation resource = ResourceLocation.of(entityString, ':');
                entityType = ForgeRegistries.ENTITIES.getValue(resource);
            }
            if (entityType == null || entityType == EntityType.PIG) {
                LOGGER.warn(ERROR_MSG + "Skipping invalid entity ID \"" + entityString + "\"");
                continue;
            }

            String soundString = entityDataString.substring(entityDataString.indexOf(';') + 1, entityDataString.lastIndexOf(';'));
            if (ResourceLocation.isValidResourceLocation(soundString)) {
                ResourceLocation resource = ResourceLocation.of(soundString, ':');
                soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(resource);
            }
            if (soundEvent == null) {
                LOGGER.error(ERROR_MSG + "Invalid sound ID \"" + soundString + "\" in line \"" + entityDataString + "\", skipping");
                continue;
            }

            int priority = 0;
            String priorityString = entityDataString.substring(entityDataString.lastIndexOf(';') + 1, entityDataString.lastIndexOf(';') + 2);
            try {
                priority = Integer.parseInt(priorityString);
            } catch(Exception e) {
                LOGGER.error(ERROR_MSG + "Invalid priority \"" + priorityString + "\" in line \"" + entityDataString + "\", defaulting to 0");
            }

            LOGGER.debug("Added battle music " + soundEvent.getLocation() + " to " + entityType + " with priority " + priority);
            ENTITY_SOUND_DATA.put(entityType, new SoundData(soundEvent, priority));
        }

        String defaultSongString = ModConfigs.DEFAULT_SONG.get();
        if (!defaultSongString.isEmpty()) {
            SoundEvent defaultSong = null;
            if (ResourceLocation.isValidResourceLocation(defaultSongString)) {
                ResourceLocation resource = ResourceLocation.of(defaultSongString, ':');
                defaultSong = ForgeRegistries.SOUND_EVENTS.getValue(resource);
            }
            if (defaultSong == null) {
                LOGGER.error(ERROR_MSG + "Invalid default song sound ID \"" + defaultSongString + "\"");
            } else {
                for (EntityType<?> e : ForgeRegistries.ENTITIES.getValues()) {
                    ENTITY_SOUND_DATA.putIfAbsent(e, new SoundData(defaultSong, Integer.MIN_VALUE));
                }
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
            newVolume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundCategory.MUSIC);
        }
        for(EntityMusicInstance instance : getInstances().values()) {
            instance.setVolume(instance.getVolume() * (newVolume / masterVolume));
            masterVolume = newVolume;
            if (instance.getVolume() > masterVolume) {
                instance.setVolume(masterVolume);
            }
        }
    }

    public static boolean isValidEntity(MobEntity mob) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        assert player != null;
        if (ENTITY_SOUND_DATA.get(mob.getType()) != null && mob.level.equals(player.level) &&
                !mob.isDeadOrDying() && !mob.isSleeping() && !mob.isAlliedTo(player) && !mob.isNoAi()
                && !(mob instanceof IAngerable && !((IAngerable) mob).isAngryAt(player))) {
            if (mob.getTarget() instanceof PlayerEntity) { return true; }
            ModifiableAttributeInstance frAttribute = mob.getAttribute(Attributes.FOLLOW_RANGE);
            double followRange = (frAttribute != null) ? frAttribute.getValue() : MAX_SONG_RANGE;
            if (mob instanceof EnderDragonEntity) {
                followRange = 300; // Because the ender dragon is special
            }
            return mob.canAttack(player, EntityPredicate.DEFAULT.range(followRange).allowUnseeable().ignoreInvisibilityTesting());
        }
        return false;
    }

    public static void spawnInstance(SoundData soundData, ClientPlayerEntity player, MobEntity entity, Float fadeInSeconds) {
        if (fadeInSeconds == null) {
            fadeInSeconds = ModConfigs.FADE_TIME.get().floatValue();
        }
        EntityMusicInstance entityMusicInstance = new EntityMusicInstance(soundData, player, entity, fadeInSeconds);
        INSTANCES.put(soundData.soundEvent, entityMusicInstance);
        Minecraft.getInstance().getSoundManager().queueTickingSound(entityMusicInstance);
    }

    public static class EntityMusicInstance extends TickableSound {
        // Fields
        public final SoundData SOUND_DATA;
        public final ClientPlayerEntity PLAYER;
        public final MobEntity ENTITY;
        private float fadeSeconds;
        private boolean fadingIn;

        // Constructors
        public EntityMusicInstance(SoundData soundData, ClientPlayerEntity player, MobEntity entity, float fadeInSeconds) {
            super(soundData.soundEvent, SoundCategory.NEUTRAL);
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
            SoundHandler soundHandler = Minecraft.getInstance().getSoundManager();
            soundHandler.stop(null, SoundCategory.MUSIC);

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
            boolean belowHpThreshold;
            if (ModConfigs.HEALTH_PITCH_PERCENT.get()) {
                belowHpThreshold = (this.PLAYER.getHealth()/this.PLAYER.getMaxHealth())*100 <= ModConfigs.HEALTH_PITCH_THRESH.get();
            } else {
                belowHpThreshold = this.PLAYER.getHealth() <= ModConfigs.HEALTH_PITCH_THRESH.get();
            }
            if (belowHpThreshold) {
                pitchMod += ModConfigs.HEALTH_PITCH_AMOUNT.get();
            }
            // Pitch up music during second phase of dragon fight
            if (this.ENTITY instanceof EnderDragonEntity && ((EnderDragonEntity)this.ENTITY).nearestCrystal == null) {
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
            SoundHandler soundHandler = Minecraft.getInstance().getSoundManager();
            soundHandler.stop(this);
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
