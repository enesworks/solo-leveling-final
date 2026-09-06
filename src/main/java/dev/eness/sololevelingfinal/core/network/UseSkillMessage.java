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
import dev.eness.sololevelingfinal.core.client.gui.FrostArchitectureClientState;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;
import dev.eness.sololevelingfinal.core.procedures.UseSkillOnKeyPressedProcedure;
import dev.eness.sololevelingfinal.core.procedures.UseSkillOnKeyReleasedProcedure;
import dev.eness.sololevelingfinal.core.util.MageQTEHelper;

@EventBusSubscriber(bus = Bus.MOD)
public class UseSkillMessage {
   int type;
   int pressedms;

   public UseSkillMessage(int type, int pressedms) {
      this.type = type;
      this.pressedms = pressedms;
   }

   public UseSkillMessage(FriendlyByteBuf buffer) {
      this.type = buffer.readInt();
      this.pressedms = buffer.readInt();
   }

   public static void buffer(UseSkillMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.type);
      buffer.writeInt(message.pressedms);
   }

   public static void handler(UseSkillMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> pressAction(context.getSender(), message.type, message.pressedms));
      context.setPacketHandled(true);
   }

   public static void pressAction(Player entity, int type, int pressedms) {
      if (entity != null) {
         Level world = entity.level();
         double x = entity.getX();
         double y = entity.getY();
         double z = entity.getZ();
         if (world.hasChunkAt(entity.blockPosition())) {
            if (type == 0) {
               toggleSkillPage(entity);
            }

            if (type >= 10 && type <= 17) {
               pressHotbarSlot(entity, type - 9);
            }

            if (type >= 20 && type <= 27) {
               releaseHotbarSlot(entity, pressedms);
            }
         }
      }
   }

   private static void toggleSkillPage(Player entity) {
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.PskillPage = capability.PskillPage >= 2.0 ? 1.0 : 2.0;
         capability.syncPlayerVariables(entity);
      });
   }

   private static void pressHotbarSlot(Player entity, int hotbarSlot) {
      Level world = entity.level();
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         if (capability.combatmode) {
            int slot = SkillSlotHelper.activeSlot(entity, hotbarSlot);
            capability.PselectedPower = SkillSlotHelper.getSlot(capability, slot);
            capability.Skillcycle = hotbarSlot;
            capability.syncPlayerVariables(entity);
         }
      });
      SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      if (vars.combatmode && !vars.PselectedPower.isEmpty()) {
         if (world.isClientSide() && "Frozen Architecture".equals(vars.PselectedPower)) {
            FrostArchitectureClientState.begin(hotbarSlot);
         } else if (!world.isClientSide() || MageQTEHelper.MAGE_SKILLS.contains(vars.PselectedPower)) {
            UseSkillOnKeyPressedProcedure.execute(world, x, y, z, entity);
         }
      }
   }

   private static void releaseHotbarSlot(Player entity, int pressedms) {
      SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      if (vars.combatmode && !vars.PselectedPower.isEmpty()) {
         if (!entity.level().isClientSide() || !"Frozen Architecture".equals(vars.PselectedPower)) {
            if (!entity.level().isClientSide() || MageQTEHelper.MAGE_SKILLS.contains(vars.PselectedPower)) {
               UseSkillOnKeyReleasedProcedure.execute(entity.level(), entity, pressedms);
            }
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(UseSkillMessage.class, UseSkillMessage::buffer, UseSkillMessage::new, UseSkillMessage::handler);
   }
}
