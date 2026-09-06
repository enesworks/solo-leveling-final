package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class TauntCastProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double chain = 0.0;
         double chainwait = 0.0;
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
            >= 100.0) {
            if (!CooldownManager.isOnCooldown(entity, "Taunt")) {
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .MP
                  - 100.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.MP = _setval;
                  capability.syncPlayerVariables(entity);
               });
               _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .progression_tanker
                  + 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.progression_tanker = _setval;
                  capability.syncPlayerVariables(entity);
               });
               CooldownManager.set(entity, "Taunt", 200);
               CooldownManager.set(entity, "mana_refresh", 40);
               chain = 10.0;

               for (int index0 = 0; index0 < (int)chain; index0++) {
                  SololevelingMod.queueServerWork(
                     (int)chainwait,
                     () -> {
                        if (world instanceof ServerLevel _level) {
                           _level.sendParticles(SololevelingModParticleTypes.GLOW_AURA_RED.get(), x, y, z, 5, 3.0, 3.0, 3.0, 1.0);
                        }

                        Vec3 _center = new Vec3(x, y, z);

                        for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.0), e -> true)
                           .stream()
                           .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                           .toList()) {
                           if (entityiterator instanceof TamableAnimal _tamEnt && _tamEnt.isTame()) {
                              if (!(entity instanceof LivingEntity _teamEnt
                                          && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                       ? _teamEnt.level()
                                          .getScoreboard()
                                          .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                          .getName()
                                       : "")
                                    .equals(
                                       (entityiterator instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) instanceof LivingEntity _teamEnt
                                             && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                          ? _teamEnt.level()
                                             .getScoreboard()
                                             .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                             .getName()
                                          : ""
                                    )
                                 && entityiterator instanceof Mob _entity
                                 && entity instanceof LivingEntity _ent) {
                                 _entity.setTarget(_ent);
                              }
                           } else if (entityiterator instanceof Mob _entity && entity instanceof LivingEntity _ent) {
                              _entity.setTarget(_ent);
                           }
                        }
                     }
                  );
                  chainwait += 2.0;
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Not enough MP!"), true);
         }
      }
   }
}
