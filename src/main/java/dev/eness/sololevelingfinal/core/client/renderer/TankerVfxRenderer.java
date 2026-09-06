package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import dev.eness.sololevelingfinal.core.client.renderer.shader.TankerVfxRenderTypes;
import dev.eness.sololevelingfinal.core.network.TankerVfxEventMessage;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class TankerVfxRenderer {
   public static final int MAX_QUEUED_EVENTS = 96;
   public static final int FULL_MAX_VISIBLE_EVENTS = 32;
   public static final int LOW_MAX_VISIBLE_EVENTS = 16;
   public static final int OFF_MAX_VISIBLE_EVENTS = 8;
   public static final int FULL_MAX_VERTICES = 24000;
   public static final int LOW_MAX_VERTICES = 8000;
   public static final int OFF_MAX_VERTICES = 2000;
   public static final int FULL_RING_SEGMENTS = 32;
   public static final int LOW_RING_SEGMENTS = 12;
   public static final int OFF_RING_SEGMENTS = 8;
   public static final int FULL_MAX_BURST_PARTICLES = 24;
   public static final int LOW_MAX_BURST_PARTICLES = 8;
   public static final int OFF_MAX_BURST_PARTICLES = 0;
   public static final int FULL_MAX_PERSISTENT_PARTICLES_PER_TICK = 2;
   public static final int FULL_MAX_PERSISTENT_PARTICLES_PER_EVENT = 96;
   private static final int MATERIAL_STEEL = 0;
   private static final int MATERIAL_GOLD = 1;
   private static final int MATERIAL_EMBER = 2;
   private static final int MATERIAL_CRACK = 3;
   private static final int MATERIAL_DUST = 4;
   private static final int STEEL_DARK = 2435886;
   private static final int STEEL_MID = 4608344;
   private static final int STEEL_EDGE = 6713464;
   private static final int GOLD_PALE = 15191426;
   private static final int GOLD_HOT = 16770209;
   private static final int EMBER = 15166509;
   private static final int EMBER_HOT = 16756826;
   private static final int DUST = 12102289;
   private static final List<TankerVfxRenderer.ActiveEvent> EVENTS = new ArrayList<>(96);
   private static long nextSequence;

   private TankerVfxRenderer() {
   }

   public static void enqueue(TankerVfxEventMessage message) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.isSameThread()) {
         minecraft.execute(() -> enqueue(message));
      } else if (minecraft.level != null) {
         long now = minecraft.level.getGameTime();
         pruneInvalidAndExpired(now);
         if (TankerVfxEventMessage.isKnownEventType(message.eventType)) {
            if (now - message.serverStartTick < message.duration) {
               if (isDuplicate(message)) {
                  if (message.eventType == 1) {
                     removeTypes(message.ownerEntityId, 0);
                  }
               } else {
                  applyStateTransition(message);
                  TankerVfxRenderer.ActiveEvent incoming = new TankerVfxRenderer.ActiveEvent(message, nextSequence++, now);
                  if (EVENTS.size() < 96 || evictFor(incoming, minecraft)) {
                     EVENTS.add(incoming);
                     trimQueueToLimit();
                     double elapsed = Math.max(0.0, now - message.serverStartTick);
                     if (elapsed <= 6.0 && !message.hasFlag(8) && !message.hasFlag(16)) {
                        playInitialSound(minecraft, message);
                     }
                  }
               }
            }
         }
      }
   }

   public static void clear() {
      EVENTS.clear();
      nextSequence = 0L;
   }

   public static void onResourceReload() {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.isSameThread()) {
         minecraft.execute(TankerVfxRenderer::onResourceReload);
      } else if (minecraft.level == null) {
         clear();
      } else {
         pruneInvalidAndExpired(minecraft.level.getGameTime());
      }
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      clear();
   }

   @SubscribeEvent
   public static void onLevelUnload(Unload event) {
      if (event.getLevel() instanceof ClientLevel) {
         clear();
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES || event.getStage() == Stage.AFTER_LEVEL) {
         if (!IrisCompat.isRenderingShadowPass()) {
            boolean renderStage = DeferredWorldShaderRenderer.isRenderStage(event, Stage.AFTER_PARTICLES);
            if (renderStage || event.getStage() == Stage.AFTER_PARTICLES) {
               Minecraft minecraft = Minecraft.getInstance();
               if (minecraft.level != null && minecraft.player != null && !EVENTS.isEmpty()) {
                  long now = minecraft.level.getGameTime();
                  pruneInvalidAndExpired(now);
                  if (!EVENTS.isEmpty()) {
                     DeferredWorldShaderRenderer.requestDepthAtStage(event, Stage.AFTER_PARTICLES);
                     if (renderStage) {
                        TankerVfxRenderer.Quality quality = TankerVfxRenderer.Quality.current(minecraft);
                        List<TankerVfxRenderer.VisibleEvent> visible = collectVisible(event, minecraft, quality, now);
                        if (!visible.isEmpty()) {
                           playAmbientCues(minecraft, visible, quality);
                           PoseStack poseStack = DeferredWorldShaderRenderer.worldPoseStack(event);
                           BufferSource buffers = minecraft.renderBuffers().bufferSource();
                           TankerVfxRenderer.RenderFrame frame = new TankerVfxRenderer.RenderFrame(
                              poseStack,
                              event.getCamera().getPosition(),
                              minecraft.getEntityRenderDispatcher().cameraOrientation(),
                              event.getPartialTick(),
                              quality
                           );
                           TankerVfxRenderer.FrameBudget budget = new TankerVfxRenderer.FrameBudget(quality.maxVertices);
                           RenderType surfaceType = TankerVfxRenderTypes.surface();
                           VertexConsumer surface = DeferredWorldShaderRenderer.buffer(buffers, surfaceType, true);

                           for (TankerVfxRenderer.VisibleEvent visual : visible) {
                              renderEvent(frame, visual, surface, budget, TankerVfxRenderer.Pass.SURFACE);
                           }

                           buffers.endBatch(surfaceType);
                           boolean hasGlow = quality != TankerVfxRenderer.Quality.OFF
                              && visible.stream().anyMatch(visualx -> quality.allowsGlow(visualx.event));
                           if (hasGlow && budget.remaining() >= 4) {
                              RenderType emissiveType = TankerVfxRenderTypes.emissive();
                              VertexConsumer emissive = DeferredWorldShaderRenderer.buffer(buffers, emissiveType, true);

                              for (TankerVfxRenderer.VisibleEvent visual : visible) {
                                 if (quality.allowsGlow(visual.event)) {
                                    renderEvent(frame, visual, emissive, budget, TankerVfxRenderer.Pass.EMISSIVE);
                                 }
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

   private static List<TankerVfxRenderer.VisibleEvent> collectVisible(
      RenderLevelStageEvent renderEvent, Minecraft minecraft, TankerVfxRenderer.Quality quality, long now
   ) {
      Vec3 camera = renderEvent.getCamera().getPosition();
      float partialTick = renderEvent.getPartialTick();
      List<TankerVfxRenderer.VisibleEvent> candidates = new ArrayList<>(EVENTS.size());

      for (TankerVfxRenderer.ActiveEvent event : EVENTS) {
         float elapsed = event.elapsed(now, partialTick);
         if (!(elapsed < 0.0F)
            && !(elapsed >= event.duration())
            && quality.allowsEvent(event)
            && (quality != TankerVfxRenderer.Quality.OFF || event.type() != 2 || !(elapsed >= 12.0F))) {
            TankerVfxRenderer.Anchor anchor = resolveAnchor(minecraft.level, minecraft, event, partialTick);
            AABB bounds = eventBounds(event.type(), anchor.position);
            double distanceSqr = distanceToBoundsSqr(camera, bounds);
            if (!(distanceSqr > quality.renderDistance * quality.renderDistance) && renderEvent.getFrustum().isVisible(bounds)) {
               int packedLight = LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(anchor.position));
               candidates.add(
                  new TankerVfxRenderer.VisibleEvent(
                     event, anchor.position, anchor.yaw, anchor.pitch, elapsed, distanceSqr, packedLight, anchor.firstPersonOwner
                  )
               );
            }
         }
      }

      candidates.sort(
         Comparator.<TankerVfxRenderer.VisibleEvent, Boolean>comparing(visual -> !visual.event.essential())
            .thenComparingDouble(visual -> visual.distanceSqr)
            .thenComparingLong(visual -> visual.event.sequence)
      );
      return candidates.size() > quality.maxVisible ? new ArrayList<>(candidates.subList(0, quality.maxVisible)) : candidates;
   }

   private static TankerVfxRenderer.Anchor resolveAnchor(ClientLevel level, Minecraft minecraft, TankerVfxRenderer.ActiveEvent event, float partialTick) {
      Vec3 position = event.origin();
      float yaw = event.yaw();
      float pitch = event.pitch();
      boolean firstPersonOwner = false;
      if (followsOwner(event.type())) {
         Entity owner = level.getEntity(event.ownerId());
         if (owner != null && !owner.isRemoved()) {
            position = new Vec3(
               Mth.lerp(partialTick, owner.xo, owner.getX()), Mth.lerp(partialTick, owner.yo, owner.getY()), Mth.lerp(partialTick, owner.zo, owner.getZ())
            );
            yaw = Mth.rotLerp(partialTick, owner.yRotO, owner.getYRot());
            pitch = Mth.lerp(partialTick, owner.xRotO, owner.getXRot());
            firstPersonOwner = minecraft.player != null && owner.getId() == minecraft.player.getId() && minecraft.options.getCameraType().isFirstPerson();
         }
      }

      return new TankerVfxRenderer.Anchor(position, yaw, pitch, firstPersonOwner);
   }

   private static AABB eventBounds(byte type, Vec3 anchor) {
      double below = 0.35;
      double radius;
      double above;
      switch (type) {
         case 1:
            radius = 6.0;
            above = 2.8;
            break;
         case 2:
            radius = 12.75;
            above = 2.5;
            break;
         case 3:
            radius = 4.8;
            above = 3.0;
            break;
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         default:
            radius = 3.25;
            above = 3.75;
            break;
         case 14:
         case 15:
         case 16:
         case 17:
            radius = 6.75;
            above = 3.0;
      }

      return new AABB(anchor.x - radius, anchor.y - below, anchor.z - radius, anchor.x + radius, anchor.y + above, anchor.z + radius);
   }

   private static double distanceToBoundsSqr(Vec3 point, AABB bounds) {
      double dx = Math.max(Math.max(bounds.minX - point.x, 0.0), point.x - bounds.maxX);
      double dy = Math.max(Math.max(bounds.minY - point.y, 0.0), point.y - bounds.maxY);
      double dz = Math.max(Math.max(bounds.minZ - point.z, 0.0), point.z - bounds.maxZ);
      return dx * dx + dy * dy + dz * dz;
   }

   private static void renderEvent(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      PoseStack stack = frame.poseStack;
      stack.pushPose();
      stack.translate(visual.position.x - frame.cameraPosition.x, visual.position.y - frame.cameraPosition.y, visual.position.z - frame.cameraPosition.z);
      stack.mulPose(Axis.YP.rotationDegrees(-visual.yaw));
      switch (visual.event.type()) {
         case 0:
            renderLeapStart(frame, visual, vertices, budget, pass);
            break;
         case 1:
            renderLeapLand(frame, visual, vertices, budget, pass);
            break;
         case 2:
            renderTaunt(frame, visual, vertices, budget, pass);
            break;
         case 3:
            renderBashSweep(frame, visual, vertices, budget, pass);
            break;
         case 4:
            renderBashHit(frame, visual, vertices, budget, pass, false);
            break;
         case 5:
            renderBashHit(frame, visual, vertices, budget, pass, true);
            break;
         case 6:
            renderReinforcementBrace(frame, visual, vertices, budget, pass);
            break;
         case 7:
            renderReinforcementHit(frame, visual, vertices, budget, pass);
            break;
         case 8:
            renderReinforcementStance(frame, visual, vertices, budget, pass, false);
            break;
         case 9:
            renderReinforcementStance(frame, visual, vertices, budget, pass, true);
            break;
         case 10:
            renderWillpower(frame, visual, vertices, budget, pass);
            break;
         case 11:
            renderWillpowerThreshold(frame, visual, vertices, budget, pass);
            break;
         case 12:
            renderWillpowerSettle(frame, visual, vertices, budget, pass);
            break;
         case 13:
            renderWillpowerBreak(frame, visual, vertices, budget, pass);
            break;
         case 14:
            renderMark(frame, visual, vertices, budget, pass);
            break;
         case 15:
            renderMarkThreshold(frame, visual, vertices, budget, pass);
            break;
         case 16:
            renderMarkEnd(frame, visual, vertices, budget, pass, true);
            break;
         case 17:
            renderMarkEnd(frame, visual, vertices, budget, pass, false);
      }

      stack.popPose();
   }

   private static void renderLeapStart(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float elapsed = visual.elapsed;
      float launch = Mth.clamp(elapsed / 4.0F, 0.0F, 1.0F);
      float release = elapsed < 4.0F ? Mth.sin(launch * (float) Math.PI) : 0.0F;
      float fade = fadeOut(elapsed, visual.event.duration(), 3.0F);
      PoseStack stack = frame.poseStack;
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         float plateAlpha = frame.quality == TankerVfxRenderer.Quality.OFF ? 255.0F : 180.0F;

         for (int side = -1; side <= 1; side += 2) {
            stack.pushPose();
            stack.translate(side * 0.27, 0.07 - release * 0.055, 0.08);
            stack.mulPose(Axis.YP.rotationDegrees(side * 5.0F));
            drawBox(vertices, stack.last(), budget, -0.19F, 0.0F, -0.35F, 0.19F, 0.1F + release * 0.055F, 0.32F, 2435886, alpha(plateAlpha * fade), 0, light);
            stack.popPose();
         }
      } else {
         if (elapsed <= 5.0F) {
            for (int side = -1; side <= 1; side += 2) {
               stack.pushPose();
               stack.translate(side * 0.27, 0.18, 0.1);
               drawPlate(vertices, stack.last(), budget, 0.29F, 0.055F, 0.0F, 1.0F, 15191426, alpha(150.0F * fade), 1, light);
               stack.popPose();
            }
         }

         if (frame.quality != TankerVfxRenderer.Quality.OFF && elapsed >= 3.0F) {
            int motes = frame.quality == TankerVfxRenderer.Quality.FULL ? 6 : 4;
            drawTrailDust(frame, visual, vertices, budget, Math.min(motes, frame.quality.burstParticles), fade);
         }
      }
   }

   private static void renderLeapLand(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float progress = easeOut(Mth.clamp(visual.elapsed / 10.0F, 0.0F, 1.0F));
      float radius = Math.max(0.45F, 5.0F * progress);
      float fade = fadeOut(visual.elapsed, visual.event.duration(), 3.2F);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         int slabs = frame.quality == TankerVfxRenderer.Quality.FULL ? 16 : 8;
         int surfaceAlpha = frame.quality == TankerVfxRenderer.Quality.OFF ? alpha(255.0F * fade) : alpha(190.0F * fade);

         for (int index = 0; index < slabs; index++) {
            float center = index * 360.0F / slabs + hash01(visual.event.seed(), index, 17) * 4.0F;
            float halfWidth = frame.quality == TankerVfxRenderer.Quality.FULL ? 7.0F : 13.0F;
            float inner = 0.36F + hash01(visual.event.seed(), index, 23) * 0.4F;
            drawFloorWedge(
               vertices,
               frame.poseStack.last(),
               budget,
               inner,
               radius,
               center - halfWidth,
               center + halfWidth,
               0.025F,
               index % 3 == 0 ? 4608344 : 2435886,
               surfaceAlpha,
               0,
               light
            );
         }
      } else {
         drawGroundRing(
            vertices,
            frame.poseStack.last(),
            budget,
            Math.max(0.15F, radius - 0.12F),
            radius,
            frame.quality.ringSegments,
            0.038F,
            15191426,
            alpha(165.0F * fade),
            1,
            light
         );
         drawGroundRing(
            vertices,
            frame.poseStack.last(),
            budget,
            0.1F,
            0.72F + progress * 0.34F,
            Math.min(12, frame.quality.ringSegments),
            0.044F,
            16756826,
            alpha(185.0F * fade),
            2,
            light
         );
         int sparks = frame.quality == TankerVfxRenderer.Quality.FULL ? 18 : 8;
         drawRadialSparks(frame, visual, vertices, budget, Math.min(sparks, frame.quality.burstParticles), radius * 0.72F, 0.08F, 0.75F, 15166509, fade);
      }
   }

   private static void renderTaunt(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float elapsed = visual.elapsed;
      float plant = easeOut(Mth.clamp(elapsed / 4.0F, 0.0F, 1.0F));
      float expansion = easeOut(Mth.clamp((elapsed - 4.0F) / 8.0F, 0.0F, 1.0F));
      float plantedRadius = 1.3F;
      float radius = elapsed < 4.0F ? 0.65F + plant * 0.65F : plantedRadius + (12.0F - plantedRadius) * expansion;
      if (elapsed >= 12.0F) {
         radius = 12.0F;
      }

      float heldPulse = elapsed < 12.0F ? 1.0F : 0.62F + 0.08F * Mth.sin(elapsed * 0.22F);
      float fade = fadeOut(elapsed, visual.event.duration(), 6.0F);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      if (pass != TankerVfxRenderer.Pass.SURFACE) {
         drawGroundRing(
            vertices,
            frame.poseStack.last(),
            budget,
            Math.max(0.0F, radius - 0.095F),
            radius,
            frame.quality.ringSegments,
            0.04F,
            15191426,
            alpha(132.0F * heldPulse * fade),
            1,
            light
         );
         if (elapsed < 9.0F) {
            int sparks = frame.quality == TankerVfxRenderer.Quality.FULL ? 10 : 4;
            drawRadialSparks(
               frame, visual, vertices, budget, Math.min(sparks, frame.quality.burstParticles), Math.max(0.7F, radius * 0.78F), 0.05F, 0.42F, 15166509, fade
            );
         }
      } else {
         int surfaceAlpha = frame.quality == TankerVfxRenderer.Quality.OFF ? 255 : alpha((elapsed < 12.0F ? 192.0F : 96.0F) * fade);
         drawGroundRing(
            vertices,
            frame.poseStack.last(),
            budget,
            Math.max(0.0F, radius - 0.34F),
            radius,
            frame.quality.ringSegments,
            0.026F,
            4608344,
            surfaceAlpha,
            0,
            light
         );
         float plateRadius = Math.max(0.72F, radius * 0.72F);

         for (int index = 0; index < 4; index++) {
            frame.poseStack.pushPose();
            frame.poseStack.mulPose(Axis.YP.rotationDegrees(index * 90.0F));
            frame.poseStack.translate(0.0, 0.11, plateRadius);
            frame.poseStack.mulPose(Axis.XP.rotationDegrees(-54.0F));
            drawPlate(vertices, frame.poseStack.last(), budget, 1.35F * Math.max(0.35F, expansion), 0.68F, 0.0F, 0.72F, 2435886, surfaceAlpha, 0, light);
            frame.poseStack.popPose();
         }

         if (elapsed < 5.0F) {
            frame.poseStack.pushPose();
            frame.poseStack.translate(0.0, 0.08, 0.0);
            drawCrest(vertices, frame.poseStack.last(), budget, 0.68F, 0.9F * plant, 2435886, surfaceAlpha, 0, light, 0);
            frame.poseStack.popPose();
         }
      }
   }

   private static void renderBashSweep(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float elapsed = visual.elapsed;
      float load = easeOut(Mth.clamp(elapsed / 2.0F, 0.0F, 1.0F));
      float lunge = easeOut(Mth.clamp((elapsed - 2.0F) / 4.0F, 0.0F, 1.0F));
      float reach = 0.72F + 2.8799999F * lunge;
      float fade = fadeOut(elapsed, visual.event.duration(), 1.4F);
      float localScale = visual.firstPersonOwner ? 0.72F : 1.0F;
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      frame.poseStack.pushPose();
      frame.poseStack.translate(0.0, visual.firstPersonOwner ? 0.2 : 0.0, visual.firstPersonOwner ? 0.45 : 0.0);
      frame.poseStack.scale(localScale, localScale, localScale);
      frame.poseStack.mulPose(Axis.XP.rotationDegrees(visual.pitch * 0.18F));
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         drawShieldArc(
            vertices,
            frame.poseStack.last(),
            budget,
            0.98F,
            0.62F,
            Math.min(10, frame.quality.ringSegments),
            reach,
            1.04F,
            2435886,
            frame.quality == TankerVfxRenderer.Quality.OFF ? alpha(255.0F * fade) : alpha(208.0F * fade),
            0,
            light
         );
         if (frame.quality == TankerVfxRenderer.Quality.FULL && lunge > 0.0F) {
            drawFloorWedge(
               vertices, frame.poseStack.last(), budget, 0.45F, Math.max(0.62F, reach), -10.0F, 10.0F, 0.08F, 4608344, alpha(92.0F * fade), 0, light
            );
         }
      } else {
         drawShieldArc(
            vertices,
            frame.poseStack.last(),
            budget,
            1.02F,
            0.89F,
            Math.min(10, frame.quality.ringSegments),
            reach + 0.012F,
            1.04F,
            16770209,
            alpha((118.0F + load * 54.0F) * fade),
            1,
            light
         );
         if (frame.quality == TankerVfxRenderer.Quality.FULL && lunge > 0.05F) {
            drawBashTrailSparks(frame, visual, vertices, budget, Math.min(6, frame.quality.burstParticles), reach, fade);
         }
      }

      frame.poseStack.popPose();
   }

   private static void renderBashHit(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass,
      boolean strainRelief
   ) {
      if (frame.quality != TankerVfxRenderer.Quality.OFF || !(visual.elapsed > 1.0F)) {
         float fade = fadeOut(visual.elapsed, visual.event.duration(), 5.0F);
         float snap = 0.84F + easeOut(Mth.clamp(visual.elapsed / 2.0F, 0.0F, 1.0F)) * 0.22F;
         int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
         frame.poseStack.pushPose();
         frame.poseStack.translate(0.0, strainRelief ? 0.82 : 0.72, strainRelief ? 0.58 : 0.0);
         frame.poseStack.scale(snap, snap, snap);
         if (pass == TankerVfxRenderer.Pass.SURFACE) {
            if (strainRelief) {
               drawBand(vertices, frame.poseStack.last(), budget, 0.72F, -0.08F, 0.08F, -92.0F, 92.0F, 8, 4608344, alpha(165.0F * fade), 0, light);
            } else {
               drawImpactSlab(
                  vertices,
                  frame.poseStack.last(),
                  budget,
                  1.16F,
                  0.82F,
                  0.18F,
                  4608344,
                  frame.quality == TankerVfxRenderer.Quality.OFF ? alpha(255.0F * fade) : alpha(208.0F * fade),
                  0,
                  light
               );
            }
         } else if (strainRelief) {
            drawBand(vertices, frame.poseStack.last(), budget, 0.74F, -0.035F, 0.035F, -88.0F, 88.0F, 8, 16770209, alpha(176.0F * fade), 1, light);
         } else {
            drawPlate(vertices, frame.poseStack.last(), budget, 0.82F, 0.55F, 0.105F, 0.66F, 16756826, alpha(208.0F * fade), 2, light);
            if (frame.quality == TankerVfxRenderer.Quality.FULL) {
               drawRadialSparks(frame, visual, vertices, budget, Math.min(10, frame.quality.burstParticles), 0.8F, 0.1F, 0.9F, 15166509, fade);
            }
         }

         frame.poseStack.popPose();
      }
   }

   private static void renderReinforcementBrace(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float lock = easeOut(Mth.clamp(visual.elapsed / 4.0F, 0.0F, 1.0F));
      float fade = fadeOut(visual.elapsed, visual.event.duration(), 2.0F);
      float firstPersonScale = visual.firstPersonOwner ? 0.68F : 1.0F;
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      frame.poseStack.pushPose();
      frame.poseStack.translate(0.0, visual.firstPersonOwner ? 0.1 : 0.0, visual.firstPersonOwner ? 1.0 : 0.62);
      frame.poseStack.scale(firstPersonScale, firstPersonScale, firstPersonScale);
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         if (frame.quality == TankerVfxRenderer.Quality.OFF) {
            drawPlate(vertices, frame.poseStack.last(), budget, 1.38F, 1.64F, 0.0F, 0.78F, 2435886, alpha(255.0F * fade), 0, light);
         } else {
            int plates = frame.quality == TankerVfxRenderer.Quality.FULL ? 5 : 3;

            for (int index = 0; index < plates; index++) {
               float centered = index - (plates - 1) * 0.5F;
               frame.poseStack.pushPose();
               frame.poseStack.translate(centered * 0.34F * lock, 0.72 + Math.abs(centered) * 0.06, -Math.abs(centered) * 0.025);
               frame.poseStack.mulPose(Axis.ZP.rotationDegrees(centered * -7.0F));
               drawPlate(
                  vertices,
                  frame.poseStack.last(),
                  budget,
                  0.66F,
                  1.38F - Math.abs(centered) * 0.08F,
                  0.0F,
                  0.76F,
                  index % 2 == 0 ? 2435886 : 4608344,
                  alpha(214.0F * fade),
                  0,
                  light
               );
               frame.poseStack.popPose();
            }
         }
      } else {
         int seams = frame.quality == TankerVfxRenderer.Quality.FULL ? 4 : 2;

         for (int index = 0; index < seams; index++) {
            float x = (index - (seams - 1) * 0.5F) * 0.32F;
            frame.poseStack.pushPose();
            frame.poseStack.translate(x * lock, 0.72, 0.018);
            drawPlate(vertices, frame.poseStack.last(), budget, 0.045F, 1.18F, 0.0F, 1.0F, 15191426, alpha(148.0F * fade), 1, light);
            frame.poseStack.popPose();
         }
      }

      frame.poseStack.popPose();
   }

   private static void renderReinforcementHit(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float fade = fadeOut(visual.elapsed, visual.event.duration(), 4.0F);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      frame.poseStack.pushPose();
      frame.poseStack.translate(0.0, 1.05, 0.0);
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         drawImpactSlab(vertices, frame.poseStack.last(), budget, 1.35F, 1.22F, 0.16F, 6713464, alpha(218.0F * fade), 0, light);
      } else {
         drawPlate(vertices, frame.poseStack.last(), budget, 0.42F, 0.42F, 0.1F, 0.72F, 16756826, alpha(196.0F * fade), 2, light);
         if (frame.quality == TankerVfxRenderer.Quality.FULL) {
            drawRadialSparks(frame, visual, vertices, budget, 1, 0.2F, 0.85F, 0.95F, 16756826, fade);
         }
      }

      frame.poseStack.popPose();
   }

   private static void renderReinforcementStance(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass,
      boolean ending
   ) {
      float progress = ending ? Mth.clamp(visual.elapsed / 6.0F, 0.0F, 1.0F) : easeOut(Mth.clamp(visual.elapsed / 6.0F, 0.0F, 1.0F));
      float fade = ending ? 1.0F - progress : fadeOut(visual.elapsed, visual.event.duration(), 5.0F);
      float open = ending ? 1.0F + progress * 0.28F : 1.12F - progress * 0.12F;
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      frame.poseStack.pushPose();
      frame.poseStack.translate(0.0, 0.9, visual.firstPersonOwner ? 0.55 : 0.0);
      frame.poseStack.scale(open, 1.0F, open);
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         if (frame.quality == TankerVfxRenderer.Quality.OFF) {
            for (int side = -1; side <= 1; side += 2) {
               frame.poseStack.pushPose();
               frame.poseStack.translate(side * 0.28, 0.0, 0.52);
               drawPlate(vertices, frame.poseStack.last(), budget, 0.42F, 0.54F, 0.0F, 0.78F, 2435886, alpha(255.0F * fade), 0, light);
               frame.poseStack.popPose();
            }
         } else {
            drawBand(vertices, frame.poseStack.last(), budget, 0.64F, -0.32F, -0.13F, -68.0F, 68.0F, 8, 2435886, alpha(132.0F * fade), 0, light);
            drawBand(vertices, frame.poseStack.last(), budget, 0.57F, 0.05F, 0.2F, -58.0F, 58.0F, 8, 4608344, alpha(112.0F * fade), 0, light);
         }
      } else {
         drawBand(vertices, frame.poseStack.last(), budget, 0.655F, -0.335F, -0.29F, -68.0F, 68.0F, 8, 15191426, alpha(78.0F * fade), 1, light);
         drawBand(vertices, frame.poseStack.last(), budget, 0.585F, 0.18F, 0.225F, -58.0F, 58.0F, 8, 15191426, alpha(64.0F * fade), 1, light);
      }

      frame.poseStack.popPose();
   }

   private static void renderWillpower(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float bind = easeOut(Mth.clamp(visual.elapsed / 8.0F, 0.0F, 1.0F));
      float fade = fadeOut(visual.elapsed, visual.event.duration(), 7.0F);
      float radius = 0.86F - bind * 0.28F;
      int threshold = Math.min(4, (visual.event.intensity + 63) / 64);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      frame.poseStack.pushPose();
      frame.poseStack.translate(0.0, 0.68, 0.0);
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         if (frame.quality == TankerVfxRenderer.Quality.OFF) {
            drawBand(
               vertices,
               frame.poseStack.last(),
               budget,
               radius,
               -0.08F,
               0.1F,
               0.0F,
               360.0F,
               8,
               threshold >= 3 ? 6713464 : 2435886,
               alpha(255.0F * fade),
               0,
               light
            );
         } else {
            for (int band = 0; band < 3; band++) {
               float y = band * 0.38F;
               drawBand(
                  vertices,
                  frame.poseStack.last(),
                  budget,
                  radius - band * 0.035F,
                  y - 0.07F,
                  y + 0.07F,
                  0.0F,
                  360.0F,
                  frame.quality.ringSegments,
                  band == 1 ? 4608344 : 2435886,
                  alpha((132.0F + threshold * 8.0F) * fade),
                  0,
                  light
               );
            }
         }
      } else if (frame.quality == TankerVfxRenderer.Quality.LOW) {
         int tint = threshold >= 3 ? 15166509 : 15191426;
         int material = threshold >= 3 ? 2 : 1;
         drawBand(
            vertices,
            frame.poseStack.last(),
            budget,
            radius + 0.012F,
            0.305F,
            0.345F,
            0.0F,
            360.0F,
            12,
            tint,
            alpha((54.0F + threshold * 16.0F) * fade),
            material,
            light
         );
      } else {
         for (int crack = 0; crack < threshold; crack++) {
            float x = -0.34F + crack * 0.22F;
            float y = 0.08F + (crack & 1) * 0.34F;
            drawEmberCrack(
               vertices,
               frame.poseStack.last(),
               budget,
               x,
               y,
               radius + 0.018F,
               0.18F + crack * 0.03F,
               visual.event.seed() + crack * 37,
               16756826,
               alpha((126.0F + threshold * 18.0F) * fade),
               light
            );
         }

         drawBand(
            vertices,
            frame.poseStack.last(),
            budget,
            radius + 0.014F,
            -0.09F,
            -0.045F,
            0.0F,
            360.0F,
            frame.quality.ringSegments,
            15191426,
            alpha(58.0F * fade),
            1,
            light
         );
      }

      frame.poseStack.popPose();
   }

   private static void renderWillpowerThreshold(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float fade = fadeOut(visual.elapsed, visual.event.duration(), 6.0F);
      int threshold = Math.max(1, Math.min(4, (visual.event.intensity + 63) / 64));
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      frame.poseStack.pushPose();
      frame.poseStack.translate(0.0, 0.98, 0.0);
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         drawBand(vertices, frame.poseStack.last(), budget, 0.62F, -0.07F, 0.07F, 0.0F, 360.0F, 12, 6713464, alpha(92.0F * fade), 0, light);
      } else if (frame.quality == TankerVfxRenderer.Quality.FULL) {
         for (int crack = 0; crack < threshold; crack++) {
            drawEmberCrack(
               vertices,
               frame.poseStack.last(),
               budget,
               -0.28F + crack * 0.18F,
               -0.22F + (crack & 1) * 0.2F,
               0.64F,
               0.28F,
               visual.event.seed() + crack * 29,
               16756826,
               alpha(188.0F * fade),
               light
            );
         }
      }

      frame.poseStack.popPose();
   }

   private static void renderWillpowerSettle(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float progress = easeOut(Mth.clamp(visual.elapsed / 10.0F, 0.0F, 1.0F));
      float radius = 2.15F - progress * 1.55F;
      float fade = fadeOut(visual.elapsed, visual.event.duration(), 3.0F);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      frame.poseStack.pushPose();
      frame.poseStack.translate(0.0, 0.72, 0.0);
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         drawBand(
            vertices,
            frame.poseStack.last(),
            budget,
            radius,
            -0.055F,
            0.055F,
            0.0F,
            360.0F,
            frame.quality.ringSegments,
            frame.quality == TankerVfxRenderer.Quality.OFF ? 6713464 : 4608344,
            frame.quality == TankerVfxRenderer.Quality.OFF ? alpha(255.0F * fade) : alpha(158.0F * fade),
            0,
            light
         );
      } else {
         int color = visual.event.intensity >= 192 ? 16756826 : 16770209;
         int material = visual.event.intensity >= 192 ? 2 : 1;
         drawBand(
            vertices,
            frame.poseStack.last(),
            budget,
            radius + 0.012F,
            -0.025F,
            0.025F,
            0.0F,
            360.0F,
            frame.quality.ringSegments,
            color,
            alpha(152.0F * fade),
            material,
            light
         );
      }

      frame.poseStack.popPose();
   }

   private static void renderWillpowerBreak(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float progress = easeOut(Mth.clamp(visual.elapsed / 8.0F, 0.0F, 1.0F));
      float fade = 1.0F - Mth.clamp(visual.elapsed / 8.0F, 0.0F, 1.0F);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;

      for (int fragment = 0; fragment < 6; fragment++) {
         float angle = fragment * 60.0F + hash01(visual.event.seed(), fragment, 73) * 14.0F;
         float radius = 0.56F + progress * (0.72F + hash01(visual.event.seed(), fragment, 79) * 0.65F);
         frame.poseStack.pushPose();
         frame.poseStack.mulPose(Axis.YP.rotationDegrees(angle));
         frame.poseStack.translate(0.0, 0.58 + fragment % 3 * 0.31, radius);
         frame.poseStack.mulPose(Axis.ZP.rotationDegrees(angle + progress * 34.0F));
         if (pass == TankerVfxRenderer.Pass.SURFACE) {
            drawPlate(vertices, frame.poseStack.last(), budget, 0.34F, 0.16F, 0.0F, 0.55F, 4608344, alpha(186.0F * fade), 0, light);
         } else {
            drawPlate(vertices, frame.poseStack.last(), budget, 0.26F, 0.045F, 0.012F, 1.0F, 16756826, alpha(158.0F * fade), 3, light);
         }

         frame.poseStack.popPose();
      }
   }

   private static void renderMark(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float deploy = easeOut(Mth.clamp(visual.elapsed / 10.0F, 0.0F, 1.0F));
      float radius = 6.0F * deploy;
      if (visual.elapsed >= 10.0F) {
         radius = 6.0F;
      }

      float fade = fadeOut(visual.elapsed, visual.event.duration(), 8.0F);
      float held = visual.elapsed < 10.0F ? 1.0F : 0.88F + 0.04F * Mth.sin(visual.elapsed * 0.16F);
      int fracture = Math.min(3, (visual.event.intensity + 63) / 64);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         drawGroundRing(
            vertices,
            frame.poseStack.last(),
            budget,
            Math.max(0.0F, radius - 0.3F),
            radius,
            frame.quality.ringSegments,
            0.028F,
            fracture >= 2 ? 6713464 : 4608344,
            frame.quality == TankerVfxRenderer.Quality.OFF ? alpha(255.0F * fade) : alpha(174.0F * held * fade),
            0,
            light
         );
         if (frame.quality != TankerVfxRenderer.Quality.OFF) {
            for (int tab = 0; tab < 8; tab++) {
               float angle = tab * 45.0F;
               drawFloorWedge(
                  vertices,
                  frame.poseStack.last(),
                  budget,
                  Math.max(0.0F, radius - 1.12F),
                  radius - 0.36F,
                  angle - 3.7F,
                  angle + 3.7F,
                  0.034F,
                  2435886,
                  alpha(188.0F * fade),
                  0,
                  light
               );
            }
         }

         frame.poseStack.pushPose();
         frame.poseStack.translate(0.0, 0.04, 0.0);
         drawCrest(
            vertices,
            frame.poseStack.last(),
            budget,
            frame.quality == TankerVfxRenderer.Quality.OFF ? 0.82F : 0.92F,
            1.35F * Math.max(0.25F, deploy),
            2435886,
            frame.quality == TankerVfxRenderer.Quality.OFF ? alpha(255.0F * fade) : alpha(216.0F * fade),
            0,
            light,
            fracture
         );
         frame.poseStack.popPose();
      } else {
         drawGroundRing(
            vertices,
            frame.poseStack.last(),
            budget,
            Math.max(0.0F, radius - 0.085F),
            radius,
            frame.quality.ringSegments,
            0.044F,
            15191426,
            alpha(112.0F * held * fade),
            1,
            light
         );

         for (int crack = 0; crack < fracture; crack++) {
            float angle = 35.0F + crack * 97.0F;
            frame.poseStack.pushPose();
            frame.poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            frame.poseStack.translate(0.0, 0.055, Math.max(0.4F, radius - 0.22F));
            frame.poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            drawEmberCrack(
               vertices, frame.poseStack.last(), budget, 0.0F, 0.0F, 0.012F, 0.42F, visual.event.seed() + crack * 61, 16756826, alpha(148.0F * fade), light
            );
            frame.poseStack.popPose();
         }
      }
   }

   private static void renderMarkThreshold(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass
   ) {
      float progress = easeOut(Mth.clamp(visual.elapsed / visual.event.duration(), 0.0F, 1.0F));
      float fade = 1.0F - progress;
      float radius = 6.0F - progress * 0.42F;
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         drawGroundRing(
            vertices, frame.poseStack.last(), budget, radius - 0.2F, radius, frame.quality.ringSegments, 0.052F, 6713464, alpha(112.0F * fade), 0, light
         );
      } else if (frame.quality == TankerVfxRenderer.Quality.FULL) {
         drawGroundRing(
            vertices, frame.poseStack.last(), budget, radius - 0.08F, radius, frame.quality.ringSegments, 0.056F, 15166509, alpha(138.0F * fade), 2, light
         );
      }
   }

   private static void renderMarkEnd(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      TankerVfxRenderer.Pass pass,
      boolean broken
   ) {
      float progress = easeOut(Mth.clamp(visual.elapsed / 8.0F, 0.0F, 1.0F));
      float radius = broken ? 6.0F + progress * 0.65F : 6.0F * (1.0F - progress);
      float fade = 1.0F - Mth.clamp(visual.elapsed / 8.0F, 0.0F, 1.0F);
      int light = pass == TankerVfxRenderer.Pass.SURFACE ? visual.packedLight : 15728880;
      int segments = frame.quality.ringSegments;
      if (pass == TankerVfxRenderer.Pass.SURFACE) {
         for (int index = 0; index < segments; index++) {
            if (!broken || (index + visual.event.seed()) % 3 != 0) {
               float center = index * 360.0F / segments;
               float half = 150.0F / segments;
               drawFloorWedge(
                  vertices,
                  frame.poseStack.last(),
                  budget,
                  Math.max(0.0F, radius - 0.28F),
                  radius,
                  center - half,
                  center + half,
                  0.035F,
                  broken ? 6713464 : 4608344,
                  frame.quality == TankerVfxRenderer.Quality.OFF ? alpha(238.0F * fade) : alpha(184.0F * fade),
                  0,
                  light
               );
            }
         }

         frame.poseStack.pushPose();
         frame.poseStack.translate(broken ? progress * 0.22F : 0.0F, 0.04, 0.0);
         drawCrest(vertices, frame.poseStack.last(), budget, 0.88F, 1.3F * fade, 2435886, alpha(218.0F * fade), 0, light, broken ? 3 : 1);
         frame.poseStack.popPose();
      } else {
         int color = broken ? 16756826 : 15191426;
         int material = broken ? 2 : 1;
         drawGroundRing(
            vertices, frame.poseStack.last(), budget, Math.max(0.0F, radius - 0.08F), radius, segments, 0.052F, color, alpha(142.0F * fade), material, light
         );
         if (broken && frame.quality == TankerVfxRenderer.Quality.FULL) {
            drawRadialSparks(frame, visual, vertices, budget, Math.min(12, frame.quality.burstParticles), 6.0F, 0.06F, 0.7F, 15166509, fade);
         }
      }
   }

   private static void drawPlate(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float width,
      float height,
      float z,
      float topScale,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      float halfWidth = width * 0.5F;
      float topHalfWidth = halfWidth * topScale;
      float halfHeight = height * 0.5F;
      quad(
         vertices,
         pose,
         budget,
         new Vec3(-halfWidth, -halfHeight, z),
         new Vec3(halfWidth, -halfHeight, z),
         new Vec3(topHalfWidth, halfHeight, z),
         new Vec3(-topHalfWidth, halfHeight, z),
         color,
         alpha,
         material,
         packedLight
      );
   }

   private static void drawFloorWedge(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float innerRadius,
      float outerRadius,
      float startDegrees,
      float endDegrees,
      float y,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      double start = Math.toRadians(startDegrees);
      double end = Math.toRadians(endDegrees);
      Vec3 innerStart = polar(innerRadius, start, y);
      Vec3 outerStart = polar(outerRadius, start, y);
      Vec3 outerEnd = polar(outerRadius, end, y);
      Vec3 innerEnd = polar(innerRadius, end, y);
      quad(vertices, pose, budget, innerStart, outerStart, outerEnd, innerEnd, color, alpha, material, packedLight);
   }

   private static void drawGroundRing(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float innerRadius,
      float outerRadius,
      int segments,
      float y,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      int boundedSegments = Mth.clamp(segments, 3, 32);

      for (int index = 0; index < boundedSegments; index++) {
         float start = index * 360.0F / boundedSegments;
         float end = (index + 1) * 360.0F / boundedSegments;
         drawFloorWedge(vertices, pose, budget, innerRadius, outerRadius, start, end, y, color, alpha, material, packedLight);
      }
   }

   private static void drawShieldArc(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float outerRadius,
      float innerRadius,
      int segments,
      float forward,
      float centerY,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      int boundedSegments = Mth.clamp(segments, 4, 16);

      for (int index = 0; index < boundedSegments; index++) {
         double start = Math.toRadians(-112.0F + index * 224.0F / boundedSegments);
         double end = Math.toRadians(-112.0F + (index + 1) * 224.0F / boundedSegments);
         Vec3 outerStart = new Vec3(Math.sin(start) * outerRadius, centerY + Math.cos(start) * outerRadius, forward);
         Vec3 outerEnd = new Vec3(Math.sin(end) * outerRadius, centerY + Math.cos(end) * outerRadius, forward);
         Vec3 innerEnd = new Vec3(Math.sin(end) * innerRadius, centerY + Math.cos(end) * innerRadius, forward + 0.012F);
         Vec3 innerStart = new Vec3(Math.sin(start) * innerRadius, centerY + Math.cos(start) * innerRadius, forward + 0.012F);
         quad(vertices, pose, budget, innerStart, outerStart, outerEnd, innerEnd, color, alpha, material, packedLight);
      }
   }

   private static void drawBand(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float radius,
      float bottomY,
      float topY,
      float startDegrees,
      float endDegrees,
      int segments,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      int boundedSegments = Mth.clamp(segments, 3, 32);
      float span = endDegrees - startDegrees;

      for (int index = 0; index < boundedSegments; index++) {
         double start = Math.toRadians(startDegrees + span * index / boundedSegments);
         double end = Math.toRadians(startDegrees + span * (index + 1) / boundedSegments);
         Vec3 lowerStart = new Vec3(Math.sin(start) * radius, bottomY, Math.cos(start) * radius);
         Vec3 lowerEnd = new Vec3(Math.sin(end) * radius, bottomY, Math.cos(end) * radius);
         Vec3 upperEnd = new Vec3(Math.sin(end) * radius, topY, Math.cos(end) * radius);
         Vec3 upperStart = new Vec3(Math.sin(start) * radius, topY, Math.cos(start) * radius);
         quad(vertices, pose, budget, lowerStart, lowerEnd, upperEnd, upperStart, color, alpha, material, packedLight);
      }
   }

   private static void drawImpactSlab(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float width,
      float height,
      float depth,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      drawBox(
         vertices, pose, budget, -width * 0.5F, -height * 0.5F, -depth * 0.5F, width * 0.5F, height * 0.5F, depth * 0.5F, color, alpha, material, packedLight
      );
   }

   private static void drawBox(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      if (budget.remaining() >= 24) {
         Vec3 lbf = new Vec3(minX, minY, minZ);
         Vec3 rbf = new Vec3(maxX, minY, minZ);
         Vec3 rtf = new Vec3(maxX, maxY, minZ);
         Vec3 ltf = new Vec3(minX, maxY, minZ);
         Vec3 lbb = new Vec3(minX, minY, maxZ);
         Vec3 rbb = new Vec3(maxX, minY, maxZ);
         Vec3 rtb = new Vec3(maxX, maxY, maxZ);
         Vec3 ltb = new Vec3(minX, maxY, maxZ);
         quad(vertices, pose, budget, lbf, rbf, rtf, ltf, color, alpha, material, packedLight);
         quad(vertices, pose, budget, rbb, lbb, ltb, rtb, color, alpha, material, packedLight);
         quad(vertices, pose, budget, lbb, lbf, ltf, ltb, color, alpha, material, packedLight);
         quad(vertices, pose, budget, rbf, rbb, rtb, rtf, color, alpha, material, packedLight);
         quad(vertices, pose, budget, ltf, rtf, rtb, ltb, color, alpha, material, packedLight);
         quad(vertices, pose, budget, lbb, rbb, rbf, lbf, color, alpha, material, packedLight);
      }
   }

   private static void drawCrest(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float width,
      float height,
      int color,
      int alpha,
      int material,
      int packedLight,
      int fractureStage
   ) {
      float half = width * 0.5F;
      float shoulderY = height * 0.58F;
      float topY = height;
      float split = fractureStage >= 3 ? width * 0.055F : 0.0F;
      quad(
         vertices,
         pose,
         budget,
         new Vec3(-half - split, shoulderY, 0.0),
         new Vec3(-split, shoulderY, 0.0),
         new Vec3(-split, topY, 0.0),
         new Vec3(-half * 0.78F - split, topY, 0.0),
         color,
         alpha,
         material,
         packedLight
      );
      quad(
         vertices,
         pose,
         budget,
         new Vec3(split, shoulderY, 0.0),
         new Vec3(half + split, shoulderY, 0.0),
         new Vec3(half * 0.78F + split, topY, 0.0),
         new Vec3(split, topY, 0.0),
         color,
         alpha,
         material,
         packedLight
      );
      quad(
         vertices,
         pose,
         budget,
         new Vec3(-half - split, shoulderY, 0.0),
         new Vec3(-split, shoulderY, 0.0),
         new Vec3(-split, 0.0, 0.0),
         new Vec3(-split, 0.0, 0.0),
         color,
         alpha,
         material,
         packedLight
      );
      quad(
         vertices,
         pose,
         budget,
         new Vec3(split, shoulderY, 0.0),
         new Vec3(half + split, shoulderY, 0.0),
         new Vec3(split, 0.0, 0.0),
         new Vec3(split, 0.0, 0.0),
         color,
         alpha,
         material,
         packedLight
      );
      int cracks = Mth.clamp(fractureStage, 0, 3);

      for (int crack = 0; crack < cracks; crack++) {
         float x = -width * 0.23F + crack * width * 0.21F;
         drawEmberCrack(
            vertices,
            pose,
            budget,
            x,
            height * 0.3F,
            0.012F,
            height * (0.2F + crack * 0.035F),
            20903 + crack * 97,
            6713464,
            Math.min(240, alpha + 18),
            packedLight
         );
      }
   }

   private static void drawEmberCrack(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float startX,
      float startY,
      float z,
      float length,
      int seed,
      int color,
      int alpha,
      int packedLight
   ) {
      float x = startX;
      float y = startY;

      for (int segment = 0; segment < 3; segment++) {
         float nextX = x + (hash01(seed, segment, 101) - 0.5F) * length * 0.48F;
         float nextY = y + length / 3.0F;
         drawRibbon2d(vertices, pose, budget, x, y, nextX, nextY, z, 0.018F + segment * 0.003F, color, alpha, 3, packedLight);
         x = nextX;
         y = nextY;
      }
   }

   private static void drawRibbon2d(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      float startX,
      float startY,
      float endX,
      float endY,
      float z,
      float halfWidth,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      float dx = endX - startX;
      float dy = endY - startY;
      float length = Mth.sqrt(dx * dx + dy * dy);
      if (!(length < 1.0E-4F)) {
         float sideX = -dy / length * halfWidth;
         float sideY = dx / length * halfWidth;
         quad(
            vertices,
            pose,
            budget,
            new Vec3(startX + sideX, startY + sideY, z),
            new Vec3(endX + sideX, endY + sideY, z),
            new Vec3(endX - sideX, endY - sideY, z),
            new Vec3(startX - sideX, startY - sideY, z),
            color,
            alpha,
            material,
            packedLight
         );
      }
   }

   private static void drawRadialSparks(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      int count,
      float radius,
      float baseY,
      float height,
      int color,
      float fade
   ) {
      for (int index = 0; index < count; index++) {
         float angle = hash01(visual.event.seed(), index, 131) * (float) (Math.PI * 2);
         float distance = radius * (0.28F + hash01(visual.event.seed(), index, 137) * 0.72F);
         float x = Mth.cos(angle) * distance;
         float z = Mth.sin(angle) * distance;
         float y = baseY + hash01(visual.event.seed(), index, 139) * height;
         float size = 0.045F + hash01(visual.event.seed(), index, 149) * 0.075F;
         drawSparkBillboard(frame, visual, vertices, budget, x, y, z, size, color, alpha((108.0F + size * 540.0F) * fade));
      }
   }

   private static void drawTrailDust(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      int count,
      float fade
   ) {
      for (int index = 0; index < count; index++) {
         float progress = (index + 1.0F) / (count + 1.0F);
         float x = (hash01(visual.event.seed(), index, 157) - 0.5F) * 0.7F;
         float y = 0.08F + hash01(visual.event.seed(), index, 163) * 0.42F;
         float z = -0.25F - progress * 1.65F;
         float size = 0.065F + hash01(visual.event.seed(), index, 167) * 0.06F;
         drawSparkBillboard(frame, visual, vertices, budget, x, y, z, size, 12102289, alpha(92.0F * fade));
      }
   }

   private static void drawBashTrailSparks(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      int count,
      float reach,
      float fade
   ) {
      for (int index = 0; index < count; index++) {
         float progress = (index + 1.0F) / (count + 1.0F);
         float x = (hash01(visual.event.seed(), index, 173) - 0.5F) * 0.54F;
         float y = 0.32F + hash01(visual.event.seed(), index, 179) * 0.8F;
         float z = 0.45F + progress * Math.max(0.2F, reach - 0.45F);
         drawSparkBillboard(frame, visual, vertices, budget, x, y, z, 0.05F, 15191426, alpha(76.0F * fade));
      }
   }

   private static void drawSparkBillboard(
      TankerVfxRenderer.RenderFrame frame,
      TankerVfxRenderer.VisibleEvent visual,
      VertexConsumer vertices,
      TankerVfxRenderer.FrameBudget budget,
      float x,
      float y,
      float z,
      float size,
      int color,
      int alpha
   ) {
      frame.poseStack.pushPose();
      frame.poseStack.translate(x, y, z);
      frame.poseStack.mulPose(Axis.YP.rotationDegrees(visual.yaw));
      frame.poseStack.mulPose(frame.billboard);
      drawPlate(vertices, frame.poseStack.last(), budget, size, size, 0.0F, 1.0F, color, alpha, 4, 15728880);
      frame.poseStack.popPose();
   }

   private static Vec3 polar(float radius, double angle, float y) {
      return new Vec3(Math.sin(angle) * radius, y, Math.cos(angle) * radius);
   }

   private static void quad(
      VertexConsumer vertices,
      Pose pose,
      TankerVfxRenderer.FrameBudget budget,
      Vec3 first,
      Vec3 second,
      Vec3 third,
      Vec3 fourth,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      if (alpha > 0 && budget.reserve(4)) {
         Vec3 normal = second.subtract(first).cross(third.subtract(first));
         if (normal.lengthSqr() < 1.0E-7) {
            normal = new Vec3(0.0, 0.0, 1.0);
         } else {
            normal = normal.normalize();
         }

         vertex(vertices, pose.pose(), pose.normal(), first, normal, 0.0F, 1.0F, color, alpha, material, packedLight);
         vertex(vertices, pose.pose(), pose.normal(), second, normal, 1.0F, 1.0F, color, alpha, material, packedLight);
         vertex(vertices, pose.pose(), pose.normal(), third, normal, 1.0F, 0.0F, color, alpha, material, packedLight);
         vertex(vertices, pose.pose(), pose.normal(), fourth, normal, 0.0F, 0.0F, color, alpha, material, packedLight);
      }
   }

   private static void vertex(
      VertexConsumer vertices,
      Matrix4f matrix,
      Matrix3f normalMatrix,
      Vec3 point,
      Vec3 normal,
      float u,
      float v,
      int color,
      int alpha,
      int material,
      int packedLight
   ) {
      int red = color >> 16 & 0xFF;
      int green = color >> 8 & 0xFF;
      int blue = color & 0xFF;
      float materialU = material + Mth.clamp(u, 0.0F, 0.999F);
      vertices.vertex(matrix, (float)point.x, (float)point.y, (float)point.z)
         .color(red, green, blue, Mth.clamp(alpha, 0, 255))
         .uv(materialU, Mth.clamp(v, 0.0F, 1.0F))
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(packedLight)
         .normal(normalMatrix, (float)normal.x, (float)normal.y, (float)normal.z)
         .endVertex();
   }

   private static float easeOut(float value) {
      float clamped = Mth.clamp(value, 0.0F, 1.0F);
      float inverse = 1.0F - clamped;
      return 1.0F - inverse * inverse * inverse;
   }

   private static float fadeOut(float elapsed, int duration, float fadeTicks) {
      return Mth.clamp((duration - elapsed) / Math.max(0.001F, fadeTicks), 0.0F, 1.0F);
   }

   private static int alpha(float value) {
      return Mth.clamp(Math.round(value), 0, 255);
   }

   private static float hash01(int seed, int index, int salt) {
      int value = seed ^ index * -1640531527 ^ salt * -2048144789;
      value ^= value >>> 16;
      value *= 2146121005;
      value ^= value >>> 15;
      value *= -2073254261;
      value ^= value >>> 16;
      return (value & 16777215) / 1.6777215E7F;
   }

   private static boolean followsOwner(byte type) {
      return switch (type) {
         case 0, 3, 5, 6, 8, 9, 10, 11, 12, 13 -> true;
         default -> false;
      };
   }

   private static void applyStateTransition(TankerVfxEventMessage message) {
      int ownerId = message.ownerEntityId;
      switch (message.eventType) {
         case 1:
            removeTypes(ownerId, 0);
         case 2:
         case 3:
         case 4:
         case 7:
         default:
            break;
         case 5:
            updateIntensity(ownerId, (byte)10, message.intensity);
            break;
         case 6:
            removeTypes(ownerId, 6, 8);
            break;
         case 8:
            removeTypes(ownerId, 6, 8);
            break;
         case 9:
            removeTypes(ownerId, 8);
            break;
         case 10:
            removeTypes(ownerId, 10);
            break;
         case 11:
            updateIntensity(ownerId, (byte)10, message.intensity);
            break;
         case 12:
         case 13:
            removeTypes(ownerId, 10);
            break;
         case 14:
            removeTypes(ownerId, 14);
            break;
         case 15:
            updateIntensity(ownerId, (byte)14, message.intensity);
            break;
         case 16:
         case 17:
            removeTypes(ownerId, 14);
      }
   }

   private static void updateIntensity(int ownerId, byte type, int intensity) {
      for (int index = EVENTS.size() - 1; index >= 0; index--) {
         TankerVfxRenderer.ActiveEvent event = EVENTS.get(index);
         if (event.ownerId() == ownerId && event.type() == type) {
            event.intensity = Mth.clamp(intensity, 0, 255);
            return;
         }
      }
   }

   private static void removeTypes(int ownerId, byte... types) {
      EVENTS.removeIf(event -> {
         if (event.ownerId() != ownerId) {
            return false;
         }

         for (byte type : types) {
            if (event.type() == type) {
               return true;
            }
         }

         return false;
      });
   }

   private static boolean isDuplicate(TankerVfxEventMessage message) {
      for (TankerVfxRenderer.ActiveEvent event : EVENTS) {
         if (event.type() == message.eventType
            && event.ownerId() == message.ownerEntityId
            && event.startTick() == message.serverStartTick
            && event.seed() == message.seed) {
            return true;
         }
      }

      return false;
   }

   private static void pruneInvalidAndExpired(long now) {
      EVENTS.removeIf(event -> !TankerVfxEventMessage.isKnownEventType(event.type()) || event.expired(now));
      trimQueueToLimit();
      if (EVENTS.isEmpty()) {
         nextSequence = 0L;
      }
   }

   private static void trimQueueToLimit() {
      while (EVENTS.size() > 96) {
         int candidate = oldestMatching(false, false);
         if (candidate < 0) {
            candidate = oldestMatching(true, false);
         }

         if (candidate < 0) {
            candidate = oldestIndex();
         }

         if (candidate < 0) {
            break;
         }

         EVENTS.remove(candidate);
      }
   }

   private static boolean evictFor(TankerVfxRenderer.ActiveEvent incoming, Minecraft minecraft) {
      Vec3 reference = minecraft.player == null ? incoming.origin() : minecraft.player.position();
      int candidate = farthestMatching(reference, false, false);
      if (candidate >= 0) {
         EVENTS.remove(candidate);
         return true;
      }

      if (!incoming.essential()) {
         return false;
      }

      candidate = farthestMatching(reference, true, false);
      if (candidate < 0) {
         candidate = oldestIndex();
      }

      if (candidate >= 0) {
         EVENTS.remove(candidate);
         return true;
      } else {
         return false;
      }
   }

   private static int farthestMatching(Vec3 reference, boolean allowEssential, boolean allowProtectedBoundary) {
      int candidate = -1;
      double farthest = -1.0;

      for (int index = 0; index < EVENTS.size(); index++) {
         TankerVfxRenderer.ActiveEvent event = EVENTS.get(index);
         if ((allowEssential || !event.essential()) && (allowProtectedBoundary || !event.protectedBoundary())) {
            double distance = event.origin().distanceToSqr(reference);
            if (distance > farthest) {
               farthest = distance;
               candidate = index;
            }
         }
      }

      return candidate;
   }

   private static int oldestIndex() {
      int candidate = -1;
      long oldest = Long.MAX_VALUE;

      for (int index = 0; index < EVENTS.size(); index++) {
         TankerVfxRenderer.ActiveEvent event = EVENTS.get(index);
         if (event.sequence < oldest) {
            oldest = event.sequence;
            candidate = index;
         }
      }

      return candidate;
   }

   private static int oldestMatching(boolean allowEssential, boolean allowProtectedBoundary) {
      int candidate = -1;
      long oldest = Long.MAX_VALUE;

      for (int index = 0; index < EVENTS.size(); index++) {
         TankerVfxRenderer.ActiveEvent event = EVENTS.get(index);
         if ((allowEssential || !event.essential()) && (allowProtectedBoundary || !event.protectedBoundary()) && event.sequence < oldest) {
            oldest = event.sequence;
            candidate = index;
         }
      }

      return candidate;
   }

   private static void playInitialSound(Minecraft minecraft, TankerVfxEventMessage message) {
      TankerVfxRenderer.SoundProfile profile = soundFor(message);
      if (profile != null && minecraft.level != null) {
         minecraft.level
            .playLocalSound(message.originX, message.originY, message.originZ, profile.sound, SoundSource.PLAYERS, profile.volume, profile.pitch, false);
      }
   }

   private static TankerVfxRenderer.SoundProfile soundFor(TankerVfxEventMessage message) {
      return switch (message.eventType) {
         case 0 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ARMOR_EQUIP_IRON, 0.58F, 0.72F);
         case 1 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ANVIL_LAND, 0.95F, 0.68F);
         case 2 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ANVIL_PLACE, 0.72F, 0.74F);
         case 3 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ARMOR_EQUIP_IRON, 0.52F, 0.84F);
         case 4 -> new TankerVfxRenderer.SoundProfile(SoundEvents.SHIELD_BLOCK, 0.88F, 0.78F);
         case 5 -> new TankerVfxRenderer.SoundProfile(SoundEvents.IRON_GOLEM_REPAIR, 0.48F, 1.32F);
         case 6 -> new TankerVfxRenderer.SoundProfile(SoundEvents.IRON_DOOR_CLOSE, 0.68F, 0.72F);
         case 7 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ANVIL_USE, 0.78F, 1.08F);
         case 8 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ARMOR_EQUIP_IRON, 0.48F, 0.74F);
         case 9 -> new TankerVfxRenderer.SoundProfile(SoundEvents.IRON_DOOR_OPEN, 0.4F, 0.92F);
         case 10 -> new TankerVfxRenderer.SoundProfile(SoundEvents.CHAIN_PLACE, 0.52F, 0.72F);
         case 11 -> new TankerVfxRenderer.SoundProfile(SoundEvents.IRON_GOLEM_DAMAGE, 0.42F, 0.78F + message.intensity / 1024.0F);
         case 12 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ANVIL_LAND, 0.46F, 0.7F + message.intensity / 850.0F);
         case 13 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ANVIL_DESTROY, 0.82F, 0.74F);
         case 14 -> new TankerVfxRenderer.SoundProfile(SoundEvents.BEACON_ACTIVATE, 0.68F, 0.72F);
         case 15 -> new TankerVfxRenderer.SoundProfile(SoundEvents.SHIELD_BLOCK, 0.34F, 1.08F);
         case 16 -> new TankerVfxRenderer.SoundProfile(SoundEvents.ANVIL_DESTROY, 0.78F, 0.68F);
         case 17 -> new TankerVfxRenderer.SoundProfile(SoundEvents.BEACON_DEACTIVATE, 0.5F, 0.88F);
         default -> null;
      };
   }

   private static void playAmbientCues(Minecraft minecraft, List<TankerVfxRenderer.VisibleEvent> visible, TankerVfxRenderer.Quality quality) {
      if (quality != TankerVfxRenderer.Quality.OFF && minecraft.level != null) {
         for (TankerVfxRenderer.VisibleEvent visual : visible) {
            TankerVfxRenderer.ActiveEvent event = visual.event;
            if (event.type() == 10 && !event.hasFlag(16)) {
               float elapsed = event.elapsed(minecraft.level.getGameTime(), 0.0F);
               int cue = elapsed < 30.0F ? -1 : Math.min(3, (int)((elapsed - 30.0F) / 40.0F));
               if (cue > event.lastAmbientCue) {
                  event.lastAmbientCue = cue;
                  minecraft.level
                     .playLocalSound(
                        visual.position.x,
                        visual.position.y + 0.8,
                        visual.position.z,
                        SoundEvents.WARDEN_HEARTBEAT,
                        SoundSource.PLAYERS,
                        0.16F,
                        0.82F + cue * 0.035F,
                        false
                     );
               }
            }
         }
      }
   }

   private static final class ActiveEvent {
      private final TankerVfxEventMessage message;
      private final long sequence;
      private int intensity;
      private int lastAmbientCue;

      private ActiveEvent(TankerVfxEventMessage message, long sequence, long now) {
         this.message = message;
         this.sequence = sequence;
         this.intensity = message.intensity;
         float elapsed = Math.max(0.0F, (float)(now - message.serverStartTick));
         this.lastAmbientCue = message.hasFlag(8) && elapsed >= 30.0F ? Math.min(3, (int)((elapsed - 30.0F) / 40.0F)) : -1;
      }

      private byte type() {
         return this.message.eventType;
      }

      private int ownerId() {
         return this.message.ownerEntityId;
      }

      private Vec3 origin() {
         return this.message.origin();
      }

      private float yaw() {
         return this.message.yawDegrees();
      }

      private float pitch() {
         return this.message.pitchDegrees();
      }

      private long startTick() {
         return this.message.serverStartTick;
      }

      private int duration() {
         return this.message.duration;
      }

      private int seed() {
         return this.message.seed;
      }

      private boolean hasFlag(int flag) {
         return this.message.hasFlag(flag);
      }

      private float elapsed(long now, float partialTick) {
         return (float)(now - this.message.serverStartTick) + partialTick;
      }

      private boolean expired(long now) {
         return now - this.message.serverStartTick >= this.message.duration;
      }

      private boolean essential() {
         if (this.message.hasFlag(1)) {
            return true;
         }

         return switch (this.message.eventType) {
            case 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 12, 13, 14, 16, 17 -> true;
            default -> false;
         };
      }

      private boolean protectedBoundary() {
         return switch (this.message.eventType) {
            case 2, 6, 8, 10, 14 -> true;
            default -> false;
         };
      }
   }

   private record Anchor(Vec3 position, float yaw, float pitch, boolean firstPersonOwner) {
   }

   private static final class FrameBudget {
      private final int limit;
      private int used;

      private FrameBudget(int limit) {
         this.limit = limit;
      }

      private boolean reserve(int vertices) {
         if (vertices > 0 && this.used + vertices <= this.limit) {
            this.used += vertices;
            return true;
         } else {
            return false;
         }
      }

      private int remaining() {
         return this.limit - this.used;
      }
   }

   private enum Pass {
      SURFACE,
      EMISSIVE;
   }

   private enum Quality {
      FULL(64.0, 32, 24000, 32, 24),
      LOW(40.0, 16, 8000, 12, 8),
      OFF(32.0, 8, 2000, 8, 0);

      private final double renderDistance;
      private final int maxVisible;
      private final int maxVertices;
      private final int ringSegments;
      private final int burstParticles;

      Quality(double renderDistance, int maxVisible, int maxVertices, int ringSegments, int burstParticles) {
         this.renderDistance = renderDistance;
         this.maxVisible = maxVisible;
         this.maxVertices = maxVertices;
         this.ringSegments = ringSegments;
         this.burstParticles = burstParticles;
      }

      private static TankerVfxRenderer.Quality current(Minecraft minecraft) {
         ParticleStatus status = minecraft.options.particles().get();
         if (status == ParticleStatus.MINIMAL) {
            return OFF;
         } else {
            return status == ParticleStatus.DECREASED ? LOW : FULL;
         }
      }

      private boolean allowsEvent(TankerVfxRenderer.ActiveEvent event) {
         return this != OFF || event.essential();
      }

      private boolean allowsGlow(TankerVfxRenderer.ActiveEvent event) {
         return this == FULL || this == LOW && event.essential();
      }
   }

   private record RenderFrame(PoseStack poseStack, Vec3 cameraPosition, Quaternionf billboard, float partialTick, TankerVfxRenderer.Quality quality) {
   }

   private record SoundProfile(SoundEvent sound, float volume, float pitch) {
   }

   private record VisibleEvent(
      TankerVfxRenderer.ActiveEvent event, Vec3 position, float yaw, float pitch, float elapsed, double distanceSqr, int packedLight, boolean firstPersonOwner
   ) {
   }
}
