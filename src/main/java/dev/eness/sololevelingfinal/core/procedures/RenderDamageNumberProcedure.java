package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.SystemClientConfig;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(Dist.CLIENT)
public class RenderDamageNumberProcedure {
   private static RenderLevelStageEvent provider = null;
   private static Map<EntityType, Entity> data = new HashMap<>();
   private static float textWidth = 1.0F;
   private static float textHeight = 1.0F;
   private static int textColor = -1;
   private static int backColor = 0;
   private static final List<RenderDamageNumberProcedure.DamageNumber> DAMAGE_NUMBERS = new ArrayList<>();
   private static final long DAMAGE_NUMBER_LIFETIME_MS = 1050L;

   public static void addNumber(double x, double y, double z, float amount, int color) {
      if (SystemClientConfig.isDamageNumbersEnabled()) {
         DAMAGE_NUMBERS.add(new RenderDamageNumberProcedure.DamageNumber(x, y, z, amount, color));
         if (DAMAGE_NUMBERS.size() > 80) {
            DAMAGE_NUMBERS.remove(0);
         }
      }
   }

   public static void setBackColor(int color) {
      backColor = color;
   }

   public static void setTextColor(int color) {
      textColor = color;
   }

   public static void setScale(float width, float height) {
      textWidth = width;
      textHeight = height;
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      DAMAGE_NUMBERS.clear();
      data.clear();
      provider = null;
   }

   public static void renderBlock(BlockState blockState, double x, double y, double z, float yaw, float pitch, float roll, float scale, boolean glowing) {
      BlockPos blockPos = BlockPos.containing(x, y, z);
      Vec3 pos = provider.getCamera().getPosition();
      int packedLight = glowing ? 15728880 : LevelRenderer.getLightColor(Minecraft.getInstance().level, blockPos);
      PoseStack poseStack = provider.getPoseStack();
      poseStack.pushPose();
      poseStack.translate(x - pos.x(), y - pos.y(), z - pos.z());
      poseStack.mulPose(Axis.YN.rotationDegrees(yaw));
      poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
      poseStack.mulPose(Axis.ZN.rotationDegrees(roll));
      poseStack.scale(scale, scale, scale);
      poseStack.translate(-0.5F, -0.5F, -0.5F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      renderBlockModel(blockState, blockPos, poseStack, packedLight);
      renderBlockEntity(blockState, blockPos, poseStack, packedLight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      poseStack.popPose();
   }

   private static void renderBlockEntity(BlockState blockState, BlockPos blockPos, PoseStack poseStack, int packedLight) {
      if (blockState.getBlock() instanceof EntityBlock entityBlock) {
         Minecraft minecraft = Minecraft.getInstance();
         ClientLevel level = minecraft.level;
         BlockEntity blockEntity = entityBlock.newBlockEntity(blockPos, blockState);
         if (blockEntity != null) {
            BlockEntityRenderer blockEntityRenderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (blockEntityRenderer != null) {
               BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
               blockEntity.setLevel(level);
               blockEntityRenderer.render(blockEntity, 0.0F, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            }
         }
      }
   }

   private static void renderBlockModel(BlockState blockState, BlockPos blockPos, PoseStack poseStack, int packedLight) {
      if (blockState.getRenderShape() == RenderShape.MODEL) {
         Minecraft minecraft = Minecraft.getInstance();
         ClientLevel level = minecraft.level;
         BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
         BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
         ModelBlockRenderer renderer = dispatcher.getModelRenderer();
         BakedModel bakedModel = dispatcher.getBlockModel(blockState);
         ModelData modelData = bakedModel.getModelData(level, blockPos, blockState, ModelData.builder().build());
         Pose pose = poseStack.last();
         int color = minecraft.getBlockColors().getColor(blockState, level, blockPos);
         float red = (color >> 16 & 0xFF) / 255.0F;
         float green = (color >> 8 & 0xFF) / 255.0F;
         float blue = (color & 0xFF) / 255.0F;

         for (RenderType renderType : bakedModel.getRenderTypes(blockState, RandomSource.create(42L), modelData)) {
            renderer.renderModel(
               pose,
               bufferSource.getBuffer(Sheets.translucentCullBlockSheet()),
               blockState,
               bakedModel,
               red,
               green,
               blue,
               packedLight,
               OverlayTexture.NO_OVERLAY,
               modelData,
               renderType
            );
         }
      }
   }

   public static void renderEntity(EntityType type, double x, double y, double z, float yaw, float pitch, float roll, float scale, boolean glowing) {
      if (type != null) {
         ClientLevel level = Minecraft.getInstance().level;
         Entity entity;
         if (data.containsKey(type)) {
            entity = data.get(type);
            if (entity.level() != level) {
               entity = type.create(level);
               data.put(type, entity);
            }
         } else {
            entity = type.create(level);
            data.put(type, entity);
         }

         renderEntity(entity, 0.0F, x, y, z, yaw, pitch, roll, scale, glowing ? 15728880 : LevelRenderer.getLightColor(level, BlockPos.containing(x, y, z)));
      }
   }

   public static void renderEntity(Entity entity, double x, double y, double z, float yaw, float pitch, float roll, float scale, boolean glowing) {
      float partialTick = provider.getPartialTick();
      int packedLight = glowing ? 15728880 : Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(entity, partialTick);
      renderEntity(entity, partialTick, x, y, z, yaw, pitch, roll, scale, packedLight);
   }

   private static void renderEntity(
      Entity entity, float partialTick, double x, double y, double z, float yaw, float pitch, float roll, float scale, int packedLight
   ) {
      if (entity != null) {
         Minecraft minecraft = Minecraft.getInstance();
         BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
         EntityRenderer renderer = minecraft.getEntityRenderDispatcher().getRenderer(entity);
         Vec3 pos = provider.getCamera().getPosition();
         float offset = entity.getBbHeight() / 2.0F * scale;
         PoseStack poseStack = provider.getPoseStack();
         poseStack.pushPose();
         poseStack.translate(x - pos.x(), y + offset - pos.y(), z - pos.z());
         poseStack.mulPose(Axis.YN.rotationDegrees(yaw));
         poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
         poseStack.mulPose(Axis.ZN.rotationDegrees(roll));
         poseStack.translate(0.0F, -offset, 0.0F);
         poseStack.scale(scale, scale, scale);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         renderer.render(entity, entity.getViewYRot(partialTick), partialTick, poseStack, bufferSource, packedLight);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         poseStack.popPose();
      }
   }

   public static void renderItem(
      ItemStack itemStack, double x, double y, double z, float yaw, float pitch, float roll, float scale, boolean flipping, boolean glowing
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
      ItemRenderer renderer = minecraft.getItemRenderer();
      Vec3 pos = provider.getCamera().getPosition();
      int packedLight = glowing ? 15728880 : LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(x, y, z));
      PoseStack poseStack = provider.getPoseStack();
      poseStack.pushPose();
      poseStack.translate(x - pos.x(), y - pos.y(), z - pos.z());
      poseStack.mulPose(Axis.YN.rotationDegrees(yaw));
      poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
      poseStack.mulPose(Axis.ZN.rotationDegrees(roll));
      poseStack.scale(scale, scale, scale);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      renderer.renderStatic(
         null, itemStack, ItemDisplayContext.FIXED, flipping, poseStack, bufferSource, minecraft.level, packedLight, OverlayTexture.NO_OVERLAY, 0
      );
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      poseStack.popPose();
   }

   public static void renderLine(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
      Vec3 pos = provider.getCamera().getPosition();
      Vector3f normal = new Vec3(x2 - x1, y2 - y1, z2 - z1).normalize().toVector3f();
      Matrix4f matrix4f = provider.getPoseStack().last().pose();
      VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
      vertexConsumer.vertex(matrix4f, (float)(x1 - pos.x()), (float)(y1 - pos.y()), (float)(z1 - pos.z()))
         .color(color)
         .normal(normal.x(), normal.y(), normal.z())
         .endVertex();
      vertexConsumer.vertex(matrix4f, (float)(x2 - pos.x()), (float)(y2 - pos.y()), (float)(z2 - pos.z()))
         .color(color)
         .normal(normal.x(), normal.y(), normal.z())
         .endVertex();
   }

   public static void renderTexts(String texts, double x, double y, double z, float yaw, float pitch, float roll, boolean glowing) {
      Minecraft minecraft = Minecraft.getInstance();
      BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
      Font font = minecraft.font;
      Vec3 pos = provider.getCamera().getPosition();
      int packedLight = glowing ? 15728880 : LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(x, y, z));
      PoseStack poseStack = provider.getPoseStack();
      poseStack.pushPose();
      poseStack.translate(x - pos.x(), y - pos.y(), z - pos.z());
      poseStack.mulPose(Axis.YN.rotationDegrees(yaw));
      poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
      poseStack.mulPose(Axis.ZN.rotationDegrees(roll));
      poseStack.scale(textWidth, -textHeight, 1.0F);
      poseStack.translate((font.width(texts) - 1) * -0.5F, (9 - 1) * -0.5F, 0.0F);
      Matrix4f matrix4f = poseStack.last().pose();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (backColor != 0) {
         font.drawInBatch(texts, 0.0F, 0.0F, 0, false, matrix4f, bufferSource, DisplayMode.SEE_THROUGH, backColor, packedLight);
      }

      font.drawInBatch(texts, 0.0F, 0.0F, textColor, false, matrix4f, bufferSource, DisplayMode.NORMAL, 0, packedLight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      poseStack.popPose();
   }

   @SubscribeEvent
   public static void renderModels(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_ENTITIES) {
         if (!SystemClientConfig.isDamageNumbersEnabled()) {
            DAMAGE_NUMBERS.clear();
         } else if (!DAMAGE_NUMBERS.isEmpty()) {
            provider = event;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            execute(provider);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
         }
      }
   }

   public static void execute() {
      execute(null);
   }

   private static void execute(@Nullable Event event) {
      if (provider != null) {
         if (!SystemClientConfig.isDamageNumbersEnabled()) {
            DAMAGE_NUMBERS.clear();
         } else {
            renderDamageNumbers();
         }
      }
   }

   private static void renderDamageNumbers() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level != null && minecraft.player != null) {
         long now = System.currentTimeMillis();
         BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
         Vec3 cameraPos = provider.getCamera().getPosition();
         PoseStack poseStack = provider.getPoseStack();
         Font font = minecraft.font;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         Iterator<RenderDamageNumberProcedure.DamageNumber> iterator = DAMAGE_NUMBERS.iterator();

         while (iterator.hasNext()) {
            RenderDamageNumberProcedure.DamageNumber number = iterator.next();
            float age = (float)(now - number.createdAt) / 1050.0F;
            if (age >= 1.0F) {
               iterator.remove();
            } else {
               float rise = age * 0.85F;
               float alpha = 1.0F - age;
               String text = number.text;
               int color = applyAlpha(number.color, alpha);
               int shadow = applyAlpha(-16644076, alpha * 0.72F);
               poseStack.pushPose();
               poseStack.translate(number.x + number.offsetX - cameraPos.x(), number.y + rise - cameraPos.y(), number.z + number.offsetZ - cameraPos.z());
               poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
               float scale = 0.026F + age * 0.006F;
               poseStack.scale(-scale, -scale, scale);
               float width = font.width(text) / 2.0F;
               Matrix4f matrix = poseStack.last().pose();
               font.drawInBatch(text, -width + 1.0F, 1.0F, shadow, false, matrix, bufferSource, DisplayMode.SEE_THROUGH, 0, 15728880);
               font.drawInBatch(text, -width, 0.0F, color, false, matrix, bufferSource, DisplayMode.SEE_THROUGH, 0, 15728880);
               poseStack.popPose();
            }
         }

         bufferSource.endBatch();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   private static String formatDamage(float amount) {
      return amount < 10.0F && amount != Math.round(amount) ? String.format(Locale.US, "%.1f", amount) : Integer.toString(Math.round(amount));
   }

   private static int applyAlpha(int color, float alpha) {
      int a = Math.max(0, Math.min(255, Math.round((color >>> 24 & 0xFF) * alpha)));
      return color & 16777215 | a << 24;
   }

   private static class DamageNumber {
      final double x;
      final double y;
      final double z;
      final double offsetX;
      final double offsetZ;
      final String text;
      final int color;
      final long createdAt;

      DamageNumber(double x, double y, double z, float amount, int color) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.text = RenderDamageNumberProcedure.formatDamage(amount);
         this.color = color;
         this.createdAt = System.currentTimeMillis();
         this.offsetX = (Math.random() - 0.5) * 0.55;
         this.offsetZ = (Math.random() - 0.5) * 0.55;
      }
   }
}
