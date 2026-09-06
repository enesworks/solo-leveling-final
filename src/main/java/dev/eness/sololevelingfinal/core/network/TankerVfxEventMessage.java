package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.renderer.TankerVfxRenderer;
import dev.eness.sololevelingfinal.core.util.TankerSkillManager;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD)
public final class TankerVfxEventMessage {
   private static boolean registered;
   public static final byte LEAP_START = 0;
   public static final byte LEAP_LAND = 1;
   public static final byte TAUNT_RING = 2;
   public static final byte BASH_SWEEP = 3;
   public static final byte BASH_HIT = 4;
   public static final byte BASH_STRAIN_RELIEF = 5;
   public static final byte REINFORCEMENT_BRACE_START = 6;
   public static final byte REINFORCEMENT_BRACE_HIT = 7;
   public static final byte REINFORCEMENT_STANCE_START = 8;
   public static final byte REINFORCEMENT_STANCE_END = 9;
   public static final byte WILLPOWER_START = 10;
   public static final byte WILLPOWER_STRAIN_THRESHOLD = 11;
   public static final byte WILLPOWER_SETTLE = 12;
   public static final byte WILLPOWER_BREAK = 13;
   public static final byte MARK_DEPLOY = 14;
   public static final byte MARK_INTEGRITY_THRESHOLD = 15;
   public static final byte MARK_BREAK = 16;
   public static final byte MARK_CANCEL = 17;
   public static final int EVENT_TYPE_COUNT = 18;
   public static final int FLAG_ESSENTIAL = 1;
   public static final int FLAG_CONFIRMED_HIT = 2;
   public static final int FLAG_PVP = 4;
   public static final int FLAG_REPLAY = 8;
   public static final int FLAG_SILENT = 16;
   public static final int INTENSITY_25 = 64;
   public static final int INTENSITY_50 = 128;
   public static final int INTENSITY_75 = 192;
   public static final int INTENSITY_100 = 255;
   public static final int LEAP_START_TICKS = 12;
   public static final int LEAP_LAND_TICKS = 10;
   public static final int TAUNT_TICKS = 120;
   public static final int BASH_SWEEP_TICKS = 6;
   public static final int BASH_ACCENT_TICKS = 6;
   public static final int REINFORCEMENT_BRACE_TICKS = 12;
   public static final int REINFORCEMENT_STANCE_TICKS = 80;
   public static final int REINFORCEMENT_END_TICKS = 6;
   public static final int WILLPOWER_TICKS = 160;
   public static final int WILLPOWER_THRESHOLD_TICKS = 8;
   public static final int WILLPOWER_SETTLE_PULSE_TICKS = 10;
   public static final int WILLPOWER_BREAK_TICKS = 8;
   public static final int MARK_TICKS = 240;
   public static final int MARK_THRESHOLD_TICKS = 8;
   public static final int MARK_END_TICKS = 8;
   public static final double TAUNT_RADIUS = 12.0;
   public static final double BASH_REACH = 3.6;
   public static final double LEAP_LAND_RADIUS = 5.0;
   public static final double MARK_RADIUS = 6.0;
   public static final double DEFAULT_SEND_RANGE = 64.0;
   public static final int MAX_DURATION_TICKS = 1200;
   public final byte eventType;
   public final int ownerEntityId;
   public final int targetEntityId;
   public final double originX;
   public final double originY;
   public final double originZ;
   public final short yaw;
   public final short pitch;
   public final long serverStartTick;
   public final int duration;
   public final int seed;
   public final int intensity;
   public final int flags;

   public TankerVfxEventMessage(
      byte eventType,
      int ownerEntityId,
      int targetEntityId,
      double originX,
      double originY,
      double originZ,
      short yaw,
      short pitch,
      long serverStartTick,
      int duration,
      int seed,
      int intensity,
      int flags
   ) {
      this.eventType = eventType;
      this.ownerEntityId = Math.max(0, ownerEntityId);
      this.targetEntityId = targetEntityId < 0 ? -1 : Math.min(targetEntityId, 2147483646);
      this.originX = finite(originX);
      this.originY = finite(originY);
      this.originZ = finite(originZ);
      this.yaw = yaw;
      this.pitch = pitch;
      this.serverStartTick = serverStartTick;
      this.duration = Mth.clamp(duration, 1, 1200);
      this.seed = seed;
      this.intensity = Mth.clamp(intensity, 0, 255);
      this.flags = flags & 0xFF;
   }

   public static TankerVfxEventMessage create(
      byte eventType, Entity owner, Entity target, Vec3 origin, float yaw, float pitch, long serverStartTick, int duration, int seed, int intensity, int flags
   ) {
      return new TankerVfxEventMessage(
         eventType,
         owner.getId(),
         target == null ? -1 : target.getId(),
         origin.x,
         origin.y,
         origin.z,
         packRotation(yaw),
         packRotation(pitch),
         serverStartTick,
         duration,
         seed,
         intensity,
         flags
      );
   }

   public static TankerVfxEventMessage create(
      byte eventType, Entity owner, Entity target, Vec3 origin, float yaw, float pitch, long serverStartTick, int seed, int intensity, int flags
   ) {
      return create(eventType, owner, target, origin, yaw, pitch, serverStartTick, defaultDuration(eventType), seed, intensity, flags);
   }

   public Vec3 origin() {
      return new Vec3(this.originX, this.originY, this.originZ);
   }

   public float yawDegrees() {
      return unpackRotation(this.yaw);
   }

   public float pitchDegrees() {
      return unpackRotation(this.pitch);
   }

   public boolean hasFlag(int flag) {
      return (this.flags & flag) != 0;
   }

   public static short packRotation(float degrees) {
      return (short)Mth.floor(degrees * 65536.0F / 360.0F);
   }

   public static float unpackRotation(short packed) {
      return (packed & 65535) * 0.005493164F;
   }

   public static int defaultDuration(byte eventType) {
      return switch (eventType) {
         case 0 -> 12;
         case 1 -> 10;
         case 2 -> 120;
         case 3 -> 6;
         case 4, 5, 7 -> 6;
         case 6 -> 12;
         case 8 -> 80;
         case 9 -> 6;
         case 10 -> 160;
         case 11 -> 8;
         case 12 -> 10;
         case 13 -> 8;
         case 14 -> 240;
         case 15 -> 8;
         case 16, 17 -> 8;
         default -> 1;
      };
   }

   public static boolean isKnownEventType(byte eventType) {
      int unsigned = eventType & 255;
      return unsigned < 18;
   }

   public static void encode(TankerVfxEventMessage message, FriendlyByteBuf buffer) {
      buffer.writeByte(message.eventType);
      buffer.writeVarInt(message.ownerEntityId);
      buffer.writeVarInt(message.targetEntityId < 0 ? 0 : message.targetEntityId + 1);
      buffer.writeDouble(message.originX);
      buffer.writeDouble(message.originY);
      buffer.writeDouble(message.originZ);
      buffer.writeShort(message.yaw);
      buffer.writeShort(message.pitch);
      buffer.writeLong(message.serverStartTick);
      buffer.writeVarInt(message.duration);
      buffer.writeInt(message.seed);
      buffer.writeByte(message.intensity);
      buffer.writeByte(message.flags);
   }

   public static TankerVfxEventMessage decode(FriendlyByteBuf buffer) {
      byte eventType = buffer.readByte();
      int ownerEntityId = buffer.readVarInt();
      int encodedTarget = buffer.readVarInt();
      int targetEntityId = encodedTarget <= 0 ? -1 : encodedTarget - 1;
      return new TankerVfxEventMessage(
         eventType,
         ownerEntityId,
         targetEntityId,
         buffer.readDouble(),
         buffer.readDouble(),
         buffer.readDouble(),
         buffer.readShort(),
         buffer.readShort(),
         buffer.readLong(),
         buffer.readVarInt(),
         buffer.readInt(),
         buffer.readUnsignedByte(),
         buffer.readUnsignedByte()
      );
   }

   public static void handle(TankerVfxEventMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TankerVfxRenderer.enqueue(message)));
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static synchronized void register(FMLCommonSetupEvent event) {
      if (!registered) {
         registered = true;
         SololevelingMod.addNetworkMessage(
            TankerVfxEventMessage.class,
            TankerVfxEventMessage::encode,
            TankerVfxEventMessage::decode,
            TankerVfxEventMessage::handle,
            NetworkDirection.PLAY_TO_CLIENT
         );
         TankerSkillManager.installVfxSink(TankerVfxEventMessage::sendManagerEvent);
      }
   }

   private static void sendManagerEvent(ServerLevel level, TankerSkillManager.VfxEvent event) {
      byte packetEvent = switch (event.eventType()) {
         case 0 -> 0;
         case 1 -> 1;
         case 2 -> 2;
         case 3 -> 3;
         case 4 -> 4;
         case 5 -> 5;
         case 6 -> 6;
         case 7 -> 7;
         case 8 -> 8;
         case 9 -> 9;
         case 10 -> 10;
         case 11 -> 11;
         case 12 -> 12;
         case 13 -> 13;
         case 14 -> 14;
         case 15 -> 15;
         case 16 -> 16;
         case 17 -> 17;
         default -> -1;
      };
      if (packetEvent >= 0) {
         sendNear(
            level,
            new TankerVfxEventMessage(
               packetEvent,
               event.ownerEntityId(),
               event.targetEntityId(),
               event.x(),
               event.y(),
               event.z(),
               event.yaw(),
               event.pitch(),
               event.serverStartTick(),
               event.duration(),
               event.seed(),
               event.intensity(),
               event.flags()
            )
         );
      }
   }

   public static void sendNear(ServerLevel level, TankerVfxEventMessage message) {
      sendNear(level, 64.0, message);
   }

   public static void sendNear(ServerLevel level, double range, TankerVfxEventMessage message) {
      double boundedRange = Mth.clamp(range, 1.0, 128.0);
      SololevelingMod.PACKET_HANDLER
         .send(PacketDistributor.NEAR.with(TargetPoint.p(message.originX, message.originY, message.originZ, boundedRange, level.dimension())), message);
   }

   public static void sendTo(ServerPlayer player, TankerVfxEventMessage message) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);
   }

   public static void sendTrackingAndSelf(Entity owner, TankerVfxEventMessage message) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> owner), message);
   }

   private static double finite(double value) {
      return Double.isFinite(value) ? value : 0.0;
   }
}
