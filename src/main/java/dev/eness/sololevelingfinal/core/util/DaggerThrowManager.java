package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.ThrownDaggerEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE)
public final class DaggerThrowManager {
   public static final String DAGGER_THROW = "Dagger Throw";
   public static final String DAGGER_RUSH = "Dagger Rush";
   public static final String DAGGER_THROW_COOLDOWN = "dagger_throw";
   public static final String DAGGER_RUSH_COOLDOWN = "dagger_rush_projectile";
   public static final String RECOVERY_PREFIX = "DAGGER_RECOVERY:";
   private static final String ESCROW_KEY = "slr_dagger_throw_escrow";
   private static final double DAGGER_THROW_BASE_MANA = 160.0;
   private static final double DAGGER_THROW_DAMAGE_MANA_SCALE = 9.0;
   private static final double DAGGER_RUSH_BASE_MANA = 220.0;
   private static final double DAGGER_RUSH_PER_DAGGER_BASE_MANA = 110.0;
   private static final double DAGGER_RUSH_DAMAGE_MANA_SCALE = 12.0;
   private static final double DAGGER_RUSH_EXPONENTIAL_FACTOR = 1.32;
   private static final Map<UUID, ThrownDaggerEntity> ACTIVE = new ConcurrentHashMap<>();

   private DaggerThrowManager() {
   }

   public static void castThrow(ServerPlayer player) {
      if (player != null && player.isAlive()) {
         CompoundTag escrow = escrow(player, false);
         if (escrow != null && escrow.hasUUID("Token")) {
            UUID token = escrow.getUUID("Token");
            ThrownDaggerEntity active = ACTIVE.get(token);
            if (active != null && active.isAlive()) {
               if (!RulersAuthorityManager.hasAuthority(player)) {
                  return;
               }

               active.beginReturn();
               player.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.recalling").withStyle(ChatFormatting.AQUA), true);
            } else {
               ensureRecoveryReward(player, escrow);
               player.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.recovery_ready").withStyle(ChatFormatting.YELLOW), true);
            }
         } else {
            DaggerThrowManager.HeldDagger held = findHeldDagger(player);
            if (held == null) {
               player.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.requires_dagger").withStyle(ChatFormatting.RED), true);
            } else if (player.isCreative() || !CooldownManager.isOnCooldown(player, "dagger_throw")) {
               double manaCost = daggerThrowManaCost(held.stack);
               if (!consumeMana(player, manaCost)) {
                  player.displayClientMessage(Component.translatable("message.sololeveling.not_enough_mana").withStyle(ChatFormatting.RED), true);
               } else {
                  ItemStack exact = held.stack.copy();
                  exact.setCount(1);
                  UUID token = UUID.randomUUID();
                  CompoundTag saved = escrow(player, true);
                  saved.putUUID("Token", token);
                  saved.put("Item", exact.save(new CompoundTag()));
                  saved.putInt("Slot", held.slot);
                  saved.putString("Reward", recoveryReward(token, exact));
                  held.stack.shrink(1);
                  Vec3 look = player.getLookAngle().normalize();
                  ThrownDaggerEntity dagger = ThrownDaggerEntity.createPhysical(
                     player, exact, token, player.getEyePosition().add(look.scale(0.65)).add(0.0, -0.18, 0.0), look.scale(2.35)
                  );
                  if (!player.serverLevel().addFreshEntity(dagger)) {
                     clearEscrow(player);
                     restoreDirectly(player, exact, held.slot);
                  } else {
                     register(dagger);
                     RewardManager.appendReward(player, recoveryReward(token, exact));
                     setAssassinCooldown(player, "dagger_throw", daggerThrowCooldownTicks(exact));
                     CooldownManager.set(player, "mana_refresh", 30);
                  }
               }
            }
         }
      }
   }

   public static void castRush(ServerPlayer player) {
      if (player != null && player.isAlive() && RulersAuthorityManager.hasAuthority(player)) {
         List<ItemStack> daggers = inventoryDaggers(player);
         if (daggers.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.requires_dagger").withStyle(ChatFormatting.RED), true);
         } else if (player.isCreative() || !CooldownManager.isOnCooldown(player, "dagger_rush_projectile")) {
            double manaCost = daggerRushManaCost(daggers);
            if (!consumeMana(player, manaCost)) {
               player.displayClientMessage(Component.translatable("message.sololeveling.not_enough_mana").withStyle(ChatFormatting.RED), true);
            } else {
               Vec3 forward = player.getLookAngle().normalize();
               Vec3 right = forward.cross(new Vec3(0.0, 1.0, 0.0));
               if (right.lengthSqr() < 0.001) {
                  right = new Vec3(1.0, 0.0, 0.0);
               }

               right = right.normalize();

               for (int i = 0; i < daggers.size(); i++) {
                  double offset = (i - (daggers.size() - 1) * 0.5) * 0.23;
                  Vec3 direction = forward.add(right.scale(offset * 0.16)).normalize();
                  Vec3 origin = player.getEyePosition().add(right.scale(offset)).add(direction.scale(0.5)).add(0.0, -0.2 + Math.abs(offset) * 0.08, 0.0);
                  ThrownDaggerEntity spectral = ThrownDaggerEntity.createSpectral(player, daggers.get(i), origin, direction.scale(2.75 + i % 2 * 0.12), i * 2);
                  player.serverLevel().addFreshEntity(spectral);
               }

               setAssassinCooldown(player, "dagger_rush_projectile", daggerRushCooldownTicks(daggers.size()));
               CooldownManager.set(player, "mana_refresh", 40);
            }
         }
      }
   }

   public static boolean isDagger(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.is(ItemTags.create(new ResourceLocation("dagger")));
   }

   public static float physicalDamage(ServerPlayer player) {
      double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      double agility = TemporaryStatBonusManager.effectiveAgility(player);
      return (float)Math.max(4.0, attack * 1.35 + agility * 0.12);
   }

   public static float rushDamage(ServerPlayer player) {
      double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      double agility = TemporaryStatBonusManager.effectiveAgility(player);
      return (float)Math.max(2.0, attack * 0.52 + agility * 0.045);
   }

   public static void register(ThrownDaggerEntity entity) {
      if (entity != null && entity.isPhysical() && entity.getEscrowToken() != null) {
         ACTIVE.put(entity.getEscrowToken(), entity);
      }
   }

   public static void unregister(ThrownDaggerEntity entity) {
      if (entity != null && entity.getEscrowToken() != null) {
         ACTIVE.remove(entity.getEscrowToken(), entity);
      }
   }

   public static boolean isAuthorized(ServerPlayer owner, UUID token) {
      CompoundTag escrow = escrow(owner, false);
      return escrow != null && token != null && escrow.hasUUID("Token") && token.equals(escrow.getUUID("Token"));
   }

   public static void updateEscrowItem(ServerPlayer owner, UUID token, ItemStack stack) {
      if (isAuthorized(owner, token)) {
         if (stack.isEmpty()) {
            String reward = rewardFor(owner);
            clearEscrow(owner);
            RewardManager.removeReward(owner, reward);
            owner.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.broken").withStyle(ChatFormatting.RED), true);
         } else {
            CompoundTag escrow = escrow(owner, false);
            escrow.put("Item", stack.save(new CompoundTag()));
         }
      }
   }

   public static boolean completeReturn(ServerPlayer owner, UUID token, ThrownDaggerEntity entity) {
      if (!isAuthorized(owner, token)) {
         return false;
      } else {
         CompoundTag escrow = escrow(owner, false);
         ItemStack stack = ItemStack.of(escrow.getCompound("Item"));
         int slot = escrow.getInt("Slot");
         if (stack.isEmpty()) {
            clearEscrow(owner);
            return false;
         } else if (!canAccept(owner, slot)) {
            ensureRecoveryReward(owner, escrow);
            owner.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.inventory_full").withStyle(ChatFormatting.RED), true);
            return false;
         } else {
            String reward = escrow.getString("Reward");
            clearEscrow(owner);
            unregister(entity);
            restoreDirectly(owner, stack, slot);
            RewardManager.removeReward(owner, reward);
            return true;
         }
      }
   }

   public static boolean claimRecovery(ServerPlayer owner, String reward) {
      CompoundTag escrow = escrow(owner, false);
      if (escrow != null && reward.equals(escrow.getString("Reward"))) {
         ItemStack stack = ItemStack.of(escrow.getCompound("Item"));
         int slot = escrow.getInt("Slot");
         if (stack.isEmpty()) {
            return true;
         }

         if (!canAccept(owner, slot)) {
            owner.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.inventory_full").withStyle(ChatFormatting.RED), true);
            return false;
         }

         UUID token = escrow.getUUID("Token");
         ThrownDaggerEntity active = ACTIVE.remove(token);
         clearEscrow(owner);
         if (active != null && active.isAlive()) {
            active.discardAsRecovered();
         }

         restoreDirectly(owner, stack, slot);
         owner.displayClientMessage(
            Component.translatable("message.sololeveling.dagger_throw.recovered", stack.getHoverName()).withStyle(ChatFormatting.AQUA), false
         );
         return true;
      } else {
         owner.displayClientMessage(Component.translatable("message.sololeveling.dagger_throw.recovery_expired").withStyle(ChatFormatting.GRAY), true);
         return true;
      }
   }

   public static void recoverEscrowForReset(ServerPlayer owner) {
      if (owner != null) {
         CompoundTag saved = escrow(owner, false);
         if (saved != null) {
            ItemStack stack = ItemStack.of(saved.getCompound("Item"));
            int preferredSlot = saved.getInt("Slot");
            String reward = saved.getString("Reward");
            UUID token = saved.hasUUID("Token") ? saved.getUUID("Token") : null;
            ThrownDaggerEntity active = token == null ? null : ACTIVE.remove(token);
            if (active != null && active.isAlive()) {
               active.discardAsRecovered();
            }

            clearEscrow(owner);
            if (!reward.isBlank()) {
               RewardManager.removeReward(owner, reward);
            }

            if (!stack.isEmpty()) {
               if (canAccept(owner, preferredSlot)) {
                  restoreDirectly(owner, stack, preferredSlot);
               } else {
                  owner.spawnAtLocation(stack);
               }
            }
         }
      }
   }

   public static String displayRecoveryName(String reward) {
      String[] parts = reward.split(":", 4);
      if (parts.length == 4) {
         try {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[2], parts[3]));
            if (item != null) {
               return Component.translatable("reward.sololeveling.dagger_recovery", new ItemStack(item).getHoverName()).getString();
            }
         } catch (Exception var3) {
         }
      }

      return Component.translatable("reward.sololeveling.dagger_recovery.generic").getString();
   }

   private static DaggerThrowManager.HeldDagger findHeldDagger(ServerPlayer player) {
      if (isDagger(player.getMainHandItem())) {
         return new DaggerThrowManager.HeldDagger(player.getMainHandItem(), player.getInventory().selected);
      } else {
         return isDagger(player.getOffhandItem()) ? new DaggerThrowManager.HeldDagger(player.getOffhandItem(), 40) : null;
      }
   }

   private static List<ItemStack> inventoryDaggers(ServerPlayer player) {
      List<ItemStack> daggers = new ArrayList<>();

      for (ItemStack stack : player.getInventory().items) {
         addDaggerCopies(daggers, stack);
      }

      for (ItemStack stack : player.getInventory().offhand) {
         addDaggerCopies(daggers, stack);
      }

      return daggers;
   }

   private static void addDaggerCopies(List<ItemStack> daggers, ItemStack stack) {
      if (isDagger(stack)) {
         for (int i = 0; i < stack.getCount(); i++) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            daggers.add(copy);
         }
      }
   }

   private static double daggerThrowManaCost(ItemStack dagger) {
      return 160.0 + daggerMeleeDamage(dagger) * 9.0;
   }

   private static int daggerThrowCooldownTicks(ItemStack dagger) {
      return Mth.clamp((int)Math.round(32.0 + daggerMeleeDamage(dagger) * 1.4), 35, 58);
   }

   private static double daggerRushManaCost(List<ItemStack> daggers) {
      int count = Math.max(1, daggers.size());
      double totalDamage = 0.0;

      for (ItemStack dagger : daggers) {
         totalDamage += daggerMeleeDamage(dagger);
      }

      double averageDamage = totalDamage / count;
      double growth = Math.pow(1.32, Math.min(32, count - 1));
      double raw = 220.0 + count * (110.0 + averageDamage * 12.0) * growth;
      return Math.min(2000000.0, raw);
   }

   private static int daggerRushCooldownTicks(int daggerCount) {
      int count = Math.max(1, daggerCount);
      double raw = 120.0 + count * 25.0 + Math.pow(1.18, Math.min(24, count)) * 20.0;
      return Mth.clamp((int)Math.round(raw), 160, 720);
   }

   private static double daggerMeleeDamage(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         double addition = 0.0;
         double multiplier = 1.0;

         for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == Operation.ADDITION) {
               addition += modifier.getAmount();
            } else if (modifier.getOperation() == Operation.MULTIPLY_BASE) {
               multiplier += modifier.getAmount();
            } else if (modifier.getOperation() == Operation.MULTIPLY_TOTAL) {
               multiplier *= 1.0 + modifier.getAmount();
            }
         }

         double enchantment = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
         return Mth.clamp(addition * multiplier + enchantment, 4.0, 40.0);
      } else {
         return 4.0;
      }
   }

   private static void setAssassinCooldown(ServerPlayer player, String key, int durationTicks) {
      if (player.isCreative()) {
         CooldownManager.clear(player, key);
      } else {
         CooldownManager.setFullDuration(player, key, durationTicks);
      }
   }

   private static boolean consumeMana(ServerPlayer player, double cost) {
      if (player.isCreative()) {
         return true;
      } else {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
         if (vars != null && !(vars.MP < cost)) {
            vars.MP -= cost;
            vars.syncPlayerVariables(player);
            return true;
         } else {
            return false;
         }
      }
   }

   private static String recoveryReward(UUID token, ItemStack stack) {
      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return "DAGGER_RECOVERY:" + token + ":" + (id == null ? "minecraft:air" : id);
   }

   private static String rewardFor(ServerPlayer player) {
      CompoundTag escrow = escrow(player, false);
      return escrow == null ? "" : escrow.getString("Reward");
   }

   private static void ensureRecoveryReward(ServerPlayer player, CompoundTag escrow) {
      String reward = escrow.getString("Reward");
      if (!reward.isBlank() && !RewardManager.allRewards(player).contains(reward)) {
         RewardManager.appendReward(player, reward);
      }
   }

   private static CompoundTag escrow(Player player, boolean create) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         if (!create) {
            return null;
         }

         root.put("PlayerPersisted", new CompoundTag());
      }

      CompoundTag persisted = root.getCompound("PlayerPersisted");
      if (!persisted.contains("slr_dagger_throw_escrow", 10)) {
         if (!create) {
            return null;
         }

         persisted.put("slr_dagger_throw_escrow", new CompoundTag());
      }

      return persisted.getCompound("slr_dagger_throw_escrow");
   }

   private static void clearEscrow(Player player) {
      CompoundTag root = player.getPersistentData();
      if (root.contains("PlayerPersisted", 10)) {
         root.getCompound("PlayerPersisted").remove("slr_dagger_throw_escrow");
      }
   }

   private static boolean canAccept(ServerPlayer player, int preferredSlot) {
      if (preferredSlot == 40 && player.getOffhandItem().isEmpty()) {
         return true;
      } else {
         return preferredSlot >= 0 && preferredSlot < player.getInventory().items.size() && player.getInventory().getItem(preferredSlot).isEmpty()
            ? true
            : player.getInventory().getFreeSlot() >= 0;
      }
   }

   private static void restoreDirectly(ServerPlayer player, ItemStack stack, int preferredSlot) {
      if (preferredSlot == 40 && player.getOffhandItem().isEmpty()) {
         player.setItemSlot(EquipmentSlot.OFFHAND, stack);
      } else if (preferredSlot >= 0 && preferredSlot < player.getInventory().items.size() && player.getInventory().getItem(preferredSlot).isEmpty()) {
         player.getInventory().setItem(preferredSlot, stack);
      } else {
         player.getInventory().add(stack);
      }
   }

   @SubscribeEvent
   public static void onClone(Clone event) {
      CompoundTag oldEscrow = escrow(event.getOriginal(), false);
      if (oldEscrow != null) {
         escrow(event.getEntity(), true).merge(oldEscrow.copy());
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CompoundTag escrow = escrow(player, false);
         if (escrow != null) {
            ensureRecoveryReward(player, escrow);
         }
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      ACTIVE.clear();
   }

   private record HeldDagger(ItemStack stack, int slot) {
   }
}
