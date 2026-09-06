package dev.eness.sololeveling3;

import com.mojang.logging.LogUtils;
import dev.eness.sololeveling3.combat.GoGunheeCombatManager;
import dev.eness.sololeveling3.command.DevCommands;
import dev.eness.sololeveling3.diagnostics.StartupDiagnostics;
import dev.eness.sololeveling3.event.ClientModEvents;
import dev.eness.sololeveling3.event.CommonModEvents;
import dev.eness.sololeveling3.registry.ModEntities;
import dev.eness.sololeveling3.registry.ModItems;
import dev.eness.sololeveling3.registry.ModParticles;
import dev.eness.sololeveling3.campaign.CampaignDirector;
import dev.eness.sololeveling3.campaign.CampaignGates;
import dev.eness.sololeveling3.campaign.CampaignArenas;
import dev.eness.sololeveling3.campaign.CampaignSurvival;
import dev.eness.sololeveling3.campaign.TempleReturnEncounter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

public final class SoloLeveling3 {
    public static final String MOD_ID = "sololeveling3";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized;

    private SoloLeveling3() {
    }

    /**
     * Registers the private expansion on whichever JavaFML container is
     * currently being constructed. The standalone development entry point and
     * the monolithic SLR bootstrap mixin both call this method; the guard keeps
     * registration strictly single-shot.
     */
    public static synchronized void bootstrap() {
        if (initialized) {
            return;
        }

        initialized = true;
        try {
            IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
            ModEntities.register(modBus);
            ModItems.register(modBus);
            ModParticles.register(modBus);
            modBus.register(CommonModEvents.class);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientOnly::register);

            MinecraftForge.EVENT_BUS.register(CampaignDirector.class);
            MinecraftForge.EVENT_BUS.register(CampaignGates.class);
            MinecraftForge.EVENT_BUS.register(CampaignArenas.class);
            MinecraftForge.EVENT_BUS.register(CampaignSurvival.class);
            MinecraftForge.EVENT_BUS.register(TempleReturnEncounter.class);
            MinecraftForge.EVENT_BUS.register(GoGunheeCombatManager.class);
            MinecraftForge.EVENT_BUS.register(StartupDiagnostics.class);
            MinecraftForge.EVENT_BUS.register(DevCommands.class);

            GeckoLib.initialize();
            LOGGER.info("Solo Leveling 3 private expansion initialized");
        } catch (RuntimeException | Error exception) {
            initialized = false;
            throw exception;
        }
    }

    /** Kept isolated so dedicated servers never resolve client-only classes. */
    private static final class ClientOnly {
        private static void register() {
            FMLJavaModLoadingContext.get().getModEventBus().register(ClientModEvents.class);
            if (Boolean.getBoolean("sl3.antares.visualReview")) {
                MinecraftForge.EVENT_BUS.register(dev.eness.sololeveling3.client.AntaresVisualReview.class);
            }
        }
    }
}
