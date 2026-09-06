package dev.eness.sololeveling3.registry;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.item.OldKeyItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SoloLeveling3.MOD_ID);

    public static final RegistryObject<Item> OLD_KEY = ITEMS.register("old_key", OldKeyItem::new);

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
