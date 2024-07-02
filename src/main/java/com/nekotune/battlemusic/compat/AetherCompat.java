package com.nekotune.battlemusic.compat;

import com.aetherteam.nitrogen.entity.BossMob;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AetherCompat {
    public static boolean isBoss(Mob mob) {
        return mob instanceof BossMob;
    }
    public static boolean isActiveBoss(Mob mob) {
        assert mob instanceof BossMob<?>;
        return ((BossMob<?>)mob).isBossFight();
    }
}
