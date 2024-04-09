package com.nekotune.battlemusic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds
{
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BattleMusic.MOD_ID);

    public static final RegistryObject<SoundEvent> ENDER_DRAGON = registerSoundEvent("ender_dragon");
    public static final RegistryObject<SoundEvent> WARDEN = registerSoundEvent("warden");
    public static final RegistryObject<SoundEvent> WITHER = registerSoundEvent("wither");
    public static final RegistryObject<SoundEvent> MINI = registerSoundEvent("mini");
    // Endergetic
    public static final RegistryObject<SoundEvent> BROOD_EETLE = registerSoundEvent("brood_eetle");
    // L_Ender's Cataclysm
    public static final RegistryObject<SoundEvent> IGNIS = registerSoundEvent("ignis");
    public static final RegistryObject<SoundEvent> ENDER_GUARDIAN = registerSoundEvent("ender_guardian");
    public static final RegistryObject<SoundEvent> HARBINGER = registerSoundEvent("harbinger");
    public static final RegistryObject<SoundEvent> NETHERITE_MONSTROSITY = registerSoundEvent("netherite_monstrosity");
    // Twilight Forest
    public static final RegistryObject<SoundEvent> HYDRA = registerSoundEvent("hydra");
    public static final RegistryObject<SoundEvent> LICH = registerSoundEvent("lich");
    public static final RegistryObject<SoundEvent> NAGA = registerSoundEvent("naga");
    public static final RegistryObject<SoundEvent> UR_GHAST = registerSoundEvent("ur_ghast");
    public static final RegistryObject<SoundEvent> SNOW_QUEEN = registerSoundEvent("snow_queen");
    // Aether
    public static final RegistryObject<SoundEvent> SUN_GOD = registerSoundEvent("sun_god");
    public static final RegistryObject<SoundEvent> SLIDER = registerSoundEvent("slider");
    // Mutant Creatures
    public static final RegistryObject<SoundEvent> MUTANT_ENDERMAN = registerSoundEvent("mutant_enderman");
    // Alex's Mobs
    public static final RegistryObject<SoundEvent> VOID_WORM = registerSoundEvent("void_worm");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(BattleMusic.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> new SoundEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
