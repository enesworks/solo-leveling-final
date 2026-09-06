package dev.eness.sololevelingfinal.core.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.DungeonBuilderClientState;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderProjectData;

@EventBusSubscriber(bus = Bus.MOD)
public final class DungeonBuilderStatusMessage {
   public static final int OK = 0;
   public static final int TODO = 1;
   public static final int ERROR = 2;
   public static final int INFO = 3;
   public static final int WARNING = 4;
   private static final int MAX_LINES = 12;
   private final DungeonBuilderStatusMessage.View view;

   public DungeonBuilderStatusMessage(DungeonBuilderStatusMessage.View view) {
      this.view = view == null ? DungeonBuilderStatusMessage.View.inactive() : view;
   }

   public DungeonBuilderStatusMessage(FriendlyByteBuf buffer) {
      boolean active = buffer.readBoolean();
      if (!active) {
         this.view = DungeonBuilderStatusMessage.View.inactive();
      } else {
         String projectId = buffer.readUtf(128);
         String type = buffer.readUtf(64);
         String ranks = buffer.readUtf(32);
         String group = buffer.readUtf(64);
         String pool = buffer.readUtf(128);
         String bounds = buffer.readUtf(128);
         String pending = buffer.readUtf(96);
         int regionCount = buffer.readVarInt();
         int socketCount = buffer.readVarInt();
         int markerCount = buffer.readVarInt();
         int encounterCount = buffer.readVarInt();
         int errors = buffer.readVarInt();
         int warnings = buffer.readVarInt();
         int lineCount = Math.min(12, Math.max(0, buffer.readVarInt()));
         List<DungeonBuilderStatusMessage.StatusLine> lines = new ArrayList<>(lineCount);

         for (int index = 0; index < lineCount; index++) {
            lines.add(new DungeonBuilderStatusMessage.StatusLine(buffer.readUnsignedByte(), buffer.readUtf(64), buffer.readUtf(192)));
         }

         this.view = new DungeonBuilderStatusMessage.View(
            true, projectId, type, ranks, group, pool, bounds, pending, regionCount, socketCount, markerCount, encounterCount, errors, warnings, lines
         );
      }
   }

   public static void buffer(DungeonBuilderStatusMessage message, FriendlyByteBuf buffer) {
      DungeonBuilderStatusMessage.View view = message.view;
      buffer.writeBoolean(view.active());
      if (view.active()) {
         buffer.writeUtf(view.projectId(), 128);
         buffer.writeUtf(view.type(), 64);
         buffer.writeUtf(view.ranks(), 32);
         buffer.writeUtf(view.group(), 64);
         buffer.writeUtf(view.pool(), 128);
         buffer.writeUtf(view.bounds(), 128);
         buffer.writeUtf(view.pending(), 96);
         buffer.writeVarInt(view.regionCount());
         buffer.writeVarInt(view.socketCount());
         buffer.writeVarInt(view.markerCount());
         buffer.writeVarInt(view.encounterCount());
         buffer.writeVarInt(view.errors());
         buffer.writeVarInt(view.warnings());
         List<DungeonBuilderStatusMessage.StatusLine> lines = view.lines().stream().limit(12L).toList();
         buffer.writeVarInt(lines.size());

         for (DungeonBuilderStatusMessage.StatusLine line : lines) {
            buffer.writeByte(line.status());
            buffer.writeUtf(line.label(), 64);
            buffer.writeUtf(line.detail(), 192);
         }
      }
   }

   public static void handler(DungeonBuilderStatusMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DungeonBuilderClientState.update(message.view)));
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         DungeonBuilderStatusMessage.class, DungeonBuilderStatusMessage::buffer, DungeonBuilderStatusMessage::new, DungeonBuilderStatusMessage::handler
      );
   }

   public static DungeonBuilderStatusMessage inactive() {
      return new DungeonBuilderStatusMessage(DungeonBuilderStatusMessage.View.inactive());
   }

   public static DungeonBuilderStatusMessage from(DungeonBuilderProjectData.Project project) {
      if (project == null) {
         return inactive();
      }

      List<DungeonBuilderProjectData.Issue> validation = project.validate();
      int errors = (int)validation.stream().filter(issue -> issue.severity() == DungeonBuilderProjectData.Severity.ERROR).count();
      int warnings = validation.size() - errors;
      long roomRegions = project.regions().stream().filter(region -> region.type().equals("room")).count();
      long spawnMarkers = project.markers().stream().filter(marker -> marker.type().equals("mob_spawn") || marker.type().equals("elite_spawn")).count();
      long unassignedSpawns = project.markers().stream().filter(marker -> marker.type().equals("spawn_point")).count();
      long bossMarkers = project.markers().stream().filter(marker -> marker.type().equals("boss_spawn")).count();
      long triggerRegions = project.regions().stream().filter(region -> region.type().equals("trigger_region")).count();
      long outside = validation.stream().filter(issue -> issue.message().toLowerCase(Locale.ROOT).contains("outside the structure bounds")).count();
      List<DungeonBuilderStatusMessage.StatusLine> lines = new ArrayList<>();
      DungeonBuilderProjectData.Bounds structure = project.structureBounds();
      String bounds;
      if (structure == null) {
         bounds = "not selected";
         lines.add(line(2, "Structure", "Select two outer corners with the Surveyor Wand."));
      } else {
         BlockPos size = structure.size();
         bounds = size.getX() + "x" + size.getY() + "x" + size.getZ() + " at " + structure.min().toShortString();
         lines.add(line(0, "Structure", size.getX() + " x " + size.getY() + " x " + size.getZ()));
      }

      lines.add(
         project.roomSnapshot().isPresent()
            ? line(0, "Snapshot", "Captured; update explicitly after block edits.")
            : line(1, "Snapshot", "Press N and choose Capture Room.")
      );
      lines.add(
         roomRegions == 1L
            ? line(0, "Room Bounds", "One walkable volume selected.")
            : line(2, "Room Bounds", roomRegions == 0L ? "Select the inside of this one room." : roomRegions + " volumes found; select again to replace them.")
      );
      if (outside > 0L) {
         lines.add(line(2, "Outside bounds", outside + " saved element" + (outside == 1L ? " is" : "s are") + " outside this project's structure."));
      }

      if (project.kind() == DungeonBuilderProjectData.ProjectKind.MODULE) {
         int sockets = project.sockets().size();
         switch (project.roomRole()) {
            case START:
            case BOSS:
               lines.add(
                  sockets == 1
                     ? line(0, "Sockets", "Exactly one doorway connector.")
                     : line(2, "Sockets", "This role needs exactly 1; currently " + sockets + ".")
               );
               break;
            case NORMAL:
            case CORRIDOR:
               lines.add(
                  sockets >= 2
                     ? line(0, "Sockets", sockets + " connectors (entrance + exit).")
                     : line(2, "Sockets", "Add an entrance and exit; currently " + sockets + ".")
               );
               break;
            default:
               lines.add(
                  sockets > 0
                     ? line(0, "Sockets", sockets + " connector" + (sockets == 1 ? "" : "s") + ".")
                     : line(2, "Sockets", "Add at least one connector.")
               );
         }

         if (project.roomRole() == DungeonBuilderProjectData.RoomRole.START) {
            lines.add(marker(project, "player_start", "Player Start", "Mark where players arrive."));
            boolean exit = hasMarker(project, "exit") || hasMarker(project, "return_portal");
            lines.add(exit ? line(0, "Return Portal", "Return position saved.") : line(2, "Return Portal", "Place it in the start room with the Feature Wand."));
         } else if (project.roomRole() == DungeonBuilderProjectData.RoomRole.BOSS) {
            lines.add(
               bossMarkers > 0L
                  ? line(0, "Boss Spawn", bossMarkers + " position saved.")
                  : (
                     unassignedSpawns > 0L
                        ? line(1, "Boss Spawn", "Assign a generic point as BOSS in Studio.")
                        : line(2, "Boss Spawn", "Place a point with the Encounter Wand.")
                  )
            );
            lines.add(
               triggerRegions > 0L
                  ? line(3, "Boss Activation", "Delayed trigger volume saved.")
                  : line(3, "Boss Activation", "Automatic; a trigger is optional.")
            );
         } else {
            lines.add(
               spawnMarkers > 0L
                  ? line(0, "Mob Spawns", spawnMarkers + " position" + (spawnMarkers == 1L ? "" : "s") + ".")
                  : (
                     unassignedSpawns > 0L
                        ? line(1, "Spawn Points", unassignedSpawns + " waiting for Studio assignment.")
                        : line(1, "Mob Spawns", "Optional: place generic spawn points.")
                  )
            );
            lines.add(
               triggerRegions > 0L
                  ? line(3, "Activation", "A delayed trigger volume is available.")
                  : line(3, "Activation", "Automatic; no trigger is required.")
            );
         }
      } else {
         lines.add(marker(project, "player_start", "Player Start", "Mark where players arrive."));
         boolean exit = hasMarker(project, "exit") || hasMarker(project, "return_portal");
         lines.add(exit ? line(0, "Return Portal", "Return position saved.") : line(2, "Return Portal", "Place an Exit or Return Portal."));
         lines.add(
            bossMarkers > 0L
               ? line(0, "Boss Spawn", bossMarkers + " position saved.")
               : (
                  unassignedSpawns > 0L
                     ? line(1, "Boss Spawn", "Assign a generic point as BOSS in Studio.")
                     : line(2, "Boss Spawn", "Preset dungeons need a boss position.")
               )
         );
      }

      lines.add(
         errors == 0
            ? line(0, "Validation", warnings == 0 ? "Ready to export." : warnings + " warning(s).")
            : line(2, "Validation", errors + " error(s), " + warnings + " warning(s).")
      );

      for (DungeonBuilderProjectData.Issue issue : validation) {
         if (lines.size() >= 12) {
            break;
         }

         if (!issue.message().toLowerCase(Locale.ROOT).contains("outside the structure bounds")) {
            lines.add(
               line(
                  issue.severity() == DungeonBuilderProjectData.Severity.ERROR ? 2 : 4,
                  issue.severity() == DungeonBuilderProjectData.Severity.ERROR ? "Fix" : "Warning",
                  issue.message()
               )
            );
         }
      }

      String type = project.kind().name().toLowerCase(Locale.ROOT);
      if (project.kind() == DungeonBuilderProjectData.ProjectKind.MODULE) {
         type = type + " / " + project.roomRole().name().toLowerCase(Locale.ROOT);
      }

      String ranks = Arrays.stream(ProceduralDungeonRank.values()).filter(project.allowedRanks()::contains).map(Enum::name).collect(Collectors.joining(","));
      String group = project.activeEncounterGroup();
      String pool = project.encounters()
         .stream()
         .filter(encounter -> encounter.id().equals(group))
         .map(DungeonBuilderProjectData.Encounter::pool)
         .findFirst()
         .orElse(project.defaultMobPool());
      String pending = project.pendingPosition() == null ? "" : "First corner: " + project.pendingPosition().toShortString();
      DungeonBuilderStatusMessage.View view = new DungeonBuilderStatusMessage.View(
         true,
         project.id(),
         type,
         ranks,
         group,
         pool,
         bounds,
         pending,
         project.regions().size(),
         project.sockets().size(),
         project.markers().size(),
         project.encounters().size(),
         errors,
         warnings,
         lines
      );
      return new DungeonBuilderStatusMessage(view);
   }

   private static boolean hasMarker(DungeonBuilderProjectData.Project project, String type) {
      return project.markers().stream().anyMatch(marker -> marker.type().equals(type));
   }

   private static DungeonBuilderStatusMessage.StatusLine marker(DungeonBuilderProjectData.Project project, String type, String label, String missing) {
      return hasMarker(project, type) ? line(0, label, "Position saved.") : line(2, label, missing);
   }

   private static DungeonBuilderStatusMessage.StatusLine line(int status, String label, String detail) {
      return new DungeonBuilderStatusMessage.StatusLine(status, label, detail);
   }

   public DungeonBuilderStatusMessage.View view() {
      return this.view;
   }

   private static String limit(String value, int maximum) {
      String safe = value == null ? "" : value;
      return safe.length() <= maximum ? safe : safe.substring(0, maximum);
   }

   public record StatusLine(int status, String label, String detail) {
      public StatusLine {
         status = Math.max(0, Math.min(4, status));
         label = DungeonBuilderStatusMessage.limit(label, 64);
         detail = DungeonBuilderStatusMessage.limit(detail, 192);
      }
   }

   public record View(
      boolean active,
      String projectId,
      String type,
      String ranks,
      String group,
      String pool,
      String bounds,
      String pending,
      int regionCount,
      int socketCount,
      int markerCount,
      int encounterCount,
      int errors,
      int warnings,
      List<DungeonBuilderStatusMessage.StatusLine> lines
   ) {
      public View {
         projectId = DungeonBuilderStatusMessage.limit(projectId, 128);
         type = DungeonBuilderStatusMessage.limit(type, 64);
         ranks = DungeonBuilderStatusMessage.limit(ranks, 32);
         group = DungeonBuilderStatusMessage.limit(group, 64);
         pool = DungeonBuilderStatusMessage.limit(pool, 128);
         bounds = DungeonBuilderStatusMessage.limit(bounds, 128);
         pending = DungeonBuilderStatusMessage.limit(pending, 96);
         lines = lines == null ? List.of() : List.copyOf(lines);
      }

      public static DungeonBuilderStatusMessage.View inactive() {
         return new DungeonBuilderStatusMessage.View(false, "", "", "", "", "", "", "", 0, 0, 0, 0, 0, 0, List.of());
      }
   }
}
