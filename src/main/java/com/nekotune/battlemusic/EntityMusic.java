package com.nekotune.battlemusic;

import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
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
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

import static com.nekotune.battlemusic.BattleMusic.LOGGER;

@OnlyIn(Dist.CLIENT)
public abstract class EntityMusic
{
    public record SoundData(SoundEvent soundEvent, int priority){}
    public static final double MAX_SONG_RANGE = 256D;
    public static Instance playing = null;
    private static float masterVolume = ModConfigs.VOLUME.get().floatValue();

    // Static hashmap of what entities play what sounds
    private static final HashMap<EntityType<?>, SoundData> ENTITY_SOUND_DATA = new HashMap<>();
    public static void updateEntitySoundData() {
        ENTITY_SOUND_DATA.clear();
        List<? extends String> entityDataStrings = ModConfigs.ENTITIES_SONGS.get();
        final String ERROR_MSG = "Error loading entity music data from battlemusic config: ";
        for (String entityDataString : entityDataStrings) {
            EntityType<?> entityType = null;
            SoundEvent soundEvent = null;

            String entityString = entityDataString.substring(0, entityDataString.indexOf(';'));
            DataResult<ResourceLocation> weakEntityResource = ResourceLocation.read(entityString);
            if (weakEntityResource.get().left().isPresent()) {
                ResourceLocation resource = weakEntityResource.get().left().get();
                entityType = ForgeRegistries.ENTITY_TYPES.getValue(resource);
            }
            if (entityType == null || entityType == EntityType.PIG) {
                LOGGER.warn(ERROR_MSG + "Skipping invalid entity ID \"" + entityString + "\" (You can ignore this warning)");
                continue;
            }

            String soundString = entityDataString.substring(entityDataString.indexOf(';') + 1, entityDataString.lastIndexOf(';'));
            DataResult<ResourceLocation> weakSoundResource = ResourceLocation.read(soundString);
            if (weakSoundResource.get().left().isPresent()) {
                ResourceLocation resource = weakSoundResource.get().left().get();
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
            DataResult<ResourceLocation> weakDefaultSongResource = ResourceLocation.read(defaultSongString);
            if (weakDefaultSongResource.get().left().isPresent()) {
                ResourceLocation resource = weakDefaultSongResource.get().left().get();
                defaultSong = ForgeRegistries.SOUND_EVENTS.getValue(resource);
            }
            if (defaultSong == null) {
                LOGGER.error(ERROR_MSG + "Invalid default song sound ID \"" + defaultSongString + "\"");
            } else {
                for (EntityType<?> e : ForgeRegistries.ENTITY_TYPES.getValues()) {
                    ENTITY_SOUND_DATA.putIfAbsent(e, new SoundData(defaultSong, Integer.MIN_VALUE));
                }
            }
        }
        if (playing != null) {
            playing.destroy();
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

        playing.setVolume(playing.getVolume() * (newVolume / masterVolume));
        masterVolume = newVolume;
        if (playing.getVolume() > masterVolume) {
            playing.setVolume(masterVolume);
        }
    }

    public static float getMasterVolume() {
        return masterVolume;
    }

    public static boolean isValidEntity(Mob mob) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
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

    public static Instance spawnInstance(SoundData soundData, Mob entity) {
        return new Instance(soundData, entity);
    }

    static void updateMusic() {
        assert playing != null;

        // Mute all other music
        Minecraft.getInstance().getSoundManager().stop(null, SoundSource.MUSIC);

        // Fade
        if (playing.fadeLength > 0f) {
            if (!playing.fadeOut) {
                playing.setVolume(playing.getVolume() + getMasterVolume() /(playing.fadeLength *20));
                if (playing.getVolume() >= getMasterVolume()) {
                    playing.setVolume(getMasterVolume());
                    playing.fadeLength = 0f;
                }
            } else {
                if (isValidEntity(playing.ENTITY)) {
                    playing.fadeOut = false;
                } else {
                    playing.setVolume(playing.getVolume() - getMasterVolume() / (playing.fadeLength * 20));
                    if (playing.getVolume() <= 0) {
                        playing.destroy();
                    }
                }
                return;
            }
        } else if (!isValidEntity(playing.ENTITY)) {
            playing.fade(2);
        } else {
            playing.setVolume(getMasterVolume());
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // Pitch up music when at low health (unless fighting the warden)
        float pitch = 1f;
        boolean belowHpThreshold;
        if (ModConfigs.HEALTH_PITCH_PERCENT.get()) {
            belowHpThreshold = (player.getHealth()/player.getMaxHealth())*100 <= ModConfigs.HEALTH_PITCH_THRESH.get();
        } else {
            belowHpThreshold = player.getHealth() <= ModConfigs.HEALTH_PITCH_THRESH.get();
        }
        if (belowHpThreshold && !(playing.ENTITY instanceof Warden)) {
            pitch += ModConfigs.HEALTH_PITCH_AMOUNT.get();
        }

        // Pitch up music during second phase of dragon and wither fights
        if (ModConfigs.PHASE2_PITCH_AMOUNT.get() != 0) {
            if (playing.ENTITY instanceof EnderDragon) {
                List<EndCrystal> list = playing.ENTITY.level().getEntitiesOfClass(EndCrystal.class, AABB.ofSize(new Vec3(0, 64, 0), 128, 128, 128));
                if (list.isEmpty()) {
                    pitch += ModConfigs.PHASE2_PITCH_AMOUNT.get();
                }
            } else if (playing.ENTITY instanceof WitherBoss && ((WitherBoss) playing.ENTITY).isPowered()) {
                pitch += ModConfigs.PHASE2_PITCH_AMOUNT.get();
            }
        }

        // Apply pitch changes
        playing.setPitch(pitch);
    }

    protected static class Instance extends AbstractSoundInstance
    {
        // Fields
        public final SoundData SOUND_DATA;
        public final Mob ENTITY;
        public float fadeLength;
        public boolean fadeOut = false;

        // Constructors
        public Instance(SoundData soundData, Mob entity) {
            super(ModSounds.MINI1.get(), SoundSource.NEUTRAL, RandomSource.create());
            this.looping = true;
            this.relative = true;
            this.SOUND_DATA = soundData;
            this.ENTITY = entity;

            float fadeInSeconds = ModConfigs.FADE_TIME.get().floatValue();
            this.volume = (fadeInSeconds == 0) ? EntityMusic.masterVolume : 0f;
            this.fadeLength = fadeInSeconds;

            Minecraft.getInstance().getSoundManager().play(this);
        }

        // Methods
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
            Minecraft.getInstance().getSoundManager().stop(this);
            playing = null;
        }

        public void fade(float seconds) {
            this.fadeOut = true;
            if (seconds == 0) {
                this.destroy();
            } else {
                this.fadeLength = seconds;
            }
        }
    }
}
