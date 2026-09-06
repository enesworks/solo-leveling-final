package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;
import dev.eness.sololevelingfinal.core.world.inventory.PathMenu;

public class OpenPathGuiProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof ServerPlayer player && player.server != null) {
         final BlockPos menuPosition = BlockPos.containing(x, y, z);
         final PathMenu.TowerState towerState = snapshot(player);
         NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
               return Component.literal("Demon King's Castle");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player menuPlayer) {
               return new PathMenu(id, inventory, menuPosition, towerState);
            }
         }, buffer -> PathMenu.writeOpeningData(buffer, menuPosition, towerState));
      }
   }

   private static PathMenu.TowerState snapshot(ServerPlayer player) {
      DkcRunSavedData.RunState runState = DkcRunSavedData.get(player.server).getOrCreate(player);
      SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      boolean insideDkc = DkcFloorRegistry.isDkc(player.level());
      int currentFloor = insideDkc ? DkcSpatialLayout.floor(player) : 0;
      if (currentFloor <= 0 && insideDkc) {
         currentFloor = clampFloor((int)player.getPersistentData().getDouble("dkc_current_floor"));
      }

      if (currentFloor <= 0) {
         currentFloor = highestFloor(runState.unlockedFloors());
      }

      int clearedFloors = clampFloor((int)Math.floor(vars.dkc_cleared));
      return new PathMenu.TowerState(
         runState.unlockedFloors(),
         runState.generatedFloors(),
         runState.armedTransitions(),
         clearedFloors,
         currentFloor,
         insideDkc,
         clearedFloors >= 20,
         DkcQuestManager.hasRadiruCastleAccess(player),
         vars.radiru_pact,
         vars.radiru_slaughtered
      );
   }

   private static int highestFloor(long mask) {
      return mask == 0L ? 0 : Math.min(20, 64 - Long.numberOfLeadingZeros(mask));
   }

   private static int clampFloor(int floor) {
      return Math.max(0, Math.min(20, floor));
   }
}
