package dev.eness.sololevelingfinal.core.client.highlight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.EntityHighlightMessage;
import dev.eness.sololevelingfinal.core.util.SystemClientConfig;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientEntityHighlightManager {
   private static final int MAX_TARGETS = 2048;
   private static final int MAX_SOURCES_PER_TARGET = 16;
   private static final int MAX_VISIBLE_PARTY_TARGETS = 7;
   private static final Map<UUID, Map<String, ClientEntityHighlightManager.Highlight>> HIGHLIGHTS = new HashMap<>();
   private static final Set<UUID> VISIBLE_TARGETS = new HashSet<>();
   private static long clientTick;
   private static long sequence;
   private static boolean selectionDirty = true;

   private ClientEntityHighlightManager() {
   }

   public static void handle(byte action, UUID targetId, ResourceLocation dimension, String source, int color, int durationTicks, int priority) {
      String safeSource = cleanSource(source);
      switch (action) {
         case 0:
            set(targetId, dimension, safeSource, color, durationTicks, priority);
            break;
         case 1:
            remove(targetId, safeSource);
            break;
         case 2:
            clearSource(safeSource);
            break;
         case 3:
            clear();
      }
   }

   public static OptionalInt colorFor(Entity entity) {
      if (entity != null && SystemClientConfig.isEntityOutlinesEnabled() && VISIBLE_TARGETS.contains(entity.getUUID())) {
         Map<String, ClientEntityHighlightManager.Highlight> sources = HIGHLIGHTS.get(entity.getUUID());
         if (sources == null) {
            return OptionalInt.empty();
         } else {
            pruneExpired(sources);
            if (sources.isEmpty()) {
               HIGHLIGHTS.remove(entity.getUUID());
               return OptionalInt.empty();
            } else {
               return bestHighlight(sources, entity.level().dimension().location())
                  .map(highlight -> OptionalInt.of(highlight.color()))
                  .orElseGet(OptionalInt::empty);
            }
         }
      } else {
         return OptionalInt.empty();
      }
   }

   public static boolean isHighlighted(Entity entity) {
      return colorFor(entity).isPresent();
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END) {
         clientTick++;
         if (clientTick % 20L == 0L) {
            pruneAll();
         }

         if (selectionDirty || clientTick % 5L == 0L) {
            refreshVisibleTargets();
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      clear();
   }

   private static void set(UUID targetId, ResourceLocation dimension, String source, int color, int durationTicks, int priority) {
      if (targetId != null && !EntityHighlightMessage.NO_TARGET.equals(targetId) && dimension != null) {
         if (!HIGHLIGHTS.containsKey(targetId) && HIGHLIGHTS.size() >= 2048) {
            pruneAll();
            if (HIGHLIGHTS.size() >= 2048) {
               evictOldestTarget();
            }
         }

         Map<String, ClientEntityHighlightManager.Highlight> sources = HIGHLIGHTS.computeIfAbsent(targetId, ignored -> new HashMap<>());
         int safePriority = Math.max(0, priority);
         if (!sources.containsKey(source) && sources.size() >= 16) {
            Entry<String, ClientEntityHighlightManager.Highlight> weakest = sources.entrySet()
               .stream()
               .min(
                  Comparator.<Entry<String, ClientEntityHighlightManager.Highlight>>comparingInt(entry -> entry.getValue().priority())
                     .thenComparingLong(entry -> entry.getValue().sequence())
               )
               .orElse(null);
            if (weakest != null && weakest.getValue().priority() > safePriority) {
               return;
            }

            if (weakest != null) {
               sources.remove(weakest.getKey());
            }
         }

         int safeDuration = Math.max(0, durationTicks);
         long expiresAt = safeDuration == 0 ? Long.MAX_VALUE : saturatingAdd(clientTick, safeDuration);
         sources.put(source, new ClientEntityHighlightManager.Highlight(dimension, color & 16777215, expiresAt, safePriority, ++sequence));
         selectionDirty = true;
      }
   }

   private static void remove(UUID targetId, String source) {
      Map<String, ClientEntityHighlightManager.Highlight> sources = HIGHLIGHTS.get(targetId);
      if (sources != null) {
         sources.remove(source);
         if (sources.isEmpty()) {
            HIGHLIGHTS.remove(targetId);
         }

         selectionDirty = true;
      }
   }

   private static void clearSource(String source) {
      Iterator<Entry<UUID, Map<String, ClientEntityHighlightManager.Highlight>>> iterator = HIGHLIGHTS.entrySet().iterator();

      while (iterator.hasNext()) {
         Map<String, ClientEntityHighlightManager.Highlight> sources = iterator.next().getValue();
         sources.remove(source);
         if (sources.isEmpty()) {
            iterator.remove();
         }
      }

      selectionDirty = true;
   }

   private static void clear() {
      HIGHLIGHTS.clear();
      VISIBLE_TARGETS.clear();
      clientTick = 0L;
      sequence = 0L;
      selectionDirty = true;
   }

   private static void pruneAll() {
      Iterator<Entry<UUID, Map<String, ClientEntityHighlightManager.Highlight>>> iterator = HIGHLIGHTS.entrySet().iterator();

      while (iterator.hasNext()) {
         Map<String, ClientEntityHighlightManager.Highlight> sources = iterator.next().getValue();
         pruneExpired(sources);
         if (sources.isEmpty()) {
            iterator.remove();
         }
      }

      selectionDirty = true;
   }

   private static void pruneExpired(Map<String, ClientEntityHighlightManager.Highlight> sources) {
      sources.values().removeIf(highlight -> highlight.expiresAt() <= clientTick);
   }

   private static Optional<ClientEntityHighlightManager.Highlight> bestHighlight(
      Map<String, ClientEntityHighlightManager.Highlight> sources, ResourceLocation dimension
   ) {
      ClientEntityHighlightManager.Highlight best = null;

      for (Entry<String, ClientEntityHighlightManager.Highlight> entry : sources.entrySet()) {
         ClientEntityHighlightManager.Highlight candidate = entry.getValue();
         if (candidate.dimension().equals(dimension)
            && sourceEnabled(entry.getKey())
            && (best == null || candidate.priority() > best.priority() || candidate.priority() == best.priority() && candidate.sequence() > best.sequence())) {
            best = candidate;
         }
      }

      return Optional.ofNullable(best);
   }

   private static boolean sourceEnabled(String source) {
      if (source.startsWith("dungeon:") || source.startsWith("dkc:")) {
         return SystemClientConfig.isEncounterOutlinesEnabled();
      } else {
         return !source.startsWith("perception:") && !source.startsWith("skill:") ? true : SystemClientConfig.isPerceptionOutlinesEnabled();
      }
   }

   private static void refreshVisibleTargets() {
      VISIBLE_TARGETS.clear();
      selectionDirty = false;
      if (SystemClientConfig.isEntityOutlinesEnabled()) {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.level != null && minecraft.player != null) {
            UUID aimedTarget = minecraft.hitResult instanceof EntityHitResult hit ? hit.getEntity().getUUID() : null;
            int density = SystemClientConfig.getOutlineDensity();

            int targetLimit = switch (density) {
               case 0 -> 4;
               case 2 -> 24;
               default -> 8;
            };

            double range = switch (density) {
               case 0 -> 48.0;
               case 2 -> 128.0;
               default -> 80.0;
            };
            double rangeSqr = range * range;
            ResourceLocation dimension = minecraft.level.dimension().location();
            List<ClientEntityHighlightManager.Candidate> candidates = new ArrayList<>();

            for (Entity entity : minecraft.level.entitiesForRendering()) {
               Map<String, ClientEntityHighlightManager.Highlight> sources = HIGHLIGHTS.get(entity.getUUID());
               if (sources != null) {
                  pruneExpired(sources);
                  Optional<ClientEntityHighlightManager.Highlight> selected = bestHighlight(sources, dimension);
                  if (!selected.isEmpty()) {
                     ClientEntityHighlightManager.Highlight highlight = selected.get();
                     ClientEntityHighlightManager.Highlight partyHighlight = activePartyHighlight(sources, dimension);
                     boolean partyMember = partyHighlight != null;
                     boolean selectedPartyColor = partyHighlight == highlight;
                     boolean targeted = entity.getUUID().equals(aimedTarget);
                     boolean boss = !selectedPartyColor && (highlight.priority() >= 300 || highlight.color() == 16724047);
                     boolean elite = !boss && !selectedPartyColor && (highlight.priority() >= 250 || highlight.color() == 16757575);
                     double distanceSqr = minecraft.player.distanceToSqr(entity);
                     if (boss || targeted || partyMember || !(distanceSqr > rangeSqr)) {
                        boolean visible = minecraft.player.hasLineOfSight(entity);
                        int tier = boss ? 2 : (elite ? 1 : 0);
                        if (targeted || partyMember || tier != 0 || (density != 0 || !visible) && (density != 1 || !visible || !(distanceSqr < 144.0))) {
                           candidates.add(
                              new ClientEntityHighlightManager.Candidate(entity.getUUID(), highlight, distanceSqr, targeted, visible, tier, partyMember)
                           );
                        }
                     }
                  }
               }
            }

            candidates.sort(
               Comparator.<ClientEntityHighlightManager.Candidate>comparingInt(candidatex -> candidatex.targeted() ? 0 : 1)
                  .thenComparingInt(candidatex -> -candidatex.tier())
                  .thenComparingInt(candidatex -> candidatex.partyMember() ? 0 : 1)
                  .thenComparingInt(candidatex -> candidatex.visible() ? 1 : 0)
                  .thenComparingInt(candidatex -> -candidatex.highlight().priority())
                  .thenComparingDouble(ClientEntityHighlightManager.Candidate::distanceSqr)
            );
            int limitedTargets = 0;
            int partyTargets = 0;

            for (ClientEntityHighlightManager.Candidate candidate : candidates) {
               if (candidate.tier() == 2 || candidate.targeted()) {
                  VISIBLE_TARGETS.add(candidate.targetId());
               } else if (candidate.partyMember()) {
                  if (partyTargets < 7) {
                     VISIBLE_TARGETS.add(candidate.targetId());
                     partyTargets++;
                  }
               } else if (limitedTargets < targetLimit) {
                  VISIBLE_TARGETS.add(candidate.targetId());
                  limitedTargets++;
               }
            }
         }
      }
   }

   private static void evictOldestTarget() {
      HIGHLIGHTS.entrySet()
         .stream()
         .min(
            Comparator.comparingLong(
               entry -> entry.getValue().values().stream().mapToLong(ClientEntityHighlightManager.Highlight::sequence).max().orElse(Long.MIN_VALUE)
            )
         )
         .map(Entry::getKey)
         .ifPresent(HIGHLIGHTS::remove);
      selectionDirty = true;
   }

   private static long saturatingAdd(long left, int right) {
      return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
   }

   private static ClientEntityHighlightManager.Highlight activePartyHighlight(
      Map<String, ClientEntityHighlightManager.Highlight> sources, ResourceLocation dimension
   ) {
      ClientEntityHighlightManager.Highlight party = sources.get("party:members");
      return party != null && party.dimension().equals(dimension) && sourceEnabled("party:members") ? party : null;
   }

   private static String cleanSource(String source) {
      if (source != null && !source.isBlank()) {
         String trimmed = source.trim();
         return trimmed.length() <= 64 ? trimmed : trimmed.substring(0, 64);
      } else {
         return "default";
      }
   }

   private record Candidate(
      UUID targetId, ClientEntityHighlightManager.Highlight highlight, double distanceSqr, boolean targeted, boolean visible, int tier, boolean partyMember
   ) {
   }

   private record Highlight(ResourceLocation dimension, int color, long expiresAt, int priority, long sequence) {
   }
}
