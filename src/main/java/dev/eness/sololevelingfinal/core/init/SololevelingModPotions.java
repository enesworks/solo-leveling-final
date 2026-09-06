package dev.eness.sololevelingfinal.core.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SololevelingModPotions {
   public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(ForgeRegistries.POTIONS, "sololeveling");
   public static final RegistryObject<Potion> KASAKAS_VENOM = REGISTRY.register(
      "kasakas_venom",
      () -> new Potion(
         new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 0, false, false), new MobEffectInstance(MobEffects.POISON, 3600, 1, false, false)
      )
   );
}
