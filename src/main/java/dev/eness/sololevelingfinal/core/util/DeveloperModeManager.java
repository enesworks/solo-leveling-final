package dev.eness.sololevelingfinal.core.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.DeveloperModeStateMessage;

@EventBusSubscriber(modid = "sololeveling")
public final class DeveloperModeManager {
   private static final String PERSISTED_FLAG = "slr_secret_developer_mode";
   private static final byte[] ACTIVATION_DIGEST = HexFormat.of().parseHex("a82fc3109f444ebbefe7290bb53f91d70ce4f9cdd33384a6ffa991ab5667c6b0");

   private DeveloperModeManager() {
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onServerChat(ServerChatEvent event) {
      if (matchesActivationCode(event.getRawText())) {
         event.setCanceled(true);
         ServerPlayer player = event.getPlayer();
         player.server.execute(() -> toggle(player));
      }
   }

   public static boolean isEnabled(Entity entity) {
      if (entity == null) {
         return false;
      } else if (entity.level().isClientSide()) {
         return DeveloperModeStateMessage.isClientEnabled();
      } else {
         return entity instanceof Player player ? hasPersistedFlag(player) : false;
      }
   }

   public static void toggle(ServerPlayer player) {
      if (player != null) {
         boolean enabled = !hasPersistedFlag(player);
         setPersistedFlag(player, enabled);
         if (!enabled) {
            VesselManager.VesselDefinition definition = VesselManager.currentDefinition(player);
            if (definition != null && "sung_il_hwan".equals(definition.identity())) {
               SungIlHwanCombatManager.resetPlayerState(player);
            }

            ShadowMonarchManager.dismissLockedPreviewShadows(player);
         }

         DeveloperModeStateMessage.sync(player, enabled);
         JobSkillManager.syncJobSkills(player);
         JobChangeQuestManager.requestSelectionScreen(player);
         player.server.getCommands().sendCommands(player);
         player.displayClientMessage(
            Component.literal("Developer testing mode " + (enabled ? "enabled" : "disabled") + ".")
               .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY),
            true
         );
      }
   }

   @SubscribeEvent
   public static void onPlayerClone(Clone event) {
      if (event.getEntity() instanceof ServerPlayer clone && hasPersistedFlag(event.getOriginal())) {
         setPersistedFlag(clone, true);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         sync(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         sync(player);
      }
   }

   private static void sync(ServerPlayer player) {
      DeveloperModeStateMessage.sync(player, hasPersistedFlag(player));
      JobSkillManager.syncJobSkills(player);
   }

   private static boolean hasPersistedFlag(Player player) {
      if (player == null) {
         return false;
      }

      CompoundTag root = player.getPersistentData();
      return root.contains("PlayerPersisted", 10) && root.getCompound("PlayerPersisted").getBoolean("slr_secret_developer_mode");
   }

   private static void setPersistedFlag(Player player, boolean enabled) {
      CompoundTag root = player.getPersistentData();
      CompoundTag persisted = root.getCompound("PlayerPersisted");
      if (enabled) {
         persisted.putBoolean("slr_secret_developer_mode", true);
      } else {
         persisted.remove("slr_secret_developer_mode");
      }

      root.put("PlayerPersisted", persisted);
   }

   private static boolean matchesActivationCode(String rawText) {
      if (rawText == null) {
         return false;
      }

      try {
         byte[] actual = MessageDigest.getInstance("SHA-256").digest(rawText.getBytes(StandardCharsets.UTF_8));
         return MessageDigest.isEqual(ACTIVATION_DIGEST, actual);
      } catch (NoSuchAlgorithmException exception) {
         SololevelingMod.LOGGER.error("Unable to validate the developer-mode chat digest", exception);
         return false;
      }
   }
}
