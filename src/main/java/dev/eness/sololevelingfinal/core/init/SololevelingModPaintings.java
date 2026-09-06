package dev.eness.sololevelingfinal.core.init;

import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SololevelingModPaintings {
   public static final DeferredRegister<PaintingVariant> REGISTRY = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, "sololeveling");
   public static final RegistryObject<PaintingVariant> AHJIN = REGISTRY.register("ahjin", () -> new PaintingVariant(32, 32));
   public static final RegistryObject<PaintingVariant> AHJIN_2 = REGISTRY.register("ahjin_2", () -> new PaintingVariant(32, 32));
}
