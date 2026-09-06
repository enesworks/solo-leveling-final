package dev.eness.sololevelingfinal.core.procedures;

import java.util.HashMap;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class InvestNumberProcedure {
   public static void execute(Entity entity, HashMap guistate) {
      if (entity != null && guistate != null) {
         if ((new Object() {
            double convert(String s) {
               try {
                  return Double.parseDouble(s.trim());
               } catch (Exception var3) {
                  return 0.0;
               }
            }
         }).convert(guistate.containsKey("text:investvalue") ? ((EditBox)guistate.get("text:investvalue")).getValue() : "") > 0.0) {
            double _setval = (new Object() {
               double convert(String s) {
                  try {
                     return Double.parseDouble(s.trim());
                  } catch (Exception var3) {
                     return 0.0;
                  }
               }
            }).convert(guistate.containsKey("text:investvalue") ? ((EditBox)guistate.get("text:investvalue")).getValue() : "");
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.investvalue = _setval;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }
}
