package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class ShadowExchangeManager {
   public static final int MAX_ANCHORS = 7;
   private static final String ROOT = "sololeveling_shadow_exchange";
   private static final String ANCHORS = "anchors";
   private static final String MIGRATED = "legacy_migrated";
   private static final int CAST_DELAY_TICKS = 24;
   private static final int COOLDOWN_TICKS = 80;
   private static final int ACCENT_NEGATIVE = -49859;
   private static final int ACCENT_SUCCESS = -4760321;

   private ShadowExchangeManager() {
   }

   public static boolean saveAnchor(LevelAccessor world, Entity entity, String shadowType, String displayName) {
      if (entity instanceof ServerPlayer player) {
         if (!canUseExchange(player)) {
            player.closeContainer();
            negative(player, "EXCHANGE UNAVAILABLE", "This ability does not belong to your current vessel.");
            return false;
         } else if (isRestrictedDimension(player)) {
            negative(player, "EXCHANGE LOCKED", "You cannot set an anchor here.");
            return false;
         } else {
            ensureMigrated(player);
            String normalizedType = normalizeType(shadowType);
            if (!hasShadowType(player, normalizedType)) {
               negative(player, "ANCHOR FAILED", "You do not have that shadow.");
               return false;
            } else {
               ListTag anchors = anchors(player);
               if (anchors.size() >= 7) {
                  negative(player, "ANCHORS FULL", "Remove an exchange anchor first.");
                  return false;
               } else {
                  BlockPos safe = findSafeStandPos(player.serverLevel(), player.blockPosition());
                  if (safe == null) {
                     negative(player, "ANCHOR FAILED", "No safe ground found nearby.");
                     return false;
                  } else {
                     CompoundTag anchor = new CompoundTag();
                     anchor.putString("id", UUID.randomUUID().toString());
                     anchor.putString("name", cleanName(displayName, anchors.size() + 1));
                     anchor.putString("shadowType", normalizedType);
                     anchor.putString("dimension", player.level().dimension().location().toString());
                     anchor.putInt("x", safe.getX());
                     anchor.putInt("y", safe.getY());
                     anchor.putInt("z", safe.getZ());
                     anchor.putLong("created", player.level().getGameTime());
                     anchors.add(anchor);
                     saveAndSync(player);
                     player.closeContainer();
                     success(player, "EXCHANGE ANCHOR SET", anchor.getString("name"));
                     playQuiet(player.serverLevel(), player.position(), SololevelingModSounds.PANELOPEN.get(), 0.34F, 0.72F);
                     player.serverLevel()
                        .sendParticles(ParticleTypes.SMOKE, safe.getX() + 0.5, safe.getY() + 0.15, safe.getZ() + 0.5, 24, 0.45, 0.12, 0.45, 0.02);
                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   public static boolean startExchange(LevelAccessor world, Entity entity, int slot) {
      if (entity instanceof ServerPlayer player) {
         if (!canUseExchange(player)) {
            player.closeContainer();
            negative(player, "EXCHANGE UNAVAILABLE", "This ability does not belong to your current vessel.");
            return false;
         } else if (CooldownManager.isOnCooldown(player, "shadow_exchange")) {
            negative(player, "EXCHANGE UNAVAILABLE", "Skill is on cooldown.");
            return false;
         } else {
            ensureMigrated(player);
            CompoundTag anchor = anchor(player, slot);
            if (anchor == null) {
               negative(player, "EXCHANGE FAILED", "No anchor in that slot.");
               return false;
            } else {
               ServerLevel targetLevel = targetLevel(player, anchor);
               if (targetLevel == null) {
                  removeAnchor(player, slot);
                  negative(player, "EXCHANGE FAILED", "Target dimension no longer exists.");
                  return false;
               } else {
                  BlockPos requested = new BlockPos(anchor.getInt("x"), anchor.getInt("y"), anchor.getInt("z"));
                  BlockPos safe = findSafeStandPos(targetLevel, requested);
                  if (safe == null) {
                     negative(player, "EXCHANGE FAILED", "No safe landing point found.");
                     return false;
                  } else {
                     Vec3 start = player.position();
                     player.closeContainer();
                     CooldownManager.set(player, "shadow_exchange", 80);
                     playQuiet(player.serverLevel(), start, SololevelingModSounds.PANELOPEN.get(), 0.32F, 0.68F);
                     player.serverLevel().sendParticles(ParticleTypes.SMOKE, start.x, start.y + 0.2, start.z, 48, 0.6, 0.9, 0.6, 0.02);
                     player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 36, 0, false, false));
                     SololevelingMod.queueServerWork(24, () -> completeExchange(player, slot, anchor.getString("id")));
                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   public static boolean removeAnchor(Entity entity, int slot) {
      if (entity instanceof ServerPlayer player) {
         ensureMigrated(player);
         ListTag anchors = anchors(player);
         int index = slot - 1;
         if (index >= 0 && index < anchors.size()) {
            anchors.remove(index);
            saveAndSync(player);
            success(player, "ANCHOR REMOVED", "Shadow Exchange slot " + slot);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean hasAnchor(Entity entity, int slot) {
      return !anchorDisplay(entity, slot).isEmpty();
   }

   public static String anchorDisplay(Entity entity, int slot) {
      if (entity != null && slot >= 1 && slot <= 7) {
         if (!entity.level().isClientSide() && entity instanceof Player player) {
            ensureMigrated(player);
         }

         List<String> coords = legacyList(vars(entity).ExchangeCords);
         return slot - 1 >= coords.size() ? "" : coords.get(slot - 1);
      } else {
         return "";
      }
   }

   private static void completeExchange(ServerPlayer player, int slot, String anchorId) {
      if (player != null && player.isAlive()) {
         if (!canUseExchange(player)) {
            negative(player, "EXCHANGE CANCELED", "Your vessel changed before the exchange completed.");
         } else {
            ensureMigrated(player);
            CompoundTag anchor = anchor(player, slot);
            if (anchor != null && anchorId.equals(anchor.getString("id"))) {
               ServerLevel targetLevel = targetLevel(player, anchor);
               if (targetLevel == null) {
                  removeAnchor(player, slot);
                  negative(player, "EXCHANGE FAILED", "Target dimension no longer exists.");
               } else {
                  BlockPos safe = findSafeStandPos(targetLevel, new BlockPos(anchor.getInt("x"), anchor.getInt("y"), anchor.getInt("z")));
                  if (safe == null) {
                     negative(player, "EXCHANGE FAILED", "No safe landing point found.");
                  } else {
                     ServerLevel startLevel = player.serverLevel();
                     Vec3 start = player.position();
                     startLevel.sendParticles(ParticleTypes.SMOKE, start.x, start.y + 0.2, start.z, 64, 0.75, 1.1, 0.75, 0.02);
                     player.teleportTo(targetLevel, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, player.getYRot(), player.getXRot());
                     targetLevel.sendParticles(ParticleTypes.SMOKE, safe.getX() + 0.5, safe.getY() + 0.2, safe.getZ() + 0.5, 64, 0.75, 1.1, 0.75, 0.02);
                     playQuiet(targetLevel, Vec3.atBottomCenterOf(safe), SololevelingModSounds.PANELCLOSE.get(), 0.38F, 0.78F);
                     player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 8, 0, false, false));
                     player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 14, 0, false, false));
                     removeAnchorSilently(player, slot);
                     success(player, "SHADOW EXCHANGE", "Arrived at " + shortDimension(anchor.getString("dimension")) + ".");
                  }
               }
            } else {
               negative(player, "EXCHANGE FAILED", "The anchor changed during casting.");
            }
         }
      }
   }

   private static void removeAnchorSilently(ServerPlayer player, int slot) {
      ListTag anchors = anchors(player);
      int index = slot - 1;
      if (index >= 0 && index < anchors.size()) {
         anchors.remove(index);
         saveAndSync(player);
      }
   }

   private static CompoundTag anchor(Player player, int slot) {
      if (player != null && slot >= 1 && slot <= 7) {
         ListTag anchors = anchors(player);
         int index = slot - 1;
         return index >= 0 && index < anchors.size() ? anchors.getCompound(index) : null;
      } else {
         return null;
      }
   }

   private static ServerLevel targetLevel(ServerPlayer player, CompoundTag anchor) {
      ResourceLocation id = ResourceLocation.tryParse(anchor.getString("dimension"));
      if (id == null) {
         return null;
      }

      ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
      return player.server.getLevel(key);
   }

   private static BlockPos findSafeStandPos(ServerLevel level, BlockPos origin) {
      level.getChunkAt(origin);
      if (isSafeStandPos(level, origin)) {
         return origin;
      }

      for (int radius = 1; radius <= 4; radius++) {
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                  for (int dy = 2; dy >= -4; dy--) {
                     BlockPos pos = origin.offset(dx, dy, dz);
                     if (isSafeStandPos(level, pos)) {
                        return pos;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static boolean isSafeStandPos(ServerLevel level, BlockPos pos) {
      if (pos.getY() > level.getMinBuildHeight() + 1 && pos.getY() < level.getMaxBuildHeight() - 2) {
         BlockState floor = level.getBlockState(pos.below());
         return floor.isFaceSturdy(level, pos.below(), Direction.UP)
            && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
            && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
      } else {
         return false;
      }
   }

   private static boolean isRestrictedDimension(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables vars = vars(player);
      String dimension = player.level().dimension().location().getPath();
      return vars.dungeoning || dimension.contains("dungeon") || dimension.contains("castle");
   }

   private static boolean canUseExchange(ServerPlayer player) {
      return player != null && VesselProgressionManager.isShadowMonarch(player) && vars(player).ShadowExchange;
   }

   private static void ensureMigrated(Player player) {
      CompoundTag root = root(player);
      if (!root.getBoolean("legacy_migrated")) {
         ListTag anchors = anchors(player);
         if (anchors.isEmpty()) {
            List<String> coords = legacyList(vars(player).ExchangeCords);
            List<String> dims = legacyList(vars(player).ExchangeDimensions);
            int count = Math.min(7, coords.size());

            for (int i = 0; i < count; i++) {
               ShadowExchangeManager.LegacyCoord coord = parseCoord(coords.get(i));
               if (coord != null) {
                  String dim = i < dims.size() ? parseDimension(dims.get(i)) : player.level().dimension().location().toString();
                  CompoundTag anchor = new CompoundTag();
                  anchor.putString("id", UUID.randomUUID().toString());
                  anchor.putString("name", "Legacy Anchor " + (anchors.size() + 1));
                  anchor.putString("shadowType", "legacy");
                  anchor.putString("dimension", dim);
                  anchor.putInt("x", coord.x);
                  anchor.putInt("y", coord.y);
                  anchor.putInt("z", coord.z);
                  anchor.putLong("created", player.level().getGameTime());
                  anchors.add(anchor);
               }
            }
         }

         root.putBoolean("legacy_migrated", true);
         saveAndSync(player);
      }
   }

   private static void saveAndSync(Player player) {
      player.getPersistentData().put("sololeveling_shadow_exchange", root(player));
      syncLegacyMirror(player);
   }

   private static void syncLegacyMirror(Player player) {
      ListTag anchors = anchors(player);
      StringBuilder cords = new StringBuilder(".");
      StringBuilder dims = new StringBuilder(".");

      for (int i = 0; i < anchors.size(); i++) {
         CompoundTag anchor = anchors.getCompound(i);
         String coord = anchor.getInt("x") + " " + anchor.getInt("y") + " " + anchor.getInt("z");
         cords.append(displayName(anchor, i + 1)).append(" | ").append(coord).append(",");
         dims.append("execute in ").append(anchor.getString("dimension")).append(" run tp @p ").append(coord).append(",");
      }

      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.ExchangeCords = cords.toString();
         capability.ExchangeDimensions = dims.toString();
         capability.syncPlayerVariables(player);
      });
   }

   private static String displayName(CompoundTag anchor, int fallbackSlot) {
      String name = anchor.getString("name");
      return name != null && !name.isBlank() ? name : "Anchor " + fallbackSlot;
   }

   private static CompoundTag root(Entity entity) {
      CompoundTag data = entity.getPersistentData();
      if (!data.contains("sololeveling_shadow_exchange", 10)) {
         data.put("sololeveling_shadow_exchange", new CompoundTag());
      }

      CompoundTag root = data.getCompound("sololeveling_shadow_exchange");
      if (!root.contains("anchors", 9)) {
         root.put("anchors", new ListTag());
      }

      return root;
   }

   private static ListTag anchors(Entity entity) {
      return root(entity).getList("anchors", 10);
   }

   private static List<String> legacyList(String value) {
      ArrayList<String> result = new ArrayList<>();
      if (value != null && !value.isBlank()) {
         String cleaned = value.startsWith(".") ? value.substring(1) : value;

         for (String part : cleaned.split(",")) {
            String item = part == null ? "" : part.trim();
            if (!item.isEmpty() && !".".equals(item)) {
               result.add(item);
            }
         }

         return result;
      } else {
         return result;
      }
   }

   private static ShadowExchangeManager.LegacyCoord parseCoord(String value) {
      if (value != null && !value.isBlank()) {
         String coord = value.contains("|") ? value.substring(value.indexOf(124) + 1).trim() : value.trim();
         String[] parts = coord.split("\\s+");
         if (parts.length < 3) {
            return null;
         }

         try {
            return new ShadowExchangeManager.LegacyCoord(
               (int)Math.floor(Double.parseDouble(parts[0])), (int)Math.floor(Double.parseDouble(parts[1])), (int)Math.floor(Double.parseDouble(parts[2]))
            );
         } catch (NumberFormatException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String parseDimension(String command) {
      if (command == null) {
         return "minecraft:overworld";
      }

      int start = command.indexOf("execute in ");
      int end = command.indexOf(" run tp");
      return start >= 0 && end > start ? command.substring(start + "execute in ".length(), end).trim() : "minecraft:overworld";
   }

   private static String cleanName(String displayName, int slot) {
      String name = displayName == null ? "" : displayName.trim();
      if (name.isEmpty()) {
         name = "Anchor " + slot;
      }

      name = name.replace(',', ' ').replace('|', ' ');
      return name.length() > 24 ? name.substring(0, 24) : name;
   }

   private static String normalizeType(String type) {
      return type == null ? "" : type.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
   }

   private static boolean hasShadowType(Player player, String type) {
      SololevelingModVariables.PlayerVariables vars = vars(player);

      return switch (type) {
         case "knight" -> vars.ordshadowmax > 0.0;
         case "goblin_club" -> vars.GobShadowMax > 0.0;
         case "goblin_archer" -> vars.ShadowGoblinArcherMax > 0.0;
         case "goblin_mage" -> vars.ShadowGoblinMageMax > 0.0;
         case "wolf" -> vars.WolfShadowMax > 0.0;
         case "polar_bear" -> vars.polarbearmax > 0.0;
         case "orc" -> vars.orcmax > 0.0;
         case "high_orc" -> vars.highorcmax > 0.0;
         default -> false;
      };
   }

   private static String shortDimension(String dimension) {
      if (dimension != null && !dimension.isBlank()) {
         int colon = dimension.indexOf(58);
         return colon >= 0 ? dimension.substring(colon + 1).replace('_', ' ') : dimension.replace('_', ' ');
      } else {
         return "target";
      }
   }

   private static void playQuiet(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch) {
      level.playSound((Player)null, BlockPos.containing(pos), sound, SoundSource.PLAYERS, volume, pitch);
   }

   private static void success(ServerPlayer player, String title, String under) {
      SystemNotifications.showTitleUnder(
         player,
         -4760321,
         70,
         Component.literal(title).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
         Component.literal(under).withStyle(ChatFormatting.LIGHT_PURPLE)
      );
   }

   private static void negative(ServerPlayer player, String title, String under) {
      SystemNotifications.showNegativeTitleUnder(
         player,
         -49859,
         80,
         Component.literal(title).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
         Component.literal(under).withStyle(ChatFormatting.RED)
      );
   }

   private static SololevelingModVariables.PlayerVariables vars(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private record LegacyCoord(int x, int y, int z) {
   }
}
