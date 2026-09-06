package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.ShadowKaiselinEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SilladIcePrisonManager;

@EventBusSubscriber
public class Ability3ResetProcedure {
   private static final String SHADOW_OWNER = "sl_shadow_owner";

   @SubscribeEvent
   public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB == 1.0
            )
          {
            if (event == null && entity instanceof Player player && SilladIcePrisonManager.guardManualDismiss(player)) {
               return;
            }

            boolean keepRiddenKaisel = event == null && isRidingOwnedKaisel(entity);
            resetShadowCounters(entity, keepRiddenKaisel);
            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(200.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               boolean ownedTameShadow = entityiterator instanceof TamableAnimal _tamIsTamedBy
                  && entity instanceof LivingEntity _livEnt
                  && _tamIsTamedBy.isOwnedBy(_livEnt);
               boolean ownedTaggedShadow = entityiterator.getPersistentData().hasUUID("sl_shadow_owner")
                  && entityiterator.getPersistentData().getUUID("sl_shadow_owner").equals(entity.getUUID());
               if ((!keepRiddenKaisel || entityiterator != entity.getVehicle())
                  && entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
                  && (ownedTameShadow || ownedTaggedShadow)
                  && !entityiterator.level().isClientSide()) {
                  ShadowMonarchManager.saveBossHealthBeforeDespawn(entity, entityiterator);
                  ShadowMonarchManager.dropStoredShadowInventory(entityiterator);
                  entityiterator.discard();
               }
            }
         }
      }
   }

   private static boolean isRidingOwnedKaisel(Entity entity) {
      Entity vehicle = entity.getVehicle();
      return vehicle instanceof ShadowKaiselinEntity
         && vehicle.getPersistentData().hasUUID("sl_shadow_owner")
         && vehicle.getPersistentData().getUUID("sl_shadow_owner").equals(entity.getUUID());
   }

   private static void resetShadowCounters(Entity entity, boolean keepRiddenKaisel) {
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.OrdShadow = 0.0;
         capability.GobShadow = 0.0;
         capability.WolfShadow = 0.0;
         capability.IgrisSpawned = 0.0;
         capability.orcspawned = 0.0;
         capability.ShadowGoblinArcherAmount = 0.0;
         capability.ShadowGoblinMageAmount = 0.0;
         capability.beru = 0.0;
         capability.summonlimitusage = 0.0;
         capability.polarbear = 0.0;
         capability.shadowdragonnum = 0.0;
         capability.highorcspawned = 0.0;
         capability.tuskspawned = 0.0;
         if (keepRiddenKaisel) {
            capability.KaiselSpawned = Math.max(1.0, capability.KaiselSpawned);
         } else {
            capability.KaiselSpawned = 0.0;
         }

         capability.syncPlayerVariables(entity);
      });
   }
}
