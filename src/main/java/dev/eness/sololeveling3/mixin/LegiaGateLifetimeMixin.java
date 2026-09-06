package dev.eness.sololeveling3.mixin;

import dev.eness.sololeveling3.story.LegiaGateSupport;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.solocraft.entity.Portal1Entity;
import net.solocraft.procedures.PortalPerTickProcedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents the one-day gate timer/global reset from orphaning the Legia quest. */
@Mixin(value = PortalPerTickProcedure.class, remap = false)
public abstract class LegiaGateLifetimeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$keepStoryGateAlive(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            CallbackInfo ci
    ) {
        if (!LegiaGateSupport.isLegiaGate(entity)) {
            return;
        }

        entity.getPersistentData().putDouble("PortalLife", 0.0D);
        if (entity.getPersistentData().getBoolean(dev.eness.sololeveling3.campaign.CampaignGates.TAG)) {
            if (entity instanceof Portal1Entity gate) gate.setTexture("portalgate2");
            ci.cancel();
            return;
        }
        if (entity instanceof Portal1Entity gate) {
            gate.setTexture("portalgate2");
            if (gate.getEntityData().get(Portal1Entity.DATA_usedbefore)) {
                return;
            }
        }
        ci.cancel();
    }
}
