package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.procedures.DkcdebugoutputProcedure;

@EventBusSubscriber
public class DkcdebugCommand {
   @SubscribeEvent
   public static void registerCommand(RegisterCommandsEvent event) {
      event.getDispatcher().register((LiteralArgumentBuilder)Commands.literal("dkcdebug").executes(arguments -> {
         Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
         double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
         double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
         double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
         if (entity == null && world instanceof ServerLevel _servLevel) {
            entity = FakePlayerFactory.getMinecraft(_servLevel);
         }

         Direction direction = Direction.DOWN;
         if (entity != null) {
            direction = entity.getDirection();
         }

         DkcdebugoutputProcedure.execute(entity);
         return 0;
      }));
   }
}
