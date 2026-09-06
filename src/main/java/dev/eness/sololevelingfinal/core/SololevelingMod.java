package dev.eness.sololevelingfinal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.simple.SimpleChannel;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlockEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.init.SololevelingModMenus;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModPaintings;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.init.SololevelingModPotions;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;
import dev.eness.sololevelingfinal.core.init.SololevelingModTabs;
import dev.eness.sololevelingfinal.core.world.dimension.rift.RiftWorldgenRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("sololeveling")
public class SololevelingMod {
   public static final Logger LOGGER = LogManager.getLogger(SololevelingMod.class);
   public static final String MODID = "sololeveling";
   private static final String PROTOCOL_VERSION = "3";
   public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
      new ResourceLocation("sololeveling", "sololeveling"), () -> "3", "3"::equals, "3"::equals
   );
   private static int messageID = 0;
   private static final Collection<SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();
   private static final Collection<SololevelingMod.ServerBoundWork> serverBoundWorkQueue = new ConcurrentLinkedQueue<>();

   public SololevelingMod() {
      MinecraftForge.EVENT_BUS.register(this);
      IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
      RiftWorldgenRegistries.register(bus);
      SololevelingModSounds.REGISTRY.register(bus);
      SololevelingModBlocks.REGISTRY.register(bus);
      SololevelingModBlockEntities.REGISTRY.register(bus);
      SololevelingModItems.REGISTRY.register(bus);
      SololevelingModEntities.REGISTRY.register(bus);
      SololevelingModTabs.REGISTRY.register(bus);
      SololevelingModMobEffects.REGISTRY.register(bus);
      SololevelingModPotions.REGISTRY.register(bus);
      SololevelingModPaintings.REGISTRY.register(bus);
      SololevelingModParticleTypes.REGISTRY.register(bus);
      SololevelingModMenus.REGISTRY.register(bus);
   }

   public static <T> void addNetworkMessage(
      Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<Context>> messageConsumer
   ) {
      PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
      messageID++;
   }

   public static <T> void addNetworkMessage(
      Class<T> messageType,
      BiConsumer<T, FriendlyByteBuf> encoder,
      Function<FriendlyByteBuf, T> decoder,
      BiConsumer<T, Supplier<Context>> messageConsumer,
      NetworkDirection direction
   ) {
      PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer, Optional.of(direction));
      messageID++;
   }

   public static void queueServerWork(int tick, Runnable action) {
      if (action != null) {
         workQueue.add(new SimpleEntry<>(action, Math.max(1, tick)));
      }
   }

   public static void queueServerWork(MinecraftServer server, int tick, Runnable action) {
      if (server != null && action != null) {
         serverBoundWorkQueue.add(new SololevelingMod.ServerBoundWork(server, Math.max(1, tick), action));
      }
   }

   @SubscribeEvent
   public void tick(ServerTickEvent event) {
      if (event.phase == Phase.END && (!workQueue.isEmpty() || !serverBoundWorkQueue.isEmpty())) {
         List<SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
         workQueue.forEach(work -> {
            work.setValue(work.getValue() - 1);
            if (work.getValue() == 0) {
               actions.add((SimpleEntry<Runnable, Integer>)work);
            }
         });
         actions.forEach(e -> e.getKey().run());
         workQueue.removeAll(actions);
         List<SololevelingMod.ServerBoundWork> finished = new ArrayList<>();
         List<Runnable> boundActions = new ArrayList<>();
         serverBoundWorkQueue.forEach(work -> {
            if (work.server != event.getServer()) {
               finished.add(work);
            } else {
               work.remainingTicks--;
               if (work.remainingTicks == 0) {
                  finished.add(work);
                  boundActions.add(work.action);
               }
            }
         });
         serverBoundWorkQueue.removeAll(finished);
         boundActions.forEach(Runnable::run);
      }
   }

   @SubscribeEvent
   public void onServerStopping(ServerStoppingEvent event) {
      workQueue.clear();
      serverBoundWorkQueue.removeIf(work -> work.server == event.getServer());
   }

   private static final class ServerBoundWork {
      private final MinecraftServer server;
      private final Runnable action;
      private int remainingTicks;

      private ServerBoundWork(MinecraftServer server, int remainingTicks, Runnable action) {
         this.server = server;
         this.remainingTicks = remainingTicks;
         this.action = action;
      }
   }
}
