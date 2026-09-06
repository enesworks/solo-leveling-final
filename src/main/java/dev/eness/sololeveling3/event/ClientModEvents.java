package dev.eness.sololeveling3.event;

import dev.eness.sololeveling3.client.particle.MonarchParticle;
import dev.eness.sololeveling3.client.particle.RakanCombatParticle;
import dev.eness.sololeveling3.vfx.RakanVfxMesh;
import dev.eness.sololeveling3.client.renderer.IceMonarchRenderer;
import dev.eness.sololeveling3.client.renderer.MonarchOfGiantsRenderer;
import dev.eness.sololeveling3.client.renderer.TarnakRenderer;
import dev.eness.sololeveling3.client.renderer.RakanRenderer;
import dev.eness.sololeveling3.client.renderer.AntaresRenderer;
import dev.eness.sololeveling3.registry.ModEntities;
import dev.eness.sololeveling3.registry.ModParticles;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MONARCH_OF_GIANTS.get(), MonarchOfGiantsRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_MONARCH.get(), IceMonarchRenderer::new);
        event.registerEntityRenderer(ModEntities.TARNAK.get(), TarnakRenderer::new);
        event.registerEntityRenderer(ModEntities.RAKAN.get(), RakanRenderer::new);
        event.registerEntityRenderer(ModEntities.ANTARES.get(), AntaresRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.RAKAN_CLAW_RIGHT.get(), sprites -> new RakanCombatParticle.Provider(sprites, RakanVfxMesh.Style.CLAW_RIGHT));
        event.registerSpriteSet(ModParticles.RAKAN_CLAW_LEFT.get(), sprites -> new RakanCombatParticle.Provider(sprites, RakanVfxMesh.Style.CLAW_LEFT));
        event.registerSpriteSet(ModParticles.RAKAN_BLOCK.get(), sprites -> new RakanCombatParticle.Provider(sprites, RakanVfxMesh.Style.BLOCK));
        event.registerSpriteSet(ModParticles.RAKAN_COUNTER.get(), sprites -> new RakanCombatParticle.Provider(sprites, RakanVfxMesh.Style.COUNTER));
        event.registerSpriteSet(ModParticles.RAKAN_SLAM.get(), sprites -> new RakanCombatParticle.Provider(sprites, RakanVfxMesh.Style.SLAM));
        event.registerSpriteSet(ModParticles.RAKAN_ROAR.get(), sprites -> new RakanCombatParticle.Provider(sprites, RakanVfxMesh.Style.ROAR));
        event.registerSpriteSet(ModParticles.DARK_CHAIN.get(), sprites -> new MonarchParticle.Provider(sprites, MonarchParticle.Style.CHAIN));
        event.registerSpriteSet(ModParticles.FROST.get(), sprites -> new MonarchParticle.Provider(sprites, MonarchParticle.Style.FROST));
        event.registerSpriteSet(ModParticles.ICE_SPIKE.get(), sprites -> new MonarchParticle.Provider(sprites, MonarchParticle.Style.SPIKE));
        event.registerSpriteSet(ModParticles.CRIMSON_AURA.get(), sprites -> new MonarchParticle.Provider(sprites, MonarchParticle.Style.CRIMSON_AURA));
        event.registerSpriteSet(ModParticles.CRIMSON_IMPACT.get(), sprites -> new MonarchParticle.Provider(sprites, MonarchParticle.Style.CRIMSON_IMPACT));
        event.registerSpriteSet(ModParticles.CRIMSON_DUST.get(), sprites -> new MonarchParticle.Provider(sprites, MonarchParticle.Style.CRIMSON_DUST));
    }
}
