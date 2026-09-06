package dev.eness.sololevelingfinal.core.mixin;

import dev.eness.sololevelingfinal.core.campaign.TempleReturnEncounter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StatueOfGodEntity.class, remap = false)
public abstract class StatueOfGodCampaignDamageMixin extends Monster {
    protected StatueOfGodCampaignDamageMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Group(name = "campaignGodHurt", min = 1, max = 1)
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sololeveling3$allowCampaignPlayerDamageDev(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        sololeveling3$allowCampaignPlayerDamage(source, amount, cir);
    }

    @Group(name = "campaignGodHurt", min = 1, max = 1)
    @Inject(method = "m_6469_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sololeveling3$allowCampaignPlayerDamageProduction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        sololeveling3$allowCampaignPlayerDamage(source, amount, cir);
    }

    private void sololeveling3$allowCampaignPlayerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (TempleReturnEncounter.shouldBypassGodHurt((StatueOfGodEntity) (Object) this, source)) {
            cir.setReturnValue(super.hurt(source, TempleReturnEncounter.campaignGodDamage(amount)));
        }
    }
}
