package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.UnlockedSkillsTab3ButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.PlistButtonConProcedure;
import dev.eness.sololevelingfinal.core.procedures.PlistReturnProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab3Menu;

public class UnlockedSkillsTab3Screen extends AbstractContainerScreen<UnlockedSkillsTab3Menu> {
   private static final HashMap<String, Object> guistate = UnlockedSkillsTab3Menu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   Button button_equip;
   Button button_equip1;
   Button button_equip2;
   Button button_equip3;
   Button button_equip4;
   Button button_equip5;
   Button button_equip6;
   Button button_equip7;
   ImageButton imagebutton_button1;
   ImageButton imagebutton_button11;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/unlocked_skills_tab_3.png");

   public UnlockedSkillsTab3Screen(UnlockedSkillsTab3Menu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(guiGraphics);
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/big_frame2.png"), this.leftPos + -75, this.topPos + -108, 0.0F, 0.0F, 150, 200, 150, 200
      );
      RenderSystem.disableBlend();
   }

   @Override
   public boolean keyPressed(int key, int b, int c) {
      if (key == 256) {
         this.minecraft.player.closeContainer();
         return true;
      } else {
         return super.keyPressed(key, b, c);
      }
   }

   @Override
   public void containerTick() {
      super.containerTick();
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 17), -61, -88, -1, false);
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 18), -61, -69, -1, false);
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 19), -61, -50, -1, false);
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 20), -61, -31, -1, false);
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 21), -61, -12, -1, false);
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 22), -61, 7, -1, false);
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 23), -61, 26, -1, false);
      guiGraphics.drawString(this.font, PlistReturnProcedure.execute(this.entity, 24), -61, 44, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.unlocked_skills_tab_3.label_empty"), -120, -22, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.unlocked_skills_tab_3.label_empty1"), 89, -22, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.button_equip = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 17)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(0, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + -95, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 17)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip", this.button_equip);
      this.addRenderableWidget(this.button_equip);
      this.button_equip1 = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip1"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 18)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(1, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + -76, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 18)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip1", this.button_equip1);
      this.addRenderableWidget(this.button_equip1);
      this.button_equip2 = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip2"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 19)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(2, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + -57, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 19)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip2", this.button_equip2);
      this.addRenderableWidget(this.button_equip2);
      this.button_equip3 = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip3"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 20)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(3, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + -38, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 20)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip3", this.button_equip3);
      this.addRenderableWidget(this.button_equip3);
      this.button_equip4 = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip4"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 21)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(4, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + -19, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 21)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip4", this.button_equip4);
      this.addRenderableWidget(this.button_equip4);
      this.button_equip5 = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip5"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 22)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(5, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + 0, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 22)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip5", this.button_equip5);
      this.addRenderableWidget(this.button_equip5);
      this.button_equip6 = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip6"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 23)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(6, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 6, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + 19, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 23)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip6", this.button_equip6);
      this.addRenderableWidget(this.button_equip6);
      this.button_equip7 = Button.builder(Component.translatable("gui.sololeveling.unlocked_skills_tab_3.button_equip7"), e -> {
         if (PlistButtonConProcedure.execute(this.entity, 24)) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(7, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 7, this.x, this.y, this.z);
         }
      }).bounds(this.leftPos + 18, this.topPos + 38, 51, 20).build(builder -> new Button(builder) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (PlistButtonConProcedure.execute(UnlockedSkillsTab3Screen.this.entity, 24)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      });
      guistate.put("button:button_equip7", this.button_equip7);
      this.addRenderableWidget(this.button_equip7);
      this.imagebutton_button1 = new ImageButton(
         this.leftPos + -127,
         this.topPos + -27,
         48,
         22,
         0,
         0,
         22,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_button1.png"),
         48,
         44,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(8, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 8, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_button1", this.imagebutton_button1);
      this.addRenderableWidget(this.imagebutton_button1);
      this.imagebutton_button11 = new ImageButton(
         this.leftPos + 80,
         this.topPos + -27,
         48,
         22,
         0,
         0,
         22,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_button11.png"),
         48,
         44,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab3ButtonMessage(9, this.x, this.y, this.z));
            UnlockedSkillsTab3ButtonMessage.handleButtonAction(this.entity, 9, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_button11", this.imagebutton_button11);
      this.addRenderableWidget(this.imagebutton_button11);
   }
}
