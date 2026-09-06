package dev.eness.sololevelingfinal.core.registry;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SoloLeveling3.MOD_ID);

    public static final RegistryObject<SimpleParticleType> DARK_CHAIN =
            PARTICLES.register("dark_chain", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FROST =
            PARTICLES.register("frost", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ICE_SPIKE =
            PARTICLES.register("ice_spike", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CRIMSON_AURA =
            PARTICLES.register("crimson_aura", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CRIMSON_IMPACT =
            PARTICLES.register("crimson_impact", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CRIMSON_DUST =
            PARTICLES.register("crimson_dust", () -> new SimpleParticleType(false));

    private ModParticles() {
    }

    public static final RegistryObject<SimpleParticleType> RAKAN_CLAW_RIGHT = PARTICLES.register("rakan_claw_right", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAKAN_CLAW_LEFT = PARTICLES.register("rakan_claw_left", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAKAN_BLOCK = PARTICLES.register("rakan_block", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAKAN_COUNTER = PARTICLES.register("rakan_counter", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAKAN_SLAM = PARTICLES.register("rakan_slam", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAKAN_ROAR = PARTICLES.register("rakan_roar", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
