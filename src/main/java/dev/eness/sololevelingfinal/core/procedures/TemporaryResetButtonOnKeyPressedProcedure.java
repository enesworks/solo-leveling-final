package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.daily.DailyQuestLifecycleManager;

public class TemporaryResetButtonOnKeyPressedProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB == 1.0
            )
          {
            SololevelingModVariables.MapVariables.get(world).shmlimit--;
            SololevelingModVariables.MapVariables.get(world).syncData(world);
            double _setval = 0.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.JOB = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         double _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Vitality = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Strength = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Intelligence = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Speed = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Durability = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Xp = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Level = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 10.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.MaxXP = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.SkillPoints = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.shadowstorageusage = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 10.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.shadowstorage = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.polarbear = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.polarbearmax = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.summonlimit = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof LivingEntity _entity) {
            _entity.setHealth(20.0F);
         }

         String _setvalx = "";
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.MainQuest = _setval;
            capability.syncPlayerVariables(entity);
         });
         String _setvalxx = "";
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Dialogue = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.JOB = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.statshown = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.GuildCode = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.xpmultiplier = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.ordshadowmax = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.OrdShadow = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.igris = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.IgrisSpawned = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.GobShadow = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.GobShadowMax = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.WolfShadow = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.WolfShadowMax = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.orcmax = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.orcspawned = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.golds = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.HunterRank = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.perception = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Classes = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.pushup = _setval;
            capability.syncPlayerVariables(entity);
         });
         SololevelingModVariables.MapVariables.get(world).shmlimit = 0.0;
         SololevelingModVariables.MapVariables.get(world).syncData(world);
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.squat = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.statshown = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof ServerPlayer serverPlayer) {
            DailyQuestLifecycleManager.resetQuestState(serverPlayer, true);
         }
      }
   }
}
