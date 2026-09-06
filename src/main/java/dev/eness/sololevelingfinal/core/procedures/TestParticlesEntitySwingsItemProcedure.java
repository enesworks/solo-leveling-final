package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TestParticlesEntitySwingsItemProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            System.out.println(entity.getStringUUID());
            _level.getServer()
               .getCommands()
               .performPrefixedCommand(
                  new CommandSourceStack(CommandSource.NULL, new Vec3(0.0, 0.0, 0.0), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null)
                     .withSuppressedOutput(),
                  "execute as " + entity.getStringUUID() + " run particlesetrel flame_beam 1"
               );
         }

         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Mod is made by \"Efkrdnz\""), false);
         }
      }
   }
}
