package dev.eness.sololevelingfinal.core.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.ItemHandlerHelper;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class InstanceDungeonKeyAccess {
   private static final String CLAIMED_TAG = "slr_instance_dungeon_key_claimed";
   private static final String COMPLETED_TAG = "slr_instance_dungeon_completed";
   private static final ResourceLocation KASAKA_ADVANCEMENT = new ResourceLocation("sololeveling", "kasakas_domain");
   private static final ResourceLocation INSTANCE_ENTRY_ADVANCEMENT = new ResourceLocation("sololeveling", "explore_dun_instance_c");

   private InstanceDungeonKeyAccess() {
   }

   public static boolean grantInitialKey(Player player) {
      if (player != null && !hasClaimed(player)) {
         markClaimed(player);
         ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(SololevelingModItems.INSTANCE_DUNGEON_KEY.get()));
         return true;
      } else {
         return false;
      }
   }

   public static boolean canEnter(ServerPlayer player) {
      if (player != null && !hasCompleted(player)) {
         boolean gettingStronger = variables(player).MainQuest.equals("Getting Stronger");
         return hasPhysicalKey(player) || gettingStronger && hasClaimed(player);
      } else {
         return false;
      }
   }

   public static boolean hasClaimed(Player player) {
      if (player == null) {
         return false;
      }

      CompoundTag persisted = persistentPlayerData(player);
      if (persisted.getBoolean("slr_instance_dungeon_key_claimed")) {
         return true;
      }

      SololevelingModVariables.PlayerVariables variables = variables(player);
      boolean legacyClaim = hasPhysicalKey(player)
         || "Getting Stronger".equals(variables.MainQuest) && variables.QuestProgression >= 1.0
         || hasAdvancement(player, INSTANCE_ENTRY_ADVANCEMENT);
      if (legacyClaim) {
         persisted.putBoolean("slr_instance_dungeon_key_claimed", true);
      }

      return legacyClaim;
   }

   public static boolean hasCompleted(Player player) {
      if (player == null) {
         return false;
      } else {
         CompoundTag persisted = persistentPlayerData(player);
         if (persisted.getBoolean("slr_instance_dungeon_completed")) {
            return true;
         } else if (hasAdvancement(player, KASAKA_ADVANCEMENT) && hasAdvancement(player, INSTANCE_ENTRY_ADVANCEMENT)) {
            persisted.putBoolean("slr_instance_dungeon_key_claimed", true);
            persisted.putBoolean("slr_instance_dungeon_completed", true);
            return true;
         } else {
            return false;
         }
      }
   }

   public static void markClaimed(Player player) {
      if (player != null) {
         persistentPlayerData(player).putBoolean("slr_instance_dungeon_key_claimed", true);
      }
   }

   public static void markCompleted(Player player) {
      if (player != null) {
         CompoundTag persisted = persistentPlayerData(player);
         persisted.putBoolean("slr_instance_dungeon_key_claimed", true);
         persisted.putBoolean("slr_instance_dungeon_completed", true);
      }
   }

   public static boolean hasPhysicalKey(Player player) {
      return player != null && player.getInventory().contains(new ItemStack(SololevelingModItems.INSTANCE_DUNGEON_KEY.get()));
   }

   public static void consumePhysicalKey(Player player) {
      if (player != null && !player.getAbilities().instabuild) {
         player.getInventory()
            .clearOrCountMatchingItems(stack -> stack.is(SololevelingModItems.INSTANCE_DUNGEON_KEY.get()), 1, player.inventoryMenu.getCraftSlots());
      }
   }

   @SubscribeEvent
   public static void onPlayerClone(Clone event) {
      CompoundTag originalRoot = event.getOriginal().getPersistentData();
      if (originalRoot.contains("PlayerPersisted", 10)) {
         CompoundTag originalPersisted = originalRoot.getCompound("PlayerPersisted");
         CompoundTag clonePersisted = persistentPlayerData(event.getEntity());
         if (originalPersisted.getBoolean("slr_instance_dungeon_key_claimed")) {
            clonePersisted.putBoolean("slr_instance_dungeon_key_claimed", true);
         }

         if (originalPersisted.getBoolean("slr_instance_dungeon_completed")) {
            clonePersisted.putBoolean("slr_instance_dungeon_completed", true);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         hasClaimed(player);
         hasCompleted(player);
      }
   }

   private static boolean hasAdvancement(Player player, ResourceLocation id) {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return false;
      } else {
         Advancement advancement = serverPlayer.server.getAdvancements().getAdvancement(id);
         return advancement != null && serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
      }
   }

   private static CompoundTag persistentPlayerData(Player player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         root.put("PlayerPersisted", new CompoundTag());
      }

      return root.getCompound("PlayerPersisted");
   }

   private static SololevelingModVariables.PlayerVariables variables(Player player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }
}
