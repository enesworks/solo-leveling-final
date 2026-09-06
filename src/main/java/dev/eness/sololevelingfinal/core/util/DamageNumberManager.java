package dev.eness.sololevelingfinal.core.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.ShowDamageNumberMessage;

@EventBusSubscriber
public final class DamageNumberManager {
   private static final int OUTGOING = -3928;
   private static final int OUTGOING_HEAVY = -30172;
   private static final int INCOMING = -49088;

   private DamageNumberManager() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingHurt(LivingHurtEvent event) {
      if (!(event.getAmount() <= 0.0F) && !event.getEntity().level().isClientSide()) {
         if (!event.getEntity().getPersistentData().getBoolean("radiru_training_dummy")) {
            Set<ServerPlayer> recipients = new HashSet<>();
            ServerPlayer owner = owningPlayer(event.getSource().getEntity());
            if (owner != null) {
               recipients.add(owner);
               send(owner, event.getEntity(), event.getAmount(), event.getAmount() >= 20.0F ? -30172 : -3928);
            }

            if (event.getEntity() instanceof ServerPlayer victim && recipients.add(victim)) {
               send(victim, event.getEntity(), event.getAmount(), -49088);
            }
         }
      }
   }

   private static void send(ServerPlayer player, Entity target, float amount, int color) {
      double x = target.getX();
      double y = target.getY() + target.getBbHeight() + 0.35;
      double z = target.getZ();
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ShowDamageNumberMessage(x, y, z, amount, color));
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
}
