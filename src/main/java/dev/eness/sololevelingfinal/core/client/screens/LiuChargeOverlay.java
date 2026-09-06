package dev.eness.sololevelingfinal.core.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Post;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.client.LiuCombatClientEvents;
import dev.eness.sololevelingfinal.core.util.LiuZhigangCombatManager;

@EventBusSubscriber(Dist.CLIENT)
public final class LiuChargeOverlay {
   private LiuChargeOverlay() {
   }

   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void render(Post event) {
      if (LiuCombatClientEvents.isCharging() && !Minecraft.getInstance().options.hideGui) {
         long ticks = LiuCombatClientEvents.getChargeTicks(0.0F);
         GuiGraphics graphics = event.getGuiGraphics();
         int centerX = event.getWindow().getGuiScaledWidth() / 2;
         int y = event.getWindow().getGuiScaledHeight() / 2 + 15;
         int width = 76;
         int x = centerX - width / 2;
         int tier = LiuZhigangCombatManager.beamChargeTier(ticks);
         float progress = Mth.clamp((float)ticks / 17.0F, 0.0F, 1.0F);
         int fill = Math.round((width - 4) * progress);

         int color = switch (tier) {
            case 1 -> -11442;
            case 2 -> -3936;
            case 3 -> -852737;
            default -> -2250457;
         };
         graphics.fill(x - 2, y - 2, x + width + 2, y + 7, -1610348531);
         graphics.fill(x - 1, y - 1, x + width + 1, y + 6, tier >= 3 ? -654314576 : -948414953);
         graphics.fill(x, y, x + width, y + 5, -536212205);
         if (fill > 0) {
            graphics.fill(x + 2, y + 1, x + 2 + fill, y + 4, color);
         }

         if (tier >= 1) {
            int shimmer = (int)(Minecraft.getInstance().level.getGameTime() * (tier + 1L) % (width - 8));
            graphics.fill(x + 3 + shimmer, y, x + 6 + shimmer, y + 5, tier >= 3 ? -268435457 : -1056966974);
         }

         if (tier >= 2) {
            int pulse = (int)(Minecraft.getInstance().level.getGameTime() % 8L);
            graphics.fill(x - 5 - pulse / 3, y + 1, x - 2, y + 4, -1593839200);
            graphics.fill(x + width + 2, y + 1, x + width + 5 + pulse / 3, y + 4, -1593839200);
         }

         if (tier >= 3) {
            graphics.fill(centerX - 1, y - 5, centerX + 1, y - 2, -788529153);
            graphics.fill(centerX - 1, y + 7, centerX + 1, y + 10, -788529153);
         }
      }
   }
}
