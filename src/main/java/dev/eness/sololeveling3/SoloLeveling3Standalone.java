package dev.eness.sololeveling3;

import net.minecraftforge.fml.common.Mod;

/** Development/addon entry point; excluded from the one-mod distribution. */
@Mod(SoloLeveling3.MOD_ID)
public final class SoloLeveling3Standalone {
    public SoloLeveling3Standalone() {
        SoloLeveling3.bootstrap();
    }
}
