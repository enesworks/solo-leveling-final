package dev.eness.sololeveling3.mixin;

import dev.eness.sololeveling3.campaign.TempleReturnEncounter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.solocraft.util.CartenonTempleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CartenonTempleManager.class, remap = false)
public abstract class CartenonTempleManagerCampaignBypassMixin {
    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$skipNativeTick(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (event.player instanceof ServerPlayer player && TempleReturnEncounter.shouldBypassNativeTempleHandler(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onLivingDamage", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$skipNativeDamage(LivingDamageEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof ServerPlayer player && TempleReturnEncounter.shouldBypassNativeTempleHandler(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onLivingDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$skipNativeDeath(LivingDeathEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof ServerPlayer player && TempleReturnEncounter.shouldBypassNativeTempleHandler(player)) {
            ci.cancel();
        }
    }
}