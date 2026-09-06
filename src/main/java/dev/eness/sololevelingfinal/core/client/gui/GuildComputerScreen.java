package dev.eness.sololevelingfinal.core.client.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemTooltip;
import dev.eness.sololevelingfinal.core.guild.GuildBuffRegistry;
import dev.eness.sololevelingfinal.core.guild.GuildDeployment;
import dev.eness.sololevelingfinal.core.guild.GuildHunter;
import dev.eness.sololevelingfinal.core.guild.GuildMemberPermissions;
import dev.eness.sololevelingfinal.core.network.GuildActionMessage;
import dev.eness.sololevelingfinal.core.world.inventory.GuildComputerMenu;

@OnlyIn(Dist.CLIENT)
public class GuildComputerScreen extends AbstractContainerScreen<GuildComputerMenu> {
   private static final int GUI_W = 340;
   private static final int GUI_H = 220;
   private static final int TAB_H = 20;
   private static final int TAB_Y_OFFSET = 4;
   private static final int TAB_OVERVIEW = 0;
   private static final int TAB_ROSTER = 1;
   private static final int TAB_TEAMS = 2;
   private static final int TAB_DUNGEONS = 3;
   private static final int TAB_STORAGE = 4;
   private static final int TAB_BUFFS = 5;
   private static final int TAB_LEADERBOARD = 6;
   private static final int TAB_MANAGEMENT = 7;
   private static final String[] TAB_LABELS = new String[]{"Overview", "Roster", "Teams", "Dungeons", "Storage", "Buffs", "Leaderboard", "Manage"};
   private static final int DIVIDER_X = 166;
   private static UUID pendingGuildId = null;
   private static int pendingActiveTab = -1;
   private static UUID pendingSelectedTeamId = null;
   private static int pendingDeployTeamIdx = 0;
   private int activeTab = 0;
   private int guiLeft;
   private int guiTop;
   private EditBox nameBox;
   private Button createBtn;
   private Button addMemberBtn;
   private EditBox addMemberBox;
   private Button deleteGuildBtn;
   private int managementScrollOffset = 0;
   private int rosterScrollOffset = 0;
   private UUID selectedTeamId = null;
   private int availableHuntersScroll = 0;
   private int deployTeamIdx = 0;
   private int deploymentsScroll = 0;
   private long localScreenTicks = 0L;
   private int buffsScroll = 0;
   private int[] tabXPositions;
   private int[] tabWidths;

   public GuildComputerScreen(GuildComputerMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.imageWidth = 340;
      this.imageHeight = 220;
      this.inventoryLabelX = -9999;
      this.titleLabelX = -9999;
   }

   @Override
   protected void init() {
      super.init();
      this.guiLeft = (this.width - 340) / 2;
      this.guiTop = (this.height - 220) / 2;
      this.computeTabLayout();
      this.restorePendingViewState();
      this.nameBox = new EditBox(this.font, this.guiLeft + 170 - 80, this.guiTop + 110 - 10, 160, 20, Component.literal("Guild Name"));
      this.nameBox.setMaxLength(24);
      this.nameBox.setVisible(false);
      this.nameBox.setHint(Component.literal("Enter guild name..."));
      this.nameBox.setBordered(true);
      this.addWidget(this.nameBox);
      this.createBtn = Button.builder(Component.literal("Create Guild"), btn -> this.sendAction("create", this.nameBox.getValue(), ""))
         .bounds(this.guiLeft + 170 - 50, this.guiTop + 110 + 18, 100, 20)
         .build();
      this.createBtn.visible = false;
      this.addRenderableWidget(this.createBtn);
      this.addMemberBox = new EditBox(this.font, this.guiLeft + 10, this.guiTop + 220 - 38, 150, 16, Component.literal("Player name"));
      this.addMemberBox.setMaxLength(32);
      this.addMemberBox.setVisible(false);
      this.addMemberBox.setHint(Component.literal("Online player name..."));
      this.addWidget(this.addMemberBox);
      this.addMemberBtn = Button.builder(Component.literal("Send Invite"), btn -> {
         String n = this.addMemberBox.getValue().trim();
         if (!n.isEmpty()) {
            this.sendAction("invite_member", n, "");
            this.addMemberBox.setValue("");
         }
      }).bounds(this.guiLeft + 165, this.guiTop + 220 - 39, 90, 18).build();
      this.addMemberBtn.visible = false;
      this.addRenderableWidget(this.addMemberBtn);
      this.deleteGuildBtn = Button.builder(Component.literal("Delete Guild"), btn -> this.sendAction("delete_guild", "", ""))
         .bounds(this.guiLeft + 340 - 82, this.guiTop + 220 - 39, 74, 18)
         .build();
      this.deleteGuildBtn.visible = false;
      this.addRenderableWidget(this.deleteGuildBtn);
      this.updateWidgetVisibility();
      if (!this.menu.hasGuild) {
         this.setInitialFocus(this.nameBox);
      }
   }

   private void computeTabLayout() {
      this.tabWidths = new int[TAB_LABELS.length];
      this.tabXPositions = new int[TAB_LABELS.length];
      int total = 0;

      for (int i = 0; i < TAB_LABELS.length; i++) {
         this.tabWidths[i] = this.font.width(TAB_LABELS[i]) + 10;
         total += this.tabWidths[i];
      }

      int bonus = (340 - total) / TAB_LABELS.length;
      int x = this.guiLeft;

      for (int i = 0; i < TAB_LABELS.length; i++) {
         this.tabWidths[i] = this.tabWidths[i] + bonus;
         this.tabXPositions[i] = x;
         x += this.tabWidths[i];
      }
   }

   private void restorePendingViewState() {
      if (this.menu.hasGuild && pendingActiveTab >= 0 && pendingGuildId != null && pendingGuildId.equals(this.menu.guildId)) {
         if (this.canAccessTab(pendingActiveTab)) {
            this.activeTab = pendingActiveTab;
         }

         if (pendingSelectedTeamId != null && this.teamExists(pendingSelectedTeamId)) {
            this.selectedTeamId = pendingSelectedTeamId;
         }

         if (!this.menu.teams.isEmpty()) {
            this.deployTeamIdx = Math.max(0, Math.min(pendingDeployTeamIdx, this.menu.teams.size() - 1));
         }

         pendingGuildId = null;
         pendingActiveTab = -1;
         pendingSelectedTeamId = null;
         pendingDeployTeamIdx = 0;
      }
   }

   private void updateWidgetVisibility() {
      boolean noGuild = !this.menu.hasGuild;
      boolean management = this.menu.hasGuild && this.activeTab == 7 && this.menu.viewerIsOwner;
      this.nameBox.setVisible(noGuild);
      this.createBtn.visible = noGuild;
      this.addMemberBox.setVisible(management);
      this.addMemberBtn.visible = management;
      this.deleteGuildBtn.visible = management;
      this.menu.storageTabActive = this.menu.hasGuild && this.activeTab == 4;
   }

   @Override
   public boolean keyPressed(int kc, int sc, int mod) {
      if (this.textBoxFocused()) {
         if (kc == 256) {
            return super.keyPressed(kc, sc, mod);
         }

         if (this.nameBox.isVisible() && this.nameBox.isFocused()) {
            this.nameBox.keyPressed(kc, sc, mod);
         }

         if (this.addMemberBox.isVisible() && this.addMemberBox.isFocused()) {
            this.addMemberBox.keyPressed(kc, sc, mod);
         }

         return true;
      } else {
         return super.keyPressed(kc, sc, mod);
      }
   }

   @Override
   public boolean charTyped(char c, int mod) {
      if (this.nameBox.isVisible() && this.nameBox.isFocused()) {
         return this.nameBox.charTyped(c, mod);
      } else {
         return this.addMemberBox.isVisible() && this.addMemberBox.isFocused() ? this.addMemberBox.charTyped(c, mod) : super.charTyped(c, mod);
      }
   }

   private boolean textBoxFocused() {
      return this.nameBox.isVisible() && this.nameBox.isFocused() || this.addMemberBox.isVisible() && this.addMemberBox.isFocused();
   }

   @Override
   public boolean mouseClicked(double mx, double my, int btn) {
      ResponsiveGuiScale.Transform transform = this.responsiveTransform();
      mx = transform.logicalX(mx);
      my = transform.logicalY(my);
      if (this.menu.hasGuild && this.canAccessAnyTab()) {
         int ty = this.guiTop + 4;
         if (my >= ty && my <= ty + 20) {
            for (int i = 0; i < TAB_LABELS.length; i++) {
               if (mx >= this.tabXPositions[i] && mx < this.tabXPositions[i] + this.tabWidths[i] && this.canAccessTab(i)) {
                  this.activeTab = i;
                  this.rosterScrollOffset = 0;
                  this.availableHuntersScroll = 0;
                  this.deploymentsScroll = 0;
                  this.buffsScroll = 0;
                  this.updateWidgetVisibility();
                  return true;
               }
            }
         }

         if (this.activeTab == 7 && this.menu.viewerIsOwner) {
            this.handleManagementClick(mx, my);
         }

         if (this.activeTab == 1) {
            this.handleRosterClick(mx, my);
         }

         if (this.activeTab == 2 && this.menu.viewerIsOwner) {
            this.handleTeamsClick(mx, my);
         }

         if (this.activeTab == 3 && this.menu.viewerIsOwner) {
            this.handleDungeonsClick(mx, my);
         }

         if (this.activeTab == 5 && this.menu.viewerIsOwner) {
            this.handleBuffsClick(mx, my);
         }
      }

      return super.mouseClicked(mx, my, btn);
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double delta) {
      ResponsiveGuiScale.Transform transform = this.responsiveTransform();
      mx = transform.logicalX(mx);
      my = transform.logicalY(my);
      if (this.activeTab == 7) {
         this.managementScrollOffset = Math.max(0, this.managementScrollOffset - (int)(delta * 6.0));
         return true;
      }

      if (this.activeTab == 1) {
         this.rosterScrollOffset = Math.max(0, this.rosterScrollOffset - (int)(delta * 6.0));
         return true;
      }

      if (this.activeTab == 2) {
         int divX = this.guiLeft + 166;
         if (mx > divX) {
            this.availableHuntersScroll = Math.max(0, this.availableHuntersScroll - (int)(delta * 6.0));
         }

         return true;
      } else if (this.activeTab == 3) {
         this.deploymentsScroll = Math.max(0, this.deploymentsScroll - (int)(delta * 6.0));
         return true;
      } else if (this.activeTab == 5) {
         this.buffsScroll = Math.max(0, Math.min(this.maxBuffsScroll(), this.buffsScroll - (int)(delta * 8.0)));
         return true;
      } else {
         return super.mouseScrolled(mx, my, delta);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      ResponsiveGuiScale.Transform transform = this.responsiveTransform();
      return super.mouseReleased(transform.logicalX(mouseX), transform.logicalY(mouseY), button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      ResponsiveGuiScale.Transform transform = this.responsiveTransform();
      return super.mouseDragged(transform.logicalX(mouseX), transform.logicalY(mouseY), button, dragX / transform.scale(), dragY / transform.scale());
   }

   @Override
   public void mouseMoved(double mouseX, double mouseY) {
      ResponsiveGuiScale.Transform transform = this.responsiveTransform();
      super.mouseMoved(transform.logicalX(mouseX), transform.logicalY(mouseY));
   }

   private void handleManagementClick(double mx, double my) {
      int rowH = 18;
      int startY = this.guiTop + 4 + 20 + 42;
      int startX = this.guiLeft + 8;
      String[] permKeys = new String[]{"canOpen", "tabOverview", "tabRoster", "tabTeams", "tabDungeons", "tabStorage", "tabBuffs", "tabLeaderboard"};
      int nameColW = 82;
      int checkGap = 28;
      int checkSize = 10;

      for (int row = 0; row < this.menu.members.size(); row++) {
         int ry = startY + row * rowH - this.managementScrollOffset;
         if (ry >= startY - rowH && ry <= this.guiTop + 220 - 50) {
            GuildMemberPermissions p = this.menu.members.get(row);

            for (int col = 0; col < permKeys.length; col++) {
               int cx = startX + nameColW + col * checkGap + 4;
               int cy = ry + 4;
               if (mx >= cx && mx <= cx + checkSize && my >= cy && my <= cy + checkSize) {
                  this.sendAction("toggle_perm", p.playerUUID.toString(), permKeys[col]);
                  return;
               }
            }

            int rx = this.guiLeft + 340 - 20;
            if (mx >= rx && mx <= rx + 12 && my >= ry + 3 && my <= ry + 13) {
               this.sendAction("remove_member", p.playerUUID.toString(), "");
               return;
            }
         }
      }
   }

   private void handleRosterClick(double mx, double my) {
      if (this.menu.viewerIsOwner) {
         int contentY = this.guiTop + 4 + 20 + 4;
         int listStartY = contentY + 14;
         int divX = this.guiLeft + 166;
         if (mx < divX) {
            int rowH = 16;

            for (int i = 0; i < this.menu.hunters.size(); i++) {
               int ry = listStartY + i * rowH - this.rosterScrollOffset;
               int bx = divX - 18;
               if (mx >= bx && mx <= bx + 14 && my >= ry + 1 && my <= ry + 13) {
                  this.sendAction("dismiss", this.menu.hunters.get(i).id.toString(), "");
                  return;
               }
            }
         }

         if (mx > divX) {
            int poolListEndY = this.guiTop + 220 - 28;
            int rowH = 20;
            int rPanelX = divX + 5;
            int rby = this.guiTop + 220 - 26;
            int rbx = rPanelX;
            int rbw = 162;
            if (mx >= rbx && mx <= rbx + rbw && my >= rby && my <= rby + 16) {
               this.sendAction("refresh_pool", "", "");
               return;
            }

            for (int i = 0; i < this.menu.recruitPool.size(); i++) {
               int ry = listStartY + i * rowH - this.rosterScrollOffset;
               if (ry + rowH >= listStartY && ry <= poolListEndY) {
                  int hx = this.guiLeft + 340 - 40;
                  if (mx >= hx && mx <= hx + 34 && my >= ry + 2 && my <= ry + 16) {
                     this.sendAction("hire", this.menu.recruitPool.get(i).id.toString(), "");
                     return;
                  }
               }
            }
         }
      }
   }

   private void handleTeamsClick(double mx, double my) {
      int contentY = this.guiTop + 4 + 20 + 4;
      int listStartY = contentY + 14;
      int divX = this.guiLeft + 166;
      if (mx < divX) {
         int rowH = 28;

         for (int i = 0; i < this.menu.teams.size(); i++) {
            int ry = listStartY + i * rowH;
            if (my >= ry && my < ry + rowH - 2) {
               this.selectedTeamId = this.menu.teams.get(i).id();
               this.availableHuntersScroll = 0;
               return;
            }
         }
      }

      GuildComputerMenu.TeamInfo team = this.getSelectedTeam();
      if (team != null && mx > divX) {
         int rPanelX = divX + 5;
         int ry = listStartY + 14;
         int autoY = ry;
         int toggleX = this.guiLeft + 340 - 88;
         int rankX = this.guiLeft + 340 - 42;
         if (my >= autoY && my <= autoY + 14) {
            if (mx >= toggleX && mx <= toggleX + 42) {
               this.sendAction("set_team_auto_raid", team.id().toString(), !team.autoRaidEnabled() + ":" + team.autoRaidMaxRank());
               return;
            }

            if (mx >= rankX && mx <= rankX + 34) {
               int nextRank = team.autoRaidMaxRank() >= 6 ? 1 : team.autoRaidMaxRank() + 1;
               this.sendAction("set_team_auto_raid", team.id().toString(), team.autoRaidEnabled() + ":" + nextRank);
               return;
            }
         }

         ry += 18;

         for (UUID memberId : team.memberIds()) {
            int bx = this.guiLeft + 340 - 20;
            if (mx >= bx && mx <= bx + 14 && my >= ry + 1 && my <= ry + 12) {
               this.sendAction("remove_from_team", team.id().toString(), memberId.toString());
               return;
            }

            ry += 14;
         }

         ry = this.teamAvailableListStartY(team, listStartY);
         List<GuildHunter> avail = this.getAvailableHunters();
         int availListStartY = ry;
         int availEndY = this.guiTop + 220 - 6;
         if (team.memberIds().size() >= 5) {
            return;
         }

         for (int i = 0; i < avail.size(); i++) {
            int rowY = availListStartY + i * 14 - this.availableHuntersScroll;
            if (rowY + 14 >= availListStartY && rowY <= availEndY) {
               int bx = this.guiLeft + 340 - 20;
               if (mx >= bx && mx <= bx + 14 && my >= rowY + 1 && my <= rowY + 12) {
                  this.sendAction("assign_hunter", team.id().toString(), avail.get(i).id.toString());
                  return;
               }
            }
         }
      }
   }

   private void handleDungeonsClick(double mx, double my) {
      int contentY = this.guiTop + 4 + 20 + 4;
      int selectorY = contentY + 2;
      int headerY = selectorY + 22;
      int listStartY = headerY + 14;
      int divX = this.guiLeft + 166;
      if (my >= selectorY && my <= selectorY + 18) {
         if (mx >= this.guiLeft + 6 && mx <= this.guiLeft + 18) {
            if (!this.menu.teams.isEmpty()) {
               this.deployTeamIdx = (this.deployTeamIdx - 1 + this.menu.teams.size()) % this.menu.teams.size();
            }

            return;
         }

         int arrowRightX = divX - 18;
         if (mx >= arrowRightX && mx <= arrowRightX + 12) {
            if (!this.menu.teams.isEmpty()) {
               this.deployTeamIdx = (this.deployTeamIdx + 1) % this.menu.teams.size();
            }

            return;
         }
      }

      if (mx < divX) {
         int rowH = 20;

         for (int i = 0; i < this.menu.nearbyGates.size(); i++) {
            int ry = listStartY + i * rowH;
            int bx = divX - 46;
            if (mx >= bx && mx <= bx + 40 && my >= ry + 2 && my <= ry + 16) {
               if (!this.menu.teams.isEmpty()) {
                  GuildComputerMenu.TeamInfo team = this.menu.teams.get(Math.min(this.deployTeamIdx, this.menu.teams.size() - 1));
                  if (!this.isTeamDeployed(team.id())) {
                     this.sendAction("deploy_team", team.id().toString(), this.menu.nearbyGates.get(i).entityId().toString());
                  }
               }

               return;
            }
         }

         int gateListH = Math.max(this.menu.nearbyGates.size(), 1) * 20;
         int simY = listStartY + gateListH + 8 + 12;
         int simBtnW = 24;
         String[] simRanks = new String[]{"E", "D", "C", "B", "A", "S"};

         for (int r = 0; r < simRanks.length; r++) {
            int bx = this.guiLeft + 6 + r * (simBtnW + 3);
            if (mx >= bx && mx <= bx + simBtnW && my >= simY && my <= simY + 16) {
               if (!this.menu.teams.isEmpty()) {
                  GuildComputerMenu.TeamInfo team = this.menu.teams.get(Math.min(this.deployTeamIdx, this.menu.teams.size() - 1));
                  if (!this.isTeamDeployed(team.id())) {
                     this.sendAction("deploy_team", team.id().toString(), "sim:" + (r + 1));
                  }
               }

               return;
            }
         }
      }

      if (mx > divX) {
         int rowH = 26;

         for (int i = 0; i < this.menu.deployments.size(); i++) {
            int ry = listStartY + i * rowH - this.deploymentsScroll;
            int bx = this.guiLeft + 340 - 46;
            if (mx >= bx && mx <= bx + 40 && my >= ry + 8 && my <= ry + 22) {
               this.sendAction("recall_team", this.menu.deployments.get(i).id().toString(), "");
               return;
            }
         }
      }
   }

   private void handleBuffsClick(double mx, double my) {
      int contentY = this.guiTop + 4 + 20 + 4;
      int slotY = contentY + 28;
      int slotW = 148;

      for (int slot = 1; slot <= 2; slot++) {
         int sx = this.guiLeft + 8 + (slot - 1) * (slotW + 18);
         boolean unlocked = slot == 1 || this.menu.guildLevel >= 10;
         if (unlocked && mx >= sx + slotW - 36 && mx <= sx + slotW - 6 && my >= slotY + 18 && my <= slotY + 32) {
            this.sendAction("set_buff", Integer.toString(slot), "0");
            return;
         }
      }

      int listY = contentY + 76;
      int rowH = 16;

      for (int i = 0; i < GuildBuffRegistry.all().size(); i++) {
         GuildBuffRegistry.GuildBuff buff = GuildBuffRegistry.all().get(i);
         int y = listY + i * rowH - this.buffsScroll;
         if (y + rowH >= listY && y <= this.buffListEndY()) {
            int bx = this.guiLeft + 340 - 54;
            boolean unlocked = this.menu.guildLevel >= buff.unlockLevel();
            boolean active = this.menu.activeBuffSlot1 == buff.id() || this.menu.activeBuffSlot2 == buff.id();
            if (unlocked && !active && mx >= bx && mx <= bx + 46 && my >= y + 1 && my <= y + 13) {
               int slot = this.menu.activeBuffSlot1 == 0 ? 1 : (this.menu.guildLevel >= 10 && this.menu.activeBuffSlot2 == 0 ? 2 : 1);
               this.sendAction("set_buff", Integer.toString(slot), Integer.toString(buff.id()));
               return;
            }
         }
      }
   }

   @Override
   public void render(GuiGraphics gg, int mouseX, int mouseY, float pt) {
      this.renderBackground(gg);
      ResponsiveGuiScale.Transform transform = this.responsiveTransform();
      int logicalMouseX = transform.logicalMouseX(mouseX);
      int logicalMouseY = transform.logicalMouseY(mouseY);
      ResponsiveGuiScale.push(gg, transform);
      super.render(gg, logicalMouseX, logicalMouseY, pt);
      ResponsiveGuiScale.pop(gg);
      if (this.activeTab == 4) {
         this.renderTooltip(gg, mouseX, mouseY);
      }

      if (this.activeTab == 5) {
         this.renderBuffTooltip(gg, logicalMouseX, logicalMouseY, mouseX, mouseY);
      }
   }

   private ResponsiveGuiScale.Transform responsiveTransform() {
      return ResponsiveGuiScale.fit(this.width, this.height, 348, 228);
   }

   @Override
   protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      this.localScreenTicks++;
   }

   @Override
   protected void renderBg(GuiGraphics gg, float pt, int mouseX, int mouseY) {
      this.fillRounded(gg, this.guiLeft, this.guiTop, 340, 220, -401600488, 6);
      this.drawBorder(gg, this.guiLeft, this.guiTop, 340, 220, -14013894, 1);
      if (!this.menu.hasGuild) {
         this.renderCreateGuildScreen(gg);
      } else if (!this.canAccessAnyTab()) {
         this.renderAccessDenied(gg);
      } else {
         this.renderTabs(gg, mouseX, mouseY);
         this.renderTabContent(gg, mouseX, mouseY, pt);
      }
   }

   private void renderCreateGuildScreen(GuiGraphics gg) {
      String t = "§eNo Guild Registered";
      String s = "§7Create a new guild for this computer:";
      gg.drawString(this.font, t, this.guiLeft + 170 - this.font.width(t) / 2, this.guiTop + 110 - 46, -1, false);
      gg.drawString(this.font, s, this.guiLeft + 170 - this.font.width(s) / 2, this.guiTop + 110 - 30, -1, false);
      this.nameBox.render(gg, 0, 0, 0.0F);
   }

   private void renderAccessDenied(GuiGraphics gg) {
      String m1 = "§cAccess Denied";
      String m2 = "§7You are not authorised to use this computer.";
      gg.drawString(this.font, m1, this.guiLeft + 170 - this.font.width(m1) / 2, this.guiTop + 110 - 10, -1, false);
      gg.drawString(this.font, m2, this.guiLeft + 170 - this.font.width(m2) / 2, this.guiTop + 110 + 6, -1, false);
   }

   private void renderTabs(GuiGraphics gg, int mx, int my) {
      int ty = this.guiTop + 4;

      for (int i = 0; i < TAB_LABELS.length; i++) {
         boolean active = i == this.activeTab;
         boolean access = this.canAccessTab(i);
         boolean hov = mx >= this.tabXPositions[i] && mx < this.tabXPositions[i] + this.tabWidths[i] && my >= ty && my <= ty + 20;
         int bg = active ? -14803410 : (hov && access ? -14013894 : -15461344);
         int text = !access ? -11184795 : (active ? -2043808 : -5592406);
         this.fillRounded(gg, this.tabXPositions[i], ty, this.tabWidths[i], 20, bg, 3);
         if (active) {
            gg.fill(this.tabXPositions[i] + 2, ty, this.tabXPositions[i] + this.tabWidths[i] - 2, ty + 2, -2043808);
         }

         int lw = this.font.width(TAB_LABELS[i]);
         gg.drawString(this.font, TAB_LABELS[i], this.tabXPositions[i] + (this.tabWidths[i] - lw) / 2, ty + 6, text, false);
      }

      gg.fill(this.guiLeft, this.guiTop + 4 + 20, this.guiLeft + 340, this.guiTop + 4 + 20 + 1, -14013894);
   }

   private void renderTabContent(GuiGraphics gg, int mx, int my, float pt) {
      int cy = this.guiTop + 4 + 20 + 4;
      int ch = 192;
      if (!this.canAccessTab(this.activeTab)) {
         this.renderComingSoon(gg, cy, "§cNo access to this tab.");
      } else {
         switch (this.activeTab) {
            case 0:
               this.renderOverview(gg, cy);
               break;
            case 1:
               this.renderRoster(gg, cy, ch, mx, my);
               break;
            case 2:
               this.renderTeams(gg, cy, ch, mx, my);
               break;
            case 3:
               this.renderDungeons(gg, cy, ch, mx, my);
               break;
            case 4:
               this.renderStorage(gg, cy);
               break;
            case 5:
               this.renderBuffs(gg, cy, mx, my);
               break;
            case 6:
               this.renderLeaderboard(gg, cy);
               break;
            case 7:
               this.renderManagement(gg, cy, ch, mx, my);
               break;
            default:
               this.renderComingSoon(gg, cy, "§7Coming in a future update.");
         }
      }
   }

   private void renderOverview(GuiGraphics gg, int startY) {
      int cx = this.guiLeft + 170;
      int y = startY + 8;
      this.drawCentered(gg, "§e§l" + this.menu.guildName, cx, y);
      y += 14;
      this.drawCentered(gg, this.guildLevelBadge(this.menu.guildLevel) + " §7Level " + this.menu.guildLevel, cx, y);
      y += 14;
      this.drawCentered(gg, "§7Owner: §f" + this.menu.ownerName, cx, y);
      y += 14;
      int barW = 200;
      int barH = 8;
      int barX = cx - barW / 2;
      gg.fill(barX, y, barX + barW, y + barH, -14540240);
      if (this.menu.xpToNext > 0L) {
         gg.fill(barX, y, barX + (int)(barW * Math.min((double)this.menu.guildXp / this.menu.xpToNext, 1.0)), y + barH, -10436512);
      }

      this.drawBorder(gg, barX, y, barW, barH, -12961206, 1);
      String xpStr = "§7XP: §a" + this.menu.guildXp + " §7/ §a" + this.menu.xpToNext;
      this.drawCentered(gg, xpStr, cx, y + barH + 3);
      y += barH + 16;
      this.drawCentered(gg, "§7Total Gate Clears: §e" + this.menu.totalClears, cx, y);
      y += 14;
      this.drawCentered(gg, "§7Members: §f" + (this.menu.members.size() + 1), cx, y);
      y += 14;
      this.drawCentered(gg, "§7Hunters: §f" + this.menu.hunters.size() + " §8/ 25", cx, y);
   }

   private void renderRoster(GuiGraphics gg, int contentY, int contentH, int mx, int my) {
      int divX = this.guiLeft + 166;
      int listStartY = contentY + 14;
      gg.fill(divX, contentY, divX + 1, this.guiTop + 220 - 4, -14013894);
      gg.drawString(this.font, "§eHired §7(" + this.menu.hunters.size() + "/25)", this.guiLeft + 6, contentY + 2, -1, false);
      gg.fill(this.guiLeft + 4, contentY + 12, divX - 1, contentY + 13, -14013894);
      int rowH = 16;
      int leftEndY = this.guiTop + 220 - 6;
      this.enableScissor(gg, this.guiLeft + 4, listStartY, 160, leftEndY - listStartY);
      if (this.menu.hunters.isEmpty()) {
         gg.drawString(this.font, "§8No hunters hired yet.", this.guiLeft + 10, listStartY + 10, -1, false);
      } else {
         for (int i = 0; i < this.menu.hunters.size(); i++) {
            int ry = listStartY + i * rowH - this.rosterScrollOffset;
            if (ry + rowH >= listStartY && ry <= leftEndY) {
               GuildHunter h = this.menu.hunters.get(i);
               if (i % 2 == 0) {
                  gg.fill(this.guiLeft + 4, ry, divX - 1, ry + rowH - 1, 553648127);
               }

               gg.fill(this.guiLeft + 6, ry + 5, this.guiLeft + 9, ry + 8, "deployed".equals(h.status) ? -10432416 : -10461088);
               gg.drawString(
                  this.font,
                  GuildHunter.rankColor(h.rank)
                     + "["
                     + h.rank
                     + "] §f"
                     + (h.name.length() > 10 ? h.name.substring(0, 9) + "…" : h.name)
                     + " §7· "
                     + GuildHunter.classColor(h.hunterClass)
                     + h.hunterClass,
                  this.guiLeft + 11,
                  ry + 4,
                  -1,
                  false
               );
               if (this.menu.viewerIsOwner) {
                  int bx = divX - 18;
                  boolean hov = mx >= bx && mx <= bx + 14 && my >= ry + 1 && my <= ry + 13;
                  gg.fill(bx, ry + 1, bx + 14, ry + 13, hov ? -3399134 : -7855582);
                  gg.drawString(this.font, "×", bx + 3, ry + 2, -1, false);
               }
            }
         }
      }

      this.disableScissor(gg);
      int rPanelX = divX + 5;
      int poolListEndY = this.guiTop + 220 - 28;
      gg.drawString(this.font, "§eRecruit Pool", rPanelX, contentY + 2, -1, false);
      gg.fill(divX + 2, contentY + 12, this.guiLeft + 340 - 4, contentY + 13, -14013894);
      int rowH2 = 20;
      this.enableScissor(gg, divX + 2, listStartY, 166, poolListEndY - listStartY);
      if (this.menu.recruitPool.isEmpty()) {
         gg.drawString(this.font, "§8Pool is empty.", rPanelX, listStartY + 10, -1, false);
      } else {
         for (int i = 0; i < this.menu.recruitPool.size(); i++) {
            int ry = listStartY + i * rowH2 - this.rosterScrollOffset;
            if (ry + rowH2 >= listStartY && ry <= poolListEndY) {
               GuildHunter h = this.menu.recruitPool.get(i);
               if (i % 2 == 0) {
                  gg.fill(divX + 2, ry, this.guiLeft + 340 - 4, ry + rowH2 - 1, 553648127);
               }

               String nameTrim = h.name.length() > 9 ? h.name.substring(0, 8) + "…" : h.name;
               gg.drawString(
                  this.font,
                  GuildHunter.rankColor(h.rank) + "[" + h.rank + "] §f" + nameTrim + " §7· " + GuildHunter.classColor(h.hunterClass) + h.hunterClass,
                  rPanelX,
                  ry + 2,
                  -1,
                  false
               );
               String hireCost = this.minecraft.player != null && this.minecraft.player.isCreative()
                  ? "§aCost: FREE §7(Creative)"
                  : "§7Cost: §e" + GuildHunter.hireCost(h.rank) + " " + GuildHunter.rankColor(h.rank) + GuildHunter.hireMaterialName(h.rank);
               gg.drawString(this.font, hireCost, rPanelX, ry + 11, -1, false);
               if (this.menu.viewerIsOwner) {
                  int hx = this.guiLeft + 340 - 40;
                  boolean hov = mx >= hx && mx <= hx + 34 && my >= ry + 2 && my <= ry + 16;
                  gg.fill(hx, ry + 2, hx + 34, ry + 16, hov ? -12533696 : -14327771);
                  String lbl = "Hire";
                  gg.drawString(this.font, "§f" + lbl, hx + (34 - this.font.width(lbl)) / 2, ry + 5, -1, false);
               }
            }
         }
      }

      this.disableScissor(gg);
      if (this.menu.viewerIsOwner) {
         int rby = this.guiTop + 220 - 26;
         int rbx = rPanelX;
         int rbw = 162;
         boolean hovRef = mx >= rbx && mx <= rbx + rbw && my >= rby && my <= rby + 16;
         gg.fill(rbx, rby, rbx + rbw, rby + 16, hovRef ? -7311344 : -10466808);
         this.drawBorder(gg, rbx, rby, rbw, 16, -8359888, 1);
         String rl = "§eRefresh Pool §7(8x Gold Ingot)";
         gg.drawString(this.font, rl, rbx + (rbw - this.font.width(rl)) / 2, rby + 4, -1, false);
      }
   }

   private void renderTeams(GuiGraphics gg, int contentY, int contentH, int mx, int my) {
      int divX = this.guiLeft + 166;
      int listStartY = contentY + 14;
      gg.fill(divX, contentY, divX + 1, this.guiTop + 220 - 4, -14013894);
      gg.drawString(this.font, "§eTeams", this.guiLeft + 6, contentY + 2, -1, false);
      gg.fill(this.guiLeft + 4, contentY + 12, divX - 1, contentY + 13, -14013894);
      int rowH = 28;

      for (int i = 0; i < this.menu.teams.size(); i++) {
         GuildComputerMenu.TeamInfo t = this.menu.teams.get(i);
         int ry = listStartY + i * rowH;
         boolean selected = t.id().equals(this.selectedTeamId);
         boolean deployed = this.isTeamDeployed(t.id());
         if (selected) {
            gg.fill(this.guiLeft + 4, ry, divX - 1, ry + rowH - 2, 1088475232);
         } else if (i % 2 == 0) {
            gg.fill(this.guiLeft + 4, ry, divX - 1, ry + rowH - 2, 419430399);
         }

         String statusDot = deployed ? "§a● " : "§8○ ";
         gg.drawString(this.font, statusDot + "§e" + t.name(), this.guiLeft + 8, ry + 6, -1, false);
         String auto = t.autoRaidEnabled() ? " §bAuto<=" + GuildDeployment.rankLabel(t.autoRaidMaxRank()) : "";
         gg.drawString(
            this.font, "§7Hunters: §f" + t.memberIds().size() + "/5" + auto + (deployed ? " §a[Deployed]" : ""), this.guiLeft + 8, ry + 17, -1, false
         );
      }

      GuildComputerMenu.TeamInfo team = this.getSelectedTeam();
      int rPanelX = divX + 5;
      if (team == null) {
         gg.drawString(this.font, "§8← Select a team to manage it", rPanelX, contentY + 40, -1, false);
      } else {
         boolean deployed = this.isTeamDeployed(team.id());
         gg.drawString(this.font, "§e" + team.name() + " §7(" + team.memberIds().size() + "/5)", rPanelX, contentY + 2, -1, false);
         gg.fill(divX + 2, contentY + 12, this.guiLeft + 340 - 4, contentY + 13, -14013894);
         int ry = listStartY + 2;
         int autoY = ry;
         gg.drawString(this.font, "§7Auto raid:", rPanelX, autoY + 3, -1, false);
         int toggleX = this.guiLeft + 340 - 88;
         int rankX = this.guiLeft + 340 - 42;
         if (this.menu.viewerIsOwner) {
            boolean hovToggle = mx >= toggleX && mx <= toggleX + 42 && my >= autoY && my <= autoY + 14;
            gg.fill(
               toggleX, autoY, toggleX + 42, autoY + 14, team.autoRaidEnabled() ? (hovToggle ? -13661560 : -14719384) : (hovToggle ? -11908518 : -14013894)
            );
            this.drawBorder(gg, toggleX, autoY, 42, 14, -12526360, 1);
            String autoLabel = team.autoRaidEnabled() ? "ON" : "OFF";
            gg.drawString(this.font, "§f" + autoLabel, toggleX + (42 - this.font.width(autoLabel)) / 2, autoY + 3, -1, false);
            boolean hovRank = mx >= rankX && mx <= rankX + 34 && my >= autoY && my <= autoY + 14;
            gg.fill(rankX, autoY, rankX + 34, autoY + 14, hovRank ? -12566448 : -14342859);
            this.drawBorder(gg, rankX, autoY, 34, 14, -12526360, 1);
            String rankText = "<=" + GuildDeployment.rankLabel(team.autoRaidMaxRank());
            gg.drawString(this.font, this.gateColor(team.autoRaidMaxRank()) + rankText, rankX + (34 - this.font.width(rankText)) / 2, autoY + 3, -1, false);
         } else {
            gg.drawString(
               this.font,
               (team.autoRaidEnabled() ? "§bON" : "§8OFF")
                  + " §7<= "
                  + this.gateColor(team.autoRaidMaxRank())
                  + GuildDeployment.rankLabel(team.autoRaidMaxRank()),
               rPanelX + 58,
               autoY + 3,
               -1,
               false
            );
         }

         ry += 18;
         gg.drawString(this.font, "§7Members:", rPanelX, ry, -1, false);
         ry += 12;
         if (team.memberIds().isEmpty()) {
            gg.drawString(this.font, "§8No hunters assigned.", rPanelX + 4, ry, -1, false);
            ry += 12;
         } else {
            for (UUID mid : team.memberIds()) {
               GuildHunter h = this.findHunter(mid);
               if (h == null) {
                  ry += 14;
               } else {
                  if (this.menu.viewerIsOwner && !deployed) {
                     int bx = this.guiLeft + 340 - 20;
                     boolean hov = mx >= bx && mx <= bx + 14 && my >= ry + 1 && my <= ry + 12;
                     gg.fill(bx, ry + 1, bx + 14, ry + 12, hov ? -3399134 : -7855582);
                     gg.drawString(this.font, "×", bx + 3, ry + 1, -1, false);
                  }

                  gg.drawString(
                     this.font,
                     GuildHunter.rankColor(h.rank)
                        + "["
                        + h.rank
                        + "] §f"
                        + (h.name.length() > 12 ? h.name.substring(0, 11) + "…" : h.name)
                        + " §7· "
                        + GuildHunter.classColor(h.hunterClass)
                        + h.hunterClass,
                     rPanelX + 4,
                     ry + 2,
                     -1,
                     false
                  );
                  ry += 14;
               }
            }
         }

         if (deployed) {
            gg.drawString(this.font, "§a[Team is currently deployed]", rPanelX, ry + 4, -1, false);
         } else {
            gg.fill(divX + 2, ry + 2, this.guiLeft + 340 - 4, ry + 3, -14013894);
            ry += 8;
            gg.drawString(this.font, "§7Available hunters:", rPanelX, ry, -1, false);
            ry += 12;
            List<GuildHunter> avail = this.getAvailableHunters();
            int availListStartY = this.teamAvailableListStartY(team, listStartY);
            int availEndY = this.guiTop + 220 - 6;
            this.enableScissor(gg, divX + 2, availListStartY, 166, availEndY - availListStartY);
            if (avail.isEmpty()) {
               gg.drawString(this.font, "§8All hunters assigned.", rPanelX + 4, availListStartY + 2, -1, false);
            } else if (team.memberIds().size() >= 5) {
               gg.drawString(this.font, "§8Team is full.", rPanelX + 4, availListStartY + 2, -1, false);
            } else {
               for (int i = 0; i < avail.size(); i++) {
                  GuildHunter h = avail.get(i);
                  int rowY = availListStartY + i * 14 - this.availableHuntersScroll;
                  if (rowY + 14 >= availListStartY && rowY <= availEndY) {
                     if (this.menu.viewerIsOwner) {
                        int bx = this.guiLeft + 340 - 20;
                        boolean hov = mx >= bx && mx <= bx + 14 && my >= rowY + 1 && my <= rowY + 12;
                        gg.fill(bx, rowY + 1, bx + 14, rowY + 12, hov ? -12533696 : -14655456);
                        gg.drawString(this.font, "+", bx + 3, rowY + 1, -1, false);
                     }

                     gg.drawString(
                        this.font,
                        GuildHunter.rankColor(h.rank)
                           + "["
                           + h.rank
                           + "] §f"
                           + (h.name.length() > 12 ? h.name.substring(0, 11) + "…" : h.name)
                           + " §7· "
                           + GuildHunter.classColor(h.hunterClass)
                           + h.hunterClass,
                        rPanelX + 4,
                        rowY + 2,
                        -1,
                        false
                     );
                  }
               }
            }

            this.disableScissor(gg);
         }
      }
   }

   private void renderDungeons(GuiGraphics gg, int contentY, int contentH, int mx, int my) {
      int divX = this.guiLeft + 166;
      int selectorY = contentY + 2;
      int headerY = selectorY + 22;
      int listY = headerY + 14;
      gg.fill(divX, contentY, divX + 1, this.guiTop + 220 - 4, -14013894);
      gg.fill(this.guiLeft + 4, selectorY, divX - 1, selectorY + 18, -15066584);
      this.drawBorder(gg, this.guiLeft + 4, selectorY, 160, 18, -14013894, 1);
      boolean hovL = mx >= this.guiLeft + 6 && mx <= this.guiLeft + 18 && my >= selectorY && my <= selectorY + 18;
      gg.fill(this.guiLeft + 6, selectorY + 3, this.guiLeft + 18, selectorY + 15, hovL ? -12566448 : -14342859);
      gg.drawString(this.font, "◄", this.guiLeft + 7, selectorY + 5, -5592406, false);
      int arrowRx = divX - 18;
      boolean hovR = mx >= arrowRx && mx <= arrowRx + 12 && my >= selectorY && my <= selectorY + 18;
      gg.fill(arrowRx, selectorY + 3, arrowRx + 12, selectorY + 15, hovR ? -12566448 : -14342859);
      gg.drawString(this.font, "►", arrowRx + 1, selectorY + 5, -5592406, false);
      String teamLabel;
      if (this.menu.teams.isEmpty()) {
         teamLabel = "§8No teams";
      } else {
         int idx = Math.min(this.deployTeamIdx, this.menu.teams.size() - 1);
         GuildComputerMenu.TeamInfo t = this.menu.teams.get(idx);
         teamLabel = this.isTeamDeployed(t.id()) ? "§7" + t.name() + " §8[deployed]" : "§e" + t.name();
      }

      int tlw = this.font.width(teamLabel);
      gg.drawString(this.font, teamLabel, this.guiLeft + 4 + (160 - tlw) / 2 + 14, selectorY + 5, -1, false);
      gg.drawString(this.font, "§eNearby Gates", this.guiLeft + 6, headerY, -1, false);
      gg.fill(this.guiLeft + 4, headerY + 10, divX - 1, headerY + 11, -14013894);
      if (this.menu.nearbyGates.isEmpty()) {
         gg.drawString(this.font, "§8No gates detected.", this.guiLeft + 10, listY + 2, -1, false);
      } else {
         int rowH = 20;

         for (int i = 0; i < this.menu.nearbyGates.size(); i++) {
            GuildComputerMenu.NearbyGate gate = this.menu.nearbyGates.get(i);
            int ry = listY + i * rowH;
            if (i % 2 == 0) {
               gg.fill(this.guiLeft + 4, ry, divX - 1, ry + rowH - 1, 419430399);
            }

            int dist = (int)Math.sqrt(
               Math.pow(gate.pos().getX() - this.menu.computerPos.getX(), 2.0) + Math.pow(gate.pos().getZ() - this.menu.computerPos.getZ(), 2.0)
            );
            String label = this.trimToWidth(this.gateColor(gate.rank()) + gate.label(), divX - this.guiLeft - 56);
            gg.drawString(this.font, label, this.guiLeft + 7, ry + 3, -1, false);
            gg.drawString(this.font, "§8~" + dist + "m", this.guiLeft + 7, ry + 11, -1, false);
            if (this.menu.viewerIsOwner && !this.menu.teams.isEmpty()) {
               int bx = divX - 46;
               boolean canDeploy = !this.isTeamDeployed(this.menu.teams.get(Math.min(this.deployTeamIdx, this.menu.teams.size() - 1)).id());
               boolean hov = canDeploy && mx >= bx && mx <= bx + 40 && my >= ry + 2 && my <= ry + 16;
               gg.fill(bx, ry + 2, bx + 40, ry + 16, !canDeploy ? -13421757 : (hov ? -12533696 : -14327771));
               String dl = "Deploy";
               gg.drawString(this.font, !canDeploy ? "§8" + dl : "§f" + dl, bx + (40 - this.font.width(dl)) / 2, ry + 5, -1, false);
            }
         }
      }

      int gateListHeight = Math.max(this.menu.nearbyGates.size(), 1) * 20;
      int simLabelY = listY + gateListHeight + 8;
      int simY = simLabelY + 12;
      if (simY + 16 <= this.guiTop + 220 - 4) {
         gg.drawString(this.font, "§8Simulated Missions:", this.guiLeft + 6, simLabelY, -1, false);
         String[] simColors = new String[]{"§7", "§f", "§a", "§b", "§e", "§6"};
         String[] simLabels = new String[]{"E", "D", "C", "B", "A", "S"};
         int btnW = 24;

         for (int r = 0; r < 6; r++) {
            int bx = this.guiLeft + 6 + r * (btnW + 3);
            if (bx + btnW > divX - 4) {
               break;
            }

            boolean hov = mx >= bx && mx <= bx + btnW && my >= simY && my <= simY + 16;
            boolean canDeploy = !this.menu.teams.isEmpty()
               && !this.isTeamDeployed(this.menu.teams.get(Math.min(this.deployTeamIdx, this.menu.teams.size() - 1)).id());
            gg.fill(bx, simY, bx + btnW, simY + 16, !canDeploy ? -14342859 : (hov ? -12566448 : -14803410));
            this.drawBorder(gg, bx, simY, btnW, 16, -14013894, 1);
            gg.drawString(this.font, simColors[r] + simLabels[r], bx + (btnW - this.font.width(simLabels[r])) / 2, simY + 4, -1, false);
         }
      }

      int rPanelX = divX + 5;
      gg.drawString(this.font, "§eActive Missions", rPanelX, headerY, -1, false);
      gg.fill(divX + 2, headerY + 10, this.guiLeft + 340 - 4, headerY + 11, -14013894);
      if (this.menu.deployments.isEmpty()) {
         gg.drawString(this.font, "§8No active missions.", rPanelX, listY + 2, -1, false);
      } else {
         int rowH = 26;
         int depEndY = this.guiTop + 220 - 6;
         this.enableScissor(gg, divX + 2, listY, 166, depEndY - listY);

         for (int i = 0; i < this.menu.deployments.size(); i++) {
            GuildComputerMenu.DeploymentInfo dep = this.menu.deployments.get(i);
            int ry = listY + i * rowH - this.deploymentsScroll;
            if (ry + rowH >= listY && ry <= depEndY) {
               if (i % 2 == 0) {
                  gg.fill(divX + 2, ry, this.guiLeft + 340 - 4, ry + rowH - 1, 419430399);
               }

               long ticksLeft = dep.completesAt() - this.currentDisplayedServerGameTime();
               String timeStr = ticksLeft <= 0L ? "§aComplete!" : "§e" + this.formatTicks(ticksLeft);
               gg.drawString(this.font, "§e" + dep.teamName() + " §8→ §f" + dep.gateLabel(), rPanelX, ry + 2, -1, false);
               gg.drawString(this.font, "§7Time: " + timeStr, rPanelX, ry + 12, -1, false);
               if (this.menu.viewerIsOwner) {
                  int bx = this.guiLeft + 340 - 46;
                  boolean hov = mx >= bx && mx <= bx + 40 && my >= ry + 8 && my <= ry + 22;
                  gg.fill(bx, ry + 8, bx + 40, ry + 22, hov ? -5627358 : -8969694);
                  String rl = "Recall";
                  gg.drawString(this.font, "§f" + rl, bx + (40 - this.font.width(rl)) / 2, ry + 11, -1, false);
               }
            }
         }

         this.disableScissor(gg);
      }
   }

   private void renderStorage(GuiGraphics gg, int contentY) {
      String title = "§e§lGuild Storage";
      gg.drawString(this.font, title, this.guiLeft + 170 - this.font.width(title) / 2, contentY + 2, -1, false);

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            int sx = this.guiLeft + 89 + col * 18;
            int sy = this.guiTop + 46 + row * 18;
            gg.fill(sx - 1, sy - 1, sx + 17, sy + 17, -13421757);
            gg.fill(sx, sy, sx + 16, sy + 16, -11184795);
         }
      }

      int invLabelY = this.guiTop + 108 - 10;
      gg.drawString(this.font, "§7Inventory", this.guiLeft + 89, invLabelY, -5592406, false);
      gg.fill(this.guiLeft + 89, invLabelY + 6, this.guiLeft + 89 + 162, invLabelY + 7, -14013894);

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            int sx = this.guiLeft + 89 + col * 18;
            int sy = this.guiTop + 108 + row * 18;
            gg.fill(sx, sy, sx + 16, sy + 16, -11184795);
         }
      }

      int hsy = this.guiTop + 166;
      gg.fill(this.guiLeft + 89, hsy - 3, this.guiLeft + 89 + 162, hsy - 2, -14013894);

      for (int col = 0; col < 9; col++) {
         int sx = this.guiLeft + 89 + col * 18;
         gg.fill(sx, hsy, sx + 16, hsy + 16, -11184795);
      }
   }

   private void renderBuffs(GuiGraphics gg, int contentY, int mx, int my) {
      this.drawCentered(gg, "§e§lGuild Buffs", this.guiLeft + 170, contentY + 3);
      gg.drawString(this.font, "§7Active Slots", this.guiLeft + 8, contentY + 18, -1, false);
      int slotY = contentY + 28;
      int slotW = 148;
      this.drawBuffSlot(gg, 1, this.menu.activeBuffSlot1, this.guiLeft + 8, slotY, slotW, true, mx, my);
      this.drawBuffSlot(gg, 2, this.menu.activeBuffSlot2, this.guiLeft + 8 + slotW + 18, slotY, slotW, this.menu.guildLevel >= 10, mx, my);
      int listY = contentY + 76;
      gg.fill(this.guiLeft + 8, listY - 8, this.guiLeft + 340 - 8, listY - 7, -14013894);
      int rowH = 16;
      int listEndY = this.buffListEndY();
      this.buffsScroll = Math.max(0, Math.min(this.maxBuffsScroll(), this.buffsScroll));
      this.enableScissor(gg, this.guiLeft + 6, listY, 328, listEndY - listY);

      for (int i = 0; i < GuildBuffRegistry.all().size(); i++) {
         GuildBuffRegistry.GuildBuff buff = GuildBuffRegistry.all().get(i);
         int y = listY + i * rowH - this.buffsScroll;
         if (y + rowH >= listY && y <= listEndY) {
            boolean unlocked = this.menu.guildLevel >= buff.unlockLevel();
            boolean active = this.menu.activeBuffSlot1 == buff.id() || this.menu.activeBuffSlot2 == buff.id();
            if (i % 2 == 0) {
               gg.fill(this.guiLeft + 6, y - 1, this.guiLeft + 340 - 6, y + rowH - 2, 419430399);
            }

            gg.drawString(this.font, this.trimToWidth((unlocked ? "§f" : "§8") + buff.name(), 116), this.guiLeft + 10, y + 3, -1, false);
            gg.drawString(this.font, unlocked ? "§7Lv " + buff.unlockLevel() : "§8Lv " + buff.unlockLevel(), this.guiLeft + 132, y + 3, -1, false);
            gg.drawString(this.font, this.trimToWidth((unlocked ? "§7" : "§8") + buff.description(), 112), this.guiLeft + 164, y + 3, -1, false);
            if (this.menu.viewerIsOwner) {
               int bx = this.guiLeft + 340 - 54;
               boolean hov = unlocked && !active && mx >= bx && mx <= bx + 46 && my >= y + 1 && my <= y + 13;
               int color = unlocked && !active ? (hov ? -12562312 : -14272424) : -14342859;
               gg.fill(bx, y + 1, bx + 46, y + 13, color);
               String text = active ? "Active" : (unlocked ? "Equip" : "Locked");
               gg.drawString(this.font, (unlocked && !active ? "§f" : "§8") + text, bx + (46 - this.font.width(text)) / 2, y + 3, -1, false);
            }
         }
      }

      this.disableScissor(gg);
      if (this.maxBuffsScroll() > 0) {
         int trackX = this.guiLeft + 340 - 6;
         int visibleH = listEndY - listY;
         int contentH = GuildBuffRegistry.all().size() * 16;
         int knobH = Math.max(12, visibleH * visibleH / contentH);
         int knobY = listY + (visibleH - knobH) * this.buffsScroll / this.maxBuffsScroll();
         gg.fill(trackX, listY, trackX + 1, listEndY, -14272424);
         gg.fill(trackX - 1, knobY, trackX + 2, knobY + knobH, -12535553);
      }
   }

   private void drawBuffSlot(GuiGraphics gg, int slot, int buffId, int x, int y, int w, boolean unlocked, int mx, int my) {
      gg.fill(x, y, x + w, y + 38, unlocked ? -15329242 : -15658730);
      this.drawBorder(gg, x, y, w, 38, unlocked ? -12957072 : -14342859, 1);
      gg.drawString(this.font, (unlocked ? "§b" : "§8") + "Slot " + slot, x + 6, y + 5, -1, false);
      String name = unlocked ? GuildBuffRegistry.displayName(buffId) : "Unlocks at guild level 10";
      gg.drawString(this.font, (buffId != 0 && unlocked ? "§f" : "§8") + this.trimToWidth(name, w - 46), x + 6, y + 19, -1, false);
      if (this.menu.viewerIsOwner && unlocked) {
         boolean hov = mx >= x + w - 36 && mx <= x + w - 6 && my >= y + 18 && my <= y + 32;
         gg.fill(x + w - 36, y + 18, x + w - 6, y + 32, hov ? -7846844 : -11195091);
         gg.drawString(this.font, "§fClear", x + w - 33, y + 21, -1, false);
      }
   }

   private void renderManagement(GuiGraphics gg, int startY, int contentH, int mx, int my) {
      if (!this.menu.viewerIsOwner) {
         this.renderComingSoon(gg, startY, "§cOnly the guild owner can access this tab.");
      } else {
         int startX = this.guiLeft + 8;
         int nameColW = 82;
         int checkGap = 28;
         int rowH = 18;
         String[] pLabels = new String[]{"Op", "Ov", "Ro", "Tm", "Du", "St", "Bf", "Lb"};
         int founderY = startY + 2;
         gg.drawString(this.font, "Â§7Founder", startX, founderY, -1, false);
         gg.drawString(this.font, "Â§d" + this.menu.ownerName, startX + 58, founderY, -1, false);
         gg.fill(this.guiLeft + 8, founderY + 12, this.guiLeft + 340 - 8, founderY + 13, -12966838);
         int hy = startY + 26;
         gg.drawString(this.font, "§7Member", startX, hy, -1, false);

         for (int c = 0; c < pLabels.length; c++) {
            gg.drawString(this.font, "§8" + pLabels[c], startX + nameColW + c * checkGap, hy, -1, false);
         }

         gg.fill(this.guiLeft + 8, hy + 10, this.guiLeft + 340 - 8, hy + 11, -14013894);
         int listY = startY + 38;
         int listEndY = this.guiTop + 220 - 42;
         this.enableScissor(gg, this.guiLeft + 4, listY, 332, listEndY - listY);

         for (int row = 0; row < this.menu.members.size(); row++) {
            int ry = listY + row * rowH - this.managementScrollOffset;
            if (ry + rowH >= listY && ry <= listEndY) {
               GuildMemberPermissions p = this.menu.members.get(row);
               if (row % 2 == 0) {
                  gg.fill(this.guiLeft + 6, ry, this.guiLeft + 340 - 6, ry + rowH - 1, 553648127);
               }

               String dn = p.playerName.length() > 14 ? p.playerName.substring(0, 13) + "…" : p.playerName;
               gg.drawString(this.font, "§f" + dn, startX, ry + 5, -1, false);
               boolean[] vals = new boolean[]{p.canOpen, p.tabOverview, p.tabRoster, p.tabTeams, p.tabDungeons, p.tabStorage, p.tabBuffs, p.tabLeaderboard};

               for (int c = 0; c < vals.length; c++) {
                  int cx = startX + nameColW + c * checkGap + 4;
                  int cy = ry + 4;
                  boolean chk = vals[c];
                  gg.fill(cx, cy, cx + 10, cy + 10, chk ? -11485104 : -13421757);
                  this.drawBorder(gg, cx, cy, 10, 10, -11908518, 1);
                  if (chk) {
                     gg.drawString(this.font, "✓", cx + 1, cy, -1, false);
                  }
               }

               int rx = this.guiLeft + 340 - 20;
               boolean hx = mx >= rx && mx <= rx + 12 && my >= ry + 3 && my <= ry + 13;
               gg.fill(rx, ry + 3, rx + 12, ry + 13, hx ? -5627358 : -8969694);
               gg.drawString(this.font, "×", rx + 2, ry + 3, -1, false);
            }
         }

         this.disableScissor(gg);
         gg.fill(this.guiLeft + 6, this.guiTop + 220 - 42, this.guiLeft + 340 - 6, this.guiTop + 220 - 41, -14013894);
         gg.drawString(this.font, "§7Invite player:", startX, this.guiTop + 220 - 38, -1, false);
         this.addMemberBox.render(gg, mx, my, 0.0F);
      }
   }

   private void renderLeaderboard(GuiGraphics gg, int startY) {
      int cx = this.guiLeft + 170;
      int y = startY + 4;
      this.drawCentered(gg, "§e§lGuild Leaderboard", cx, y);
      y += 14;
      gg.drawString(this.font, "§8#", this.guiLeft + 12, y, -1, false);
      gg.drawString(this.font, "§8Name", this.guiLeft + 28, y, -1, false);
      gg.drawString(this.font, "§8Lvl", this.guiLeft + 340 - 80, y, -1, false);
      gg.drawString(this.font, "§8Clears", this.guiLeft + 340 - 50, y, -1, false);
      gg.fill(this.guiLeft + 8, y + 10, this.guiLeft + 340 - 8, y + 11, -14013894);
      y += 14;
      if (this.menu.leaderboard.isEmpty()) {
         this.drawCentered(gg, "§7No guilds yet.", cx, y + 20);
      } else {
         for (int i = 0; i < this.menu.leaderboard.size(); i++) {
            GuildComputerMenu.LeaderboardEntry e = this.menu.leaderboard.get(i);
            int ry = y + i * 16;
            if (e.isOwnGuild()) {
               gg.fill(this.guiLeft + 8, ry - 1, this.guiLeft + 340 - 8, ry + 11, 635490400);
            }
            String rank = switch (i) {
               case 0 -> "§6#1";
               case 1 -> "§7#2";
               case 2 -> "§c#3";
               default -> "§8#" + (i + 1);
            };
            gg.drawString(this.font, rank, this.guiLeft + 12, ry, -1, false);
            gg.drawString(this.font, (e.isOwnGuild() ? "§e" : "§f") + e.name(), this.guiLeft + 28, ry, -1, false);
            gg.drawString(this.font, "§a" + e.level(), this.guiLeft + 340 - 80, ry, -1, false);
            gg.drawString(this.font, "§f" + e.clears(), this.guiLeft + 340 - 50, ry, -1, false);
         }
      }
   }

   private void renderComingSoon(GuiGraphics gg, int y, String msg) {
      gg.drawString(this.font, msg, this.guiLeft + 170 - this.font.width(msg) / 2, y + 40, -1, false);
   }

   private boolean canAccessTab(int tab) {
      if (this.menu.viewerIsOwner) {
         return true;
      }

      GuildMemberPermissions p = this.getViewerPerms();
      if (p == null) {
         return false;
      }

      if (p.canOpen) {
         return true;
      }

      return switch (tab) {
         case 0 -> p.tabOverview;
         case 1 -> p.tabRoster;
         case 2 -> p.tabTeams;
         case 3 -> p.tabDungeons;
         case 4 -> p.tabStorage;
         case 5 -> p.tabBuffs;
         case 6 -> p.tabLeaderboard;
         case 7 -> false;
         default -> false;
      };
   }

   private boolean canAccessAnyTab() {
      if (this.menu.viewerIsOwner) {
         return true;
      }

      GuildMemberPermissions p = this.getViewerPerms();
      return p != null && (p.canOpen || p.tabOverview || p.tabRoster || p.tabTeams || p.tabDungeons || p.tabStorage || p.tabBuffs || p.tabLeaderboard);
   }

   private GuildMemberPermissions getViewerPerms() {
      for (GuildMemberPermissions p : this.menu.members) {
         if (p.playerUUID.equals(this.menu.viewerUUID)) {
            return p;
         }
      }

      return null;
   }

   private GuildComputerMenu.TeamInfo getSelectedTeam() {
      if (this.selectedTeamId == null) {
         return null;
      }

      for (GuildComputerMenu.TeamInfo t : this.menu.teams) {
         if (t.id().equals(this.selectedTeamId)) {
            return t;
         }
      }

      return null;
   }

   private int teamAvailableListStartY(GuildComputerMenu.TeamInfo team, int listStartY) {
      int ry = listStartY + 2;
      ry += 18;
      ry += 12;
      ry += team.memberIds().isEmpty() ? 12 : team.memberIds().size() * 14;
      ry += 8;
      return ry + 12;
   }

   private List<GuildHunter> getAvailableHunters() {
      Set<UUID> inTeam = new HashSet<>();

      for (GuildComputerMenu.TeamInfo t : this.menu.teams) {
         inTeam.addAll(t.memberIds());
      }

      List<GuildHunter> avail = new ArrayList<>();

      for (GuildHunter h : this.menu.hunters) {
         if (!inTeam.contains(h.id)) {
            avail.add(h);
         }
      }

      return avail;
   }

   private GuildHunter findHunter(UUID id) {
      for (GuildHunter h : this.menu.hunters) {
         if (h.id.equals(id)) {
            return h;
         }
      }

      return null;
   }

   private int buffListEndY() {
      return this.guiTop + 220 - 8;
   }

   private int maxBuffsScroll() {
      int contentY = this.guiTop + 4 + 20 + 4;
      int listY = contentY + 76;
      int visibleH = Math.max(1, this.buffListEndY() - listY);
      return Math.max(0, GuildBuffRegistry.all().size() * 16 - visibleH);
   }

   private void renderBuffTooltip(GuiGraphics gg, int mx, int my, int screenMouseX, int screenMouseY) {
      int contentY = this.guiTop + 4 + 20 + 4;
      int listY = contentY + 76;
      int listEndY = this.buffListEndY();
      if (mx >= this.guiLeft + 6 && mx <= this.guiLeft + 340 - 6 && my >= listY && my <= listEndY) {
         int rowH = 16;

         for (int i = 0; i < GuildBuffRegistry.all().size(); i++) {
            GuildBuffRegistry.GuildBuff buff = GuildBuffRegistry.all().get(i);
            int y = listY + i * rowH - this.buffsScroll;
            if (my >= y && my <= y + rowH) {
               boolean unlocked = this.menu.guildLevel >= buff.unlockLevel();
               boolean active = this.menu.activeBuffSlot1 == buff.id() || this.menu.activeBuffSlot2 == buff.id();
               List<Component> lines = new ArrayList<>();
               lines.add(Component.literal("§b" + buff.name()));
               lines.add(Component.literal((unlocked ? "§7Unlocked" : "§8Locked") + " §7at Guild Level " + buff.unlockLevel()));

               for (String line : this.wrapTooltipText(buff.description(), 190)) {
                  lines.add(Component.literal((unlocked ? "§f" : "§8") + line));
               }

               if (active) {
                  lines.add(Component.literal("§aCurrently active."));
               } else if (unlocked && this.menu.viewerIsOwner) {
                  lines.add(Component.literal("§7Click Equip to activate this passive."));
               }

               SystemTooltip.render(gg, this.font, lines, screenMouseX, screenMouseY, this.width, this.height);
               return;
            }
         }
      }
   }

   private List<String> wrapTooltipText(String text, int maxWidth) {
      List<String> lines = new ArrayList<>();
      StringBuilder current = new StringBuilder();

      for (String word : text.split(" ")) {
         String next = current.length() == 0 ? word : current + " " + word;
         if (this.font.width(next) > maxWidth && current.length() > 0) {
            lines.add(current.toString());
            current = new StringBuilder(word);
         } else {
            current = new StringBuilder(next);
         }
      }

      if (current.length() > 0) {
         lines.add(current.toString());
      }

      return lines;
   }

   private boolean isTeamDeployed(UUID teamId) {
      for (GuildComputerMenu.DeploymentInfo d : this.menu.deployments) {
         if (d.teamId().equals(teamId)) {
            return true;
         }
      }

      return false;
   }

   private int nextIdleTeamIndexAfter(UUID justDeployedTeamId) {
      if (this.menu.teams.isEmpty()) {
         return 0;
      }

      int start = Math.max(0, Math.min(this.deployTeamIdx, this.menu.teams.size() - 1));

      for (int offset = 1; offset <= this.menu.teams.size(); offset++) {
         int idx = (start + offset) % this.menu.teams.size();
         UUID teamId = this.menu.teams.get(idx).id();
         if (!teamId.equals(justDeployedTeamId) && !this.isTeamDeployed(teamId)) {
            return idx;
         }
      }

      return start;
   }

   private boolean teamExists(UUID teamId) {
      for (GuildComputerMenu.TeamInfo t : this.menu.teams) {
         if (t.id().equals(teamId)) {
            return true;
         }
      }

      return false;
   }

   private String formatTicks(long ticks) {
      long secs = ticks / 20L;
      return secs / 60L + "m " + secs % 60L + "s";
   }

   private long currentDisplayedServerGameTime() {
      return this.menu.serverGameTime + this.localScreenTicks;
   }

   private String gateColor(int rank) {
      return switch (rank) {
         case 2 -> "§f";
         case 3 -> "§a";
         case 4 -> "§b";
         case 5 -> "§e";
         case 6 -> "§6";
         default -> "§7";
      };
   }

   private String trimToWidth(String text, int maxWidth) {
      if (this.font.width(text) <= maxWidth) {
         return text;
      }

      String suffix = "...";
      String raw = text;

      while (!raw.isEmpty() && this.font.width(raw + suffix) > maxWidth) {
         raw = raw.substring(0, raw.length() - 1);
      }

      return raw + suffix;
   }

   private void sendAction(String action, String p1, String p2) {
      pendingGuildId = this.menu.hasGuild ? this.menu.guildId : null;
      pendingActiveTab = this.activeTab;
      pendingSelectedTeamId = this.selectedTeamId;
      pendingDeployTeamIdx = this.deployTeamIdx;
      if ("deploy_team".equals(action)) {
         try {
            pendingDeployTeamIdx = this.nextIdleTeamIndexAfter(UUID.fromString(p1));
         } catch (IllegalArgumentException var5) {
         }
      }

      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.SERVER.noArg(), new GuildActionMessage(action, this.menu.computerPos, p1, p2));
   }

   private void drawCentered(GuiGraphics gg, String text, int cx, int y) {
      gg.drawString(this.font, text, cx - this.font.width(text) / 2, y, -1, false);
   }

   private void fillRounded(GuiGraphics gg, int x, int y, int w, int h, int color, int r) {
      gg.fill(x + r, y, x + w - r, y + h, color);
      gg.fill(x, y + r, x + r, y + h - r, color);
      gg.fill(x + w - r, y + r, x + w, y + h - r, color);
   }

   private void drawBorder(GuiGraphics gg, int x, int y, int w, int h, int color, int t) {
      gg.fill(x, y, x + w, y + t, color);
      gg.fill(x, y + h - t, x + w, y + h, color);
      gg.fill(x, y, x + t, y + h, color);
      gg.fill(x + w - t, y, x + w, y + h, color);
   }

   private void enableScissor(GuiGraphics gg, int x, int y, int w, int h) {
      ResponsiveGuiScale.enableScissor(gg, this.responsiveTransform(), x, y, x + w, y + h);
   }

   private void disableScissor(GuiGraphics gg) {
      gg.disableScissor();
   }

   private String guildLevelBadge(int level) {
      return switch (level) {
         case 1 -> "§7[E]";
         case 2 -> "§7[D]";
         case 3 -> "§a[C]";
         case 4 -> "§b[B]";
         case 5 -> "§e[A]";
         default -> "§6[S]";
      };
   }
}
