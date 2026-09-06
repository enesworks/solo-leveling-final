package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.FrostMonarchManager;
import dev.eness.sololevelingfinal.core.util.RulersAuthorityManager;
import dev.eness.sololevelingfinal.core.util.SilladIcePrisonManager;

public class Ability2OnKeyPressedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(world, x, y, z, entity, 0, 0);
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, int inputType, int pressedMs) {
      if (entity != null) {
         if (!FrostMonarchManager.isDirectAbilityMode(entity) && (inputType != 1 || !FrostMonarchManager.hasActiveFrozenPath(entity))) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).combatmode
               )
             {
               if (RulersAuthorityManager.hasAbility(entity)) {
                  if (entity instanceof ServerPlayer player) {
                     if (inputType == 0) {
                        RulersAuthorityManager.begin(player);
                     } else if (inputType == 1) {
                        RulersAuthorityManager.release(player, pressedMs);
                     } else if (inputType == 2) {
                        RulersAuthorityManager.adjustDistance(player, pressedMs);
                     }
                  }
               } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("You have to unlock this ability first!"), true);
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 1.0) {
               if (entity instanceof Player player && SilladIcePrisonManager.guardManualDismiss(player)) {
                  return;
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(200.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator instanceof TamableAnimal _tamIsTamedBy
                     && entity instanceof LivingEntity _livEnt
                     && _tamIsTamedBy.isOwnedBy(_livEnt)
                     && entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
                     && !entityiterator.level().isClientSide()) {
                     entityiterator.discard();
                  }
               }

               double _setval = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.OrdShadow = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.GobShadow = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.WolfShadow = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.IgrisSpawned = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.orcspawned = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.ShadowGoblinArcherAmount = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.ShadowGoblinArcherAmount = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.ShadowGoblinMageAmount = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.beru = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.summonlimitusage = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.polarbear = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.shadowdragonnum = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxxxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.highorcspawned = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxxxxxxxxxxxxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.tuskspawned = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 2.0) {
               MeteorRainProcedure.execute(world, y, entity);
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 4.0
               && !CooldownManager.isOnCooldown(entity, "job_2")) {
               LightningStormActivationProcedure.execute(entity);
            }
         } else {
            if (inputType == 0) {
               FrostMonarchManager.castFrozenPath(entity);
            } else if (inputType == 1) {
               FrostMonarchManager.releaseFrozenPath(entity);
            }
         }
      }
   }
}
