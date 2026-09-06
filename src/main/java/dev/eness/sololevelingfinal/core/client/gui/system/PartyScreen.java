package dev.eness.sololevelingfinal.core.client.gui.system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.PartyClientState;
import dev.eness.sololevelingfinal.core.network.AbilitiesGUIButtonMessage;

public class PartyScreen extends SystemScreen {
   private static final int SECTION_FILL = -1979314913;
   private static final int SECTION_INNER = 1242571576;
   private static final int OFFLINE = -10785416;
   private static final int WARNING = -9882;
   private static final int DANGER = -41625;
   private static final int CONSTELLATION_X = 10;
   private static final int CONSTELLATION_Y = 46;
   private static final int CONSTELLATION_W = 320;
   private static final int CONSTELLATION_H = 174;
   private static final int SIGNALS_X = 336;
   private static final int SIGNALS_Y = 46;
   private static final int SIGNALS_W = 214;
   private static final int SIGNALS_H = 174;
   private static final int CONTROL_Y = 226;
   private static final int CONTROL_H = 116;
   private static final int PERIODIC_REFRESH_TICKS = 100;
   private static final int GLOW_COMMIT_DELAY_TICKS = 4;
   private static final int GLOW_ACK_TIMEOUT_TICKS = 100;
   private final boolean returnToSkills;
   private PartyClientState.Snapshot party = PartyClientState.snapshot();
   private UUID selectedMemberId;
   private UUID selectedPartyId;
   private UUID selectedRequestId;
   private EditBox partyName;
   private String draftPartyName = "";
   private int nearbyPage;
   private int requestPage;
   private int localGlowColor;
   private boolean localGlowEnabled;
   private boolean glowEditDirty;
   private boolean awaitingGlowAck;
   private boolean pendingGlowEnabled;
   private int pendingGlowColor;
   private int glowEditIdleTicks;
   private int glowAckTicks;
   private int refreshTicks;
   private boolean deferredRebuild;
   private String confirmationAction = "";
   private UUID confirmationTarget;

   public PartyScreen(boolean returnToSkills) {
      super(Component.literal("PARTY LINK"));
      this.returnToSkills = returnToSkills;
      this.panelW = 560;
      this.panelH = 350;
      this.localGlowColor = this.party.glowColor();
      this.localGlowEnabled = this.party.glowEnabled();
   }

   @Override
   protected void init() {
      boolean restoreNameFocus = this.partyName != null && this.partyName.isFocused();
      this.captureDraftName();
      super.init();
      this.party = PartyClientState.snapshot();
      if (!this.glowEditDirty && !this.awaitingGlowAck) {
         this.localGlowColor = this.party.glowColor();
         this.localGlowEnabled = this.party.glowEnabled();
      }

      this.validateSelections();
      this.buildWidgets();
      if (restoreNameFocus && this.partyName != null) {
         this.setInitialFocus(this.partyName);
      }

      PartyClientState.requestSnapshot();
   }

   public void onPartyStateChanged(PartyClientState.Snapshot updated) {
      boolean activeScreen = this.minecraft != null && this.minecraft.screen == this;
      if (activeScreen) {
         this.captureDraftName();
      }

      PartyClientState.Snapshot next = updated == null ? PartyClientState.snapshot() : updated;
      if (this.awaitingGlowAck && next.glowEnabled() == this.pendingGlowEnabled && next.glowColor() == this.pendingGlowColor) {
         this.awaitingGlowAck = false;
         this.glowAckTicks = 0;
      }

      boolean preserveGlowDraft = this.glowEditDirty || this.awaitingGlowAck;
      this.party = next;
      if (!preserveGlowDraft) {
         this.localGlowColor = this.party.glowColor();
         this.localGlowEnabled = this.party.glowEnabled();
      }

      this.validateSelections();
      if (activeScreen) {
         if (this.hasActiveDraftInteraction()) {
            this.deferredRebuild = true;
         } else {
            this.deferredRebuild = false;
            this.clearWidgets();
            this.buildWidgets();
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.glowEditDirty) {
         this.glowEditIdleTicks++;
         if (this.glowEditIdleTicks >= 4) {
            this.commitGlowEdit();
         }
      }

      if (this.awaitingGlowAck && ++this.glowAckTicks >= 100) {
         this.awaitingGlowAck = false;
         this.glowAckTicks = 0;
         this.localGlowColor = this.party.glowColor();
         this.localGlowEnabled = this.party.glowEnabled();
         this.deferredRebuild = true;
      }

      if (this.deferredRebuild && !this.hasActiveDraftInteraction()) {
         this.deferredRebuild = false;
         this.rebuildPartyWidgets();
      }

      if (++this.refreshTicks >= 100) {
         this.refreshTicks = 0;
         if (!this.hasActiveDraftInteraction() && !this.awaitingGlowAck) {
            PartyClientState.requestSnapshot();
         }
      }
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   private void buildWidgets() {
      this.partyName = null;
      this.addRenderableWidget(new SystemScreen.SystemButton(this.panelX + 3, this.panelY + 3, 46, 12, Component.literal("< Back"), button -> this.goBack()));
      this.addRenderableWidget(new SystemScreen.SystemButton(this.panelX + this.panelW - 53, this.panelY + 3, 50, 12, Component.literal("Refresh"), button -> {
         this.clearConfirmation();
         PartyClientState.refresh();
      }));
      if (this.party.inParty()) {
         this.buildMemberNodes();
      } else {
         this.buildCreateControls();
      }

      this.buildNearbyControls();
      if (this.isLeader()) {
         this.buildRequestControls();
      }

      this.buildManagementControls();
      if (this.party.inParty()) {
         this.buildGlowControls();
      }
   }

   private void buildCreateControls() {
      int x = this.panelX + 10 + 24;
      int y = this.panelY + 46 + 82;
      this.partyName = new EditBox(this.font, x, y, 190, 20, Component.literal("Party name"));
      this.partyName.setMaxLength(24);
      this.partyName.setHint(Component.literal("Name your link").withStyle(style -> style.withColor(-7358248)));
      this.partyName.setTextColor(-1509633);
      this.partyName.setBordered(true);
      this.partyName.setValue(this.draftPartyName);
      this.addRenderableWidget(this.partyName);
      this.addRenderableWidget(new SystemScreen.SystemButton(x + 196, y, 76, 20, Component.literal("Create"), button -> this.createParty()));
   }

   private void buildMemberNodes() {
      for (PartyScreen.NodePlacement placement : this.nodePlacements()) {
         PartyClientState.Member member = placement.member();
         this.addRenderableWidget(
            new PartyScreen.MemberNodeButton(
               this.panelX + placement.x(), this.panelY + placement.y(), 88, 27, member, Objects.equals(this.selectedMemberId, member.id()), button -> {
                  this.clearConfirmation();
                  this.selectedMemberId = member.id();
                  this.rebuildPartyWidgets();
               }
            )
         );
      }
   }

   private void buildNearbyControls() {
      List<PartyClientState.NearbyParty> nearby = this.party.nearby();
      int pageCount = Math.max(1, (nearby.size() + 2) / 3);
      this.nearbyPage = Math.max(0, Math.min(this.nearbyPage, pageCount - 1));
      int start = this.nearbyPage * 3;
      int rows = Math.min(3, nearby.size() - start);

      for (int index = 0; index < rows; index++) {
         PartyClientState.NearbyParty signal = nearby.get(start + index);
         int y = this.panelY + 46 + 20 + index * 22;
         this.addRenderableWidget(
            new PartyScreen.NearbySignalButton(this.panelX + 336 + 8, y, 198, 20, signal, Objects.equals(this.selectedPartyId, signal.id()), button -> {
               this.clearConfirmation();
               this.selectedPartyId = signal.id();
               this.rebuildPartyWidgets();
            })
         );
      }

      if (pageCount > 1) {
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               this.panelX + 336 + 214 - 46, this.panelY + 46 + 2, 17, 12, Component.literal("<"), button -> this.changeNearbyPage(-1)
            )
         );
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               this.panelX + 336 + 214 - 26, this.panelY + 46 + 2, 17, 12, Component.literal(">"), button -> this.changeNearbyPage(1)
            )
         );
      }

      PartyClientState.NearbyParty selected = this.selectedNearby();
      if (!this.party.inParty() && selected != null) {
         String label = selected.requested() ? "Cancel Request" : (selected.available() ? "Request Link" : "Signal Unavailable");
         this.addRenderableWidget(
            new PartyScreen.SignalActionButton(
               this.panelX + 336 + 8,
               this.panelY + 46 + 89,
               198,
               18,
               Component.literal(label),
               selected.requested() || selected.available(),
               button -> this.requestJoin(selected)
            )
         );
      }
   }

   private void buildRequestControls() {
      List<PartyClientState.JoinRequest> requests = this.party.requests();
      int pageCount = Math.max(1, requests.size());
      this.requestPage = Math.max(0, Math.min(this.requestPage, pageCount - 1));
      int start = this.requestPage;
      int rows = Math.min(1, requests.size() - start);

      for (int index = 0; index < rows; index++) {
         PartyClientState.JoinRequest request = requests.get(start);
         int y = this.panelY + 46 + 134;
         this.addRenderableWidget(
            new PartyScreen.RequestSignalButton(this.panelX + 336 + 8, y, 198, 17, request, Objects.equals(this.selectedRequestId, request.id()), button -> {
               this.clearConfirmation();
               this.selectedRequestId = request.id();
               this.rebuildPartyWidgets();
            })
         );
      }

      if (pageCount > 1) {
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               this.panelX + 336 + 214 - 46, this.panelY + 46 + 118, 17, 12, Component.literal("<"), button -> this.changeRequestPage(-1)
            )
         );
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               this.panelX + 336 + 214 - 26, this.panelY + 46 + 118, 17, 12, Component.literal(">"), button -> this.changeRequestPage(1)
            )
         );
      }

      PartyClientState.JoinRequest selected = this.selectedRequest();
      if (selected != null) {
         int x = this.panelX + 336 + 8;
         int y = this.panelY + 46 + 154;
         this.addRenderableWidget(new SystemScreen.SystemButton(x, y, 94, 14, Component.literal("Accept"), button -> {
            this.clearConfirmation();
            this.playerAction("accept_request", selected.id());
         }));
         this.addRenderableWidget(
            new PartyScreen.DestructiveButton(
               x + 100,
               y,
               98,
               14,
               Component.literal(this.confirmationLabel("deny_request", selected.id(), "Deny", "Confirm Deny")),
               button -> this.confirmAction("deny_request", selected.id())
            )
         );
      }
   }

   private void buildManagementControls() {
      int x = this.panelX + 18;
      int y = this.panelY + 226 + 42;
      if (this.party.inParty()) {
         if (this.isLeader()) {
            this.addRenderableWidget(
               new SystemScreen.SystemButton(x, y, 112, 18, Component.literal(this.party.discoverable() ? "Signal: Public" : "Signal: Hidden"), button -> {
                  this.clearConfirmation();
                  PartyClientState.sendAction("toggle_discoverable");
               })
            );
            this.addRenderableWidget(
               new PartyScreen.DestructiveButton(
                  x + 130,
                  y,
                  112,
                  18,
                  Component.literal(this.confirmationLabel("disband", null, "Disband", "Confirm Disband")),
                  button -> this.confirmAction("disband", null)
               )
            );
         } else {
            this.addRenderableWidget(
               new PartyScreen.DestructiveButton(
                  x + 130,
                  y,
                  112,
                  18,
                  Component.literal(this.confirmationLabel("leave", null, "Leave Party", "Confirm Leave")),
                  button -> this.confirmAction("leave", null)
               )
            );
         }

         PartyClientState.Member selected = this.selectedMember();
         if (this.isLeader() && selected != null && !selected.leader() && !this.isLocalPlayer(selected.id())) {
            if (selected.online()) {
               this.addRenderableWidget(
                  new PartyScreen.DestructiveButton(
                     x,
                     y + 25,
                     112,
                     18,
                     Component.literal(this.confirmationLabel("transfer_leader", selected.id(), "Transfer Lead", "Confirm Transfer")),
                     button -> this.confirmAction("transfer_leader", selected.id())
                  )
               );
            }

            this.addRenderableWidget(
               new PartyScreen.DestructiveButton(
                  x + 130,
                  y + 25,
                  112,
                  18,
                  Component.literal(this.confirmationLabel("kick_member", selected.id(), "Remove Member", "Confirm Remove")),
                  button -> this.confirmAction("kick_member", selected.id())
               )
            );
         }
      }
   }

   private void buildGlowControls() {
      int x = this.panelX + 284;
      int y = this.panelY + 226 + 20;
      this.addRenderableWidget(
         new SystemScreen.SystemButton(x, y, 90, 18, Component.literal(this.localGlowEnabled ? "Outline: ON" : "Outline: OFF"), button -> {
            this.clearConfirmation();
            this.sendGlow(!this.localGlowEnabled, this.localGlowColor);
         })
      );
      int[] presets = new int[]{4179711, 16767334, 11106303, 16735118};
      String[] labels = new String[]{"CYAN", "GOLD", "VIOLET", "ROSE"};

      for (int index = 0; index < presets.length; index++) {
         int color = presets[index];
         this.addRenderableWidget(new PartyScreen.ColorPresetButton(x + index * 62, y + 23, 57, 17, Component.literal(labels[index]), color, button -> {
            this.clearConfirmation();
            this.localGlowColor = color;
            this.sendGlow(this.localGlowEnabled, color);
         }));
      }

      this.addRenderableWidget(this.rgbSlider(x, y + 46, "R", 16));
      this.addRenderableWidget(this.rgbSlider(x, y + 63, "G", 8));
      this.addRenderableWidget(this.rgbSlider(x, y + 80, "B", 0));
   }

   private SystemScreen.SystemSlider rgbSlider(int x, int y, String channel, int shift) {
      int value = this.localGlowColor >> shift & 0xFF;
      return new SystemScreen.SystemSlider(x, y, 258, 14, value / 255.0, position -> Component.literal(channel + " " + channelValue(position)), position -> {
         int channelValue = channelValue(position);
         this.localGlowColor = this.localGlowColor & ~(255 << shift) | channelValue << shift;
         this.glowEditDirty = true;
         this.glowEditIdleTicks = 0;
      }, this::commitGlowEdit);
   }

   private void rebuildPartyWidgets() {
      this.captureDraftName();
      this.clearWidgets();
      this.buildWidgets();
   }

   private void captureDraftName() {
      if (this.partyName != null) {
         this.draftPartyName = this.partyName.getValue();
      }
   }

   private void changeNearbyPage(int direction) {
      this.clearConfirmation();
      int pageCount = Math.max(1, (this.party.nearby().size() + 2) / 3);
      this.nearbyPage = Math.floorMod(this.nearbyPage + direction, pageCount);
      int start = this.nearbyPage * 3;
      this.selectedPartyId = this.party.nearby().isEmpty() ? null : this.party.nearby().get(Math.min(start, this.party.nearby().size() - 1)).id();
      this.rebuildPartyWidgets();
   }

   private void changeRequestPage(int direction) {
      this.clearConfirmation();
      int pageCount = Math.max(1, this.party.requests().size());
      this.requestPage = Math.floorMod(this.requestPage + direction, pageCount);
      int start = this.requestPage;
      this.selectedRequestId = this.party.requests().isEmpty() ? null : this.party.requests().get(Math.min(start, this.party.requests().size() - 1)).id();
      this.rebuildPartyWidgets();
   }

   private void createParty() {
      if (this.partyName != null) {
         String name = this.partyName.getValue().trim();
         if (!name.isEmpty()) {
            CompoundTag payload = new CompoundTag();
            payload.putString("Name", name);
            PartyClientState.sendAction("create", payload);
         }
      }
   }

   private void requestJoin(PartyClientState.NearbyParty selected) {
      if (selected.id() != null && (selected.requested() || selected.available())) {
         CompoundTag payload = new CompoundTag();
         payload.putUUID("PartyId", selected.id());
         PartyClientState.sendAction(selected.requested() ? "cancel_request" : "request_join", payload);
      }
   }

   private void playerAction(String action, UUID playerId) {
      if (playerId != null) {
         CompoundTag payload = new CompoundTag();
         payload.putUUID("PlayerId", playerId);
         PartyClientState.sendAction(action, payload);
      }
   }

   private void sendGlow(boolean enabled, int color) {
      this.localGlowEnabled = enabled;
      this.localGlowColor = color & 16777215;
      this.pendingGlowEnabled = enabled;
      this.pendingGlowColor = this.localGlowColor;
      this.glowEditDirty = false;
      this.glowEditIdleTicks = 0;
      this.awaitingGlowAck = true;
      this.glowAckTicks = 0;
      CompoundTag payload = new CompoundTag();
      payload.putBoolean("Enabled", enabled);
      payload.putInt("Color", this.localGlowColor);
      PartyClientState.sendAction("set_glow", payload);
   }

   private void commitGlowEdit() {
      if (this.glowEditDirty) {
         this.sendGlow(this.localGlowEnabled, this.localGlowColor);
      }
   }

   private void confirmAction(String action, UUID target) {
      if (Objects.equals(this.confirmationAction, action) && Objects.equals(this.confirmationTarget, target)) {
         this.clearConfirmation();
         if (target == null) {
            PartyClientState.sendAction(action);
         } else {
            this.playerAction(action, target);
         }
      } else {
         this.confirmationAction = action;
         this.confirmationTarget = target;
         this.rebuildPartyWidgets();
      }
   }

   private String confirmationLabel(String action, UUID target, String normal, String confirmation) {
      return Objects.equals(this.confirmationAction, action) && Objects.equals(this.confirmationTarget, target) ? confirmation : normal;
   }

   private void clearConfirmation() {
      this.confirmationAction = "";
      this.confirmationTarget = null;
   }

   private boolean hasActiveDraftInteraction() {
      return this.glowEditDirty || !this.party.inParty() && this.partyName != null && this.partyName.isFocused();
   }

   private void goBack() {
      if (this.minecraft != null) {
         if (!this.returnToSkills) {
            this.openChild(new SystemPanelScreen());
         } else {
            Player player = this.minecraft.player;
            if (player != null) {
               BlockPos position = player.blockPosition();
               this.minecraft.setScreen(null);
               SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(5, position.getX(), position.getY(), position.getZ()));
            }
         }
      }
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.drawSection(graphics, this.panelX + 10, this.panelY + 46, 320, 174, "LINKED CONSTELLATION");
      this.drawSection(graphics, this.panelX + 336, this.panelY + 46, 214, 174, "NEARBY SIGNALS");
      this.drawSection(graphics, this.panelX + 10, this.panelY + 226, 260, 116, this.party.inParty() ? "PARTY PROTOCOL" : "ESTABLISH LINK");
      this.drawSection(graphics, this.panelX + 276, this.panelY + 226, 274, 116, "OUTLINE CHANNEL");
      this.drawTopStatus(graphics);
      if (this.party.inParty()) {
         this.drawConstellation(graphics);
      } else {
         this.drawCreateState(graphics);
      }

      this.drawSignalsState(graphics);
      this.drawManagementState(graphics);
      this.drawGlowState(graphics);
   }

   private void drawTopStatus(GuiGraphics graphics) {
      String state = this.party.inParty()
         ? this.party.partyName() + "  //  " + this.party.members().size() + "/" + this.party.maxMembers()
         : "NO ACTIVE LINK  //  SELECT A SIGNAL OR CREATE YOUR OWN";
      graphics.drawString(this.font, this.fit(state, this.panelW - 24), this.panelX + 12, this.panelY + 27, this.party.inParty() ? -12597505 : -7358248, false);
      String notice = this.party.notice();
      if (!notice.isEmpty()) {
         graphics.drawString(
            this.font,
            this.fit(notice, this.panelW - 24),
            this.panelX + this.panelW - 12 - this.font.width(this.fit(notice, this.panelW - 24)),
            this.panelY + 37,
            -9882,
            false
         );
      }
   }

   private void drawCreateState(GuiGraphics graphics) {
      int x = this.panelX + 10;
      int y = this.panelY + 46;
      this.drawCentered(graphics, "CREATE A NEW PARTY LINK", x, y + 40, 320, -1509633);
      this.drawCentered(graphics, "You control visibility, membership and outline channel.", x, y + 56, 320, -7358248);
      this.drawCircuitBrackets(graphics, x + 22, y + 74, 276, 40);
      this.drawCentered(graphics, "Nearby public signals remain visible on the right.", x, y + 132, 320, -7358248);
   }

   private void drawConstellation(GuiGraphics graphics) {
      List<PartyScreen.NodePlacement> placements = this.nodePlacements();
      PartyScreen.NodePlacement leader = placements.stream()
         .filter(placementx -> placementx.member().leader())
         .findFirst()
         .orElse(placements.isEmpty() ? null : placements.get(0));
      if (leader != null) {
         int coreX = this.panelX + leader.x() + 44;
         int coreY = this.panelY + leader.y() + 13;

         for (PartyScreen.NodePlacement placement : placements) {
            if (placement != leader) {
               int nodeX = this.panelX + placement.x() + 44;
               int nodeY = this.panelY + placement.y() + 13;
               this.drawSignalLine(graphics, coreX, coreY, nodeX, nodeY, placement.member().online() ? 1430243071 : 861629816);
            }
         }

         graphics.fill(coreX - 2, coreY - 2, coreX + 3, coreY + 3, 1430243071);
      }

      if (this.party.members().size() > placements.size()) {
         String overflow = "+" + (this.party.members().size() - placements.size()) + " REMOTE";
         graphics.drawString(this.font, overflow, this.panelX + 10 + 320 - 62, this.panelY + 46 + 174 - 12, -7358248, false);
      }
   }

   private void drawSignalsState(GuiGraphics graphics) {
      int x = this.panelX + 336;
      int y = this.panelY + 46;
      if (this.party.nearby().isEmpty()) {
         this.drawCentered(graphics, "NO PUBLIC SIGNALS IN RANGE", x, y + 40, 214, -10785416);
      } else if (this.party.nearby().size() > 3) {
         int pages = (this.party.nearby().size() + 2) / 3;
         String page = this.nearbyPage + 1 + "/" + pages;
         graphics.drawString(this.font, page, x + 214 - 52 - this.font.width(page), y + 5, -7358248, false);
      }

      graphics.fill(x + 8, y + 115, x + 214 - 8, y + 116, 1430243071);
      graphics.drawString(this.font, "REQUEST INBOX", x + 8, y + 121, this.isLeader() ? -12597505 : -10785416, false);
      if (this.isLeader() && this.party.requests().size() > 1) {
         String page = this.requestPage + 1 + "/" + this.party.requests().size();
         graphics.drawString(this.font, page, x + 214 - 52 - this.font.width(page), y + 121, -7358248, false);
      }

      if (!this.party.inParty()) {
         graphics.drawString(this.font, "Join a party to receive requests.", x + 8, y + 140, -10785416, false);
      } else if (!this.isLeader()) {
         graphics.drawString(this.font, "Leader channel only.", x + 8, y + 140, -10785416, false);
      } else if (this.party.requests().isEmpty()) {
         graphics.drawString(this.font, "No pending requests.", x + 8, y + 140, -7358248, false);
      }
   }

   private void drawManagementState(GuiGraphics graphics) {
      int x = this.panelX + 18;
      int y = this.panelY + 226;
      if (!this.party.inParty()) {
         graphics.drawString(this.font, this.fit("Create from the constellation console above.", 230), x, y + 19, -7358248, false);
         graphics.drawString(this.font, "Public visibility starts enabled.", x, y + 76, -7358248, false);
         graphics.drawString(this.font, "The leader can change it at any time.", x, y + 88, -7358248, false);
      } else {
         PartyClientState.Member selected = this.selectedMember();
         String leader = this.party.leaderName().isEmpty() ? "Unknown" : this.party.leaderName();
         graphics.drawString(this.font, "LEADER  " + this.fit(leader, 115), x, y + 19, -7358248, false);
         if (selected != null) {
            String detail = selected.name() + "  //  LV." + selected.level() + (selected.rank().isEmpty() ? "" : "  " + selected.rank());
            graphics.drawString(this.font, this.fit(detail, 230), x, y + 31, selected.online() ? -1509633 : -10785416, false);
         } else {
            graphics.drawString(this.font, "Select a constellation node to manage it.", x, y + 31, -7358248, false);
         }

         if (!this.confirmationAction.isEmpty()) {
            graphics.drawString(this.font, "Press the highlighted action again to confirm.", x, y + 94, -9882, false);
         }
      }
   }

   private void drawGlowState(GuiGraphics graphics) {
      int x = this.panelX + 284;
      int y = this.panelY + 226;
      if (!this.party.inParty()) {
         graphics.drawString(this.font, "OUTLINE CHANNEL OFFLINE", x, y + 28, -10785416, false);
         graphics.drawString(this.font, "Party members can share a personal", x, y + 48, -7358248, false);
         graphics.drawString(this.font, "client-side outline once linked.", x, y + 60, -7358248, false);
      } else {
         int previewX = x + 102;
         int previewY = y + 20;
         int previewColor = 0xFF000000 | this.localGlowColor;
         graphics.fill(previewX, previewY, previewX + 156, previewY + 18, -1442642158);
         graphics.fill(previewX + 1, previewY + 1, previewX + 155, previewY + 2, previewColor);
         graphics.fill(previewX + 1, previewY + 16, previewX + 155, previewY + 17, previewColor);
         graphics.fill(previewX + 1, previewY + 1, previewX + 2, previewY + 17, previewColor);
         graphics.fill(previewX + 154, previewY + 1, previewX + 155, previewY + 17, previewColor);
         String hex = String.format("#%06X", this.localGlowColor & 16777215);
         graphics.drawCenteredString(this.font, hex, previewX + 78, previewY + 5, -1509633);
      }
   }

   private void drawSection(GuiGraphics graphics, int x, int y, int width, int height, String label) {
      graphics.fill(x, y, x + width, y + height, -1979314913);
      graphics.fill(x + 1, y + 17, x + width - 1, y + height - 1, 1242571576);
      outline(graphics, x, y, width, height, -14519384);
      graphics.fill(x, y + 16, x + width, y + 17, 1430243071);
      graphics.drawString(this.font, label, x + 8, y + 5, -12597505, false);
      graphics.fill(x + width - 20, y + 5, x + width - 8, y + 6, -14519384);
      graphics.fill(x + width - 8, y + 5, x + width - 7, y + 12, -12597505);
   }

   private void drawCircuitBrackets(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x, y, x + 22, y + 1, -14519384);
      graphics.fill(x, y, x + 1, y + 10, -14519384);
      graphics.fill(x + width - 22, y, x + width, y + 1, -14519384);
      graphics.fill(x + width - 1, y, x + width, y + 10, -14519384);
      graphics.fill(x, y + height - 1, x + 22, y + height, -14519384);
      graphics.fill(x, y + height - 10, x + 1, y + height, -14519384);
      graphics.fill(x + width - 22, y + height - 1, x + width, y + height, -14519384);
      graphics.fill(x + width - 1, y + height - 10, x + width, y + height, -14519384);
   }

   private void drawSignalLine(GuiGraphics graphics, int fromX, int fromY, int toX, int toY, int color) {
      int middleX = (fromX + toX) / 2;
      fillHorizontal(graphics, fromX, middleX, fromY, color);
      fillVertical(graphics, middleX, fromY, toY, color);
      fillHorizontal(graphics, middleX, toX, toY, color);
      graphics.fill(middleX - 1, toY - 1, middleX + 2, toY + 2, color);
   }

   private static void fillHorizontal(GuiGraphics graphics, int x0, int x1, int y, int color) {
      graphics.fill(Math.min(x0, x1), y, Math.max(x0, x1) + 1, y + 1, color);
   }

   private static void fillVertical(GuiGraphics graphics, int x, int y0, int y1, int color) {
      graphics.fill(x, Math.min(y0, y1), x + 1, Math.max(y0, y1) + 1, color);
   }

   private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      graphics.fill(x, y, x + width, y + 1, color);
      graphics.fill(x, y + height - 1, x + width, y + height, color);
      graphics.fill(x, y, x + 1, y + height, color);
      graphics.fill(x + width - 1, y, x + width, y + height, color);
   }

   private void drawCentered(GuiGraphics graphics, String text, int x, int y, int width, int color) {
      graphics.drawCenteredString(this.font, this.fit(text, width - 12), x + width / 2, y, color);
   }

   private String fit(String text, int maximumWidth) {
      if (this.font.width(text) <= maximumWidth) {
         return text;
      }

      String suffix = "...";
      return this.font.plainSubstrByWidth(text, Math.max(1, maximumWidth - this.font.width(suffix))) + suffix;
   }

   private List<PartyScreen.NodePlacement> nodePlacements() {
      List<PartyClientState.Member> ordered = new ArrayList<>(this.party.members());
      ordered.sort(Comparator.comparing(PartyClientState.Member::leader).reversed().thenComparing(PartyClientState.Member::name, String.CASE_INSENSITIVE_ORDER));
      int[][] positions = new int[][]{{126, 121}, {28, 74}, {224, 74}, {18, 158}, {234, 158}, {72, 187}, {180, 187}, {126, 70}};
      List<PartyScreen.NodePlacement> placements = new ArrayList<>();

      for (int index = 0; index < Math.min(positions.length, ordered.size()); index++) {
         placements.add(new PartyScreen.NodePlacement(ordered.get(index), positions[index][0], positions[index][1]));
      }

      return placements;
   }

   private boolean isLeader() {
      Player player = Minecraft.getInstance().player;
      return this.party.inParty() && player != null && Objects.equals(player.getUUID(), this.party.leaderId());
   }

   private boolean isLocalPlayer(UUID playerId) {
      Player player = Minecraft.getInstance().player;
      return player != null && Objects.equals(player.getUUID(), playerId);
   }

   private PartyClientState.Member selectedMember() {
      return this.party.members().stream().filter(member -> Objects.equals(member.id(), this.selectedMemberId)).findFirst().orElse(null);
   }

   private PartyClientState.NearbyParty selectedNearby() {
      return this.party.nearby().stream().filter(nearby -> Objects.equals(nearby.id(), this.selectedPartyId)).findFirst().orElse(null);
   }

   private PartyClientState.JoinRequest selectedRequest() {
      return this.party.requests().stream().filter(request -> Objects.equals(request.id(), this.selectedRequestId)).findFirst().orElse(null);
   }

   private void validateSelections() {
      if (this.party.inParty()) {
         this.draftPartyName = "";
      }

      if (this.selectedMember() == null) {
         this.selectedMemberId = this.party
            .members()
            .stream()
            .filter(PartyClientState.Member::leader)
            .map(PartyClientState.Member::id)
            .findFirst()
            .orElse(this.party.members().isEmpty() ? null : this.party.members().get(0).id());
      }

      if (this.selectedNearby() == null) {
         this.selectedPartyId = this.party.nearby().isEmpty() ? null : this.party.nearby().get(0).id();
      }

      int nearbyIndex = this.indexOfNearby(this.selectedPartyId);
      this.nearbyPage = nearbyIndex < 0 ? 0 : nearbyIndex / 3;
      if (this.selectedRequest() == null) {
         this.selectedRequestId = this.party.requests().isEmpty() ? null : this.party.requests().get(0).id();
      }

      int requestIndex = this.indexOfRequest(this.selectedRequestId);
      this.requestPage = requestIndex < 0 ? 0 : requestIndex;
      if (!this.confirmationAction.isEmpty()) {
         boolean leaderAction = this.confirmationAction.equals("deny_request")
            || this.confirmationAction.equals("transfer_leader")
            || this.confirmationAction.equals("kick_member")
            || this.confirmationAction.equals("disband");
         boolean targetStillValid = this.confirmationTarget == null
            ? this.party.inParty()
            : (
               this.confirmationAction.equals("deny_request")
                  ? this.indexOfRequest(this.confirmationTarget) >= 0
                  : this.party.members().stream().anyMatch(member -> Objects.equals(member.id(), this.confirmationTarget))
            );
         if (!targetStillValid || leaderAction && !this.isLeader()) {
            this.clearConfirmation();
         }
      }
   }

   private int indexOfNearby(UUID partyId) {
      for (int index = 0; index < this.party.nearby().size(); index++) {
         if (Objects.equals(this.party.nearby().get(index).id(), partyId)) {
            return index;
         }
      }

      return -1;
   }

   private int indexOfRequest(UUID playerId) {
      for (int index = 0; index < this.party.requests().size(); index++) {
         if (Objects.equals(this.party.requests().get(index).id(), playerId)) {
            return index;
         }
      }

      return -1;
   }

   private static int channelValue(double position) {
      return Math.max(0, Math.min(255, (int)Math.round(position * 255.0)));
   }

   private static String trim(Font font, String text, int width) {
      if (text == null) {
         return "";
      }

      if (font.width(text) <= width) {
         return text;
      }

      String suffix = "...";
      return font.plainSubstrByWidth(text, Math.max(1, width - font.width(suffix))) + suffix;
   }

   private static class ColorPresetButton extends Button {
      private final int color;

      ColorPresetButton(int x, int y, int width, int height, Component message, int color, OnPress onPress) {
         super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
         this.color = color;
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         int argb = 0xFF000000 | this.color;
         graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.isHoveredOrFocused() ? -1441257414 : -1727524834);
         PartyScreen.outline(graphics, this.getX(), this.getY(), this.width, this.height, this.isHoveredOrFocused() ? -1 : argb);
         graphics.fill(this.getX() + 2, this.getY() + this.height - 3, this.getX() + this.width - 2, this.getY() + this.height - 1, argb);
         graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + 3, -1509633);
      }
   }

   private static class DestructiveButton extends Button {
      DestructiveButton(int x, int y, int width, int height, Component message, OnPress onPress) {
         super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         boolean hovered = this.isHoveredOrFocused();
         int border = hovered ? -30063 : -6539968;
         graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, hovered ? -2008214243 : 1714426907);
         PartyScreen.outline(graphics, this.getX(), this.getY(), this.width, this.height, border);
         graphics.drawCenteredString(
            Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, hovered ? -1 : -41625
         );
      }
   }

   private static class MemberNodeButton extends Button {
      private final PartyClientState.Member member;
      private final boolean selected;

      MemberNodeButton(int x, int y, int width, int height, PartyClientState.Member member, boolean selected, OnPress onPress) {
         super(x, y, width, height, Component.literal(member.name()), onPress, DEFAULT_NARRATION);
         this.member = member;
         this.selected = selected;
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         int border = !this.selected && !this.isHoveredOrFocused() ? (this.member.online() ? -14519384 : -10785416) : -12597505;
         int fill = this.selected ? 2048275275 : -1341648866;
         graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, fill);
         PartyScreen.outline(graphics, this.getX(), this.getY(), this.width, this.height, border);
         int nodeColor = this.member.online() ? (this.member.leader() ? -9882 : -12597505) : -10785416;
         graphics.fill(this.getX() + 5, this.getY() + 8, this.getX() + 16, this.getY() + 19, -16644596);
         PartyScreen.outline(graphics, this.getX() + 5, this.getY() + 8, 11, 11, nodeColor);
         if (this.member.leader()) {
            graphics.fill(this.getX() + 8, this.getY() + 11, this.getX() + 13, this.getY() + 16, nodeColor);
         }

         Font font = Minecraft.getInstance().font;
         String name = PartyScreen.trim(font, this.member.name().isEmpty() ? "Unknown" : this.member.name(), 64);
         graphics.drawString(font, name, this.getX() + 21, this.getY() + 4, this.member.online() ? -1509633 : -10785416, false);
         String detail = "LV." + this.member.level() + (this.member.rank().isEmpty() ? "" : " " + this.member.rank());
         graphics.drawString(font, PartyScreen.trim(font, detail, 64), this.getX() + 21, this.getY() + 15, this.member.leader() ? -9882 : -7358248, false);
      }
   }

   private static class NearbySignalButton extends Button {
      private final PartyClientState.NearbyParty party;
      private final boolean selected;

      NearbySignalButton(int x, int y, int width, int height, PartyClientState.NearbyParty party, boolean selected, OnPress onPress) {
         super(x, y, width, height, Component.literal(party.name()), onPress, DEFAULT_NARRATION);
         this.party = party;
         this.selected = selected;
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         int border = !this.selected && !this.isHoveredOrFocused() ? -14519384 : -12597505;
         if (!this.party.available() && !this.party.requested()) {
            border = -10785416;
         }

         graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.selected ? 2048275275 : -1710747618);
         PartyScreen.outline(graphics, this.getX(), this.getY(), this.width, this.height, border);
         Font font = Minecraft.getInstance().font;
         graphics.drawString(
            font,
            PartyScreen.trim(font, this.party.name().isEmpty() ? "Unnamed Party" : this.party.name(), 105),
            this.getX() + 5,
            this.getY() + 3,
            -1509633,
            false
         );
         String state = this.party.available() ? this.party.distance() + "m" : (this.party.requested() ? "PENDING" : "CLOSED");
         String telemetry = this.party.members() + "/" + this.party.maxMembers() + "  " + state;
         int color = this.party.requested() ? -9882 : (this.party.available() ? -7358248 : -10785416);
         graphics.drawString(font, telemetry, this.getX() + this.width - font.width(telemetry) - 5, this.getY() + 3, color, false);
         graphics.drawString(font, "Lead: " + PartyScreen.trim(font, this.party.leaderName(), 70), this.getX() + 5, this.getY() + 12, -7358248, false);
      }
   }

   private record NodePlacement(PartyClientState.Member member, int x, int y) {
   }

   private static class RequestSignalButton extends Button {
      private final PartyClientState.JoinRequest request;
      private final boolean selected;

      RequestSignalButton(int x, int y, int width, int height, PartyClientState.JoinRequest request, boolean selected, OnPress onPress) {
         super(x, y, width, height, Component.literal(request.name()), onPress, DEFAULT_NARRATION);
         this.request = request;
         this.selected = selected;
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         int border = !this.selected && !this.isHoveredOrFocused() ? (this.request.online() ? -14519384 : -10785416) : -12597505;
         graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.selected ? 2048275275 : -2012737506);
         PartyScreen.outline(graphics, this.getX(), this.getY(), this.width, this.height, border);
         Font font = Minecraft.getInstance().font;
         graphics.drawString(
            font,
            PartyScreen.trim(font, this.request.name(), this.width - 40),
            this.getX() + 5,
            this.getY() + 5,
            this.request.online() ? -1509633 : -10785416,
            false
         );
         String state = this.request.online() ? "LIVE" : "AWAY";
         graphics.drawString(
            font, state, this.getX() + this.width - font.width(state) - 5, this.getY() + 5, this.request.online() ? -12597505 : -10785416, false
         );
      }
   }

   private static class SignalActionButton extends Button {
      private final boolean enabled;

      SignalActionButton(int x, int y, int width, int height, Component message, boolean enabled, OnPress onPress) {
         super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
         this.enabled = enabled;
      }

      @Override
      public void onPress() {
         if (this.enabled) {
            super.onPress();
         }
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         boolean hovered = this.enabled && this.isHoveredOrFocused();
         int border = !this.enabled ? -10785416 : (hovered ? -8395521 : -14519384);
         int fill = !this.enabled ? 1141905444 : (hovered ? -2142258968 : 1427120952);
         graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, fill);
         PartyScreen.outline(graphics, this.getX(), this.getY(), this.width, this.height, border);
         graphics.drawCenteredString(
            Minecraft.getInstance().font,
            this.getMessage(),
            this.getX() + this.width / 2,
            this.getY() + (this.height - 8) / 2,
            this.enabled ? -1509633 : -10785416
         );
      }
   }
}
