package com.nekotune.battlemusic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class ModSounds
{
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BattleMusic.MOD_ID);

    public static final RegistryObject<SoundEvent> ENDERMAN = registerSoundEvent("enderman");
    public static final RegistryObject<SoundEvent> NECROMANCER = registerSoundEvent("necromancer");
    public static final RegistryObject<SoundEvent> WITHER_STORM = registerSoundEvent("wither_storm");
    public static final RegistryObject<SoundEvent> MINI1 = registerSoundEvent("mini1");
    public static final RegistryObject<SoundEvent> MINI2 = registerSoundEvent("mini2");
    public static final RegistryObject<SoundEvent> CAULDRON = registerSoundEvent("cauldron");
    public static final RegistryObject<SoundEvent> ANCIENT = registerSoundEvent("ancient");
    public static final RegistryObject<SoundEvent> ANCIENT_GUARDIAN = registerSoundEvent("ancient_guardian");
    public static final RegistryObject<SoundEvent> ARENA1 = registerSoundEvent("arena1");
    public static final RegistryObject<SoundEvent> ARENA2 = registerSoundEvent("arena2");
    public static final RegistryObject<SoundEvent> ASCENSION = registerSoundEvent("ascension");
    // Twilight Forest
    public static final RegistryObject<SoundEvent> BOSS = registerSoundEvent("boss");
    public static final RegistryObject<SoundEvent> BROKEN_HEART_OF_ENDER = registerSoundEvent("broken_heart_of_ender");
    public static final RegistryObject<SoundEvent> GHAST = registerSoundEvent("ghast");
    public static final RegistryObject<SoundEvent> KERMETIC = registerSoundEvent("kermetic");
    public static final RegistryObject<SoundEvent> MENTA_MENARDI = registerSoundEvent("menta_menardi");
    // Aether
    public static final RegistryObject<SoundEvent> METALUNA = registerSoundEvent("metaluna");
    public static final RegistryObject<SoundEvent> PORCUS_HUMUNGOUS = registerSoundEvent("porcus_humungous");
    // Mutant Creatures
    public static final RegistryObject<SoundEvent> REDSTONE_MONSTROSITY = registerSoundEvent("redstone_monstrosity");
    // Alex's Mobs
    public static final RegistryObject<SoundEvent> SHATTERED = registerSoundEvent("shattered");
    public static final RegistryObject<SoundEvent> SUMMIT = registerSoundEvent("summit");
    public static final RegistryObject<SoundEvent> WILDFIRE = registerSoundEvent("wildfire");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(BattleMusic.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
