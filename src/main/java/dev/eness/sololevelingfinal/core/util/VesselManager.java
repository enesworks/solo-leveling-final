package dev.eness.sololevelingfinal.core.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class VesselManager {
   public static final String RULER = "ruler";
   public static final String MONARCH = "monarch";
   public static final String ANTARES_IDENTITY = "antares";
   private static final Set<String> WORK_IN_PROGRESS = Set.of("christopher_reed", "sung_il_hwan", "go_gunhee");
   private static final VesselManager.VesselDefinition ANTARES_DEFINITION = new VesselManager.VesselDefinition(
      "monarch", "antares", 10, "Antares", "Monarch of Destruction", "Build Ruin and unleash the destructive authority of the King of Dragons."
   );
   private static final List<VesselManager.VesselDefinition> DEFINITIONS = List.of(
      new VesselManager.VesselDefinition("ruler", "ashborn", 1, "Ashborn", "Shadow Monarch", "Command the dead and an endless shadow army."),
      new VesselManager.VesselDefinition(
         "ruler", "christopher_reed", 2, "Christopher Reed", "Flame Incarnation", "Overwhelm enemies with a Ruler's destructive flame."
      ),
      new VesselManager.VesselDefinition("ruler", "thomas_andre", 5, "Thomas Andre", "Goliath", "Crush the battlefield through unmatched physical force."),
      new VesselManager.VesselDefinition("ruler", "liu_zhigang", 6, "Liu Zhigang", "Sword Sovereign", "Release vast sword beams with absolute precision."),
      new VesselManager.VesselDefinition("ruler", "sung_il_hwan", 7, "Sung Il-Hwan", "Silent Authority", "Fight through speed, assassination, and Ruler power."),
      new VesselManager.VesselDefinition("ruler", "go_gunhee", 8, "Go Gunhee", "Brilliant Fragment", "Dominate close combat with reinforced authority."),
      new VesselManager.VesselDefinition("monarch", "sillad", 3, "Sillad", "Frost Monarch", "Freeze the battlefield and shatter immobilized enemies."),
      new VesselManager.VesselDefinition("monarch", "baran", 4, "Baran", "Monarch of White Flames", "Rule demonic flame, lightning, and infernal armies."),
      new VesselManager.VesselDefinition("monarch", "rakan", 9, "Rakan", "Monarch of Fangs", "Hunt with feral speed, claws, and bestial power."),
      ANTARES_DEFINITION
   );

   private VesselManager() {
   }

   public static int assign(CommandContext<CommandSourceStack> context, String type, String identity) {
      VesselManager.VesselDefinition definition = definition(type, identity);
      if (definition == null) {
         return 0;
      }

      try {
         int changed = 0;
         int locked = 0;

         for (Entity target : EntityArgument.getEntities(context, "name")) {
            if (target instanceof ServerPlayer player) {
               VesselManager.AssignmentResult result = assignPlayer(player, definition, true);
               if (result == VesselManager.AssignmentResult.LOCKED) {
                  locked++;
                  player.sendSystemMessage(Component.literal("That vessel has reached the server limit.").withStyle(ChatFormatting.RED));
               } else if (result == VesselManager.AssignmentResult.SUCCESS) {
                  JobChangeQuestManager.finish(player);
                  player.sendSystemMessage(
                     Component.literal("Vessel assigned: " + definition.commandDisplay())
                        .withStyle("ruler".equals(definition.type()) ? ChatFormatting.AQUA : ChatFormatting.DARK_PURPLE)
                  );
                  changed++;
               }
            }
         }

         int result = changed;
         int failed = locked;
         ((CommandSourceStack)context.getSource())
            .sendSuccess(
               () -> Component.literal(
                  "Assigned "
                     + definition.commandDisplay()
                     + " to "
                     + result
                     + " player(s)"
                     + (failed > 0 ? "; " + failed + " blocked by the vessel limit" : "")
               ),
               true
            );
         return changed;
      } catch (CommandSyntaxException exception) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("Unable to resolve vessel targets"));
         return 0;
      }
   }

   public static int reset(CommandContext<CommandSourceStack> context) {
      try {
         int changed = 0;

         for (Entity target : EntityArgument.getEntities(context, "name")) {
            if (target instanceof ServerPlayer player) {
               resetPlayer(player);
               player.sendSystemMessage(Component.literal("Vessel status reset").withStyle(ChatFormatting.GRAY));
               changed++;
            }
         }

         int result = changed;
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Reset vessel status for " + result + " player(s)"), true);
         return changed;
      } catch (CommandSyntaxException exception) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("Unable to resolve vessel targets"));
         return 0;
      }
   }

   public static int openSelection(CommandContext<CommandSourceStack> context) {
      try {
         int opened = 0;

         for (Entity target : EntityArgument.getEntities(context, "name")) {
            if (target instanceof ServerPlayer player) {
               JobChangeQuestManager.openSelectionFromCommand(player);
               opened++;
            }
         }

         int result = opened;
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Opened vessel selection for " + result + " player(s)"), true);
         return opened;
      } catch (CommandSyntaxException exception) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("Unable to resolve vessel targets"));
         return 0;
      }
   }

   public static VesselManager.AssignmentResult assignPlayer(ServerPlayer player, String type, String identity, boolean enforceLimit) {
      VesselManager.VesselDefinition definition = definition(type, identity);
      return definition == null ? VesselManager.AssignmentResult.INVALID : assignPlayer(player, definition, enforceLimit);
   }

   public static VesselManager.AssignmentResult assignAntaresVessel(ServerPlayer player, boolean enforceLimit) {
      return assignPlayer(player, ANTARES_DEFINITION, enforceLimit);
   }

   public static VesselManager.AssignmentResult assignPlayer(ServerPlayer player, VesselManager.VesselDefinition definition, boolean enforceLimit) {
      if (player != null && definition != null) {
         VesselClaimSavedData claims = VesselClaimSavedData.get(player.serverLevel());
         int limit = vesselLimit(player);
         if (enforceLimit && !claims.tryClaim(definition.key(), player.getUUID(), limit)) {
            return VesselManager.AssignmentResult.LOCKED;
         }

         if (!enforceLimit) {
            claims.claimExisting(definition.key(), player.getUUID());
         }

         applyDefinition(player, definition);
         return VesselManager.AssignmentResult.SUCCESS;
      } else {
         return VesselManager.AssignmentResult.INVALID;
      }
   }

   public static void resetPlayer(ServerPlayer player) {
      if (player != null) {
         AntaresCombatManager.resetPlayerState(player);
         if (LiuManifestationManager.isActive(player)) {
            LiuManifestationManager.restore(player);
         }

         TemporaryArmorSessionManager.endForVesselChange(player);
         VesselClaimSavedData.get(player.serverLevel()).release(player.getUUID());
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            revokeAutomaticAuthority(capability);
            capability.vesselType = "";
            capability.vesselIdentity = "";
            capability.JOB = 0.0;
            capability.syncPlayerVariables(player);
         });
         VesselProgressionManager.reconcileEntitlements(player);
      }
   }

   public static void releaseClaim(ServerPlayer player) {
      if (player != null) {
         VesselClaimSavedData.get(player.serverLevel()).release(player.getUUID());
      }
   }

   public static boolean isRulerVessel(Entity entity) {
      return entity != null
         && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .map(capability -> "ruler".equals(capability.vesselType))
            .orElse(false);
   }

   public static String identity(Entity entity) {
      return entity == null
         ? ""
         : entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.vesselIdentity).orElse("");
   }

   public static List<VesselManager.VesselDefinition> definitions() {
      return DEFINITIONS;
   }

   public static VesselManager.VesselDefinition antaresDefinition() {
      return ANTARES_DEFINITION;
   }

   public static boolean isAntares(VesselManager.VesselDefinition definition) {
      return definition != null && "antares".equals(definition.identity()) && "monarch".equals(definition.type());
   }

   public static boolean isWorkInProgress(VesselManager.VesselDefinition definition) {
      return definition != null && WORK_IN_PROGRESS.contains(definition.identity());
   }

   public static boolean isDeveloperPreview(VesselManager.VesselDefinition definition) {
      return definition != null && "sung_il_hwan".equals(definition.identity());
   }

   public static boolean isSelectableFor(ServerPlayer player, VesselManager.VesselDefinition definition) {
      return !isWorkInProgress(definition) || isDeveloperPreview(definition) && DeveloperModeManager.isEnabled(player);
   }

   public static VesselManager.VesselDefinition definition(String type, String identity) {
      String normalizedType = type == null ? "" : type.toLowerCase();
      String normalizedIdentity = normalizeIdentity(identity);
      return DEFINITIONS.stream().filter(value -> value.type().equals(normalizedType) && value.identity().equals(normalizedIdentity)).findFirst().orElse(null);
   }

   public static VesselManager.VesselDefinition definitionForJob(int jobId) {
      return DEFINITIONS.stream().filter(value -> value.jobId() == jobId).findFirst().orElse(null);
   }

   public static VesselManager.VesselDefinition currentDefinition(Entity entity) {
      if (entity == null) {
         return null;
      }

      SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      VesselManager.VesselDefinition explicit = definition(vars.vesselType, vars.vesselIdentity);
      return explicit != null ? explicit : definitionForJob((int)vars.JOB);
   }

   public static int vesselLimit(ServerPlayer player) {
      return player.level().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_MONARCH_LIMIT);
   }

   public static int claimCount(ServerPlayer player, VesselManager.VesselDefinition definition) {
      return VesselClaimSavedData.get(player.serverLevel()).count(definition.key());
   }

   public static int[] claimCounts(ServerPlayer player) {
      VesselClaimSavedData data = VesselClaimSavedData.get(player.serverLevel());
      int[] counts = new int[DEFINITIONS.size()];

      for (int i = 0; i < DEFINITIONS.size(); i++) {
         counts[i] = data.count(DEFINITIONS.get(i).key());
      }

      return counts;
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         reconcileExistingPlayer(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         reconcileExistingPlayer(player);
      }
   }

   private static void reconcileExistingPlayer(ServerPlayer player) {
      VesselManager.VesselDefinition definition = currentDefinition(player);
      if (definition == null) {
         releaseClaim(player);
      } else {
         assignPlayer(player, definition, false);
      }
   }

   private static void applyDefinition(ServerPlayer player, VesselManager.VesselDefinition definition) {
      AntaresCombatManager.resetPlayerState(player);
      if (LiuManifestationManager.isActive(player)) {
         LiuManifestationManager.restore(player);
      }

      TemporaryArmorSessionManager.endForVesselChange(player);
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         if ("ruler".equals(definition.type())) {
            if (!hasAbility(capability.abilities, "telekinesis")) {
               capability.abilities = appendAbility(capability.abilities, "telekinesis");
               capability.vesselGrantedAuthority = true;
            }
         } else {
            revokeAutomaticAuthority(capability);
         }

         capability.vesselType = definition.type();
         capability.vesselIdentity = definition.identity();
         capability.JOB = definition.jobId();
         capability.syncPlayerVariables(player);
      });
      VesselProgressionManager.reconcileEntitlements(player);
   }

   private static String normalizeIdentity(String identity) {
      if (identity == null) {
         return "";
      } else {
         return "sung_il_whan".equalsIgnoreCase(identity) ? "sung_il_hwan" : identity.toLowerCase();
      }
   }

   private static void revokeAutomaticAuthority(SololevelingModVariables.PlayerVariables capability) {
      if (capability.vesselGrantedAuthority) {
         capability.abilities = removeAbility(capability.abilities, "telekinesis");
         capability.vesselGrantedAuthority = false;
      }
   }

   private static boolean hasAbility(String abilities, String ability) {
      return abilityList(abilities).stream().anyMatch(ability::equalsIgnoreCase);
   }

   private static String appendAbility(String abilities, String ability) {
      List<String> values = abilityList(abilities);
      if (values.stream().noneMatch(ability::equalsIgnoreCase)) {
         values.add(ability);
      }

      return String.join(" ", values);
   }

   private static String removeAbility(String abilities, String ability) {
      List<String> values = abilityList(abilities);
      values.removeIf(ability::equalsIgnoreCase);
      return values.isEmpty() ? "\"\"" : String.join(" ", values);
   }

   private static List<String> abilityList(String abilities) {
      return abilities != null && !abilities.isBlank()
         ? new ArrayList<>(Arrays.stream(abilities.replace('"', ' ').trim().split("\\s+")).filter(value -> !value.isBlank()).toList())
         : new ArrayList<>();
   }

   public enum AssignmentResult {
      SUCCESS,
      LOCKED,
      INVALID;
   }

   public record VesselDefinition(String type, String identity, int jobId, String name, String powerName, String description) {
      public String key() {
         return this.type + ":" + this.identity;
      }

      public String commandDisplay() {
         return ("ruler".equals(this.type) ? "Ruler" : "Monarch") + " / " + this.name;
      }
   }
}
