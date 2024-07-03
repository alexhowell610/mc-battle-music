package com.nekotune.battlemusic.compat;

import com.aetherteam.nitrogen.entity.BossMob;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AetherCompat {
    public final static String ACTIVE_BOSS_TAG = "active_boss_aether";

    public static boolean isBoss(Mob mob) {
        return mob instanceof BossMob;
    }

    public static boolean isActiveBoss(Mob mob) {
        if (mob instanceof BossMob) {
            return ((BossMob<?>)mob).isBossFight();
        }
        return false;
    }
}
