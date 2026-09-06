package dev.eness.sololevelingfinal.core.item;

import io.netty.buffer.Unpooled;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.item.inventory.HunterIDInventoryCapability;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.world.inventory.HunterIDGuiMenu;

public class HunterIDItem extends Item {
   private static final String OWNER_TAG = "EvaluationOwner";

   public HunterIDItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, final Player entity, final InteractionHand hand) {
      InteractionResultHolder<ItemStack> result = super.use(world, entity, hand);
      if (entity instanceof ServerPlayer serverPlayer) {
         NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
               return Component.literal("Hunter ID");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
               FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
               packetBuffer.writeBlockPos(entity.blockPosition());
               packetBuffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
               return new HunterIDGuiMenu(id, inventory, packetBuffer);
            }
         }, buffer -> {
            buffer.writeBlockPos(entity.blockPosition());
            buffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
         });
      }

      return result;
   }

   @Override
   public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(itemstack, world, entity, slot, selected);
      if (!world.isClientSide() && entity instanceof ServerPlayer) {
         refreshStack(entity, itemstack);
      }
   }

   public static ItemStack createBoundCard(ServerPlayer player) {
      ItemStack card = new ItemStack(SololevelingModItems.HUNTER_ID.get());
      refreshStack(player, card);
      return card;
   }

   public static void refreshAll(ServerPlayer player) {
      if (player != null) {
         for (ItemStack stack : player.getInventory().items) {
            refreshStack(player, stack);
         }

         for (ItemStack stack : player.getInventory().offhand) {
            refreshStack(player, stack);
         }

         for (ItemStack stack : player.getInventory().armor) {
            refreshStack(player, stack);
         }

         player.getInventory().setChanged();
      }
   }

   public static void refreshStack(Entity entity, ItemStack itemstack) {
      if (entity instanceof ServerPlayer player && itemstack != null && !itemstack.isEmpty() && itemstack.getItem() == SololevelingModItems.HUNTER_ID.get()) {
         CompoundTag tag = itemstack.getOrCreateTag();
         if (!tag.hasUUID("EvaluationOwner")) {
            tag.putUUID("EvaluationOwner", player.getUUID());
         }

         if (player.getUUID().equals(tag.getUUID("EvaluationOwner"))) {
            SololevelingModVariables.PlayerVariables variables = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables());
            int certifiedRank = bounded((int)Math.round(variables.prevRank), 0, 6);
            int classId = bounded((int)Math.round(variables.Classes), 0, 6);
            putOrRemove(tag, "Rank", rankDisplay(certifiedRank));
            putOrRemove(tag, "Class", classDisplay(classId));
            tag.putString("Person", "§c" + player.getDisplayName().getString());
            tag.putInt("EvaluationSchema", 2);
         }
      }
   }

   private static String rankDisplay(int rank) {
      return switch (rank) {
         case 1 -> "§7E";
         case 2 -> "§aD";
         case 3 -> "§bC";
         case 4 -> "§9B";
         case 5 -> "§dA";
         case 6 -> "§6S";
         default -> "";
      };
   }

   private static String classDisplay(int classId) {
      return switch (classId) {
         case 1 -> "§9Assassin";
         case 2 -> "§5Mage";
         case 3 -> "§fFighter";
         case 4 -> "§9Tanker";
         case 5 -> "§aHealer";
         case 6 -> "§6Ranger";
         default -> "";
      };
   }

   private static void putOrRemove(CompoundTag tag, String key, String value) {
      if (value != null && !value.isEmpty()) {
         tag.putString(key, value);
      } else {
         tag.remove(key);
      }
   }

   private static int bounded(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   @Override
   public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag compound) {
      return new HunterIDInventoryCapability();
   }

   @Override
   public CompoundTag getShareTag(ItemStack stack) {
      CompoundTag nbt = stack.getOrCreateTag();
      stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> nbt.put("Inventory", ((ItemStackHandler)capability).serializeNBT()));
      return nbt;
   }

   @Override
   public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
      super.readShareTag(stack, nbt);
      if (nbt != null) {
         stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
            .ifPresent(capability -> ((ItemStackHandler)capability).deserializeNBT((CompoundTag)nbt.get("Inventory")));
      }
   }
}
