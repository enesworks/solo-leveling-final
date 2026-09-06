package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.party.PartyService;

public final class JoiningPartyProcedure {
   private JoiningPartyProcedure() {
   }

   public static void execute(LevelAccessor world, CommandContext<CommandSourceStack> arguments, Entity entity) {
      if (entity instanceof ServerPlayer player) {
         PartyService.legacyRequestJoin(player, StringArgumentType.getString(arguments, "name"));
      }
   }
}
