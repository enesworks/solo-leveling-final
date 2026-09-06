package dev.eness.sololevelingfinal.core.client.gui.system;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.DatapackGateSelectionMessage;
import dev.eness.sololevelingfinal.core.network.DatapackGateSelectionStateMessage;

public final class DatapackGateSelectionScreen extends SystemScreen {
   private static final int VISIBLE_ROWS = 7;
   private static final int ROW_HEIGHT = 20;
   private static final int LIST_TOP = 48;
   private final UUID gateId;
   private long revision;
   private List<DatapackGateSelectionStateMessage.Option> options;
   private String notice;
   private int selectedIndex;
   private int scroll;
   private String selectedRank = "";
   private boolean submitted;

   private DatapackGateSelectionScreen(UUID gateId, long revision, List<DatapackGateSelectionStateMessage.Option> options, String notice) {
      super(Component.literal("DATAPACK GATE"));
      this.panelW = 352;
      this.panelH = 294;
      this.gateId = gateId;
      this.applyState(revision, options, notice);
   }

   public static void handleServerState(boolean open, UUID gateId, long revision, List<DatapackGateSelectionStateMessage.Option> options, String notice) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!open) {
         if (minecraft.screen instanceof DatapackGateSelectionScreen screen && screen.gateId.equals(gateId)) {
            screen.submitted = true;
            minecraft.setScreen(null);
         }
      } else {
         if (minecraft.screen instanceof DatapackGateSelectionScreen screen && screen.gateId.equals(gateId)) {
            screen.applyState(revision, options, notice);
            if (screen.minecraft != null) {
               screen.rebuildWidgets();
            }
         } else {
            minecraft.setScreen(new DatapackGateSelectionScreen(gateId, revision, options, notice));
         }
      }
   }

   @Override
   protected void init() {
      super.init();
      this.rebuildWidgets();
   }

   @Override
   protected void rebuildWidgets() {
      this.clearWidgets();
      int listX = this.panelX + 12;
      int listY = this.panelY + 48;
      int listW = this.panelW - 24;
      int end = Math.min(this.options.size(), this.scroll + 7);

      for (int index = this.scroll; index < end; index++) {
         int optionIndex = index;
         DatapackGateSelectionStateMessage.Option option = this.options.get(index);
         String prefix = index == this.selectedIndex ? "> " : "  ";
         String label = this.fit(prefix + option.dungeonId(), listW - 12);
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               listX, listY + (index - this.scroll) * 20, listW, 18, Component.literal(label), button -> this.selectOption(optionIndex)
            )
         );
      }

      DatapackGateSelectionStateMessage.Option selected = this.selectedOption();
      if (selected != null) {
         int rankY = this.panelY + 216;
         int rankX = this.panelX + 12;

         for (String rank : selected.ranks()) {
            String value = rank;
            String label = value.equals(this.selectedRank) ? "[" + value + "]" : value;
            this.addRenderableWidget(new SystemScreen.SystemButton(rankX, rankY, 36, 18, Component.literal(label), button -> this.selectRank(value)));
            rankX += 40;
         }
      }

      if (!this.submitted && selected != null && !this.selectedRank.isBlank()) {
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               this.panelX + this.panelW - 166, this.panelY + this.panelH - 28, 76, 18, Component.literal("Bind Gate"), button -> this.submitSelection()
            )
         );
      }

      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + this.panelW - 84, this.panelY + this.panelH - 28, 72, 18, Component.literal("Cancel"), button -> this.beginClose()
         )
      );
   }

   private void applyState(long revision, List<DatapackGateSelectionStateMessage.Option> nextOptions, String nextNotice) {
      String previousId = this.selectedOption() == null ? "" : this.selectedOption().dungeonId();
      this.revision = Math.max(0L, revision);
      this.options = nextOptions == null ? List.of() : List.copyOf(nextOptions.stream().filter(Objects::nonNull).limit(256L).toList());
      this.notice = clean(nextNotice, 192);
      this.submitted = false;
      this.selectedIndex = 0;

      for (int index = 0; index < this.options.size(); index++) {
         if (this.options.get(index).dungeonId().equals(previousId)) {
            this.selectedIndex = index;
            break;
         }
      }

      this.scroll = clamp(this.scroll, 0, this.maxScroll());
      this.chooseValidRank();
   }

   private void selectOption(int index) {
      if (index >= 0 && index < this.options.size()) {
         this.selectedIndex = index;
         this.chooseValidRank();
         this.rebuildWidgets();
      }
   }

   private void selectRank(String rank) {
      DatapackGateSelectionStateMessage.Option selected = this.selectedOption();
      if (selected != null && selected.ranks().contains(rank)) {
         this.selectedRank = rank;
         this.rebuildWidgets();
      }
   }

   private void chooseValidRank() {
      DatapackGateSelectionStateMessage.Option selected = this.selectedOption();
      if (selected != null && !selected.ranks().isEmpty()) {
         if (!selected.ranks().contains(this.selectedRank)) {
            this.selectedRank = selected.ranks().get(0);
         }
      } else {
         this.selectedRank = "";
      }
   }

   private void submitSelection() {
      DatapackGateSelectionStateMessage.Option selected = this.selectedOption();
      if (!this.submitted && selected != null && selected.ranks().contains(this.selectedRank)) {
         this.submitted = true;
         this.rebuildWidgets();
         SololevelingMod.PACKET_HANDLER.sendToServer(new DatapackGateSelectionMessage(this.gateId, this.revision, selected.dungeonId(), this.selectedRank));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      int next = clamp(this.scroll + (delta < 0.0 ? 1 : -1), 0, this.maxScroll());
      if (next != this.scroll) {
         this.scroll = next;
         this.rebuildWidgets();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, delta);
      }
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      graphics.drawCenteredString(this.font, "Choose the dungeon and gate rank.", this.panelX + this.panelW / 2, this.panelY + 26, -1509633);
      graphics.fill(this.panelX + 10, this.panelY + 41, this.panelX + this.panelW - 10, this.panelY + 42, -14519384);
      if (this.options.isEmpty()) {
         graphics.drawCenteredString(this.font, "NO DATAPACK DUNGEONS AVAILABLE", this.panelX + this.panelW / 2, this.panelY + 100, -38024);
      } else {
         DatapackGateSelectionStateMessage.Option selected = this.selectedOption();
         if (selected != null) {
            String details = selected.kind() + "  |  " + roomLabel(selected.minRooms(), selected.maxRooms());
            graphics.drawString(this.font, this.fit(details, this.panelW - 24), this.panelX + 12, this.panelY + 195, -7358248, false);
            graphics.drawString(this.font, "RANK", this.panelX + 12, this.panelY + 207, -7358248, false);
         }

         if (this.maxScroll() > 0) {
            graphics.drawString(
               this.font,
               this.scroll + 1 + "-" + Math.min(this.options.size(), this.scroll + 7) + "/" + this.options.size(),
               this.panelX + this.panelW - 58,
               this.panelY + 34,
               -7358248,
               false
            );
         }
      }

      if (!this.notice.isBlank()) {
         int color = this.options.isEmpty() ? -23379 : -11930;
         graphics.drawCenteredString(this.font, this.fit(this.notice, this.panelW - 24), this.panelX + this.panelW / 2, this.panelY + 247, color);
      }
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   @Override
   protected boolean shouldPlaySystemSounds() {
      return true;
   }

   private DatapackGateSelectionStateMessage.Option selectedOption() {
      return this.options != null && this.selectedIndex >= 0 && this.selectedIndex < this.options.size() ? this.options.get(this.selectedIndex) : null;
   }

   private int maxScroll() {
      return Math.max(0, (this.options == null ? 0 : this.options.size()) - 7);
   }

   private String fit(String text, int width) {
      String value = text == null ? "" : text;
      if (this.font.width(value) <= width) {
         return value;
      }

      String ellipsis = "...";

      while (!value.isEmpty() && this.font.width(value + ellipsis) > width) {
         value = value.substring(0, value.length() - 1);
      }

      return value + ellipsis;
   }

   private static String roomLabel(int min, int max) {
      return min == max ? min + " ROOMS" : min + "-" + max + " ROOMS";
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static String clean(String value, int maximum) {
      if (value == null) {
         return "";
      }

      String clean = value.replace('\u0000', ' ').trim();
      return clean.length() <= maximum ? clean : clean.substring(0, maximum);
   }
}
