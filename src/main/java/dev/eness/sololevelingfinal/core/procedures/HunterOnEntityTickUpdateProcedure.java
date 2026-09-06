package dev.eness.sololevelingfinal.core.procedures;

import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

public class HunterOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         HunterAIHelper.tickCooldowns(entity);
         String hunterClass = entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_HunterClass) : "";
         String _expectedName = (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "") + " Rank " + hunterClass;
         if (entity.getCustomName() == null || !entity.getCustomName().getString().equals(_expectedName)) {
            entity.setCustomName(Component.literal(_expectedName));
         }

         if (entity instanceof HunterEntity hunter && hunter.isStoryTempleFollower()) {
            hunter.setTarget(null);
         } else {
            boolean legacyHandled = switch (hunterClass) {
               case "Fighter" -> invokeLegacy(
                  "dev.eness.sololevelingfinal.core.procedures.RandomHunterFighterTickProcedure",
                  new Class[]{double.class, double.class, double.class, Entity.class},
                  x,
                  y,
                  z,
                  entity
               );
               case "Assassin" -> invokeLegacy("dev.eness.sololevelingfinal.core.procedures.RandomHunterAssassinTickProcedure", new Class[]{Entity.class}, entity);
               case "Mage" -> invokeLegacy(
                  "dev.eness.sololevelingfinal.core.procedures.RandomHunterMageTickProcedure",
                  new Class[]{LevelAccessor.class, double.class, double.class, double.class, Entity.class},
                  world,
                  x,
                  y,
                  z,
                  entity
               );
               case "Ranger" -> invokeLegacy("dev.eness.sololevelingfinal.core.procedures.RandomHunterRangerTickProcedure", new Class[]{Entity.class}, entity);
               case "Tanker" -> invokeLegacy("dev.eness.sololevelingfinal.core.procedures.RandomHunterTankerTickProcedure", new Class[]{Entity.class}, entity);
               case "Healer" -> invokeLegacy(
                  "dev.eness.sololevelingfinal.core.procedures.RandomHunterHealerTickProcedure",
                  new Class[]{LevelAccessor.class, double.class, double.class, double.class, Entity.class},
                  world,
                  x,
                  y,
                  z,
                  entity
               );
               default -> true;
            };
            if (!legacyHandled) {
               runFallback(hunterClass, entity);
            }
         }
      }
   }

   private static boolean invokeLegacy(String className, Class<?>[] parameterTypes, Object... args) {
      try {
         Class<?> procedureClass = Class.forName(className);
         Method execute = procedureClass.getMethod("execute", parameterTypes);
         execute.invoke(null, args);
         return true;
      } catch (ReflectiveOperationException | LinkageError ignored) {
         return false;
      }
   }

   private static void runFallback(String hunterClass, Entity entity) {
      switch (hunterClass) {
         case "Fighter":
            HunterAIHelper.fighterCombatTick(entity);
            break;
         case "Assassin":
            HunterAIHelper.assassinCombatTick(entity);
            break;
         case "Mage":
            HunterAIHelper.casterBacklineTick(entity);
            break;
         case "Ranger":
            HunterAIHelper.rangerCombatTick(entity);
            break;
         case "Tanker":
            HunterAIHelper.tankerCombatTick(entity);
            break;
         case "Healer":
            HunterAIHelper.casterBacklineTick(entity);
            HunterAIHelper.healerSupportTick(entity);
      }
   }
}
