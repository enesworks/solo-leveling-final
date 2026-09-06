package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class DeferredWorldShaderRenderer {
   private static final int MAX_BATCHES = 4096;
   private static final int MAX_VERTICES = 1000000;
   private static final int MAX_POOLED_BATCHES = 128;
   private static final int INITIAL_VERTEX_CAPACITY = 32;
   private static final int MAX_RETAINED_VERTEX_CAPACITY = 16384;
   private static final List<DeferredWorldShaderRenderer.CapturedBatch> CAPTURED_BATCHES = new ArrayList<>();
   private static final Deque<DeferredWorldShaderRenderer.CapturedBatch> BATCH_POOL = new ArrayDeque<>();
   private static final VertexConsumer DISCARDING_CONSUMER = new DeferredWorldShaderRenderer.DiscardingVertexConsumer();
   private static TextureTarget finalDepthSnapshot;
   private static Matrix4f worldModelView;
   private static Matrix4f worldPose;
   private static Matrix3f worldNormal;
   private static Matrix4f previousProjection;
   private static VertexSorting previousVertexSorting;
   private static boolean modelViewPushed;
   private static boolean depthRequested;
   private static boolean depthCapturedThisFrame;
   private static boolean depthRestoredThisFrame;
   private static int capturedVertexCount;

   private DeferredWorldShaderRenderer() {
   }

   public static VertexConsumer buffer(MultiBufferSource originalBuffers, RenderType renderType) {
      return buffer(originalBuffers, renderType, null, true);
   }

   public static VertexConsumer buffer(MultiBufferSource originalBuffers, RenderType renderType, boolean requiresDepth) {
      return buffer(originalBuffers, renderType, null, requiresDepth);
   }

   public static VertexConsumer buffer(MultiBufferSource originalBuffers, RenderType renderType, Runnable setupUniforms) {
      return buffer(originalBuffers, renderType, setupUniforms, true);
   }

   public static VertexConsumer buffer(MultiBufferSource originalBuffers, RenderType renderType, Runnable setupUniforms, boolean requiresDepth) {
      if (!IrisCompat.isShaderPackInUse()) {
         return originalBuffers.getBuffer(renderType);
      }

      if (IrisCompat.isRenderingShadowPass()) {
         return DISCARDING_CONSUMER;
      }

      if (CAPTURED_BATCHES.size() < 4096 && capturedVertexCount < 1000000) {
         DeferredWorldShaderRenderer.CapturedBatch batch = lastCompatibleBatch(renderType, setupUniforms);
         if (batch == null) {
            batch = BATCH_POOL.pollFirst();
            if (batch == null) {
               batch = new DeferredWorldShaderRenderer.CapturedBatch();
            }

            batch.prepare(renderType, setupUniforms);
            CAPTURED_BATCHES.add(batch);
         } else {
            batch.consumer.prepareForWrite();
         }

         if (requiresDepth) {
            depthRequested = true;
         }

         return batch.consumer;
      } else {
         return DISCARDING_CONSUMER;
      }
   }

   private static DeferredWorldShaderRenderer.CapturedBatch lastCompatibleBatch(RenderType renderType, Runnable setupUniforms) {
      if (CAPTURED_BATCHES.isEmpty()) {
         return null;
      }

      DeferredWorldShaderRenderer.CapturedBatch last = CAPTURED_BATCHES.get(CAPTURED_BATCHES.size() - 1);
      return last.renderType == renderType && last.setupUniforms == setupUniforms ? last : null;
   }

   public static void requestDepthAtStage(RenderLevelStageEvent event, Stage normalStage) {
      if (event.getStage() == normalStage && IrisCompat.isShaderPackInUse() && !IrisCompat.isRenderingShadowPass()) {
         depthRequested = true;
      }
   }

   public static boolean isRenderStage(RenderLevelStageEvent event, Stage normalStage) {
      Stage expected = IrisCompat.isShaderPackInUse() ? Stage.AFTER_LEVEL : normalStage;
      return event.getStage() == expected;
   }

   public static boolean beginWorldPass(RenderLevelStageEvent event) {
      if (!IrisCompat.isShaderPackInUse()) {
         return true;
      }

      if (!IrisCompat.isRenderingShadowPass() && !modelViewPushed) {
         bindFinalTarget();
         previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
         previousVertexSorting = RenderSystem.getVertexSorting();
         PoseStack modelView = RenderSystem.getModelViewStack();
         modelView.pushPose();
         modelViewPushed = true;

         try {
            modelView.setIdentity();
            if (worldModelView != null) {
               modelView.mulPoseMatrix(worldModelView);
            }

            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f(event.getProjectionMatrix()), VertexSorting.DISTANCE_TO_ORIGIN);
            return true;
         } catch (RuntimeException exception) {
            endWorldPass();
            throw exception;
         }
      } else {
         return false;
      }
   }

   public static PoseStack worldPoseStack(RenderLevelStageEvent event) {
      if (IrisCompat.isShaderPackInUse() && event.getStage() == Stage.AFTER_LEVEL && worldPose != null) {
         PoseStack copy = new PoseStack();
         copy.last().pose().set(worldPose);
         if (worldNormal != null) {
            copy.last().normal().set(worldNormal);
         }

         return copy;
      } else {
         return event.getPoseStack();
      }
   }

   public static void endWorldPass() {
      if (modelViewPushed) {
         if (previousProjection != null && previousVertexSorting != null) {
            RenderSystem.setProjectionMatrix(previousProjection, previousVertexSorting);
         }

         PoseStack modelView = RenderSystem.getModelViewStack();
         modelView.popPose();
         RenderSystem.applyModelViewMatrix();
         previousProjection = null;
         previousVertexSorting = null;
         modelViewPushed = false;
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void captureMainCameraMatrices(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY && IrisCompat.isShaderPackInUse() && !IrisCompat.isRenderingShadowPass()) {
         worldModelView = new Matrix4f(RenderSystem.getModelViewMatrix());
         worldPose = new Matrix4f(event.getPoseStack().last().pose());
         worldNormal = new Matrix3f(event.getPoseStack().last().normal());
         depthCapturedThisFrame = false;
         depthRestoredThisFrame = false;
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void captureFinalDepth(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_LEVEL && depthRequested && IrisCompat.isShaderPackInUse() && !IrisCompat.isRenderingShadowPass()) {
         Minecraft minecraft = Minecraft.getInstance();
         RenderTarget mainTarget = minecraft.getMainRenderTarget();
         if (mainTarget != null && mainTarget.useDepth) {
            if (finalDepthSnapshot == null || finalDepthSnapshot.width != mainTarget.width || finalDepthSnapshot.height != mainTarget.height) {
               releaseDepthSnapshot();
               finalDepthSnapshot = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
            }

            finalDepthSnapshot.copyDepthFrom(mainTarget);
            mainTarget.bindWrite(false);
            depthCapturedThisFrame = true;
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void flushCapturedWorldQuads(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_LEVEL) {
         try {
            if (!IrisCompat.isShaderPackInUse()) {
               clearCapturedBatches();
               return;
            }

            if (CAPTURED_BATCHES.isEmpty() || IrisCompat.isRenderingShadowPass()) {
               return;
            }

            if (beginWorldPass(event)) {
               BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

               try {
                  for (DeferredWorldShaderRenderer.CapturedBatch batch : CAPTURED_BATCHES) {
                     if (batch.setupUniforms != null) {
                        batch.setupUniforms.run();
                     }

                     VertexConsumer output = buffers.getBuffer(batch.renderType);
                     batch.replay(output);
                     buffers.endBatch(batch.renderType);
                  }

                  return;
               } finally {
                  clearCapturedBatches();
                  endWorldPass();
               }
            }

            clearCapturedBatches();
         } finally {
            depthRequested = false;
            depthCapturedThisFrame = false;
            depthRestoredThisFrame = false;
         }
      }
   }

   @SubscribeEvent
   public static void clearWhenWorldCloses(ClientTickEvent event) {
      if (event.phase == Phase.END) {
         if (Minecraft.getInstance().level == null || !IrisCompat.isShaderPackInUse()) {
            clearCapturedBatches();
            releaseDepthSnapshot();
            depthRequested = false;
            depthCapturedThisFrame = false;
            depthRestoredThisFrame = false;
            worldModelView = null;
            worldPose = null;
            worldNormal = null;
         }
      }
   }

   private static void bindFinalTarget() {
      RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
      if (mainTarget != null) {
         if (depthCapturedThisFrame
            && !depthRestoredThisFrame
            && finalDepthSnapshot != null
            && finalDepthSnapshot.width == mainTarget.width
            && finalDepthSnapshot.height == mainTarget.height) {
            mainTarget.copyDepthFrom(finalDepthSnapshot);
            depthRestoredThisFrame = true;
         }

         mainTarget.bindWrite(false);
      }
   }

   private static void releaseDepthSnapshot() {
      if (finalDepthSnapshot != null) {
         finalDepthSnapshot.destroyBuffers();
         finalDepthSnapshot = null;
      }
   }

   private static void clearCapturedBatches() {
      for (DeferredWorldShaderRenderer.CapturedBatch batch : CAPTURED_BATCHES) {
         batch.clear();
         if (BATCH_POOL.size() < 128) {
            BATCH_POOL.addLast(batch);
         }
      }

      CAPTURED_BATCHES.clear();
      capturedVertexCount = 0;
      if (Minecraft.getInstance().level == null || !IrisCompat.isShaderPackInUse()) {
         worldModelView = null;
         worldPose = null;
         worldNormal = null;
      }
   }

   private static int packColor(int red, int green, int blue, int alpha) {
      return (red & 0xFF) << 24 | (green & 0xFF) << 16 | (blue & 0xFF) << 8 | alpha & 0xFF;
   }

   private static int packPair(int low, int high) {
      return low & 65535 | (high & 65535) << 16;
   }

   private static int unpackLow(int packed) {
      return packed & 65535;
   }

   private static int unpackHigh(int packed) {
      return packed >>> 16 & 65535;
   }

   private static final class CapturedBatch {
      private RenderType renderType;
      private Runnable setupUniforms;
      private int vertexCount;
      private float[] positions = new float[96];
      private int[] colors = new int[32];
      private float[] uvs = new float[64];
      private int[] overlays = new int[32];
      private int[] lights = new int[32];
      private float[] normals = new float[96];
      private final DeferredWorldShaderRenderer.CapturingVertexConsumer consumer = new DeferredWorldShaderRenderer.CapturingVertexConsumer(this);

      private void prepare(RenderType renderType, Runnable setupUniforms) {
         this.renderType = renderType;
         this.setupUniforms = setupUniforms;
         this.vertexCount = 0;
         this.consumer.prepareForWrite();
      }

      private int add(float x, float y, float z, int color) {
         this.ensureCapacity(this.vertexCount + 1);
         int index = this.vertexCount++;
         int positionOffset = index * 3;
         this.positions[positionOffset] = x;
         this.positions[positionOffset + 1] = y;
         this.positions[positionOffset + 2] = z;
         this.colors[index] = color;
         int uvOffset = index * 2;
         this.uvs[uvOffset] = 0.0F;
         this.uvs[uvOffset + 1] = 0.0F;
         this.overlays[index] = 0;
         this.lights[index] = DeferredWorldShaderRenderer.packPair(240, 240);
         this.normals[positionOffset] = 0.0F;
         this.normals[positionOffset + 1] = 1.0F;
         this.normals[positionOffset + 2] = 0.0F;
         return index;
      }

      private void ensureCapacity(int required) {
         if (required > this.colors.length) {
            int capacity = Math.max(required, this.colors.length * 2);
            this.positions = Arrays.copyOf(this.positions, capacity * 3);
            this.colors = Arrays.copyOf(this.colors, capacity);
            this.uvs = Arrays.copyOf(this.uvs, capacity * 2);
            this.overlays = Arrays.copyOf(this.overlays, capacity);
            this.lights = Arrays.copyOf(this.lights, capacity);
            this.normals = Arrays.copyOf(this.normals, capacity * 3);
         }
      }

      private void replay(VertexConsumer output) {
         for (int index = 0; index < this.vertexCount; index++) {
            int positionOffset = index * 3;
            int uvOffset = index * 2;
            int color = this.colors[index];
            int overlay = this.overlays[index];
            int light = this.lights[index];
            output.vertex(this.positions[positionOffset], this.positions[positionOffset + 1], this.positions[positionOffset + 2])
               .color(color >>> 24 & 0xFF, color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF)
               .uv(this.uvs[uvOffset], this.uvs[uvOffset + 1])
               .overlayCoords(DeferredWorldShaderRenderer.unpackLow(overlay), DeferredWorldShaderRenderer.unpackHigh(overlay))
               .uv2(DeferredWorldShaderRenderer.unpackLow(light), DeferredWorldShaderRenderer.unpackHigh(light))
               .normal(this.normals[positionOffset], this.normals[positionOffset + 1], this.normals[positionOffset + 2])
               .endVertex();
         }
      }

      private void clear() {
         this.renderType = null;
         this.setupUniforms = null;
         this.vertexCount = 0;
         this.consumer.prepareForWrite();
         if (this.colors.length > 16384) {
            this.positions = new float[96];
            this.colors = new int[32];
            this.uvs = new float[64];
            this.overlays = new int[32];
            this.lights = new int[32];
            this.normals = new float[96];
         }
      }
   }

   private static final class CapturingVertexConsumer implements VertexConsumer {
      private final DeferredWorldShaderRenderer.CapturedBatch batch;
      private int currentIndex = -1;
      private boolean hasDefaultColor;
      private int defaultRed = 255;
      private int defaultGreen = 255;
      private int defaultBlue = 255;
      private int defaultAlpha = 255;

      private CapturingVertexConsumer(DeferredWorldShaderRenderer.CapturedBatch batch) {
         this.batch = batch;
      }

      private void prepareForWrite() {
         this.currentIndex = -1;
         this.hasDefaultColor = false;
         this.defaultRed = 255;
         this.defaultGreen = 255;
         this.defaultBlue = 255;
         this.defaultAlpha = 255;
      }

      @Override
      public VertexConsumer vertex(double x, double y, double z) {
         if (DeferredWorldShaderRenderer.capturedVertexCount >= 1000000) {
            this.currentIndex = -1;
            return this;
         } else {
            int color = DeferredWorldShaderRenderer.packColor(
               this.hasDefaultColor ? this.defaultRed : 255,
               this.hasDefaultColor ? this.defaultGreen : 255,
               this.hasDefaultColor ? this.defaultBlue : 255,
               this.hasDefaultColor ? this.defaultAlpha : 255
            );
            this.currentIndex = this.batch.add((float)x, (float)y, (float)z, color);
            DeferredWorldShaderRenderer.capturedVertexCount++;
            return this;
         }
      }

      @Override
      public VertexConsumer color(int red, int green, int blue, int alpha) {
         if (this.currentIndex >= 0) {
            this.batch.colors[this.currentIndex] = DeferredWorldShaderRenderer.packColor(red, green, blue, alpha);
         }

         return this;
      }

      @Override
      public VertexConsumer uv(float u, float v) {
         if (this.currentIndex >= 0) {
            int offset = this.currentIndex * 2;
            this.batch.uvs[offset] = u;
            this.batch.uvs[offset + 1] = v;
         }

         return this;
      }

      @Override
      public VertexConsumer overlayCoords(int u, int v) {
         if (this.currentIndex >= 0) {
            this.batch.overlays[this.currentIndex] = DeferredWorldShaderRenderer.packPair(u, v);
         }

         return this;
      }

      @Override
      public VertexConsumer uv2(int u, int v) {
         if (this.currentIndex >= 0) {
            this.batch.lights[this.currentIndex] = DeferredWorldShaderRenderer.packPair(u, v);
         }

         return this;
      }

      @Override
      public VertexConsumer normal(float x, float y, float z) {
         if (this.currentIndex >= 0) {
            int offset = this.currentIndex * 3;
            this.batch.normals[offset] = x;
            this.batch.normals[offset + 1] = y;
            this.batch.normals[offset + 2] = z;
         }

         return this;
      }

      @Override
      public void endVertex() {
         this.currentIndex = -1;
      }

      @Override
      public void defaultColor(int red, int green, int blue, int alpha) {
         this.hasDefaultColor = true;
         this.defaultRed = red;
         this.defaultGreen = green;
         this.defaultBlue = blue;
         this.defaultAlpha = alpha;
      }

      @Override
      public void unsetDefaultColor() {
         this.hasDefaultColor = false;
      }
   }

   private static final class DiscardingVertexConsumer implements VertexConsumer {
      @Override
      public VertexConsumer vertex(double x, double y, double z) {
         return this;
      }

      @Override
      public VertexConsumer color(int red, int green, int blue, int alpha) {
         return this;
      }

      @Override
      public VertexConsumer uv(float u, float v) {
         return this;
      }

      @Override
      public VertexConsumer overlayCoords(int u, int v) {
         return this;
      }

      @Override
      public VertexConsumer uv2(int u, int v) {
         return this;
      }

      @Override
      public VertexConsumer normal(float x, float y, float z) {
         return this;
      }

      @Override
      public void endVertex() {
      }

      @Override
      public void defaultColor(int red, int green, int blue, int alpha) {
      }

      @Override
      public void unsetDefaultColor() {
      }
   }
}
