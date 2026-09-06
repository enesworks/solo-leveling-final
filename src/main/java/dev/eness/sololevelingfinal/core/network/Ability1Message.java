package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.procedures.Ability1OnKeyPressedProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability1OnKeyReleasedProcedure;
import dev.eness.sololevelingfinal.core.util.BeastMonarchManager;
import dev.eness.sololevelingfinal.core.util.GoliathCombatManager;
import dev.eness.sololevelingfinal.core.util.LiuZhigangCombatManager;

@EventBusSubscriber(bus = Bus.MOD)
public class Ability1Message {
   int type;
   int pressedms;

   public Ability1Message(int type, int pressedms) {
      this.type = type;
      this.pressedms = pressedms;
   }

   public Ability1Message(FriendlyByteBuf buffer) {
      this.type = buffer.readInt();
      this.pressedms = buffer.readInt();
   }

   public static void buffer(Ability1Message message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.type);
      buffer.writeInt(message.pressedms);
   }

   public static void handler(Ability1Message message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> pressAction(context.getSender(), message.type, message.pressedms));
      context.setPacketHandled(true);
   }

   public static void pressAction(Player entity, int type, int pressedms) {
      Level world = entity.level();
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      if (world.hasChunkAt(entity.blockPosition())) {
         if (type == 0) {
            Ability1OnKeyPressedProcedure.execute(world, x, y, z, entity);
         }

         if (type == 1) {
            if (BeastMonarchManager.isFangStance(entity)) {
               BeastMonarchManager.releasePredatorsIntercept(entity, pressedms);
            } else if (GoliathCombatManager.isCombatStance(entity)) {
               GoliathCombatManager.releasePursuit(entity, pressedms);
            } else if (LiuZhigangCombatManager.isCombatStance(entity)) {
               LiuZhigangCombatManager.releaseDragonFlash(entity, pressedms);
            } else {
               Ability1OnKeyReleasedProcedure.execute(world, x, y, z, entity);
            }
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(Ability1Message.class, Ability1Message::buffer, Ability1Message::new, Ability1Message::handler);
   }
}
