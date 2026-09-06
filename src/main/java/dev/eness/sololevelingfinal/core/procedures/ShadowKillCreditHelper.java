package dev.eness.sololevelingfinal.core.procedures;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public final class ShadowKillCreditHelper {
   private static final String LAST_PLAYER_DAMAGE_UUID = "SLRLastPlayerDamageUUID";
   private static final String LAST_PLAYER_DAMAGE_EXPIRES = "SLRLastPlayerDamageExpires";
   private static final long ENVIRONMENTAL_KILL_CREDIT_TICKS = 400L;

   private ShadowKillCreditHelper() {
   }

   public static Player creditedPlayer(LevelAccessor world, @Nullable Entity source) {
      if (source == null) {
         return null;
      } else if (source instanceof Player player) {
         return player;
      } else if (source instanceof Projectile projectile && projectile.getOwner() != null) {
         return creditedPlayer(world, projectile.getOwner());
      } else if (source instanceof TamableAnimal tame && tame.isTame() && tame.getOwner() instanceof Player owner) {
         return owner;
      } else {
         UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(source);
         if (ownerId == null) {
            return null;
         } else if (world instanceof ServerLevel level) {
            return level.getPlayerByUUID(ownerId);
         } else {
            for (Player player : world.players()) {
               if (player.getUUID().equals(ownerId)) {
                  return player;
               }
            }

            return null;
         }
      }
   }

   public static ServerPlayer creditedServerPlayer(LevelAccessor world, @Nullable Entity source) {
      return creditedPlayer(world, source) instanceof ServerPlayer serverPlayer ? serverPlayer : null;
   }

   public static Entity creditedSource(LevelAccessor world, @Nullable Entity source) {
      Player player = creditedPlayer(world, source);
      return player != null ? player : source;
   }

   public static void rememberRecentPlayerDamage(LivingEntity victim, @Nullable Entity source, @Nullable Entity directSource) {
      if (victim != null && !victim.level().isClientSide()) {
         Player player = creditedPlayer(victim.level(), source);
         if (player == null) {
            player = creditedPlayer(victim.level(), directSource);
         }

         if (player != null && player != victim) {
            victim.getPersistentData().putUUID("SLRLastPlayerDamageUUID", player.getUUID());
            victim.getPersistentData().putLong("SLRLastPlayerDamageExpires", victim.level().getGameTime() + 400L);
         }
      }
   }

   @Nullable
   public static Player creditedPlayerForDeath(LevelAccessor world, LivingEntity victim, @Nullable Entity source, @Nullable Entity directSource) {
      Player player = creditedPlayer(world, source);
      if (player == null) {
         player = creditedPlayer(world, directSource);
      }

      if (player == null && victim != null) {
         player = creditedPlayer(world, victim.getKillCredit());
      }

      if (player == null && victim != null) {
         if (!victim.getPersistentData().hasUUID("SLRLastPlayerDamageUUID")) {
            return null;
         } else if (victim.getPersistentData().getLong("SLRLastPlayerDamageExpires") < victim.level().getGameTime()) {
            victim.getPersistentData().remove("SLRLastPlayerDamageUUID");
            victim.getPersistentData().remove("SLRLastPlayerDamageExpires");
            return null;
         } else {
            UUID playerId = victim.getPersistentData().getUUID("SLRLastPlayerDamageUUID");
            if (world instanceof ServerLevel level) {
               return level.getServer().getPlayerList().getPlayer(playerId);
            } else {
               for (Player candidate : world.players()) {
                  if (candidate.getUUID().equals(playerId)) {
                     return candidate;
                  }
               }

               return null;
            }
         }
      } else {
         return player;
      }
   }

   @Nullable
   public static Entity creditedSourceForDeath(LevelAccessor world, LivingEntity victim, @Nullable Entity source, @Nullable Entity directSource) {
      Player player = creditedPlayerForDeath(world, victim, source, directSource);
      if (player != null) {
         return player;
      } else {
         return source != null ? source : directSource;
      }
   }
}
