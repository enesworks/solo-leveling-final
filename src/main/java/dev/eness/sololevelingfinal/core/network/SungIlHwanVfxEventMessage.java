package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import javax.annotation.Nullable;
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
import dev.eness.sololevelingfinal.core.client.renderer.SungIlHwanVfxClientState;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD)
public final class SungIlHwanVfxEventMessage {
   private static final double WORLD_LIMIT = 3.0E7;
   private static boolean registered;
   public static final byte STAGE_ONE = 0;
   public static final byte STAGE_TWO = 1;
   public static final byte STAGE_END = 2;
   public static final byte FEAR_PULSE = 3;
   public static final byte FEAR_MARK = 4;
   public static final byte SPATIAL_SLASH = 5;
   public static final byte EXECUTION_PUBLIC_CHARGE = 6;
   public static final byte EXECUTION_PRIVATE_TARGET = 7;
   public static final byte EXECUTION_RELEASE = 8;
   public static final byte EXECUTION_FRACTURE = 9;
   public static final byte EXECUTION_CANCEL = 10;
   public static final byte EXHAUSTION = 11;
   public static final byte RISK_FEEDBACK = 12;
   public static final int EVENT_TYPE_COUNT = 13;
   public static final int STAGE_NONE_VALUE = 0;
   public static final int STAGE_ONE_VALUE = 1;
   public static final int STAGE_TWO_VALUE = 2;
   public static final int FLAG_ESSENTIAL = 1;
   public static final int FLAG_PRIVATE_CASTER = 2;
   public static final int FLAG_CONFIRMED_HIT = 4;
   public static final int FLAG_REPLAY = 8;
   public static final int FLAG_SILENT = 16;
   public static final int FLAG_HIGH_RISK = 32;
   public static final int DEFAULT_STAGE_TICKS = 1200;
   public static final int FEAR_PULSE_TICKS = 24;
   public static final int FEAR_MARK_TICKS = 80;
   public static final int SPATIAL_SLASH_TICKS = 13;
   public static final int EXECUTION_CHARGE_TICKS = 100;
   public static final int EXECUTION_RELEASE_TICKS = 42;
   public static final int EXECUTION_FRACTURE_TICKS = 18;
   public static final int EXECUTION_CANCEL_TICKS = 9;
   public static final int EXHAUSTION_TICKS = 50;
   public static final int RISK_TICKS = 36;
   public static final int MAX_DURATION_TICKS = 24000;
   public static final int MAX_FUTURE_START_TICKS = 100;
   public static final double DEFAULT_SEND_RANGE = 80.0;
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

   public SungIlHwanVfxEventMessage(
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
      this.targetEntityId = targetEntityId < 0 ? -1 : Math.min(targetEntityId, 2147483646);
      this.originX = coordinate(originX);
      this.originY = coordinate(originY);
      this.originZ = coordinate(originZ);
      this.focusX = coordinate(focusX);
      this.focusY = coordinate(focusY);
      this.focusZ = coordinate(focusZ);
      this.yaw = yaw;
      this.pitch = pitch;
      this.serverStartTick = Math.max(0L, serverStartTick);
      this.duration = Mth.clamp(duration, 1, 24000);
      this.seed = seed;
      this.intensity = Mth.clamp(intensity, 0, 255);
      this.variant = Mth.clamp(variant, 0, 15);
      this.flags = flags & 0xFF;
      this.radius = Mth.clamp(Float.isFinite(radius) ? radius : 1.0F, 0.25F, 32.0F);
   }

   public static SungIlHwanVfxEventMessage create(
      byte eventType,
      Entity caster,
      @Nullable Entity target,
      Vec3 origin,
      Vec3 focus,
      float yaw,
      float pitch,
      long serverStartTick,
      int duration,
      int seed,
      int intensity,
      int variant,
      int flags,
      float radius
   ) {
      Vec3 safeOrigin = origin == null ? caster.position() : origin;
      Vec3 safeFocus = focus == null ? safeOrigin : focus;
      return new SungIlHwanVfxEventMessage(
         eventType,
         caster.getId(),
         target == null ? -1 : target.getId(),
         safeOrigin.x,
         safeOrigin.y,
         safeOrigin.z,
         safeFocus.x,
         safeFocus.y,
         safeFocus.z,
         packRotation(yaw),
         packRotation(pitch),
         serverStartTick,
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

   public static boolean isKnownEventType(byte eventType) {
      return (eventType & 255) < 13;
   }

   public static short packRotation(float degrees) {
      float safe = Float.isFinite(degrees) ? degrees : 0.0F;
      return (short)Mth.floor(safe * 65536.0F / 360.0F);
   }

   public static float unpackRotation(short packed) {
      return (packed & 65535) * 0.005493164F;
   }

   public static void encode(SungIlHwanVfxEventMessage message, FriendlyByteBuf buffer) {
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

   public static SungIlHwanVfxEventMessage decode(FriendlyByteBuf buffer) {
      byte eventType = buffer.readByte();
      int casterEntityId = buffer.readVarInt();
      int encodedTarget = buffer.readVarInt();
      int targetEntityId = encodedTarget <= 0 ? -1 : encodedTarget - 1;
      return new SungIlHwanVfxEventMessage(
         eventType,
         casterEntityId,
         targetEntityId,
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

   public static void handle(SungIlHwanVfxEventMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SungIlHwanVfxClientState.enqueue(message)));
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static synchronized void register(FMLCommonSetupEvent event) {
      if (!registered) {
         registered = true;
         SololevelingMod.addNetworkMessage(
            SungIlHwanVfxEventMessage.class,
            SungIlHwanVfxEventMessage::encode,
            SungIlHwanVfxEventMessage::decode,
            SungIlHwanVfxEventMessage::handle,
            NetworkDirection.PLAY_TO_CLIENT
         );
      }
   }

   public static void sendStage(ServerPlayer caster, int stage, int durationTicks, int seed) {
      if (validCaster(caster)) {
         int boundedStage = Mth.clamp(stage, 1, 2);
         byte type = (byte)(boundedStage == 2 ? 1 : 0);
         int intensity = boundedStage == 2 ? 255 : 176;
         int flags = 1;
         SungIlHwanVfxEventMessage message = create(
            type,
            caster,
            null,
            caster.position(),
            caster.position(),
            caster.getYRot(),
            caster.getXRot(),
            caster.serverLevel().getGameTime(),
            durationTicks <= 0 ? 1200 : durationTicks,
            seed,
            intensity,
            boundedStage,
            flags,
            boundedStage == 2 ? 2.4F : 1.8F
         );
         sendTrackingAndSelf(caster, message);
      }
   }

   public static void sendStageEnd(ServerPlayer caster, int seed) {
      if (validCaster(caster)) {
         sendTrackingAndSelf(
            caster,
            create(
               (byte)2,
               caster,
               null,
               caster.position(),
               caster.position(),
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               12,
               seed,
               144,
               0,
               1,
               1.8F
            )
         );
      }
   }

   public static void sendFearPulse(ServerPlayer caster, Vec3 origin, double radius, int seed) {
      if (validCaster(caster)) {
         Vec3 safeOrigin = origin == null ? caster.position() : origin;
         sendNear(
            caster.serverLevel(),
            80.0,
            create(
               (byte)3, caster, null, safeOrigin, safeOrigin, caster.getYRot(), 0.0F, caster.serverLevel().getGameTime(), 24, seed, 220, 0, 1, (float)radius
            )
         );
      }
   }

   public static void sendFearMark(ServerPlayer caster, @Nullable Entity target, int durationTicks, int seed) {
      if (validCaster(caster) && target != null && target.level() == caster.level()) {
         Vec3 focus = target.position().add(0.0, Math.max(0.75, target.getBbHeight() * 0.72), 0.0);
         sendNear(
            caster.serverLevel(),
            80.0,
            create(
               (byte)4,
               caster,
               target,
               caster.position(),
               focus,
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               durationTicks <= 0 ? 80 : durationTicks,
               seed,
               205,
               0,
               1,
               Math.max(0.65F, target.getBbWidth() * 0.9F)
            )
         );
      }
   }

   public static void sendSpatialSlash(ServerPlayer caster, @Nullable Entity target, Vec3 from, Vec3 to, int comboIndex, boolean confirmedHit, int seed) {
      if (validCaster(caster)) {
         Vec3 origin = from == null ? caster.getEyePosition().add(caster.getLookAngle().scale(0.35)) : from;
         Vec3 focus = to == null ? origin.add(caster.getLookAngle().scale(9.0)) : to;
         int flags = confirmedHit ? 4 : 0;
         sendNear(
            caster.serverLevel(),
            80.0,
            create(
               (byte)5,
               caster,
               target,
               origin,
               focus,
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               13,
               seed,
               confirmedHit ? 255 : 205,
               Mth.clamp(comboIndex, 0, 7),
               flags,
               0.72F + Mth.clamp(comboIndex, 0, 7) * 0.035F
            )
         );
      }
   }

   public static void sendExecutionCharge(ServerPlayer caster, Vec3 focus, double radius, int durationTicks, int seed) {
      if (validCaster(caster)) {
         long now = caster.serverLevel().getGameTime();
         int boundedDuration = durationTicks <= 0 ? 60 : durationTicks;
         Vec3 origin = caster.position();
         sendNear(
            caster.serverLevel(),
            80.0,
            create((byte)6, caster, null, origin, origin, caster.getYRot(), caster.getXRot(), now, boundedDuration, seed, 230, 0, 1, 2.2F)
         );
         Vec3 privateFocus = origin;
         sendTo(
            caster,
            create((byte)7, caster, null, origin, privateFocus, caster.getYRot(), caster.getXRot(), now, boundedDuration, seed, 32, 0, 3, (float)radius)
         );
      }
   }

   public static void sendExecutionTarget(ServerPlayer caster, @Nullable Entity target, Vec3 focus, double radius, int remainingTicks, int seed) {
      if (validCaster(caster)) {
         Entity scopedTarget = target != null && target.level() == caster.level() ? target : null;
         Vec3 privateFocus = scopedTarget != null ? scopedTarget.position().add(0.0, scopedTarget.getBbHeight() * 0.5, 0.0) : caster.position();
         int chargeProgress = Mth.clamp(255 - Mth.floor(Math.max(0, remainingTicks) * 255.0F / 100.0F), 32, 255);
         sendTo(
            caster,
            create(
               (byte)7,
               caster,
               scopedTarget,
               caster.position(),
               privateFocus,
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               Math.max(2, remainingTicks),
               seed,
               chargeProgress,
               scopedTarget == null ? 1 : 2,
               3,
               (float)radius
            )
         );
      }
   }

   public static void sendExecutionRelease(ServerPlayer caster, @Nullable Entity target, Vec3 focus, double radius, int chargeTier, int seed) {
      if (validCaster(caster)) {
         Vec3 impact = focus == null ? caster.position() : focus;
         sendNear(
            caster.serverLevel(),
            112.0,
            create(
               (byte)8,
               caster,
               null,
               caster.position(),
               impact,
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               42,
               seed,
               190 + Mth.clamp(chargeTier, 0, 3) * 21,
               Mth.clamp(chargeTier, 0, 3),
               1,
               (float)radius
            )
         );
      }
   }

   public static void sendExecutionFracture(ServerPlayer caster, Vec3 focus, double radius, int delayTicks, int seed) {
      if (validCaster(caster)) {
         Vec3 impact = focus == null ? caster.position() : focus;
         int delay = Mth.clamp(delayTicks, 0, 100);
         sendNear(
            caster.serverLevel(),
            112.0,
            create(
               (byte)9, caster, null, impact, impact, caster.getYRot(), 0.0F, caster.serverLevel().getGameTime() + delay, 18, seed, 255, 0, 1, (float)radius
            )
         );
      }
   }

   public static void sendExecutionCancel(ServerPlayer caster, int seed) {
      if (validCaster(caster)) {
         sendNear(
            caster.serverLevel(),
            80.0,
            create(
               (byte)10,
               caster,
               null,
               caster.position(),
               caster.position(),
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               9,
               seed,
               128,
               0,
               1,
               1.6F
            )
         );
      }
   }

   public static void sendExhaustion(ServerPlayer caster, int severity, int durationTicks, int seed) {
      if (validCaster(caster)) {
         int boundedSeverity = Mth.clamp(severity, 0, 255);
         sendTrackingAndSelf(
            caster,
            create(
               (byte)11,
               caster,
               null,
               caster.position(),
               caster.position(),
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               durationTicks <= 0 ? 50 : durationTicks,
               seed,
               boundedSeverity,
               0,
               1,
               1.4F + boundedSeverity / 255.0F
            )
         );
      }
   }

   public static void sendRiskFeedback(ServerPlayer caster, int severity, int durationTicks, int seed) {
      if (validCaster(caster)) {
         int boundedSeverity = Mth.clamp(severity, 0, 255);
         int flags = 2 | (boundedSeverity >= 192 ? 32 : 0);
         sendTo(
            caster,
            create(
               (byte)12,
               caster,
               null,
               caster.position(),
               caster.position(),
               caster.getYRot(),
               caster.getXRot(),
               caster.serverLevel().getGameTime(),
               durationTicks <= 0 ? 36 : durationTicks,
               seed,
               boundedSeverity,
               0,
               flags,
               1.0F
            )
         );
      }
   }

   public static void sendNear(ServerLevel level, SungIlHwanVfxEventMessage message) {
      sendNear(level, 80.0, message);
   }

   public static void sendNear(ServerLevel level, double range, SungIlHwanVfxEventMessage message) {
      if (level != null && message != null && !message.privateToCaster()) {
         double boundedRange = Mth.clamp(range, 1.0, 128.0);
         SololevelingMod.PACKET_HANDLER
            .send(PacketDistributor.NEAR.with(TargetPoint.p(message.originX, message.originY, message.originZ, boundedRange, level.dimension())), message);
      }
   }

   public static void sendTo(ServerPlayer player, SungIlHwanVfxEventMessage message) {
      if (player != null && message != null) {
         if (!message.privateToCaster() || player.getId() == message.casterEntityId) {
            SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);
         }
      }
   }

   public static void sendTrackingAndSelf(Entity caster, SungIlHwanVfxEventMessage message) {
      if (caster != null && message != null && !message.privateToCaster()) {
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster), message);
      }
   }

   private static boolean validCaster(ServerPlayer caster) {
      return caster != null && caster.server != null && !caster.isRemoved();
   }

   private static double coordinate(double value) {
      return !Double.isFinite(value) ? 0.0 : Mth.clamp(value, -3.0E7, 3.0E7);
   }
}
