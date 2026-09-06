package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.level.LevelEvent.Load;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DKCTeamInitProcedure {
   @SubscribeEvent
   public static void onWorldLoad(Load event) {
      execute(event, event.getLevel());
   }

   public static void execute(LevelAccessor world) {
      execute(null, world);
   }

   private static void execute(@Nullable Event event, LevelAccessor world) {
      if (world instanceof Level level) {
         PlayerTeam team = level.getScoreboard().getPlayerTeam("slr_dkc_demon");
         if (team == null) {
            team = level.getScoreboard().addPlayerTeam("slr_dkc_demon");
         }

         team.setColor(ChatFormatting.DARK_RED);
      }
   }
}
