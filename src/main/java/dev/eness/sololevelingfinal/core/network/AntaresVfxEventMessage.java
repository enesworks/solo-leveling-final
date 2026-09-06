package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import dev.eness.sololevelingfinal.core.client.renderer.AntaresVfxClientState;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD)
public final class AntaresVfxEventMessage {
   private static final double WORLD_LIMIT = 3.0E7;
   private static final double SEND_RANGE = 112.0;
   private static boolean registered;
   public static final byte RUIN_SYNC = 0;
   public static final byte CLAW = 1;
   public static final byte BREATH_CHARGE = 2;
   public static final byte BREATH_STREAM = 3;
   public static final byte BREATH_END = 4;
   public static final byte DESCENT_LAUNCH = 5;
   public static final byte DESCENT_IMPACT = 6;
   public static final byte ROAR_CHARGE = 7;
   public static final byte ROAR_RELEASE = 8;
   public static final byte OVERAWED_MARK = 9;
   public static final byte EXTINCTION_CHARGE = 10;
   public static final byte EXTINCTION_PULSE = 11;
   public static final byte EXTINCTION_AFTERMATH = 12;
   public static final byte MANIFESTATION_START = 13;
   public static final byte MANIFESTATION_END = 14;
   public static final int EVENT_TYPE_COUNT = 15;
   public static final int FLAG_ESSENTIAL = 1;
   public static final int FLAG_PRIVATE_CASTER = 2;
   public static final int FLAG_CONFIRMED_HIT = 4;
   public static final int FLAG_MANIFESTED = 8;
   public static final int FLAG_FINISHER = 16;
   public static final int MAX_DURATION_TICKS = 6000;
   public static final int MAX_FUTURE_START_TICKS = 100;
   public final byte eventType;
   public final int casterEntityId;
   public final int targetEntityId;
   public final double originX;
   public final double originY;
   public final double originZ;
   public final double focusX;
   public final double focusY;
   public final double focusZ;
   public final short yaw;
   public final short pitch;
   public final long serverStartTick;
   public final int duration;
   public final int seed;
   public final int intensity;
   public final int variant;
   public final int flags;
   public final float radius;

   public AntaresVfxEventMessage(
      byte eventType,
      int casterEntityId,
      int targetEntityId,
      double originX,
      double originY,
      double originZ,
      double focusX,
      double focusY,
      double focusZ,
      short yaw,
      short pitch,
      long serverStartTick,
      int duration,
      int seed,
      int intensity,
      int variant,
      int flags,
      float radius
   ) {
      this.eventType = eventType;
      this.casterEntityId = Math.max(0, casterEntityId);
      this.targetEntityId = targetEntityId < 0 ? -1 : targetEntityId;
      this.originX = coordinate(originX);
      this.originY = coordinate(originY);
      this.originZ = coordinate(originZ);
      this.focusX = coordinate(focusX);
      this.focusY = coordinate(focusY);
      this.focusZ = coordinate(focusZ);
      this.yaw = yaw;
      this.pitch = pitch;
      this.serverStartTick = Math.max(0L, serverStartTick);
      this.duration = Mth.clamp(duration, 1, 6000);
      this.seed = seed;
      this.intensity = Mth.clamp(intensity, 0, 255);
      this.variant = Mth.clamp(variant, 0, 31);
      this.flags = flags & 0xFF;
      this.radius = Mth.clamp(Float.isFinite(radius) ? radius : 1.0F, 0.1F, 48.0F);
   }

   public static AntaresVfxEventMessage create(
      byte type, Entity caster, @Nullable Entity target, Vec3 origin, Vec3 focus, int duration, int seed, int intensity, int variant, int flags, float radius
   ) {
      Vec3 safeOrigin = origin == null ? caster.position() : origin;
      Vec3 safeFocus = focus == null ? safeOrigin : focus;
      return new AntaresVfxEventMessage(
         type,
         caster.getId(),
         target == null ? -1 : target.getId(),
         safeOrigin.x,
         safeOrigin.y,
         safeOrigin.z,
         safeFocus.x,
         safeFocus.y,
         safeFocus.z,
         packRotation(caster.getYRot()),
         packRotation(caster.getXRot()),
         caster.level().getGameTime(),
         duration,
         seed,
         intensity,
         variant,
         flags,
         radius
      );
   }

   public Vec3 origin() {
      return new Vec3(this.originX, this.originY, this.originZ);
   }

   public Vec3 focus() {
      return new Vec3(this.focusX, this.focusY, this.focusZ);
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

   public boolean privateToCaster() {
      return this.hasFlag(2);
   }

   public static boolean isKnownEventType(byte type) {
      return (type & 255) < 15;
   }

   public static void encode(AntaresVfxEventMessage message, FriendlyByteBuf buffer) {
      buffer.writeByte(message.eventType);
      buffer.writeVarInt(message.casterEntityId);
      buffer.writeVarInt(message.targetEntityId < 0 ? 0 : message.targetEntityId + 1);
      buffer.writeDouble(message.originX);
      buffer.writeDouble(message.originY);
      buffer.writeDouble(message.originZ);
      buffer.writeDouble(message.focusX);
      buffer.writeDouble(message.focusY);
      buffer.writeDouble(message.focusZ);
      buffer.writeShort(message.yaw);
      buffer.writeShort(message.pitch);
      buffer.writeLong(message.serverStartTick);
      buffer.writeVarInt(message.duration);
      buffer.writeInt(message.seed);
      buffer.writeByte(message.intensity);
      buffer.writeByte(message.variant);
      buffer.writeByte(message.flags);
      buffer.writeFloat(message.radius);
   }

   public static AntaresVfxEventMessage decode(FriendlyByteBuf buffer) {
      byte type = buffer.readByte();
      int caster = buffer.readVarInt();
      int encodedTarget = buffer.readVarInt();
      return new AntaresVfxEventMessage(
         type,
         caster,
         encodedTarget == 0 ? -1 : encodedTarget - 1,
         buffer.readDouble(),
         buffer.readDouble(),
         buffer.readDouble(),
         buffer.readDouble(),
         buffer.readDouble(),
         buffer.readDouble(),
         buffer.readShort(),
         buffer.readShort(),
         buffer.readLong(),
         buffer.readVarInt(),
         buffer.readInt(),
         buffer.readUnsignedByte(),
         buffer.readUnsignedByte(),
         buffer.readUnsignedByte(),
         buffer.readFloat()
      );
   }

   public static void handle(AntaresVfxEventMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AntaresVfxClientState.enqueue(message)));
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static synchronized void register(FMLCommonSetupEvent event) {
      if (!registered) {
         registered = true;
         SololevelingMod.addNetworkMessage(
            AntaresVfxEventMessage.class,
            AntaresVfxEventMessage::encode,
            AntaresVfxEventMessage::decode,
            AntaresVfxEventMessage::handle,
            NetworkDirection.PLAY_TO_CLIENT
         );
      }
   }

   public static void sendRuin(ServerPlayer caster, int charges, int maximum, boolean manifested) {
      if (valid(caster)) {
         int flags = 3 | (manifested ? 8 : 0);
         sendTo(
            caster,
            create(
               (byte)0,
               caster,
               null,
               caster.position(),
               caster.position(),
               40,
               caster.tickCount * 31,
               255,
               Mth.clamp(charges, 0, 31),
               flags,
               Math.max(1, maximum)
            )
         );
      }
   }

   public static void sendClaw(ServerPlayer caster, Vec3 origin, Vec3 focus, boolean finisher, boolean hit, int seed) {
      int flags = 1 | (finisher ? 16 : 0) | (hit ? 4 : 0);
      sendNear(caster, create((byte)1, caster, null, origin, focus, 13, seed, finisher ? 255 : 210, finisher ? 1 : 0, flags, finisher ? 2.6F : 1.65F));
   }

   public static void sendBreathCharge(ServerPlayer caster, Vec3 origin, Vec3 focus, int duration, boolean manifested, int seed) {
      sendNear(
         caster,
         create((byte)2, caster, null, origin, focus, duration, seed, manifested ? 235 : 205, 0, 1 | manifestedFlag(manifested), manifested ? 1.8F : 1.4F)
      );
   }

   public static void sendBreathStream(ServerPlayer caster, Vec3 origin, Vec3 focus, boolean manifested, boolean hit, int seed) {
      int flags = 1 | manifestedFlag(manifested) | (hit ? 4 : 0);
      sendNear(caster, create((byte)3, caster, null, origin, focus, 7, seed, manifested ? 245 : 220, 0, flags, manifested ? 1.8F : 1.4F));
   }

   public static void sendBreathEnd(ServerPlayer caster, Vec3 origin, Vec3 focus, int seed) {
      sendNear(caster, create((byte)4, caster, null, origin, focus, 10, seed, 150, 0, 1, 1.4F));
   }

   public static void sendDescentLaunch(ServerPlayer caster, boolean manifested, int seed) {
      sendNear(
         caster, create((byte)5, caster, null, caster.position(), caster.position(), 45, seed, manifested ? 240 : 210, 0, 1 | manifestedFlag(manifested), 2.5F)
      );
   }

   public static void sendDescentImpact(ServerPlayer caster, Vec3 center, float radius, boolean manifested, boolean hit, int seed) {
      int flags = 1 | manifestedFlag(manifested) | (hit ? 4 : 0);
      sendNear(caster, create((byte)6, caster, null, center, center, 34, seed, manifested ? 255 : 225, 0, flags, radius));
   }

   public static void sendRoarCharge(ServerPlayer caster, boolean manifested, int seed) {
      sendNear(
         caster, create((byte)7, caster, null, caster.position(), caster.position(), 7, seed, manifested ? 240 : 210, 0, 1 | manifestedFlag(manifested), 3.4F)
      );
   }

   public static void sendRoarRelease(ServerPlayer caster, Vec3 center, float radius, boolean manifested, boolean hit, int seed) {
      int flags = 1 | manifestedFlag(manifested) | (hit ? 4 : 0);
      sendNear(caster, create((byte)8, caster, null, center, center, 24, seed, manifested ? 255 : 225, 0, flags, radius));
   }

   public static void sendOverawedMark(ServerPlayer caster, LivingEntity target, int duration, int seed) {
      sendNear(
         caster, create((byte)9, caster, target, target.position(), target.position(), duration, seed, 205, 0, 0, Mth.clamp(target.getBbHeight(), 0.8F, 4.0F))
      );
   }

   public static void sendExtinctionCharge(ServerPlayer caster, Vec3 origin, Vec3 focus, boolean manifested, int seed) {
      sendNear(
         caster, create((byte)10, caster, null, origin, focus, 20, seed, manifested ? 255 : 235, 0, 1 | manifestedFlag(manifested), manifested ? 2.9F : 2.35F)
      );
   }

   public static void sendExtinctionPulse(ServerPlayer caster, Vec3 origin, Vec3 focus, int pulse, boolean manifested, boolean hit, int seed) {
      int flags = 1 | manifestedFlag(manifested) | (hit ? 4 : 0);
      sendNear(caster, create((byte)11, caster, null, origin, focus, 14, seed, 255, Mth.clamp(pulse, 0, 2), flags, manifested ? 2.9F : 2.35F));
   }

   public static void sendExtinctionAftermath(ServerPlayer caster, Vec3 origin, Vec3 focus, boolean manifested, int seed) {
      sendNear(
         caster, create((byte)12, caster, null, origin, focus, 70, seed, manifested ? 235 : 205, 0, 1 | manifestedFlag(manifested), manifested ? 4.8F : 4.0F)
      );
   }

   public static void sendManifestation(ServerPlayer caster, boolean active, int seed) {
      sendNear(
         caster,
         create(
            (byte)(active ? 13 : 14),
            caster,
            null,
            caster.position(),
            caster.position(),
            active ? 28 : 14,
            seed,
            active ? 255 : 175,
            0,
            1,
            active ? 3.2F : 2.4F
         )
      );
   }

   private static int manifestedFlag(boolean manifested) {
      return manifested ? 8 : 0;
   }

   private static void sendNear(ServerPlayer caster, AntaresVfxEventMessage message) {
      if (valid(caster)) {
         ServerLevel level = caster.serverLevel();
         SololevelingMod.PACKET_HANDLER
            .send(PacketDistributor.NEAR.with(() -> new TargetPoint(message.originX, message.originY, message.originZ, 112.0, level.dimension())), message);
      }
   }

   private static void sendTo(ServerPlayer player, AntaresVfxEventMessage message) {
      if (valid(player)) {
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);
      }
   }

   private static boolean valid(ServerPlayer player) {
      return player != null && player.connection != null && player.level() != null;
   }

   private static short packRotation(float degrees) {
      float safe = Float.isFinite(degrees) ? degrees : 0.0F;
      return (short)Mth.floor(safe * 65536.0F / 360.0F);
   }

   private static float unpackRotation(short packed) {
      return (packed & 65535) * 0.005493164F;
   }

   private static double coordinate(double value) {
      return !Double.isFinite(value) ? 0.0 : Mth.clamp(value, -3.0E7, 3.0E7);
   }
}
