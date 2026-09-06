package dev.eness.sololevelingfinal.core.client.gui.dungeonbuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.gui.ResponsiveGuiScale;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.util.DungeonBuilderMode;

public final class DungeonBuilderStudioScreen extends SystemScreen {
   private static final int WIDE_WIDTH = 500;
   private static final int WIDE_HEIGHT = 338;
   private static final int MIN_WIDTH = 312;
   private static final int MIN_HEIGHT = 236;
   private static final int TAB_HEIGHT = 18;
   private static final int ROW_HEIGHT = 27;
   private static final int RAISED = -435416029;
   private static final int CANVAS = -268038888;
   private static final int BORDER = -1205701720;
   private static final int GRID = 539128744;
   private static final int SELECTED = 1883275168;
   private static final int SUCCESS = -10493044;
   private static final int WARNING = -11930;
   private static final int ERROR = -41620;
   private static final int INFO = -9971457;
   private static final int DISABLED = -10060918;
   private static final List<String> DUNGEON_RANKS = List.of("E", "D", "C", "B", "A", "S");
   private DungeonBuilderStudioModel model;
   private final DungeonBuilderStudioController controller;
   private DungeonBuilderStudioScreen.Tab activeTab = DungeonBuilderStudioScreen.Tab.ROOMS;
   private DungeonBuilderStudioScreen.CompactPane compactPane = DungeonBuilderStudioScreen.CompactPane.CANVAS;
   private boolean compact;
   private boolean requestSent;
   private boolean closeReported;
   private boolean canvasPanning;
   private boolean layoutNodeDragging;
   private boolean layoutDragBlocked;
   private DungeonBuilderStudioScreen.GraphMap layoutDragMap;
   private double layoutDragRemainderX;
   private double layoutDragRemainderZ;
   private boolean layoutDirty;
   private DungeonBuilderStudioScreen.Dialog activeDialog = DungeonBuilderStudioScreen.Dialog.NONE;
   private String selectedProjectId;
   private String selectedPoolId;
   private String selectedAnchorId = "";
   private String selectedSocketId = "";
   private String selectedSimRoomId = "";
   private String selectedLayoutNodeId = "";
   private String selectedLayoutSocketId = "";
   private String selectedDungeonDraftId = "";
   private String pendingConnectionNodeId = "";
   private String pendingConnectionSocketId = "";
   private String pendingPoolDelete = "";
   private String pendingProjectDelete = "";
   private long simulationSeed;
   private int catalogScroll;
   private EditBox seedBox;
   private EditBox poolIdBox;
   private EditBox roomIdBox;
   private EditBox selectorIdBox;
   private EditBox entryWeightBox;
   private EditBox requiredModBox;
   private EditBox eligibleMinBox;
   private EditBox eligibleMaxBox;
   private EditBox spawnMinBox;
   private EditBox spawnMaxBox;
   private EditBox baseXpBox;
   private EditBox anchorEncounterBox;
   private EditBox anchorMinLevelBox;
   private EditBox anchorMaxLevelBox;
   private EditBox shellBlockBox;
   private EditBox shellThicknessBox;
   private EditBox maxDepthBox;
   private EditBox newDungeonIdBox;
   private SystemScreen.SystemButton selectorKindButton;
   private SystemScreen.SystemButton projectKindButton;
   private SystemScreen.SystemButton eligibleRangeButton;
   private SystemScreen.SystemButton spawnRangeButton;
   private SystemScreen.SystemButton xpModeButton;
   private SystemScreen.SystemButton anchorLevelModeButton;
   private String poolIdDraft = "";
   private String roomIdDraft = "";
   private String selectorIdDraft = "minecraft:zombie";
   private String entryWeightDraft = "1";
   private String requiredModDraft = "";
   private String eligibleMinDraft = "1";
   private String eligibleMaxDraft = "1000";
   private String spawnMinDraft = "1";
   private String spawnMaxDraft = "1";
   private String baseXpDraft = "0";
   private String anchorEncounterDraft = "default";
   private String anchorMinLevelDraft = "1";
   private String anchorMaxLevelDraft = "1";
   private String shellBlockDraft = "minecraft:bedrock";
   private String shellThicknessDraft = "1";
   private String maxDepthDraft = "8";
   private String newDungeonIdDraft = "";
   private String editingSelectorId = "";
   private DungeonBuilderStudioModel.SelectorKind editingSelectorKind = DungeonBuilderStudioModel.SelectorKind.ENTITY;
   private DungeonBuilderStudioModel.SelectorKind selectorKindDraft = DungeonBuilderStudioModel.SelectorKind.ENTITY;
   private DungeonBuilderStudioModel.ProjectKind projectKindDraft = DungeonBuilderStudioModel.ProjectKind.MODULE;
   private boolean eligibleRangePresent;
   private boolean spawnRangePresent = true;
   private boolean baseXpPresent;
   private boolean anchorLevelOverrideDraft;
   private final Set<String> rankDraft = new LinkedHashSet<>();
   private String dialogError = "";
   private int entitySuggestionCursor;
   private final int[] leftScroll = new int[DungeonBuilderStudioScreen.Tab.values().length];
   private final int[] centerScroll = new int[DungeonBuilderStudioScreen.Tab.values().length];
   private final int[] inspectorScroll = new int[DungeonBuilderStudioScreen.Tab.values().length];
   private double canvasZoom = 1.0;
   private double canvasPanX;
   private double canvasPanY;
   private List<Component> hoverTooltip;
   private String localFeedback = "";
   private DungeonBuilderStudioModel.Severity localFeedbackSeverity = DungeonBuilderStudioModel.Severity.INFO;

   public DungeonBuilderStudioScreen(DungeonBuilderStudioModel model, DungeonBuilderStudioController controller) {
      super(Component.literal("DUNGEON BUILDER STUDIO"));
      this.panelW = 500;
      this.panelH = 338;
      this.model = model == null ? DungeonBuilderStudioModel.loadingState() : model;
      this.controller = controller == null ? DungeonBuilderStudioController.noop() : controller;
      this.selectedProjectId = preferredProjectId(this.model, this.model.selectedProjectId());
      this.selectedPoolId = preferredPoolId(this.model, this.model.selectedPoolId());
      this.selectedDungeonDraftId = preferredDraftId(this.model, this.model.dungeonId());
      this.simulationSeed = this.model.simulation().seed();
   }

   public void updateModel(DungeonBuilderStudioModel next) {
      this.captureDialogValues();
      String previousDungeonId = this.model.dungeonId();
      DungeonBuilderStudioModel replacement = next == null ? DungeonBuilderStudioModel.empty() : next;
      if (this.layoutDirty) {
         DungeonBuilderStudioModel.LayoutDraft localLayout = this.model.layout();
         String localDungeonId = this.model.dungeonId();
         if (replacement.layout().equals(localLayout) && replacement.dungeonId().equals(localDungeonId)) {
            this.layoutDirty = false;
         } else {
            replacement = new DungeonBuilderStudioModel(
               replacement.revision(),
               replacement.selectedProjectId(),
               replacement.selectedPoolId(),
               localDungeonId,
               replacement.loading(),
               replacement.projects(),
               replacement.pools(),
               localLayout,
               replacement.simulation(),
               replacement.validation(),
               replacement.notice(),
               replacement.dungeonDrafts()
            );
         }
      }

      this.model = replacement;
      this.selectedProjectId = preferredProjectId(
         replacement, replacement.project(this.selectedProjectId).isPresent() ? this.selectedProjectId : replacement.selectedProjectId()
      );
      this.selectedPoolId = preferredPoolId(replacement, replacement.pool(this.selectedPoolId).isPresent() ? this.selectedPoolId : replacement.selectedPoolId());
      if (!previousDungeonId.equals(replacement.dungeonId())) {
         this.selectedDungeonDraftId = preferredDraftId(replacement, replacement.dungeonId());
         this.selectedLayoutNodeId = "";
         this.selectedLayoutSocketId = "";
         this.clearPendingConnection();
         this.catalogScroll = 0;
      } else if (replacement.draft(this.selectedDungeonDraftId).isEmpty()) {
         this.selectedDungeonDraftId = preferredDraftId(replacement, replacement.dungeonId());
      }

      if (replacement.layout().nodes().stream().noneMatch(node -> node.id().equals(this.selectedLayoutNodeId))) {
         this.selectedLayoutNodeId = "";
         this.selectedLayoutSocketId = "";
      }

      if (replacement.simulation().status() != DungeonBuilderStudioModel.SimulationStatus.IDLE) {
         this.simulationSeed = replacement.simulation().seed();
      }

      if (this.minecraft != null && this.minecraft.screen == this) {
         this.rebuildWidgets();
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

   @Override
   protected void init() {
      if (this.minecraft != null && this.minecraft.level != null && DungeonBuilderMode.isActive(this.minecraft.level)) {
         this.compact = this.width < 520;
         this.panelW = this.compact ? Math.max(312, this.width - 8) : 500;
         this.panelH = Math.min(338, Math.max(236, this.height - 8));
         super.init();
         this.rebuildWidgets();
         if (!this.requestSent) {
            this.requestSent = true;
            this.controller.submit(new DungeonBuilderStudioController.RequestSnapshot());
         }
      } else {
         if (this.minecraft != null) {
            this.minecraft.setScreen(null);
         }
      }
   }

   @Override
   public void removed() {
      super.removed();
      if (!this.closeReported) {
         this.closeReported = true;
         this.controller.screenClosed();
      }
   }

   @Override
   protected void rebuildWidgets() {
      if (this.minecraft != null) {
         this.captureDialogValues();
         String seedValue = this.seedBox == null ? Long.toString(this.simulationSeed) : this.seedBox.getValue();
         this.clearWidgets();
         this.seedBox = null;
         this.clearDialogWidgetReferences();
         if (this.activeDialog != DungeonBuilderStudioScreen.Dialog.NONE) {
            this.buildDialogWidgets();
         } else {
            this.addRenderableWidget(
               new SystemScreen.SystemButton(this.panelX + this.panelW - 15, this.panelY + 3, 12, 12, Component.literal("X"), button -> this.beginClose())
            );
            int tabX = this.panelX + 6;
            int tabY = this.panelY + 21;
            int columns = this.compact ? 3 : DungeonBuilderStudioScreen.Tab.values().length;
            int rows = this.compact ? 2 : 1;
            int usable = this.panelW - 12;
            int tabW = usable / columns;

            for (int index = 0; index < DungeonBuilderStudioScreen.Tab.values().length; index++) {
               DungeonBuilderStudioScreen.Tab tab = DungeonBuilderStudioScreen.Tab.values()[index];
               int col = index % columns;
               int row = index / columns;
               int width = col == columns - 1 ? usable - col * tabW : tabW;
               this.addRenderableWidget(
                  new DungeonBuilderStudioScreen.StudioTabButton(
                     tabX + col * tabW, tabY + row * 18, width, 18, Component.literal(tab.name()), () -> this.activeTab == tab, () -> this.setTab(tab)
                  )
               );
            }

            DungeonBuilderStudioScreen.WorkspaceLayout layout = this.workspaceLayout();
            if (this.activeTab == DungeonBuilderStudioScreen.Tab.LAYOUT && this.canUseDungeonCatalog()) {
               int contextLeftWidth = Math.max(100, layout.context().w() * 62 / 100);
               this.addButton(layout.context().x() + contextLeftWidth - 70, layout.context().y() + 4, 66, 19, "Dungeons", this::openDungeonCatalog);
            }

            if (this.compact) {
               int half = layout.mainToggle().w() / 2;
               this.addRenderableWidget(
                  new DungeonBuilderStudioScreen.StudioTabButton(
                     layout.mainToggle().x(),
                     layout.mainToggle().y(),
                     half,
                     16,
                     Component.literal("CANVAS"),
                     () -> this.compactPane == DungeonBuilderStudioScreen.CompactPane.CANVAS,
                     () -> this.setCompactPane(DungeonBuilderStudioScreen.CompactPane.CANVAS)
                  )
               );
               this.addRenderableWidget(
                  new DungeonBuilderStudioScreen.StudioTabButton(
                     layout.mainToggle().x() + half,
                     layout.mainToggle().y(),
                     layout.mainToggle().w() - half,
                     16,
                     Component.literal("INSPECT"),
                     () -> this.compactPane == DungeonBuilderStudioScreen.CompactPane.INSPECTOR,
                     () -> this.setCompactPane(DungeonBuilderStudioScreen.CompactPane.INSPECTOR)
                  )
               );
            }

            this.buildFooterWidgets(layout.footer(), seedValue);
         }
      }
   }

   private void buildDialogWidgets() {
      DungeonBuilderStudioScreen.Rect dialog = this.dialogRect();
      switch (this.activeDialog) {
         case NEW_ROOM:
            this.buildNewRoomDialog(dialog);
            break;
         case NEW_POOL:
            this.buildNewPoolDialog(dialog);
            break;
         case POOL_ENTRY:
            this.buildPoolEntryDialog(dialog);
            break;
         case ANCHOR_SETUP:
            this.buildAnchorSetupDialog(dialog);
            break;
         case PRESET_SETUP:
            this.buildPresetSetupDialog(dialog);
            break;
         case DUNGEON_CATALOG:
            this.buildDungeonCatalogDialog(dialog);
            break;
         case NEW_DUNGEON:
            this.buildNewDungeonDialog(dialog);
            break;
         case DELETE_DUNGEON:
            this.buildDeleteDungeonDialog(dialog);
            break;
         case LAYOUT_SETUP:
            this.buildLayoutSetupDialog(dialog);
      }
   }

   private void buildNewRoomDialog(DungeonBuilderStudioScreen.Rect dialog) {
      this.roomIdBox = this.addDialogField(
         dialog.x() + 82, dialog.y() + 32, dialog.w() - 94, this.roomIdDraft, 81, DungeonBuilderStudioScreen::validRoomProjectDraft, "namespace:start_room"
      );
      this.projectKindButton = new SystemScreen.SystemButton(
         dialog.x() + 82, dialog.y() + 57, 92, 18, Component.literal("TYPE: " + this.projectKindDraft.name()), button -> this.toggleProjectKind()
      );
      this.addRenderableWidget(this.projectKindButton);
      int buttonY = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, buttonY, 66, 18, "Create", this::confirmCreateRoom);
      this.addButton(dialog.right() - 78, buttonY, 66, 18, "Cancel", this::closeDialog);
   }

   private void buildNewPoolDialog(DungeonBuilderStudioScreen.Rect dialog) {
      this.poolIdBox = this.addDialogField(
         dialog.x() + 82, dialog.y() + 34, dialog.w() - 94, this.poolIdDraft, 192, DungeonBuilderStudioScreen::validDungeonResourceDraft, "namespace:pool_name"
      );
      int buttonY = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, buttonY, 66, 18, "Create", this::confirmCreatePool);
      this.addButton(dialog.right() - 78, buttonY, 66, 18, "Cancel", this::closeDialog);
   }

   private void buildPoolEntryDialog(DungeonBuilderStudioScreen.Rect dialog) {
      int top = dialog.y() + 30;
      this.selectorKindButton = new SystemScreen.SystemButton(
         dialog.x() + 10, top, 70, 18, Component.literal(this.selectorKindLabel()), button -> this.toggleSelectorKind()
      );
      this.addRenderableWidget(this.selectorKindButton);
      this.selectorIdBox = this.addDialogField(
         dialog.x() + 85, top, dialog.w() - 153, this.selectorIdDraft, 192, DungeonBuilderStudioScreen::validDungeonResourceDraft, "namespace:entity_or_tag"
      );
      this.addButton(dialog.right() - 63, top, 53, 18, "Suggest", this::suggestEntityId);
      this.entryWeightBox = this.addDialogField(
         dialog.x() + 66, top + 27, 54, this.entryWeightDraft, 7, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "1"
      );
      this.requiredModBox = this.addDialogField(
         dialog.x() + 205, top + 27, Math.max(70, dialog.w() - 215), this.requiredModDraft, 64, DungeonBuilderStudioScreen::validModIdDraft, "leave_blank"
      );
      this.eligibleRangeButton = new SystemScreen.SystemButton(
         dialog.x() + 10, top + 54, 92, 18, Component.literal(this.eligibleRangeLabel()), button -> this.toggleEligibleRange()
      );
      this.addRenderableWidget(this.eligibleRangeButton);
      this.eligibleMinBox = this.addDialogField(
         dialog.x() + 112, top + 54, 48, this.eligibleMinDraft, 4, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "min"
      );
      this.eligibleMaxBox = this.addDialogField(
         dialog.x() + 170, top + 54, 48, this.eligibleMaxDraft, 4, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "max"
      );
      this.spawnRangeButton = new SystemScreen.SystemButton(
         dialog.x() + 10, top + 81, 92, 18, Component.literal(this.spawnRangeLabel()), button -> this.toggleSpawnRange()
      );
      this.addRenderableWidget(this.spawnRangeButton);
      this.spawnMinBox = this.addDialogField(
         dialog.x() + 112, top + 81, 48, this.spawnMinDraft, 4, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "min"
      );
      this.spawnMaxBox = this.addDialogField(
         dialog.x() + 170, top + 81, 48, this.spawnMaxDraft, 4, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "max"
      );
      this.xpModeButton = new SystemScreen.SystemButton(
         dialog.x() + 10, top + 108, 92, 18, Component.literal(this.xpModeLabel()), button -> this.toggleXpMode()
      );
      this.addRenderableWidget(this.xpModeButton);
      this.baseXpBox = this.addDialogField(
         dialog.x() + 112, top + 108, 72, this.baseXpDraft, 8, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "base XP"
      );
      int buttonY = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, buttonY, 66, 18, this.editingSelectorId.isBlank() ? "Add" : "Update", this::confirmPoolEntry);
      this.addButton(dialog.right() - 78, buttonY, 66, 18, "Cancel", this::closeDialog);
   }

   private void buildAnchorSetupDialog(DungeonBuilderStudioScreen.Rect dialog) {
      int top = dialog.y() + 30;
      this.anchorEncounterBox = this.addDialogField(
         dialog.x() + 98, top, dialog.w() - 108, this.anchorEncounterDraft, 64, DungeonBuilderStudioScreen::validEncounterDraft, "room_mobs"
      );
      if (!this.selectedAnchorIsTrigger()) {
         this.anchorLevelModeButton = new SystemScreen.SystemButton(
            dialog.x() + 10, top + 29, 126, 18, Component.literal(this.anchorLevelModeLabel()), button -> this.toggleAnchorLevelOverride()
         );
         this.addRenderableWidget(this.anchorLevelModeButton);
         this.anchorMinLevelBox = this.addDialogField(
            dialog.x() + 146, top + 29, 48, this.anchorMinLevelDraft, 4, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "min"
         );
         this.anchorMaxLevelBox = this.addDialogField(
            dialog.x() + 204, top + 29, 48, this.anchorMaxLevelDraft, 4, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "max"
         );
      }

      int buttonY = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, buttonY, 66, 18, "Apply", this::confirmAnchorSetup);
      this.addButton(dialog.right() - 78, buttonY, 66, 18, "Cancel", this::closeDialog);
   }

   private void buildLayoutSetupDialog(DungeonBuilderStudioScreen.Rect dialog) {
      int top = dialog.y() + 30;
      int rankX = dialog.x() + 72;
      this.addRenderableWidget(
         new DungeonBuilderStudioScreen.StudioTabButton(rankX, top + 27, 36, 18, Component.literal("ALL"), this.rankDraft::isEmpty, this.rankDraft::clear)
      );
      rankX += 40;

      for (String rank : DUNGEON_RANKS) {
         String value = rank;
         this.addRenderableWidget(
            new DungeonBuilderStudioScreen.StudioTabButton(
               rankX, top + 27, 27, 18, Component.literal(rank), () -> this.rankDraft.contains(value), () -> this.toggleRank(value)
            )
         );
         rankX += 30;
      }

      this.shellBlockBox = this.addDialogField(
         dialog.x() + 92, top + 54, dialog.w() - 170, this.shellBlockDraft, 128, DungeonBuilderStudioScreen::validResourceDraft, "minecraft:bedrock"
      );
      this.addButton(dialog.right() - 72, top + 54, 62, 18, "Bedrock", () -> this.shellBlockBox.setValue("minecraft:bedrock"));
      this.shellThicknessBox = this.addDialogField(
         dialog.x() + 92, top + 81, 48, this.shellThicknessDraft, 1, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "1"
      );
      this.maxDepthBox = this.addDialogField(dialog.x() + 92, top + 108, 48, this.maxDepthDraft, 2, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "8");
      int buttonY = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, buttonY, 66, 18, "Apply", this::confirmLayoutSetup);
      this.addButton(dialog.right() - 78, buttonY, 66, 18, "Cancel", this::closeDialog);
   }

   private void buildPresetSetupDialog(DungeonBuilderStudioScreen.Rect dialog) {
      int top = dialog.y() + 30;
      int rankX = dialog.x() + 72;
      this.addRenderableWidget(
         new DungeonBuilderStudioScreen.StudioTabButton(rankX, top, 36, 18, Component.literal("ALL"), this.rankDraft::isEmpty, this.rankDraft::clear)
      );
      rankX += 40;

      for (String rank : DUNGEON_RANKS) {
         String value = rank;
         this.addRenderableWidget(
            new DungeonBuilderStudioScreen.StudioTabButton(
               rankX, top, 27, 18, Component.literal(rank), () -> this.rankDraft.contains(value), () -> this.toggleRank(value)
            )
         );
         rankX += 30;
      }

      this.shellBlockBox = this.addDialogField(
         dialog.x() + 92, top + 27, dialog.w() - 170, this.shellBlockDraft, 128, DungeonBuilderStudioScreen::validResourceDraft, "minecraft:bedrock"
      );
      this.addButton(dialog.right() - 72, top + 27, 62, 18, "Bedrock", () -> this.shellBlockBox.setValue("minecraft:bedrock"));
      this.shellThicknessBox = this.addDialogField(
         dialog.x() + 92, top + 54, 48, this.shellThicknessDraft, 1, DungeonBuilderStudioScreen::validUnsignedIntegerDraft, "1"
      );
      int buttonY = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, buttonY, 66, 18, "Apply", this::confirmPresetSetup);
      this.addButton(dialog.right() - 78, buttonY, 66, 18, "Cancel", this::closeDialog);
   }

   private void buildDungeonCatalogDialog(DungeonBuilderStudioScreen.Rect dialog) {
      int y = dialog.bottom() - 26;
      int x = dialog.x() + 10;
      x = this.addButton(x, y, 54, 18, "Open", this::openSelectedDungeon) + 4;
      x = this.addButton(x, y, 48, 18, "New", this::openNewDungeonDialog) + 4;
      x = this.addButton(x, y, 56, 18, "Delete", this::openDeleteDungeonDialog) + 4;
      this.addButton(x, y, 48, 18, "Close", this::closeDialog);
   }

   private void buildNewDungeonDialog(DungeonBuilderStudioScreen.Rect dialog) {
      this.newDungeonIdBox = this.addDialogField(
         dialog.x() + 92,
         dialog.y() + 34,
         dialog.w() - 104,
         this.newDungeonIdDraft,
         192,
         DungeonBuilderStudioScreen::validDungeonResourceDraft,
         "namespace:dungeon_name"
      );
      int y = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, y, 66, 18, "Create", this::confirmNewDungeon);
      this.addButton(dialog.right() - 78, y, 66, 18, "Cancel", this::openDungeonCatalog);
   }

   private void buildDeleteDungeonDialog(DungeonBuilderStudioScreen.Rect dialog) {
      int y = dialog.bottom() - 26;
      this.addButton(dialog.right() - 150, y, 66, 18, "Delete", this::confirmDeleteDungeon);
      this.addButton(dialog.right() - 78, y, 66, 18, "Cancel", this::openDungeonCatalog);
   }

   private EditBox addDialogField(int x, int y, int width, String value, int maxLength, Predicate<String> filter, String hint) {
      EditBox field = new EditBox(this.font, x, y, Math.max(38, width), 18, Component.literal(hint));
      field.setMaxLength(maxLength);
      field.setFilter(filter);
      field.setValue(value == null ? "" : value);
      field.setHint(Component.literal(hint));
      this.addRenderableWidget(field);
      return field;
   }

   private void captureDialogValues() {
      if (this.roomIdBox != null) {
         this.roomIdDraft = this.roomIdBox.getValue();
      }

      if (this.poolIdBox != null) {
         this.poolIdDraft = this.poolIdBox.getValue();
      }

      if (this.selectorIdBox != null) {
         this.selectorIdDraft = this.selectorIdBox.getValue();
      }

      if (this.entryWeightBox != null) {
         this.entryWeightDraft = this.entryWeightBox.getValue();
      }

      if (this.requiredModBox != null) {
         this.requiredModDraft = this.requiredModBox.getValue();
      }

      if (this.eligibleMinBox != null) {
         this.eligibleMinDraft = this.eligibleMinBox.getValue();
      }

      if (this.eligibleMaxBox != null) {
         this.eligibleMaxDraft = this.eligibleMaxBox.getValue();
      }

      if (this.spawnMinBox != null) {
         this.spawnMinDraft = this.spawnMinBox.getValue();
      }

      if (this.spawnMaxBox != null) {
         this.spawnMaxDraft = this.spawnMaxBox.getValue();
      }

      if (this.baseXpBox != null) {
         this.baseXpDraft = this.baseXpBox.getValue();
      }

      if (this.anchorEncounterBox != null) {
         this.anchorEncounterDraft = this.anchorEncounterBox.getValue();
      }

      if (this.anchorMinLevelBox != null) {
         this.anchorMinLevelDraft = this.anchorMinLevelBox.getValue();
      }

      if (this.anchorMaxLevelBox != null) {
         this.anchorMaxLevelDraft = this.anchorMaxLevelBox.getValue();
      }

      if (this.shellBlockBox != null) {
         this.shellBlockDraft = this.shellBlockBox.getValue();
      }

      if (this.shellThicknessBox != null) {
         this.shellThicknessDraft = this.shellThicknessBox.getValue();
      }

      if (this.maxDepthBox != null) {
         this.maxDepthDraft = this.maxDepthBox.getValue();
      }

      if (this.newDungeonIdBox != null) {
         this.newDungeonIdDraft = this.newDungeonIdBox.getValue();
      }
   }

   private void clearDialogWidgetReferences() {
      this.roomIdBox = this.poolIdBox = this.selectorIdBox = this.entryWeightBox = this.requiredModBox = null;
      this.eligibleMinBox = this.eligibleMaxBox = this.spawnMinBox = this.spawnMaxBox = this.baseXpBox = null;
      this.anchorEncounterBox = this.anchorMinLevelBox = this.anchorMaxLevelBox = null;
      this.shellBlockBox = this.shellThicknessBox = this.maxDepthBox = this.newDungeonIdBox = null;
      this.selectorKindButton = this.projectKindButton = this.eligibleRangeButton = this.spawnRangeButton = this.xpModeButton = this.anchorLevelModeButton = null;
   }

   private void buildFooterWidgets(DungeonBuilderStudioScreen.Rect footer, String seedValue) {
      if (this.compact) {
         this.buildCompactFooterWidgets(footer, seedValue);
      } else {
         int x = footer.x() + 2;
         int y = footer.y() + 2;
         int h = Math.max(16, footer.h() - 4);
         boolean fixedLayout = this.activeTab == DungeonBuilderStudioScreen.Tab.LAYOUT
            && this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.FIXED;
         x = this.addButton(
               x,
               y,
               52,
               h,
               fixedLayout ? "Mode" : "Refresh",
               fixedLayout ? this::cycleLayoutMode : () -> this.controller.submit(new DungeonBuilderStudioController.RequestSnapshot())
            )
            + 4;
         switch (this.activeTab) {
            case ROOMS: {
               Optional<DungeonBuilderStudioModel.Project> project = this.selectedProject();
               x = this.addButton(x, y, 58, h, "New Room", this::createRoomProject) + 4;
               String capture = project.<String>map(value -> value.snapshotCaptured() ? "Update Snapshot" : "Capture Room").orElse("Capture Room");
               x = this.addButton(x, y, 88, h, capture, this::captureSelectedProject) + 4;
               if (project.map(value -> value.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET).orElse(false)) {
                  x = this.addButton(x, y, 72, h, "Preset Setup", this::openPresetSetupDialog) + 4;
               } else {
                  x = this.addButton(x, y, 42, h, "Role", this::cycleRoomRole) + 4;
                  x = this.addButton(x, y, 48, h, "Default-", () -> this.adjustRoomWeight(-1)) + 4;
                  x = this.addButton(x, y, 48, h, "Default+", () -> this.adjustRoomWeight(1)) + 4;
                  if (this.selectedSocket().isPresent()) {
                     x = this.addButton(x, y, 58, h, "Required", this::toggleSelectedSocketRequired) + 4;
                  }
               }

               String delete = this.pendingProjectDelete.equals(this.selectedProjectId) ? "Confirm" : "Delete";
               this.addButton(x, y, this.pendingProjectDelete.equals(this.selectedProjectId) ? 54 : 44, h, delete, this::deleteRoomProject);
               break;
            }
            case ANCHORS:
               if (!this.selectedAnchorIsTrigger()) {
                  x = this.addButton(x, y, 58, h, "Next Role", this::cycleSelectedAnchorRole) + 4;
                  x = this.addButton(x, y, 58, h, "Next Pool", this::cycleSelectedAnchorPool) + 4;
                  x = this.addButton(x, y, 44, h, "Level -", () -> this.shiftSelectedAnchorLevel(-1)) + 4;
                  x = this.addButton(x, y, 44, h, "Level +", () -> this.shiftSelectedAnchorLevel(1)) + 4;
                  x = this.addButton(x, y, 56, h, "Delayed", this::toggleSelectedAnchorDelay) + 4;
               }

               this.addButton(x, y, 62, h, "Configure", this::openAnchorSetupDialog);
               break;
            case POOLS: {
               x = this.addButton(x, y, 60, h, "New Pool", this::createPoolDraft) + 4;
               x = this.addButton(x, y, 68, h, "Add Entity", this::beginPoolEntryDraft) + 4;
               x = this.addButton(x, y, 70, h, "Save Draft", this::savePoolDraft) + 4;
               String delete = this.pendingPoolDelete.equals(this.selectedPoolId) ? "Confirm Delete" : "Delete";
               this.addButton(x, y, this.pendingPoolDelete.equals(this.selectedPoolId) ? 82 : 52, h, delete, this::deletePool);
               break;
            }
            case LAYOUT:
               if (this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.FIXED) {
                  x = this.addButton(x, y, 42, h, "Add", this::addSelectedRoomNode) + 4;
                  x = this.addButton(x, y, 24, h, "-X", () -> this.moveSelectedLayoutNode(-1, 0)) + 4;
                  x = this.addButton(x, y, 24, h, "+X", () -> this.moveSelectedLayoutNode(1, 0)) + 4;
                  x = this.addButton(x, y, 24, h, "-Z", () -> this.moveSelectedLayoutNode(0, -1)) + 4;
                  x = this.addButton(x, y, 24, h, "+Z", () -> this.moveSelectedLayoutNode(0, 1)) + 4;
                  x = this.addButton(x, y, 38, h, "Rotate", this::rotateSelectedLayoutNode) + 4;
                  x = this.addButton(x, y, 42, h, "Socket", this::cycleLayoutSocket) + 4;
                  x = this.addButton(x, y, 42, h, this.layoutLinkLabel(), this::connectSelectedLayoutSocket) + 4;
                  x = this.addButton(x, y, 36, h, "Delete", this::deleteSelectedLayoutNode) + 4;
                  x = this.addButton(x, y, 42, h, "Setup", this::openLayoutSetupDialog) + 4;
                  this.addButton(x, y, 42, h, "Apply", this::submitLayout);
               } else {
                  x = this.addButton(x, y, 42, h, "Mode", this::cycleLayoutMode) + 4;
                  x = this.addButton(x, y, 58, h, "Topology", this::cycleTopology) + 4;
                  x = this.addButton(x, y, 54, h, this.layoutIncludeLabel(), this::toggleSelectedProjectIncluded) + 4;
                  x = this.addButton(x, y, 32, h, "Min-", () -> this.adjustRoomRange(-1, 0)) + 4;
                  x = this.addButton(x, y, 32, h, "Min+", () -> this.adjustRoomRange(1, 0)) + 4;
                  x = this.addButton(x, y, 32, h, "Max-", () -> this.adjustRoomRange(0, -1)) + 4;
                  x = this.addButton(x, y, 32, h, "Max+", () -> this.adjustRoomRange(0, 1)) + 4;
                  x = this.addButton(x, y, 44, h, "Setup", this::openLayoutSetupDialog) + 4;
                  this.addButton(x, y, 44, h, "Apply", this::submitLayout);
               }
               break;
            case SIMULATE:
               this.seedBox = new EditBox(this.font, x, y, 126, h, Component.literal("Simulation seed"));
               this.seedBox.setMaxLength(20);
               this.seedBox.setFilter(DungeonBuilderStudioScreen::validSeedText);
               this.seedBox.setValue(seedValue != null && !seedValue.isBlank() ? seedValue : Long.toString(this.simulationSeed));
               this.seedBox.setHint(Component.literal("Seed"));
               this.addRenderableWidget(this.seedBox);
               x += 130;
               x = this.addButton(x, y, 68, h, "New Seed", this::newSimulationSeed) + 4;
               this.addButton(x, y, 76, h, "Run Preview", this::runSimulation);
               break;
            case EXPORT:
               x = this.addButton(x, y, 70, h, "Validate", this::validateDungeon) + 4;
               this.addButton(x, y, 78, h, "Export Pack", this::exportDungeon);
         }
      }
   }

   private void buildCompactFooterWidgets(DungeonBuilderStudioScreen.Rect footer, String seedValue) {
      int x = footer.x() + 2;
      int firstY = footer.y() + 2;
      int secondY = footer.y() + 20;
      int h = 16;
      boolean fixedLayout = this.activeTab == DungeonBuilderStudioScreen.Tab.LAYOUT && this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.FIXED;
      x = this.addButton(
            x,
            firstY,
            52,
            h,
            fixedLayout ? "Mode" : "Refresh",
            fixedLayout ? this::cycleLayoutMode : () -> this.controller.submit(new DungeonBuilderStudioController.RequestSnapshot())
         )
         + 4;
      switch (this.activeTab) {
         case ROOMS: {
            x = this.addButton(x, firstY, 58, h, "New Room", this::createRoomProject) + 4;
            String capture = this.selectedProject().map(value -> value.snapshotCaptured() ? "Update Snapshot" : "Capture Room").orElse("Capture Room");
            this.addButton(x, firstY, 90, h, capture, this::captureSelectedProject);
            x = footer.x() + 2;
            if (this.selectedProject().map(value -> value.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET).orElse(false)) {
               x = this.addButton(x, secondY, 76, h, "Preset Setup", this::openPresetSetupDialog) + 4;
            } else {
               x = this.addButton(x, secondY, 42, h, "Role", this::cycleRoomRole) + 4;
               x = this.addButton(x, secondY, 48, h, "Default-", () -> this.adjustRoomWeight(-1)) + 4;
               x = this.addButton(x, secondY, 48, h, "Default+", () -> this.adjustRoomWeight(1)) + 4;
               if (this.selectedSocket().isPresent()) {
                  x = this.addButton(x, secondY, 58, h, "Required", this::toggleSelectedSocketRequired) + 4;
               }
            }

            String delete = this.pendingProjectDelete.equals(this.selectedProjectId) ? "Confirm Delete" : "Delete";
            this.addButton(x, secondY, this.pendingProjectDelete.equals(this.selectedProjectId) ? 78 : 44, h, delete, this::deleteRoomProject);
            break;
         }
         case ANCHORS:
            if (!this.selectedAnchorIsTrigger()) {
               x = this.addButton(x, firstY, 58, h, "Next Role", this::cycleSelectedAnchorRole) + 4;
               x = this.addButton(x, firstY, 58, h, "Next Pool", this::cycleSelectedAnchorPool) + 4;
            }

            this.addButton(x, firstY, 62, h, "Configure", this::openAnchorSetupDialog);
            if (!this.selectedAnchorIsTrigger()) {
               x = footer.x() + 2;
               x = this.addButton(x, secondY, 44, h, "Level -", () -> this.shiftSelectedAnchorLevel(-1)) + 4;
               x = this.addButton(x, secondY, 44, h, "Level +", () -> this.shiftSelectedAnchorLevel(1)) + 4;
               this.addButton(x, secondY, 56, h, "Delayed", this::toggleSelectedAnchorDelay);
            }
            break;
         case POOLS: {
            x = this.addButton(x, firstY, 60, h, "New Pool", this::createPoolDraft) + 4;
            this.addButton(x, firstY, 68, h, "Add Entity", this::beginPoolEntryDraft);
            x = footer.x() + 2;
            x = this.addButton(x, secondY, 70, h, "Save Draft", this::savePoolDraft) + 4;
            String delete = this.pendingPoolDelete.equals(this.selectedPoolId) ? "Confirm Delete" : "Delete";
            this.addButton(x, secondY, this.pendingPoolDelete.equals(this.selectedPoolId) ? 82 : 52, h, delete, this::deletePool);
            break;
         }
         case LAYOUT:
            if (this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.FIXED) {
               x = this.addButton(x, firstY, 40, h, "Add", this::addSelectedRoomNode) + 4;
               x = this.addButton(x, firstY, 46, h, "Rotate", this::rotateSelectedLayoutNode) + 4;
               x = this.addButton(x, firstY, 46, h, "Socket", this::cycleLayoutSocket) + 4;
               this.addButton(x, firstY, 40, h, this.layoutLinkLabel(), this::connectSelectedLayoutSocket);
               x = footer.x() + 2;
               x = this.addButton(x, secondY, 26, h, "-X", () -> this.moveSelectedLayoutNode(-1, 0)) + 4;
               x = this.addButton(x, secondY, 26, h, "+X", () -> this.moveSelectedLayoutNode(1, 0)) + 4;
               x = this.addButton(x, secondY, 26, h, "-Z", () -> this.moveSelectedLayoutNode(0, -1)) + 4;
               x = this.addButton(x, secondY, 26, h, "+Z", () -> this.moveSelectedLayoutNode(0, 1)) + 4;
               x = this.addButton(x, secondY, 40, h, "Delete", this::deleteSelectedLayoutNode) + 4;
               x = this.addButton(x, secondY, 42, h, "Setup", this::openLayoutSetupDialog) + 4;
               this.addButton(x, secondY, 42, h, "Apply", this::submitLayout);
            } else {
               x = this.addButton(x, firstY, 50, h, "Mode", this::cycleLayoutMode) + 4;
               x = this.addButton(x, firstY, 60, h, "Topology", this::cycleTopology) + 4;
               x = this.addButton(x, firstY, 54, h, this.layoutIncludeLabel(), this::toggleSelectedProjectIncluded) + 4;
               this.addButton(x, firstY, 44, h, "Apply", this::submitLayout);
               x = footer.x() + 2;
               x = this.addButton(x, secondY, 34, h, "Min-", () -> this.adjustRoomRange(-1, 0)) + 4;
               x = this.addButton(x, secondY, 34, h, "Min+", () -> this.adjustRoomRange(1, 0)) + 4;
               x = this.addButton(x, secondY, 34, h, "Max-", () -> this.adjustRoomRange(0, -1)) + 4;
               x = this.addButton(x, secondY, 34, h, "Max+", () -> this.adjustRoomRange(0, 1)) + 4;
               this.addButton(x, secondY, 48, h, "Setup", this::openLayoutSetupDialog);
            }
            break;
         case SIMULATE:
            this.seedBox = new EditBox(this.font, x, firstY, Math.max(84, footer.w() - 136), h, Component.literal("Simulation seed"));
            this.seedBox.setMaxLength(20);
            this.seedBox.setFilter(DungeonBuilderStudioScreen::validSeedText);
            this.seedBox.setValue(seedValue != null && !seedValue.isBlank() ? seedValue : Long.toString(this.simulationSeed));
            this.seedBox.setHint(Component.literal("Seed"));
            this.addRenderableWidget(this.seedBox);
            this.addButton(this.seedBox.getX() + this.seedBox.getWidth() + 4, firstY, 72, h, "Run Preview", this::runSimulation);
            this.addButton(footer.x() + 2, secondY, 68, h, "New Seed", this::newSimulationSeed);
            break;
         case EXPORT:
            x = this.addButton(x, firstY, 70, h, "Validate", this::validateDungeon) + 4;
            this.addButton(x, firstY, 78, h, "Export Pack", this::exportDungeon);
      }
   }

   private int addButton(int x, int y, int width, int height, String label, Runnable action) {
      this.addRenderableWidget(new SystemScreen.SystemButton(x, y, width, height, Component.literal(label), button -> action.run()));
      return x + width;
   }

   private void setTab(DungeonBuilderStudioScreen.Tab tab) {
      if (this.activeTab != tab) {
         this.activeTab = tab;
         this.layoutNodeDragging = false;
         this.layoutDragMap = null;
         this.canvasZoom = 1.0;
         this.canvasPanX = this.canvasPanY = 0.0;
         this.localFeedback = "";
         this.rebuildWidgets();
      }
   }

   private void setCompactPane(DungeonBuilderStudioScreen.CompactPane pane) {
      if (this.compactPane != pane) {
         this.compactPane = pane;
         this.rebuildWidgets();
      }
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.hoverTooltip = null;
      DungeonBuilderStudioScreen.WorkspaceLayout layout = this.workspaceLayout();
      this.renderContext(graphics, layout.context(), mouseX, mouseY);
      this.renderLeftRail(graphics, layout.left(), mouseX, mouseY);
      if (!this.compact || this.compactPane == DungeonBuilderStudioScreen.CompactPane.CANVAS) {
         this.renderMain(graphics, layout.main(), mouseX, mouseY);
      }

      if (!this.compact) {
         this.renderInspector(graphics, layout.inspector(), mouseX, mouseY);
      } else if (this.compactPane == DungeonBuilderStudioScreen.CompactPane.INSPECTOR) {
         this.renderInspector(graphics, layout.main(), mouseX, mouseY);
      }

      this.drawPanel(graphics, layout.footer(), -435416029, -1205701720);
      if (this.activeDialog != DungeonBuilderStudioScreen.Dialog.NONE) {
         this.renderDialogOverlay(graphics, mouseX, mouseY);
      }
   }

   private void renderDialogOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.fill(this.panelX + 1, this.panelY + 17, this.panelX + this.panelW - 1, this.panelY + this.panelH - 1, -654310903);
      DungeonBuilderStudioScreen.Rect dialog = this.dialogRect();
      this.drawPanel(graphics, dialog, -100134625, -12597505);

      String title = switch (this.activeDialog) {
         case NEW_ROOM -> "CREATE ROOM PROJECT";
         case NEW_POOL -> "CREATE MOB POOL";
         case POOL_ENTRY -> this.editingSelectorId.isBlank() ? "ADD POOL ENTRY" : "EDIT POOL ENTRY";
         case ANCHOR_SETUP -> "ANCHOR ENCOUNTER SETUP";
         case PRESET_SETUP -> "PRESET RANK & SHELL";
         case DUNGEON_CATALOG -> "SAVED DUNGEONS";
         case NEW_DUNGEON -> "CREATE DUNGEON DRAFT";
         case DELETE_DUNGEON -> "DELETE DUNGEON?";
         case LAYOUT_SETUP -> "DUNGEON SETUP";
         default -> "";
      };
      graphics.drawString(this.font, title, dialog.x() + 10, dialog.y() + 8, -9971457, false);
      switch (this.activeDialog) {
         case NEW_ROOM:
            graphics.drawString(this.font, "ROOM ID", dialog.x() + 10, dialog.y() + 37, -7358248, false);
            graphics.drawString(this.font, "PROJECT", dialog.x() + 10, dialog.y() + 62, -7358248, false);
            this.drawClipped(
               graphics,
               this.projectKindDraft == DungeonBuilderStudioModel.ProjectKind.MODULE
                  ? "MODULE is one procedural room. PRESET is a complete prebuilt dungeon."
                  : "PRESET captures one complete dungeon; use MODULE for procedural rooms.",
               dialog.x() + 10,
               dialog.y() + 81,
               dialog.w() - 20,
               -7358248,
               mouseX,
               mouseY
            );
            break;
         case NEW_POOL:
            graphics.drawString(this.font, "POOL ID", dialog.x() + 10, dialog.y() + 39, -7358248, false);
            this.drawClipped(
               graphics,
               "Use namespace:name. This ID is referenced by room spawn anchors.",
               dialog.x() + 10,
               dialog.y() + 59,
               dialog.w() - 20,
               -7358248,
               mouseX,
               mouseY
            );
            break;
         case POOL_ENTRY:
            this.renderPoolEntryDialogLabels(graphics, dialog, mouseX, mouseY);
            break;
         case ANCHOR_SETUP:
            this.renderAnchorSetupDialogLabels(graphics, dialog, mouseX, mouseY);
            break;
         case PRESET_SETUP:
            this.renderPresetSetupDialogLabels(graphics, dialog, mouseX, mouseY);
            break;
         case DUNGEON_CATALOG:
            this.renderDungeonCatalogDialog(graphics, dialog, mouseX, mouseY);
            break;
         case NEW_DUNGEON:
            graphics.drawString(this.font, "DUNGEON ID", dialog.x() + 10, dialog.y() + 39, -7358248, false);
            this.drawClipped(
               graphics,
               "Creates and opens a blank saved draft. Existing dungeons are never overwritten.",
               dialog.x() + 10,
               dialog.y() + 59,
               dialog.w() - 20,
               -7358248,
               mouseX,
               mouseY
            );
            break;
         case DELETE_DUNGEON:
            this.renderDeleteDungeonDialog(graphics, dialog, mouseX, mouseY);
            break;
         case LAYOUT_SETUP:
            this.renderLayoutSetupDialogLabels(graphics, dialog, mouseX, mouseY);
      }

      if (!this.dialogError.isBlank()) {
         this.drawClipped(graphics, "[ERROR] " + this.dialogError, dialog.x() + 10, dialog.bottom() - 38, dialog.w() - 20, -41620, mouseX, mouseY);
      }
   }

   private void renderDungeonCatalogDialog(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect dialog, int mouseX, int mouseY) {
      DungeonBuilderStudioScreen.Rect list = this.dungeonCatalogListRect(dialog);
      this.drawPanel(graphics, list, -268038888, -1205701720);
      if (this.model.dungeonDrafts().isEmpty()) {
         this.renderEmpty(graphics, list.inset(6, 6), "No saved dungeons", "Press New to create the first namespaced dungeon draft.");
      } else {
         this.enableScissor(graphics, list);
         int y = list.y() - this.catalogScroll;

         for (DungeonBuilderStudioModel.DraftSummary draft : this.model.dungeonDrafts()) {
            if (y + 27 >= list.y() && y <= list.bottom()) {
               boolean selected = draft.id().equals(this.selectedDungeonDraftId);
               boolean active = draft.id().equals(this.model.dungeonId());
               if (selected) {
                  graphics.fill(list.x() + 1, y, list.right() - 1, y + 26, 1883275168);
               }

               this.drawClipped(
                  graphics,
                  (active ? "[ACTIVE] " : "") + draft.id(),
                  list.x() + 5,
                  y + 3,
                  list.w() - 10,
                  active ? -10493044 : (selected ? -1509633 : -7358248),
                  mouseX,
                  mouseY
               );
               String count = draft.mode() == DungeonBuilderStudioModel.LayoutMode.FIXED
                  ? draft.placementCount() + " placements"
                  : draft.roomCount() + " rooms";
               this.drawClipped(
                  graphics,
                  draft.mode().name() + " | " + draft.topology().name() + " | " + count,
                  list.x() + 5,
                  y + 14,
                  list.w() - 10,
                  -7358248,
                  mouseX,
                  mouseY
               );
            }

            y += 28;
         }

         graphics.disableScissor();
      }
   }

   private void renderDeleteDungeonDialog(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect dialog, int mouseX, int mouseY) {
      String id = this.selectedDungeonDraftId.isBlank() ? "No dungeon selected" : this.selectedDungeonDraftId;
      this.drawClipped(graphics, id, dialog.x() + 10, dialog.y() + 34, dialog.w() - 20, id.equals(this.model.dungeonId()) ? -11930 : -1509633, mouseX, mouseY);
      this.drawWrapped(
         graphics,
         "This permanently removes the saved dungeon draft. Room projects and mob pools are not deleted.",
         dialog.x() + 10,
         dialog.y() + 54,
         dialog.w() - 20,
         -7358248,
         4
      );
   }

   private void renderPoolEntryDialogLabels(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect dialog, int mouseX, int mouseY) {
      int top = dialog.y() + 30;
      graphics.drawString(this.font, "WEIGHT", dialog.x() + 10, top + 32, -7358248, false);
      graphics.drawString(this.font, "OPTIONAL MOD", dialog.x() + 127, top + 32, -7358248, false);
      graphics.drawString(this.font, "MIN", dialog.x() + 224, top + 59, -7358248, false);
      graphics.drawString(this.font, "MAX", dialog.x() + 251, top + 59, -7358248, false);
      graphics.drawString(this.font, "MIN", dialog.x() + 224, top + 86, -7358248, false);
      graphics.drawString(this.font, "MAX", dialog.x() + 251, top + 86, -7358248, false);
      graphics.drawString(this.font, "VALUE", dialog.x() + 190, top + 113, -7358248, false);
      this.drawClipped(graphics, this.selectorResolutionText(), dialog.x() + 10, top + 132, dialog.w() - 20, this.selectorResolutionColor(), mouseX, mouseY);
   }

   private void renderLayoutSetupDialogLabels(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect dialog, int mouseX, int mouseY) {
      int top = dialog.y() + 30;
      graphics.drawString(this.font, "ACTIVE DUNGEON", dialog.x() + 10, top + 5, -7358248, false);
      graphics.fill(dialog.x() + 92, top, dialog.right() - 10, top + 18, -1073345256);
      drawOutline(graphics, dialog.x() + 92, top, dialog.w() - 102, 18, -1205701720);
      this.drawClipped(graphics, this.model.dungeonId(), dialog.x() + 97, top + 5, dialog.w() - 112, -1509633, mouseX, mouseY);
      graphics.drawString(this.font, "RANKS", dialog.x() + 10, top + 32, -7358248, false);
      graphics.drawString(this.font, "SHELL", dialog.x() + 10, top + 59, -7358248, false);
      graphics.drawString(this.font, "THICKNESS", dialog.x() + 10, top + 86, -7358248, false);
      graphics.drawString(this.font, "MAX DEPTH", dialog.x() + 10, top + 113, -7358248, false);
      if (this.dialogError.isBlank()) {
         this.drawClipped(
            graphics,
            "ALL means every rank. Max depth limits graph distance from the start room.",
            dialog.x() + 10,
            top + 135,
            dialog.w() - 20,
            -7358248,
            mouseX,
            mouseY
         );
      }
   }

   private void renderAnchorSetupDialogLabels(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect dialog, int mouseX, int mouseY) {
      int top = dialog.y() + 30;
      graphics.drawString(this.font, "ENCOUNTER ID", dialog.x() + 10, top + 5, -7358248, false);
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (anchor != null && anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER) {
         this.drawClipped(
            graphics,
            "Trigger volume "
               + (
                  anchor.triggerBounds() == null
                     ? "is not set"
                     : anchor.triggerBounds().width() + " x " + anchor.triggerBounds().height() + " x " + anchor.triggerBounds().depth()
               ),
            dialog.x() + 10,
            top + 34,
            dialog.w() - 20,
            anchor.triggerBounds() == null ? -11930 : -9971457,
            mouseX,
            mouseY
         );
         this.drawClipped(
            graphics,
            "This gate activates its encounter group; spawn role, pool, level, XP, and delay are configured on spawn anchors.",
            dialog.x() + 10,
            top + 52,
            dialog.w() - 20,
            -7358248,
            mouseX,
            mouseY
         );
      } else {
         graphics.drawString(this.font, "MIN", dialog.x() + 158, top + 20, -7358248, false);
         graphics.drawString(this.font, "MAX", dialog.x() + 216, top + 20, -7358248, false);
         String assignment = anchor == null
            ? "No anchor selected"
            : "POOL " + (anchor.poolId().isBlank() ? "NOT SET" : anchor.poolId()) + "  |  ROLE " + anchor.spawnRole();
         this.drawClipped(
            graphics, assignment, dialog.x() + 10, top + 58, dialog.w() - 20, anchor != null && !anchor.poolId().isBlank() ? -7358248 : -11930, mouseX, mouseY
         );
         this.drawClipped(
            graphics,
            "Encounter IDs group spawn points. Inherit uses the pool or dungeon rank level.",
            dialog.x() + 10,
            top + 72,
            dialog.w() - 20,
            -7358248,
            mouseX,
            mouseY
         );
      }
   }

   private void renderPresetSetupDialogLabels(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect dialog, int mouseX, int mouseY) {
      int top = dialog.y() + 30;
      graphics.drawString(this.font, "RANKS", dialog.x() + 10, top + 5, -7358248, false);
      graphics.drawString(this.font, "SHELL", dialog.x() + 10, top + 32, -7358248, false);
      graphics.drawString(this.font, "THICKNESS", dialog.x() + 10, top + 59, -7358248, false);
      if (this.dialogError.isBlank()) {
         this.drawClipped(
            graphics,
            "These settings route this complete preset and wrap its captured bounds.",
            dialog.x() + 10,
            top + 78,
            dialog.w() - 20,
            -7358248,
            mouseX,
            mouseY
         );
      }
   }

   private void renderContext(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      this.drawPanel(graphics, rect, -771092192, -1205701720);
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      String room = project == null ? "ROOM  No room selected" : "ROOM  " + project.id();
      int leftWidth = Math.max(100, rect.w() * 62 / 100);
      int reserved = this.activeTab == DungeonBuilderStudioScreen.Tab.LAYOUT && this.canUseDungeonCatalog() ? 74 : 0;
      this.drawClipped(graphics, room, rect.x() + 6, rect.y() + 4, Math.max(24, leftWidth - 8 - reserved), -1509633, mouseX, mouseY);
      String context = "DUNGEON  " + this.model.dungeonId() + "  |  REV " + this.model.revision();
      this.drawClipped(graphics, context, rect.x() + 6, rect.y() + 14, Math.max(24, leftWidth - 8 - reserved), -7358248, mouseX, mouseY);
      DungeonBuilderStudioModel.Severity severity = this.feedbackSeverity();
      String status = this.feedbackText();
      int statusX = rect.x() + leftWidth;
      this.drawClipped(
         graphics, statusLabel(severity) + "  " + status, statusX, rect.y() + 9, rect.x() + rect.w() - statusX - 5, severityColor(severity), mouseX, mouseY
      );
   }

   private void renderLeftRail(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      this.drawPanel(graphics, rect, -435416029, -1205701720);
      switch (this.activeTab) {
         case POOLS:
            this.renderPoolLibrary(graphics, rect, mouseX, mouseY);
            break;
         case LAYOUT:
         default:
            this.renderProjectLibrary(graphics, rect, mouseX, mouseY);
            break;
         case SIMULATE:
            this.renderSimulationRoomList(graphics, rect, mouseX, mouseY);
            break;
         case EXPORT:
            this.renderIssueList(graphics, rect, mouseX, mouseY);
      }
   }

   private void renderProjectLibrary(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      this.drawSectionTitle(graphics, rect, "ROOM LIBRARY", this.model.projects().size());
      if (this.model.loading()) {
         this.renderEmpty(graphics, rect.inset(7, 24), "Loading rooms...", "The server is preparing your workspace.");
      } else if (this.model.projects().isEmpty()) {
         this.renderEmpty(graphics, rect.inset(7, 24), "No rooms saved", "Build one room in-world, mark its bounds, then capture it here.");
      } else {
         DungeonBuilderStudioScreen.Rect clip = new DungeonBuilderStudioScreen.Rect(rect.x() + 2, rect.y() + 18, rect.w() - 4, rect.h() - 20);
         this.enableScissor(graphics, clip);
         int y = clip.y() - this.leftScroll[this.activeTab.ordinal()];

         for (DungeonBuilderStudioModel.Project project : this.model.projects()) {
            if (y + 27 >= clip.y() && y <= clip.bottom()) {
               boolean selected = project.id().equals(this.selectedProjectId);
               if (selected) {
                  graphics.fill(clip.x(), y, clip.right(), y + 27 - 1, 1883275168);
               }

               boolean layoutRoom = this.activeTab == DungeonBuilderStudioScreen.Tab.LAYOUT;
               boolean included = this.model.layout().enabledProjectIds().contains(project.id());
               long nodeCount = this.model.layout().nodes().stream().filter(node -> node.projectId().equals(project.id())).count();
               boolean weightControls = layoutRoom
                  && this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.PROCEDURAL
                  && included
                  && project.kind() == DungeonBuilderStudioModel.ProjectKind.MODULE;
               String snapshot = layoutRoom
                  ? (
                     this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.FIXED
                        ? nodeCount + " NODE" + (nodeCount == 1L ? "" : "S")
                        : (included ? "DW " + this.dungeonRoomWeight(project) : "EXCLUDED")
                  )
                  : (project.snapshotOutdated() ? "TODO UPDATE" : (project.snapshotCaptured() ? "CAPTURED" : "TODO CAPTURE"));
               int stateColor = layoutRoom
                  ? (!included && nodeCount <= 0L ? -10060918 : -10493044)
                  : (!project.snapshotOutdated() && project.snapshotCaptured() ? -10493044 : -11930);
               this.drawClipped(graphics, project.name(), clip.x() + 5, y + 4, clip.w() - 10, selected ? -1509633 : -7358248, mouseX, mouseY);
               int detailWidth = weightControls ? clip.w() - 49 : clip.w() - 10;
               this.drawClipped(
                  graphics,
                  project.role().name() + "  |  " + snapshot,
                  clip.x() + 5,
                  y + 15,
                  detailWidth,
                  project.errors() > 0 ? -41620 : stateColor,
                  mouseX,
                  mouseY
               );
               if (weightControls) {
                  graphics.drawString(this.font, "[-] [+]", clip.right() - 39, y + 15, -9971457, false);
               }
            }

            y += 27;
         }

         graphics.disableScissor();
      }
   }

   private void renderPoolLibrary(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      this.drawSectionTitle(graphics, rect, "MOB POOLS", this.model.pools().size());
      if (this.model.pools().isEmpty()) {
         this.renderEmpty(graphics, rect.inset(7, 24), "No mob pools", "Press New Pool, then add entities or entity tags.");
      } else {
         DungeonBuilderStudioScreen.Rect clip = new DungeonBuilderStudioScreen.Rect(rect.x() + 2, rect.y() + 18, rect.w() - 4, rect.h() - 20);
         this.enableScissor(graphics, clip);
         int y = clip.y() - this.leftScroll[this.activeTab.ordinal()];

         for (DungeonBuilderStudioModel.MobPool pool : this.model.pools()) {
            if (y + 27 >= clip.y() && y <= clip.bottom()) {
               boolean selected = pool.id().equals(this.selectedPoolId);
               if (selected) {
                  graphics.fill(clip.x(), y, clip.right(), y + 27 - 1, 1883275168);
               }

               this.drawClipped(graphics, pool.id(), clip.x() + 5, y + 4, clip.w() - 10, selected ? -1509633 : -7358248, mouseX, mouseY);
               String state = pool.draft() ? "TODO DRAFT" : "SAVED";
               graphics.drawString(
                  this.font, state + "  |  " + pool.entries().size() + " entries", clip.x() + 5, y + 15, pool.draft() ? -11930 : -10493044, false
               );
            }

            y += 27;
         }

         graphics.disableScissor();
      }
   }

   private void renderSimulationRoomList(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Simulation simulation = this.model.simulation();
      this.drawSectionTitle(graphics, rect, "GENERATED ROOMS", simulation.rooms().size());
      if (simulation.rooms().isEmpty()) {
         this.renderEmpty(graphics, rect.inset(7, 24), "No preview yet", "Set a seed and press Run Preview.");
      } else {
         DungeonBuilderStudioScreen.Rect clip = new DungeonBuilderStudioScreen.Rect(rect.x() + 2, rect.y() + 18, rect.w() - 4, rect.h() - 20);
         this.enableScissor(graphics, clip);
         int y = clip.y() - this.leftScroll[this.activeTab.ordinal()];

         for (DungeonBuilderStudioModel.SimRoom room : simulation.rooms()) {
            if (y + 23 >= clip.y() && y <= clip.bottom()) {
               boolean selected = room.id().equals(this.selectedSimRoomId);
               if (selected) {
                  graphics.fill(clip.x(), y, clip.right(), y + 22, 1883275168);
               }

               this.drawClipped(graphics, room.projectId(), clip.x() + 5, y + 3, clip.w() - 10, selected ? -1509633 : -7358248, mouseX, mouseY);
               graphics.drawString(this.font, room.role().name() + "  R" + room.rotation(), clip.x() + 5, y + 13, roleColor(room.role()), false);
            }

            y += 23;
         }

         graphics.disableScissor();
      }
   }

   private void renderIssueList(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      List<DungeonBuilderStudioModel.ValidationIssue> issues = this.model.validation().issues();
      this.drawSectionTitle(graphics, rect, "VALIDATION", issues.size());
      if (!this.model.validation().hasRun()) {
         this.renderEmpty(graphics, rect.inset(7, 24), "Not validated", "Press Validate before exporting.");
      } else if (issues.isEmpty()) {
         this.renderEmpty(graphics, rect.inset(7, 24), "PASS", "No blocking issues were reported.");
      } else {
         DungeonBuilderStudioScreen.Rect clip = new DungeonBuilderStudioScreen.Rect(rect.x() + 2, rect.y() + 18, rect.w() - 4, rect.h() - 20);
         this.enableScissor(graphics, clip);
         int y = clip.y() - this.leftScroll[this.activeTab.ordinal()];

         for (DungeonBuilderStudioModel.ValidationIssue issue : issues) {
            if (y + 34 >= clip.y() && y <= clip.bottom()) {
               graphics.drawString(this.font, statusLabel(issue.severity()), clip.x() + 5, y + 3, severityColor(issue.severity()), false);
               this.drawClipped(graphics, issue.projectId(), clip.x() + 5, y + 13, clip.w() - 10, -7358248, mouseX, mouseY);
               this.drawClipped(graphics, issue.message(), clip.x() + 5, y + 23, clip.w() - 10, -1509633, mouseX, mouseY);
            }

            y += 34;
         }

         graphics.disableScissor();
      }
   }

   private void renderMain(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      this.drawPanel(graphics, rect, -268038888, -1205701720);
      switch (this.activeTab) {
         case ROOMS:
         case ANCHORS:
            this.renderRoomCanvas(graphics, rect, mouseX, mouseY);
            break;
         case POOLS:
            this.renderPoolEntries(graphics, rect, mouseX, mouseY);
            break;
         case LAYOUT:
            this.renderLayoutCanvas(graphics, rect, mouseX, mouseY);
            break;
         case SIMULATE:
            this.renderSimulationCanvas(graphics, rect, mouseX, mouseY);
            break;
         case EXPORT:
            this.renderExportOverview(graphics, rect, mouseX, mouseY);
      }
   }

   private void renderRoomCanvas(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project == null) {
         this.renderEmpty(graphics, rect.inset(8, 8), "Select a room", "Choose a room from the library to inspect its saved top view.");
      } else if (project.bounds() == null) {
         this.renderEmpty(graphics, rect.inset(8, 8), "Bounds missing", "Use the Surveyor Wand to mark structure bounds, then refresh.");
      } else {
         this.drawBlueprintGrid(graphics, rect);
         DungeonBuilderStudioScreen.Rect viewport = rect.inset(7, 18);
         this.enableScissor(graphics, viewport);
         DungeonBuilderStudioScreen.RoomMap map = DungeonBuilderStudioScreen.RoomMap.forProject(
            project, viewport, this.canvasZoom, this.canvasPanX, this.canvasPanY
         );
         if (!project.footprint().isEmpty()) {
            for (DungeonBuilderStudioModel.FootprintCell cell : project.footprint()) {
               int x0 = map.localX(cell.x());
               int z0 = map.localZ(cell.z());
               int x1 = Math.max(x0 + 1, map.localX(cell.x() + 1));
               int z1 = Math.max(z0 + 1, map.localZ(cell.z() + 1));
               int color = cell.argb() & 16777215 | -1342177280;
               graphics.fill(x0, z0, x1, z1, color);
            }
         } else {
            graphics.fill(map.minScreenX(), map.minScreenZ(), map.maxScreenX(), map.maxScreenZ(), 2048406856);
         }

         drawOutline(
            graphics,
            map.minScreenX(),
            map.minScreenZ(),
            Math.max(1, map.maxScreenX() - map.minScreenX()),
            Math.max(1, map.maxScreenZ() - map.minScreenZ()),
            -12597505
         );

         for (DungeonBuilderStudioModel.Socket socket : project.sockets()) {
            this.drawSocket(graphics, map, socket, socket.id().equals(this.selectedSocketId));
         }

         for (DungeonBuilderStudioModel.Anchor anchor : project.anchors()) {
            this.drawAnchor(graphics, map, anchor, anchor.id().equals(this.selectedAnchorId));
         }

         graphics.disableScissor();
         String heading = "TOP VIEW  |  " + Math.round(this.canvasZoom * 100.0) + "%";
         graphics.drawString(this.font, heading, rect.x() + 6, rect.y() + 5, -9971457, false);
         String help = this.activeTab == DungeonBuilderStudioScreen.Tab.ANCHORS
            ? "Click point: inspect  |  Wheel: zoom  |  Middle drag: pan  |  F: fit"
            : "Click socket: inspect  |  Wheel: zoom  |  Middle drag: pan  |  F: fit";
         this.drawClipped(graphics, help, rect.x() + 6, rect.bottom() - 11, rect.w() - 12, -7358248, mouseX, mouseY);
      }
   }

   private void renderPoolEntries(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.MobPool pool = this.selectedPool().orElse(null);
      if (pool == null) {
         this.renderEmpty(graphics, rect.inset(8, 8), "Select a mob pool", "Choose a pool on the left or create a new draft.");
      } else {
         graphics.drawString(
            this.font, pool.draft() ? "TODO  UNSAVED DRAFT" : "SAVED POOL", rect.x() + 6, rect.y() + 5, pool.draft() ? -11930 : -10493044, false
         );
         if (pool.entries().isEmpty()) {
            this.renderEmpty(graphics, rect.inset(8, 22), "Pool is empty", "Press Add Entity to choose a loaded entity or entity tag.");
         } else {
            DungeonBuilderStudioScreen.Rect clip = new DungeonBuilderStudioScreen.Rect(rect.x() + 3, rect.y() + 18, rect.w() - 6, rect.h() - 21);
            this.enableScissor(graphics, clip);
            int y = clip.y() - this.centerScroll[this.activeTab.ordinal()];
            int total = Math.max(1, pool.totalWeight());

            for (DungeonBuilderStudioModel.PoolEntry entry : pool.entries()) {
               if (y + 36 >= clip.y() && y <= clip.bottom()) {
                  graphics.fill(clip.x(), y, clip.right(), y + 35, -1609821149);
                  drawOutline(graphics, clip.x(), y, clip.w(), 35, 1747088296);
                  this.drawClipped(graphics, entry.selectorLabel(), clip.x() + 5, y + 4, clip.w() - 52, -1509633, mouseX, mouseY);
                  int percent = Math.max(1, Math.round(entry.weight() * 100.0F / total));
                  String xp = entry.baseXp().present() ? Integer.toString(entry.baseXp().value()) : "AUTO";
                  String spawn = entry.spawnLevel().present() ? entry.spawnLevel().min() + "-" + entry.spawnLevel().max() : "DUNGEON";
                  graphics.drawString(this.font, "WEIGHT " + entry.weight() + " (" + percent + "%)  |  XP " + xp, clip.x() + 5, y + 15, -7358248, false);
                  this.drawClipped(
                     graphics,
                     "SPAWN LEVEL " + spawn + (entry.requiredMod().isBlank() ? "" : "  |  MOD " + entry.requiredMod()),
                     clip.x() + 5,
                     y + 25,
                     clip.w() - 55,
                     -7358248,
                     mouseX,
                     mouseY
                  );
                  graphics.drawString(this.font, "EDIT  [-] [+] [X]", clip.right() - 92, y + 15, -9971457, false);
               }

               y += 38;
            }

            graphics.disableScissor();
         }
      }
   }

   private void renderLayoutCanvas(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.LayoutDraft draft = this.model.layout();
      this.drawBlueprintGrid(graphics, rect);
      String title = draft.mode().name() + "  |  " + draft.topology().name() + "  |  " + draft.minRooms() + "-" + draft.maxRooms() + " ROOMS";
      this.drawClipped(graphics, title, rect.x() + 6, rect.y() + 5, rect.w() - 12, -9971457, mouseX, mouseY);
      if (draft.nodes().isEmpty()) {
         String line1 = draft.mode() == DungeonBuilderStudioModel.LayoutMode.PROCEDURAL ? "Procedural rules are ready" : "No fixed rooms placed";
         String line2 = draft.mode() == DungeonBuilderStudioModel.LayoutMode.PROCEDURAL
            ? "Include room assets on the left; weights and sockets decide turns without overlap."
            : "Select a room in the library and press Add to place its first exact node.";
         this.renderEmpty(graphics, rect.inset(8, 22), line1, line2);
      } else {
         this.renderNodeGraph(graphics, rect.inset(7, 18), draft.nodes(), draft.connections(), mouseX, mouseY);
         if (draft.mode() == DungeonBuilderStudioModel.LayoutMode.FIXED) {
            this.drawClipped(
               graphics,
               "Drag free node | click socket point + Link | arrows move 1 block",
               rect.x() + 6,
               rect.bottom() - 11,
               rect.w() - 12,
               -7358248,
               mouseX,
               mouseY
            );
         }
      }
   }

   private void renderSimulationCanvas(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Simulation simulation = this.model.simulation();
      this.drawBlueprintGrid(graphics, rect);

      String title = switch (simulation.status()) {
         case IDLE -> "TODO  PREVIEW NOT RUN";
         case RUNNING -> "RUNNING  PLANNER WORKING";
         case SUCCESS -> "PASS  SEED " + simulation.seed();
         case FAILED -> "ERROR  SEED " + simulation.seed();
      };

      DungeonBuilderStudioModel.Severity severity = switch (simulation.status()) {
         case RUNNING -> DungeonBuilderStudioModel.Severity.INFO;
         case SUCCESS -> DungeonBuilderStudioModel.Severity.PASS;
         case FAILED -> DungeonBuilderStudioModel.Severity.ERROR;
         default -> DungeonBuilderStudioModel.Severity.TODO;
      };
      this.drawClipped(graphics, title, rect.x() + 6, rect.y() + 5, rect.w() - 12, severityColor(severity), mouseX, mouseY);
      if (simulation.rooms().isEmpty()) {
         this.renderEmpty(graphics, rect.inset(8, 22), "No generated layout", "Set a seed below and press Run Preview.");
      } else {
         this.renderSimulationGraph(graphics, rect.inset(7, 18), simulation, mouseX, mouseY);
      }
   }

   private void renderExportOverview(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Severity severity = this.model.validation().severity();
      String heading = statusLabel(severity) + "  " + this.model.validation().errors() + " ERRORS  |  " + this.model.validation().warnings() + " WARNINGS";
      graphics.drawCenteredString(this.font, heading, rect.x() + rect.w() / 2, rect.y() + 18, severityColor(severity));
      int y = rect.y() + 42;
      y = this.exportLine(
         graphics,
         rect,
         y,
         this.model.projects().isEmpty() ? DungeonBuilderStudioModel.Severity.ERROR : DungeonBuilderStudioModel.Severity.PASS,
         "ROOM ASSETS",
         this.model.projects().isEmpty() ? "Capture at least one room." : this.model.projects().size() + " rooms available."
      );
      y = this.exportLine(
         graphics,
         rect,
         y,
         this.model.pools().stream().anyMatch(DungeonBuilderStudioModel.MobPool::draft)
            ? DungeonBuilderStudioModel.Severity.WARNING
            : DungeonBuilderStudioModel.Severity.PASS,
         "MOB POOLS",
         this.model.pools().stream().anyMatch(DungeonBuilderStudioModel.MobPool::draft)
            ? "Save remaining drafts."
            : this.model.pools().size() + " pools saved."
      );
      y = this.exportLine(
         graphics,
         rect,
         y,
         this.model.layout().enabledProjectIds().isEmpty() ? DungeonBuilderStudioModel.Severity.TODO : DungeonBuilderStudioModel.Severity.PASS,
         "LAYOUT",
         this.model.layout().enabledProjectIds().isEmpty() ? "Choose rooms for generation." : "Planner rules configured."
      );
      y = this.exportLine(
         graphics,
         rect,
         y,
         this.model.simulation().status() == DungeonBuilderStudioModel.SimulationStatus.SUCCESS
            ? DungeonBuilderStudioModel.Severity.PASS
            : DungeonBuilderStudioModel.Severity.TODO,
         "SIMULATION",
         this.model.simulation().status() == DungeonBuilderStudioModel.SimulationStatus.SUCCESS
            ? "Latest seed generated successfully."
            : "Run at least one preview seed."
      );
      this.exportLine(
         graphics,
         rect,
         y,
         severity,
         "DATAPACK",
         severity == DungeonBuilderStudioModel.Severity.PASS ? "Ready for an explicit export." : "Validate and resolve blocking errors first."
      );
   }

   private int exportLine(
      GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int y, DungeonBuilderStudioModel.Severity severity, String label, String detail
   ) {
      graphics.fill(rect.x() + 12, y, rect.right() - 12, y + 28, -1609821149);
      graphics.drawString(this.font, statusLabel(severity) + "  " + label, rect.x() + 18, y + 5, severityColor(severity), false);
      this.drawClipped(graphics, detail, rect.x() + 18, y + 16, rect.w() - 36, -7358248, -1, -1);
      return y + 33;
   }

   private void renderInspector(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      this.drawPanel(graphics, rect, -435416029, -1205701720);
      DungeonBuilderStudioScreen.Rect clip = new DungeonBuilderStudioScreen.Rect(rect.x() + 2, rect.y() + 18, rect.w() - 4, rect.h() - 20);
      graphics.drawString(this.font, this.inspectorTitle(), rect.x() + 6, rect.y() + 5, -9971457, false);
      this.enableScissor(graphics, clip);
      int y = clip.y() + 2 - this.inspectorScroll[this.activeTab.ordinal()];
      switch (this.activeTab) {
         case ROOMS:
            this.renderRoomInspector(graphics, clip, y, mouseX, mouseY);
            break;
         case ANCHORS:
            this.renderAnchorInspector(graphics, clip, y, mouseX, mouseY);
            break;
         case POOLS:
            this.renderPoolInspector(graphics, clip, y, mouseX, mouseY);
            break;
         case LAYOUT:
            this.renderLayoutInspector(graphics, clip, y, mouseX, mouseY);
            break;
         case SIMULATE:
            this.renderSimulationInspector(graphics, clip, y, mouseX, mouseY);
            break;
         case EXPORT:
            this.renderExportInspector(graphics, clip, y, mouseX, mouseY);
      }

      graphics.disableScissor();
   }

   private void renderRoomInspector(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect clip, int y, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project == null) {
         this.renderEmpty(graphics, clip, "No room selected", "Select a room from the library.");
      } else {
         y = this.inspectorValue(graphics, clip, y, "ROOM ID", project.id(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "ROLE", project.role().name(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "SNAPSHOT", project.snapshotLabel() + "  REV " + project.snapshotRevision(), mouseX, mouseY);
         String size = project.bounds() == null ? "NOT SET" : project.bounds().width() + " x " + project.bounds().height() + " x " + project.bounds().depth();
         y = this.inspectorValue(graphics, clip, y, "BOUNDS", size, mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "DEFAULT WEIGHT", Integer.toString(project.weight()), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "RANKS", rankLabel(project.ranks()), mouseX, mouseY);
         if (project.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET) {
            y = this.inspectorValue(graphics, clip, y, "SHELL", project.shellBlock(), mouseX, mouseY);
            y = this.inspectorValue(graphics, clip, y, "THICKNESS", Integer.toString(project.shellThickness()), mouseX, mouseY);
         }

         y = this.inspectorValue(graphics, clip, y, "SOCKETS", project.sockets().size() + " total", mouseX, mouseY);
         DungeonBuilderStudioModel.Socket socket = this.selectedSocket().orElse(null);
         if (socket != null) {
            y += 4;
            graphics.drawString(this.font, "SELECTED SOCKET", clip.x() + 4, y, -9971457, false);
            y += 12;
            y = this.inspectorValue(graphics, clip, y, "ID", socket.id(), mouseX, mouseY);
            y = this.inspectorValue(graphics, clip, y, "FACING", socket.facing().name(), mouseX, mouseY);
            y = this.inspectorValue(graphics, clip, y, "TYPE", socket.type().name(), mouseX, mouseY);
            this.inspectorValue(graphics, clip, y, "POLICY", socket.required() ? "MUST CONNECT" : "OPTIONAL", mouseX, mouseY);
         }
      }
   }

   private void renderAnchorInspector(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect clip, int y, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (anchor == null) {
         this.renderEmpty(graphics, clip, "Select a point", "Click a labeled marker in the top view. Generic points show TODO until assigned.");
      } else {
         y = this.inspectorValue(graphics, clip, y, "ANCHOR ID", anchor.id(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "KIND", anchor.kind().name(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "POSITION", pointText(anchor.position()), mouseX, mouseY);
         if (anchor.triggerBounds() != null) {
            y = this.inspectorValue(
               graphics,
               clip,
               y,
               "TRIGGER VOLUME",
               anchor.triggerBounds().width() + " x " + anchor.triggerBounds().height() + " x " + anchor.triggerBounds().depth(),
               mouseX,
               mouseY
            );
         }

         y = this.inspectorValue(graphics, clip, y, "ENCOUNTER", anchor.encounterId(), mouseX, mouseY);
         if (anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER) {
            this.drawWrapped(
               graphics,
               "Trigger anchors only activate this encounter group. Configure its mobs and levels on spawn anchors.",
               clip.x() + 4,
               y + 3,
               clip.w() - 8,
               -7358248,
               5
            );
         } else {
            y = this.inspectorValue(graphics, clip, y, "SPAWN ROLE", anchor.spawnRole().name(), mouseX, mouseY);
            y = this.inspectorValue(graphics, clip, y, "MOB POOL", anchor.poolId().isBlank() ? "TODO  NOT ASSIGNED" : anchor.poolId(), mouseX, mouseY);
            y = this.inspectorValue(
               graphics,
               clip,
               y,
               "LEVEL",
               anchor.levelOverride() ? anchor.minLevel() + "-" + anchor.maxLevel() + " (OVERRIDE)" : "POOL / DUNGEON DEFAULT",
               mouseX,
               mouseY
            );
            this.inspectorValue(graphics, clip, y, "ACTIVATION", anchor.delayed() ? "DELAYED BY TRIGGER" : "ON GENERATION", mouseX, mouseY);
         }
      }
   }

   private void renderPoolInspector(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect clip, int y, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.MobPool pool = this.selectedPool().orElse(null);
      if (pool == null) {
         this.renderEmpty(graphics, clip, "No pool selected", "Create a reusable pool or select one from the library.");
      } else {
         y = this.inspectorValue(graphics, clip, y, "POOL ID", pool.id(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "STATUS", pool.draft() ? "TODO  UNSAVED" : "SAVED", mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "ENTRIES", Integer.toString(pool.entries().size()), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "TOTAL WEIGHT", Integer.toString(pool.totalWeight()), mouseX, mouseY);
         y += 5;
         graphics.drawString(this.font, "WEIGHT MEANING", clip.x() + 4, y, -9971457, false);
         y += 12;
         this.drawWrapped(
            graphics,
            "Chance is entry weight divided by total eligible weight. Entity tags resolve when the datapack reloads.",
            clip.x() + 4,
            y,
            clip.w() - 8,
            -7358248,
            7
         );
      }
   }

   private void renderLayoutInspector(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect clip, int y, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.LayoutDraft draft = this.model.layout();
      y = this.inspectorValue(graphics, clip, y, "STATUS", this.layoutDirty ? "UNSAVED - PRESS APPLY" : "SERVER SNAPSHOT", mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "DUNGEON ID", this.model.dungeonId(), mouseX, mouseY);
      DungeonBuilderStudioModel.Project selectedRoom = this.selectedProject().orElse(null);
      if (selectedRoom != null && selectedRoom.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET) {
         y = this.inspectorValue(graphics, clip, y, "SAVED DUNGEONS", "PRESET EXPORTS DIRECTLY", mouseX, mouseY);
      }

      y = this.inspectorValue(graphics, clip, y, "MODE", draft.mode().name(), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "TOPOLOGY", draft.topology().name(), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "ROOM RANGE", draft.minRooms() + "-" + draft.maxRooms(), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "MAX DEPTH", Integer.toString(draft.maxDepth()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "RANKS", rankLabel(draft.ranks()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "SHELL", draft.shellBlock(), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "THICKNESS", Integer.toString(draft.shellThickness()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "ROOM ASSETS", draft.enabledProjectIds().size() + " enabled", mouseX, mouseY);
      if (selectedRoom != null) {
         String dungeonWeight = selectedRoom.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET
            ? "PRESET EXPORTS DIRECTLY"
            : (
               draft.mode() == DungeonBuilderStudioModel.LayoutMode.FIXED
                  ? "NOT USED IN FIXED MODE"
                  : (!draft.enabledProjectIds().contains(selectedRoom.id()) ? "EXCLUDED" : Integer.toString(this.dungeonRoomWeight(selectedRoom)))
            );
         y = this.inspectorValue(graphics, clip, y, "DUNGEON WEIGHT", dungeonWeight, mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "PROJECT DEFAULT", Integer.toString(selectedRoom.weight()), mouseX, mouseY);
      }

      y = this.inspectorValue(graphics, clip, y, "FIXED NODES", Integer.toString(draft.nodes().size()), mouseX, mouseY);
      DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
      if (node != null) {
         y += 4;
         graphics.drawString(this.font, "SELECTED FIXED NODE", clip.x() + 4, y, -9971457, false);
         y += 12;
         y = this.inspectorValue(graphics, clip, y, "NODE", node.id(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "ROOM", node.projectId(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "POSITION", node.x() + ", " + node.y() + ", " + node.z(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "ROTATION", node.rotation() + " DEGREES", mouseX, mouseY);
         DungeonBuilderStudioModel.Socket socket = this.layoutSocket(node, this.selectedLayoutSocketId).orElse(null);
         y = this.inspectorValue(graphics, clip, y, "SOCKET", socket == null ? "PRESS SOCKET TO SELECT" : socket.id(), mouseX, mouseY);
         if (socket != null) {
            y = this.inspectorValue(graphics, clip, y, "FACING", rotatedFacing(socket.facing(), node.rotation()).name(), mouseX, mouseY);
         }

         this.inspectorValue(
            graphics,
            clip,
            y,
            "LINK",
            this.pendingConnectionNodeId.isBlank() ? "NO FIRST ENDPOINT" : this.pendingConnectionNodeId + " / " + this.pendingConnectionSocketId,
            mouseX,
            mouseY
         );
      }
   }

   private void renderSimulationInspector(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect clip, int y, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Simulation simulation = this.model.simulation();
      y = this.inspectorValue(graphics, clip, y, "STATUS", simulation.status().name(), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "SEED", Long.toString(simulation.seed()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "ROOMS", Integer.toString(simulation.rooms().size()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "CONNECTIONS", Integer.toString(simulation.connections().size()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "ATTEMPTS", Integer.toString(simulation.attempts()), mouseX, mouseY);
      DungeonBuilderStudioModel.SimRoom room = this.selectedSimulationRoom().orElse(null);
      if (room != null) {
         y += 5;
         graphics.drawString(this.font, "SELECTED GENERATED ROOM", clip.x() + 4, y, -9971457, false);
         y += 12;
         y = this.inspectorValue(graphics, clip, y, "PROJECT", room.projectId(), mouseX, mouseY);
         y = this.inspectorValue(graphics, clip, y, "ROLE", room.role().name(), mouseX, mouseY);
         this.inspectorValue(graphics, clip, y, "ROTATION", room.rotation() + " DEGREES", mouseX, mouseY);
      }
   }

   private void renderExportInspector(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect clip, int y, int mouseX, int mouseY) {
      y = this.inspectorValue(graphics, clip, y, "DUNGEON ID", this.model.dungeonId(), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "REVISION", Long.toString(this.model.revision()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "VALIDATION", statusLabel(this.model.validation().severity()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "ERRORS", Integer.toString(this.model.validation().errors()), mouseX, mouseY);
      y = this.inspectorValue(graphics, clip, y, "WARNINGS", Integer.toString(this.model.validation().warnings()), mouseX, mouseY);
      y += 5;
      graphics.drawString(this.font, "EXPORT IS EXPLICIT", clip.x() + 4, y, -11930, false);
      y += 12;
      this.drawWrapped(
         graphics,
         "Metadata autosaves, but captured blocks only change when you press Capture Room or Update Snapshot.",
         clip.x() + 4,
         y,
         clip.w() - 8,
         -7358248,
         7
      );
   }

   private int inspectorValue(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect clip, int y, String label, String value, int mouseX, int mouseY) {
      graphics.drawString(this.font, label, clip.x() + 4, y, -7358248, false);
      this.drawClipped(graphics, value, clip.x() + 4, y + 10, clip.w() - 8, -1509633, mouseX, mouseY);
      graphics.fill(clip.x() + 4, y + 21, clip.right() - 4, y + 22, 941781928);
      return y + 27;
   }

   @Override
   protected List<Component> getHoverTooltip(int mouseX, int mouseY) {
      return this.hoverTooltip;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (this.activeDialog == DungeonBuilderStudioScreen.Dialog.DUNGEON_CATALOG) {
         if (button == 0) {
            this.handleDungeonCatalogClick((int)Math.round(this.logicalMouseX(mouseX)), (int)Math.round(this.logicalMouseY(mouseY)));
         }

         return true;
      } else {
         if (this.activeDialog != DungeonBuilderStudioScreen.Dialog.NONE) {
            return true;
         }

         int logicalX = (int)Math.round(this.logicalMouseX(mouseX));
         int logicalY = (int)Math.round(this.logicalMouseY(mouseY));
         DungeonBuilderStudioScreen.WorkspaceLayout layout = this.workspaceLayout();
         if (button != 2 || !layout.main().contains(logicalX, logicalY) || this.compact && this.compactPane != DungeonBuilderStudioScreen.CompactPane.CANVAS) {
            if (layout.left().contains(logicalX, logicalY)) {
               this.handleLeftClick(layout.left(), logicalX, logicalY, button);
               return true;
            }

            if ((!this.compact || this.compactPane == DungeonBuilderStudioScreen.CompactPane.CANVAS) && layout.main().contains(logicalX, logicalY)) {
               if (button != 0
                  || this.compact
                  || this.activeTab != DungeonBuilderStudioScreen.Tab.LAYOUT
                  || this.model.layout().mode() != DungeonBuilderStudioModel.LayoutMode.FIXED) {
                  this.handleMainClick(layout.main(), logicalX, logicalY);
                  return true;
               }

               if (this.selectLayoutSocketAt(layout.main(), logicalX, logicalY)) {
                  return true;
               }

               DungeonBuilderStudioModel.LayoutNode selected = this.selectLayoutNodeAt(layout.main(), logicalX, logicalY);
               if (selected != null && !selected.locked() && !this.isLayoutNodeConnected(selected.id())) {
                  this.layoutNodeDragging = true;
                  this.layoutDragBlocked = false;
                  this.layoutDragMap = this.layoutGraphMap(layout.main().inset(7, 18), this.model.layout().nodes());
                  this.layoutDragRemainderX = 0.0;
                  this.layoutDragRemainderZ = 0.0;
               }

               return true;
            } else {
               return false;
            }
         } else {
            this.canvasPanning = true;
            return true;
         }
      }
   }

   private void handleLeftClick(DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY, int button) {
      int localY = mouseY - (rect.y() + 18) + this.leftScroll[this.activeTab.ordinal()];
      if (localY >= 0) {
         switch (this.activeTab) {
            case POOLS:
               int indexxx = localY / 27;
               if (indexxx >= 0 && indexxx < this.model.pools().size()) {
                  this.selectedPoolId = this.model.pools().get(indexxx).id();
                  this.pendingPoolDelete = "";
                  this.centerScroll[this.activeTab.ordinal()] = 0;
                  this.rebuildWidgets();
               }
               break;
            case LAYOUT:
            default:
               int indexxxx = localY / 27;
               if (indexxxx >= 0 && indexxxx < this.model.projects().size()) {
                  DungeonBuilderStudioModel.Project project = this.model.projects().get(indexxxx);
                  this.selectedProjectId = project.id();
                  this.selectedAnchorId = "";
                  this.selectedSocketId = "";
                  if (this.activeTab == DungeonBuilderStudioScreen.Tab.ROOMS || this.activeTab == DungeonBuilderStudioScreen.Tab.ANCHORS) {
                     this.canvasZoom = 1.0;
                     this.canvasPanX = this.canvasPanY = 0.0;
                  }

                  this.controller.submit(new DungeonBuilderStudioController.SelectProject(project.id()));
                  boolean weightControl = button == 0
                     && this.activeTab == DungeonBuilderStudioScreen.Tab.LAYOUT
                     && this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.PROCEDURAL
                     && project.kind() == DungeonBuilderStudioModel.ProjectKind.MODULE
                     && this.model.layout().enabledProjectIds().contains(project.id())
                     && mouseX >= rect.right() - 42;
                  if (weightControl) {
                     this.adjustDungeonRoomWeight(project, mouseX < rect.right() - 22 ? -1 : 1);
                     this.rebuildWidgets();
                     return;
                  }

                  this.rebuildWidgets();
               }
               break;
            case SIMULATE:
               int indexx = localY / 23;
               if (indexx >= 0 && indexx < this.model.simulation().rooms().size()) {
                  this.selectedSimRoomId = this.model.simulation().rooms().get(indexx).id();
               }
               break;
            case EXPORT:
               int index = localY / 34;
               if (index >= 0 && index < this.model.validation().issues().size()) {
                  String projectId = this.model.validation().issues().get(index).projectId();
                  if (!projectId.isBlank() && this.model.project(projectId).isPresent()) {
                     this.selectedProjectId = projectId;
                     this.controller.submit(new DungeonBuilderStudioController.SelectProject(projectId));
                  }
               }
         }
      }
   }

   private void handleDungeonCatalogClick(int mouseX, int mouseY) {
      DungeonBuilderStudioScreen.Rect list = this.dungeonCatalogListRect(this.dialogRect());
      if (list.contains(mouseX, mouseY)) {
         int localY = mouseY - list.y() + this.catalogScroll;
         int index = localY / 28;
         if (index >= 0 && index < this.model.dungeonDrafts().size()) {
            this.selectedDungeonDraftId = this.model.dungeonDrafts().get(index).id();
            this.dialogError = "";
         }
      }
   }

   private void handleMainClick(DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      switch (this.activeTab) {
         case ROOMS:
         case ANCHORS:
            this.selectRoomElementAt(rect, mouseX, mouseY);
            break;
         case POOLS:
            this.handlePoolEntryClick(rect, mouseX, mouseY);
            break;
         case LAYOUT:
            if (!this.selectLayoutSocketAt(rect, mouseX, mouseY)) {
               this.selectLayoutNodeAt(rect, mouseX, mouseY);
            }
            break;
         case SIMULATE:
            this.selectSimulationRoomAt(rect, mouseX, mouseY);
      }
   }

   private void selectRoomElementAt(DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project != null && project.bounds() != null) {
         DungeonBuilderStudioScreen.RoomMap map = DungeonBuilderStudioScreen.RoomMap.forProject(
            project, rect.inset(7, 18), this.canvasZoom, this.canvasPanX, this.canvasPanY
         );
         if (this.activeTab == DungeonBuilderStudioScreen.Tab.ANCHORS) {
            DungeonBuilderStudioModel.Anchor closest = null;
            double distance = Double.MAX_VALUE;

            for (DungeonBuilderStudioModel.Anchor anchor : project.anchors()) {
               double dx = mouseX - map.worldX(anchor.position().x());
               double dz = mouseY - map.worldZ(anchor.position().z());
               double candidate = dx * dx + dz * dz;
               if (candidate <= 64.0 && candidate < distance) {
                  closest = anchor;
                  distance = candidate;
               }
            }

            if (closest != null) {
               this.selectedAnchorId = closest.id();
               if (this.compact) {
                  this.compactPane = DungeonBuilderStudioScreen.CompactPane.INSPECTOR;
               }

               this.rebuildWidgets();
            }
         } else {
            DungeonBuilderStudioModel.Socket closest = null;
            double distance = Double.MAX_VALUE;

            for (DungeonBuilderStudioModel.Socket socket : project.sockets()) {
               double dx = mouseX - map.worldX(socket.position().x());
               double dz = mouseY - map.worldZ(socket.position().z());
               double candidate = dx * dx + dz * dz;
               if (candidate <= 64.0 && candidate < distance) {
                  closest = socket;
                  distance = candidate;
               }
            }

            if (closest != null) {
               this.selectedSocketId = closest.id();
               if (this.compact) {
                  this.compactPane = DungeonBuilderStudioScreen.CompactPane.INSPECTOR;
               }

               this.rebuildWidgets();
            }
         }
      }
   }

   private void handlePoolEntryClick(DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      DungeonBuilderStudioModel.MobPool pool = this.selectedPool().orElse(null);
      if (pool != null) {
         int localY = mouseY - (rect.y() + 18) + this.centerScroll[this.activeTab.ordinal()];
         int index = localY / 38;
         if (index >= 0 && index < pool.entries().size()) {
            DungeonBuilderStudioModel.PoolEntry entry = pool.entries().get(index);
            int right = rect.right() - 8;
            if (mouseX >= right - 54 && mouseX < right - 36) {
               this.controller.submit(new DungeonBuilderStudioController.UpsertPoolEntry(pool.id(), copyEntryWeight(entry, entry.weight() - 1)));
            } else if (mouseX >= right - 36 && mouseX < right - 18) {
               this.controller.submit(new DungeonBuilderStudioController.UpsertPoolEntry(pool.id(), copyEntryWeight(entry, entry.weight() + 1)));
            } else if (mouseX >= right - 18) {
               List<DungeonBuilderStudioModel.PoolEntry> replacement = new ArrayList<>(pool.entries());
               replacement.remove(index);
               this.controller.submit(new DungeonBuilderStudioController.SavePoolDraft(pool.id(), replacement));
            } else {
               this.openPoolEntryDialog(entry);
            }
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (this.activeDialog == DungeonBuilderStudioScreen.Dialog.DUNGEON_CATALOG) {
         int logicalX = (int)Math.round(this.logicalMouseX(mouseX));
         int logicalY = (int)Math.round(this.logicalMouseY(mouseY));
         DungeonBuilderStudioScreen.Rect dialog = this.dialogRect();
         if (this.dungeonCatalogListRect(dialog).contains(logicalX, logicalY)) {
            this.catalogScroll = clamp(this.catalogScroll - (int)Math.round(delta * 28.0), 0, this.maxCatalogScroll(dialog));
         }

         return true;
      } else {
         if (this.activeDialog != DungeonBuilderStudioScreen.Dialog.NONE) {
            return true;
         }

         int logicalX = (int)Math.round(this.logicalMouseX(mouseX));
         int logicalY = (int)Math.round(this.logicalMouseY(mouseY));
         DungeonBuilderStudioScreen.WorkspaceLayout layout = this.workspaceLayout();
         if (layout.left().contains(logicalX, logicalY)) {
            this.leftScroll[this.activeTab.ordinal()] = clamp(
               this.leftScroll[this.activeTab.ordinal()] - (int)Math.round(delta * 18.0), 0, this.maxLeftScroll(layout.left())
            );
            return true;
         }

         DungeonBuilderStudioScreen.Rect inspector = this.compact ? layout.main() : layout.inspector();
         if ((!this.compact || this.compactPane == DungeonBuilderStudioScreen.CompactPane.INSPECTOR) && inspector.contains(logicalX, logicalY)) {
            this.inspectorScroll[this.activeTab.ordinal()] = clamp(this.inspectorScroll[this.activeTab.ordinal()] - (int)Math.round(delta * 14.0), 0, 360);
            return true;
         }

         if ((!this.compact || this.compactPane == DungeonBuilderStudioScreen.CompactPane.CANVAS) && layout.main().contains(logicalX, logicalY)) {
            if (this.activeTab == DungeonBuilderStudioScreen.Tab.POOLS) {
               this.centerScroll[this.activeTab.ordinal()] = clamp(
                  this.centerScroll[this.activeTab.ordinal()] - (int)Math.round(delta * 18.0), 0, this.maxCenterScroll(layout.main())
               );
            } else {
               this.canvasZoom = clamp(this.canvasZoom + delta * 0.12, 0.5, 4.0);
            }

            return true;
         } else {
            return super.mouseScrolled(mouseX, mouseY, delta);
         }
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.activeDialog != DungeonBuilderStudioScreen.Dialog.NONE) {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }

      if (this.canvasPanning && button == 2) {
         float responsiveScale = this.responsiveTransform().scale();
         this.canvasPanX += dragX / responsiveScale;
         this.canvasPanY += dragY / responsiveScale;
         return true;
      }

      if (this.layoutNodeDragging
         && button == 0
         && this.activeTab == DungeonBuilderStudioScreen.Tab.LAYOUT
         && this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.FIXED) {
         DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
         List<DungeonBuilderStudioModel.LayoutNode> nodes = this.model.layout().nodes();
         if (node != null && !nodes.isEmpty() && !this.isLayoutNodeConnected(node.id())) {
            DungeonBuilderStudioScreen.GraphMap map = this.layoutDragMap == null
               ? this.layoutGraphMap(this.workspaceLayout().main().inset(7, 18), nodes)
               : this.layoutDragMap;
            float responsiveScale = this.responsiveTransform().scale();
            this.layoutDragRemainderX = this.layoutDragRemainderX + dragX / responsiveScale / map.scale();
            this.layoutDragRemainderZ = this.layoutDragRemainderZ + dragY / responsiveScale / map.scale();
            int moveX = (int)this.layoutDragRemainderX;
            int moveZ = (int)this.layoutDragRemainderZ;
            if (moveX == 0 && moveZ == 0) {
               return true;
            }

            this.layoutDragRemainderX -= moveX;
            this.layoutDragRemainderZ -= moveZ;
            DungeonBuilderStudioModel.LayoutNode moved = this.translateLayoutNodeWithoutCollision(node, moveX, moveZ);
            if (moved.x() != node.x() + moveX || moved.z() != node.z() + moveZ) {
               this.layoutDragBlocked = true;
               this.layoutDragRemainderX = 0.0;
               this.layoutDragRemainderZ = 0.0;
            }

            if (moved.x() != node.x() || moved.z() != node.z()) {
               this.replaceLayoutNode(moved);
            }

            return true;
         } else {
            this.layoutNodeDragging = false;
            this.layoutDragMap = null;
            return true;
         }
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 2) {
         this.canvasPanning = false;
      }

      if (button == 0 && this.layoutNodeDragging) {
         this.layoutNodeDragging = false;
         this.layoutDragMap = null;
         DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
         if (node != null) {
            this.feedback(
               this.layoutDragBlocked ? DungeonBuilderStudioModel.Severity.WARNING : DungeonBuilderStudioModel.Severity.INFO,
               (this.layoutDragBlocked ? "Stopped at another room. " : "Moved room. ")
                  + "Position: X "
                  + node.x()
                  + ", Z "
                  + node.z()
                  + ". Press Apply to save."
            );
         }

         this.layoutDragBlocked = false;
         return true;
      } else {
         return super.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (this.activeDialog != DungeonBuilderStudioScreen.Dialog.NONE) {
         if (key == 256) {
            this.closeDialog();
            return true;
         }

         if (key != 257 && key != 335) {
            return super.keyPressed(key, scanCode, modifiers);
         }

         this.confirmActiveDialog();
         return true;
      } else {
         if (key == 258 && hasControlDown()) {
            int step = hasShiftDown() ? -1 : 1;
            this.setTab(DungeonBuilderStudioScreen.Tab.values()[Math.floorMod(this.activeTab.ordinal() + step, DungeonBuilderStudioScreen.Tab.values().length)]);
            return true;
         }

         if (key != 70 || this.seedBox != null && this.seedBox.isFocused()) {
            if (key == 257 && this.activeTab == DungeonBuilderStudioScreen.Tab.SIMULATE && this.seedBox != null && this.seedBox.isFocused()) {
               this.runSimulation();
               return true;
            } else {
               return super.keyPressed(key, scanCode, modifiers);
            }
         } else {
            this.canvasZoom = 1.0;
            this.canvasPanX = this.canvasPanY = 0.0;
            return true;
         }
      }
   }

   private void createRoomProject() {
      this.roomIdDraft = this.suggestedRoomId();
      this.projectKindDraft = DungeonBuilderStudioModel.ProjectKind.MODULE;
      this.dialogError = "";
      this.activeDialog = DungeonBuilderStudioScreen.Dialog.NEW_ROOM;
      this.rebuildWidgets();
   }

   private void cycleRoomRole() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a room first.");
      } else {
         DungeonBuilderStudioModel.RoomRole[] roles = DungeonBuilderStudioModel.RoomRole.values();
         DungeonBuilderStudioModel.RoomRole next = roles[(project.role().ordinal() + 1) % roles.length];
         this.controller.submit(new DungeonBuilderStudioController.SetRoomRole(project.id(), next));
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Setting room role to " + next.name().toLowerCase(Locale.ROOT) + "...");
      }
   }

   private void adjustRoomWeight(int amount) {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a room first.");
      } else {
         this.controller.submit(new DungeonBuilderStudioController.SetRoomWeight(project.id(), clamp(project.weight() + amount, 1, 10000)));
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Setting the project's DEFAULT WEIGHT. Existing dungeon-specific weights are unchanged...");
      }
   }

   private void deleteRoomProject() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a room first.");
      } else if (!this.pendingProjectDelete.equals(project.id())) {
         this.pendingProjectDelete = project.id();
         this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Press Confirm Delete to remove " + project.id() + ".");
         this.rebuildWidgets();
      } else {
         this.controller.submit(new DungeonBuilderStudioController.DeleteProject(project.id()));
         this.pendingProjectDelete = "";
      }
   }

   private void openNewPoolDialog() {
      this.poolIdDraft = this.suggestedPoolId();
      this.dialogError = "";
      this.activeDialog = DungeonBuilderStudioScreen.Dialog.NEW_POOL;
      this.rebuildWidgets();
   }

   private void openPoolEntryDialog(DungeonBuilderStudioModel.PoolEntry entry) {
      if (this.selectedPool().isEmpty()) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select or create a pool first.");
      } else {
         DungeonBuilderStudioModel.PoolEntry source = entry == null
            ? new DungeonBuilderStudioModel.PoolEntry(
               DungeonBuilderStudioModel.SelectorKind.ENTITY,
               "minecraft:zombie",
               1,
               "",
               DungeonBuilderStudioModel.LevelRange.unset(),
               DungeonBuilderStudioModel.LevelRange.of(1, 1),
               DungeonBuilderStudioModel.OptionalXp.automatic()
            )
            : entry;
         this.editingSelectorId = entry == null ? "" : entry.selectorId();
         this.editingSelectorKind = entry == null ? DungeonBuilderStudioModel.SelectorKind.ENTITY : entry.selectorKind();
         this.selectorKindDraft = source.selectorKind();
         this.selectorIdDraft = source.selectorId();
         this.entryWeightDraft = Integer.toString(source.weight());
         this.requiredModDraft = source.requiredMod();
         this.eligibleRangePresent = source.eligibleLevel().present();
         this.eligibleMinDraft = Integer.toString(source.eligibleLevel().min());
         this.eligibleMaxDraft = Integer.toString(source.eligibleLevel().max());
         this.spawnRangePresent = source.spawnLevel().present();
         this.spawnMinDraft = Integer.toString(source.spawnLevel().min());
         this.spawnMaxDraft = Integer.toString(source.spawnLevel().max());
         this.baseXpPresent = source.baseXp().present();
         this.baseXpDraft = Integer.toString(source.baseXp().value());
         this.dialogError = "";
         this.entitySuggestionCursor = 0;
         this.activeDialog = DungeonBuilderStudioScreen.Dialog.POOL_ENTRY;
         this.rebuildWidgets();
      }
   }

   private void openLayoutSetupDialog() {
      if (this.model.draft(this.model.dungeonId()).isEmpty()) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Create or open a saved dungeon before editing its setup.");
      } else {
         DungeonBuilderStudioModel.LayoutDraft draft = this.model.layout();
         this.shellBlockDraft = draft.shellBlock();
         this.shellThicknessDraft = Integer.toString(draft.shellThickness());
         this.maxDepthDraft = Integer.toString(draft.maxDepth());
         this.rankDraft.clear();
         this.rankDraft.addAll(draft.ranks());
         if (this.rankDraft.containsAll(DUNGEON_RANKS)) {
            this.rankDraft.clear();
         }

         this.dialogError = "";
         this.activeDialog = DungeonBuilderStudioScreen.Dialog.LAYOUT_SETUP;
         this.rebuildWidgets();
      }
   }

   private void openDungeonCatalog() {
      if (!this.canUseDungeonCatalog()) {
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Preset exports directly; saved dungeon drafts are for MODULE rooms.");
      } else {
         if (this.model.draft(this.selectedDungeonDraftId).isEmpty()) {
            this.selectedDungeonDraftId = preferredDraftId(this.model, this.model.dungeonId());
         }

         this.dialogError = "";
         this.activeDialog = DungeonBuilderStudioScreen.Dialog.DUNGEON_CATALOG;
         this.catalogScroll = clamp(this.catalogScroll, 0, this.maxCatalogScroll(this.dialogRect()));
         this.rebuildWidgets();
      }
   }

   private void openNewDungeonDialog() {
      if (!this.canUseDungeonCatalog()) {
         this.dialogError = "Preset exports directly; select a MODULE room first.";
      } else if (this.layoutDirty) {
         this.dialogError = "Apply or discard the current layout edits before creating another dungeon.";
      } else {
         this.newDungeonIdDraft = this.suggestedDungeonId();
         this.dialogError = "";
         this.activeDialog = DungeonBuilderStudioScreen.Dialog.NEW_DUNGEON;
         this.rebuildWidgets();
      }
   }

   private void openDeleteDungeonDialog() {
      DungeonBuilderStudioModel.DraftSummary selected = this.model.draft(this.selectedDungeonDraftId).orElse(null);
      if (selected == null) {
         this.dialogError = "Select a saved dungeon first.";
      } else if (this.layoutDirty) {
         this.dialogError = "Apply or discard the current layout edits before deleting a dungeon.";
      } else {
         this.dialogError = "";
         this.activeDialog = DungeonBuilderStudioScreen.Dialog.DELETE_DUNGEON;
         this.rebuildWidgets();
      }
   }

   private void openSelectedDungeon() {
      DungeonBuilderStudioModel.DraftSummary selected = this.model.draft(this.selectedDungeonDraftId).orElse(null);
      if (selected == null) {
         this.dialogError = "Select a saved dungeon first.";
      } else if (selected.id().equals(this.model.dungeonId())) {
         this.closeDialog();
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, selected.id() + " is already the active dungeon.");
      } else if (this.layoutDirty) {
         this.dialogError = "Apply or discard the current layout edits before opening another dungeon.";
      } else {
         this.controller.submit(new DungeonBuilderStudioController.SelectDungeon(selected.id()));
         this.closeDialog();
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Opening saved dungeon " + selected.id() + "...");
      }
   }

   private void confirmNewDungeon() {
      this.captureDialogValues();
      String id = normalizedResourceId(this.newDungeonIdDraft);
      if (!validDungeonId(id)) {
         this.dialogError = "Use lowercase namespace:path (max 192); path segments cannot be empty or equal to '.' or '..'.";
      } else if (this.model.draft(id).isPresent()) {
         this.dialogError = "A saved dungeon with this ID already exists. Use Open instead.";
      } else {
         this.controller.submit(new DungeonBuilderStudioController.NewDungeon(id));
         this.selectedDungeonDraftId = id;
         this.closeDialog();
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Creating and opening blank dungeon " + id + "...");
      }
   }

   private void confirmDeleteDungeon() {
      DungeonBuilderStudioModel.DraftSummary selected = this.model.draft(this.selectedDungeonDraftId).orElse(null);
      if (selected == null) {
         this.dialogError = "The selected dungeon no longer exists.";
      } else if (this.layoutDirty) {
         this.dialogError = "Apply or discard the current layout edits before deleting a dungeon.";
      } else {
         String id = selected.id();
         this.controller.submit(new DungeonBuilderStudioController.DeleteDungeon(id));
         this.selectedDungeonDraftId = "";
         this.closeDialog();
         this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Deleting saved dungeon " + id + "...");
      }
   }

   private void openAnchorSetupDialog() {
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (anchor == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select an anchor point in the top view first.");
      } else {
         this.anchorEncounterDraft = anchor.encounterId();
         this.anchorMinLevelDraft = Integer.toString(anchor.minLevel());
         this.anchorMaxLevelDraft = Integer.toString(anchor.maxLevel());
         this.anchorLevelOverrideDraft = anchor.levelOverride();
         this.dialogError = "";
         this.activeDialog = DungeonBuilderStudioScreen.Dialog.ANCHOR_SETUP;
         this.rebuildWidgets();
      }
   }

   private void openPresetSetupDialog() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project != null && project.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET) {
         this.rankDraft.clear();
         this.rankDraft.addAll(project.ranks());
         if (this.rankDraft.containsAll(DUNGEON_RANKS)) {
            this.rankDraft.clear();
         }

         this.shellBlockDraft = project.shellBlock();
         this.shellThicknessDraft = Integer.toString(project.shellThickness());
         this.dialogError = "";
         this.activeDialog = DungeonBuilderStudioScreen.Dialog.PRESET_SETUP;
         this.rebuildWidgets();
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a PRESET project first.");
      }
   }

   private void confirmActiveDialog() {
      switch (this.activeDialog) {
         case NEW_ROOM:
            this.confirmCreateRoom();
            break;
         case NEW_POOL:
            this.confirmCreatePool();
            break;
         case POOL_ENTRY:
            this.confirmPoolEntry();
            break;
         case ANCHOR_SETUP:
            this.confirmAnchorSetup();
            break;
         case PRESET_SETUP:
            this.confirmPresetSetup();
            break;
         case DUNGEON_CATALOG:
            this.openSelectedDungeon();
            break;
         case NEW_DUNGEON:
            this.confirmNewDungeon();
            break;
         case DELETE_DUNGEON:
            this.confirmDeleteDungeon();
            break;
         case LAYOUT_SETUP:
            this.confirmLayoutSetup();
      }
   }

   private void confirmCreateRoom() {
      this.captureDialogValues();
      String requested = normalizedResourceId(this.roomIdDraft);
      if (!validRoomProjectId(requested)) {
         this.dialogError = "Room ID needs namespace:name; each part starts with a letter/number, uses _, - or ., and the name cannot contain /.";
      } else if (this.model.project(requested).isPresent()) {
         this.dialogError = "A room project with this ID already exists.";
      } else {
         int separator = requested.indexOf(58);
         this.controller
            .submit(
               new DungeonBuilderStudioController.CreateProject(requested.substring(0, separator), requested.substring(separator + 1), this.projectKindDraft)
            );
         this.selectedProjectId = requested;
         this.closeDialog();
         this.feedback(
            DungeonBuilderStudioModel.Severity.INFO, "Creating " + requested + " as a " + this.projectKindDraft.name().toLowerCase(Locale.ROOT) + "..."
         );
      }
   }

   private void confirmCreatePool() {
      this.captureDialogValues();
      String requested = normalizedResourceId(this.poolIdDraft);
      if (!validDatapackResourceId(requested, 192)) {
         this.dialogError = "Pool ID must use a filesystem-safe lowercase namespace:path.";
      } else if (this.model.pool(requested).isPresent()) {
         this.dialogError = "A pool with this ID already exists.";
      } else {
         this.controller.submit(new DungeonBuilderStudioController.CreatePool(requested));
         this.selectedPoolId = requested;
         this.closeDialog();
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Creating mob pool " + requested + "...");
      }
   }

   private void confirmPoolEntry() {
      this.captureDialogValues();
      DungeonBuilderStudioModel.MobPool pool = this.selectedPool().orElse(null);
      if (pool == null) {
         this.dialogError = "The selected pool no longer exists; close and refresh.";
      } else {
         String selectorId = normalizedResourceId(this.selectorIdDraft);
         if (!validResourceId(selectorId)) {
            this.dialogError = "Entity/tag ID must use lowercase namespace:name syntax.";
         } else {
            String requiredMod = this.requiredModDraft.trim().toLowerCase(Locale.ROOT);
            if (!validModId(requiredMod)) {
               this.dialogError = "Required mod must be empty or 2-64 characters: start with a letter, then use letters, numbers, _ or -.";
            } else {
               try {
                  int weight = parseIntDraft(this.entryWeightDraft, "Weight", 1, 1000000);
                  DungeonBuilderStudioModel.LevelRange eligible = this.parseRange(
                     this.eligibleRangePresent, this.eligibleMinDraft, this.eligibleMaxDraft, "Eligible level"
                  );
                  DungeonBuilderStudioModel.LevelRange spawn = this.parseRange(this.spawnRangePresent, this.spawnMinDraft, this.spawnMaxDraft, "Spawn level");
                  DungeonBuilderStudioModel.OptionalXp xp = this.baseXpPresent
                     ? new DungeonBuilderStudioModel.OptionalXp(true, parseIntDraft(this.baseXpDraft, "Base XP", 0, 1000000))
                     : DungeonBuilderStudioModel.OptionalXp.automatic();
                  DungeonBuilderStudioModel.PoolEntry entry = new DungeonBuilderStudioModel.PoolEntry(
                     this.selectorKindDraft, selectorId, weight, requiredMod, eligible, spawn, xp
                  );
                  String previousId = this.editingSelectorId;
                  List<DungeonBuilderStudioModel.PoolEntry> replacement = new ArrayList<>();

                  for (DungeonBuilderStudioModel.PoolEntry existing : pool.entries()) {
                     boolean original = !previousId.isBlank()
                        && existing.selectorKind() == this.editingSelectorKind
                        && existing.selectorId().equals(previousId);
                     boolean duplicate = previousId.isBlank()
                        && existing.selectorKind() == entry.selectorKind()
                        && existing.selectorId().equals(entry.selectorId());
                     if (!original && !duplicate) {
                        replacement.add(existing);
                     }
                  }

                  replacement.add(entry);
                  this.controller.submit(new DungeonBuilderStudioController.SavePoolDraft(pool.id(), replacement));
                  this.closeDialog();
                  this.feedback(DungeonBuilderStudioModel.Severity.INFO, (previousId.isBlank() ? "Adding " : "Updating ") + entry.selectorLabel() + "...");
               } catch (IllegalArgumentException exception) {
                  this.dialogError = exception.getMessage();
               }
            }
         }
      }
   }

   private void confirmLayoutSetup() {
      this.captureDialogValues();
      String dungeonId = this.model.dungeonId();
      String shellBlock = normalizedResourceId(this.shellBlockDraft);
      if (this.model.draft(dungeonId).isEmpty()) {
         this.dialogError = "Create or open a saved dungeon before editing its setup.";
      } else if (!validResourceId(shellBlock)) {
         this.dialogError = "Shell block must use lowercase namespace:name syntax.";
      } else {
         try {
            int thickness = parseIntDraft(this.shellThicknessDraft, "Shell thickness", 0, 4);
            int maxDepth = parseIntDraft(this.maxDepthDraft, "Maximum depth", 1, 64);
            DungeonBuilderStudioModel.LayoutDraft source = this.model.layout();
            DungeonBuilderStudioModel.LayoutDraft updated = new DungeonBuilderStudioModel.LayoutDraft(
               source.mode(),
               source.topology(),
               source.minRooms(),
               source.maxRooms(),
               maxDepth,
               Set.copyOf(this.rankDraft),
               shellBlock,
               thickness,
               source.enabledProjectIds(),
               source.roomWeights(),
               source.nodes(),
               source.connections()
            );
            this.setLocalLayout(updated);
            this.closeDialog();
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Updated setup for " + dungeonId + ". Press Layout Apply to save.");
         } catch (IllegalArgumentException exception) {
            this.dialogError = exception.getMessage();
         }
      }
   }

   private void confirmAnchorSetup() {
      this.captureDialogValues();
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (project != null && anchor != null) {
         String encounterId = this.anchorEncounterDraft.trim().toLowerCase(Locale.ROOT);
         if (!validEncounterId(encounterId)) {
            this.dialogError = "Encounter ID is 1-64 characters, starts with a letter/number, uses _, - or ., and cannot end with a dot.";
         } else if (anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER) {
            this.controller
               .submit(
                  new DungeonBuilderStudioController.AssignAnchor(
                     project.id(),
                     anchor.id(),
                     DungeonBuilderStudioModel.AnchorKind.TRIGGER,
                     DungeonBuilderStudioModel.SpawnRole.NONE,
                     anchor.triggerBounds(),
                     encounterId,
                     "",
                     false,
                     anchor.minLevel(),
                     anchor.maxLevel(),
                     false
                  )
               );
            this.closeDialog();
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Saving trigger encounter group...");
         } else {
            try {
               int min = anchor.minLevel();
               int max = anchor.maxLevel();
               if (this.anchorLevelOverrideDraft) {
                  DungeonBuilderStudioModel.LevelRange range = this.parseRange(true, this.anchorMinLevelDraft, this.anchorMaxLevelDraft, "Anchor level");
                  min = range.min();
                  max = range.max();
               }

               this.controller
                  .submit(
                     new DungeonBuilderStudioController.AssignAnchor(
                        project.id(),
                        anchor.id(),
                        anchor.kind(),
                        anchor.spawnRole(),
                        anchor.triggerBounds(),
                        encounterId,
                        anchor.poolId(),
                        this.anchorLevelOverrideDraft,
                        min,
                        max,
                        anchor.delayed()
                     )
                  );
               this.closeDialog();
               this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Saving encounter group and level policy...");
            } catch (IllegalArgumentException exception) {
               this.dialogError = exception.getMessage();
            }
         }
      } else {
         this.dialogError = "The selected anchor no longer exists; close and refresh.";
      }
   }

   private void confirmPresetSetup() {
      this.captureDialogValues();
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      if (project != null && project.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET) {
         String shellBlock = normalizedResourceId(this.shellBlockDraft);
         if (!validResourceId(shellBlock)) {
            this.dialogError = "Shell block must use lowercase namespace:name syntax.";
         } else {
            try {
               int thickness = parseIntDraft(this.shellThicknessDraft, "Shell thickness", 0, 4);
               this.controller.submit(new DungeonBuilderStudioController.SetProjectSettings(project.id(), Set.copyOf(this.rankDraft), shellBlock, thickness));
               this.closeDialog();
               this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Saving preset rank routing and shell rules...");
            } catch (IllegalArgumentException exception) {
               this.dialogError = exception.getMessage();
            }
         }
      } else {
         this.dialogError = "The selected preset no longer exists; close and refresh.";
      }
   }

   private void closeDialog() {
      this.captureDialogValues();
      this.activeDialog = DungeonBuilderStudioScreen.Dialog.NONE;
      this.dialogError = "";
      this.editingSelectorId = "";
      this.editingSelectorKind = DungeonBuilderStudioModel.SelectorKind.ENTITY;
      this.rebuildWidgets();
   }

   private void toggleSelectorKind() {
      this.selectorKindDraft = this.selectorKindDraft == DungeonBuilderStudioModel.SelectorKind.ENTITY
         ? DungeonBuilderStudioModel.SelectorKind.TAG
         : DungeonBuilderStudioModel.SelectorKind.ENTITY;
      if (this.selectorIdBox != null) {
         String value = this.selectorIdBox.getValue();
         this.selectorIdBox.setValue(value.startsWith("#") ? value.substring(1) : value);
      }

      this.dialogError = "";
      if (this.selectorKindButton != null) {
         this.selectorKindButton.setMessage(Component.literal(this.selectorKindLabel()));
      }
   }

   private void toggleProjectKind() {
      this.projectKindDraft = this.projectKindDraft == DungeonBuilderStudioModel.ProjectKind.MODULE
         ? DungeonBuilderStudioModel.ProjectKind.PRESET
         : DungeonBuilderStudioModel.ProjectKind.MODULE;
      if (this.projectKindButton != null) {
         this.projectKindButton.setMessage(Component.literal("TYPE: " + this.projectKindDraft.name()));
      }
   }

   private void toggleEligibleRange() {
      this.eligibleRangePresent = !this.eligibleRangePresent;
      if (this.eligibleRangeButton != null) {
         this.eligibleRangeButton.setMessage(Component.literal(this.eligibleRangeLabel()));
      }
   }

   private void toggleSpawnRange() {
      this.spawnRangePresent = !this.spawnRangePresent;
      if (this.spawnRangeButton != null) {
         this.spawnRangeButton.setMessage(Component.literal(this.spawnRangeLabel()));
      }
   }

   private void toggleXpMode() {
      this.baseXpPresent = !this.baseXpPresent;
      if (this.xpModeButton != null) {
         this.xpModeButton.setMessage(Component.literal(this.xpModeLabel()));
      }
   }

   private void toggleAnchorLevelOverride() {
      this.anchorLevelOverrideDraft = !this.anchorLevelOverrideDraft;
      if (this.anchorLevelModeButton != null) {
         this.anchorLevelModeButton.setMessage(Component.literal(this.anchorLevelModeLabel()));
      }
   }

   private void toggleRank(String rank) {
      if (!this.rankDraft.remove(rank)) {
         this.rankDraft.add(rank);
      }
   }

   private void suggestEntityId() {
      if (this.selectorKindDraft == DungeonBuilderStudioModel.SelectorKind.TAG) {
         this.dialogError = "Entity tags come from datapacks; enter their namespaced ID without the # symbol.";
      } else {
         List<String> installed = BuiltInRegistries.ENTITY_TYPE.keySet().stream().map(ResourceLocation::toString).sorted().toList();
         if (installed.isEmpty()) {
            this.dialogError = "No installed entity registry entries are available.";
         } else {
            String current = this.selectorIdBox == null ? this.selectorIdDraft : this.selectorIdBox.getValue();
            int exact = installed.indexOf(current);
            String suggestion;
            if (exact >= 0) {
               suggestion = installed.get((exact + 1) % installed.size());
            } else {
               List<String> matches = installed.stream().filter(id -> id.startsWith(current)).limit(256L).toList();
               List<String> candidates = matches.isEmpty() ? installed : matches;
               suggestion = candidates.get(Math.floorMod(this.entitySuggestionCursor++, candidates.size()));
            }

            this.selectorIdDraft = suggestion;
            if (this.selectorIdBox != null) {
               this.selectorIdBox.setValue(suggestion);
            }

            this.dialogError = "";
         }
      }
   }

   private String selectorKindLabel() {
      return this.selectorKindDraft == DungeonBuilderStudioModel.SelectorKind.ENTITY ? "ENTITY" : "TAG (#)";
   }

   private String eligibleRangeLabel() {
      return this.eligibleRangePresent ? "ELIGIBLE: SET" : "ELIGIBLE: ALL";
   }

   private String spawnRangeLabel() {
      return this.spawnRangePresent ? "SPAWN: SET" : "SPAWN: RANK";
   }

   private String xpModeLabel() {
      return this.baseXpPresent ? "XP: EXPLICIT" : "XP: AUTO";
   }

   private String anchorLevelModeLabel() {
      return this.anchorLevelOverrideDraft ? "LEVEL: OVERRIDE" : "LEVEL: INHERIT";
   }

   private String selectorResolutionText() {
      if (this.selectorKindDraft == DungeonBuilderStudioModel.SelectorKind.TAG) {
         return "[TAG] Resolved from loaded datapacks when the dungeon is loaded.";
      } else {
         String value = this.selectorIdBox == null ? this.selectorIdDraft : this.selectorIdBox.getValue();
         String required = this.requiredModBox == null ? this.requiredModDraft : this.requiredModBox.getValue();
         ResourceLocation id = ResourceLocation.tryParse(value);
         if (id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id) && "sololeveling".equals(required)) {
            return "[LOADED] Optional Mod is unnecessary for Solo Leveling entities.";
         } else {
            return id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id)
               ? "[LOADED] Entity is present in the current mod registry."
               : "[EXTERNAL] Allowed; set Optional Mod only when this addon may be absent.";
         }
      }
   }

   private int selectorResolutionColor() {
      if (this.selectorKindDraft == DungeonBuilderStudioModel.SelectorKind.TAG) {
         return -9971457;
      }

      String value = this.selectorIdBox == null ? this.selectorIdDraft : this.selectorIdBox.getValue();
      ResourceLocation id = ResourceLocation.tryParse(value);
      return id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id) ? -10493044 : -11930;
   }

   private DungeonBuilderStudioModel.LevelRange parseRange(boolean present, String minText, String maxText, String label) {
      if (!present) {
         return DungeonBuilderStudioModel.LevelRange.unset();
      } else {
         int min = parseIntDraft(minText, label + " minimum", 1, 1000);
         int max = parseIntDraft(maxText, label + " maximum", 1, 1000);
         if (max < min) {
            throw new IllegalArgumentException(label + " maximum cannot be lower than its minimum.");
         } else {
            return DungeonBuilderStudioModel.LevelRange.of(min, max);
         }
      }
   }

   private static int parseIntDraft(String text, String label, int min, int max) {
      try {
         int value = Integer.parseInt(text != null && !text.isBlank() ? text : "-1");
         if (value >= min && value <= max) {
            return value;
         } else {
            throw new IllegalArgumentException(label + " must be between " + min + " and " + max + ".");
         }
      } catch (NumberFormatException exception) {
         throw new IllegalArgumentException(label + " must be a whole number.");
      }
   }

   private String preferredNamespace() {
      String source = this.selectedProject().map(DungeonBuilderStudioModel.Project::id).orElse(this.model.dungeonId());
      int separator = source.indexOf(58);
      return separator > 0 ? source.substring(0, separator) : "builder";
   }

   private String suggestedPoolId() {
      String namespace = this.preferredNamespace();
      int suffix = Math.max(1, this.model.pools().size() + 1);

      String id;
      do {
         id = namespace + ":pool_" + suffix++;
      } while (this.model.pool(id).isPresent());

      return id;
   }

   private String suggestedRoomId() {
      String namespace = this.preferredNamespace();
      int suffix = 1;

      String id;
      do {
         id = namespace + ":room_" + suffix++;
      } while (this.model.project(id).isPresent());

      return id;
   }

   private String suggestedDungeonId() {
      String namespace = this.preferredNamespace();
      int suffix = 1;

      String id;
      do {
         id = namespace + ":dungeon_" + suffix++;
      } while (this.model.draft(id).isPresent());

      return id;
   }

   private void captureSelectedProject() {
      this.selectedProject()
         .ifPresentOrElse(
            project -> {
               if (project.bounds() == null) {
                  this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Set structure bounds before capturing.");
               } else {
                  this.controller.submit(new DungeonBuilderStudioController.CaptureSnapshot(project.id(), project.snapshotCaptured()));
                  this.feedback(
                     DungeonBuilderStudioModel.Severity.INFO, project.snapshotCaptured() ? "Updating explicit room snapshot..." : "Capturing room snapshot..."
                  );
               }
            },
            () -> this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a room first.")
         );
   }

   private void toggleSelectedSocketRequired() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      DungeonBuilderStudioModel.Socket socket = this.selectedSocket().orElse(null);
      if (project != null && socket != null) {
         this.controller.submit(new DungeonBuilderStudioController.EditSocket(project.id(), socket.id(), socket.type(), !socket.required()));
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a socket in the top view first.");
      }
   }

   private void cycleSelectedSocketType() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      DungeonBuilderStudioModel.Socket socket = this.selectedSocket().orElse(null);
      if (project != null && socket != null) {
         DungeonBuilderStudioModel.SocketType next = DungeonBuilderStudioModel.SocketType.values()[(socket.type().ordinal() + 1)
            % DungeonBuilderStudioModel.SocketType.values().length];
         this.controller.submit(new DungeonBuilderStudioController.EditSocket(project.id(), socket.id(), next, socket.required()));
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a socket in the top view first.");
      }
   }

   private void cycleSelectedAnchorRole() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (project != null && anchor != null) {
         if (anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER) {
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Trigger anchors only edit their encounter group through Configure.");
         } else {
            DungeonBuilderStudioModel.SpawnRole next = switch (anchor.spawnRole()) {
               case NONE, BOSS -> DungeonBuilderStudioModel.SpawnRole.NORMAL;
               case NORMAL -> DungeonBuilderStudioModel.SpawnRole.ELITE;
               case ELITE -> DungeonBuilderStudioModel.SpawnRole.BOSS;
            };

            DungeonBuilderStudioModel.AnchorKind kind = switch (next) {
               case BOSS -> DungeonBuilderStudioModel.AnchorKind.BOSS_SPAWN;
               case NORMAL -> DungeonBuilderStudioModel.AnchorKind.MOB_SPAWN;
               case ELITE -> DungeonBuilderStudioModel.AnchorKind.ELITE_SPAWN;
               default -> DungeonBuilderStudioModel.AnchorKind.SPAWN_POINT;
            };
            String pool = anchor.poolId();
            if (pool.isBlank()) {
               if (this.model.pools().isEmpty()) {
                  this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Create a mob pool first; a spawn role requires both role and pool.");
                  return;
               }

               pool = this.model.pools().get(0).id();
            }

            this.submitAnchor(project, anchor, kind, next, pool, anchor.levelOverride(), anchor.minLevel(), anchor.maxLevel(), anchor.delayed());
         }
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select an anchor in the top view first.");
      }
   }

   private void cycleSelectedAnchorPool() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (project != null && anchor != null) {
         if (anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER) {
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Trigger anchors do not own mob pools; configure the group's spawn anchors.");
         } else if (this.model.pools().isEmpty()) {
            this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Create a mob pool before assigning this anchor.");
         } else {
            int current = -1;

            for (int index = 0; index < this.model.pools().size(); index++) {
               if (this.model.pools().get(index).id().equals(anchor.poolId())) {
                  current = index;
               }
            }

            String pool = this.model.pools().get((current + 1) % this.model.pools().size()).id();
            DungeonBuilderStudioModel.SpawnRole role = anchor.spawnRole() == DungeonBuilderStudioModel.SpawnRole.NONE
               ? DungeonBuilderStudioModel.SpawnRole.NORMAL
               : anchor.spawnRole();
            DungeonBuilderStudioModel.AnchorKind kind = anchor.spawnRole() == DungeonBuilderStudioModel.SpawnRole.NONE
               ? DungeonBuilderStudioModel.AnchorKind.MOB_SPAWN
               : anchor.kind();
            this.submitAnchor(project, anchor, kind, role, pool, anchor.levelOverride(), anchor.minLevel(), anchor.maxLevel(), anchor.delayed());
         }
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select an anchor in the top view first.");
      }
   }

   private void shiftSelectedAnchorLevel(int amount) {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (project != null && anchor != null) {
         if (anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER) {
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Trigger anchors inherit encounter timing; levels belong to spawn anchors.");
         } else {
            int min = clamp(anchor.minLevel() + amount, 1, 1000);
            int max = clamp(anchor.maxLevel() + amount, min, 1000);
            this.submitAnchor(project, anchor, anchor.kind(), anchor.spawnRole(), anchor.poolId(), true, min, max, anchor.delayed());
         }
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select an anchor in the top view first.");
      }
   }

   private void toggleSelectedAnchorDelay() {
      DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
      DungeonBuilderStudioModel.Anchor anchor = this.selectedAnchor().orElse(null);
      if (project != null && anchor != null) {
         if (anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER) {
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Trigger anchors are the gate; delay is configured on spawn anchors.");
         } else {
            this.submitAnchor(
               project,
               anchor,
               anchor.kind(),
               anchor.spawnRole(),
               anchor.poolId(),
               anchor.levelOverride(),
               anchor.minLevel(),
               anchor.maxLevel(),
               !anchor.delayed()
            );
         }
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select an anchor in the top view first.");
      }
   }

   private void submitAnchor(
      DungeonBuilderStudioModel.Project project,
      DungeonBuilderStudioModel.Anchor anchor,
      DungeonBuilderStudioModel.AnchorKind kind,
      DungeonBuilderStudioModel.SpawnRole role,
      String pool,
      boolean levelOverride,
      int minLevel,
      int maxLevel,
      boolean delayed
   ) {
      this.controller
         .submit(
            new DungeonBuilderStudioController.AssignAnchor(
               project.id(), anchor.id(), kind, role, anchor.triggerBounds(), anchor.encounterId(), pool, levelOverride, minLevel, maxLevel, delayed
            )
         );
   }

   private void createPoolDraft() {
      this.openNewPoolDialog();
   }

   private void beginPoolEntryDraft() {
      this.openPoolEntryDialog(null);
   }

   private void savePoolDraft() {
      this.selectedPool()
         .ifPresentOrElse(
            pool -> this.controller.submit(new DungeonBuilderStudioController.SavePoolDraft(pool.id(), pool.entries())),
            () -> this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select or create a pool first.")
         );
   }

   private void deletePool() {
      DungeonBuilderStudioModel.MobPool pool = this.selectedPool().orElse(null);
      if (pool == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a pool first.");
      } else if (!this.pendingPoolDelete.equals(pool.id())) {
         this.pendingPoolDelete = pool.id();
         this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Press Confirm Delete to remove " + pool.id() + ".");
         this.rebuildWidgets();
      } else {
         this.controller.submit(new DungeonBuilderStudioController.DeletePool(pool.id()));
         this.pendingPoolDelete = "";
      }
   }

   private String layoutIncludeLabel() {
      return this.selectedProject().filter(project -> this.model.layout().enabledProjectIds().contains(project.id())).isPresent() ? "Exclude" : "Include";
   }

   private void toggleSelectedProjectIncluded() {
      if (this.hasActiveDungeonDraft()) {
         DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
         if (project == null) {
            this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a room in the library first.");
         } else if (project.kind() != DungeonBuilderStudioModel.ProjectKind.MODULE) {
            this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "PRESET is a complete dungeon. Include MODULE rooms in procedural layouts.");
         } else {
            DungeonBuilderStudioModel.LayoutDraft source = this.model.layout();
            List<String> enabled = new ArrayList<>(source.enabledProjectIds());
            boolean removing = enabled.remove(project.id());
            if (!removing) {
               enabled.add(project.id());
            }

            List<DungeonBuilderStudioModel.RoomWeight> weights = this.ensureRoomWeight(source.roomWeights(), project);
            this.setLocalLayout(this.copyLayoutParts(source, enabled, weights, source.nodes(), source.connections()));
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, (removing ? "Excluded " : "Included ") + project.id() + ". Press Apply to save.");
            this.rebuildWidgets();
         }
      }
   }

   private void addSelectedRoomNode() {
      if (this.hasActiveDungeonDraft()) {
         if (this.model.layout().mode() != DungeonBuilderStudioModel.LayoutMode.FIXED) {
            this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Switch Layout Mode to FIXED before placing exact nodes.");
         } else {
            DungeonBuilderStudioModel.Project project = this.selectedProject().orElse(null);
            if (project != null && project.kind() == DungeonBuilderStudioModel.ProjectKind.MODULE && project.bounds() != null) {
               DungeonBuilderStudioModel.LayoutDraft source = this.model.layout();
               if (source.nodes().size() >= 64) {
                  this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Fixed layouts support at most 64 placements.");
               } else {
                  int suffix = 1;

                  String candidate;
                  do {
                     candidate = "node_" + suffix++;
                  } while (!source.nodes().stream().noneMatch(nodex -> nodex.id().equals(candidate)));

                  String nodeId = candidate;
                  int var9 = source.nodes().stream().mapToInt(nodex -> nodex.x() + nodex.width()).max().orElse(-4) + 4;
                  DungeonBuilderStudioModel.LayoutNode node = new DungeonBuilderStudioModel.LayoutNode(
                     nodeId, project.id(), project.role(), var9, 0, 0, project.bounds().width(), project.bounds().depth(), 0, false
                  );
                  List<DungeonBuilderStudioModel.LayoutNode> nodes = new ArrayList<>(source.nodes());
                  nodes.add(node);
                  List<String> enabled = new ArrayList<>(source.enabledProjectIds());
                  if (!enabled.contains(project.id())) {
                     enabled.add(project.id());
                  }

                  this.setLocalLayout(this.copyLayoutParts(source, enabled, this.ensureRoomWeight(source.roomWeights(), project), nodes, source.connections()));
                  this.selectedLayoutNodeId = node.id();
                  this.selectedLayoutSocketId = "";
                  this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Placed " + node.id() + " at X " + node.x() + ", Z " + node.z() + ".");
                  this.rebuildWidgets();
               }
            } else {
               this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a captured room with valid bounds first.");
            }
         }
      }
   }

   private void moveSelectedLayoutNode(int dx, int dz) {
      DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
      if (node == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Click a fixed-layout node first.");
      } else if (this.isLayoutNodeConnected(node.id())) {
         this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Disconnect this node before moving it; connected socket geometry must stay aligned.");
      } else {
         int step = hasShiftDown() ? 4 : 1;
         DungeonBuilderStudioModel.LayoutNode moved = this.translateLayoutNodeWithoutCollision(node, dx * step, dz * step);
         if (moved.x() == node.x() && moved.z() == node.z()) {
            this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Another room blocks movement in that direction.");
         } else {
            this.replaceLayoutNode(moved);
            boolean blocked = moved.x() != node.x() + dx * step || moved.z() != node.z() + dz * step;
            this.feedback(
               blocked ? DungeonBuilderStudioModel.Severity.WARNING : DungeonBuilderStudioModel.Severity.INFO,
               (blocked ? "Stopped at another room. " : "") + "Node position: X " + moved.x() + ", Z " + moved.z() + "."
            );
         }
      }
   }

   private void rotateSelectedLayoutNode() {
      DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
      if (node == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Click a fixed-layout node first.");
      } else if (this.isLayoutNodeConnected(node.id())) {
         this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Disconnect this node before rotating it; connected socket geometry must stay aligned.");
      } else {
         DungeonBuilderStudioModel.LayoutNode rotated = new DungeonBuilderStudioModel.LayoutNode(
            node.id(), node.projectId(), node.role(), node.x(), node.y(), node.z(), node.depth(), node.width(), node.rotation() + 90, node.locked()
         );
         if (this.layoutNodeCollides(rotated)) {
            this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Rotation would overlap another room. Move this node first.");
         } else {
            this.replaceLayoutNode(rotated);
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Rotated " + node.id() + " to " + Math.floorMod(node.rotation() + 90, 360) + " degrees.");
         }
      }
   }

   private void deleteSelectedLayoutNode() {
      DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
      if (node == null) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Click a fixed-layout node first.");
      } else {
         DungeonBuilderStudioModel.LayoutDraft source = this.model.layout();
         List<DungeonBuilderStudioModel.LayoutNode> nodes = source.nodes().stream().filter(value -> !value.id().equals(node.id())).toList();
         List<DungeonBuilderStudioModel.LayoutConnection> connections = source.connections()
            .stream()
            .filter(connection -> !connection.fromNodeId().equals(node.id()) && !connection.toNodeId().equals(node.id()))
            .toList();
         List<String> enabled = new ArrayList<>(source.enabledProjectIds());
         if (nodes.stream().noneMatch(value -> value.projectId().equals(node.projectId()))) {
            enabled.remove(node.projectId());
         }

         this.setLocalLayout(this.copyLayoutParts(source, enabled, source.roomWeights(), nodes, connections));
         this.selectedLayoutNodeId = "";
         this.selectedLayoutSocketId = "";
         if (this.pendingConnectionNodeId.equals(node.id())) {
            this.clearPendingConnection();
         }

         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Removed " + node.id() + " and its connections.");
         this.rebuildWidgets();
      }
   }

   private void cycleLayoutSocket() {
      DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
      DungeonBuilderStudioModel.Project project = node == null ? null : this.model.project(node.projectId()).orElse(null);
      if (node != null && project != null && !project.sockets().isEmpty()) {
         int current = -1;

         for (int index = 0; index < project.sockets().size(); index++) {
            if (project.sockets().get(index).id().equals(this.selectedLayoutSocketId)) {
               current = index;
            }
         }

         DungeonBuilderStudioModel.Socket socket = project.sockets().get((current + 1) % project.sockets().size());
         this.selectedLayoutSocketId = socket.id();
         this.feedback(
            DungeonBuilderStudioModel.Severity.INFO,
            "Endpoint " + node.id() + " / " + socket.id() + " faces " + rotatedFacing(socket.facing(), node.rotation()).name() + "."
         );
         this.rebuildWidgets();
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a node whose room has at least one socket.");
      }
   }

   private String layoutLinkLabel() {
      DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
      return node != null && !this.selectedLayoutSocketId.isBlank() && this.isLayoutSocketUsed(node.id(), this.selectedLayoutSocketId) ? "Unlink" : "Link";
   }

   private void connectSelectedLayoutSocket() {
      DungeonBuilderStudioModel.LayoutNode node = this.selectedLayoutNode().orElse(null);
      DungeonBuilderStudioModel.Socket socket = node == null ? null : this.layoutSocket(node, this.selectedLayoutSocketId).orElse(null);
      if (node != null && socket != null) {
         if (this.isLayoutSocketUsed(node.id(), socket.id())) {
            List<DungeonBuilderStudioModel.LayoutConnection> remaining = this.model
               .layout()
               .connections()
               .stream()
               .filter(
                  connection -> (!connection.fromNodeId().equals(node.id()) || !connection.fromSocketId().equals(socket.id()))
                     && (!connection.toNodeId().equals(node.id()) || !connection.toSocketId().equals(socket.id()))
               )
               .toList();
            this.setLocalLayout(
               this.copyLayoutParts(
                  this.model.layout(), this.model.layout().enabledProjectIds(), this.model.layout().roomWeights(), this.model.layout().nodes(), remaining
               )
            );
            this.clearPendingConnection();
            this.feedback(
               DungeonBuilderStudioModel.Severity.INFO,
               "Disconnected " + node.id() + " / " + socket.id() + ". The rooms keep their current positions until you move or relink them."
            );
            this.rebuildWidgets();
         } else if (this.pendingConnectionNodeId.isBlank()) {
            this.pendingConnectionNodeId = node.id();
            this.pendingConnectionSocketId = socket.id();
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "First endpoint stored. Select another node/socket and press Link.");
         } else if (this.model.layout().connections().size() >= 128) {
            this.clearPendingConnection();
            this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Fixed layouts support at most 128 connections.");
         } else {
            DungeonBuilderStudioModel.LayoutNode fromNode = this.model
               .layout()
               .nodes()
               .stream()
               .filter(valuex -> valuex.id().equals(this.pendingConnectionNodeId))
               .findFirst()
               .orElse(null);
            DungeonBuilderStudioModel.Socket fromSocket = fromNode == null ? null : this.layoutSocket(fromNode, this.pendingConnectionSocketId).orElse(null);
            if (fromNode != null && fromSocket != null) {
               if (fromNode.id().equals(node.id())) {
                  this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "Choose an endpoint on a different node.");
               } else if (this.isLayoutSocketUsed(fromNode.id(), fromSocket.id())) {
                  this.clearPendingConnection();
                  this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "The first endpoint was connected elsewhere; select another endpoint.");
               } else if (!this.compatibleLayoutSockets(fromNode, fromSocket, node, socket)) {
                  this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Sockets must face opposite directions and match type/opening size.");
               } else {
                  boolean fromConnected = this.isLayoutNodeConnected(fromNode.id());
                  boolean targetConnected = this.isLayoutNodeConnected(node.id());
                  if (fromConnected && targetConnected) {
                     this.feedback(
                        DungeonBuilderStudioModel.Severity.ERROR,
                        "Both rooms already belong to the layout graph. Link a new/unconnected node to avoid breaking existing geometry."
                     );
                  } else {
                     DungeonBuilderStudioModel.LayoutNode movingNode = targetConnected ? fromNode : node;
                     DungeonBuilderStudioModel.LayoutNode snapped = targetConnected
                        ? this.snapLayoutNode(node, socket, fromNode, fromSocket)
                        : this.snapLayoutNode(fromNode, fromSocket, node, socket);
                     if (snapped == null) {
                        this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Socket coordinates could not be transformed from the captured room bounds.");
                     } else if (this.layoutNodeCollides(snapped)) {
                        this.feedback(
                           DungeonBuilderStudioModel.Severity.ERROR, "Auto-placement would overlap another room. Move or rotate that branch, then Link again."
                        );
                     } else {
                        List<DungeonBuilderStudioModel.LayoutConnection> connections = new ArrayList<>(this.model.layout().connections());
                        connections.add(new DungeonBuilderStudioModel.LayoutConnection(fromNode.id(), fromSocket.id(), node.id(), socket.id()));
                        List<DungeonBuilderStudioModel.LayoutNode> snappedNodes = new ArrayList<>(this.model.layout().nodes().size());

                        for (DungeonBuilderStudioModel.LayoutNode value : this.model.layout().nodes()) {
                           snappedNodes.add(value.id().equals(snapped.id()) ? snapped : value);
                        }

                        this.setLocalLayout(
                           this.copyLayoutParts(
                              this.model.layout(), this.model.layout().enabledProjectIds(), this.model.layout().roomWeights(), snappedNodes, connections
                           )
                        );
                        this.clearPendingConnection();
                        this.feedback(
                           DungeonBuilderStudioModel.Severity.PASS,
                           "Connected and snapped "
                              + movingNode.id()
                              + " to X "
                              + snapped.x()
                              + ", Y "
                              + snapped.y()
                              + ", Z "
                              + snapped.z()
                              + ". Press Apply to save."
                        );
                        this.rebuildWidgets();
                     }
                  }
               }
            } else {
               this.clearPendingConnection();
               this.feedback(DungeonBuilderStudioModel.Severity.WARNING, "The first endpoint disappeared; select it again.");
            }
         }
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Select a node, press Socket to choose its endpoint, then press Link.");
      }
   }

   private DungeonBuilderStudioModel.LayoutNode snapLayoutNode(
      DungeonBuilderStudioModel.LayoutNode sourceNode,
      DungeonBuilderStudioModel.Socket sourceSocket,
      DungeonBuilderStudioModel.LayoutNode targetNode,
      DungeonBuilderStudioModel.Socket targetSocket
   ) {
      DungeonBuilderStudioModel.Point sourceOffset = this.transformedSocketOffset(sourceNode, sourceSocket);
      DungeonBuilderStudioModel.Point targetOffset = this.transformedSocketOffset(targetNode, targetSocket);
      DungeonBuilderStudioModel.Project sourceProject = this.model.project(sourceNode.projectId()).orElse(null);
      DungeonBuilderStudioModel.Project targetProject = this.model.project(targetNode.projectId()).orElse(null);
      if (sourceOffset != null
         && targetOffset != null
         && sourceProject != null
         && targetProject != null
         && sourceProject.bounds() != null
         && targetProject.bounds() != null) {
         int sourceSocketX = sourceNode.x() + sourceOffset.x();
         int sourceSocketY = sourceNode.y() + sourceOffset.y();
         int sourceSocketZ = sourceNode.z() + sourceOffset.z();
         int x = targetNode.x();
         int y = sourceSocketY - targetOffset.y();
         int z = targetNode.z();
         switch (rotatedFacing(sourceSocket.facing(), sourceNode.rotation())) {
            case EAST:
               x = sourceNode.x() + sourceNode.width();
               z = sourceSocketZ - targetOffset.z();
               break;
            case WEST:
               x = sourceNode.x() - targetNode.width();
               z = sourceSocketZ - targetOffset.z();
               break;
            case SOUTH:
               x = sourceSocketX - targetOffset.x();
               z = sourceNode.z() + sourceNode.depth();
               break;
            case NORTH:
               x = sourceSocketX - targetOffset.x();
               z = sourceNode.z() - targetNode.depth();
               break;
            case UP:
               x = sourceSocketX - targetOffset.x();
               y = sourceNode.y() + sourceProject.bounds().height();
               z = sourceSocketZ - targetOffset.z();
               break;
            case DOWN:
               x = sourceSocketX - targetOffset.x();
               y = sourceNode.y() - targetProject.bounds().height();
               z = sourceSocketZ - targetOffset.z();
         }

         return new DungeonBuilderStudioModel.LayoutNode(
            targetNode.id(),
            targetNode.projectId(),
            targetNode.role(),
            x,
            y,
            z,
            targetNode.width(),
            targetNode.depth(),
            targetNode.rotation(),
            targetNode.locked()
         );
      } else {
         return null;
      }
   }

   private DungeonBuilderStudioModel.Point transformedSocketOffset(DungeonBuilderStudioModel.LayoutNode node, DungeonBuilderStudioModel.Socket socket) {
      DungeonBuilderStudioModel.Project project = this.model.project(node.projectId()).orElse(null);
      if (project != null && project.bounds() != null) {
         DungeonBuilderStudioModel.Bounds bounds = project.bounds();
         int localX = socket.position().x() - bounds.min().x();
         int localY = socket.position().y() - bounds.min().y();
         int localZ = socket.position().z() - bounds.min().z();

         return switch (Math.floorMod(node.rotation(), 360)) {
            case 90 -> new DungeonBuilderStudioModel.Point(bounds.depth() - 1 - localZ, localY, localX);
            case 180 -> new DungeonBuilderStudioModel.Point(bounds.width() - 1 - localX, localY, bounds.depth() - 1 - localZ);
            case 270 -> new DungeonBuilderStudioModel.Point(localZ, localY, bounds.width() - 1 - localX);
            default -> new DungeonBuilderStudioModel.Point(localX, localY, localZ);
         };
      } else {
         return null;
      }
   }

   private boolean layoutNodeCollides(DungeonBuilderStudioModel.LayoutNode candidate) {
      DungeonBuilderStudioModel.Project candidateProject = this.model.project(candidate.projectId()).orElse(null);
      if (candidateProject != null && candidateProject.bounds() != null) {
         int candidateHeight = candidateProject.bounds().height();

         for (DungeonBuilderStudioModel.LayoutNode other : this.model.layout().nodes()) {
            if (!other.id().equals(candidate.id())) {
               DungeonBuilderStudioModel.Project otherProject = this.model.project(other.projectId()).orElse(null);
               if (otherProject != null && otherProject.bounds() != null) {
                  boolean overlapX = candidate.x() < other.x() + other.width() && candidate.x() + candidate.width() > other.x();
                  boolean overlapY = candidate.y() < other.y() + otherProject.bounds().height() && candidate.y() + candidateHeight > other.y();
                  boolean overlapZ = candidate.z() < other.z() + other.depth() && candidate.z() + candidate.depth() > other.z();
                  if (overlapX && overlapY && overlapZ) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private DungeonBuilderStudioModel.LayoutNode translateLayoutNodeWithoutCollision(DungeonBuilderStudioModel.LayoutNode start, int deltaX, int deltaZ) {
      int steps = Math.max(Math.abs(deltaX), Math.abs(deltaZ));
      if (steps == 0) {
         return start;
      }

      DungeonBuilderStudioModel.LayoutNode current = start;

      for (int index = 1; index <= steps; index++) {
         int x = start.x() + (int)Math.round(deltaX * ((double)index / steps));
         int z = start.z() + (int)Math.round(deltaZ * ((double)index / steps));
         if (x != current.x() || z != current.z()) {
            DungeonBuilderStudioModel.LayoutNode candidate = new DungeonBuilderStudioModel.LayoutNode(
               start.id(), start.projectId(), start.role(), x, start.y(), z, start.width(), start.depth(), start.rotation(), start.locked()
            );
            if (this.layoutNodeCollides(candidate)) {
               return current;
            }

            current = candidate;
         }
      }

      return current;
   }

   private void replaceLayoutNode(DungeonBuilderStudioModel.LayoutNode replacement) {
      DungeonBuilderStudioModel.LayoutDraft source = this.model.layout();
      List<DungeonBuilderStudioModel.LayoutNode> nodes = new ArrayList<>(source.nodes().size());

      for (DungeonBuilderStudioModel.LayoutNode node : source.nodes()) {
         nodes.add(node.id().equals(replacement.id()) ? replacement : node);
      }

      this.setLocalLayout(this.copyLayoutParts(source, source.enabledProjectIds(), source.roomWeights(), nodes, source.connections()));
   }

   private List<DungeonBuilderStudioModel.RoomWeight> ensureRoomWeight(
      List<DungeonBuilderStudioModel.RoomWeight> source, DungeonBuilderStudioModel.Project project
   ) {
      if (source.stream().anyMatch(weight -> weight.projectId().equals(project.id()))) {
         return source;
      }

      List<DungeonBuilderStudioModel.RoomWeight> result = new ArrayList<>(source);
      result.add(new DungeonBuilderStudioModel.RoomWeight(project.id(), project.weight()));
      return result;
   }

   private int dungeonRoomWeight(DungeonBuilderStudioModel.Project project) {
      return this.model
         .layout()
         .roomWeights()
         .stream()
         .filter(weight -> weight.projectId().equals(project.id()))
         .mapToInt(DungeonBuilderStudioModel.RoomWeight::weight)
         .findFirst()
         .orElse(project.weight());
   }

   private void adjustDungeonRoomWeight(DungeonBuilderStudioModel.Project project, int amount) {
      if (this.model.draft(this.model.dungeonId()).isEmpty()) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Create or open a saved dungeon before changing its room weights.");
      } else {
         DungeonBuilderStudioModel.LayoutDraft source = this.model.layout();
         if (source.mode() == DungeonBuilderStudioModel.LayoutMode.PROCEDURAL
            && project.kind() == DungeonBuilderStudioModel.ProjectKind.MODULE
            && source.enabledProjectIds().contains(project.id())) {
            int next = clamp(this.dungeonRoomWeight(project) + amount, 1, 1000000);
            List<DungeonBuilderStudioModel.RoomWeight> weights = new ArrayList<>(source.roomWeights().size() + 1);
            boolean replaced = false;

            for (DungeonBuilderStudioModel.RoomWeight weight : source.roomWeights()) {
               if (weight.projectId().equals(project.id())) {
                  if (!replaced) {
                     weights.add(new DungeonBuilderStudioModel.RoomWeight(project.id(), next));
                  }

                  replaced = true;
               } else {
                  weights.add(weight);
               }
            }

            if (!replaced) {
               weights.add(new DungeonBuilderStudioModel.RoomWeight(project.id(), next));
            }

            this.setLocalLayout(this.copyLayoutParts(source, source.enabledProjectIds(), weights, source.nodes(), source.connections()));
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Dungeon weight for " + project.id() + " is " + next + ". Press Apply to save.");
         }
      }
   }

   private DungeonBuilderStudioModel.LayoutDraft copyLayoutParts(
      DungeonBuilderStudioModel.LayoutDraft source,
      List<String> enabled,
      List<DungeonBuilderStudioModel.RoomWeight> weights,
      List<DungeonBuilderStudioModel.LayoutNode> nodes,
      List<DungeonBuilderStudioModel.LayoutConnection> connections
   ) {
      return new DungeonBuilderStudioModel.LayoutDraft(
         source.mode(),
         source.topology(),
         source.minRooms(),
         source.maxRooms(),
         source.maxDepth(),
         source.ranks(),
         source.shellBlock(),
         source.shellThickness(),
         enabled,
         weights,
         nodes,
         connections
      );
   }

   private Optional<DungeonBuilderStudioModel.LayoutNode> selectedLayoutNode() {
      return this.model.layout().nodes().stream().filter(node -> node.id().equals(this.selectedLayoutNodeId)).findFirst();
   }

   private Optional<DungeonBuilderStudioModel.Socket> layoutSocket(DungeonBuilderStudioModel.LayoutNode node, String socketId) {
      return this.model.project(node.projectId()).flatMap(project -> project.sockets().stream().filter(socket -> socket.id().equals(socketId)).findFirst());
   }

   private boolean isLayoutSocketUsed(String nodeId, String socketId) {
      return this.model
         .layout()
         .connections()
         .stream()
         .anyMatch(
            connection -> connection.fromNodeId().equals(nodeId) && connection.fromSocketId().equals(socketId)
               || connection.toNodeId().equals(nodeId) && connection.toSocketId().equals(socketId)
         );
   }

   private boolean isLayoutNodeConnected(String nodeId) {
      return this.model.layout().connections().stream().anyMatch(connection -> connection.fromNodeId().equals(nodeId) || connection.toNodeId().equals(nodeId));
   }

   private boolean compatibleLayoutSockets(
      DungeonBuilderStudioModel.LayoutNode fromNode,
      DungeonBuilderStudioModel.Socket from,
      DungeonBuilderStudioModel.LayoutNode toNode,
      DungeonBuilderStudioModel.Socket to
   ) {
      DungeonBuilderStudioModel.Facing fromFacing = rotatedFacing(from.facing(), fromNode.rotation());
      DungeonBuilderStudioModel.Facing toFacing = rotatedFacing(to.facing(), toNode.rotation());
      if (opposite(fromFacing) != toFacing || from.type() != to.type()) {
         return false;
      }

      if (fromFacing != DungeonBuilderStudioModel.Facing.UP && fromFacing != DungeonBuilderStudioModel.Facing.DOWN) {
         return from.openingWidth() == to.openingWidth() && from.openingHeight() == to.openingHeight();
      }

      int fromX = swapsHorizontalAxes(fromNode.rotation()) ? from.openingHeight() : from.openingWidth();
      int fromZ = swapsHorizontalAxes(fromNode.rotation()) ? from.openingWidth() : from.openingHeight();
      int toX = swapsHorizontalAxes(toNode.rotation()) ? to.openingHeight() : to.openingWidth();
      int toZ = swapsHorizontalAxes(toNode.rotation()) ? to.openingWidth() : to.openingHeight();
      return fromX == toX && fromZ == toZ;
   }

   private static boolean swapsHorizontalAxes(int rotation) {
      return Math.floorMod(rotation, 180) == 90;
   }

   private static DungeonBuilderStudioModel.Facing rotatedFacing(DungeonBuilderStudioModel.Facing facing, int rotation) {
      return facing != DungeonBuilderStudioModel.Facing.UP && facing != DungeonBuilderStudioModel.Facing.DOWN
         ? DungeonBuilderStudioModel.Facing.values()[Math.floorMod(facing.ordinal() + Math.floorMod(rotation, 360) / 90, 4)]
         : facing;
   }

   private static DungeonBuilderStudioModel.Facing opposite(DungeonBuilderStudioModel.Facing facing) {
      return switch (facing) {
         case EAST -> DungeonBuilderStudioModel.Facing.WEST;
         case WEST -> DungeonBuilderStudioModel.Facing.EAST;
         case SOUTH -> DungeonBuilderStudioModel.Facing.NORTH;
         case NORTH -> DungeonBuilderStudioModel.Facing.SOUTH;
         case UP -> DungeonBuilderStudioModel.Facing.DOWN;
         case DOWN -> DungeonBuilderStudioModel.Facing.UP;
      };
   }

   private void clearPendingConnection() {
      this.pendingConnectionNodeId = "";
      this.pendingConnectionSocketId = "";
   }

   private void cycleLayoutMode() {
      if (this.hasActiveDungeonDraft()) {
         DungeonBuilderStudioModel.LayoutDraft draft = this.model.layout();
         DungeonBuilderStudioModel.LayoutMode next = draft.mode() == DungeonBuilderStudioModel.LayoutMode.PROCEDURAL
            ? DungeonBuilderStudioModel.LayoutMode.FIXED
            : DungeonBuilderStudioModel.LayoutMode.PROCEDURAL;
         this.setLocalLayout(copyLayout(draft, next, draft.topology(), draft.minRooms(), draft.maxRooms()));
         this.clearPendingConnection();
         this.selectedLayoutSocketId = "";
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Layout mode is now " + next.name() + ". Press Apply to save.");
         this.rebuildWidgets();
      }
   }

   private void cycleTopology() {
      if (this.hasActiveDungeonDraft()) {
         DungeonBuilderStudioModel.LayoutDraft draft = this.model.layout();
         DungeonBuilderStudioModel.Topology next = draft.topology() == DungeonBuilderStudioModel.Topology.LINEAR
            ? DungeonBuilderStudioModel.Topology.BRANCHING
            : DungeonBuilderStudioModel.Topology.LINEAR;
         int minimum = next == DungeonBuilderStudioModel.Topology.BRANCHING ? Math.max(4, draft.minRooms()) : draft.minRooms();
         int maximum = Math.max(minimum, next == DungeonBuilderStudioModel.Topology.BRANCHING ? Math.max(4, draft.maxRooms()) : draft.maxRooms());
         this.setLocalLayout(copyLayout(draft, draft.mode(), next, minimum, maximum));
         if (next == DungeonBuilderStudioModel.Topology.BRANCHING && draft.minRooms() < 4) {
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Branching needs at least 4 rooms; the minimum was raised automatically.");
         }
      }
   }

   private void adjustRoomRange(int minDelta, int maxDelta) {
      if (this.hasActiveDungeonDraft()) {
         DungeonBuilderStudioModel.LayoutDraft draft = this.model.layout();
         int lowerBound = draft.topology() == DungeonBuilderStudioModel.Topology.BRANCHING ? 4 : 3;
         int min = clamp(draft.minRooms() + minDelta, lowerBound, 64);
         int max = clamp(draft.maxRooms() + maxDelta, min, 64);
         this.setLocalLayout(copyLayout(draft, draft.mode(), draft.topology(), min, max));
      }
   }

   private void setLocalLayout(DungeonBuilderStudioModel.LayoutDraft layout) {
      this.model = new DungeonBuilderStudioModel(
         this.model.revision(),
         this.selectedProjectId,
         this.selectedPoolId,
         this.model.dungeonId(),
         this.model.loading(),
         this.model.projects(),
         this.model.pools(),
         layout,
         this.model.simulation(),
         this.model.validation(),
         this.model.notice(),
         this.model.dungeonDrafts()
      );
      this.layoutDirty = true;
   }

   private void submitLayout() {
      if (this.hasActiveDungeonDraft()) {
         this.controller.submit(new DungeonBuilderStudioController.UpdateLayout(this.model.dungeonId(), this.model.layout()));
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Saving layout rules...");
      }
   }

   private void newSimulationSeed() {
      this.simulationSeed = System.nanoTime() ^ (long)this.selectedProjectId.hashCode() << 32;
      if (this.seedBox != null) {
         this.seedBox.setValue(Long.toString(this.simulationSeed));
      }
   }

   private void runSimulation() {
      if (this.layoutDirty) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Press Apply on the Layout tab before running a server preview.");
      } else {
         try {
            this.simulationSeed = Long.parseLong(this.seedBox == null ? Long.toString(this.simulationSeed) : this.seedBox.getValue());
            this.controller.submit(new DungeonBuilderStudioController.RunSimulation(this.model.dungeonId(), this.simulationSeed));
            this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Running the canonical dungeon planner...");
         } catch (NumberFormatException exception) {
            this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Seed must be a whole number from -9223372036854775808 to 9223372036854775807.");
         }
      }
   }

   private void validateDungeon() {
      if (this.layoutDirty) {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Press Apply on the Layout tab before validation.");
      } else {
         this.controller.submit(new DungeonBuilderStudioController.ValidateDungeon(this.model.dungeonId()));
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Validating the current revision...");
      }
   }

   private void exportDungeon() {
      if (this.layoutDirty) {
         this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Unsaved layout changes exist. Press Apply before export.");
      } else if (this.model.validation().hasRun() && this.model.validation().errors() <= 0) {
         this.controller.submit(new DungeonBuilderStudioController.ExportDungeon(this.model.dungeonId()));
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Exporting the validated datapack...");
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.ERROR, "Validate and resolve all blocking errors before export.");
      }
   }

   private Optional<DungeonBuilderStudioModel.Project> selectedProject() {
      return this.model.project(this.selectedProjectId);
   }

   private boolean canUseDungeonCatalog() {
      return this.selectedProject().map(project -> project.kind() == DungeonBuilderStudioModel.ProjectKind.MODULE).orElse(false);
   }

   private boolean hasActiveDungeonDraft() {
      if (this.model.draft(this.model.dungeonId()).isPresent()) {
         return true;
      } else if (this.selectedProject().map(project -> project.kind() == DungeonBuilderStudioModel.ProjectKind.PRESET).orElse(false)) {
         this.feedback(DungeonBuilderStudioModel.Severity.INFO, "Preset exports directly. Configure it with Preset Setup on the Rooms tab.");
         return false;
      } else {
         this.feedback(DungeonBuilderStudioModel.Severity.TODO, "Open Dungeons and create or select a saved dungeon before editing layout rules.");
         return false;
      }
   }

   private Optional<DungeonBuilderStudioModel.MobPool> selectedPool() {
      return this.model.pool(this.selectedPoolId);
   }

   private Optional<DungeonBuilderStudioModel.Socket> selectedSocket() {
      return this.selectedProject().flatMap(project -> project.sockets().stream().filter(socket -> socket.id().equals(this.selectedSocketId)).findFirst());
   }

   private Optional<DungeonBuilderStudioModel.Anchor> selectedAnchor() {
      return this.selectedProject().flatMap(project -> project.anchors().stream().filter(anchor -> anchor.id().equals(this.selectedAnchorId)).findFirst());
   }

   private boolean selectedAnchorIsTrigger() {
      return this.selectedAnchor().map(anchor -> anchor.kind() == DungeonBuilderStudioModel.AnchorKind.TRIGGER).orElse(false);
   }

   private Optional<DungeonBuilderStudioModel.SimRoom> selectedSimulationRoom() {
      return this.model.simulation().rooms().stream().filter(room -> room.id().equals(this.selectedSimRoomId)).findFirst();
   }

   private String inspectorTitle() {
      return switch (this.activeTab) {
         case ROOMS -> "ROOM INSPECTOR";
         case ANCHORS -> "ANCHOR INSPECTOR";
         case POOLS -> "POOL INSPECTOR";
         case LAYOUT -> "LAYOUT RULES";
         case SIMULATE -> "PREVIEW DETAILS";
         case EXPORT -> "EXPORT DETAILS";
      };
   }

   private DungeonBuilderStudioModel.Severity feedbackSeverity() {
      if (!this.localFeedback.isBlank()) {
         return this.localFeedbackSeverity;
      }

      DungeonBuilderStudioModel.Notice notice = this.model.notice();
      return !notice.message().isBlank() ? notice.severity() : this.model.validation().severity();
   }

   private String feedbackText() {
      if (!this.localFeedback.isBlank()) {
         return this.localFeedback;
      }

      if (!this.model.notice().message().isBlank()) {
         return this.model.notice().message();
      }

      if (this.model.loading()) {
         return "Loading workspace";
      }

      return switch (this.model.validation().severity()) {
         case PASS -> "Ready to export";
         case ERROR -> this.model.validation().errors() + " blocking errors";
         case WARNING -> this.model.validation().warnings() + " warnings";
         default -> "Validation not run";
      };
   }

   private void feedback(DungeonBuilderStudioModel.Severity severity, String message) {
      this.localFeedbackSeverity = severity;
      this.localFeedback = message == null ? "" : message;
   }

   private DungeonBuilderStudioScreen.WorkspaceLayout workspaceLayout() {
      int tabRows = this.compact ? 2 : 1;
      int contextY = this.panelY + 21 + tabRows * 18 + 3;
      DungeonBuilderStudioScreen.Rect context = new DungeonBuilderStudioScreen.Rect(this.panelX + 6, contextY, this.panelW - 12, 27);
      int footerH = this.compact ? 38 : 20;
      DungeonBuilderStudioScreen.Rect footer = new DungeonBuilderStudioScreen.Rect(
         this.panelX + 6, this.panelY + this.panelH - footerH - 5, this.panelW - 12, footerH
      );
      int bodyY = context.bottom() + 4;
      DungeonBuilderStudioScreen.Rect body = new DungeonBuilderStudioScreen.Rect(
         this.panelX + 6, bodyY, this.panelW - 12, Math.max(100, footer.y() - bodyY - 4)
      );
      int leftW = this.compact ? Math.min(112, Math.max(94, body.w() / 3)) : 126;
      DungeonBuilderStudioScreen.Rect left = new DungeonBuilderStudioScreen.Rect(body.x(), body.y(), leftW, body.h());
      int mainX = left.right() + 4;
      if (this.compact) {
         DungeonBuilderStudioScreen.Rect toggle = new DungeonBuilderStudioScreen.Rect(mainX, body.y(), body.right() - mainX, 16);
         DungeonBuilderStudioScreen.Rect main = new DungeonBuilderStudioScreen.Rect(mainX, body.y() + 19, body.right() - mainX, body.h() - 19);
         return new DungeonBuilderStudioScreen.WorkspaceLayout(context, body, left, main, DungeonBuilderStudioScreen.Rect.empty(), toggle, footer);
      } else {
         int inspectorW = 140;
         DungeonBuilderStudioScreen.Rect inspector = new DungeonBuilderStudioScreen.Rect(body.right() - inspectorW, body.y(), inspectorW, body.h());
         DungeonBuilderStudioScreen.Rect main = new DungeonBuilderStudioScreen.Rect(mainX, body.y(), inspector.x() - mainX - 4, body.h());
         return new DungeonBuilderStudioScreen.WorkspaceLayout(context, body, left, main, inspector, DungeonBuilderStudioScreen.Rect.empty(), footer);
      }
   }

   private DungeonBuilderStudioScreen.Rect dialogRect() {
      int width = switch (this.activeDialog) {
         case NEW_ROOM -> Math.min(350, this.panelW - 20);
         case NEW_POOL -> Math.min(330, this.panelW - 20);
         case POOL_ENTRY -> Math.min(390, this.panelW - 20);
         case ANCHOR_SETUP -> Math.min(360, this.panelW - 20);
         case PRESET_SETUP -> Math.min(370, this.panelW - 20);
         case DUNGEON_CATALOG -> Math.min(390, this.panelW - 20);
         case NEW_DUNGEON, DELETE_DUNGEON -> Math.min(350, this.panelW - 20);
         case LAYOUT_SETUP -> Math.min(370, this.panelW - 20);
         default -> Math.min(300, this.panelW - 20);
      };

      int height = switch (this.activeDialog) {
         case NEW_ROOM -> 132;
         case NEW_POOL -> 112;
         case POOL_ENTRY -> Math.min(218, this.panelH - 18);
         case ANCHOR_SETUP -> 150;
         case PRESET_SETUP -> 154;
         case DUNGEON_CATALOG -> Math.min(256, this.panelH - 18);
         case NEW_DUNGEON -> 112;
         case DELETE_DUNGEON -> 126;
         case LAYOUT_SETUP -> Math.min(202, this.panelH - 18);
         default -> 100;
      };
      return new DungeonBuilderStudioScreen.Rect(this.panelX + (this.panelW - width) / 2, this.panelY + (this.panelH - height) / 2, width, height);
   }

   private DungeonBuilderStudioScreen.Rect dungeonCatalogListRect(DungeonBuilderStudioScreen.Rect dialog) {
      return new DungeonBuilderStudioScreen.Rect(dialog.x() + 10, dialog.y() + 27, dialog.w() - 20, Math.max(44, dialog.h() - 75));
   }

   private int maxCatalogScroll(DungeonBuilderStudioScreen.Rect dialog) {
      return Math.max(0, this.model.dungeonDrafts().size() * 28 - this.dungeonCatalogListRect(dialog).h());
   }

   private int maxLeftScroll(DungeonBuilderStudioScreen.Rect rect) {
      int rows;
      int rowHeight;
      switch (this.activeTab) {
         case POOLS:
            rows = this.model.pools().size();
            rowHeight = 27;
            break;
         case LAYOUT:
         default:
            rows = this.model.projects().size();
            rowHeight = 27;
            break;
         case SIMULATE:
            rows = this.model.simulation().rooms().size();
            rowHeight = 23;
            break;
         case EXPORT:
            rows = this.model.validation().issues().size();
            rowHeight = 34;
      }

      return Math.max(0, rows * rowHeight - (rect.h() - 20));
   }

   private int maxCenterScroll(DungeonBuilderStudioScreen.Rect rect) {
      return this.selectedPool().map(pool -> Math.max(0, pool.entries().size() * 38 - (rect.h() - 21))).orElse(0);
   }

   private void drawBlueprintGrid(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect) {
      for (int x = rect.x() + 6; x < rect.right(); x += 12) {
         graphics.fill(x, rect.y() + 1, x + 1, rect.bottom() - 1, 539128744);
      }

      for (int y = rect.y() + 6; y < rect.bottom(); y += 12) {
         graphics.fill(rect.x() + 1, y, rect.right() - 1, y + 1, 539128744);
      }
   }

   private void drawSocket(GuiGraphics graphics, DungeonBuilderStudioScreen.RoomMap map, DungeonBuilderStudioModel.Socket socket, boolean selected) {
      int x = map.worldX(socket.position().x());
      int z = map.worldZ(socket.position().z());
      int color = selected ? -11930 : (socket.required() ? -12597505 : -8869956);
      if (socket.facing() != DungeonBuilderStudioModel.Facing.UP && socket.facing() != DungeonBuilderStudioModel.Facing.DOWN) {
         int dx = socket.facing().stepX();
         int dz = socket.facing().stepZ();
         graphics.fill(x - 2, z - 2, x + 3, z + 3, -586738656);
         drawOutline(graphics, x - 2, z - 2, 5, 5, color);
         graphics.fill(Math.min(x, x + dx * 8), Math.min(z, z + dz * 8), Math.max(x, x + dx * 8) + 1, Math.max(z, z + dz * 8) + 1, color);
         int tipX = x + dx * 8;
         int tipZ = z + dz * 8;
         if (dx != 0) {
            graphics.fill(tipX - dx * 2, tipZ - 2, tipX + dx + 1, tipZ + 3, color);
         } else {
            graphics.fill(tipX - 2, tipZ - dz * 2, tipX + 3, tipZ + dz + 1, color);
         }

         if (socket.required()) {
            graphics.drawString(this.font, "!", x + 4, z - 5, color, false);
         }
      } else {
         graphics.fill(x - 4, z - 4, x + 5, z + 5, -586738656);
         drawOutline(graphics, x - 4, z - 4, 9, 9, color);
         graphics.drawString(this.font, socket.facing() == DungeonBuilderStudioModel.Facing.UP ? "U" : "D", x - 2, z - 4, color, false);
      }
   }

   private void drawAnchor(GuiGraphics graphics, DungeonBuilderStudioScreen.RoomMap map, DungeonBuilderStudioModel.Anchor anchor, boolean selected) {
      int x = map.worldX(anchor.position().x());
      int z = map.worldZ(anchor.position().z());
      int color = selected ? -11930 : this.anchorColor(anchor);
      switch (anchor.kind()) {
         case BOSS_SPAWN:
            graphics.fill(x, z - 4, x + 1, z + 5, color);
            graphics.fill(x - 3, z - 1, x + 4, z + 2, color);
            graphics.fill(x - 2, z - 2, x + 3, z + 3, color);
            break;
         case PLAYER_START:
            graphics.fill(x - 4, z, x + 5, z + 1, color);
            graphics.fill(x, z - 4, x + 1, z + 5, color);
            break;
         case TRIGGER:
            drawOutline(graphics, x - 4, z - 4, 9, 9, color);
            break;
         case RETURN_PORTAL:
            drawOutline(graphics, x - 4, z - 4, 9, 9, color);
            drawOutline(graphics, x - 2, z - 2, 5, 5, color);
            break;
         default:
            graphics.fill(x - 3, z - 3, x + 4, z + 4, color);
      }

      graphics.drawString(this.font, anchor.kind().symbol(), x + 5, z - 4, color, false);
   }

   private int anchorColor(DungeonBuilderStudioModel.Anchor anchor) {
      return switch (anchor.kind()) {
         case BOSS_SPAWN -> -41620;
         case PLAYER_START -> -9247233;
         case TRIGGER -> -11930;
         case RETURN_PORTAL -> -4682753;
         case UNASSIGNED, SPAWN_POINT -> -11930;
         case MOB_SPAWN -> -9705824;
         case ELITE_SPAWN -> -19364;
         case LOOT -> -7302;
         case CUSTOM -> -1509633;
      };
   }

   private void renderNodeGraph(
      GuiGraphics graphics,
      DungeonBuilderStudioScreen.Rect rect,
      List<DungeonBuilderStudioModel.LayoutNode> nodes,
      List<DungeonBuilderStudioModel.LayoutConnection> connections,
      int mouseX,
      int mouseY
   ) {
      DungeonBuilderStudioScreen.GraphMap map = this.layoutNodeDragging && this.layoutDragMap != null ? this.layoutDragMap : this.layoutGraphMap(rect, nodes);
      this.enableScissor(graphics, rect);

      for (DungeonBuilderStudioModel.LayoutConnection connection : connections) {
         DungeonBuilderStudioModel.LayoutNode from = nodes.stream().filter(nodex -> nodex.id().equals(connection.fromNodeId())).findFirst().orElse(null);
         DungeonBuilderStudioModel.LayoutNode to = nodes.stream().filter(nodex -> nodex.id().equals(connection.toNodeId())).findFirst().orElse(null);
         DungeonBuilderStudioModel.Socket fromSocket = from == null ? null : this.layoutSocket(from, connection.fromSocketId()).orElse(null);
         DungeonBuilderStudioModel.Socket toSocket = to == null ? null : this.layoutSocket(to, connection.toSocketId()).orElse(null);
         DungeonBuilderStudioModel.Point fromOffset = from != null && fromSocket != null ? this.transformedSocketOffset(from, fromSocket) : null;
         DungeonBuilderStudioModel.Point toOffset = to != null && toSocket != null ? this.transformedSocketOffset(to, toSocket) : null;
         if (from != null && to != null) {
            int fromX = fromOffset == null ? map.centerX(from.x(), from.width()) : map.x(from.x() + fromOffset.x());
            int fromZ = fromOffset == null ? map.centerZ(from.z(), from.depth()) : map.z(from.z() + fromOffset.z());
            int toX = toOffset == null ? map.centerX(to.x(), to.width()) : map.x(to.x() + toOffset.x());
            int toZ = toOffset == null ? map.centerZ(to.z(), to.depth()) : map.z(to.z() + toOffset.z());
            this.drawOrthogonalConnection(graphics, fromX, fromZ, toX, toZ, -14519384);
         }
      }

      for (DungeonBuilderStudioModel.LayoutNode node : nodes) {
         this.drawGraphRoom(
            graphics,
            map,
            node.id(),
            node.projectId(),
            node.role(),
            node.x(),
            node.z(),
            node.width(),
            node.depth(),
            node.id().equals(this.selectedLayoutNodeId),
            mouseX,
            mouseY
         );
      }

      for (DungeonBuilderStudioModel.LayoutNode node : nodes) {
         DungeonBuilderStudioModel.Project project = this.model.project(node.projectId()).orElse(null);
         if (project != null) {
            for (DungeonBuilderStudioModel.Socket socket : project.sockets()) {
               this.drawLayoutSocket(graphics, map, node, socket, mouseX, mouseY);
            }
         }
      }

      graphics.disableScissor();
   }

   private void drawLayoutSocket(
      GuiGraphics graphics,
      DungeonBuilderStudioScreen.GraphMap map,
      DungeonBuilderStudioModel.LayoutNode node,
      DungeonBuilderStudioModel.Socket socket,
      int mouseX,
      int mouseY
   ) {
      DungeonBuilderStudioModel.Point offset = this.transformedSocketOffset(node, socket);
      if (offset != null) {
         int x = map.x(node.x() + offset.x());
         int z = map.z(node.z() + offset.z());
         boolean selected = node.id().equals(this.selectedLayoutNodeId) && socket.id().equals(this.selectedLayoutSocketId);
         boolean pending = node.id().equals(this.pendingConnectionNodeId) && socket.id().equals(this.pendingConnectionSocketId);
         boolean used = this.isLayoutSocketUsed(node.id(), socket.id());
         int color = pending ? -10493044 : (selected ? -11930 : (used ? -10060918 : (socket.required() ? -12597505 : -9971457)));
         graphics.fill(x - 2, z - 2, x + 3, z + 3, color);
         DungeonBuilderStudioModel.Facing facing = rotatedFacing(socket.facing(), node.rotation());
         if (facing != DungeonBuilderStudioModel.Facing.UP && facing != DungeonBuilderStudioModel.Facing.DOWN) {
            int endX = x + facing.stepX() * 5;
            int endZ = z + facing.stepZ() * 5;
            graphics.fill(Math.min(x, endX), Math.min(z, endZ), Math.max(x, endX) + 1, Math.max(z, endZ) + 1, color);
         } else {
            graphics.drawString(this.font, facing == DungeonBuilderStudioModel.Facing.UP ? "U" : "D", x + 3, z - 4, color, false);
         }

         if (mouseX >= x - 4 && mouseX <= x + 4 && mouseY >= z - 4 && mouseY <= z + 4) {
            this.hoverTooltip = List.of(
               Component.literal(node.id() + " / " + socket.id()),
               Component.literal((used ? "CONNECTED | " : "") + facing.name() + " | " + socket.type().name())
            );
         }
      }
   }

   private void renderSimulationGraph(
      GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, DungeonBuilderStudioModel.Simulation simulation, int mouseX, int mouseY
   ) {
      List<DungeonBuilderStudioModel.SimRoom> rooms = simulation.rooms();
      int minX = rooms.stream().mapToInt(DungeonBuilderStudioModel.SimRoom::x).min().orElse(0);
      int minZ = rooms.stream().mapToInt(DungeonBuilderStudioModel.SimRoom::z).min().orElse(0);
      int maxX = rooms.stream().mapToInt(roomx -> roomx.x() + roomx.width()).max().orElse(1);
      int maxZ = rooms.stream().mapToInt(roomx -> roomx.z() + roomx.depth()).max().orElse(1);
      DungeonBuilderStudioScreen.GraphMap map = DungeonBuilderStudioScreen.GraphMap.create(
         rect, minX, minZ, maxX, maxZ, this.canvasZoom, this.canvasPanX, this.canvasPanY
      );
      this.enableScissor(graphics, rect);

      for (DungeonBuilderStudioModel.SimConnection connection : simulation.connections()) {
         DungeonBuilderStudioModel.SimRoom from = rooms.stream().filter(roomx -> roomx.id().equals(connection.fromRoomId())).findFirst().orElse(null);
         DungeonBuilderStudioModel.SimRoom to = rooms.stream().filter(roomx -> roomx.id().equals(connection.toRoomId())).findFirst().orElse(null);
         if (from != null && to != null) {
            this.drawOrthogonalConnection(
               graphics,
               map.centerX(from.x(), from.width()),
               map.centerZ(from.z(), from.depth()),
               map.centerX(to.x(), to.width()),
               map.centerZ(to.z(), to.depth()),
               -12597505
            );
         }
      }

      for (DungeonBuilderStudioModel.SimRoom room : rooms) {
         this.drawGraphRoom(
            graphics,
            map,
            room.id(),
            room.projectId(),
            room.role(),
            room.x(),
            room.z(),
            room.width(),
            room.depth(),
            room.id().equals(this.selectedSimRoomId),
            mouseX,
            mouseY
         );
      }

      graphics.disableScissor();
   }

   private void drawGraphRoom(
      GuiGraphics graphics,
      DungeonBuilderStudioScreen.GraphMap map,
      String id,
      String projectId,
      DungeonBuilderStudioModel.RoomRole role,
      int x,
      int z,
      int width,
      int depth,
      boolean selected,
      int mouseX,
      int mouseY
   ) {
      DungeonBuilderStudioScreen.Rect box = graphRoomRect(map, x, z, width, depth);
      graphics.fill(box.x(), box.y(), box.right(), box.bottom(), selected ? 1883275168 : -803719864);
      drawOutline(graphics, box.x(), box.y(), box.w(), box.h(), selected ? -11930 : roleColor(role));
      String label = this.fit(projectId, Math.max(1, box.w() - 4));
      graphics.drawString(this.font, label, box.x() + 2, box.y() + 2, -1509633, false);
      if (!label.equals(projectId) && box.contains(mouseX, mouseY)) {
         this.hoverTooltip = List.of(Component.literal(projectId), Component.literal(id));
      }
   }

   private static DungeonBuilderStudioScreen.Rect graphRoomRect(DungeonBuilderStudioScreen.GraphMap map, int x, int z, int width, int depth) {
      int x0 = map.x(x);
      int z0 = map.z(z);
      int x1 = Math.max(x0 + 8, map.x(x + width));
      int z1 = Math.max(z0 + 8, map.z(z + depth));
      return new DungeonBuilderStudioScreen.Rect(x0, z0, x1 - x0, z1 - z0);
   }

   private void selectSimulationRoomAt(DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      List<DungeonBuilderStudioModel.SimRoom> rooms = this.model.simulation().rooms();
      if (!rooms.isEmpty()) {
         DungeonBuilderStudioScreen.Rect viewport = rect.inset(7, 18);
         if (viewport.contains(mouseX, mouseY)) {
            int minX = rooms.stream().mapToInt(DungeonBuilderStudioModel.SimRoom::x).min().orElse(0);
            int minZ = rooms.stream().mapToInt(DungeonBuilderStudioModel.SimRoom::z).min().orElse(0);
            int maxX = rooms.stream().mapToInt(roomx -> roomx.x() + roomx.width()).max().orElse(1);
            int maxZ = rooms.stream().mapToInt(roomx -> roomx.z() + roomx.depth()).max().orElse(1);
            DungeonBuilderStudioScreen.GraphMap map = DungeonBuilderStudioScreen.GraphMap.create(
               viewport, minX, minZ, maxX, maxZ, this.canvasZoom, this.canvasPanX, this.canvasPanY
            );

            for (int index = rooms.size() - 1; index >= 0; index--) {
               DungeonBuilderStudioModel.SimRoom room = rooms.get(index);
               if (graphRoomRect(map, room.x(), room.z(), room.width(), room.depth()).contains(mouseX, mouseY)) {
                  this.selectedSimRoomId = room.id();
                  if (this.compact) {
                     this.compactPane = DungeonBuilderStudioScreen.CompactPane.INSPECTOR;
                     this.rebuildWidgets();
                  }

                  return;
               }
            }
         }
      }
   }

   private boolean selectLayoutSocketAt(DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      List<DungeonBuilderStudioModel.LayoutNode> nodes = this.model.layout().nodes();
      if (!nodes.isEmpty() && this.model.layout().mode() == DungeonBuilderStudioModel.LayoutMode.FIXED) {
         DungeonBuilderStudioScreen.Rect viewport = rect.inset(7, 18);
         if (!viewport.contains(mouseX, mouseY)) {
            return false;
         }

         DungeonBuilderStudioScreen.GraphMap map = this.layoutGraphMap(viewport, nodes);
         DungeonBuilderStudioModel.LayoutNode closestNode = null;
         DungeonBuilderStudioModel.Socket closestSocket = null;
         double closestDistance = 36.0;

         for (int nodeIndex = nodes.size() - 1; nodeIndex >= 0; nodeIndex--) {
            DungeonBuilderStudioModel.LayoutNode node = nodes.get(nodeIndex);
            DungeonBuilderStudioModel.Project project = this.model.project(node.projectId()).orElse(null);
            if (project != null) {
               for (DungeonBuilderStudioModel.Socket socket : project.sockets()) {
                  DungeonBuilderStudioModel.Point offset = this.transformedSocketOffset(node, socket);
                  if (offset != null) {
                     double dx = mouseX - map.x(node.x() + offset.x());
                     double dz = mouseY - map.z(node.z() + offset.z());
                     double distance = dx * dx + dz * dz;
                     if (distance <= closestDistance) {
                        closestDistance = distance;
                        closestNode = node;
                        closestSocket = socket;
                     }
                  }
               }
            }
         }

         if (closestNode != null && closestSocket != null) {
            this.selectedLayoutNodeId = closestNode.id();
            this.selectedLayoutSocketId = closestSocket.id();
            this.selectedProjectId = closestNode.projectId();
            if (!closestNode.projectId().equals(this.model.selectedProjectId())) {
               this.controller.submit(new DungeonBuilderStudioController.SelectProject(closestNode.projectId()));
            }

            boolean used = this.isLayoutSocketUsed(closestNode.id(), closestSocket.id());
            this.feedback(
               DungeonBuilderStudioModel.Severity.INFO,
               "Selected endpoint "
                  + closestNode.id()
                  + " / "
                  + closestSocket.id()
                  + (used ? ". Press Unlink to detach it." : ". Press Link to store or connect it.")
            );
            if (this.compact) {
               this.compactPane = DungeonBuilderStudioScreen.CompactPane.INSPECTOR;
            }

            this.rebuildWidgets();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private DungeonBuilderStudioModel.LayoutNode selectLayoutNodeAt(DungeonBuilderStudioScreen.Rect rect, int mouseX, int mouseY) {
      List<DungeonBuilderStudioModel.LayoutNode> nodes = this.model.layout().nodes();
      if (nodes.isEmpty()) {
         return null;
      }

      DungeonBuilderStudioScreen.Rect viewport = rect.inset(7, 18);
      if (!viewport.contains(mouseX, mouseY)) {
         return null;
      }

      DungeonBuilderStudioScreen.GraphMap map = this.layoutGraphMap(viewport, nodes);

      for (int index = nodes.size() - 1; index >= 0; index--) {
         DungeonBuilderStudioModel.LayoutNode node = nodes.get(index);
         if (graphRoomRect(map, node.x(), node.z(), node.width(), node.depth()).contains(mouseX, mouseY)) {
            this.selectedLayoutNodeId = node.id();
            this.selectedLayoutSocketId = "";
            this.selectedProjectId = node.projectId();
            if (!node.projectId().equals(this.model.selectedProjectId())) {
               this.controller.submit(new DungeonBuilderStudioController.SelectProject(node.projectId()));
            }

            if (this.compact) {
               this.compactPane = DungeonBuilderStudioScreen.CompactPane.INSPECTOR;
            }

            this.rebuildWidgets();
            return node;
         }
      }

      return null;
   }

   private DungeonBuilderStudioScreen.GraphMap layoutGraphMap(DungeonBuilderStudioScreen.Rect viewport, List<DungeonBuilderStudioModel.LayoutNode> nodes) {
      int contentMinX = nodes.stream().mapToInt(DungeonBuilderStudioModel.LayoutNode::x).min().orElse(0);
      int contentMinZ = nodes.stream().mapToInt(DungeonBuilderStudioModel.LayoutNode::z).min().orElse(0);
      int contentMaxX = nodes.stream().mapToInt(node -> node.x() + node.width()).max().orElse(1);
      int contentMaxZ = nodes.stream().mapToInt(node -> node.z() + node.depth()).max().orElse(1);
      int minX = Math.min(-32, contentMinX - 6);
      int minZ = Math.min(-32, contentMinZ - 6);
      int maxX = Math.max(32, contentMaxX + 6);
      int maxZ = Math.max(32, contentMaxZ + 6);
      return DungeonBuilderStudioScreen.GraphMap.create(viewport, minX, minZ, maxX, maxZ, this.canvasZoom, this.canvasPanX, this.canvasPanY);
   }

   private void drawOrthogonalConnection(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
      int middleX = (x0 + x1) / 2;
      graphics.fill(Math.min(x0, middleX), y0, Math.max(x0, middleX) + 1, y0 + 2, color);
      graphics.fill(middleX, Math.min(y0, y1), middleX + 2, Math.max(y0, y1) + 1, color);
      graphics.fill(Math.min(middleX, x1), y1, Math.max(middleX, x1) + 1, y1 + 2, color);
   }

   private void drawSectionTitle(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, String label, int count) {
      graphics.drawString(this.font, label, rect.x() + 6, rect.y() + 5, -9971457, false);
      String countLabel = Integer.toString(count);
      graphics.drawString(this.font, countLabel, rect.right() - 6 - this.font.width(countLabel), rect.y() + 5, -7358248, false);
   }

   private void renderEmpty(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, String title, String instruction) {
      int titleY = Math.max(rect.y() + 3, rect.y() + rect.h() / 2 - 17);
      graphics.drawCenteredString(this.font, title, rect.x() + rect.w() / 2, titleY, -1509633);
      this.drawWrappedCentered(graphics, instruction, rect, titleY + 13, -7358248, 4);
   }

   private void drawWrappedCentered(GuiGraphics graphics, String text, DungeonBuilderStudioScreen.Rect rect, int y, int color, int maxLines) {
      List<String> lines = this.wrap(text, Math.max(20, rect.w() - 10));

      for (int index = 0; index < Math.min(maxLines, lines.size()); index++) {
         graphics.drawCenteredString(this.font, lines.get(index), rect.x() + rect.w() / 2, y + index * 10, color);
      }
   }

   private void drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int color, int maxLines) {
      List<String> lines = this.wrap(text, width);

      for (int index = 0; index < Math.min(maxLines, lines.size()); index++) {
         graphics.drawString(this.font, lines.get(index), x, y + index * 10, color, false);
      }
   }

   private List<String> wrap(String text, int width) {
      List<String> result = new ArrayList<>();
      String remaining = text == null ? "" : text.trim();

      while (!remaining.isEmpty()) {
         String line = this.font.plainSubstrByWidth(remaining, width);
         if (line.isEmpty()) {
            break;
         }

         int split = line.length();
         if (split < remaining.length()) {
            int space = line.lastIndexOf(32);
            if (space > 0) {
               split = space;
            }
         }

         result.add(remaining.substring(0, split).trim());
         remaining = remaining.substring(split).trim();
      }

      return result;
   }

   private void drawClipped(GuiGraphics graphics, String text, int x, int y, int width, int color, int mouseX, int mouseY) {
      String safe = text == null ? "" : text;
      String fitted = this.fit(safe, width);
      graphics.drawString(this.font, fitted, x, y, color, false);
      if (!fitted.equals(safe) && mouseX >= x && mouseX < x + width && mouseY >= y - 1 && mouseY < y + 10) {
         this.hoverTooltip = List.of(Component.literal(safe));
      }
   }

   private String fit(String text, int width) {
      if (this.font.width(text) <= width) {
         return text;
      }

      String suffix = "...";
      return this.font.plainSubstrByWidth(text, Math.max(1, width - this.font.width(suffix))) + suffix;
   }

   private void drawPanel(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect, int fill, int border) {
      graphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), fill);
      drawOutline(graphics, rect.x(), rect.y(), rect.w(), rect.h(), border);
   }

   private static void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      if (width > 0 && height > 0) {
         graphics.fill(x, y, x + width, y + 1, color);
         graphics.fill(x, y + height - 1, x + width, y + height, color);
         graphics.fill(x, y, x + 1, y + height, color);
         graphics.fill(x + width - 1, y, x + width, y + height, color);
      }
   }

   private void enableScissor(GuiGraphics graphics, DungeonBuilderStudioScreen.Rect rect) {
      ResponsiveGuiScale.enableScissor(graphics, this.responsiveTransform(), rect.x(), rect.y(), rect.right(), rect.bottom());
   }

   private static int roleColor(DungeonBuilderStudioModel.RoomRole role) {
      return switch (role) {
         case START -> -10364417;
         case BOSS -> -41620;
         case JUNCTION -> -11930;
         case TREASURE -> -6774;
         case STAIR -> -4748289;
         default -> -10493044;
      };
   }

   private static int severityColor(DungeonBuilderStudioModel.Severity severity) {
      return switch (severity) {
         case PASS -> -10493044;
         case ERROR -> -41620;
         case WARNING, TODO -> -11930;
         case INFO -> -9971457;
      };
   }

   private static String statusLabel(DungeonBuilderStudioModel.Severity severity) {
      return switch (severity) {
         case PASS -> "[PASS]";
         case ERROR -> "[ERROR]";
         case WARNING -> "[WARN]";
         case TODO -> "[TODO]";
         case INFO -> "[INFO]";
      };
   }

   private static String pointText(DungeonBuilderStudioModel.Point point) {
      return point.x() + ", " + point.y() + ", " + point.z();
   }

   private static String rankLabel(Set<String> ranks) {
      return ranks != null && !ranks.isEmpty() && !ranks.containsAll(DUNGEON_RANKS) ? String.join(",", ranks) : "ALL";
   }

   private static String normalizedResourceId(String value) {
      return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
   }

   private static boolean validResourceId(String value) {
      return value != null && value.matches("[a-z0-9_.-]+:[a-z0-9/._-]+");
   }

   private static boolean validDungeonId(String value) {
      return validDatapackResourceId(value, 192);
   }

   private static boolean validDatapackResourceId(String value, int maxLength) {
      if (validResourceId(value) && value.length() <= maxLength) {
         int separator = value.indexOf(58);
         if (!safeFilesystemSegment(value.substring(0, separator))) {
            return false;
         }

         String path = value.substring(separator + 1);
         if (!path.startsWith("/") && !path.endsWith("/")) {
            for (String segment : path.split("/", -1)) {
               if (!safeFilesystemSegment(segment)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean safeFilesystemSegment(String segment) {
      return segment != null && !segment.isBlank() && !segment.equals(".") && !segment.equals("..") && !segment.endsWith(".")
         ? !segment.toLowerCase(Locale.ROOT).matches("(con|prn|aux|nul|com[1-9]|lpt[1-9])(\\..*)?")
         : false;
   }

   private static boolean validResourceDraft(String value) {
      return value == null || value.matches("[a-z0-9_./:-]{0,128}");
   }

   private static boolean validDungeonResourceDraft(String value) {
      return value == null || value.matches("[a-z0-9_./:-]{0,192}");
   }

   private static boolean validRoomProjectDraft(String value) {
      return value == null || value.length() <= 81 && value.matches("[a-z0-9_.:-]*") && value.chars().filter(character -> character == 58).count() <= 1L;
   }

   private static boolean validRoomProjectId(String value) {
      if (value == null) {
         return false;
      } else {
         int separator = value.indexOf(58);
         if (separator > 0 && separator == value.lastIndexOf(58)) {
            String namespace = value.substring(0, separator);
            String name = value.substring(separator + 1);
            return namespace.length() <= 32 && name.length() <= 48 && validProjectIdPart(namespace) && validProjectIdPart(name);
         } else {
            return false;
         }
      }
   }

   private static boolean validProjectIdPart(String value) {
      return value.matches("[a-z0-9][a-z0-9_.-]*") && !value.endsWith(".") && !value.matches("(con|prn|aux|nul|com[1-9]|lpt[1-9])(\\..*)?");
   }

   private static boolean validModIdDraft(String value) {
      return value == null || value.isEmpty() || value.matches("[a-z][a-z0-9_-]{0,63}");
   }

   private static boolean validModId(String value) {
      return value != null && (value.isEmpty() || value.matches("[a-z][a-z0-9_-]{1,63}"));
   }

   private static boolean validEncounterDraft(String value) {
      return value == null || value.isEmpty() || value.matches("[a-z0-9][a-z0-9_.-]{0,63}");
   }

   private static boolean validEncounterId(String value) {
      return value != null && value.matches("[a-z0-9][a-z0-9_.-]{0,63}") && !value.endsWith(".");
   }

   private static boolean validUnsignedIntegerDraft(String value) {
      return value == null || value.matches("[0-9]{0,8}");
   }

   private static boolean validSeedText(String value) {
      return value == null || value.isEmpty() || value.equals("-") || value.matches("-?[0-9]{0,19}");
   }

   private static String preferredProjectId(DungeonBuilderStudioModel model, String requested) {
      if (requested != null && model.project(requested).isPresent()) {
         return requested;
      } else {
         return model.projects().isEmpty() ? "" : model.projects().get(0).id();
      }
   }

   private static String preferredPoolId(DungeonBuilderStudioModel model, String requested) {
      if (requested != null && model.pool(requested).isPresent()) {
         return requested;
      } else {
         return model.pools().isEmpty() ? "" : model.pools().get(0).id();
      }
   }

   private static String preferredDraftId(DungeonBuilderStudioModel model, String requested) {
      if (requested != null && model.draft(requested).isPresent()) {
         return requested;
      } else if (model.draft(model.dungeonId()).isPresent()) {
         return model.dungeonId();
      } else {
         return model.dungeonDrafts().isEmpty() ? "" : model.dungeonDrafts().get(0).id();
      }
   }

   private static DungeonBuilderStudioModel.PoolEntry copyEntryWeight(DungeonBuilderStudioModel.PoolEntry entry, int weight) {
      return new DungeonBuilderStudioModel.PoolEntry(
         entry.selectorKind(), entry.selectorId(), Math.max(1, weight), entry.requiredMod(), entry.eligibleLevel(), entry.spawnLevel(), entry.baseXp()
      );
   }

   private static DungeonBuilderStudioModel.LayoutDraft copyLayout(
      DungeonBuilderStudioModel.LayoutDraft source,
      DungeonBuilderStudioModel.LayoutMode mode,
      DungeonBuilderStudioModel.Topology topology,
      int minRooms,
      int maxRooms
   ) {
      return new DungeonBuilderStudioModel.LayoutDraft(
         mode,
         topology,
         minRooms,
         maxRooms,
         source.maxDepth(),
         source.ranks(),
         source.shellBlock(),
         source.shellThickness(),
         source.enabledProjectIds(),
         source.roomWeights(),
         source.nodes(),
         source.connections()
      );
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private enum CompactPane {
      CANVAS,
      INSPECTOR;
   }

   private enum Dialog {
      NONE,
      NEW_ROOM,
      NEW_POOL,
      POOL_ENTRY,
      ANCHOR_SETUP,
      PRESET_SETUP,
      DUNGEON_CATALOG,
      NEW_DUNGEON,
      DELETE_DUNGEON,
      LAYOUT_SETUP;
   }

   private record GraphMap(DungeonBuilderStudioScreen.Rect rect, int minX, int minZ, int maxX, int maxZ, double scale, double offsetX, double offsetZ) {
      private static DungeonBuilderStudioScreen.GraphMap create(
         DungeonBuilderStudioScreen.Rect rect, int minX, int minZ, int maxX, int maxZ, double zoom, double panX, double panZ
      ) {
         double fit = Math.max(0.2, Math.min((rect.w() - 12.0) / Math.max(1, maxX - minX), (rect.h() - 12.0) / Math.max(1, maxZ - minZ)));
         double scale = fit * DungeonBuilderStudioScreen.clamp(zoom, 0.5, 4.0);
         double offsetX = rect.x() + rect.w() / 2.0 + panX - (minX + maxX) / 2.0 * scale;
         double offsetZ = rect.y() + rect.h() / 2.0 + panZ - (minZ + maxZ) / 2.0 * scale;
         return new DungeonBuilderStudioScreen.GraphMap(rect, minX, minZ, maxX, maxZ, scale, offsetX, offsetZ);
      }

      private int x(int worldX) {
         return (int)Math.round(this.offsetX + worldX * this.scale);
      }

      private int z(int worldZ) {
         return (int)Math.round(this.offsetZ + worldZ * this.scale);
      }

      private int centerX(int x, int width) {
         return this.x(x + width / 2);
      }

      private int centerZ(int z, int depth) {
         return this.z(z + depth / 2);
      }
   }

   private record Rect(int x, int y, int w, int h) {
      private static DungeonBuilderStudioScreen.Rect empty() {
         return new DungeonBuilderStudioScreen.Rect(0, 0, 0, 0);
      }

      private int right() {
         return this.x + this.w;
      }

      private int bottom() {
         return this.y + this.h;
      }

      private boolean contains(double px, double py) {
         return px >= this.x && px < this.right() && py >= this.y && py < this.bottom();
      }

      private DungeonBuilderStudioScreen.Rect inset(int horizontal, int vertical) {
         return new DungeonBuilderStudioScreen.Rect(
            this.x + horizontal, this.y + vertical, Math.max(1, this.w - horizontal * 2), Math.max(1, this.h - vertical * 2)
         );
      }
   }

   private record RoomMap(DungeonBuilderStudioModel.Bounds bounds, DungeonBuilderStudioScreen.Rect rect, double scale, double centerX, double centerZ) {
      private static DungeonBuilderStudioScreen.RoomMap forProject(
         DungeonBuilderStudioModel.Project project, DungeonBuilderStudioScreen.Rect rect, double zoom, double panX, double panY
      ) {
         DungeonBuilderStudioModel.Bounds bounds = project.bounds();
         double fit = Math.min((rect.w() - 12.0) / Math.max(1, bounds.width()), (rect.h() - 12.0) / Math.max(1, bounds.depth()));
         fit = Math.max(1.0, fit) * zoom;
         return new DungeonBuilderStudioScreen.RoomMap(bounds, rect, fit, rect.x() + rect.w() / 2.0 + panX, rect.y() + rect.h() / 2.0 + panY);
      }

      private int worldX(int worldX) {
         double local = worldX - this.bounds.min().x() + 0.5 - this.bounds.width() / 2.0;
         return (int)Math.round(this.centerX + local * this.scale);
      }

      private int worldZ(int worldZ) {
         double local = worldZ - this.bounds.min().z() + 0.5 - this.bounds.depth() / 2.0;
         return (int)Math.round(this.centerZ + local * this.scale);
      }

      private int localX(int localX) {
         double local = localX - this.bounds.width() / 2.0;
         return (int)Math.round(this.centerX + local * this.scale);
      }

      private int localZ(int localZ) {
         double local = localZ - this.bounds.depth() / 2.0;
         return (int)Math.round(this.centerZ + local * this.scale);
      }

      private int minScreenX() {
         return this.localX(0);
      }

      private int maxScreenX() {
         return this.localX(this.bounds.width());
      }

      private int minScreenZ() {
         return this.localZ(0);
      }

      private int maxScreenZ() {
         return this.localZ(this.bounds.depth());
      }
   }

   private static final class StudioTabButton extends SystemScreen.SystemButton {
      private final BooleanSupplier selected;

      private StudioTabButton(int x, int y, int width, int height, Component label, BooleanSupplier selected, Runnable onPress) {
         super(x, y, width, height, label, button -> onPress.run());
         this.selected = selected;
      }

      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         super.renderWidget(graphics, mouseX, mouseY, partialTicks);
         if (this.selected.getAsBoolean()) {
            graphics.fill(this.getX() + 1, this.getY() + this.height - 2, this.getX() + this.width - 1, this.getY() + this.height, -12597505);
            graphics.fill(this.getX() + 2, this.getY() + 1, this.getX() + this.width - 2, this.getY() + this.height - 2, 524273407);
         }
      }
   }

   private enum Tab {
      ROOMS,
      ANCHORS,
      POOLS,
      LAYOUT,
      SIMULATE,
      EXPORT;
   }

   private record WorkspaceLayout(
      DungeonBuilderStudioScreen.Rect context,
      DungeonBuilderStudioScreen.Rect body,
      DungeonBuilderStudioScreen.Rect left,
      DungeonBuilderStudioScreen.Rect main,
      DungeonBuilderStudioScreen.Rect inspector,
      DungeonBuilderStudioScreen.Rect mainToggle,
      DungeonBuilderStudioScreen.Rect footer
   ) {
   }
}
