package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.world.dimension.rift.RiftGeometry;

@EventBusSubscriber(modid = "sololeveling")
public final class DimensionalRiftCommand {
   private DimensionalRiftCommand() {
   }

   @SubscribeEvent
   public static void register(RegisterCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> rift = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("rift")
         .then(Commands.literal("info").executes(context -> info((CommandSourceStack)context.getSource())));
      event.getDispatcher()
         .register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("slr").requires(source -> source.hasPermission(3))).then(rift));
   }

   private static int info(CommandSourceStack source) {
      double x = source.getPosition().x;
      double z = source.getPosition().z;
      RiftGeometry.Region region = RiftGeometry.resolveDefault(x, z);
      String regionName = region.type().name().toLowerCase(Locale.ROOT).replace('_', ' ');
      String territory = region.territory() == null ? "none" : region.territory().displayName();
      int expectedLevel = RiftGeometry.levelForDistance(region.distance());
      double remaining = Math.max(0.0, 3500.0 - region.distance());
      double starEdge = RiftGeometry.starRadius(RiftGeometry.angle(x, z), 600.0, 1200.0, 1.7);
      source.sendSuccess(
         () -> Component.literal(
               String.format(
                  Locale.ROOT,
                  "Rift: %s | territory: %s | radius: %.1f | expected mob level: %d | void in: %.1f | local star edge: %.1f",
                  regionName,
                  territory,
                  region.distance(),
                  expectedLevel,
                  remaining,
                  starEdge
               )
            )
            .withStyle(ChatFormatting.AQUA),
         false
      );
      return 1;
   }
}
