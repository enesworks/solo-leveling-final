package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class TitleManager {
   public static final int NONE = 0;
   public static final int WOLF_ASSASSIN = 1;
   public static final String WOLF_ASSASSIN_KEY = "wolf_assassin";
   public static final int WOLF_ASSASSIN_REQUIRED_KILLS = 20;
   private static final UUID WOLF_ASSASSIN_SPEED_ID = UUID.fromString("2ba3c4d2-2cf0-42f9-bf4d-89a296e1c304");
   private static final String WOLF_ASSASSIN_SPEED_NAME = "Wolf Assassin title speed";

   private TitleManager() {
   }

   public static String displayName(int titleId) {
      return titleId == 1 ? "Wolf Assassin" : "None";
   }

   public static boolean isUnlocked(SololevelingModVariables.PlayerVariables vars, int titleId) {
      if (titleId == 0) {
         return true;
      } else {
         return vars.title == 1.0 && titleId == 1 ? true : titleId == 1 && hasToken(vars.unlockedTitles, "wolf_assassin");
      }
   }

   public static List<Component> tooltip(SololevelingModVariables.PlayerVariables vars, int titleId) {
      List<Component> lines = new ArrayList<>();
      if (titleId == 0) {
         lines.add(Component.literal("None").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
         lines.add(Component.literal("No title effect equipped.").withStyle(ChatFormatting.DARK_GRAY));
         return lines;
      }

      if (titleId == 1) {
         boolean unlocked = isUnlocked(vars, 1);
         lines.add(Component.literal("Wolf Assassin").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
         if (unlocked) {
            lines.add(Component.literal("+10% damage vs wolves and lycans").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("+3% movement speed").withStyle(ChatFormatting.GRAY));
         } else {
            lines.add(Component.literal("Locked").withStyle(ChatFormatting.RED));
            lines.add(Component.literal((int)Math.min(vars.wolfAssassinKills, 20.0) + "/20 wolf-family kills").withStyle(ChatFormatting.GRAY));
         }
      }

      return lines;
   }

   public static boolean selectTitle(ServerPlayer player, int titleId) {
      if (player != null && titleId >= 0 && titleId <= 1) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            ensureLegacyUnlock(vars);
            if (isUnlocked(vars, titleId)) {
               vars.title = titleId;
               vars.syncPlayerVariables(player);
            }
         });
         applySpeed(player);
         return true;
      } else {
         return false;
      }
   }

   public static boolean isWolfFamily(Entity entity) {
      return entity instanceof SteelFangWolfEntity || entity instanceof SteelFangedLycanEntity;
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onLivingHurt(LivingHurtEvent event) {
      if (isWolfFamily(event.getEntity())) {
         ServerPlayer owner = owningPlayer(event.getSource().getEntity());
         if (owner != null) {
            SololevelingModVariables.PlayerVariables vars = owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables());
            ensureLegacyUnlock(vars);
            if ((int)vars.title == 1 && isUnlocked(vars, 1)) {
               event.setAmount(event.getAmount() * 1.1F);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (isWolfFamily(event.getEntity())) {
         ServerPlayer owner = owningPlayer(event.getSource().getEntity());
         if (owner != null) {
            owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .ifPresent(
                  vars -> {
                     ensureLegacyUnlock(vars);
                     if (!isUnlocked(vars, 1)) {
                        vars.wolfAssassinKills = Math.min(20.0, vars.wolfAssassinKills + 1.0);
                        if (vars.wolfAssassinKills >= 20.0) {
                           vars.unlockedTitles = addToken(vars.unlockedTitles, "wolf_assassin");
                           SystemNotifications.showTitleUnder(
                              owner,
                              -18371,
                              110,
                              Component.literal("TITLE UNLOCKED").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                              Component.literal("Wolf Assassin").withStyle(ChatFormatting.WHITE)
                           );
                        }

                        vars.syncPlayerVariables(owner);
                     }
                  }
               );
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         if (player.tickCount % 20 == 0) {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(TitleManager::ensureLegacyUnlock);
            applySpeed(player);
         }
      }
   }

   private static void applySpeed(ServerPlayer player) {
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         AttributeModifier old = speed.getModifier(WOLF_ASSASSIN_SPEED_ID);
         if (old != null) {
            speed.removeModifier(WOLF_ASSASSIN_SPEED_ID);
         }

         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         ensureLegacyUnlock(vars);
         if ((int)vars.title == 1 && isUnlocked(vars, 1)) {
            speed.addTransientModifier(new AttributeModifier(WOLF_ASSASSIN_SPEED_ID, "Wolf Assassin title speed", 0.03, Operation.MULTIPLY_TOTAL));
         }
      }
   }

   private static ServerPlayer owningPlayer(Entity source) {
      if (source instanceof ServerPlayer player) {
         return player;
      } else if (source instanceof TamableAnimal tame && tame.getOwner() instanceof ServerPlayer owner) {
         return owner;
      } else if (source instanceof Projectile projectile && projectile.getOwner() != null) {
         return owningPlayer(projectile.getOwner());
      } else {
         if (source != null) {
            UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(source);
            if (ownerId != null && source.getServer() != null) {
               return source.getServer().getPlayerList().getPlayer(ownerId);
            }
         }

         return null;
      }
   }

   private static void ensureLegacyUnlock(SololevelingModVariables.PlayerVariables vars) {
      if ((int)vars.title == 1 && !hasToken(vars.unlockedTitles, "wolf_assassin")) {
         vars.unlockedTitles = addToken(vars.unlockedTitles, "wolf_assassin");
      }
   }

   private static boolean hasToken(String csv, String token) {
      if (csv != null && !csv.isBlank()) {
         for (String part : csv.split(",")) {
            if (part.trim().equals(token)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static String addToken(String csv, String token) {
      if (hasToken(csv, token)) {
         return csv;
      } else {
         return csv != null && !csv.isBlank() ? csv + "," + token : token;
      }
   }
}
