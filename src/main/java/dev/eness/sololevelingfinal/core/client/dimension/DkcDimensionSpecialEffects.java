package dev.eness.sololevelingfinal.core.client.dimension;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.ViewportEvent.ComputeFogColor;
import net.minecraftforge.client.event.ViewportEvent.RenderFog;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "sololeveling", value = Dist.CLIENT)
public final class DkcDimensionSpecialEffects extends DimensionSpecialEffects {
   private static final float SKY_RADIUS = 128.0F;
   private static final float SKY_TOP = 92.0F;
   private static final float SKY_BOTTOM = -68.0F;
   private static final float BLOOD_MOON_DISTANCE = 112.0F;
   private static final float BLOOD_MOON_ELEVATION = (float)Math.toRadians(45.0);
   private static final float BLOOD_MOON_HALF_SIZE = 128.0F;
   private static final ResourceLocation VANILLA_MOON_TEXTURE = new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
   private static final int[] FOG_COLORS = new int[]{
      0,
      5903637,
      6561817,
      4988954,
      5514524,
      6955280,
      7546382,
      5837849,
      4919318,
      7215112,
      8003589,
      2692129,
      2364460,
      2102575,
      2692154,
      3281732,
      1514557,
      1252938,
      2234703,
      3479123,
      4591709
   };
   private static final int[] SKY_COLORS = new int[]{
      0,
      11939104,
      13059621,
      10178093,
      11819570,
      14703129,
      15755799,
      13970993,
      12132397,
      15877901,
      16738824,
      7418973,
      6765688,
      5849731,
      7289748,
      8861864,
      4610993,
      4090834,
      6772184,
      10175957,
      13252072
   };
   private static final float[] FOG_FAR = new float[]{
      0.0F,
      84.0F,
      148.0F,
      136.0F,
      124.0F,
      92.0F,
      116.0F,
      108.0F,
      100.0F,
      94.0F,
      78.0F,
      142.0F,
      132.0F,
      122.0F,
      112.0F,
      96.0F,
      92.0F,
      88.0F,
      84.0F,
      78.0F,
      72.0F
   };
   private static final DkcDimensionSpecialEffects.Profile[] PROFILES = createProfiles();
   private static DkcDimensionSpecialEffects.Profile cachedPlayerProfile = PROFILES[1];

   public DkcDimensionSpecialEffects() {
      super(Float.NaN, true, SkyType.NONE, false, false);
   }

   @Override
   public Vec3 getBrightnessDependentFogColor(Vec3 ignored, float sunHeight) {
      DkcDimensionSpecialEffects.Profile profile = activeProfile();
      float light = 0.76F + 0.12F * Mth.clamp(sunHeight, 0.0F, 1.0F);
      return colorVector(profile.fogColor()).scale(light);
   }

   @Override
   public boolean isFoggyAt(int x, int z) {
      return activeProfile().denseFog();
   }

   @Override
   public boolean renderClouds(
      ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix
   ) {
      return true;
   }

   @Override
   public boolean renderSky(
      ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog
   ) {
      if (isSharedDkc(level) && camera.getFluidInCamera() == FogType.NONE) {
         setupFog.run();
         return true;
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public static void onRenderLevelStage(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         Minecraft minecraft = Minecraft.getInstance();
         ClientLevel level = minecraft.level;
         Camera camera = event.getCamera();
         if (isSharedDkc(level) && camera.getFluidInCamera() == FogType.NONE) {
            renderSkyLayer(event.getPoseStack(), camera, event.getRenderTick(), event.getPartialTick());
         }
      }
   }

   private static void renderSkyLayer(PoseStack poseStack, Camera camera, int ticks, float partialTick) {
      DkcDimensionSpecialEffects.Profile profile = profileAt(camera.getBlockPosition());
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix = poseStack.last().pose();
      renderVault(matrix, profile);
      renderVanillaBloodMoon(matrix, profile);
      float time = ticks + partialTick;
      switch (profile.family()) {
         case EMBER:
            renderEmberSky(matrix, profile, time);
            break;
         case FURNACE:
            renderFurnaceSky(matrix, profile, time);
            break;
         case TEMPEST:
            renderTempestSky(matrix, profile, time);
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
   }

   @Override
   public void adjustLightmapColors(
      ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker, float skyLight, int pixelX, int pixelY, Vector3f colors
   ) {
      if (isSharedDkc(level)) {
         DkcDimensionSpecialEffects.Profile profile = activeProfile();
         float skyContribution = pixelY / 15.0F;
         float strength = profile.lightTintStrength() * (0.72F + skyContribution * 0.28F);
         colors.mul(Mth.lerp(strength, 1.0F, profile.lightRed()), Mth.lerp(strength, 1.0F, profile.lightGreen()), Mth.lerp(strength, 1.0F, profile.lightBlue()));
         colors.set(Mth.clamp(colors.x(), 0.0F, 1.0F), Mth.clamp(colors.y(), 0.0F, 1.0F), Mth.clamp(colors.z(), 0.0F, 1.0F));
      }
   }

   @SubscribeEvent
   public static void onFogColor(ComputeFogColor event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (isSharedDkc(minecraft.level) && event.getCamera().getFluidInCamera() == FogType.NONE) {
         int color = profileAt(event.getCamera().getBlockPosition()).fogColor();
         event.setRed((color >> 16 & 0xFF) / 255.0F);
         event.setGreen((color >> 8 & 0xFF) / 255.0F);
         event.setBlue((color & 0xFF) / 255.0F);
      }
   }

   @SubscribeEvent
   public static void onRenderFog(RenderFog event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (isSharedDkc(minecraft.level) && event.getType() == FogType.NONE) {
         DkcDimensionSpecialEffects.Profile profile = profileAt(event.getCamera().getBlockPosition());
         float far = Math.min(event.getFarPlaneDistance(), profile.fogFar());
         if (event.getMode() == FogMode.FOG_SKY) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(far * 0.86F);
         } else {
            event.setNearPlaneDistance(Math.min(profile.fogNear(), far * 0.48F));
            event.setFarPlaneDistance(far);
         }

         event.setFogShape(FogShape.CYLINDER);
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft minecraft = Minecraft.getInstance();
         ClientLevel level = minecraft.level;
         if (minecraft.player != null && isSharedDkc(level)) {
            cachedPlayerProfile = profileAt(minecraft.player.blockPosition());
            if (!minecraft.isPaused()) {
               DkcDimensionSpecialEffects.Profile profile = cachedPlayerProfile;
               long gameTime = level.getGameTime();
               ParticleStatus status = minecraft.options.particles().get();
               int interval = profile.particleInterval();
               if (status == ParticleStatus.DECREASED) {
                  interval *= 2;
               } else if (status == ParticleStatus.MINIMAL) {
                  interval *= 4;
               }

               if (gameTime % interval == profile.floor() % interval) {
                  RandomSource random = level.random;
                  int count = status == ParticleStatus.ALL && (gameTime + profile.floor()) % 3L == 0L ? 2 : 1;

                  for (int i = 0; i < count; i++) {
                     spawnAmbientParticle(level, minecraft.player.position(), profile, random);
                  }
               }
            }
         } else {
            cachedPlayerProfile = PROFILES[1];
         }
      }
   }

   private static void spawnAmbientParticle(ClientLevel level, Vec3 center, DkcDimensionSpecialEffects.Profile profile, RandomSource random) {
      double angle = random.nextDouble() * Math.PI * 2.0;
      double distance = 5.0 + random.nextDouble() * 17.0;
      double x = center.x + Math.cos(angle) * distance;
      double y = center.y - 1.5 + random.nextDouble() * 10.0;
      double z = center.z + Math.sin(angle) * distance;
      double drift = profile.family() == DkcDimensionSpecialEffects.SkyFamily.TEMPEST ? 0.018 : 0.008;
      double velocityX = random.nextGaussian() * drift;
      double velocityY = profile.family() == DkcDimensionSpecialEffects.SkyFamily.FURNACE ? 0.014 : -0.004;
      double velocityZ = random.nextGaussian() * drift;
      level.addParticle(profile.particle(), x, y, z, velocityX, velocityY, velocityZ);
   }

   private static DkcDimensionSpecialEffects.Profile activeProfile() {
      return cachedPlayerProfile;
   }

   private static DkcDimensionSpecialEffects.Profile profileAt(BlockPos position) {
      int floor = DkcSpatialLayout.floorAt(position);
      if (floor < 1 || floor > 20) {
         floor = 1;
      }

      return PROFILES[floor];
   }

   private static boolean isSharedDkc(ClientLevel level) {
      return level != null && DkcFloorRegistry.SHARED_DIMENSION.equals(level.dimension());
   }

   private static DkcDimensionSpecialEffects.Profile[] createProfiles() {
      DkcDimensionSpecialEffects.Profile[] profiles = new DkcDimensionSpecialEffects.Profile[21];

      for (int floor = 1; floor <= 20; floor++) {
         DkcDimensionSpecialEffects.SkyFamily family = floor >= 16
            ? DkcDimensionSpecialEffects.SkyFamily.TEMPEST
            : (floor >= 6 && floor <= 10 ? DkcDimensionSpecialEffects.SkyFamily.FURNACE : DkcDimensionSpecialEffects.SkyFamily.EMBER);
         boolean dense = floor == 1 || floor == 5 || floor == 10 || floor >= 16;
         float far = FOG_FAR[floor];
         float near = dense ? far * 0.14F : far * 0.31F;
         int horizon = mixColor(FOG_COLORS[floor], SKY_COLORS[floor], 0.52F);
         int zenith = scaleColor(SKY_COLORS[floor], family == DkcDimensionSpecialEffects.SkyFamily.FURNACE ? 0.105F : 0.19F);
         float lightRed;
         float lightGreen;
         float lightBlue;
         float tintStrength;
         ParticleOptions particle;
         if (floor <= 5) {
            lightRed = 1.12F;
            lightGreen = 0.9F;
            lightBlue = 0.84F;
            tintStrength = 0.18F + floor * 0.008F;
            particle = ParticleTypes.ASH;
         } else if (floor <= 10) {
            lightRed = 1.16F;
            lightGreen = 0.84F;
            lightBlue = 0.72F;
            tintStrength = 0.2F + (floor - 5) * 0.01F;
            particle = ParticleTypes.ASH;
         } else if (floor <= 15) {
            lightRed = 1.03F;
            lightGreen = 0.88F;
            lightBlue = 1.1F;
            tintStrength = 0.2F + (floor - 10) * 0.008F;
            particle = ParticleTypes.WHITE_ASH;
         } else {
            lightRed = 0.93F;
            lightGreen = 0.92F;
            lightBlue = 1.18F;
            tintStrength = 0.22F + (floor - 15) * 0.01F;
            particle = ParticleTypes.CRIMSON_SPORE;
         }

         profiles[floor] = new DkcDimensionSpecialEffects.Profile(
            floor,
            FOG_COLORS[floor],
            zenith,
            horizon,
            SKY_COLORS[floor],
            family,
            dense,
            near,
            far,
            lightRed,
            lightGreen,
            lightBlue,
            tintStrength,
            particle,
            Math.max(3, 7 - floor / 5)
         );
      }

      return profiles;
   }

   private static void renderVault(Matrix4f matrix, DkcDimensionSpecialEffects.Profile profile) {
      int top = profile.zenithColor();
      int horizon = profile.horizonColor();
      int bottom = scaleColor(profile.fogColor(), 0.3F);
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      vertex(buffer, matrix, -128.0F, 92.0F, -128.0F, top, 1.0F);
      vertex(buffer, matrix, -128.0F, 92.0F, 128.0F, top, 1.0F);
      vertex(buffer, matrix, 128.0F, 92.0F, 128.0F, top, 1.0F);
      vertex(buffer, matrix, 128.0F, 92.0F, -128.0F, top, 1.0F);
      vaultSide(buffer, matrix, -128.0F, -128.0F, 128.0F, -128.0F, top, horizon, bottom);
      vaultSide(buffer, matrix, 128.0F, -128.0F, 128.0F, 128.0F, top, horizon, bottom);
      vaultSide(buffer, matrix, 128.0F, 128.0F, -128.0F, 128.0F, top, horizon, bottom);
      vaultSide(buffer, matrix, -128.0F, 128.0F, -128.0F, -128.0F, top, horizon, bottom);
      vertex(buffer, matrix, -128.0F, -68.0F, 128.0F, bottom, 1.0F);
      vertex(buffer, matrix, -128.0F, -68.0F, -128.0F, bottom, 1.0F);
      vertex(buffer, matrix, 128.0F, -68.0F, -128.0F, bottom, 1.0F);
      vertex(buffer, matrix, 128.0F, -68.0F, 128.0F, bottom, 1.0F);
      BufferUploader.drawWithShader(buffer.end());
   }

   private static void vaultSide(BufferBuilder buffer, Matrix4f matrix, float x0, float z0, float x1, float z1, int top, int horizon, int bottom) {
      vertex(buffer, matrix, x0, -68.0F, z0, bottom, 1.0F);
      vertex(buffer, matrix, x1, -68.0F, z1, bottom, 1.0F);
      vertex(buffer, matrix, x1, 11.0F, z1, horizon, 1.0F);
      vertex(buffer, matrix, x0, 11.0F, z0, horizon, 1.0F);
      vertex(buffer, matrix, x0, 11.0F, z0, horizon, 1.0F);
      vertex(buffer, matrix, x1, 11.0F, z1, horizon, 1.0F);
      vertex(buffer, matrix, x1, 92.0F, z1, top, 1.0F);
      vertex(buffer, matrix, x0, 92.0F, z0, top, 1.0F);
   }

   private static void renderEmberSky(Matrix4f matrix, DkcDimensionSpecialEffects.Profile profile, float time) {
      renderRibbon(matrix, time * 0.006F, -116.0F, -88.0F, 4.0F, 13.0F, profile.accentColor(), 0.085F);
      renderSkySparks(matrix, profile, time, 22);
   }

   private static void renderFurnaceSky(Matrix4f matrix, DkcDimensionSpecialEffects.Profile profile, float time) {
      renderRibbon(matrix, time * 0.004F, -119.0F, -92.0F, 5.0F, 27.0F, profile.accentColor(), 0.095F);
      renderRibbon(matrix, -time * 0.0032F + 2.3F, -117.0F, -98.0F, 31.0F, 21.0F, profile.fogColor(), 0.13F);
      renderRibbon(matrix, time * 0.0027F + 4.7F, -115.0F, -86.0F, 57.0F, 18.0F, profile.horizonColor(), 0.08F);
      renderSkySparks(matrix, profile, time * 0.62F, 14);
   }

   private static void renderTempestSky(Matrix4f matrix, DkcDimensionSpecialEffects.Profile profile, float time) {
      renderRibbon(matrix, time * 0.009F, -119.0F, -96.0F, 12.0F, 25.0F, profile.accentColor(), 0.14F);
      renderRibbon(matrix, -time * 0.007F + 3.0F, -117.0F, -91.0F, 43.0F, 23.0F, profile.horizonColor(), 0.12F);
      float flashCycle = (time + profile.floor() * 37.0F) % 190.0F;
      if (flashCycle < 5.0F) {
         renderLightning(matrix, profile.floor(), (1.0F - flashCycle / 5.0F) * 0.68F);
      }

      renderSkySparks(matrix, profile, time * 1.2F, 18);
   }

   private static void renderRibbon(Matrix4f matrix, float phase, float z, float startX, float baseY, float height, int color, float alpha) {
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

      for (int i = 0; i <= 16; i++) {
         float x = startX + i * 12.0F;
         float wave = Mth.sin(phase + i * 0.58F) * 5.0F + Mth.sin(phase * 0.71F + i * 0.23F) * 2.4F;
         vertex(buffer, matrix, x, baseY + wave, z, color, 0.0F);
         vertex(buffer, matrix, x, baseY + wave + height, z, color, alpha);
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static void renderSkySparks(Matrix4f matrix, DkcDimensionSpecialEffects.Profile profile, float time, int count) {
      float alpha = profile.family() == DkcDimensionSpecialEffects.SkyFamily.FURNACE ? 0.38F : 0.52F;
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

      for (int i = 0; i < count; i++) {
         int hash = i * 73428767 ^ profile.floor() * 912931;
         float x = -104.0F + Math.floorMod(hash, 209);
         float y = -8.0F + Math.floorMod(hash >>> 8, 87);
         float drift = Mth.sin(time * 0.012F + i * 1.73F) * 2.2F;
         float size = 0.25F + Math.floorMod(hash >>> 16, 5) * 0.11F;
         float z = -118.0F + Math.floorMod(hash >>> 22, 7);
         vertex(buffer, matrix, x - size, y + drift - size, z, profile.accentColor(), alpha);
         vertex(buffer, matrix, x + size, y + drift - size, z, profile.accentColor(), alpha);
         vertex(buffer, matrix, x + size, y + drift + size, z, profile.accentColor(), alpha);
         vertex(buffer, matrix, x - size, y + drift + size, z, profile.accentColor(), alpha);
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static void renderLightning(Matrix4f matrix, int floor, float alpha) {
      float x = -63.0F + (floor - 16) * 25.0F;
      float y = 77.0F;
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

      for (int i = 0; i < 6; i++) {
         float nextX = x + (i % 2 == 0 ? 7.0F : -4.0F);
         float nextY = y - 17.0F;
         lightningSegment(buffer, matrix, x, y, nextX, nextY, -119.0F, 0.75F, alpha);
         x = nextX;
         y = nextY;
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static void lightningSegment(BufferBuilder buffer, Matrix4f matrix, float x0, float y0, float x1, float y1, float z, float width, float alpha) {
      float dx = x1 - x0;
      float dy = y1 - y0;
      float length = Mth.sqrt(dx * dx + dy * dy);
      float ox = -dy / length * width;
      float oy = dx / length * width;
      vertex(buffer, matrix, x0 - ox, y0 - oy, z, 0.72F, 0.78F, 1.0F, alpha);
      vertex(buffer, matrix, x0 + ox, y0 + oy, z, 0.72F, 0.78F, 1.0F, alpha);
      vertex(buffer, matrix, x1 + ox, y1 + oy, z, 0.72F, 0.78F, 1.0F, alpha);
      vertex(buffer, matrix, x1 - ox, y1 - oy, z, 0.72F, 0.78F, 1.0F, alpha);
   }

   private static void renderVanillaBloodMoon(Matrix4f matrix, DkcDimensionSpecialEffects.Profile profile) {
      float sin = Mth.sin(BLOOD_MOON_ELEVATION);
      float cos = Mth.cos(BLOOD_MOON_ELEVATION);
      float directionZ = profile.floor() == 15 ? -1.0F : 1.0F;
      float centerY = 112.0F * sin;
      float centerZ = 112.0F * cos * directionZ;
      float verticalY = cos;
      float verticalZ = -sin * directionZ;
      renderMoonDisc(matrix, 0.0F, centerY, centerZ, verticalY, verticalZ, 0.0F, 0.0F, 42.0F, 16717856, 0.2F, 0.0F);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, VANILLA_MOON_TEXTURE);
      RenderSystem.setShaderColor(1.0F, 0.12F, 0.15F, 1.0F);
      RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO);
      float size = 128.0F;
      float lowerY = centerY - size * verticalY;
      float lowerZ = centerZ - size * verticalZ;
      float upperY = centerY + size * verticalY;
      float upperZ = centerZ + size * verticalZ;
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.vertex(matrix, -size, lowerY, lowerZ).uv(0.0F, 0.5F).endVertex();
      buffer.vertex(matrix, size, lowerY, lowerZ).uv(0.25F, 0.5F).endVertex();
      buffer.vertex(matrix, size, upperY, upperZ).uv(0.25F, 0.0F).endVertex();
      buffer.vertex(matrix, -size, upperY, upperZ).uv(0.0F, 0.0F).endVertex();
      BufferUploader.drawWithShader(buffer.end());
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
   }

   private static void renderMoonDisc(
      Matrix4f matrix,
      float baseX,
      float baseY,
      float baseZ,
      float verticalY,
      float verticalZ,
      float localX,
      float localY,
      float radius,
      int color,
      float centerAlpha,
      float edgeAlpha
   ) {
      float centerX = baseX + localX;
      float centerY = baseY + localY * verticalY;
      float centerZ = baseZ + localY * verticalZ;
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
      vertex(buffer, matrix, centerX, centerY, centerZ, color, centerAlpha);

      for (int i = 0; i <= 40; i++) {
         float angle = (float)((Math.PI * 2) * i / 40.0);
         float horizontal = Mth.cos(angle) * radius;
         float vertical = Mth.sin(angle) * radius;
         vertex(buffer, matrix, centerX + horizontal, centerY + vertical * verticalY, centerZ + vertical * verticalZ, color, edgeAlpha);
      }

      BufferUploader.drawWithShader(buffer.end());
   }

   private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, int color, float alpha) {
      vertex(buffer, matrix, x, y, z, (color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
   }

   private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha) {
      buffer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
   }

   private static Vec3 colorVector(int rgb) {
      return new Vec3((rgb >> 16 & 0xFF) / 255.0, (rgb >> 8 & 0xFF) / 255.0, (rgb & 0xFF) / 255.0);
   }

   private static int scaleColor(int rgb, float scale) {
      int red = Mth.clamp(Math.round((rgb >> 16 & 0xFF) * scale), 0, 255);
      int green = Mth.clamp(Math.round((rgb >> 8 & 0xFF) * scale), 0, 255);
      int blue = Mth.clamp(Math.round((rgb & 0xFF) * scale), 0, 255);
      return red << 16 | green << 8 | blue;
   }

   private static int mixColor(int first, int second, float secondWeight) {
      float firstWeight = 1.0F - secondWeight;
      int red = Math.round((first >> 16 & 0xFF) * firstWeight + (second >> 16 & 0xFF) * secondWeight);
      int green = Math.round((first >> 8 & 0xFF) * firstWeight + (second >> 8 & 0xFF) * secondWeight);
      int blue = Math.round((first & 0xFF) * firstWeight + (second & 0xFF) * secondWeight);
      return red << 16 | green << 8 | blue;
   }

   private record Profile(
      int floor,
      int fogColor,
      int zenithColor,
      int horizonColor,
      int accentColor,
      DkcDimensionSpecialEffects.SkyFamily family,
      boolean denseFog,
      float fogNear,
      float fogFar,
      float lightRed,
      float lightGreen,
      float lightBlue,
      float lightTintStrength,
      ParticleOptions particle,
      int particleInterval
   ) {
   }

   private enum SkyFamily {
      EMBER,
      FURNACE,
      TEMPEST;
   }
}
