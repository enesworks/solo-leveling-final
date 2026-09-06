package dev.eness.sololevelingfinal.core.world.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import dev.eness.sololevelingfinal.core.init.SololevelingModMenus;
import dev.eness.sololevelingfinal.core.procedures.StorePotionNewFillProcedure;

@EventBusSubscriber
public class StorePotionNewMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
   public static final HashMap<String, Object> guistate = new HashMap<>();
   public static final int[] COLS = new int[]{-26, 10, 46};
   public static final int[] ROWS = new int[]{-56, -20, 16};
   public final Level world;
   public final Player entity;
   public int x;
   public int y;
   public int z;
   private ContainerLevelAccess access = ContainerLevelAccess.NULL;
   private IItemHandler internal;
   private final Map<Integer, Slot> customSlots = new HashMap<>();
   private boolean bound = false;

   public StorePotionNewMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
      super(SololevelingModMenus.STORE_POTION_NEW.get(), id);
      this.entity = inv.player;
      this.world = inv.player.level();
      this.internal = new ItemStackHandler(9);
      BlockPos pos = null;
      if (extraData != null) {
         pos = extraData.readBlockPos();
         this.x = pos.getX();
         this.y = pos.getY();
         this.z = pos.getZ();
         this.access = ContainerLevelAccess.create(this.world, pos);
      }

      for (int r = 0; r < 3; r++) {
         for (int c = 0; c < 3; c++) {
            int idx = r * 3 + c;
            this.customSlots.put(idx, this.addSlot(new SlotItemHandler(this.internal, idx, COLS[c], ROWS[r]) {
               @Override
               public boolean mayPickup(Player entity) {
                  return false;
               }

               @Override
               public boolean mayPlace(ItemStack itemstack) {
                  return false;
               }
            }));
         }
      }

      for (int si = 0; si < 3; si++) {
         for (int sj = 0; sj < 9; sj++) {
            this.addSlot(new Slot(inv, sj + (si + 1) * 9, -68 + sj * 18, 52 + si * 18));
         }
      }

      for (int si = 0; si < 9; si++) {
         this.addSlot(new Slot(inv, si, -68 + si * 18, 112));
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      return ItemStack.EMPTY;
   }

   public Map<Integer, Slot> get() {
      return this.customSlots;
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      Player entity = event.player;
      if (event.phase == Phase.END && entity.containerMenu instanceof StorePotionNewMenu) {
         StorePotionNewFillProcedure.execute(entity);
      }
   }
}
