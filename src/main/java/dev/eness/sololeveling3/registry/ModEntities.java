package dev.eness.sololeveling3.registry;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.entity.IceMonarchEntity;
import dev.eness.sololeveling3.entity.MonarchOfGiantsEntity;
import dev.eness.sololeveling3.entity.RakanEntity;
import dev.eness.sololeveling3.entity.AntaresEntity;
import dev.eness.sololeveling3.entity.TarnakEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoloLeveling3.MOD_ID);

    public static final RegistryObject<EntityType<MonarchOfGiantsEntity>> MONARCH_OF_GIANTS = ENTITIES.register(
            "monarch_of_giants",
            () -> EntityType.Builder.of(MonarchOfGiantsEntity::new, MobCategory.MONSTER)
                    .sized(1.45F, 4.15F)
                    .clientTrackingRange(96)
                    .updateInterval(2)
                    .build(SoloLeveling3.MOD_ID + ":monarch_of_giants")
    );

    public static final RegistryObject<EntityType<IceMonarchEntity>> ICE_MONARCH = ENTITIES.register(
            "ice_monarch",
            () -> EntityType.Builder.of(IceMonarchEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.05F)
                    .clientTrackingRange(96)
                    .updateInterval(2)
                    .build(SoloLeveling3.MOD_ID + ":ice_monarch")
    );

    public static final RegistryObject<EntityType<TarnakEntity>> TARNAK = ENTITIES.register(
            "tarnak",
            () -> EntityType.Builder.of(TarnakEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build(SoloLeveling3.MOD_ID + ":tarnak")
    );

    public static final RegistryObject<EntityType<RakanEntity>> RAKAN = ENTITIES.register(
            "rakan",
            () -> EntityType.Builder.of(RakanEntity::new, MobCategory.MONSTER)
                    .sized(0.72F, 1.95F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build(SoloLeveling3.MOD_ID + ":rakan")
    );

    private ModEntities() {
    }

    public static final RegistryObject<EntityType<AntaresEntity>> ANTARES = ENTITIES.register("antares",
            () -> EntityType.Builder.of(AntaresEntity::new, MobCategory.MONSTER).sized(.85F, 2.95F)
                    .clientTrackingRange(96).updateInterval(1).fireImmune().build(SoloLeveling3.MOD_ID + ":antares"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
