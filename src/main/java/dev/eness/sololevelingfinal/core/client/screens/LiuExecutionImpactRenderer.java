package dev.eness.sololevelingfinal.core.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;

@EventBusSubscriber(modid = "sololeveling", value = Dist.CLIENT)
public final class LiuExecutionImpactRenderer {
   private static final ResourceLocation CHAIN = new ResourceLocation("sololeveling", "shaders/post/liu_execution_impact.json");
   private static PostChain postChain;
   private static PostPass impactPass;
   private static long startedAt = Long.MIN_VALUE;
   private static int durationTicks = 16;
   private static int primaryColor = 16765774;
   private static int secondaryColor = 16774064;
   private static int renderWidth = -1;
   private static int renderHeight = -1;
   private static boolean failed;

   private LiuExecutionImpactRenderer() {
   }

   public static void start(int duration, int primary, int secondary) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level != null) {
         startedAt = minecraft.level.getGameTime();
         durationTicks = Mth.clamp(duration, 6, 30);
         primaryColor = primary & 16777215;
         secondaryColor = secondary & 16777215;
         failed = false;
         closeChain();
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void render(Pre event) {
      if (startedAt != Long.MIN_VALUE) {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.level == null) {
            stop();
         } else {
            float elapsed = (float)(minecraft.level.getGameTime() - startedAt) + event.getPartialTick();
            if (elapsed >= durationTicks) {
               stop();
            } else if (minecraft.options.getCameraType().isFirstPerson() && !failed) {
               try {
                  ensureChain(minecraft);
                  LiuExecutionImpactRenderer.Frame frame = frame(elapsed, durationTicks);
                  impactPass.getEffect().safeGetUniform("Phase").set(frame.phase);
                  impactPass.getEffect().safeGetUniform("Intensity").set(frame.intensity);
                  impactPass.getEffect().safeGetUniform("AccentA").set(red(primaryColor), green(primaryColor), blue(primaryColor));
                  impactPass.getEffect().safeGetUniform("AccentB").set(red(secondaryColor), green(secondaryColor), blue(secondaryColor));
                  impactPass.getEffect().safeGetUniform("SequenceTime").set(elapsed / durationTicks);
                  postChain.process(event.getPartialTick());
                  minecraft.getMainRenderTarget().bindWrite(true);
               } catch (Exception exception) {
                  failed = true;
                  closeChain();
                  SololevelingMod.LOGGER.error("Failed to render Liu execution impact frame", exception);
               }
            }
         }
      }
   }

   private static void ensureChain(Minecraft minecraft) throws Exception {
      if (postChain == null) {
         postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), CHAIN);
         impactPass = postChain.addPass("sololeveling:liu_execution_impact", minecraft.getMainRenderTarget(), postChain.getTempTarget("swap"));
         postChain.addPass("blit", postChain.getTempTarget("swap"), minecraft.getMainRenderTarget());
         renderWidth = -1;
         renderHeight = -1;
      }

      int width = minecraft.getWindow().getWidth();
      int height = minecraft.getWindow().getHeight();
      if (width != renderWidth || height != renderHeight) {
         postChain.resize(width, height);
         renderWidth = width;
         renderHeight = height;
      }
   }

   private static LiuExecutionImpactRenderer.Frame frame(float elapsed, float duration) {
      float progress = Mth.clamp(elapsed / Math.max(0.01F, duration), 0.0F, 1.0F);
      if (progress < 0.28F) {
         return new LiuExecutionImpactRenderer.Frame(0.0F, 0.82F + 0.18F * Mth.clamp(progress / 0.28F, 0.0F, 1.0F));
      }

      if (progress < 0.36F) {
         return new LiuExecutionImpactRenderer.Frame(1.0F, 1.0F);
      }

      if (progress < 0.69F) {
         return new LiuExecutionImpactRenderer.Frame(2.0F, 0.88F + 0.12F * Mth.clamp((progress - 0.36F) / 0.33F, 0.0F, 1.0F));
      }

      if (progress < 0.77F) {
         return new LiuExecutionImpactRenderer.Frame(1.0F, 1.18F);
      }

      float build = Mth.clamp((progress - 0.77F) / 0.23F, 0.0F, 1.0F);
      return new LiuExecutionImpactRenderer.Frame(3.0F, 0.86F + build * 0.42F);
   }

   private static float red(int color) {
      return (color >> 16 & 0xFF) / 255.0F;
   }

   private static float green(int color) {
      return (color >> 8 & 0xFF) / 255.0F;
   }

   private static float blue(int color) {
      return (color & 0xFF) / 255.0F;
   }

   private static void stop() {
      startedAt = Long.MIN_VALUE;
      failed = false;
      closeChain();
   }

   private static void closeChain() {
      if (postChain != null) {
         postChain.close();
      }

      postChain = null;
      impactPass = null;
      renderWidth = -1;
      renderHeight = -1;
   }

   private record Frame(float phase, float intensity) {
   }
}
