package com.nekotune.battlemusic.compat;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@OnlyIn(Dist.CLIENT)
public abstract class AetherCompat {
    public static final String ACTIVE_BOSS_TAG = "active_boss_aether";

    private static final String BOSS_MOB_CLASS = "com.aetherteam.nitrogen.entity.BossMob";
    private static final Class<?> bossMobClass;
    private static final Method isBossFightMethod;

    static {
        Class<?> clazz = null;
        Method method = null;

        try {
            clazz = Class.forName(BOSS_MOB_CLASS);
            method = clazz.getMethod("isBossFight");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            // The Aether isn't present; compatibility hooks remain inactive.
        }

        bossMobClass = clazz;
        isBossFightMethod = method;
    }

    public static boolean isBoss(Mob mob) {
        return bossMobClass != null && bossMobClass.isInstance(mob);
    }

    public static boolean isActiveBoss(Mob mob) {
        if (!isBoss(mob) || isBossFightMethod == null) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(isBossFightMethod.invoke(mob));
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }
}
