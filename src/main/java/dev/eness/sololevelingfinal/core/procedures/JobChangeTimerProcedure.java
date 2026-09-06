package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

@EventBusSubscriber
public class JobChangeTimerProcedure {
   private static final int JOB_CHANGE_ACCENT = -8758017;
   private static final int RIDDLE_DURATION = 56;
   private static final int RESULT_DURATION = 90;

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         execute(player.level(), player.getX(), player.getY(), player.getZ(), player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof ServerPlayer player && JobChangeQuestManager.isShadowPresentation(player)) {
         SololevelingModVariables.PlayerVariables data = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (data.JobChange_timer > 0.0 && data.JobChange_timer < 9.0 && world.getLevelData().getGameTime() % 60L == 0L) {
            int next = (int)data.JobChange_timer + 1;
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.JobChange_timer = next;
               capability.syncPlayerVariables(player);
            });
            playPanelSound(player, "sololeveling:panelopen");
            switch (next) {
               case 2:
                  show(player, systemTitle("JOB CHANGE"), systemUnder("Wherever the player goes, the angel of death follows..."), 56);
                  break;
               case 3:
                  show(
                     player,
                     systemTitle("JOB CHANGE"),
                     systemUnder("Every path the player takes is littered with corpses, and the stench of blood remains."),
                     56
                  );
                  break;
               case 4:
                  show(player, systemTitle("JOB CHANGE"), systemUnder("The player craves great power and blazed a trail without relying on others."), 56);
                  break;
               case 5:
                  show(player, systemTitle("JOB CHANGE"), systemUnder("That thirst for strength calls forth the ghosts that roam the valley of death."), 56);
                  break;
               case 6:
                  show(
                     player,
                     systemTitle("JOB CHANGE"),
                     systemUnder("Those ghosts will become a shadow army, obeying the player and making way for no one else."),
                     56
                  );
                  break;
               case 7:
                  show(player, systemTitle("JOB ASSIGNED"), Component.literal("[Necromancer]").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), 90);
                  break;
               case 8:
                  show(player, systemTitle("ADVANCEMENT"), systemUnder("The advancement points acquired have opened the path to an exceptional class."), 56);
            }
         } else {
            if (data.JobChange_timer == 9.0) {
               JobChangeCleanupProcedure.execute(world, x, y, z);
               Component evolution = Component.empty()
                  .append(Component.literal("[Necromancer]").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nV\n").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("[Shadow Monarch]").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
               show(player, Component.literal("CLASS EVOLUTION").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), evolution, 90);
               JobChangeQuestManager.finish(player);
               playPanelSound(player, "sololeveling:panelclose");
            }
         }
      }
   }

   private static void show(ServerPlayer player, Component title, Component under, int durationTicks) {
      SystemNotifications.showTitleUnder(player, -8758017, durationTicks, title, under);
   }

   private static Component systemTitle(String text) {
      return Component.literal(text).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
   }

   private static Component systemUnder(String text) {
      return Component.literal(text).withStyle(ChatFormatting.GRAY);
   }

   private static void playPanelSound(ServerPlayer player, String soundId) {
      if (ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(soundId)) != null) {
         player.serverLevel()
            .playSound(
               (Player)null,
               BlockPos.containing(player.position()),
               ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(soundId)),
               SoundSource.NEUTRAL,
               1.0F,
               1.0F
            );
      }
   }
}
