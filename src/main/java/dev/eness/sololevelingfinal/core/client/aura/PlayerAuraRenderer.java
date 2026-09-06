package dev.eness.sololevelingfinal.core.client.aura;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent.Post;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.client.renderer.shader.DeferredWorldShaderRenderer;
import dev.eness.sololevelingfinal.core.client.renderer.shader.PlayerAuraRenderTypes;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.util.LiuManifestationManager;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "sololeveling", value = Dist.CLIENT)
public final class PlayerAuraRenderer {
   private static final int SHELL_SEGMENTS = 16;
   private static final int RING_SEGMENTS = 24;

   private PlayerAuraRenderer() {
   }

   @SubscribeEvent
   public static void renderPlayerAura(Post event) {
      Player player = event.getEntity();
      Minecraft minecraft = Minecraft.getInstance();
      if (!shouldSkipAuraRendering(player, minecraft)) {
         List<ClientPlayerAuraManager.AuraInstance> active = collectActiveAuras(player);
         if (active.isEmpty()) {
            ClientPlayerAuraManager.clearTrail(player.getId());
         } else {
            long gameTime = minecraft.level.getGameTime();
            Vec3 currentPosition = player.getPosition(event.getPartialTick());
            boolean fluidAuraActive = active.stream()
               .map(instancex -> PlayerAuraRegistry.get(instancex.auraId()))
               .anyMatch(definitionx -> definitionx != null && definitionx.fluid() != null && !isShadowRift(definitionx));
            if (fluidAuraActive) {
               ClientPlayerAuraManager.recordTrail(player.getId(), currentPosition);
            } else {
               ClientPlayerAuraManager.clearTrail(player.getId());
            }

            for (ClientPlayerAuraManager.AuraInstance instance : active) {
               PlayerAuraDefinition definition = PlayerAuraRegistry.get(instance.auraId());
               if (definition != null) {
                  if (definition.fluid() != null && !isShadowRift(definition)) {
                     renderFluidTrail(event, definition, instance, currentPosition, gameTime, minecraft);
                  }

                  renderInstance(event, definition, instance, gameTime, minecraft);
               }
            }
         }
      }
   }

   private static boolean shouldSkipAuraRendering(Player player, Minecraft minecraft) {
      return !player.isSpectator() && minecraft.level != null ? player == minecraft.player && minecraft.options.getCameraType().isFirstPerson() : true;
   }

   public static List<ClientPlayerAuraManager.AuraInstance> activeAuras(Player player) {
      return collectActiveAuras(player);
   }

   private static List<ClientPlayerAuraManager.AuraInstance> collectActiveAuras(Player player) {
      List<ClientPlayerAuraManager.AuraInstance> active = ClientPlayerAuraManager.activeFor(player.getId());
      if (wearsFullGoliathSet(player)) {
         addEquipmentAura(active, PlayerAuraRegistry.GOLIATH, player);
      }

      if (wearsFullShadowMonarchSet(player)) {
         addEquipmentAura(active, PlayerAuraRegistry.SHADOW_MONARCH_MANIFESTATION, player);
      }

      if (LiuManifestationManager.isManifestedSword(player.getMainHandItem()) && LiuManifestationManager.isManifestedSword(player.getOffhandItem())) {
         addEquipmentAura(active, PlayerAuraRegistry.LIU_DRAGON_SWORDS, player);
      }

      return active;
   }

   private static void renderFluidTrail(
      Post event, PlayerAuraDefinition definition, ClientPlayerAuraManager.AuraInstance instance, Vec3 currentPosition, long gameTime, Minecraft minecraft
   ) {
      List<ClientPlayerAuraManager.TrailPoint> trail = ClientPlayerAuraManager.trailFor(event.getEntity().getId());
      if (trail.size() >= 2) {
         float partialTick = event.getPartialTick();
         float envelope = instance.envelope(partialTick, gameTime) * instance.intensity();
         float radius = Math.max(event.getEntity().getBbWidth() * 0.5F + 0.28F, definition.radius());
         float height = Math.max(1.25F, event.getEntity().getBbHeight() * definition.heightScale());
         VertexConsumer vertices = DeferredWorldShaderRenderer.buffer(event.getMultiBufferSource(), PlayerAuraRenderTypes.aura(definition));
         PoseStack poseStack = event.getPoseStack();

         for (int i = 1; i < trail.size(); i++) {
            ClientPlayerAuraManager.TrailPoint point = trail.get(i);
            float age = (float)(gameTime - point.tick()) + partialTick;
            float life = Mth.clamp(1.0F - age / 14.0F, 0.0F, 1.0F);
            float fade = life * life * envelope;
            if (!(fade < 0.015F)) {
               Vec3 offset = point.position().subtract(currentPosition);
               poseStack.pushPose();
               poseStack.translate(offset.x, offset.y + 0.015, offset.z);
               drawTrailEcho(vertices, poseStack, definition, radius, height, event.getEntity().tickCount + partialTick - age, fade, i, minecraft);
               poseStack.popPose();
            }
         }
      }
   }

   private static boolean wearsFullGoliathSet(Player player) {
      return player.getItemBySlot(EquipmentSlot.HEAD).is(SololevelingModItems.GOLIATH_ARMOR_HELMET.get())
         && player.getItemBySlot(EquipmentSlot.CHEST).is(SololevelingModItems.GOLIATH_ARMOR_CHESTPLATE.get())
         && player.getItemBySlot(EquipmentSlot.LEGS).is(SololevelingModItems.GOLIATH_ARMOR_LEGGINGS.get())
         && player.getItemBySlot(EquipmentSlot.FEET).is(SololevelingModItems.GOLIATH_ARMOR_BOOTS.get());
   }

   private static boolean wearsFullShadowMonarchSet(Player player) {
      return player.getItemBySlot(EquipmentSlot.HEAD).is(SololevelingModItems.SHADOW_ARMOR_HELMET.get())
         && player.getItemBySlot(EquipmentSlot.CHEST).is(SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get())
         && player.getItemBySlot(EquipmentSlot.LEGS).is(SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get())
         && player.getItemBySlot(EquipmentSlot.FEET).is(SololevelingModItems.SHADOW_ARMOR_BOOTS.get());
   }

   private static void addEquipmentAura(List<ClientPlayerAuraManager.AuraInstance> active, PlayerAuraDefinition definition, Player player) {
      if (active.stream().noneMatch(instance -> definition.id().equals(instance.auraId()))) {
         active.add(new ClientPlayerAuraManager.AuraInstance(definition.id(), 0L, -1, 1.0F, player.getUUID().hashCode() ^ definition.id().hashCode()));
      }
   }

   private static boolean isShadowRift(PlayerAuraDefinition definition) {
      return definition.fluid() != null && definition.fluid().style() == PlayerAuraDefinition.FluidStyle.SHADOW_RIFT;
   }

   private static void renderInstance(
      Post event, PlayerAuraDefinition definition, ClientPlayerAuraManager.AuraInstance instance, long gameTime, Minecraft minecraft
   ) {
      float partialTick = event.getPartialTick();
      float envelope = instance.envelope(partialTick, gameTime) * instance.intensity();
      if (!(envelope <= 0.01F)) {
         Player player = event.getEntity();
         float age = player.tickCount + partialTick;
         float motion = age * definition.speed();
         float height = Math.max(1.25F, player.getBbHeight() * definition.heightScale());
         float bodyClearance = player.getBbWidth() * 0.5F + 0.28F;
         float radius = Math.max(bodyClearance, definition.radius());
         if (instance.duration() >= 0) {
            float progress = Math.min(1.0F, ((float)(gameTime - instance.startTick()) + partialTick) / instance.duration());
            radius *= 0.76F + progress * 0.52F;
         }

         PoseStack poseStack = event.getPoseStack();
         MultiBufferSource buffers = event.getMultiBufferSource();
         Vec3 lean = AuraSmokeField.lean(player.getId());
         poseStack.pushPose();
         poseStack.translate(lean.x, 0.015, lean.z);
         if (isShadowRift(definition)) {
            poseStack.popPose();
         } else {
            VertexConsumer vertices = DeferredWorldShaderRenderer.buffer(buffers, PlayerAuraRenderTypes.aura(definition));

            for (int layer = 0; layer < definition.shellLayers(); layer++) {
               float layerRadius = radius * (0.88F + layer * 0.16F);
               float rotation = motion * (1.15F + layer * 0.31F) * (layer % 2 == 0 ? 1.0F : -1.0F);
               int layerAlpha = alpha((34.0F - layer * 7.0F) * envelope);
               drawShell(vertices, poseStack, definition, layerRadius, height * (0.96F + layer * 0.04F), rotation, layerAlpha);
            }

            drawWisps(vertices, poseStack, definition, instance.seed(), radius, height, motion, envelope, minecraft);
            drawSpikes(vertices, poseStack, definition, instance.seed(), radius, height, motion, envelope, minecraft);
            if (definition.fluid() != null) {
               drawFluidField(vertices, poseStack, definition, player, radius, height, motion, envelope, partialTick, minecraft);
               if (isWhiteFlameHair(definition)) {
                  drawWhiteFlameHair(vertices, poseStack, definition, player, radius, motion, envelope, minecraft);
               }
            }

            if (definition.groundRing()) {
               drawGroundRing(vertices, poseStack, definition, radius * 1.16F, motion, alpha(74.0F * envelope));
            }

            poseStack.popPose();
         }
      }
   }

   private static boolean isWhiteFlameHair(PlayerAuraDefinition definition) {
      return definition.fluid() != null && definition.fluid().style() == PlayerAuraDefinition.FluidStyle.WHITE_FLAME_HAIR;
   }

   private static void drawWhiteFlameHair(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      Player player,
      float radius,
      float motion,
      float envelope,
      Minecraft minecraft
   ) {
      float headY = player.getBbHeight() * 0.88F;
      int tongues = 18;

      for (int i = 0; i < tongues; i++) {
         float random = hash(i * 149 + definition.id().hashCode());
         float angle = i * (float) (Math.PI * 2) / tongues + Mth.sin(motion * 0.055F + i) * 0.09F;
         float orbit = radius * (0.18F + random * 0.3F);
         float riseCycle = fract(i * 0.381966F + motion * (0.01F + i % 3 * 0.0015F));
         float width = radius * (0.16F + hash(i * 47 + 3) * 0.14F);
         float flameHeight = 0.72F + hash(i * 59 + 11) * 0.72F;
         float y = headY + riseCycle * 0.42F + Mth.sin(motion * 0.08F + i * 1.7F) * 0.04F;
         float lifeFade = (float)(Mth.smoothstep(Math.min(1.0F, riseCycle * 5.0F)) * Mth.smoothstep(Math.min(1.0F, (1.0F - riseCycle) * 4.0F)));
         int flameAlpha = alpha((138.0F + random * 58.0F) * envelope * lifeFade);
         poseStack.pushPose();
         poseStack.translate(Mth.sin(angle) * orbit, y, Mth.cos(angle) * orbit);
         applyFacing(poseStack, PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA, angle, minecraft);
         poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(motion * 0.07F + i * 2.1F) * 8.0F));
         drawFluidQuad(vertices, poseStack.last(), width, flameHeight, definition, flameAlpha, 8.0F);
         poseStack.popPose();
      }

      for (int i = 0; i < 5; i++) {
         float angle = i * (float) (Math.PI * 2) / 5.0F + motion * 0.012F;
         poseStack.pushPose();
         poseStack.translate(Mth.sin(angle) * radius * 0.3F, headY + 0.35F, Mth.cos(angle) * radius * 0.3F);
         applyFacing(poseStack, PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA, angle, minecraft);
         drawFluidQuad(vertices, poseStack.last(), radius * 0.34F, 1.45F, definition, alpha(92.0F * envelope), 10.0F);
         poseStack.popPose();
      }
   }

   private static void drawShell(
      VertexConsumer vertices, PoseStack poseStack, PlayerAuraDefinition definition, float radius, float height, float rotationDegrees, int alpha
   ) {
      poseStack.pushPose();
      poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

      for (int segment = 0; segment < 16; segment++) {
         float t0 = segment / 16.0F;
         float t1 = (segment + 1) / 16.0F;
         double a0 = (Math.PI * 2) * t0;
         double a1 = (Math.PI * 2) * t1;
         float x0 = (float)Math.cos(a0) * radius;
         float z0 = (float)Math.sin(a0) * radius;
         float x1 = (float)Math.cos(a1) * radius;
         float z1 = (float)Math.sin(a1) * radius;
         vertex(vertices, poseStack.last(), x0, 0.04F, z0, t0, 1.0F, definition.secondaryColor(), alpha);
         vertex(vertices, poseStack.last(), x1, 0.04F, z1, t1, 1.0F, definition.secondaryColor(), alpha);
         vertex(vertices, poseStack.last(), x1, height, z1, t1, 0.0F, definition.primaryColor(), 0);
         vertex(vertices, poseStack.last(), x0, height, z0, t0, 0.0F, definition.primaryColor(), 0);
      }

      poseStack.popPose();
   }

   private static void drawWisps(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      int seed,
      float radius,
      float height,
      float motion,
      float envelope,
      Minecraft minecraft
   ) {
      for (int i = 0; i < definition.wispCount(); i++) {
         float phase = fract(i * 0.618034F + motion * (0.008F + i % 3 * 0.0018F) + hash(seed + i) * 0.2F);
         float angle = i * (360.0F / Math.max(1, definition.wispCount())) + motion * (0.72F + i % 2 * 0.23F);
         float orbit = radius * (1.0F + 0.08F * wave(motion * 0.04F + i * 2.1F));
         float y = 0.02F + phase * height * 0.76F;
         float wispHeight = height * (0.25F + hash(seed * 3 + i) * 0.22F);
         float width = radius * (0.16F + hash(seed * 7 + i) * 0.13F);
         float x = (float)Math.sin(Math.toRadians(angle)) * orbit;
         float z = (float)Math.cos(Math.toRadians(angle)) * orbit;
         poseStack.pushPose();
         poseStack.translate(x, y, z);
         applyFacing(poseStack, definition.facing(), angle, minecraft);
         int wispAlpha = alpha((72.0F + hash(seed + i * 13) * 52.0F) * envelope * (0.55F + 0.45F * wave(phase * (float) Math.PI)));
         drawTaperedQuad(vertices, poseStack.last(), width, wispHeight, definition, wispAlpha, 2.0F);
         if (definition.facing() == PlayerAuraDefinition.Facing.CROSSED) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            drawTaperedQuad(vertices, poseStack.last(), width, wispHeight, definition, wispAlpha / 2, 2.0F);
         }

         poseStack.popPose();
      }
   }

   private static void drawSpikes(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      int seed,
      float radius,
      float height,
      float motion,
      float envelope,
      Minecraft minecraft
   ) {
      for (int i = 0; i < definition.spikeCount(); i++) {
         float pulse = 0.5F + 0.5F * wave(motion * 0.12F + i * 1.77F);
         float angle = i * (360.0F / Math.max(1, definition.spikeCount())) - motion * 1.34F;
         float orbit = radius * (1.1F + pulse * 0.14F);
         float y = height * (0.12F + hash(seed + i * 19) * 0.74F);
         float spikeHeight = height * (0.08F + pulse * 0.13F);
         float width = radius * (0.045F + pulse * 0.045F);
         poseStack.pushPose();
         poseStack.translate((float)Math.sin(Math.toRadians(angle)) * orbit, y, (float)Math.cos(Math.toRadians(angle)) * orbit);
         applyFacing(poseStack, definition.facing(), angle, minecraft);
         poseStack.mulPose(Axis.ZP.rotationDegrees(-18.0F + hash(seed + i) * 36.0F));
         drawDiamond(vertices, poseStack.last(), width, spikeHeight, definition, alpha((105.0F + pulse * 90.0F) * envelope));
         poseStack.popPose();
      }
   }

   private static void drawFluidField(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      Player player,
      float radius,
      float height,
      float motion,
      float envelope,
      float partialTick,
      Minecraft minecraft
   ) {
      PlayerAuraDefinition.FluidProfile fluid = definition.fluid();
      drawFluidLobes(vertices, poseStack, definition, fluid, radius, height, motion, envelope, minecraft);
      drawFluidVeils(vertices, poseStack, definition, fluid, radius, height, motion, envelope, minecraft);
      drawFluidBackflow(vertices, poseStack, definition, fluid, player, radius, height, motion, envelope, partialTick, minecraft);
   }

   private static void drawTrailEcho(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      float radius,
      float height,
      float motion,
      float fade,
      int sampleIndex,
      Minecraft minecraft
   ) {
      PlayerAuraDefinition.FluidProfile fluid = definition.fluid();
      float uvBase = fluid.style() == PlayerAuraDefinition.FluidStyle.SHADOW_RIFT ? 26.0F : 8.0F;

      for (int i = 0; i < 4; i++) {
         float angle = (i * 0.5F + sampleIndex * 0.17F) * (float) Math.PI;
         float echoRadius = radius * fluid.radiusScale() * (0.48F + i * 0.07F);
         float y = height * (0.1F + i * 0.19F) + Mth.sin(motion * 0.04F + i * 1.9F) * height * 0.035F;
         float width = radius * (0.3F + i * 0.035F);
         float echoHeight = height * (0.2F + i * 0.025F);
         int echoAlpha = alpha(72.0F * fluid.opacity() * fade);
         poseStack.pushPose();
         poseStack.translate(Mth.sin(angle) * echoRadius, y, Mth.cos(angle) * echoRadius);
         applyFacing(poseStack, PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA, 0.0F, minecraft);
         drawFluidQuad(vertices, poseStack.last(), width, echoHeight, definition, echoAlpha, uvBase);
         poseStack.popPose();
      }
   }

   private static void drawFluidLobes(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      PlayerAuraDefinition.FluidProfile fluid,
      float radius,
      float height,
      float motion,
      float envelope,
      Minecraft minecraft
   ) {
      float uvBase = fluid.style() == PlayerAuraDefinition.FluidStyle.SHADOW_RIFT ? 20.0F : 8.0F;

      for (int i = 0; i < fluid.lobeCount(); i++) {
         float random = hash(i * 97 + definition.id().hashCode());
         float angle = i * ((float) (Math.PI * 2) / Math.max(1, fluid.lobeCount())) + (random - 0.5F) * 0.42F;
         float life = fract(i * 0.618034F + motion * fluid.speed() * (0.0065F + i % 4 * 5.5E-4F));
         float lifeFade = (float)(Mth.smoothstep(Math.min(1.0F, life * 6.0F)) * Mth.smoothstep(Math.min(1.0F, (1.0F - life) * 5.0F)));
         float orbit = radius * fluid.radiusScale() * (0.7F + random * 0.24F) * (1.0F + Mth.sin(motion * 0.035F + i * 1.41F) * 0.045F * fluid.turbulence());
         float y = -height * 0.1F + life * height * 0.98F;
         float width = radius * (0.34F + hash(i * 31 + 7) * 0.32F);
         float lobeHeight = height * (0.22F + hash(i * 43 + 11) * 0.22F);
         int lobeAlpha = alpha(150.0F * fluid.opacity() * envelope * lifeFade);
         poseStack.pushPose();
         poseStack.translate(Mth.sin(angle) * orbit, y, Mth.cos(angle) * orbit);
         applyFacing(poseStack, PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA, 0.0F, minecraft);
         poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(motion * 0.028F + i * 2.3F) * 7.0F));
         drawFluidQuad(vertices, poseStack.last(), width, lobeHeight, definition, lobeAlpha, uvBase);
         poseStack.popPose();
      }
   }

   private static void drawFluidVeils(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      PlayerAuraDefinition.FluidProfile fluid,
      float radius,
      float height,
      float motion,
      float envelope,
      Minecraft minecraft
   ) {
      float uvBase = fluid.style() == PlayerAuraDefinition.FluidStyle.SHADOW_RIFT ? 22.0F : 10.0F;

      for (int i = 0; i < fluid.veilCount(); i++) {
         float random = hash(i * 71 + definition.id().hashCode());
         float angle = i * ((float) (Math.PI * 2) / Math.max(1, fluid.veilCount())) + random * 0.31F;
         float breathing = 1.0F + Mth.sin(motion * 0.026F + i * 1.83F) * 0.07F * fluid.turbulence();
         float veilRadius = radius * fluid.radiusScale() * (0.82F + random * 0.22F) * breathing;
         float width = radius * (0.54F + random * 0.24F);
         float veilHeight = height * (0.72F + hash(i * 29 + 3) * 0.25F);
         float y = height * (hash(i * 47 + 19) * 0.12F - 0.04F);
         int veilAlpha = alpha(86.0F * fluid.opacity() * envelope);
         poseStack.pushPose();
         poseStack.translate(Mth.sin(angle) * veilRadius, y, Mth.cos(angle) * veilRadius);
         applyFacing(poseStack, PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA, 0.0F, minecraft);
         drawFluidQuad(vertices, poseStack.last(), width, veilHeight, definition, veilAlpha, uvBase);
         poseStack.popPose();
      }
   }

   private static void drawFluidBackflow(
      VertexConsumer vertices,
      PoseStack poseStack,
      PlayerAuraDefinition definition,
      PlayerAuraDefinition.FluidProfile fluid,
      Player player,
      float radius,
      float height,
      float motion,
      float envelope,
      float partialTick,
      Minecraft minecraft
   ) {
      float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
      float backwardX = Mth.sin(bodyYaw);
      float backwardZ = -Mth.cos(bodyYaw);
      float rightX = Mth.cos(bodyYaw);
      float rightZ = Mth.sin(bodyYaw);
      float cameraYaw = minecraft.gameRenderer.getMainCamera().getYRot() * (float) (Math.PI / 180.0);
      float cameraRightX = Mth.cos(cameraYaw);
      float cameraRightZ = Mth.sin(cameraYaw);
      float uvBase = fluid.style() == PlayerAuraDefinition.FluidStyle.SHADOW_RIFT ? 24.0F : 12.0F;

      for (int i = 0; i < fluid.backflowCount(); i++) {
         float random = hash(i * 113 + definition.id().hashCode());
         float lateral = ((float)i / Math.max(1, fluid.backflowCount() - 1) - 0.5F) * radius * 1.05F;
         float startX = backwardX * radius * 0.52F + rightX * lateral;
         float startZ = backwardZ * radius * 0.52F + rightZ * lateral;
         float startY = height * (0.08F + random * 0.58F);
         float length = radius * (0.62F + random * 0.56F);
         float rise = height * (0.18F + hash(i * 53 + 5) * 0.25F);
         float width = radius * (0.2F + hash(i * 37 + 13) * 0.15F);
         int backflowAlpha = alpha(104.0F * fluid.opacity() * envelope);
         drawSoftBackflow(
            vertices,
            poseStack.last(),
            definition,
            startX,
            startY,
            startZ,
            backwardX,
            backwardZ,
            rightX,
            rightZ,
            cameraRightX,
            cameraRightZ,
            length,
            rise,
            width,
            motion,
            i,
            backflowAlpha,
            uvBase
         );
      }
   }

   private static void drawFluidQuad(VertexConsumer vertices, Pose pose, float width, float height, PlayerAuraDefinition definition, int alpha, float uvBase) {
      int bottomColor = mixColor(definition.secondaryColor(), definition.primaryColor(), 0.2F);
      int topColor = mixColor(definition.primaryColor(), 16777215, 0.18F);
      vertex(vertices, pose, -width, -height * 0.5F, 0.0F, uvBase + 0.05F, 1.0F, bottomColor, alpha);
      vertex(vertices, pose, width, -height * 0.5F, 0.0F, uvBase + 0.95F, 1.0F, bottomColor, alpha);
      vertex(vertices, pose, width, height * 0.5F, 0.0F, uvBase + 0.95F, 0.0F, topColor, alpha);
      vertex(vertices, pose, -width, height * 0.5F, 0.0F, uvBase + 0.05F, 0.0F, topColor, alpha);
   }

   private static void drawSoftBackflow(
      VertexConsumer vertices,
      Pose pose,
      PlayerAuraDefinition definition,
      float startX,
      float startY,
      float startZ,
      float backwardX,
      float backwardZ,
      float rightX,
      float rightZ,
      float cameraRightX,
      float cameraRightZ,
      float length,
      float rise,
      float width,
      float motion,
      int index,
      int alpha,
      float uvBase
   ) {
      int segments = 5;

      for (int segment = 0; segment < 5; segment++) {
         float t0 = segment / 5.0F;
         float t1 = (segment + 1) / 5.0F;
         float sway0 = Mth.sin(motion * 0.047F + index * 1.91F + t0 * 4.0F) * length * 0.09F;
         float sway1 = Mth.sin(motion * 0.047F + index * 1.91F + t1 * 4.0F) * length * 0.09F;
         float x0 = startX + backwardX * length * t0 + rightX * sway0;
         float z0 = startZ + backwardZ * length * t0 + rightZ * sway0;
         float y0 = startY + rise * (t0 + t0 * t0 * 0.35F);
         float x1 = startX + backwardX * length * t1 + rightX * sway1;
         float z1 = startZ + backwardZ * length * t1 + rightZ * sway1;
         float y1 = startY + rise * (t1 + t1 * t1 * 0.35F);
         float width0 = width * (0.95F - t0 * 0.25F);
         float width1 = width * (0.95F - t1 * 0.25F);
         int color0 = mixColor(definition.secondaryColor(), definition.primaryColor(), 0.35F + t0 * 0.45F);
         int color1 = mixColor(definition.secondaryColor(), definition.primaryColor(), 0.35F + t1 * 0.45F);
         vertex(vertices, pose, x0 - cameraRightX * width0, y0, z0 - cameraRightZ * width0, uvBase + 0.05F, 1.0F - t0, color0, alpha);
         vertex(vertices, pose, x0 + cameraRightX * width0, y0, z0 + cameraRightZ * width0, uvBase + 0.95F, 1.0F - t0, color0, alpha);
         vertex(vertices, pose, x1 + cameraRightX * width1, y1, z1 + cameraRightZ * width1, uvBase + 0.95F, 1.0F - t1, color1, alpha);
         vertex(vertices, pose, x1 - cameraRightX * width1, y1, z1 - cameraRightZ * width1, uvBase + 0.05F, 1.0F - t1, color1, alpha);
      }
   }

   private static void drawGroundRing(VertexConsumer vertices, PoseStack poseStack, PlayerAuraDefinition definition, float radius, float motion, int alpha) {
      float pulse = 1.0F + wave(motion * 0.055F) * 0.035F;
      float outer = radius * pulse;
      float inner = outer - Math.max(0.035F, radius * 0.075F);

      for (int segment = 0; segment < 24; segment++) {
         float t0 = segment / 24.0F;
         float t1 = (segment + 1) / 24.0F;
         double a0 = (Math.PI * 2) * t0;
         double a1 = (Math.PI * 2) * t1;
         vertex(
            vertices,
            poseStack.last(),
            (float)Math.cos(a0) * inner,
            0.025F,
            (float)Math.sin(a0) * inner,
            6.0F + t0,
            1.0F,
            definition.secondaryColor(),
            alpha / 2
         );
         vertex(
            vertices,
            poseStack.last(),
            (float)Math.cos(a1) * inner,
            0.025F,
            (float)Math.sin(a1) * inner,
            6.0F + t1,
            1.0F,
            definition.secondaryColor(),
            alpha / 2
         );
         vertex(vertices, poseStack.last(), (float)Math.cos(a1) * outer, 0.025F, (float)Math.sin(a1) * outer, 6.0F + t1, 0.0F, definition.primaryColor(), alpha);
         vertex(vertices, poseStack.last(), (float)Math.cos(a0) * outer, 0.025F, (float)Math.sin(a0) * outer, 6.0F + t0, 0.0F, definition.primaryColor(), alpha);
      }
   }

   private static void applyFacing(PoseStack poseStack, PlayerAuraDefinition.Facing facing, float crossedAngle, Minecraft minecraft) {
      if (facing == PlayerAuraDefinition.Facing.CAMERA) {
         poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
      } else if (facing == PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA) {
         Camera camera = minecraft.gameRenderer.getMainCamera();
         poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
      } else {
         poseStack.mulPose(Axis.YP.rotationDegrees(crossedAngle));
      }
   }

   private static void drawTaperedQuad(VertexConsumer vertices, Pose pose, float width, float height, PlayerAuraDefinition definition, int alpha, float uvBase) {
      vertex(vertices, pose, -width, 0.0F, 0.0F, uvBase + 0.05F, 1.0F, definition.secondaryColor(), alpha);
      vertex(vertices, pose, width, 0.0F, 0.0F, uvBase + 0.95F, 1.0F, definition.secondaryColor(), alpha);
      vertex(vertices, pose, width * 0.18F, height, 0.0F, uvBase + 0.95F, 0.0F, definition.primaryColor(), alpha);
      vertex(vertices, pose, -width * 0.18F, height, 0.0F, uvBase + 0.05F, 0.0F, definition.primaryColor(), alpha);
   }

   private static void drawDiamond(VertexConsumer vertices, Pose pose, float width, float height, PlayerAuraDefinition definition, int alpha) {
      vertex(vertices, pose, 0.0F, -height, 0.0F, 4.5F, 1.0F, definition.secondaryColor(), alpha);
      vertex(vertices, pose, width, 0.0F, 0.0F, 5.0F, 0.5F, definition.primaryColor(), alpha);
      vertex(vertices, pose, 0.0F, height, 0.0F, 4.5F, 0.0F, definition.primaryColor(), alpha);
      vertex(vertices, pose, -width, 0.0F, 0.0F, 4.0F, 0.5F, definition.primaryColor(), alpha);
   }

   private static void vertex(VertexConsumer vertices, Pose pose, float x, float y, float z, float u, float v, int color, int alpha) {
      Matrix4f matrix = pose.pose();
      Matrix3f normal = pose.normal();
      vertices.vertex(matrix, x, y, z)
         .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Math.max(0, Math.min(255, alpha)))
         .uv(u, v)
         .overlayCoords(OverlayTexture.NO_OVERLAY)
         .uv2(240)
         .normal(normal, 0.0F, 1.0F, 0.0F)
         .endVertex();
   }

   private static int alpha(float value) {
      return Math.max(0, Math.min(255, Math.round(value)));
   }

   private static float fract(float value) {
      return value - (float)Math.floor(value);
   }

   private static float wave(float value) {
      return (float)Math.sin(value);
   }

   private static float hash(int value) {
      int mixed = value * 73244475;
      mixed = (mixed ^ mixed >>> 16) * 73244475;
      mixed ^= mixed >>> 16;
      return (mixed & 2147483647) / 2.1474836E9F;
   }

   private static int mixColor(int first, int second, float amount) {
      float t = Mth.clamp(amount, 0.0F, 1.0F);
      int red = Math.round(Mth.lerp(t, first >> 16 & 0xFF, second >> 16 & 0xFF));
      int green = Math.round(Mth.lerp(t, first >> 8 & 0xFF, second >> 8 & 0xFF));
      int blue = Math.round(Mth.lerp(t, first & 0xFF, second & 0xFF));
      return red << 16 | green << 8 | blue;
   }
}
