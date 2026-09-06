package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcQuestProgressTracker;
import dev.eness.sololevelingfinal.core.dkc.DkcRadiruManager;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.party.PartyHighlightManager;
import dev.eness.sololevelingfinal.core.procedures.DungeonDimensionPlayerLeavesDimensionProcedure;
import dev.eness.sololevelingfinal.core.procedures.JobChangeCleanupProcedure;
import dev.eness.sololevelingfinal.core.util.daily.DailyQuestLifecycleManager;

public final class PlayerProgressResetManager {
   private static final String STORY_DUNGEON_TAG = "story_intro_ancient_golem";
   private static final String IGRIS_DUNGEON = "dungeon_dimension_igris";

   private PlayerProgressResetManager() {
   }

   public static boolean reset(ServerPlayer player) {
      if (player != null && player.server != null) {
         if (!StoryModeIntroManager.isStoryOwner(player) && !player.getPersistentData().getBoolean("slr_cartenon_awakening_pending")) {
            SololevelingModVariables.PlayerVariables current = variables(player);
            PlayerProgressResetManager.PreservedState preserved = PlayerProgressResetManager.PreservedState.capture(current);
            boolean legacyTemporaryArmor = hasEquippedTemporaryArmor(player);
            boolean temporaryArmorEscrow = TemporaryArmorSessionManager.hasActiveEscrow(player) || legacyTemporaryArmor;
            boolean temporaryArmorWasEquipped = TemporaryArmorSessionManager.hasEquippedEscrow(player) || legacyTemporaryArmor;
            TemporaryArmorSessionManager.invalidatePendingEquip(player);
            PlayerEntryGenerationGuard.invalidate(player);
            detachFromActiveDungeon(player, current);
            DaggerThrowManager.recoverEscrowForReset(player);
            AssassinSkillManager.resetPlayerState(player);
            ArcaneMageSpellManager.resetPlayerState(player);
            BarrierMageSpellManager.resetPlayerState(player);
            FireMageSpellManager.resetPlayerState(player);
            StormMageSpellManager.resetPlayerState(player);
            RangerCombatManager.resetPlayerState(player);
            ClassPassiveManager.resetPlayerState(player);
            TankerSkillManager.resetPlayerState(player);
            FrostArchitectureManager.resetPlayerState(player);
            FrostMonarchManager.resetPlayerState(player);
            RulersAuthorityManager.resetPlayerState(player);
            GoliathCombatManager.resetPlayerState(player);
            LiuZhigangCombatManager.resetPlayerState(player);
            SungIlHwanCombatManager.resetPlayerState(player);
            BeastMonarchManager.resetPlayerState(player);
            AntaresCombatManager.resetPlayerState(player);
            WhiteFlameMonarchManager.resetPlayerState(player);
            ShadowMonarchManager.resetPlayerProgress(player);
            UrgentQuestManager.resetForPlayerReset(player);
            restoreEscrowedArmor(player, current, temporaryArmorEscrow, temporaryArmorWasEquipped);
            VesselManager.resetPlayer(player);
            JobChangeQuestManager.resetForPlayerReset(player);
            DkcFloorBuilder.cancelPlayerBuilds(player.server, player.getUUID());
            DkcRadiruManager.resetPlayerState(player);
            DkcQuestProgressTracker.resetPlayerState(player);
            DkcRunSavedData.get(player.server).resetProgress(player.getUUID());
            PlayerAuraSystem.clearContinuous(player);
            PartyHighlightManager.clearNow(player);
            EntityHighlightSystem.clearAll(player);
            CooldownManager.clearAll(player);
            clearModPersistentData(player.getPersistentData());
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.readNBT(new SololevelingModVariables.PlayerVariables().writeNBT());
               preserved.restore(capability);
               capability.syncPlayerVariables(player);
            });
            DailyQuestLifecycleManager.resetQuestState(player, true);
            revokeModProgressAdvancements(player);
            removeModEffects(player);
            player.setNoGravity(false);
            VesselProgressionManager.reconcileEntitlements(player);
            PlayerVitalSync.restoreAfterRespawn(player);
            PartyHighlightManager.syncNow(player);
            return true;
         } else {
            player.displayClientMessage(
               Component.literal("Character reset is unavailable while the System awakening story is active.").withStyle(ChatFormatting.RED), false
            );
            return false;
         }
      } else {
         return false;
      }
   }

   private static void detachFromActiveDungeon(ServerPlayer player, SololevelingModVariables.PlayerVariables variables) {
      ResourceLocation dimension = player.level().dimension().location();
      boolean jobDungeon = "sololeveling".equals(dimension.getNamespace()) && "dungeon_dimension_igris".equals(dimension.getPath());
      if (jobDungeon && player.serverLevel().players().stream().noneMatch(other -> other != player && JobChangeQuestManager.canResumeDungeon(other))) {
         JobChangeCleanupProcedure.execute(player.serverLevel(), player.getX(), player.getY(), player.getZ());
      }

      boolean boundToRuntime = !player.getPersistentData().getString("slr_dungeon_instance").isBlank();
      boolean resettableModDimension = "sololeveling".equals(dimension.getNamespace())
         && (dimension.getPath().contains("dungeon") || dimension.getPath().contains("castle"));
      if (boundToRuntime || variables.dungeoning || resettableModDimension) {
         DungeonDimensionPlayerLeavesDimensionProcedure.emergencyExit(player);
      }
   }

   private static void clearModPersistentData(CompoundTag root) {
      clearKnownKeys(root);
      if (root.contains("PlayerPersisted", 10)) {
         clearKnownKeys(root.getCompound("PlayerPersisted"));
      }
   }

   private static void clearKnownKeys(CompoundTag tag) {
      for (String key : new ArrayList<>(tag.getAllKeys())) {
         if ((!"dungeon_tag".equals(key) || !"story_intro_ancient_golem".equals(tag.getString(key))) && PlayerResetKeyPolicy.shouldClear(key)) {
            tag.remove(key);
         }
      }
   }

   private static void revokeModProgressAdvancements(ServerPlayer player) {
      for (Advancement advancement : player.server.getAdvancements().getAllAdvancements()) {
         if ("sololeveling".equals(advancement.getId().getNamespace()) && !"awakened".equals(advancement.getId().getPath())) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            ArrayList<String> completedCriteria = new ArrayList<>();

            for (String criterion : progress.getCompletedCriteria()) {
               completedCriteria.add(criterion);
            }

            for (String criterion : completedCriteria) {
               player.getAdvancements().revoke(advancement, criterion);
            }
         }
      }
   }

   private static void removeModEffects(ServerPlayer player) {
      for (MobEffectInstance active : new ArrayList<>(player.getActiveEffects())) {
         ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(active.getEffect());
         if (effectId != null && "sololeveling".equals(effectId.getNamespace())) {
            player.removeEffect(active.getEffect());
         }
      }
   }

   private static void restoreEscrowedArmor(ServerPlayer player, SololevelingModVariables.PlayerVariables variables, boolean activeEscrow, boolean wasEquipped) {
      if (activeEscrow) {
         if (wasEquipped) {
            restoreArmorSlot(player, EquipmentSlot.HEAD, variables.overridehead);
            restoreArmorSlot(player, EquipmentSlot.CHEST, variables.overridetorso);
            restoreArmorSlot(player, EquipmentSlot.LEGS, variables.overridelegs);
            restoreArmorSlot(player, EquipmentSlot.FEET, variables.overridefeet);
            player.getInventory().setChanged();
         }

         TemporaryArmorSessionManager.finishAfterRestore(player);
      }
   }

   private static void restoreArmorSlot(ServerPlayer player, EquipmentSlot slot, ItemStack saved) {
      ItemStack escrowed = saved == null ? ItemStack.EMPTY : saved;
      if (isTemporaryArmorForSlot(player.getItemBySlot(slot), slot)) {
         player.setItemSlot(slot, escrowed.copy());
      } else if (!escrowed.isEmpty()) {
         ItemStack returned = escrowed.copy();
         player.getInventory().add(returned);
         if (!returned.isEmpty()) {
            player.spawnAtLocation(returned);
         }
      }
   }

   private static boolean isTemporaryArmorForSlot(ItemStack stack, EquipmentSlot slot) {
      if (stack != null && !stack.isEmpty()) {
         Item item = stack.getItem();

         return switch (slot) {
            case HEAD -> item == SololevelingModItems.SHADOW_ARMOR_HELMET.get() || item == SololevelingModItems.GOLIATH_ARMOR_HELMET.get();
            case CHEST -> item == SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get() || item == SololevelingModItems.GOLIATH_ARMOR_CHESTPLATE.get();
            case LEGS -> item == SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get() || item == SololevelingModItems.GOLIATH_ARMOR_LEGGINGS.get();
            case FEET -> item == SololevelingModItems.SHADOW_ARMOR_BOOTS.get() || item == SololevelingModItems.GOLIATH_ARMOR_BOOTS.get();
            default -> false;
         };
      } else {
         return false;
      }
   }

   private static boolean hasEquippedTemporaryArmor(ServerPlayer player) {
      return isTemporaryArmorForSlot(player.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD)
         || isTemporaryArmorForSlot(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)
         || isTemporaryArmorForSlot(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS)
         || isTemporaryArmorForSlot(player.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
   }

   private static SololevelingModVariables.PlayerVariables variables(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private record PreservedState(boolean systemPlayer, double loreAccurateRankStart, boolean customHud, boolean pvpUrgentQuests, String party, double guildCode) {
      private static PlayerProgressResetManager.PreservedState capture(SololevelingModVariables.PlayerVariables variables) {
         return new PlayerProgressResetManager.PreservedState(
            variables.Player,
            variables.LoreAccurateRankStart,
            variables.CustomHUD,
            variables.pvpUrgentQuests,
            variables.party == null ? "" : variables.party,
            variables.GuildCode
         );
      }

      private void restore(SololevelingModVariables.PlayerVariables variables) {
         variables.Player = this.systemPlayer;
         variables.LoreAccurateRankStart = this.loreAccurateRankStart;
         variables.CustomHUD = this.customHud;
         variables.pvpUrgentQuests = this.pvpUrgentQuests;
         variables.party = this.party;
         variables.GuildCode = this.guildCode;
      }
   }
}
