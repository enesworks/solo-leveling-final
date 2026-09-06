package dev.eness.sololevelingfinal.core.dungeon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataManager;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataSnapshot;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDefinition;
import dev.eness.sololevelingfinal.core.entity.DatapackGateEntity;
import dev.eness.sololevelingfinal.core.network.DatapackGateSelectionStateMessage;

public final class DatapackGateSelectionService {
   private static final double MAX_INTERACTION_DISTANCE_SQR = 64.0;

   private DatapackGateSelectionService() {
   }

   public static void requestOpen(ServerPlayer player, DatapackGateEntity gate) {
      if (!validInteraction(player, gate)) {
         close(player, gate == null ? new UUID(0L, 0L) : gate.getUUID(), "This gate is no longer available.");
      } else {
         sendState(player, gate, "");
      }
   }

   public static void select(ServerPlayer player, UUID gateId, long expectedRevision, String dungeonText, String rankText) {
      if (player != null && gateId != null) {
         if (player.serverLevel().getEntity(gateId) instanceof DatapackGateEntity gate && validInteraction(player, gate)) {
            DungeonDataSnapshot snapshot = DungeonDataManager.snapshot();
            if (snapshot.revision() != expectedRevision) {
               sendState(player, gate, "Datapacks changed while this screen was open. Choose again.");
            } else {
               ResourceLocation dungeonId = ResourceLocation.tryParse(clean(dungeonText, 192));
               ProceduralDungeonRank rank = parseRank(rankText);
               if (dungeonId != null && rank != null) {
                  Optional<DungeonDefinition> definition = snapshot.dungeon(dungeonId);
                  if (!definition.isEmpty() && definition.get().supportsRank(rank)) {
                     DatapackDungeonGateHandler.BindingResult result = DatapackDungeonGateHandler.bindSelection(player, gate, dungeonId, rank, expectedRevision);
                     if (!result.success()) {
                        sendState(player, gate, result.message());
                     } else {
                        close(player, gateId, result.message());
                        if (result.message() != null && !result.message().isBlank()) {
                           player.displayClientMessage(Component.literal(result.message()), true);
                        }
                     }
                  } else {
                     sendState(player, gate, "That dungeon no longer supports the selected gate rank.");
                  }
               } else {
                  sendState(player, gate, "That dungeon or rank selection is invalid.");
               }
            }
         } else {
            close(player, gateId, "This gate is no longer close enough to configure.");
         }
      }
   }

   private static void sendState(ServerPlayer player, DatapackGateEntity gate, String notice) {
      DungeonDataSnapshot snapshot = DungeonDataManager.snapshot();
      List<DatapackGateSelectionStateMessage.Option> options = new ArrayList<>();

      for (ResourceLocation id : snapshot.dungeonIds()) {
         if (options.size() >= 256) {
            break;
         }

         snapshot.dungeon(id)
            .ifPresent(
               definition -> {
                  List<String> ranks = definition.allowedRanks().stream().sorted(Comparator.comparingInt(Enum::ordinal)).map(Enum::name).toList();
                  if (!ranks.isEmpty()) {
                     options.add(
                        new DatapackGateSelectionStateMessage.Option(
                           id.toString(), definition.kind().name(), definition.roomCount().min(), definition.roomCount().max(), ranks
                        )
                     );
                  }
               }
            );
      }

      String message = notice;
      if (options.isEmpty() && message.isBlank()) {
         message = "No valid datapack dungeons are currently loaded.";
      }

      SololevelingMod.PACKET_HANDLER
         .send(PacketDistributor.PLAYER.with(() -> player), new DatapackGateSelectionStateMessage(true, gate.getUUID(), snapshot.revision(), options, message));
   }

   private static void close(ServerPlayer player, UUID gateId, String notice) {
      if (player != null) {
         SololevelingMod.PACKET_HANDLER
            .send(
               PacketDistributor.PLAYER.with(() -> player),
               new DatapackGateSelectionStateMessage(false, gateId, DungeonDataManager.snapshot().revision(), List.of(), notice)
            );
      }
   }

   private static boolean validInteraction(ServerPlayer player, DatapackGateEntity gate) {
      return player != null && gate != null && !gate.isRemoved() && player.level() == gate.level() && player.distanceToSqr(gate) <= 64.0;
   }

   private static ProceduralDungeonRank parseRank(String value) {
      String clean = clean(value, 8).toUpperCase(Locale.ROOT);

      for (ProceduralDungeonRank rank : ProceduralDungeonRank.values()) {
         if (rank.name().equals(clean)) {
            return rank;
         }
      }

      return null;
   }

   private static String clean(String value, int maximum) {
      if (value == null) {
         return "";
      }

      String clean = value.replace('\u0000', ' ').trim();
      return clean.length() <= maximum ? clean : clean.substring(0, maximum);
   }
}
