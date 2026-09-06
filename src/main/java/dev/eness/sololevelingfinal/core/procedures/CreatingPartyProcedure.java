package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.party.PartyService;

public final class CreatingPartyProcedure {
   private CreatingPartyProcedure() {
   }

   public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
      if (entity instanceof ServerPlayer player) {
         PartyService.legacyCreate(player, StringArgumentType.getString(arguments, "name"));
      }
   }
}
