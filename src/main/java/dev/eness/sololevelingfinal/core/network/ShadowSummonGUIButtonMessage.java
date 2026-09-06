package dev.eness.sololevelingfinal.core.network;

import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowCustomizationMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowDismissMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowSummonGUIMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class ShadowSummonGUIButtonMessage {
   public static final int HEAL_BOSS_SHADOWS_BUTTON_ID = 102;
   public static final int HEAL_ALL_SHADOWS_BUTTON_ID = 103;
   private static final int CUSTOMIZE_BUTTON_OFFSET = 200;
   private static final int GRAND_MARSHAL_BUTTON_OFFSET = 300;
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;
   private final String payload;

   public ShadowSummonGUIButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
      this.payload = buffer.readUtf(64);
   }

   public ShadowSummonGUIButtonMessage(int buttonID, int x, int y, int z) {
      this(buttonID, x, y, z, "");
   }

   public ShadowSummonGUIButtonMessage(int buttonID, int x, int y, int z, String payload) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
      this.payload = payload == null ? "" : payload;
   }

   public static void buffer(ShadowSummonGUIButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
      buffer.writeUtf(message.payload, 64);
   }

   public static void handler(ShadowSummonGUIButtonMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         Player entity = context.getSender();
         int buttonID = message.buttonID;
         int x = message.x;
         int y = message.y;
         int z = message.z;
         handleButtonAction(entity, buttonID, x, y, z, message.payload);
      });
      context.setPacketHandled(true);
   }

   public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
      handleButtonAction(entity, buttonID, x, y, z, "");
   }

   public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z, String payload) {
      if (entity != null) {
         Level world = entity.level();
         HashMap guistate = ShadowSummonGUIMenu.guistate;
         if (world.hasChunkAt(new BlockPos(x, y, z))) {
            if (buttonID >= 300 && buttonID < 314) {
               assignGrandMarshal(entity, buttonID - 300, x, y, z);
            } else if (buttonID >= 200 && buttonID < 214) {
               openCustomization(entity, buttonID - 200, x, y, z);
            } else if (buttonID == 100) {
               String name = ShadowMonarchManager.saveFormationFromSummoned(entity, payload);
               if (!name.isEmpty() && !entity.level().isClientSide()) {
                  entity.displayClientMessage(Component.literal("Saved formation: " + name), true);
               }
            } else if (buttonID == 101) {
               openDismiss(entity, x, y, z);
            } else if (buttonID != 102 && buttonID != 103) {
               String type = ShadowMonarchManager.typeForSummonButton(buttonID);
               if (!type.isEmpty()) {
                  if ("all".equalsIgnoreCase(payload)) {
                     ShadowMonarchManager.summonAllOfType(world, x, y, z, entity, type);
                  } else {
                     ShadowMonarchManager.summonType(world, x, y, z, entity, type);
                  }
               }
            } else {
               healSummonedShadows(entity, buttonID == 102, x, y, z);
            }
         }
      }
   }

   private static void healSummonedShadows(Player entity, boolean bossesOnly, int x, int y, int z) {
      if (entity instanceof ServerPlayer serverPlayer
         && entity.containerMenu instanceof ShadowSummonGUIMenu summonMenu
         && summonMenu.x == x
         && summonMenu.y == y
         && summonMenu.z == z) {
         ShadowMonarchManager.ShadowHealingResult result = ShadowMonarchManager.healSummonedShadows(serverPlayer, bossesOnly);
         serverPlayer.displayClientMessage(Component.literal(result.message()).withStyle(result.success() ? ChatFormatting.AQUA : ChatFormatting.RED), true);
      }
   }

   private static void assignGrandMarshal(Player entity, int summonButtonId, int x, int y, int z) {
      if (entity instanceof ServerPlayer serverPlayer
         && entity.containerMenu instanceof ShadowSummonGUIMenu summonMenu
         && summonMenu.x == x
         && summonMenu.y == y
         && summonMenu.z == z) {
         String type = ShadowMonarchManager.typeForSummonButton(summonButtonId);
         if (ShadowMonarchManager.hasShadowForDisplay(serverPlayer, type)) {
            ShadowMonarchManager.GrandMarshalAssignmentResult result = ShadowMonarchManager.assignGrandMarshal(serverPlayer, type);
            serverPlayer.displayClientMessage(Component.literal(result.message()), !result.success());
         }
      }
   }

   private static void openDismiss(Player entity, int x, int y, int z) {
      if (entity instanceof ServerPlayer serverPlayer) {
         final BlockPos pos = new BlockPos(x, y, z);
         NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
               return Component.literal("Shadow Dismiss");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
               return new ShadowDismissMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
            }
         }, pos);
      }
   }

   private static void openCustomization(Player entity, int summonButtonId, int x, int y, int z) {
      if (entity instanceof ServerPlayer serverPlayer
         && entity.containerMenu instanceof ShadowSummonGUIMenu summonMenu
         && summonMenu.x == x
         && summonMenu.y == y
         && summonMenu.z == z) {
         String type = ShadowMonarchManager.typeForSummonButton(summonButtonId);
         if (!type.isEmpty() && ShadowMonarchManager.hasShadowForDisplay(serverPlayer, type)) {
            BlockPos pos = new BlockPos(x, y, z);
            final Consumer<FriendlyByteBuf> writer = data -> {
               data.writeBlockPos(pos);
               data.writeUtf(type, 24);

               for (String candidate : ShadowMonarchManager.customizableTypes()) {
                  data.writeBoolean(ShadowMonarchManager.hasShadowForDisplay(serverPlayer, candidate));
                  data.writeInt(ShadowMonarchManager.glowColor(serverPlayer, candidate));
               }
            };
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
               @Override
               public Component getDisplayName() {
                  return Component.literal("Shadow Customization");
               }

               @Override
               public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                  FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
                  writer.accept(data);
                  return new ShadowCustomizationMenu(id, inventory, data);
               }
            }, writer::accept);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         ShadowSummonGUIButtonMessage.class, ShadowSummonGUIButtonMessage::buffer, ShadowSummonGUIButtonMessage::new, ShadowSummonGUIButtonMessage::handler
      );
   }
}
