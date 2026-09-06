package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import dev.eness.sololevelingfinal.core.client.renderer.shader.IrisCompat;
import dev.eness.sololevelingfinal.core.client.renderer.shader.SungIlHwanVfxRenderTypes;
import dev.eness.sololevelingfinal.core.network.SungIlHwanVfxEventMessage;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class SungIlHwanVfxRenderer {
   private static final int MATERIAL_VOID = 0;
   private static final int MATERIAL_SILVER = 1;
   private static final int MATERIAL_GOLD = 2;
   private static final int MATERIAL_FRACTURE = 3;
   private static final int MATERIAL_SOFT = 4;
   private static final int MATERIAL_SLASH = 5;
   private static final int VOID_BLACK = 461069;
   private static final int VOID_EDGE = 1382946;
   private static final int SILVER_DARK = 7568518;
   private static final int GOLD_DARK = 9392645;
   private static final int GOLD_AMBER = 15048987;
   private static final int GOLD_PALE = 15978086;
   private static final int GOLD_HOT = 16773548;
   private static final int GOLD_WHITE = 16775640;

   private SungIlHwanVfxRenderer() {
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      SungIlHwanVfxClientState.clear();
   }

   @SubscribeEvent
   public static void onLevelUnload(Unload event) {
      if (event.getLevel() instanceof ClientLevel) {
         SungIlHwanVfxClientState.clear();
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES || event.getStage() == Stage.AFTER_LEVEL) {
         if (!IrisCompat.isRenderingShadowPass()) {
            boolean renderStage = DeferredWorldShaderRenderer.isRenderStage(event, Stage.AFTER_PARTICLES);
            if (renderStage || event.getStage() == Stage.AFTER_PARTICLES) {
               Minecraft minecraft = Minecraft.getInstance();
               if (minecraft.level != null && minecraft.player != null) {
                  long now = minecraft.level.getGameTime();
                  List<SungIlHwanVfxClientState.ActiveEvent> active = SungIlHwanVfxClientState.snapshot(now);
                  if (!active.isEmpty()) {
                     DeferredWorldShaderRenderer.requestDepthAtStage(event, Stage.AFTER_PARTICLES);
                     if (renderStage) {
                        SungIlHwanVfxRenderer.Quality quality = SungIlHwanVfxRenderer.Quality.current(minecraft);
                        List<SungIlHwanVfxRenderer.VisibleEvent> visible = collectVisible(event, minecraft, active, quality, now);
                        if (!visible.isEmpty()) {
                           PoseStack stack = DeferredWorldShaderRenderer.worldPoseStack(event);
                           BufferSource buffers = minecraft.renderBuffers().bufferSource();
                           SungIlHwanVfxRenderer.RenderFrame frame = new SungIlHwanVfxRenderer.RenderFrame(
                              stack,
                              event.getCamera().getPosition(),
                              minecraft.getEntityRenderDispatcher().cameraOrientation(),
                              event.getPartialTick(),
                              quality
                           );
                           SungIlHwanVfxRenderer.FrameBudget budget = new SungIlHwanVfxRenderer.FrameBudget(quality.maxVertices);
                           RenderType surfaceType = SungIlHwanVfxRenderTypes.surface();
                           VertexConsumer surface = DeferredWorldShaderRenderer.buffer(buffers, surfaceType, true);

                           for (SungIlHwanVfxRenderer.VisibleEvent visual : visible) {
                              render(frame, visual, surface, budget, SungIlHwanVfxRenderer.Pass.SURFACE);
                           }

                           buffers.endBatch(surfaceType);
                           if (quality != SungIlHwanVfxRenderer.Quality.MINIMAL && budget.remaining >= 4) {
                              RenderType emissiveType = SungIlHwanVfxRenderTypes.emissive();
                              VertexConsumer emissive = DeferredWorldShaderRenderer.buffer(buffers, emissiveType, true);

                              for (SungIlHwanVfxRenderer.VisibleEvent visual : visible) {
                                 render(frame, visual, emissive, budget, SungIlHwanVfxRenderer.Pass.EMISSIVE);
                              }

                              buffers.endBatch(emissiveType);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static List<SungIlHwanVfxRenderer.VisibleEvent> collectVisible(
      RenderLevelStageEvent event, Minecraft minecraft, List<SungIlHwanVfxClientState.ActiveEvent> active, SungIlHwanVfxRenderer.Quality quality, long now
   ) {
      Vec3 camera = event.getCamera().getPosition();
      float partialTick = event.getPartialTick();
      List<SungIlHwanVfxRenderer.VisibleEvent> visible = new ArrayList<>(active.size());

      for (SungIlHwanVfxClientState.ActiveEvent timeline : active) {
         SungIlHwanVfxEventMessage message = timeline.message();
         float elapsed = timeline.elapsed(now, partialTick);
         if (!(elapsed < 0.0F) && !(elapsed >= message.duration) && (quality != SungIlHwanVfxRenderer.Quality.MINIMAL || timeline.essential())) {
            SungIlHwanVfxRenderer.Anchors anchors = resolveAnchors(minecraft, message, partialTick);
            AABB bounds = bounds(message, anchors);
            double distanceSqr = distanceToBoundsSqr(camera, bounds);
            if (!(distanceSqr > quality.renderDistance * quality.renderDistance) && event.getFrustum().isVisible(bounds)) {
               int light = LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(anchors.anchor));
               visible.add(
                  new SungIlHwanVfxRenderer.VisibleEvent(
                     timeline, anchors.anchor, anchors.focus, anchors.yaw, anchors.pitch, elapsed, distanceSqr, light, anchors.firstPersonCaster
                  )
               );
            }
         }
      }

      visible.sort(
         Comparator.<SungIlHwanVfxRenderer.VisibleEvent, Boolean>comparing(item -> !item.timeline.essential())
            .thenComparingDouble(item -> item.distanceSqr)
            .thenComparingLong(item -> item.timeline.sequence())
      );
      return visible.size() > quality.maxVisible ? new ArrayList<>(visible.subList(0, quality.maxVisible)) : visible;
   }

   private static SungIlHwanVfxRenderer.Anchors resolveAnchors(Minecraft minecraft, SungIlHwanVfxEventMessage message, float partialTick) {
      Vec3 anchor = message.origin();
      Vec3 focus = message.focus();
      float yaw = message.yawDegrees();
      float pitch = message.pitchDegrees();
      boolean firstPerson = false;
      Entity caster = minecraft.level.getEntity(message.casterEntityId);
      if (caster != null && !caster.isRemoved()) {
         Vec3 live = interpolated(caster, partialTick);
         if (followsCaster(message.eventType)) {
            Vec3 offset = focus.subtract(anchor);
            anchor = live;
            focus = live.add(offset);
         }

         yaw = Mth.rotLerp(partialTick, caster.yRotO, caster.getYRot());
         pitch = Mth.lerp(partialTick, caster.xRotO, caster.getXRot());
         firstPerson = minecraft.player.getId() == caster.getId() && minecraft.options.getCameraType().isFirstPerson();
      }

      if ((message.eventType == 4 || message.eventType == 7) && message.targetEntityId >= 0) {
         Entity target = minecraft.level.getEntity(message.targetEntityId);
         if (target != null && !target.isRemoved()) {
            focus = interpolated(target, partialTick).add(0.0, Math.max(0.7, target.getBbHeight() * 0.68), 0.0);
         }
      }

      if (message.eventType == 4 || message.eventType == 7 && message.variant >= 2) {
         anchor = focus;
      }

      return new SungIlHwanVfxRenderer.Anchors(anchor, focus, yaw, pitch, firstPerson);
   }

   private static Vec3 interpolated(Entity entity, float partialTick) {
      return new Vec3(
         Mth.lerp(partialTick, entity.xo, entity.getX()), Mth.lerp(partialTick, entity.yo, entity.getY()), Mth.lerp(partialTick, entity.zo, entity.getZ())
      );
   }

   private static boolean followsCaster(byte type) {
      return type == 0 || type == 1 || type == 2 || type == 6 || type == 7 || type == 10 || type == 11 || type == 12;
   }

   private static AABB bounds(SungIlHwanVfxEventMessage message, SungIlHwanVfxRenderer.Anchors anchors) {
      double radius = message.eventType == 7 && message.variant >= 2 ? 2.25 : message.radius + 1.5;
      double minX = Math.min(anchors.anchor.x, anchors.focus.x) - radius;
      double minY = Math.min(anchors.anchor.y, anchors.focus.y) - radius;
      double minZ = Math.min(anchors.anchor.z, anchors.focus.z) - radius;
      double maxX = Math.max(anchors.anchor.x, anchors.focus.x) + radius;
      double maxY = Math.max(anchors.anchor.y, anchors.focus.y) + radius + 2.5;
      double maxZ = Math.max(anchors.anchor.z, anchors.focus.z) + radius;
      return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
   }

   private static double distanceToBoundsSqr(Vec3 point, AABB bounds) {
      double dx = Math.max(Math.max(bounds.minX - point.x, 0.0), point.x - bounds.maxX);
      double dy = Math.max(Math.max(bounds.minY - point.y, 0.0), point.y - bounds.maxY);
      double dz = Math.max(Math.max(bounds.minZ - point.z, 0.0), point.z - bounds.maxZ);
      return dx * dx + dy * dy + dz * dz;
   }

   private static void render(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      if (budget.remaining >= 4) {
         PoseStack stack = frame.stack;
         stack.pushPose();
         stack.translate(visual.anchor.x - frame.camera.x, visual.anchor.y - frame.camera.y, visual.anchor.z - frame.camera.z);
         switch (visual.timeline.message().eventType) {
            case 0:
               renderStage(frame, visual, vertices, budget, pass, false);
               break;
            case 1:
               renderStage(frame, visual, vertices, budget, pass, true);
               break;
            case 2:
               renderStageEnd(frame, visual, vertices, budget, pass);
               break;
            case 3:
               renderFearPulse(frame, visual, vertices, budget, pass);
               break;
            case 4:
               renderFearMark(frame, visual, vertices, budget, pass);
               break;
            case 5:
               renderSpatialSlash(frame, visual, vertices, budget, pass);
               break;
            case 6:
               renderPublicCharge(frame, visual, vertices, budget, pass);
               break;
            case 7:
               renderPrivateTarget(frame, visual, vertices, budget, pass);
               break;
            case 8:
               renderExecutionRelease(frame, visual, vertices, budget, pass);
               break;
            case 9:
               renderFracture(frame, visual, vertices, budget, pass);
               break;
            case 10:
               renderCancel(frame, visual, vertices, budget, pass);
               break;
            case 11:
               renderExhaustion(frame, visual, vertices, budget, pass);
               break;
            case 12:
               renderRisk(frame, visual, vertices, budget, pass);
         }

         stack.popPose();
      }
   }

   private static void renderStage(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass,
      boolean stageTwo
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float fade = fadeInOut(visual.elapsed, event.duration, stageTwo ? 10.0F : 7.0F, 12.0F);
      float time = visual.elapsed;
      float strength = event.intensity / 255.0F;
      float radius = event.radius * (0.88F + Mth.sin(time * 0.12F) * 0.035F);
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      renderSpiritualGroundEnergy(frame, visual, out, budget, pass, stageTwo, radius, fade, strength, time, light);
      if (pass == SungIlHwanVfxRenderer.Pass.EMISSIVE && !visual.firstPersonCaster) {
         int motes = stageTwo ? frame.quality.stageTwoMotes : frame.quality.stageOneMotes;

         for (int index = 0; index < motes; index++) {
            float phase = index * (float) (Math.PI * 2) / Math.max(1, motes) + event.seed * 0.0071F + time * (stageTwo ? 0.19F : 0.11F);
            float y = 0.26F + index * 1.9F / Math.max(1, motes - 1);
            float orbit = (stageTwo ? 0.8F : 0.6F) + 0.08F * Mth.sin(time * 0.17F + index);
            Vec3 mote = new Vec3(Mth.cos(phase) * orbit, y, Mth.sin(phase) * orbit);
            drawDiamondBillboard(
               frame, out, budget, mote, stageTwo ? 0.11F : 0.075F, index % 3 == 0 ? 16775640 : 15978086, alpha((stageTwo ? 170.0F : 120.0F) * fade), 2, light
            );
         }

         if (stageTwo) {
            for (int side = -1; side <= 1; side += 2) {
               Vec3 start = new Vec3(side * 0.18, 1.28, 0.05);
               Vec3 end = new Vec3(side * 1.05, 2.28, 0.18);
               drawLine(out, frame.stack.last(), budget, start, end, 0.052F, 16773548, alpha(175.0F * fade), 2, light);
            }
         }
      }
   }

   private static void renderStageEnd(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      float progress = visual.timeline.progress(Minecraft.getInstance().level.getGameTime(), frame.partialTick);
      float fade = 1.0F - progress;
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      int strands = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? frame.quality.stageOneWisps + 2 : Math.max(4, frame.quality.stageOneWisps - 1);
      Random random = new Random(visual.timeline.message().seed ^ 1705578273L);

      for (int index = 0; index < strands; index++) {
         float angle = random.nextFloat() * (float) (Math.PI * 2);
         float distance = 0.18F + random.nextFloat() * 0.62F;
         Vec3 start = new Vec3(Mth.cos(angle) * distance, 0.05 + random.nextFloat() * 0.16, Mth.sin(angle) * distance);
         float reach = 0.75F + progress * 1.9F + random.nextFloat() * 0.5F;
         Vec3 end = new Vec3(Mth.cos(angle) * reach, 0.12 + progress * (0.35 + random.nextDouble()), Mth.sin(angle) * reach);
         drawEnergyStrand(
            out,
            frame.stack.last(),
            budget,
            start,
            end,
            0.05F * (1.0F - progress * 0.55F),
            pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 1382946 : (index % 4 == 0 ? 16773548 : 15048987),
            alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 72.0F : 142.0F) * fade),
            pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 4 : 2,
            light,
            Math.max(2, frame.quality.slashSegments / 2),
            index * 31 + visual.timeline.message().seed
         );
      }
   }

   private static void renderFearPulse(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float progress = Mth.clamp(visual.elapsed / event.duration, 0.0F, 1.0F);
      float eased = Mth.sqrt(progress);
      float fade = 1.0F - Mth.clamp((progress - 0.62F) / 0.38F, 0.0F, 1.0F);
      float radius = 0.72F + Math.max(0.2F, event.radius - 0.72F) * eased;
      float bodyEnvelope = 1.0F - Mth.clamp(progress / 0.48F, 0.0F, 1.0F);
      float shellEnvelope = Mth.sin(Mth.clamp(progress, 0.0F, 1.0F) * (float) Math.PI) * fade;
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      renderPresenceBodySurge(frame, visual, out, budget, pass, bodyEnvelope, progress, light);
      renderPresenceShockShell(frame, visual, out, budget, pass, radius, shellEnvelope, progress, light);
   }

   private static void renderFearMark(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      float pulse = 0.94F + 0.08F * Mth.sin(visual.elapsed * 0.48F);
      float fade = fadeInOut(visual.elapsed, visual.timeline.message().duration, 5.0F, 10.0F);
      float size = visual.timeline.message().radius * pulse;
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      frame.stack.pushPose();
      frame.stack.mulPose(frame.cameraOrientation);
      if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
         drawDiamondPlane(out, frame.stack.last(), budget, size * 0.76F, size, 461069, alpha(120.0F * fade), 0, light);
      } else {
         drawDiamondOutline(out, frame.stack.last(), budget, size * 0.72F, size * 0.94F, 0.045F, 16773548, alpha(225.0F * fade), 2, light);
         drawLine(
            out,
            frame.stack.last(),
            budget,
            new Vec3(-size * 0.38F, 0.0, 0.01),
            new Vec3(size * 0.38F, 0.0, 0.01),
            0.032F,
            15978086,
            alpha(188.0F * fade),
            2,
            light
         );
      }

      frame.stack.popPose();
   }

   private static void renderSpatialSlash(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float progress = Mth.clamp(visual.elapsed / event.duration, 0.0F, 1.0F);
      float reveal = Mth.clamp(progress / 0.14F, 0.0F, 1.0F);
      float fade = 1.0F - Mth.clamp((progress - 0.34F) / 0.66F, 0.0F, 1.0F);
      Vec3 authored = visual.focus.subtract(visual.anchor);
      Vec3 direction = authored.lengthSqr() > 1.0E-4 ? authored.normalize() : lookDirection(event.yawDegrees(), event.pitchDegrees());
      double reach = Math.max(8.0 + event.variant * 0.32, authored.length());
      Vec3 upReference = Math.abs(direction.y) > 0.92 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 lateral = direction.cross(upReference).normalize();
      Vec3 vertical = lateral.cross(direction).normalize();
      float halfLength = 4.15F + Math.min(1.55F, event.variant * 0.2F);

      float diagonal = switch (event.variant % 4) {
         case 0 -> 0.52F;
         case 1 -> -0.52F;
         case 2 -> 0.2F;
         default -> -0.72F;
      };
      Vec3 center = direction.scale(Math.min(6.2, 2.8 + reach * 0.38));
      Vec3 cutStart = center.add(lateral.scale(-halfLength)).add(vertical.scale(-halfLength * diagonal));
      Vec3 cutEnd = center.add(lateral.scale(halfLength)).add(vertical.scale(halfLength * diagonal));
      Vec3 revealedStart = center.add(cutStart.subtract(center).scale(reveal));
      Vec3 revealedEnd = center.add(cutEnd.subtract(center).scale(reveal));
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
         drawSharpBlade(
            out,
            frame.stack.last(),
            budget,
            revealedStart,
            revealedEnd,
            0.22F + event.radius * 0.08F,
            0.3F,
            461069,
            alpha(178.0F * fade),
            5,
            light,
            frame.quality.slashSegments
         );
      } else {
         Vec3 echoOffset = vertical.scale(-0.18).add(lateral.scale(-0.08)).add(direction.scale(-0.06));
         drawSharpBlade(
            out,
            frame.stack.last(),
            budget,
            revealedStart.add(echoOffset),
            revealedEnd.add(echoOffset),
            0.13F,
            0.24F,
            9392645,
            alpha(92.0F * fade),
            5,
            light,
            frame.quality.slashSegments
         );
         drawSharpBlade(
            out,
            frame.stack.last(),
            budget,
            revealedStart,
            revealedEnd,
            0.105F + event.radius * 0.018F,
            0.2F,
            15048987,
            alpha(225.0F * fade),
            5,
            light,
            frame.quality.slashSegments
         );
         drawSharpBlade(
            out, frame.stack.last(), budget, revealedStart, revealedEnd, 0.025F, 0.08F, 16775640, alpha(255.0F * fade), 5, light, frame.quality.slashSegments
         );
         if (event.variant >= 3) {
            Vec3 crossStart = center.add(lateral.scale(-halfLength * 0.9)).add(vertical.scale(halfLength * diagonal * 0.92));
            Vec3 crossEnd = center.add(lateral.scale(halfLength * 0.9)).add(vertical.scale(-halfLength * diagonal * 0.92));
            float secondaryReveal = Mth.clamp((reveal - 0.12F) / 0.88F, 0.0F, 1.0F);
            crossStart = center.add(crossStart.subtract(center).scale(secondaryReveal));
            crossEnd = center.add(crossEnd.subtract(center).scale(secondaryReveal));
            drawSharpBlade(
               out, frame.stack.last(), budget, crossStart, crossEnd, 0.082F, -0.2F, 15978086, alpha(210.0F * fade), 5, light, frame.quality.slashSegments
            );
            drawSharpBlade(
               out,
               frame.stack.last(),
               budget,
               revealedStart.add(vertical.scale(0.3)),
               revealedEnd.add(vertical.scale(0.3)),
               0.019F,
               0.08F,
               16773548,
               alpha(175.0F * fade),
               5,
               light,
               Math.max(5, frame.quality.slashSegments - 2)
            );
         }
      }
   }

   private static void renderPublicCharge(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float progress = Mth.clamp(visual.elapsed / event.duration, 0.0F, 1.0F);
      float pulse = 0.92F + 0.08F * Mth.sin(visual.elapsed * (0.28F + progress * 0.22F));
      float fade = fadeInOut(visual.elapsed, event.duration, 5.0F, 5.0F);
      float radius = event.radius * pulse;
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      if (pass != SungIlHwanVfxRenderer.Pass.SURFACE) {
         drawHorizontalRing(
            out,
            frame.stack.last(),
            budget,
            radius * (1.0F - progress * 0.32F),
            radius * (1.0F - progress * 0.32F) - 0.055F,
            0.08F + progress * 0.8F,
            16773548,
            alpha(190.0F * fade),
            2,
            light,
            frame.quality.ringSegments
         );
         drawHorizontalRing(
            out,
            frame.stack.last(),
            budget,
            radius * (0.54F + progress * 0.18F),
            radius * (0.5F + progress * 0.18F),
            1.25F + progress * 0.7F,
            15978086,
            alpha((95.0F + progress * 90.0F) * fade),
            2,
            light,
            frame.quality.ringSegments
         );
         if (!visual.firstPersonCaster) {
            drawLine(
               out,
               frame.stack.last(),
               budget,
               new Vec3(0.0, 0.25, 0.0),
               new Vec3(0.0, 2.35 + progress * 0.55, 0.0),
               0.055F,
               16773548,
               alpha((85.0F + progress * 110.0F) * fade),
               2,
               light
            );
         }
      } else {
         drawHorizontalRing(out, frame.stack.last(), budget, radius, radius - 0.34F, 0.04F, 461069, alpha(118.0F * fade), 0, light, frame.quality.ringSegments);
         if (!visual.firstPersonCaster) {
            int blades = frame.quality.chargeBlades;

            for (int index = 0; index < blades; index++) {
               frame.stack.pushPose();
               frame.stack.mulPose(Axis.YP.rotationDegrees(index * 360.0F / blades - visual.elapsed * (3.0F + progress * 4.0F)));
               frame.stack.translate(0.0, 0.0, 0.55 + index % 2 * 0.22);
               drawTaperedBlade(out, frame.stack.last(), budget, 0.18F, 1.45F + progress * 0.9F, 1382946, alpha(76.0F * fade), 4, light);
               frame.stack.popPose();
            }
         }
      }
   }

   private static void renderPrivateTarget(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      if (event.variant >= 2) {
         renderExecutionTargetMark(frame, visual, out, budget, pass);
      } else {
         float progress = Mth.clamp(visual.elapsed / event.duration, 0.0F, 1.0F);
         float authoredProgress = event.intensity / 255.0F;
         float charge = Mth.clamp(Math.max(progress, authoredProgress), 0.0F, 1.0F);
         float pulse = 0.98F + 0.025F * Mth.sin(visual.elapsed * 0.42F);
         float radius = event.variant == 0 ? event.radius * (0.16F + 0.84F * charge) * pulse : event.radius * pulse;
         int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
         int segments = frame.quality.sphereSegments;
         frame.stack.pushPose();
         frame.stack.translate(0.0, 1.0, 0.0);
         if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
            drawSphereBands(out, frame.stack.last(), budget, radius, 0.035F, 1382946, 42, 0, light, segments, false);
            frame.stack.popPose();
         } else {
            drawSphereBands(out, frame.stack.last(), budget, radius, 0.028F, 15978086, alpha(115.0F + charge * 70.0F), 2, light, segments, true);
            drawHorizontalRing(
               out,
               frame.stack.last(),
               budget,
               radius * (0.42F + 0.08F * Mth.sin(visual.elapsed * 0.3F)),
               radius * 0.38F,
               0.0F,
               16773548,
               alpha(175.0F),
               2,
               light,
               segments
            );
            frame.stack.pushPose();
            frame.stack.mulPose(frame.cameraOrientation);
            drawDiamondOutline(
               out, frame.stack.last(), budget, Math.min(0.9F, radius * 0.24F), Math.min(1.15F, radius * 0.32F), 0.03F, 16775640, alpha(225.0F), 2, light
            );
            frame.stack.popPose();
            frame.stack.popPose();
         }
      }
   }

   private static void renderExecutionTargetMark(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float fade = fadeInOut(visual.elapsed, event.duration, 2.0F, 6.0F);
      float pulse = 0.92F + 0.1F * Mth.sin(visual.elapsed * 0.62F + event.seed * 0.01F);
      float size = 0.72F * pulse;
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      frame.stack.pushPose();
      frame.stack.mulPose(frame.cameraOrientation);
      if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
         drawDiamondPlane(out, frame.stack.last(), budget, size * 0.72F, size, 461069, alpha(112.0F * fade), 0, light);
      } else {
         drawDiamondOutline(out, frame.stack.last(), budget, size * 0.76F, size, 0.052F, 16773548, alpha(238.0F * fade), 2, light);
         drawLine(
            out,
            frame.stack.last(),
            budget,
            new Vec3(-size * 0.94, 0.0, 0.01),
            new Vec3(size * 0.94, 0.0, 0.01),
            0.026F,
            16775640,
            alpha(220.0F * fade),
            2,
            light
         );
      }

      frame.stack.popPose();
   }

   private static void renderExecutionRelease(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float progress = Mth.clamp(visual.elapsed / event.duration, 0.0F, 1.0F);
      float fade = 1.0F - Mth.clamp((progress - 0.58F) / 0.42F, 0.0F, 1.0F);
      float radius = event.radius;
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      Vec3 sphereCenter = new Vec3(0.0, 1.0, 0.0);
      frame.stack.pushPose();
      frame.stack.translate(sphereCenter.x, sphereCenter.y, sphereCenter.z);
      if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
         drawSphereBands(out, frame.stack.last(), budget, radius, 0.06F, 461069, alpha(74.0F * fade), 0, light, frame.quality.sphereSegments, true);
      } else {
         drawSphereBands(out, frame.stack.last(), budget, radius, 0.025F, 15048987, alpha(92.0F * fade), 2, light, frame.quality.sphereSegments, true);
      }

      frame.stack.popPose();
      Random random = new Random(event.seed ^ 1247102023);
      int cuts = frame.quality.executionCuts;

      for (int index = 0; index < cuts; index++) {
         Vec3 center = sphereCenter.add(randomPointInSphere(random, radius * 0.76F));
         Vec3 direction = randomUnit(random);
         double length = radius * (0.28 + random.nextDouble() * 0.42);
         Vec3 start = center.subtract(direction.scale(length * 0.5));
         Vec3 end = center.add(direction.scale(length * 0.5));
         float activation = (float)index / Math.max(1, cuts) * 0.44F;
         float localReveal = Mth.clamp((progress - activation) / 0.055F, 0.0F, 1.0F);
         float localFade = 1.0F - Mth.clamp((progress - activation - 0.16F) / 0.32F, 0.0F, 1.0F);
         if (!(localReveal <= 0.0F) && !(localFade <= 0.0F)) {
            Vec3 midpoint = start.add(end).scale(0.5);
            start = midpoint.add(start.subtract(midpoint).scale(localReveal));
            end = midpoint.add(end.subtract(midpoint).scale(localReveal));
            int fieldSegments = Math.max(4, (frame.quality.slashSegments + 1) / 2);
            float bend = index % 2 == 0 ? 0.22F : -0.22F;
            if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
               drawSharpBlade(out, frame.stack.last(), budget, start, end, 0.105F, bend, 461069, alpha(152.0F * localFade * fade), 5, light, fieldSegments);
            } else {
               drawSharpBlade(
                  out,
                  frame.stack.last(),
                  budget,
                  start,
                  end,
                  0.062F,
                  bend,
                  index % 4 == 0 ? 16775640 : 16773548,
                  alpha(238.0F * localFade * fade),
                  5,
                  light,
                  fieldSegments
               );
               drawSharpBlade(
                  out, frame.stack.last(), budget, start, end, 0.018F, bend * 0.35F, 16775640, alpha(248.0F * localFade * fade), 5, light, fieldSegments
               );
            }
         }
      }

      Random echoRandom = new Random(event.seed ^ 1397312584);
      Vec3 previous = sphereCenter;

      for (int echo = 0; echo < frame.quality.afterimages; echo++) {
         Vec3 position = sphereCenter.add(randomPointInSphere(echoRandom, radius * 0.68F));
         position = new Vec3(position.x, Math.max(0.0, position.y - 0.55), position.z);
         float echoWindow = Mth.clamp(1.0F - Math.abs(progress - (0.08F + echo * 0.09F)) / 0.18F, 0.0F, 1.0F);
         drawHumanoidAfterimage(
            frame,
            out,
            budget,
            position,
            0.82F,
            pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 1382946 : 15978086,
            alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 76.0F : 142.0F) * echoWindow * fade),
            pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 4 : 2,
            light
         );
         if (pass == SungIlHwanVfxRenderer.Pass.EMISSIVE) {
            drawLine(out, frame.stack.last(), budget, previous, position.add(0.0, 0.75, 0.0), 0.026F, 15048987, alpha(92.0F * echoWindow * fade), 2, light);
         }

         previous = position.add(0.0, 0.75, 0.0);
      }
   }

   private static void drawHumanoidAfterimage(
      SungIlHwanVfxRenderer.RenderFrame frame,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      Vec3 position,
      float scale,
      int color,
      int alpha,
      int material,
      int light
   ) {
      frame.stack.pushPose();
      frame.stack.translate(position.x, position.y, position.z);
      frame.stack.mulPose(frame.cameraOrientation);
      drawPlane(out, frame.stack.last(), budget, -0.22F * scale, 0.45F * scale, 0.22F * scale, 1.43F * scale, color, alpha, material, light);
      drawDiamondPlane(out, frame.stack.last(), budget, 0.18F * scale, 0.22F * scale, color, alpha, material, light);
      frame.stack.translate(0.0, 1.62 * scale, 0.0);
      drawDiamondPlane(out, frame.stack.last(), budget, 0.17F * scale, 0.21F * scale, color, alpha, material, light);
      frame.stack.popPose();
   }

   private static void renderFracture(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float progress = Mth.clamp(visual.elapsed / event.duration, 0.0F, 1.0F);
      float reveal = Mth.clamp(progress / 0.16F, 0.0F, 1.0F);
      float fade = 1.0F - Mth.clamp((progress - 0.48F) / 0.52F, 0.0F, 1.0F);
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      Vec3 sphereCenter = Vec3.ZERO;
      frame.stack.pushPose();
      frame.stack.translate(sphereCenter.x, sphereCenter.y, sphereCenter.z);
      if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
         drawSphereBands(
            out,
            frame.stack.last(),
            budget,
            event.radius * (0.88F + progress * 0.2F),
            0.11F,
            461069,
            alpha(152.0F * fade),
            3,
            light,
            frame.quality.sphereSegments,
            true
         );
      } else {
         drawSphereBands(
            out,
            frame.stack.last(),
            budget,
            event.radius * (0.88F + progress * 0.2F),
            0.045F,
            16775640,
            alpha(245.0F * fade),
            2,
            light,
            frame.quality.sphereSegments,
            true
         );
         drawHorizontalRing(
            out,
            frame.stack.last(),
            budget,
            event.radius * (0.65F + progress * 0.62F),
            event.radius * (0.65F + progress * 0.62F) - 0.08F,
            0.0F,
            16773548,
            alpha(220.0F * fade),
            2,
            light,
            frame.quality.ringSegments
         );
      }

      frame.stack.popPose();
      Random random = new Random(event.seed ^ 1179795779);
      int cuts = frame.quality.executionCuts + frame.quality.fractureRays;

      for (int index = 0; index < cuts; index++) {
         Vec3 center = sphereCenter.add(randomPointInSphere(random, event.radius * 0.8F));
         Vec3 direction = randomUnit(random);
         double length = event.radius * (0.3 + random.nextDouble() * 0.52);
         Vec3 start = center.subtract(direction.scale(length * 0.5 * reveal));
         Vec3 end = center.add(direction.scale(length * 0.5 * reveal));
         int fieldSegments = Math.max(4, (frame.quality.slashSegments + 1) / 2);
         float bend = index % 2 == 0 ? 0.28F : -0.28F;
         if (pass == SungIlHwanVfxRenderer.Pass.SURFACE) {
            drawSharpBlade(out, frame.stack.last(), budget, start, end, 0.135F, bend, 461069, alpha(184.0F * fade), 5, light, fieldSegments);
         } else {
            drawSharpBlade(
               out, frame.stack.last(), budget, start, end, 0.068F, bend, index % 5 == 0 ? 16775640 : 16773548, alpha(252.0F * fade), 5, light, fieldSegments
            );
            drawSharpBlade(out, frame.stack.last(), budget, start, end, 0.019F, bend * 0.32F, 16775640, alpha(255.0F * fade), 5, light, fieldSegments);
         }
      }
   }

   private static void renderCancel(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      float progress = visual.timeline.progress(Minecraft.getInstance().level.getGameTime(), frame.partialTick);
      float radius = visual.timeline.message().radius * (1.0F - progress * 0.72F);
      float fade = 1.0F - progress;
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      drawHorizontalRing(
         out,
         frame.stack.last(),
         budget,
         radius,
         Math.max(0.0F, radius - (pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.25F : 0.045F)),
         0.25F + progress,
         pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 461069 : 15048987,
         alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 90.0F : 125.0F) * fade),
         pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0 : 2,
         light,
         frame.quality.ringSegments
      );
   }

   private static void renderExhaustion(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      float severity = event.intensity / 255.0F;
      float fade = fadeInOut(visual.elapsed, event.duration, 3.0F, 12.0F);
      int light = pass == SungIlHwanVfxRenderer.Pass.EMISSIVE ? 15728880 : visual.light;
      float radius = event.radius * (0.85F + 0.08F * Mth.sin(visual.elapsed * 0.42F));
      drawHorizontalRing(
         out,
         frame.stack.last(),
         budget,
         radius,
         Math.max(0.0F, radius - (pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.26F : 0.035F)),
         0.055F,
         pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 461069 : 7568518,
         alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 105.0F : 120.0F) * fade * (0.6F + severity * 0.4F)),
         pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0 : 1,
         light,
         frame.quality.ringSegments
      );
      if (!visual.firstPersonCaster) {
         int shards = frame.quality == SungIlHwanVfxRenderer.Quality.FULL ? 6 : 3;

         for (int index = 0; index < shards; index++) {
            float angle = index * (float) (Math.PI * 2) / shards + event.seed * 0.01F;
            Vec3 start = new Vec3(Mth.cos(angle) * 0.52, 1.7 + index % 2 * 0.18, Mth.sin(angle) * 0.52);
            Vec3 end = start.add(0.0, -0.5 - severity * 0.55, 0.0);
            drawLine(
               out,
               frame.stack.last(),
               budget,
               start,
               end,
               pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.07F : 0.025F,
               pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 1382946 : 15978086,
               alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 95.0F : 145.0F) * fade),
               pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 4 : 2,
               light
            );
         }
      }
   }

   private static void renderRisk(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass
   ) {
      if (pass != SungIlHwanVfxRenderer.Pass.SURFACE) {
         float fade = fadeInOut(visual.elapsed, visual.timeline.message().duration, 2.0F, 8.0F);
         float severity = visual.timeline.message().intensity / 255.0F;
         float radius = 0.72F + severity * 0.42F;
         drawHorizontalRing(
            out, frame.stack.last(), budget, radius, radius - 0.035F, 0.12F, 16773548, alpha(110.0F * fade * severity), 2, 15728880, frame.quality.ringSegments
         );
      }
   }

   private static void renderSpiritualGroundEnergy(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass,
      boolean stageTwo,
      float radius,
      float fade,
      float strength,
      float time,
      int light
   ) {
      SungIlHwanVfxEventMessage event = visual.timeline.message();
      int wisps = stageTwo ? frame.quality.stageTwoWisps : frame.quality.stageOneWisps;
      Random random = new Random(event.seed ^ 1299852313L);

      for (int index = 0; index < wisps; index++) {
         float baseAngle = random.nextFloat() * (float) (Math.PI * 2);
         float drift = Mth.sin(time * (0.045F + random.nextFloat() * 0.025F) + index * 1.73F) * (stageTwo ? 0.18F : 0.11F);
         float angle = baseAngle + drift;
         float baseRadius = radius * (0.16F + random.nextFloat() * 0.34F);
         Vec3 root = new Vec3(Mth.cos(angle) * baseRadius, 0.035, Mth.sin(angle) * baseRadius);
         float rootReach = radius * (0.72F + random.nextFloat() * 0.34F);
         Vec3 outerRoot = new Vec3(Mth.cos(angle) * rootReach, 0.045 + random.nextDouble() * 0.07, Mth.sin(angle) * rootReach);
         float flicker = 0.76F + 0.24F * Mth.sin(time * 0.26F + index * 2.17F);
         int rootColor = pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 1382946 : (index % 4 == 0 ? 16773548 : 15048987);
         drawEnergyStrand(
            out,
            frame.stack.last(),
            budget,
            root,
            outerRoot,
            (pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.075F : 0.032F) * (stageTwo ? 1.16F : 1.0F),
            rootColor,
            alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 76.0F : 158.0F) * fade * flicker * strength),
            pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 4 : 2,
            light,
            Math.max(2, frame.quality.slashSegments / 2),
            event.seed + index * 43
         );
         float height = (stageTwo ? 1.65F : 1.08F) * (0.72F + random.nextFloat() * 0.62F) * flicker;
         Vec3 flameEnd = root.add(
            Mth.cos(angle + (float) (Math.PI / 2)) * (0.16F + random.nextFloat() * 0.25F),
            height,
            Mth.sin(angle + (float) (Math.PI / 2)) * (0.16F + random.nextFloat() * 0.25F)
         );
         int flameColor = pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 9392645 : (index % 3 == 0 ? 16775640 : 15978086);
         drawEnergyStrand(
            out,
            frame.stack.last(),
            budget,
            root,
            flameEnd,
            (pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.095F : 0.047F) * (stageTwo ? 1.2F : 1.0F),
            flameColor,
            alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 70.0F : 184.0F) * fade * flicker * strength),
            4,
            light,
            Math.max(3, frame.quality.slashSegments / 2),
            event.seed ^ index * 101
         );
      }

      if (pass == SungIlHwanVfxRenderer.Pass.EMISSIVE) {
         int streaks = Math.max(4, wisps - 1);

         for (int index = 0; index < streaks; index++) {
            float angle = index * (float) (Math.PI * 2) / streaks + event.seed * 0.0037F + Mth.sin(time * 0.07F + index) * 0.1F;
            float pulse = 0.72F + 0.28F * Mth.sin(time * 0.32F + index * 1.31F);
            Vec3 start = new Vec3(Mth.cos(angle) * radius * 0.12F, 0.065, Mth.sin(angle) * radius * 0.12F);
            Vec3 end = new Vec3(Mth.cos(angle) * radius * (0.62F + 0.24F * pulse), 0.07 + 0.06 * pulse, Mth.sin(angle) * radius * (0.62F + 0.24F * pulse));
            drawEnergyStrand(
               out,
               frame.stack.last(),
               budget,
               start,
               end,
               stageTwo ? 0.03F : 0.023F,
               index % 3 == 0 ? 16775640 : 15048987,
               alpha((stageTwo ? 152.0F : 112.0F) * fade * pulse),
               2,
               light,
               Math.max(2, frame.quality.slashSegments / 3),
               event.seed - index * 59
            );
         }
      }
   }

   private static void renderPresenceBodySurge(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass,
      float envelope,
      float progress,
      int light
   ) {
      if (!(envelope <= 0.001F)) {
         SungIlHwanVfxEventMessage event = visual.timeline.message();
         int strands = Math.max(8, frame.quality.stageTwoWisps * 2);
         Random random = new Random(event.seed ^ 1580246996L);
         float firstPersonScale = visual.firstPersonCaster ? 0.48F : 1.0F;

         for (int index = 0; index < strands; index++) {
            float angle = random.nextFloat() * (float) (Math.PI * 2);
            float startY = 0.08F + random.nextFloat() * 1.82F;
            float startRadius = 0.12F + random.nextFloat() * 0.33F;
            Vec3 start = new Vec3(Mth.cos(angle) * startRadius, startY, Mth.sin(angle) * startRadius);
            float reach = 0.75F + random.nextFloat() * 1.7F + progress * 1.45F;
            float rise = (random.nextFloat() - 0.16F) * (1.25F + reach * 0.38F);
            Vec3 end = start.add(Mth.cos(angle) * reach, rise, Mth.sin(angle) * reach);
            float flicker = 0.74F + 0.26F * Mth.sin(visual.elapsed * 0.42F + index * 1.91F);
            int color = pass == SungIlHwanVfxRenderer.Pass.SURFACE
               ? (index % 4 == 0 ? 9392645 : 1382946)
               : (index % 5 == 0 ? 16775640 : (index % 2 == 0 ? 16773548 : 15048987));
            drawEnergyStrand(
               out,
               frame.stack.last(),
               budget,
               start,
               end,
               (pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.13F : 0.056F) * (0.82F + random.nextFloat() * 0.42F),
               color,
               alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 118.0F : 232.0F) * envelope * flicker * firstPersonScale),
               4,
               light,
               Math.max(3, frame.quality.slashSegments / 2),
               event.seed + index * 79
            );
         }

         int columns = Math.max(3, frame.quality.stageOneWisps / 2);

         for (int index = 0; index < columns; index++) {
            float angle = index * (float) (Math.PI * 2) / columns + event.seed * 0.005F;
            Vec3 start = new Vec3(Mth.cos(angle) * 0.22F, 0.03, Mth.sin(angle) * 0.22F);
            Vec3 end = new Vec3(Mth.cos(angle + 0.7F) * 0.46F, 2.55 + index * 0.18, Mth.sin(angle + 0.7F) * 0.46F);
            drawEnergyStrand(
               out,
               frame.stack.last(),
               budget,
               start,
               end,
               pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.16F : 0.067F,
               pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 9392645 : 16775640,
               alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 100.0F : 245.0F) * envelope * firstPersonScale),
               4,
               light,
               Math.max(4, frame.quality.slashSegments / 2),
               event.seed ^ index * 181
            );
         }
      }
   }

   private static void renderPresenceShockShell(
      SungIlHwanVfxRenderer.RenderFrame frame,
      SungIlHwanVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      SungIlHwanVfxRenderer.Pass pass,
      float radius,
      float envelope,
      float progress,
      int light
   ) {
      if (!(envelope <= 0.001F)) {
         SungIlHwanVfxEventMessage event = visual.timeline.message();
         float verticalRadius = Math.max(1.05F, radius * 0.64F);
         int longitudeSegments = Math.max(8, frame.quality.sphereSegments + 4);
         int latitudeSegments = Math.max(4, frame.quality.sphereSegments / 2 + 1);
         frame.stack.pushPose();
         frame.stack.translate(0.0, 1.0, 0.0);
         drawEnergyShell(
            out,
            frame.stack.last(),
            budget,
            radius,
            verticalRadius,
            pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 9392645 : 16773548,
            alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 42.0F : 88.0F) * envelope),
            4,
            light,
            longitudeSegments,
            latitudeSegments,
            event.seed,
            progress,
            pass == SungIlHwanVfxRenderer.Pass.EMISSIVE
         );
         frame.stack.popPose();
         int rays = Math.max(8, frame.quality.stageTwoWisps * 2);
         Random random = new Random(event.seed ^ 699875779L);
         Vec3 center = new Vec3(0.0, 1.0, 0.0);

         for (int index = 0; index < rays; index++) {
            Vec3 direction = randomUnit(random);
            direction = new Vec3(direction.x, direction.y * 0.72, direction.z).normalize();
            float jitter = 0.8F + random.nextFloat() * 0.18F;
            Vec3 start = center.add(direction.scale(radius * jitter));
            Vec3 end = center.add(direction.scale(radius * (1.02F + random.nextFloat() * 0.09F) + 0.25F + random.nextFloat() * 0.6F));
            int color = pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 9392645 : (index % 5 == 0 ? 16775640 : 15978086);
            drawEnergyStrand(
               out,
               frame.stack.last(),
               budget,
               start,
               end,
               pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 0.075F : 0.035F,
               color,
               alpha((pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 78.0F : 196.0F) * envelope),
               pass == SungIlHwanVfxRenderer.Pass.SURFACE ? 4 : 2,
               light,
               Math.max(2, frame.quality.slashSegments / 3),
               event.seed + index * 137
            );
         }
      }
   }

   private static void drawEnergyShell(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      float horizontalRadius,
      float verticalRadius,
      int color,
      int alpha,
      int material,
      int light,
      int longitudeSegments,
      int latitudeSegments,
      int seed,
      float time,
      boolean broken
   ) {
      if (alpha > 0 && !(horizontalRadius <= 0.0F) && !(verticalRadius <= 0.0F)) {
         for (int latitude = 0; latitude < latitudeSegments; latitude++) {
            float firstV = (float)latitude / latitudeSegments;
            float secondV = (latitude + 1.0F) / latitudeSegments;
            float firstLatitude = (float) (-Math.PI / 2) + firstV * (float) Math.PI;
            float secondLatitude = (float) (-Math.PI / 2) + secondV * (float) Math.PI;

            for (int longitude = 0; longitude < longitudeSegments; longitude++) {
               int hash = seed + longitude * 7349 + latitude * 1933;
               if (!broken || (hash & 7) != 0) {
                  if (!budget.take(4)) {
                     return;
                  }

                  float firstU = (float)longitude / longitudeSegments;
                  float secondU = (longitude + 1.0F) / longitudeSegments;
                  float firstLongitude = firstU * (float) (Math.PI * 2);
                  float secondLongitude = secondU * (float) (Math.PI * 2);
                  float ripple = 1.0F + 0.025F * Mth.sin(firstLongitude * 5.0F + firstLatitude * 3.0F + time * 14.0F + seed * 0.001F);
                  Vec3 a = ellipsoidPoint(firstLatitude, firstLongitude, horizontalRadius * ripple, verticalRadius * ripple);
                  Vec3 b = ellipsoidPoint(firstLatitude, secondLongitude, horizontalRadius * ripple, verticalRadius * ripple);
                  Vec3 c = ellipsoidPoint(secondLatitude, secondLongitude, horizontalRadius * ripple, verticalRadius * ripple);
                  Vec3 d = ellipsoidPoint(secondLatitude, firstLongitude, horizontalRadius * ripple, verticalRadius * ripple);
                  int panelAlpha = alpha(alpha * (0.78F + 0.22F * (hash >>> 3 & 3) / 3.0F));
                  shellVertex(out, pose, a, color, panelAlpha, material + 0.02F, 0.02F, light, horizontalRadius, verticalRadius);
                  shellVertex(out, pose, b, color, panelAlpha, material + 0.98F, 0.02F, light, horizontalRadius, verticalRadius);
                  shellVertex(out, pose, c, color, panelAlpha, material + 0.98F, 0.98F, light, horizontalRadius, verticalRadius);
                  shellVertex(out, pose, d, color, panelAlpha, material + 0.02F, 0.98F, light, horizontalRadius, verticalRadius);
               }
            }
         }
      }
   }

   private static Vec3 ellipsoidPoint(float latitude, float longitude, float horizontalRadius, float verticalRadius) {
      float horizontal = Mth.cos(latitude) * horizontalRadius;
      return new Vec3(Mth.cos(longitude) * horizontal, Mth.sin(latitude) * verticalRadius, Mth.sin(longitude) * horizontal);
   }

   private static void shellVertex(
      VertexConsumer out, Pose pose, Vec3 point, int color, int alpha, float u, float v, int light, float horizontalRadius, float verticalRadius
   ) {
      Vec3 normal = new Vec3(
         point.x / Math.max(0.001, horizontalRadius * horizontalRadius),
         point.y / Math.max(0.001, verticalRadius * verticalRadius),
         point.z / Math.max(0.001, horizontalRadius * horizontalRadius)
      );
      if (normal.lengthSqr() < 1.0E-6) {
         normal = new Vec3(0.0, 1.0, 0.0);
      } else {
         normal = normal.normalize();
      }

      vertex(out, pose, (float)point.x, (float)point.y, (float)point.z, color, alpha, u, v, light, (float)normal.x, (float)normal.y, (float)normal.z);
   }

   private static void drawEnergyStrand(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      Vec3 start,
      Vec3 end,
      float width,
      int color,
      int alpha,
      int material,
      int light,
      int segments,
      int seed
   ) {
      Vec3 delta = end.subtract(start);
      if (alpha > 0 && !(width <= 0.0F) && !(delta.lengthSqr() < 1.0E-6)) {
         Vec3 direction = delta.normalize();
         Vec3 reference = Math.abs(direction.y) < 0.82 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
         Vec3 bend = direction.cross(reference);
         if (bend.lengthSqr() < 1.0E-6) {
            bend = new Vec3(0.0, 0.0, 1.0);
         }

         bend = bend.normalize().scale(0.05 + Math.min(0.48, delta.length() * 0.09));
         if ((seed & 1) == 0) {
            bend = bend.scale(-1.0);
         }

         int safeSegments = Math.max(2, Math.min(8, segments));
         Vec3 previous = start;

         for (int index = 1; index <= safeSegments; index++) {
            float t = (float)index / safeSegments;
            float curve = Mth.sin(t * (float) Math.PI) * Mth.sin(seed * 0.013F + t * 5.7F);
            Vec3 next = start.add(delta.scale(t)).add(bend.scale(curve));
            float taper = 1.0F - t * 0.68F;
            int localAlpha = alpha(alpha * (0.96F - t * 0.38F));
            drawCrossedLine(out, pose, budget, previous, next, Math.max(0.006F, width * taper), color, localAlpha, material, light);
            previous = next;
         }
      }
   }

   private static Vec3 lookDirection(float yawDegrees, float pitchDegrees) {
      float yaw = -yawDegrees * (float) (Math.PI / 180.0) - (float) Math.PI;
      float pitch = -pitchDegrees * (float) (Math.PI / 180.0);
      float horizontal = -Mth.cos(pitch);
      Vec3 direction = new Vec3(Mth.sin(yaw) * horizontal, Mth.sin(pitch), Mth.cos(yaw) * horizontal);
      return direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
   }

   private static Vec3 randomUnit(Random random) {
      double y = random.nextDouble() * 2.0 - 1.0;
      double angle = random.nextDouble() * Math.PI * 2.0;
      double horizontal = Math.sqrt(Math.max(0.0, 1.0 - y * y));
      return new Vec3(Math.cos(angle) * horizontal, y, Math.sin(angle) * horizontal);
   }

   private static Vec3 randomPointInSphere(Random random, double radius) {
      double distance = Math.cbrt(random.nextDouble()) * Math.max(0.0, radius);
      return randomUnit(random).scale(distance);
   }

   private static void drawCurvedTrail(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      Vec3 end,
      float curve,
      float width,
      float reveal,
      int color,
      int alpha,
      int material,
      int light,
      int segments,
      int seed
   ) {
      if (!(reveal <= 0.0F) && !(end.lengthSqr() < 0.001)) {
         Vec3 direction = end.normalize();
         Vec3 lateral = direction.cross(new Vec3(0.0, 1.0, 0.0));
         if (lateral.lengthSqr() < 0.001) {
            lateral = new Vec3(1.0, 0.0, 0.0);
         }

         lateral = lateral.normalize();
         Vec3 control = end.scale(0.5).add(lateral.scale(curve * Math.min(5.0, end.length()))).add(0.0, Math.abs(curve) * 0.75, 0.0);
         int visibleSegments = Math.max(1, Math.min(segments, Mth.ceil(segments * reveal)));
         Vec3 previous = Vec3.ZERO;

         for (int index = 1; index <= visibleSegments; index++) {
            float t = (float)index / segments;
            float inverse = 1.0F - t;
            Vec3 next = control.scale(2.0 * inverse * t).add(end.scale(t * t));
            float localWidth = width * (0.42F + Mth.sin(t * (float) Math.PI) * 0.72F);
            drawLine(out, pose, budget, previous, next, localWidth, color, alpha, material, light);
            previous = next;
         }
      }
   }

   private static void drawSphereBands(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      float radius,
      float width,
      int color,
      int alpha,
      int material,
      int light,
      int segments,
      boolean full
   ) {
      int latitudeCount = full ? 3 : 2;

      for (int latitude = 0; latitude < latitudeCount; latitude++) {
         float normalized = latitudeCount == 1 ? 0.0F : (float)latitude / (latitudeCount - 1);
         float y = (normalized - 0.5F) * radius * 1.2F;
         float ringRadius = Mth.sqrt(Math.max(0.01F, radius * radius - y * y));
         drawHorizontalRing(out, pose, budget, ringRadius, Math.max(0.0F, ringRadius - width), y, color, alpha, material, light, segments);
      }

      int longitudes = full ? 3 : 2;

      for (int longitude = 0; longitude < longitudes; longitude++) {
         float angle = longitude * (float) Math.PI / longitudes;
         Vec3[] points = new Vec3[segments + 1];

         for (int index = 0; index <= segments; index++) {
            float phase = index * (float) (Math.PI * 2) / segments;
            float planar = Mth.cos(phase) * radius;
            points[index] = new Vec3(Mth.cos(angle) * planar, Mth.sin(phase) * radius, Mth.sin(angle) * planar);
         }

         drawPolyline(out, pose, budget, points, width, color, alpha, material, light);
      }
   }

   private static void drawPolyline(
      VertexConsumer out, Pose pose, SungIlHwanVfxRenderer.FrameBudget budget, Vec3[] points, float width, int color, int alpha, int material, int light
   ) {
      for (int index = 1; index < points.length; index++) {
         drawLine(out, pose, budget, points[index - 1], points[index], width, color, alpha, material, light);
      }
   }

   private static void drawHorizontalRing(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      float outer,
      float inner,
      float y,
      int color,
      int alpha,
      int material,
      int light,
      int segments
   ) {
      if (!(outer <= 0.0F) && alpha > 0) {
         float safeInner = Mth.clamp(inner, 0.0F, outer);

         for (int index = 0; index < segments; index++) {
            if (!budget.take(4)) {
               return;
            }

            float first = index * (float) (Math.PI * 2) / segments;
            float second = (index + 1) * (float) (Math.PI * 2) / segments;
            float firstCos = Mth.cos(first);
            float firstSin = Mth.sin(first);
            float secondCos = Mth.cos(second);
            float secondSin = Mth.sin(second);
            vertex(out, pose, firstCos * safeInner, y, firstSin * safeInner, color, alpha, material + 0.02F, 0.02F, light, 0.0F, 1.0F, 0.0F);
            vertex(out, pose, firstCos * outer, y, firstSin * outer, color, alpha, material + 0.98F, 0.02F, light, 0.0F, 1.0F, 0.0F);
            vertex(out, pose, secondCos * outer, y, secondSin * outer, color, alpha, material + 0.98F, 0.98F, light, 0.0F, 1.0F, 0.0F);
            vertex(out, pose, secondCos * safeInner, y, secondSin * safeInner, color, alpha, material + 0.02F, 0.98F, light, 0.0F, 1.0F, 0.0F);
         }
      }
   }

   private static void drawTaperedBlade(
      VertexConsumer out, Pose pose, SungIlHwanVfxRenderer.FrameBudget budget, float halfWidth, float height, int color, int alpha, int material, int light
   ) {
      if (budget.take(4)) {
         vertex(out, pose, -halfWidth, 0.0F, 0.0F, color, 0, material + 0.02F, 0.98F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, halfWidth, 0.0F, 0.0F, color, 0, material + 0.98F, 0.98F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, halfWidth * 0.12F, height * 0.82F, 0.0F, color, alpha, material + 0.98F, 0.18F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, 0.0F, height, 0.0F, color, 0, material + 0.5F, 0.02F, light, 0.0F, 0.0F, 1.0F);
      }
   }

   private static void drawDiamondBillboard(
      SungIlHwanVfxRenderer.RenderFrame frame,
      VertexConsumer out,
      SungIlHwanVfxRenderer.FrameBudget budget,
      Vec3 center,
      float size,
      int color,
      int alpha,
      int material,
      int light
   ) {
      frame.stack.pushPose();
      frame.stack.translate(center.x, center.y, center.z);
      frame.stack.mulPose(frame.cameraOrientation);
      drawDiamondPlane(out, frame.stack.last(), budget, size, size, color, alpha, material, light);
      frame.stack.popPose();
   }

   private static void drawDiamondPlane(
      VertexConsumer out, Pose pose, SungIlHwanVfxRenderer.FrameBudget budget, float width, float height, int color, int alpha, int material, int light
   ) {
      if (budget.take(4)) {
         vertex(out, pose, 0.0F, -height, 0.0F, color, 0, material + 0.5F, 0.98F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, width, 0.0F, 0.0F, color, alpha, material + 0.98F, 0.5F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, 0.0F, height, 0.0F, color, alpha, material + 0.5F, 0.02F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, -width, 0.0F, 0.0F, color, alpha, material + 0.02F, 0.5F, light, 0.0F, 0.0F, 1.0F);
      }
   }

   private static void drawPlane(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      float minX,
      float minY,
      float maxX,
      float maxY,
      int color,
      int alpha,
      int material,
      int light
   ) {
      if (budget.take(4)) {
         vertex(out, pose, minX, minY, 0.0F, color, alpha, material + 0.02F, 0.98F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, maxX, minY, 0.0F, color, alpha, material + 0.98F, 0.98F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, maxX, maxY, 0.0F, color, alpha, material + 0.98F, 0.02F, light, 0.0F, 0.0F, 1.0F);
         vertex(out, pose, minX, maxY, 0.0F, color, alpha, material + 0.02F, 0.02F, light, 0.0F, 0.0F, 1.0F);
      }
   }

   private static void drawDiamondOutline(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      float width,
      float height,
      float lineWidth,
      int color,
      int alpha,
      int material,
      int light
   ) {
      Vec3 top = new Vec3(0.0, height, 0.0);
      Vec3 right = new Vec3(width, 0.0, 0.0);
      Vec3 bottom = new Vec3(0.0, -height, 0.0);
      Vec3 left = new Vec3(-width, 0.0, 0.0);
      drawLine(out, pose, budget, top, right, lineWidth, color, alpha, material, light);
      drawLine(out, pose, budget, right, bottom, lineWidth, color, alpha, material, light);
      drawLine(out, pose, budget, bottom, left, lineWidth, color, alpha, material, light);
      drawLine(out, pose, budget, left, top, lineWidth, color, alpha, material, light);
   }

   private static void drawSharpBlade(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      Vec3 start,
      Vec3 end,
      float width,
      float bend,
      int color,
      int alpha,
      int material,
      int light,
      int segments
   ) {
      Vec3 delta = end.subtract(start);
      if (!(delta.lengthSqr() < 1.0E-6) && !(width <= 0.0F) && alpha > 0) {
         Vec3 direction = delta.normalize();
         Vec3 reference = Math.abs(direction.y) < 0.86 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
         Vec3 primary = direction.cross(reference);
         if (primary.lengthSqr() < 1.0E-6) {
            primary = new Vec3(0.0, 0.0, 1.0);
         }

         primary = primary.normalize();
         Vec3 secondary = direction.cross(primary).normalize();
         int boundedSegments = Mth.clamp(segments, 3, 16);
         drawTaperedSlashRibbon(
            out, pose, budget, start, end, primary, secondary.scale(width * bend * 2.25), width, color, alpha, material, light, boundedSegments
         );
         drawTaperedSlashRibbon(
            out, pose, budget, start, end, secondary, primary.scale(-width * bend * 0.72), width * 0.34F, color, alpha, material, light, boundedSegments
         );
      }
   }

   private static void drawTaperedSlashRibbon(
      VertexConsumer out,
      Pose pose,
      SungIlHwanVfxRenderer.FrameBudget budget,
      Vec3 start,
      Vec3 end,
      Vec3 side,
      Vec3 curve,
      float width,
      int color,
      int alpha,
      int material,
      int light,
      int segments
   ) {
      Vec3 normal = end.subtract(start).normalize().cross(side).normalize();

      for (int index = 0; index < segments; index++) {
         if (!budget.take(4)) {
            return;
         }

         float first = (float)index / segments;
         float second = (float)(index + 1) / segments;
         float firstProfile = (float)Math.pow(Math.max(0.0, Math.sin(Math.PI * first)), 0.62);
         float secondProfile = (float)Math.pow(Math.max(0.0, Math.sin(Math.PI * second)), 0.62);
         float firstWidth = width * firstProfile * (0.9F + 0.1F * Mth.sin(first * (float) (Math.PI * 2)));
         float secondWidth = width * secondProfile * (0.9F + 0.1F * Mth.sin(second * (float) (Math.PI * 2)));
         Vec3 firstCenter = start.lerp(end, first).add(curve.scale(Mth.sin(first * (float) Math.PI)));
         Vec3 secondCenter = start.lerp(end, second).add(curve.scale(Mth.sin(second * (float) Math.PI)));
         Vec3 a = firstCenter.subtract(side.scale(firstWidth));
         Vec3 b = firstCenter.add(side.scale(firstWidth));
         Vec3 c = secondCenter.add(side.scale(secondWidth));
         Vec3 d = secondCenter.subtract(side.scale(secondWidth));
         vertex(out, pose, (float)a.x, (float)a.y, (float)a.z, color, alpha, material + 0.02F, first, light, (float)normal.x, (float)normal.y, (float)normal.z);
         vertex(out, pose, (float)b.x, (float)b.y, (float)b.z, color, alpha, material + 0.98F, first, light, (float)normal.x, (float)normal.y, (float)normal.z);
         vertex(out, pose, (float)c.x, (float)c.y, (float)c.z, color, alpha, material + 0.98F, second, light, (float)normal.x, (float)normal.y, (float)normal.z);
         vertex(out, pose, (float)d.x, (float)d.y, (float)d.z, color, alpha, material + 0.02F, second, light, (float)normal.x, (float)normal.y, (float)normal.z);
      }
   }

   private static void drawCrossedLine(
      VertexConsumer out, Pose pose, SungIlHwanVfxRenderer.FrameBudget budget, Vec3 start, Vec3 end, float width, int color, int alpha, int material, int light
   ) {
      drawLine(out, pose, budget, start, end, width, color, alpha, material, light);
      Vec3 delta = end.subtract(start);
      if (!(delta.lengthSqr() < 1.0E-6)) {
         Vec3 direction = delta.normalize();
         Vec3 reference = Math.abs(direction.x) < 0.75 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 0.0, 1.0);
         Vec3 side = direction.cross(reference);
         if (!(side.lengthSqr() < 1.0E-5)) {
            drawLineRibbon(out, pose, budget, start, end, side.normalize().scale(width), color, alpha, material, light);
         }
      }
   }

   private static void drawLine(
      VertexConsumer out, Pose pose, SungIlHwanVfxRenderer.FrameBudget budget, Vec3 start, Vec3 end, float width, int color, int alpha, int material, int light
   ) {
      Vec3 delta = end.subtract(start);
      if (!(delta.lengthSqr() < 1.0E-6)) {
         Vec3 side = delta.normalize().cross(new Vec3(0.0, 1.0, 0.0));
         if (side.lengthSqr() < 1.0E-5) {
            side = new Vec3(1.0, 0.0, 0.0);
         }

         drawLineRibbon(out, pose, budget, start, end, side.normalize().scale(width), color, alpha, material, light);
      }
   }

   private static void drawLineRibbon(
      VertexConsumer out, Pose pose, SungIlHwanVfxRenderer.FrameBudget budget, Vec3 start, Vec3 end, Vec3 side, int color, int alpha, int material, int light
   ) {
      if (budget.take(4)) {
         Vec3 a = start.subtract(side);
         Vec3 b = start.add(side);
         Vec3 c = end.add(side);
         Vec3 d = end.subtract(side);
         vertex(out, pose, a, color, alpha, material + 0.02F, 0.02F, light);
         vertex(out, pose, b, color, alpha, material + 0.98F, 0.02F, light);
         vertex(out, pose, c, color, alpha, material + 0.98F, 0.98F, light);
         vertex(out, pose, d, color, alpha, material + 0.02F, 0.98F, light);
      }
   }

   private static void vertex(VertexConsumer out, Pose pose, Vec3 position, int color, int alpha, float u, float v, int light) {
      vertex(out, pose, (float)position.x, (float)position.y, (float)position.z, color, alpha, u, v, light, 0.0F, 1.0F, 0.0F);
   }

   private static void vertex(
      VertexConsumer out, Pose pose, float x, float y, float z, int color, int alpha, float u, float v, int light, float normalX, float normalY, float normalZ
   ) {
      out.vertex(pose.pose(), x, y, z)
         .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Mth.clamp(alpha, 0, 255))
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .normal(pose.normal(), normalX, normalY, normalZ)
         .endVertex();
   }

   private static float fadeInOut(float elapsed, int duration, float inTicks, float outTicks) {
      float fadeIn = Mth.clamp(elapsed / Math.max(1.0F, inTicks), 0.0F, 1.0F);
      float fadeOut = Mth.clamp((duration - elapsed) / Math.max(1.0F, outTicks), 0.0F, 1.0F);
      return fadeIn * fadeOut;
   }

   private static int alpha(float value) {
      return Mth.clamp(Math.round(value), 0, 255);
   }

   private record Anchors(Vec3 anchor, Vec3 focus, float yaw, float pitch, boolean firstPersonCaster) {
   }

   private static final class FrameBudget {
      private int remaining;

      private FrameBudget(int maximum) {
         this.remaining = Math.max(0, maximum);
      }

      private boolean take(int count) {
         if (count > 0 && this.remaining >= count) {
            this.remaining -= count;
            return true;
         } else {
            return false;
         }
      }
   }

   private enum Pass {
      SURFACE,
      EMISSIVE;
   }

   private enum Quality {
      FULL(88.0, 112, 22000, 32, 14, 7, 11, 8, 12, 8, 3, 10),
      REDUCED(56.0, 104, 8000, 16, 9, 4, 7, 5, 8, 5, 2, 6),
      MINIMAL(40.0, 100, 2800, 8, 6, 2, 4, 3, 5, 3, 1, 4);

      private final double renderDistance;
      private final int maxVisible;
      private final int maxVertices;
      private final int ringSegments;
      private final int sphereSegments;
      private final int stageOneWisps;
      private final int stageTwoWisps;
      private final int stageOneMotes;
      private final int stageTwoMotes;
      private final int chargeBlades;
      private final int afterimages;
      private final int releaseRays;
      private final int slashSegments;
      private final int fractureRays;
      private final int executionCuts;

      Quality(
         double renderDistance,
         int maxVisible,
         int maxVertices,
         int ringSegments,
         int sphereSegments,
         int stageOneWisps,
         int stageTwoWisps,
         int stageOneMotes,
         int stageTwoMotes,
         int chargeBlades,
         int afterimages,
         int releaseRays
      ) {
         this.renderDistance = renderDistance;
         this.maxVisible = maxVisible;
         this.maxVertices = maxVertices;
         this.ringSegments = ringSegments;
         this.sphereSegments = sphereSegments;
         this.stageOneWisps = stageOneWisps;
         this.stageTwoWisps = stageTwoWisps;
         this.stageOneMotes = stageOneMotes;
         this.stageTwoMotes = stageTwoMotes;
         this.chargeBlades = chargeBlades;
         this.afterimages = afterimages;
         this.releaseRays = releaseRays;
         this.slashSegments = Math.max(5, sphereSegments - 2);
         this.fractureRays = Math.max(4, releaseRays - 1);
         this.executionCuts = Math.max(8, releaseRays * 3);
      }

      private static SungIlHwanVfxRenderer.Quality current(Minecraft minecraft) {
         ParticleStatus status = minecraft.options.particles().get();
         if (status == ParticleStatus.MINIMAL) {
            return MINIMAL;
         } else {
            return status == ParticleStatus.DECREASED ? REDUCED : FULL;
         }
      }
   }

   private record RenderFrame(PoseStack stack, Vec3 camera, Quaternionf cameraOrientation, float partialTick, SungIlHwanVfxRenderer.Quality quality) {
   }

   private record VisibleEvent(
      SungIlHwanVfxClientState.ActiveEvent timeline,
      Vec3 anchor,
      Vec3 focus,
      float yaw,
      float pitch,
      float elapsed,
      double distanceSqr,
      int light,
      boolean firstPersonCaster
   ) {
   }
}
