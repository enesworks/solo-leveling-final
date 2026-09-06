package dev.eness.sololevelingfinal.core.procedures;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ReturnShopSwords2Procedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         double slot = 0.0;
         slot = 1.0;
         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.EMERALD_DAGGER.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 3400.0
               ? "§a3400"
               : "§c3400";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.MYTHIC_DAGGER.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 3675.0
               ? "§a3675"
               : "§c3675";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.E_TIER_SWORD.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 200.0
               ? "§a200"
               : "§c200";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.D_TIER_SWORD.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 250.0
               ? "§a250"
               : "§c250";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.C_TIER_SWORD.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 300.0
               ? "§a300"
               : "§c300";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.B_TIER_SWORD.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 400.0
               ? "§a400"
               : "§c400";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.A_TIER_SWORD.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 600.0
               ? "§a600"
               : "§c600";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.S_TIER_SWORD.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 800.0
               ? "§a800"
               : "§c800";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.KATANA_STIER.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 2000.0
               ? "§a2000"
               : "§c2000";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.HAMMER.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 2140.0
               ? "§a2140"
               : "§c2140";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.WAR_AXE.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 2140.0
               ? "§a2140"
               : "§c2140";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.SWORD_CURVED_D.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 540.0
               ? "§a540"
               : "§c540";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.SWORD_WARRIOR_D.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 480.0
               ? "§a480"
               : "§c480";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.DAGGER_KNIGHT_D.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 505.0
               ? "§a505"
               : "§c505";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.DAGGER_KARAMBIT_E.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 380.0
               ? "§a380"
               : "§c380";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.SWORD_CURVED_D.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 460.0
               ? "§a460"
               : "§c460";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.SWORD_TWINWING_C.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 590.0
               ? "§a590"
               : "§c590";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.DAGGER_CHAIN_C.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 600.0
               ? "§a600"
               : "§c600";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.SWORD_ENRICHED_B.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 720.0
               ? "§a720"
               : "§c720";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.SWORD_NATURE_B.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 700.0
               ? "§a700"
               : "§c700";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.DAGGER_GOLDEN_B.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 735.0
               ? "§a735"
               : "§c735";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.DAGGER_HEAT_A.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 965.0
               ? "§a965"
               : "§c965";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.DAGGER_DUOLITY_A.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 940.0
               ? "§a940"
               : "§c940";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.KNIGHT_KILLER.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 3000.0
               ? "§a3000"
               : "§c3000";
         } else if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                  ? ((Slot)_slt.get((int)slot)).getItem()
                  : ItemStack.EMPTY)
               .getItem()
            == SololevelingModItems.GRAVITY_DAGGER.get()) {
            return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
                  >= 2840.0
               ? "§a2840"
               : "§c2840";
         } else {
            return "";
         }
      }
   }
}
