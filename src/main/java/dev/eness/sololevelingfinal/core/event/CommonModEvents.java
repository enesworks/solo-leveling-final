package dev.eness.sololevelingfinal.core.event;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.entity.IceMonarchEntity;
import dev.eness.sololevelingfinal.core.entity.MonarchOfGiantsEntity;
import dev.eness.sololevelingfinal.core.entity.TarnakEntity;
import dev.eness.sololevelingfinal.core.entity.RakanEntity;
import dev.eness.sololevelingfinal.core.entity.AntaresEntity;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommonModEvents {
    private CommonModEvents() {
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CommonModEvents::assertFinalSingleMod);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MONARCH_OF_GIANTS.get(), MonarchOfGiantsEntity.createAttributes().build());
        event.put(ModEntities.ICE_MONARCH.get(), IceMonarchEntity.createAttributes().build());
        event.put(ModEntities.TARNAK.get(), TarnakEntity.createAttributes().build());
        event.put(ModEntities.RAKAN.get(), RakanEntity.createAttributes().build());
        event.put(ModEntities.ANTARES.get(), AntaresEntity.createAttributes().build());
    }

    private static void assertFinalSingleMod() {
        if (!Boolean.getBoolean("sl3.final.singleMod")) {
            return;
        }

        Set<String> expectedIds = Set.of("minecraft", "forge", "sololeveling");
        Set<String> actualIds = ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String visible = ModList.get().getMods().stream()
                .map(info -> info.getModId() + "=" + info.getDisplayName())
                .collect(Collectors.joining(", "));
        SoloLeveling3.LOGGER.info("SL3 final single-mod visible containers: {}", visible);

        if (!actualIds.equals(expectedIds)) {
            throw new IllegalStateException("Expected final visible mods " + expectedIds + " but found " + actualIds + " (" + visible + ")");
        }

        String displayName = ModList.get().getModContainerById("sololeveling")
                .map(container -> container.getModInfo().getDisplayName())
                .orElse("");
        if (!"Solo Leveling Final".equals(displayName)) {
            throw new IllegalStateException("Expected Solo Leveling Final display name for sololeveling but found " + displayName);
        }

        if (actualIds.contains("geckolib") || actualIds.contains(SoloLeveling3.MOD_ID)) {
            throw new IllegalStateException("Final build exposed addon/library mod container: " + actualIds);
        }
    }
}
