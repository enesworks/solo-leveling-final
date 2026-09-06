package dev.eness.sololevelingfinal.core.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Post;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.renderer.AntaresVfxClientState;
import dev.eness.sololevelingfinal.core.util.AntaresCombatManager;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class AntaresRuinOverlay {
   private static final int BACK = -1207302903;
   private static final int EMPTY = -868610024;
   private static final int FILLED = -1887938;
   private static final int HOT = -19842;

   private AntaresRuinOverlay() {
   }

   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void render(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.level != null && !minecraft.options.hideGui && AntaresCombatManager.isAntaresVessel(minecraft.player)) {
         AntaresVfxClientState.HudState state = AntaresVfxClientState.hudState(event.getPartialTick());
         GuiGraphics graphics = event.getGuiGraphics();
         int width = event.getWindow().getGuiScaledWidth();
         int height = event.getWindow().getGuiScaledHeight();
         renderRuin(graphics, minecraft, state, width / 2, height - 35);
         if (state.extinctionChargeProgress() >= 0.0F) {
            renderExtinctionLock(graphics, state.extinctionChargeProgress(), width / 2, height / 2);
         }
      }
   }

   private static void renderRuin(GuiGraphics graphics, Minecraft minecraft, AntaresVfxClientState.HudState state, int centerX, int y) {
      int maximum = Mth.clamp(state.maximum(), 1, 6);
      int plateWidth = 29 + maximum * 9;
      graphics.fill(centerX - plateWidth / 2, y - 2, centerX + plateWidth / 2, y + 9, -1207302903);
      Component label = Component.translatable("gui.sololeveling.antares.ruin");
      graphics.drawString(minecraft.font, label, centerX - plateWidth / 2 + 3, y, 14198697, false);
      int startX = centerX + plateWidth / 2 - maximum * 9 + 3;

      for (int index = 0; index < maximum; index++) {
         boolean active = index < state.charges();
         float pulse = active && index == state.charges() - 1 ? state.pulse() : 0.0F;
         int color = active ? (pulse > 0.42F ? -19842 : -1887938) : -868610024;
         drawDiamond(graphics, startX + index * 9, y + 3, color, active && state.manifested());
      }
   }

   private static void drawDiamond(GuiGraphics graphics, int centerX, int centerY, int color, boolean manifested) {
      if (manifested) {
         graphics.fill(centerX - 3, centerY, centerX + 4, centerY + 1, -1325444049);
      }

      graphics.fill(centerX, centerY - 3, centerX + 1, centerY + 4, color);
      graphics.fill(centerX - 1, centerY - 2, centerX + 2, centerY + 3, color);
      graphics.fill(centerX - 2, centerY - 1, centerX + 3, centerY + 2, color);
   }

   private static void renderExtinctionLock(GuiGraphics graphics, float progress, int centerX, int centerY) {
      float eased = progress * progress * (3.0F - 2.0F * progress);
      int radius = Math.round(Mth.lerp(eased, 44.0F, 17.0F));
      int alpha = Mth.clamp(Math.round(110.0F + progress * 120.0F), 0, 230);
      int crimson = alpha << 24 | 14889278;
      int hot = Math.min(255, alpha + 18) << 24 | 16757374;
      int length = 8;

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sy = -1; sy <= 1; sy += 2) {
            int x = centerX + sx * radius;
            int y = centerY + sy * radius;
            graphics.fill(Math.min(x, x - sx * length), y, Math.max(x, x - sx * length) + 1, y + 1, crimson);
            graphics.fill(x, Math.min(y, y - sy * length), x + 1, Math.max(y, y - sy * length) + 1, crimson);
         }
      }

      int core = progress > 0.82F ? hot : crimson;
      graphics.fill(centerX - 5, centerY, centerX - 1, centerY + 1, core);
      graphics.fill(centerX + 2, centerY, centerX + 6, centerY + 1, core);
      graphics.fill(centerX, centerY - 5, centerX + 1, centerY - 1, core);
      graphics.fill(centerX, centerY + 2, centerX + 1, centerY + 6, core);
   }
}
