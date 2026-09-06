package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
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
import dev.eness.sololevelingfinal.core.client.renderer.shader.AntaresVfxRenderTypes;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import dev.eness.sololevelingfinal.core.client.renderer.shader.IrisCompat;
import dev.eness.sololevelingfinal.core.network.AntaresVfxEventMessage;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class AntaresVfxRenderer {
   private static final int MATERIAL_TELEGRAPH = 0;
   private static final int MATERIAL_VOID = 1;
   private static final int MATERIAL_FLOW = 2;
   private static final int MATERIAL_HOT = 3;
   private static final int MATERIAL_FRACTURE = 4;
   private static final int MATERIAL_SMOKE = 5;
   private static final int MATERIAL_MEMBRANE = 6;
   private static final int MATERIAL_SIGIL = 7;
   private static final int OBSIDIAN = 591114;
   private static final int VOID_RED = 2687754;
   private static final int BLOOD_DARK = 5965585;
   private static final int CRIMSON = 12784424;
   private static final int DESTRUCTION = 15874098;
   private static final int EMBER = 16740417;
   private static final int HOT = 16765347;
   private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);

   private AntaresVfxRenderer() {
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      AntaresVfxClientState.clear();
   }

   @SubscribeEvent
   public static void onLevelUnload(Unload event) {
      if (event.getLevel() instanceof ClientLevel) {
         AntaresVfxClientState.clear();
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
                  List<AntaresVfxClientState.ActiveEvent> active = AntaresVfxClientState.snapshot(now);
                  if (!active.isEmpty()) {
                     DeferredWorldShaderRenderer.requestDepthAtStage(event, Stage.AFTER_PARTICLES);
                     if (renderStage) {
                        AntaresVfxRenderer.Quality quality = AntaresVfxRenderer.Quality.current(minecraft);
                        List<AntaresVfxRenderer.VisibleEvent> visible = collectVisible(event, minecraft, active, quality, now);
                        if (!visible.isEmpty()) {
                           PoseStack stack = DeferredWorldShaderRenderer.worldPoseStack(event);
                           BufferSource buffers = minecraft.renderBuffers().bufferSource();
                           AntaresVfxRenderer.RenderFrame frame = new AntaresVfxRenderer.RenderFrame(
                              stack,
                              event.getCamera().getPosition(),
                              minecraft.getEntityRenderDispatcher().cameraOrientation(),
                              event.getPartialTick(),
                              quality
                           );
                           AntaresVfxRenderer.FrameBudget budget = new AntaresVfxRenderer.FrameBudget(quality.maxVertices);
                           RenderType surfaceType = AntaresVfxRenderTypes.surface();
                           VertexConsumer surface = DeferredWorldShaderRenderer.buffer(buffers, surfaceType, true);

                           for (AntaresVfxRenderer.VisibleEvent visual : visible) {
                              render(frame, visual, surface, budget, AntaresVfxRenderer.Pass.SURFACE);
                           }

                           buffers.endBatch(surfaceType);
                           if (quality != AntaresVfxRenderer.Quality.MINIMAL && budget.remaining >= 4) {
                              RenderType emissiveType = AntaresVfxRenderTypes.emissive();
                              VertexConsumer emissive = DeferredWorldShaderRenderer.buffer(buffers, emissiveType, true);

                              for (AntaresVfxRenderer.VisibleEvent visual : visible) {
                                 render(frame, visual, emissive, budget, AntaresVfxRenderer.Pass.EMISSIVE);
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

   private static List<AntaresVfxRenderer.VisibleEvent> collectVisible(
      RenderLevelStageEvent event, Minecraft minecraft, List<AntaresVfxClientState.ActiveEvent> active, AntaresVfxRenderer.Quality quality, long now
   ) {
      Vec3 camera = event.getCamera().getPosition();
      float partialTick = event.getPartialTick();
      List<AntaresVfxRenderer.VisibleEvent> visible = new ArrayList<>(active.size());

      for (AntaresVfxClientState.ActiveEvent timeline : active) {
         AntaresVfxEventMessage message = timeline.message();
         float elapsed = timeline.elapsed(now, partialTick);
         if (!(elapsed < 0.0F) && !(elapsed >= message.duration) && (quality != AntaresVfxRenderer.Quality.MINIMAL || timeline.essential())) {
            AntaresVfxRenderer.Anchors anchors = resolveAnchors(minecraft, message, partialTick);
            AABB bounds = bounds(message, anchors);
            double distanceSqr = distanceToBoundsSqr(camera, bounds);
            if (!(distanceSqr > quality.renderDistance * quality.renderDistance) && event.getFrustum().isVisible(bounds)) {
               int light = LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(anchors.anchor));
               visible.add(
                  new AntaresVfxRenderer.VisibleEvent(
                     timeline, anchors.anchor, anchors.focus, anchors.yaw, anchors.pitch, elapsed, distanceSqr, light, anchors.firstPersonCaster
                  )
               );
            }
         }
      }

      visible.sort(
         Comparator.<AntaresVfxRenderer.VisibleEvent, Boolean>comparing(item -> !item.timeline.essential())
            .thenComparingDouble(item -> item.distanceSqr)
            .thenComparingLong(item -> item.timeline.sequence())
      );
      return visible.size() > quality.maxVisible ? new ArrayList<>(visible.subList(0, quality.maxVisible)) : visible;
   }

   private static AntaresVfxRenderer.Anchors resolveAnchors(Minecraft minecraft, AntaresVfxEventMessage message, float partialTick) {
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

      if (message.eventType == 9 && message.targetEntityId >= 0) {
         Entity target = minecraft.level.getEntity(message.targetEntityId);
         if (target != null && !target.isRemoved()) {
            focus = interpolated(target, partialTick).add(0.0, Math.max(0.8, target.getBbHeight() * 0.68), 0.0);
            anchor = focus;
         }
      }

      return new AntaresVfxRenderer.Anchors(anchor, focus, yaw, pitch, firstPerson);
   }

   private static boolean followsCaster(byte type) {
      return type == 2 || type == 5 || type == 7 || type == 13 || type == 14;
   }

   private static Vec3 interpolated(Entity entity, float partialTick) {
      return new Vec3(
         Mth.lerp(partialTick, entity.xo, entity.getX()), Mth.lerp(partialTick, entity.yo, entity.getY()), Mth.lerp(partialTick, entity.zo, entity.getZ())
      );
   }

   private static AABB bounds(AntaresVfxEventMessage message, AntaresVfxRenderer.Anchors anchors) {
      double padding = message.radius + 2.5;
      return new AABB(
         Math.min(anchors.anchor.x, anchors.focus.x) - padding,
         Math.min(anchors.anchor.y, anchors.focus.y) - padding,
         Math.min(anchors.anchor.z, anchors.focus.z) - padding,
         Math.max(anchors.anchor.x, anchors.focus.x) + padding,
         Math.max(anchors.anchor.y, anchors.focus.y) + padding + 2.5,
         Math.max(anchors.anchor.z, anchors.focus.z) + padding
      );
   }

   private static double distanceToBoundsSqr(Vec3 point, AABB bounds) {
      double dx = Math.max(Math.max(bounds.minX - point.x, 0.0), point.x - bounds.maxX);
      double dy = Math.max(Math.max(bounds.minY - point.y, 0.0), point.y - bounds.maxY);
      double dz = Math.max(Math.max(bounds.minZ - point.z, 0.0), point.z - bounds.maxZ);
      return dx * dx + dy * dy + dz * dz;
   }

   private static void render(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      if (budget.remaining >= 4) {
         frame.stack.pushPose();
         frame.stack.translate(visual.anchor.x - frame.camera.x, visual.anchor.y - frame.camera.y, visual.anchor.z - frame.camera.z);
         switch (visual.timeline.message().eventType) {
            case 1:
               renderClaw(frame, visual, out, budget, pass);
               break;
            case 2:
               renderBreathCharge(frame, visual, out, budget, pass);
               break;
            case 3:
               renderBreathStream(frame, visual, out, budget, pass, false);
               break;
            case 4:
               renderBreathEnd(frame, visual, out, budget, pass);
               break;
            case 5:
               renderDescentLaunch(frame, visual, out, budget, pass);
               break;
            case 6:
               renderDescentImpact(frame, visual, out, budget, pass);
               break;
            case 7:
               renderRoarCharge(frame, visual, out, budget, pass);
               break;
            case 8:
               renderRoarRelease(frame, visual, out, budget, pass);
               break;
            case 9:
               renderOverawedMark(frame, visual, out, budget, pass);
               break;
            case 10:
               renderExtinctionCharge(frame, visual, out, budget, pass);
               break;
            case 11:
               renderBreathStream(frame, visual, out, budget, pass, true);
               break;
            case 12:
               renderExtinctionAftermath(frame, visual, out, budget, pass);
               break;
            case 13:
            case 14:
               renderManifestationTransition(frame, visual, out, budget, pass);
         }

         frame.stack.popPose();
      }
   }

   private static void renderClaw(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      AntaresVfxEventMessage event = visual.timeline.message();
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = Mth.sin(progress * (float) Math.PI);
      float reveal = Mth.clamp(progress * 3.2F, 0.0F, 1.0F);
      boolean finisher = event.hasFlag(16);
      Vec3 end = visual.focus.subtract(visual.anchor);
      int count = finisher ? 4 : 3;

      for (int index = 0; index < count; index++) {
         float spread = (index - (count - 1) * 0.5F) * (finisher ? 0.48F : 0.34F);
         int color = pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : (index == count / 2 ? 16765347 : 15874098);
         int material = pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 3;
         drawCurvedSlash(
            frame,
            out,
            budget,
            end,
            spread,
            (finisher ? 0.17F : 0.11F) * (1.0F - progress * 0.35F),
            color,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 205.0F : 230.0F) * fade),
            material,
            visual.light,
            reveal,
            frame.quality.curveSegments
         );
      }

      if (finisher && progress > 0.18F) {
         float radius = event.radius * Mth.clamp((progress - 0.18F) * 2.2F, 0.0F, 1.0F);
         drawOrientedRing(
            out,
            frame.stack.last(),
            budget,
            end,
            safeDirection(end),
            radius,
            0.07F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 5965585 : 16740417,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 120.0F : 190.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 0 : 3,
            passLight(pass, visual.light),
            frame.quality.ringSegments
         );
      }
   }

   private static void renderBreathCharge(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      AntaresVfxEventMessage event = visual.timeline.message();
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = fadeInOut(visual.elapsed, event.duration, 2.0F, 2.5F);
      Vec3 direction = lookDirection(visual.yaw, visual.pitch);
      Vec3 mouth = new Vec3(0.0, 1.48, 0.0).add(direction.scale(0.55));

      for (int ring = 0; ring < 3; ring++) {
         float phase = Mth.clamp(progress * 1.35F - ring * 0.13F, 0.0F, 1.0F);
         float radius = (1.05F - phase * 0.78F) * (1.0F + ring * 0.12F);
         drawOrientedRing(
            out,
            frame.stack.last(),
            budget,
            mouth.add(direction.scale(0.35 + ring * 0.28)),
            direction,
            radius,
            0.045F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 5965585 : 12784424,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 105.0F : 185.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 0 : 2,
            passLight(pass, visual.light),
            frame.quality.ringSegments
         );
      }

      if (!visual.firstPersonCaster) {
         drawDragonHead(frame, out, budget, new Vec3(0.0, 1.35, 0.0), direction, 1.15F + progress * 0.28F, fade, pass, visual.light, event.seed);
      }

      drawBillboard(
         frame,
         out,
         budget,
         mouth,
         0.18F + progress * 0.12F,
         pass == AntaresVfxRenderer.Pass.SURFACE ? 2687754 : 16765347,
         alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 155.0F : 235.0F) * fade),
         pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 7,
         passLight(pass, visual.light)
      );
   }

   private static void renderBreathStream(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass,
      boolean extinction
   ) {
      AntaresVfxEventMessage event = visual.timeline.message();
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = fadeInOut(visual.elapsed, event.duration, 1.4F, extinction ? 6.0F : 3.0F);
      Vec3 end = visual.focus.subtract(visual.anchor);
      Vec3 direction = safeDirection(end);
      float pulse = extinction ? 1.0F + event.variant * 0.075F : 1.0F;
      float radius = event.radius * pulse * (0.82F + 0.12F * Mth.sin(visual.elapsed * 0.75F));
      int sides = extinction ? frame.quality.extinctionSides : frame.quality.beamSides;
      int segments = extinction ? frame.quality.extinctionSides + 6 : frame.quality.beamSides + 4;
      float breathTime = visual.elapsed;
      if (pass == AntaresVfxRenderer.Pass.SURFACE) {
         drawFlowTube(
            out,
            frame.stack.last(),
            budget,
            Vec3.ZERO,
            end,
            radius * 0.55F,
            radius * 1.35F,
            591114,
            alpha((extinction ? 220.0F : 188.0F) * fade),
            1,
            visual.light,
            sides,
            segments,
            1.15F,
            0.1F,
            breathTime
         );
         drawFlowTube(
            out,
            frame.stack.last(),
            budget,
            Vec3.ZERO.add(direction.scale(0.04)),
            end,
            radius * 0.44F,
            radius * 1.05F,
            5965585,
            alpha((extinction ? 178.0F : 145.0F) * fade),
            2,
            visual.light,
            sides,
            segments,
            -1.75F,
            0.16F,
            breathTime
         );
      } else {
         drawFlowTube(
            out,
            frame.stack.last(),
            budget,
            Vec3.ZERO.add(direction.scale(0.08)),
            end,
            radius * 0.26F,
            radius * 0.62F,
            extinction ? 16765347 : 15874098,
            alpha((extinction ? 245.0F : 220.0F) * fade),
            3,
            15728880,
            sides,
            segments,
            2.4F,
            0.07F,
            breathTime
         );
         drawFlowTube(
            out,
            frame.stack.last(),
            budget,
            Vec3.ZERO.add(direction.scale(0.06)),
            end,
            radius * 0.36F,
            radius * 0.9F,
            16740417,
            alpha((extinction ? 165.0F : 135.0F) * fade),
            2,
            15728880,
            sides,
            segments,
            -3.1F,
            0.22F,
            breathTime
         );
         drawCrossedLine(out, frame.stack.last(), budget, Vec3.ZERO, end, radius * (extinction ? 0.2F : 0.15F), 16765347, alpha(245.0F * fade), 3, 15728880);
      }

      drawOrientedRing(
         out,
         frame.stack.last(),
         budget,
         end,
         direction,
         radius * (1.04F + progress * 0.3F),
         radius * 0.08F,
         pass == AntaresVfxRenderer.Pass.SURFACE ? 5965585 : 16740417,
         alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 135.0F : 215.0F) * fade),
         pass == AntaresVfxRenderer.Pass.SURFACE ? 0 : 3,
         passLight(pass, visual.light),
         frame.quality.ringSegments
      );
      if (extinction && !visual.firstPersonCaster) {
         drawDragonHead(frame, out, budget, direction.scale(-1.6).add(0.0, 1.1, 0.0), direction, 2.35F, fade, pass, visual.light, event.seed);
      }
   }

   private static void renderBreathEnd(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = 1.0F - progress;
      Vec3 end = visual.focus.subtract(visual.anchor);
      if (pass == AntaresVfxRenderer.Pass.SURFACE) {
         drawTube(
            out, frame.stack.last(), budget, Vec3.ZERO, end, 0.75F * fade, 0.36F * fade, 5965585, alpha(92.0F * fade), 5, visual.light, frame.quality.beamSides
         );
      }

      int motes = pass == AntaresVfxRenderer.Pass.EMISSIVE ? frame.quality.motes : Math.max(2, frame.quality.motes / 2);
      Random random = new Random(visual.timeline.message().seed);

      for (int index = 0; index < motes; index++) {
         float along = random.nextFloat();
         Vec3 center = end.scale(along).add((random.nextDouble() - 0.5) * 0.7, progress * (0.4 + random.nextDouble()), (random.nextDouble() - 0.5) * 0.7);
         drawBillboard(
            frame,
            out,
            budget,
            center,
            0.12F + random.nextFloat() * 0.2F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 12784424,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 70.0F : 105.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 5 : 2,
            passLight(pass, visual.light)
         );
      }
   }

   private static void renderDescentLaunch(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      float fade = fadeInOut(visual.elapsed, visual.timeline.message().duration, 3.0F, 6.0F);
      float flap = Mth.sin(visual.elapsed * 0.31F) * 0.16F;
      if (!visual.firstPersonCaster) {
         drawWingPair(out, frame.stack.last(), budget, visual.yaw, 1.0F + flap, fade, pass, visual.light);
      }

      Vec3 direction = lookDirection(visual.yaw, visual.pitch);
      Vec3 trailEnd = direction.scale(-3.2).add(0.0, 1.15, 0.0);
      drawCrossedLine(
         out,
         frame.stack.last(),
         budget,
         new Vec3(0.0, 1.15, 0.0),
         trailEnd,
         pass == AntaresVfxRenderer.Pass.SURFACE ? 0.36F : 0.11F,
         pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 15874098,
         alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 115.0F : 190.0F) * fade),
         pass == AntaresVfxRenderer.Pass.SURFACE ? 5 : 3,
         passLight(pass, visual.light)
      );
   }

   private static void renderDescentImpact(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      AntaresVfxEventMessage event = visual.timeline.message();
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = 1.0F - Mth.clamp((progress - 0.62F) / 0.38F, 0.0F, 1.0F);
      float radius = event.radius * Mth.sqrt(progress);

      for (int ring = 0; ring < 2; ring++) {
         drawHorizontalRing(
            out,
            frame.stack.last(),
            budget,
            0.05 + ring * 0.07,
            radius * (1.0F - ring * 0.18F),
            0.08F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 5965585 : 15874098,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 125.0F : 210.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 0 : 3,
            passLight(pass, visual.light),
            frame.quality.ringSegments
         );
      }

      drawFractures(out, frame.stack.last(), budget, event.seed, radius, fade, pass, visual.light, frame.quality.fractures);
      int spires = Math.max(4, frame.quality.fractures / 2);

      for (int index = 0; index < spires; index++) {
         float angle = index * (float) (Math.PI * 2) / spires + event.seed * 0.013F;
         float distance = radius * (0.24F + 0.52F * (index % 3 / 2.0F));
         Vec3 base = new Vec3(Mth.cos(angle) * distance, 0.04, Mth.sin(angle) * distance);
         Vec3 tip = base.add(Mth.cos(angle) * 0.3, (1.1 + index % 3 * 0.5) * fade, Mth.sin(angle) * 0.3);
         drawCrossedLine(
            out,
            frame.stack.last(),
            budget,
            base,
            tip,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 0.12F : 0.045F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 16740417,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 145.0F : 215.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 3,
            passLight(pass, visual.light)
         );
      }
   }

   private static void renderRoarCharge(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = Mth.sin(progress * (float) Math.PI * 0.82F);
      Vec3 direction = lookDirection(visual.yaw, 0.0F);
      if (!visual.firstPersonCaster) {
         drawDragonHead(
            frame,
            out,
            budget,
            new Vec3(0.0, 1.05, 0.0).add(direction.scale(-0.72)),
            direction,
            1.7F + progress * 0.55F,
            fade,
            pass,
            visual.light,
            visual.timeline.message().seed
         );
      }

      for (int ring = 0; ring < 2; ring++) {
         drawHorizontalRing(
            out,
            frame.stack.last(),
            budget,
            0.08 + ring * 0.05,
            0.85F + progress * (1.1F + ring * 0.35F),
            0.045F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 5965585 : 12784424,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 95.0F : 155.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 0 : 2,
            passLight(pass, visual.light),
            frame.quality.ringSegments
         );
      }
   }

   private static void renderRoarRelease(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      AntaresVfxEventMessage event = visual.timeline.message();
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = 1.0F - Mth.clamp((progress - 0.72F) / 0.28F, 0.0F, 1.0F);
      float radius = event.radius * Mth.sqrt(progress);

      for (int layer = 0; layer < 3; layer++) {
         float layerProgress = Mth.clamp(progress * 1.25F - layer * 0.11F, 0.0F, 1.0F);
         drawHorizontalRing(
            out,
            frame.stack.last(),
            budget,
            0.22 + layer * 0.34,
            event.radius * Mth.sqrt(layerProgress),
            0.075F + layer * 0.02F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : (layer == 0 ? 16765347 : 12784424),
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 118.0F : 190.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 2,
            passLight(pass, visual.light),
            frame.quality.ringSegments
         );
      }

      if (pass == AntaresVfxRenderer.Pass.EMISSIVE) {
         int rays = frame.quality.motes;

         for (int index = 0; index < rays; index++) {
            float angle = index * (float) (Math.PI * 2) / rays + event.seed * 0.019F;
            Vec3 end = new Vec3(Mth.cos(angle) * radius, 0.45 + index % 3 * 0.22, Mth.sin(angle) * radius);
            drawCrossedLine(
               out, frame.stack.last(), budget, new Vec3(0.0, 1.0, 0.0), end, 0.035F, index % 4 == 0 ? 16765347 : 15874098, alpha(135.0F * fade), 3, 15728880
            );
         }
      }
   }

   private static void renderOverawedMark(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      float fade = fadeInOut(visual.elapsed, visual.timeline.message().duration, 4.0F, 8.0F);
      float pulse = 0.92F + Mth.sin(visual.elapsed * 0.42F) * 0.08F;
      drawBillboard(
         frame,
         out,
         budget,
         new Vec3(0.0, 0.55, 0.0),
         0.32F * pulse,
         pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 15874098,
         alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 185.0F : 220.0F) * fade),
         pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 7,
         passLight(pass, visual.light)
      );

      for (int side = -1; side <= 1; side += 2) {
         drawCrossedLine(
            out,
            frame.stack.last(),
            budget,
            new Vec3(side * 0.08, 0.2, 0.0),
            new Vec3(side * 0.24, 0.95, 0.0),
            0.025F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 5965585 : 16740417,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 130.0F : 190.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 0 : 3,
            passLight(pass, visual.light)
         );
      }
   }

   private static void renderExtinctionCharge(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      AntaresVfxEventMessage event = visual.timeline.message();
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = fadeInOut(visual.elapsed, event.duration, 3.0F, 2.0F);
      Vec3 end = visual.focus.subtract(visual.anchor);
      Vec3 direction = safeDirection(end);
      if (pass == AntaresVfxRenderer.Pass.SURFACE) {
         drawTube(
            out,
            frame.stack.last(),
            budget,
            Vec3.ZERO,
            end,
            event.radius * (0.74F - progress * 0.25F),
            event.radius * (0.54F - progress * 0.18F),
            5965585,
            alpha(82.0F * fade),
            0,
            visual.light,
            frame.quality.beamSides
         );
      }

      for (int ring = 0; ring < 4; ring++) {
         float along = 0.08F + ring * 0.18F;
         float radius = event.radius * (1.25F - progress * 0.72F) * (1.0F - ring * 0.08F);
         drawOrientedRing(
            out,
            frame.stack.last(),
            budget,
            end.scale(along),
            direction,
            radius,
            0.055F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : (ring == 0 ? 16765347 : 12784424),
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 132.0F : 208.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 3,
            passLight(pass, visual.light),
            frame.quality.ringSegments
         );
      }

      if (!visual.firstPersonCaster) {
         drawDragonHead(frame, out, budget, direction.scale(-2.0).add(0.0, 1.2, 0.0), direction, 2.0F + progress * 1.05F, fade, pass, visual.light, event.seed);
      }
   }

   private static void renderExtinctionAftermath(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      AntaresVfxEventMessage event = visual.timeline.message();
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float fade = 1.0F - progress;
      Vec3 end = visual.focus.subtract(visual.anchor);
      Vec3 direction = safeDirection(end);
      Vec3 side = basisRight(direction);
      int scars = frame.quality.aftermathScars;
      Random random = new Random(event.seed ^ 1779033703);

      for (int index = 0; index < scars; index++) {
         float along = (index + 0.5F) / scars;
         Vec3 center = end.scale(along).add(side.scale((random.nextDouble() - 0.5) * event.radius * 1.4));
         Vec3 scarEnd = center.add(direction.scale(0.7 + random.nextDouble() * 1.8)).add(side.scale((random.nextDouble() - 0.5) * 0.8));
         drawLineRibbon(
            out,
            frame.stack.last(),
            budget,
            center.add(0.0, 0.035, 0.0),
            scarEnd.add(0.0, 0.035, 0.0),
            side.scale(0.08),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 12784424,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 120.0F : 150.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 4 : 3,
            passLight(pass, visual.light)
         );
         if (index % 2 == 0) {
            drawBillboard(
               frame,
               out,
               budget,
               center.add(0.0, 0.3 + progress * 0.9, 0.0),
               0.35F + progress * 0.35F,
               pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 5965585,
               alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 75.0F : 55.0F) * fade),
               5,
               passLight(pass, visual.light)
            );
         }
      }
   }

   private static void renderManifestationTransition(
      AntaresVfxRenderer.RenderFrame frame,
      AntaresVfxRenderer.VisibleEvent visual,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      AntaresVfxRenderer.Pass pass
   ) {
      boolean starting = visual.timeline.message().eventType == 13;
      float progress = visual.timeline.progress(gameTime(), frame.partialTick);
      float envelope = starting ? smoothOut(progress) : 1.0F - progress;
      float fade = starting ? fadeInOut(visual.elapsed, visual.timeline.message().duration, 3.0F, 5.0F) : 1.0F - progress;
      if (!visual.firstPersonCaster) {
         drawWingPair(out, frame.stack.last(), budget, visual.yaw, envelope, fade, pass, visual.light);
      }

      for (int ring = 0; ring < 3; ring++) {
         float radius = (0.8F + ring * 0.56F) * (starting ? 0.45F + envelope * 1.2F : 1.0F + progress);
         drawHorizontalRing(
            out,
            frame.stack.last(),
            budget,
            0.05 + ring * 0.13,
            radius,
            0.06F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : (ring == 0 ? 16765347 : 12784424),
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 140.0F : 205.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 2,
            passLight(pass, visual.light),
            frame.quality.ringSegments
         );
      }
   }

   private static void drawCurvedSlash(
      AntaresVfxRenderer.RenderFrame frame,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      Vec3 end,
      float spread,
      float width,
      int color,
      int alpha,
      int material,
      int light,
      float reveal,
      int segments
   ) {
      Vec3 direction = safeDirection(end);
      Vec3 side = basisRight(direction);
      Vec3 lift = safeDirection(side.cross(direction));
      Vec3 previous = side.scale(spread * 0.55).add(lift.scale(-0.18));
      int count = Math.max(3, segments);

      for (int segment = 1; segment <= count; segment++) {
         float t = (float)segment / count;
         if (t > reveal) {
            break;
         }

         float curve = Mth.sin(t * (float) Math.PI);
         Vec3 point = end.scale(t).add(side.scale(spread * (0.55 - t) + curve * spread * 0.52)).add(lift.scale(curve * (0.36 + Math.abs(spread) * 0.3) - 0.18));
         float taper = Mth.sin((float) Math.PI * Mth.clamp(t, 0.03F, 0.97F));
         drawLineRibbon(out, frame.stack.last(), budget, previous, point, side.scale(Math.max(0.012, width * taper)), color, alpha, material, light);
         previous = point;
      }
   }

   private static void drawDragonHead(
      AntaresVfxRenderer.RenderFrame frame,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
      Vec3 center,
      Vec3 forward,
      float scale,
      float fade,
      AntaresVfxRenderer.Pass pass,
      int light,
      int seed
   ) {
      Vec3 direction = safeDirection(forward);
      Vec3 right = basisRight(direction);
      Vec3 up = safeDirection(right.cross(direction));
      Vec3 back = center.add(direction.scale(-0.62 * scale));
      Vec3 brow = center.add(up.scale(0.24 * scale));
      Vec3 snout = center.add(direction.scale(0.72 * scale)).add(up.scale(-0.08 * scale));
      int color = pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 12784424;
      int material = pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 2;
      int alpha = alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 170.0F : 185.0F) * fade);
      drawQuad(
         out,
         frame.stack.last(),
         budget,
         back.add(right.scale(-0.48 * scale)),
         back.add(right.scale(0.48 * scale)),
         snout.add(right.scale(0.19 * scale)),
         snout.add(right.scale(-0.19 * scale)),
         color,
         alpha,
         material,
         passLight(pass, light)
      );
      drawQuad(
         out,
         frame.stack.last(),
         budget,
         back.add(up.scale(-0.35 * scale)),
         brow.add(up.scale(0.36 * scale)),
         snout.add(up.scale(0.09 * scale)),
         snout.add(up.scale(-0.2 * scale)),
         color,
         alpha,
         material,
         passLight(pass, light)
      );

      for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
         Vec3 hornRoot = brow.add(right.scale(sideSign * 0.32 * scale));
         Vec3 hornTip = back.add(right.scale(sideSign * 0.9 * scale)).add(up.scale(0.72 * scale));
         drawCrossedLine(
            out,
            frame.stack.last(),
            budget,
            hornRoot,
            hornTip,
            0.045F * scale,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 2687754 : 16740417,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 195.0F : 225.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 3,
            passLight(pass, light)
         );
         Vec3 eye = center.add(direction.scale(0.16 * scale)).add(right.scale(sideSign * 0.27 * scale)).add(up.scale(0.14 * scale));
         drawBillboard(
            frame,
            out,
            budget,
            eye,
            0.085F * scale,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 5965585 : 16765347,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 170.0F : 245.0F) * fade),
            7,
            passLight(pass, light)
         );
      }
   }

   public static void drawWingPair(
      VertexConsumer out, Pose pose, AntaresVfxRenderer.FrameBudget budget, float yaw, float spread, float fade, AntaresVfxRenderer.Pass pass, int light
   ) {
      float radians = yaw * (float) (Math.PI / 180.0);
      Vec3 forward = new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
      Vec3 right = new Vec3(forward.z, 0.0, -forward.x);
      Vec3 back = forward.scale(-1.0);

      for (int sign = -1; sign <= 1; sign += 2) {
         Vec3 root = new Vec3(0.0, 1.48, 0.0).add(right.scale(sign * 0.18));
         Vec3 elbow = root.add(right.scale(sign * 1.25 * spread)).add(0.0, 0.78 * spread, 0.0).add(back.scale(0.44));
         Vec3 tip = root.add(right.scale(sign * 3.15 * spread)).add(0.0, 0.32 * spread, 0.0).add(back.scale(1.06));
         Vec3 lower = root.add(right.scale(sign * 1.62 * spread)).add(0.0, -1.0 * spread, 0.0).add(back.scale(0.82));
         int membraneColor = pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 5965585;
         drawQuad(
            out,
            pose,
            budget,
            root,
            elbow,
            tip,
            lower,
            membraneColor,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 188.0F : 105.0F) * fade),
            6,
            passLight(pass, light)
         );
         int boneColor = pass == AntaresVfxRenderer.Pass.SURFACE ? 2687754 : 15874098;

         for (Vec3 destination : new Vec3[]{elbow, tip, lower}) {
            drawCrossedLine(
               out,
               pose,
               budget,
               root,
               destination,
               pass == AntaresVfxRenderer.Pass.SURFACE ? 0.055F : 0.026F,
               boneColor,
               alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 220.0F : 205.0F) * fade),
               pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 3,
               passLight(pass, light)
            );
         }
      }

      Vec3 crown = new Vec3(0.0, 1.78, 0.0);

      for (int sign = -1; sign <= 1; sign += 2) {
         Vec3 root = crown.add(right.scale(sign * 0.13));
         Vec3 tip = crown.add(right.scale(sign * 0.48 * spread)).add(0.0, 0.62 * spread, 0.0).add(back.scale(0.35));
         drawCrossedLine(
            out,
            pose,
            budget,
            root,
            tip,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 0.045F : 0.021F,
            pass == AntaresVfxRenderer.Pass.SURFACE ? 2687754 : 16740417,
            alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 225.0F : 215.0F) * fade),
            pass == AntaresVfxRenderer.Pass.SURFACE ? 1 : 3,
            passLight(pass, light)
         );
      }
   }

   private static void drawFractures(
      VertexConsumer out,
      Pose pose,
      AntaresVfxRenderer.FrameBudget budget,
      int seed,
      float radius,
      float fade,
      AntaresVfxRenderer.Pass pass,
      int light,
      int count
   ) {
      Random random = new Random(seed ^ -1150833019);

      for (int index = 0; index < count; index++) {
         float angle = random.nextFloat() * (float) (Math.PI * 2);
         float reach = radius * (0.42F + random.nextFloat() * 0.58F);
         Vec3 side = new Vec3(-Mth.sin(angle), 0.0, Mth.cos(angle));
         Vec3 start = new Vec3(Mth.cos(angle) * 0.2, 0.035, Mth.sin(angle) * 0.2);
         Vec3 middle = new Vec3(Mth.cos(angle) * reach * 0.55, 0.038, Mth.sin(angle) * reach * 0.55)
            .add(side.scale((random.nextDouble() - 0.5) * reach * 0.22));
         Vec3 end = new Vec3(Mth.cos(angle) * reach, 0.04, Mth.sin(angle) * reach).add(side.scale((random.nextDouble() - 0.5) * reach * 0.14));
         int color = pass == AntaresVfxRenderer.Pass.SURFACE ? 591114 : 15874098;
         int material = pass == AntaresVfxRenderer.Pass.SURFACE ? 4 : 3;
         int alpha = alpha((pass == AntaresVfxRenderer.Pass.SURFACE ? 175.0F : 210.0F) * fade);
         drawLineRibbon(out, pose, budget, start, middle, side.scale(0.07), color, alpha, material, passLight(pass, light));
         drawLineRibbon(out, pose, budget, middle, end, side.scale(0.045), color, alpha, material, passLight(pass, light));
      }
   }

   private static void drawTube(
      VertexConsumer out,
      Pose pose,
      AntaresVfxRenderer.FrameBudget budget,
      Vec3 start,
      Vec3 end,
      float startRadius,
      float endRadius,
      int color,
      int alpha,
      int material,
      int light,
      int sides
   ) {
      Vec3 axis = safeDirection(end.subtract(start));
      Vec3 right = basisRight(axis);
      Vec3 up = safeDirection(right.cross(axis));
      int count = Math.max(4, sides);

      for (int side = 0; side < count; side++) {
         float a0 = side * (float) (Math.PI * 2) / count;
         float a1 = (side + 1) * (float) (Math.PI * 2) / count;
         Vec3 r0 = right.scale(Mth.cos(a0)).add(up.scale(Mth.sin(a0)));
         Vec3 r1 = right.scale(Mth.cos(a1)).add(up.scale(Mth.sin(a1)));
         drawQuad(
            out,
            pose,
            budget,
            start.add(r0.scale(startRadius)),
            start.add(r1.scale(startRadius)),
            end.add(r1.scale(endRadius)),
            end.add(r0.scale(endRadius)),
            color,
            alpha,
            material,
            light
         );
      }
   }

   private static void drawFlowTube(
      VertexConsumer out,
      Pose pose,
      AntaresVfxRenderer.FrameBudget budget,
      Vec3 start,
      Vec3 end,
      float startRadius,
      float endRadius,
      int color,
      int alpha,
      int material,
      int light,
      int sides,
      int segments,
      float swirl,
      float turbulence,
      float time
   ) {
      Vec3 axis = safeDirection(end.subtract(start));
      Vec3 right = basisRight(axis);
      Vec3 up = safeDirection(right.cross(axis));
      int ringCount = Math.max(2, segments);
      int sideCount = Math.max(4, sides);
      Vec3 span = end.subtract(start);
      Vec3[] centers = new Vec3[ringCount + 1];
      float[] radii = new float[ringCount + 1];
      float[] twists = new float[ringCount + 1];

      for (int ring = 0; ring <= ringCount; ring++) {
         float t = (float)ring / ringCount;
         float eased = t * t * (3.0F - 2.0F * t);
         float radius = startRadius + (endRadius - startRadius) * eased;
         radius *= 1.0F + 0.16F * Mth.sin(t * 7.3F + time * 2.1F);
         float wobble = turbulence * radius;
         centers[ring] = start.add(span.scale(t))
            .add(right.scale(Mth.sin(t * 5.1F + time * 1.7F) * wobble))
            .add(up.scale(Mth.cos(t * 4.3F + time * 1.9F) * wobble));
         radii[ring] = radius;
         twists[ring] = swirl * t + time * 0.6F;
      }

      for (int side = 0; side < sideCount; side++) {
         float base0 = side * (float) (Math.PI * 2) / sideCount;
         float base1 = (side + 1) * (float) (Math.PI * 2) / sideCount;

         for (int ring = 0; ring < ringCount; ring++) {
            float v0 = (float)ring / ringCount;
            float v1 = (float)(ring + 1) / ringCount;
            drawQuadUv(
               out,
               pose,
               budget,
               centers[ring].add(radial(right, up, base0 + twists[ring], radii[ring])),
               centers[ring].add(radial(right, up, base1 + twists[ring], radii[ring])),
               centers[ring + 1].add(radial(right, up, base1 + twists[ring + 1], radii[ring + 1])),
               centers[ring + 1].add(radial(right, up, base0 + twists[ring + 1], radii[ring + 1])),
               color,
               alpha,
               material,
               light,
               v0,
               v1
            );
         }
      }
   }

   private static Vec3 radial(Vec3 right, Vec3 up, float angle, float radius) {
      return right.scale(Mth.cos(angle) * radius).add(up.scale(Mth.sin(angle) * radius));
   }

   private static void drawHorizontalRing(
      VertexConsumer out,
      Pose pose,
      AntaresVfxRenderer.FrameBudget budget,
      double y,
      float radius,
      float width,
      int color,
      int alpha,
      int material,
      int light,
      int segments
   ) {
      drawOrientedRing(out, pose, budget, new Vec3(0.0, y, 0.0), UP, radius, width, color, alpha, material, light, segments);
   }

   private static void drawOrientedRing(
      VertexConsumer out,
      Pose pose,
      AntaresVfxRenderer.FrameBudget budget,
      Vec3 center,
      Vec3 normal,
      float radius,
      float width,
      int color,
      int alpha,
      int material,
      int light,
      int segments
   ) {
      Vec3 axis = safeDirection(normal);
      Vec3 right = basisRight(axis);
      Vec3 up = safeDirection(right.cross(axis));
      float inner = Math.max(0.01F, radius - width);
      float outer = radius + width;
      int count = Math.max(6, segments);

      for (int segment = 0; segment < count; segment++) {
         float a0 = segment * (float) (Math.PI * 2) / count;
         float a1 = (segment + 1) * (float) (Math.PI * 2) / count;
         Vec3 d0 = right.scale(Mth.cos(a0)).add(up.scale(Mth.sin(a0)));
         Vec3 d1 = right.scale(Mth.cos(a1)).add(up.scale(Mth.sin(a1)));
         drawQuad(
            out,
            pose,
            budget,
            center.add(d0.scale(inner)),
            center.add(d0.scale(outer)),
            center.add(d1.scale(outer)),
            center.add(d1.scale(inner)),
            color,
            alpha,
            material,
            light
         );
      }
   }

   private static void drawCrossedLine(
      VertexConsumer out, Pose pose, AntaresVfxRenderer.FrameBudget budget, Vec3 start, Vec3 end, float width, int color, int alpha, int material, int light
   ) {
      Vec3 axis = safeDirection(end.subtract(start));
      Vec3 side = basisRight(axis).scale(width);
      Vec3 second = safeDirection(axis.cross(side)).scale(width);
      drawLineRibbon(out, pose, budget, start, end, side, color, alpha, material, light);
      drawLineRibbon(out, pose, budget, start, end, second, color, alpha, material, light);
   }

   private static void drawLineRibbon(
      VertexConsumer out, Pose pose, AntaresVfxRenderer.FrameBudget budget, Vec3 start, Vec3 end, Vec3 side, int color, int alpha, int material, int light
   ) {
      drawQuad(out, pose, budget, start.subtract(side), start.add(side), end.add(side), end.subtract(side), color, alpha, material, light);
   }

   private static void drawBillboard(
      AntaresVfxRenderer.RenderFrame frame,
      VertexConsumer out,
      AntaresVfxRenderer.FrameBudget budget,
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
      drawQuad(
         out,
         frame.stack.last(),
         budget,
         new Vec3(-size, -size, 0.0),
         new Vec3(size, -size, 0.0),
         new Vec3(size, size, 0.0),
         new Vec3(-size, size, 0.0),
         color,
         alpha,
         material,
         light
      );
      frame.stack.popPose();
   }

   private static void drawQuad(
      VertexConsumer out, Pose pose, AntaresVfxRenderer.FrameBudget budget, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color, int alpha, int material, int light
   ) {
      if (budget.take(4)) {
         Vec3 normal = b.subtract(a).cross(c.subtract(a));
         if (normal.lengthSqr() < 1.0E-8) {
            normal = UP;
         } else {
            normal = normal.normalize();
         }

         vertex(out, pose, a, color, alpha, material + 0.01F, 0.01F, light, normal);
         vertex(out, pose, b, color, alpha, material + 0.99F, 0.01F, light, normal);
         vertex(out, pose, c, color, alpha, material + 0.99F, 0.99F, light, normal);
         vertex(out, pose, d, color, alpha, material + 0.01F, 0.99F, light, normal);
      }
   }

   private static void drawQuadUv(
      VertexConsumer out,
      Pose pose,
      AntaresVfxRenderer.FrameBudget budget,
      Vec3 a,
      Vec3 b,
      Vec3 c,
      Vec3 d,
      int color,
      int alpha,
      int material,
      int light,
      float v0,
      float v1
   ) {
      if (budget.take(4)) {
         Vec3 normal = b.subtract(a).cross(c.subtract(a));
         if (normal.lengthSqr() < 1.0E-8) {
            normal = UP;
         } else {
            normal = normal.normalize();
         }

         float low = Mth.clamp(v0, 0.01F, 0.99F);
         float high = Mth.clamp(v1, 0.01F, 0.99F);
         vertex(out, pose, a, color, alpha, material + 0.01F, low, light, normal);
         vertex(out, pose, b, color, alpha, material + 0.99F, low, light, normal);
         vertex(out, pose, c, color, alpha, material + 0.99F, high, light, normal);
         vertex(out, pose, d, color, alpha, material + 0.01F, high, light, normal);
      }
   }

   private static void vertex(VertexConsumer out, Pose pose, Vec3 position, int color, int alpha, float u, float v, int light, Vec3 normal) {
      out.vertex(pose.pose(), (float)position.x, (float)position.y, (float)position.z)
         .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Mth.clamp(alpha, 0, 255))
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(light)
         .normal(pose.normal(), (float)normal.x, (float)normal.y, (float)normal.z)
         .endVertex();
   }

   private static Vec3 basisRight(Vec3 direction) {
      Vec3 right = direction.cross(UP);
      if (right.lengthSqr() < 1.0E-6) {
         right = direction.cross(new Vec3(1.0, 0.0, 0.0));
      }

      return safeDirection(right);
   }

   private static Vec3 safeDirection(Vec3 value) {
      return value.lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : value.normalize();
   }

   private static Vec3 lookDirection(float yaw, float pitch) {
      return Vec3.directionFromRotation(pitch, yaw);
   }

   private static long gameTime() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.level == null ? 0L : minecraft.level.getGameTime();
   }

   private static int passLight(AntaresVfxRenderer.Pass pass, int light) {
      return pass == AntaresVfxRenderer.Pass.EMISSIVE ? 15728880 : light;
   }

   private static float fadeInOut(float elapsed, int duration, float inTicks, float outTicks) {
      return Mth.clamp(elapsed / Math.max(1.0F, inTicks), 0.0F, 1.0F) * Mth.clamp((duration - elapsed) / Math.max(1.0F, outTicks), 0.0F, 1.0F);
   }

   private static float smoothOut(float value) {
      float clamped = Mth.clamp(value, 0.0F, 1.0F);
      return 1.0F - (1.0F - clamped) * (1.0F - clamped);
   }

   private static int alpha(float value) {
      return Mth.clamp(Math.round(value), 0, 255);
   }

   private record Anchors(Vec3 anchor, Vec3 focus, float yaw, float pitch, boolean firstPersonCaster) {
   }

   public static final class FrameBudget {
      private int remaining;

      public FrameBudget(int maximum) {
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

   public enum Pass {
      SURFACE,
      EMISSIVE;
   }

   private enum Quality {
      FULL(104.0, 72, 30000, 24, 12, 16, 12, 12, 10, 16),
      REDUCED(72.0, 48, 13000, 16, 8, 11, 8, 8, 7, 10),
      MINIMAL(44.0, 28, 4200, 10, 6, 7, 5, 5, 4, 6);

      private final double renderDistance;
      private final int maxVisible;
      private final int maxVertices;
      private final int ringSegments;
      private final int beamSides;
      private final int extinctionSides;
      private final int curveSegments;
      private final int motes;
      private final int fractures;
      private final int aftermathScars;

      Quality(
         double renderDistance,
         int maxVisible,
         int maxVertices,
         int ringSegments,
         int beamSides,
         int extinctionSides,
         int curveSegments,
         int motes,
         int fractures,
         int aftermathScars
      ) {
         this.renderDistance = renderDistance;
         this.maxVisible = maxVisible;
         this.maxVertices = maxVertices;
         this.ringSegments = ringSegments;
         this.beamSides = beamSides;
         this.extinctionSides = extinctionSides;
         this.curveSegments = curveSegments;
         this.motes = motes;
         this.fractures = fractures;
         this.aftermathScars = aftermathScars;
      }

      private static AntaresVfxRenderer.Quality current(Minecraft minecraft) {
         ParticleStatus status = minecraft.options.particles().get();
         if (status == ParticleStatus.MINIMAL) {
            return MINIMAL;
         } else {
            return status == ParticleStatus.DECREASED ? REDUCED : FULL;
         }
      }
   }

   private record RenderFrame(PoseStack stack, Vec3 camera, Quaternionf cameraOrientation, float partialTick, AntaresVfxRenderer.Quality quality) {
   }

   private record VisibleEvent(
      AntaresVfxClientState.ActiveEvent timeline,
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
