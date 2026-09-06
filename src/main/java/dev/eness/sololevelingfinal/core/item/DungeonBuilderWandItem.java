package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderPreview;
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderProjectData;
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderRoomStore;
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderStudioService;
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderTool;
import dev.eness.sololevelingfinal.core.util.DungeonBuilderMode;

public final class DungeonBuilderWandItem extends Item {
   private static final String MODE_TAG = "slr_builder_mode";
   private final DungeonBuilderTool tool;

   public DungeonBuilderWandItem(DungeonBuilderTool tool) {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
      this.tool = tool;
   }

   public DungeonBuilderTool tool() {
      return this.tool;
   }

   public String currentMode(ItemStack stack) {
      return this.mode(stack);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Player rawPlayer = context.getPlayer();
      Level level = context.getLevel();
      if (rawPlayer == null) {
         return InteractionResult.PASS;
      }

      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (rawPlayer instanceof ServerPlayer player && canEdit(player)) {
         ItemStack stack = context.getItemInHand();
         DungeonBuilderProjectData data = DungeonBuilderProjectData.get(player.serverLevel());
         DungeonBuilderProjectData.Project project = data.project(player);
         String mode = this.mode(stack);
         String result = this.applyBlockAction(player, data, project, mode, context.getClickedPos(), context.getClickedFace());
         data.setDirty();
         if (result != null) {
            message(player, result, this.tool.color());
         }

         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.FAIL;
      }
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player rawPlayer, InteractionHand hand) {
      ItemStack stack = rawPlayer.getItemInHand(hand);
      if (level.isClientSide()) {
         return InteractionResultHolder.success(stack);
      } else if (!(rawPlayer instanceof ServerPlayer player && canEdit(player))) {
         return InteractionResultHolder.fail(stack);
      } else {
         if (rawPlayer.isShiftKeyDown()) {
            this.cycleMode(player, stack);
            return InteractionResultHolder.consume(stack);
         }

         DungeonBuilderProjectData data = DungeonBuilderProjectData.get(player.serverLevel());
         DungeonBuilderProjectData.Project project = data.project(player);
         if (this.tool == DungeonBuilderTool.BUILDER) {
            runBuilderAction(player, data, project, this.mode(stack), null);
         } else {
            message(player, project.summary() + " | Mode: " + display(this.mode(stack)), this.tool.color());
         }

         return InteractionResultHolder.consume(stack);
      }
   }

   @Nullable
   private String applyBlockAction(
      ServerPlayer player, DungeonBuilderProjectData data, DungeonBuilderProjectData.Project project, String mode, BlockPos clickedPos, Direction clickedFace
   ) {
      return switch (this.tool) {
         case SURVEYOR -> {
            switch (mode) {
               case "structure_bounds":
                  yield project.selectStructureCorner(clickedPos);
               case "room_bounds":
                  yield project.selectRegionCorner("room", clickedPos);
               default:
                  yield null;
            }
         }
         case SOCKET -> {
            boolean required = mode.startsWith("required_");
            String socketType = mode.contains("stair_") ? "stair" : "corridor";
            Direction facing = mode.endsWith("stair_up") ? Direction.UP : (mode.endsWith("stair_down") ? Direction.DOWN : clickedFace);
            yield socketType.equals("corridor") && facing.getAxis().isVertical()
               ? "Corridor sockets must face horizontally. Click a vertical guide-block face."
               : project.selectSocketCorner(socketType, required, clickedPos, facing);
         }
         case ENCOUNTER -> mode.equals("trigger_region")
            ? project.selectRegionCorner("trigger_region", clickedPos)
            : project.addMarker(mode, clickedPos.relative(clickedFace));
         case FEATURE -> project.addMarker(mode, mode.equals("loot") ? clickedPos : clickedPos.relative(clickedFace));
         case BUILDER -> {
            runBuilderAction(player, data, project, mode, clickedPos);
            yield null;
         }
      };
   }

   private static void runBuilderAction(
      ServerPlayer player, DungeonBuilderProjectData data, DungeonBuilderProjectData.Project project, String mode, @Nullable BlockPos target
   ) {
      switch (mode) {
         case "studio":
            DungeonBuilderStudioService.requestOpen(player);
            break;
         case "capture":
            DungeonBuilderRoomStore.CaptureResult result = DungeonBuilderRoomStore.capture(player, project, data.revision(player) + 1L);
            player.sendSystemMessage(Component.literal(result.message()).withStyle(result.success() ? ChatFormatting.GREEN : ChatFormatting.RED));
            break;
         case "preview":
            DungeonBuilderPreview.show(player, project);
            message(
               player,
               "Preview shown for 2 seconds: cyan bounds, purple regions, orange sockets/facing rays, red encounters, gold features.",
               ChatFormatting.GREEN
            );
            break;
         case "validate":
            sendValidation(player, project);
            break;
         case "undo":
            message(player, project.undoLast(), ChatFormatting.GREEN);
            data.setDirty();
            break;
         case "erase":
            if (target == null) {
               message(player, "Right-click a block within 8 blocks of the marker, socket, or region to erase.", ChatFormatting.YELLOW);
            } else {
               message(player, project.eraseNearest(target), ChatFormatting.GREEN);
               data.setDirty();
            }
            break;
         case "export":
            DungeonBuilderStudioService.requestOpen(player);
            message(
               player,
               "Use Validate and Export in the Studio so the selected layout, snapshots, and authored mob pools are compiled together.",
               ChatFormatting.GREEN
            );
            break;
         default:
            message(player, project.summary(), ChatFormatting.GREEN);
      }
   }

   public static void sendValidation(ServerPlayer player, DungeonBuilderProjectData.Project project) {
      List<DungeonBuilderProjectData.Issue> issues = project.validate();
      long errors = issues.stream().filter(issuex -> issuex.severity() == DungeonBuilderProjectData.Severity.ERROR).count();
      long warnings = issues.size() - errors;
      player.sendSystemMessage(
         Component.literal("Dungeon Builder validation: " + errors + " error(s), " + warnings + " warning(s).")
            .withStyle(errors == 0L ? ChatFormatting.GREEN : ChatFormatting.RED)
      );
      if (issues.isEmpty()) {
         player.sendSystemMessage(Component.literal("Ready to export.").withStyle(ChatFormatting.GREEN));
      } else {
         for (int index = 0; index < Math.min(issues.size(), 12); index++) {
            DungeonBuilderProjectData.Issue issue = issues.get(index);
            ChatFormatting color = issue.severity() == DungeonBuilderProjectData.Severity.ERROR ? ChatFormatting.RED : ChatFormatting.YELLOW;
            player.sendSystemMessage(
               Component.literal((issue.severity() == DungeonBuilderProjectData.Severity.ERROR ? "[Error] " : "[Warning] ") + issue.message()).withStyle(color)
            );
         }

         if (issues.size() > 12) {
            player.sendSystemMessage(Component.literal("...and " + (issues.size() - 12) + " more issue(s).").withStyle(ChatFormatting.GRAY));
         }
      }
   }

   private void cycleMode(ServerPlayer player, ItemStack stack) {
      int current = this.modeIndex(stack);
      int next = this.tool.nextMode(current);
      stack.getOrCreateTag().putInt("slr_builder_mode", next);
      message(player, this.toolName() + " mode: " + display(this.tool.mode(next)), this.tool.color());
   }

   private String mode(ItemStack stack) {
      return this.tool.mode(this.modeIndex(stack));
   }

   private int modeIndex(ItemStack stack) {
      return stack.hasTag() ? Math.floorMod(stack.getTag().getInt("slr_builder_mode"), this.tool.modes().size()) : 0;
   }

   private static boolean canEdit(ServerPlayer player) {
      if (DungeonBuilderMode.isActive(player.level()) && DungeonBuilderMode.isBuilderWorld(player.getServer())) {
         boolean authorized = player.isCreative() && (player.hasPermissions(2) || player.getServer().isSingleplayerOwner(player.getGameProfile()));
         if (!authorized) {
            message(player, "Creative mode and operator permission are required to edit or export dungeons.", ChatFormatting.RED);
         }

         return authorized;
      } else {
         message(player, "Dungeon Builder wands only work in a Dungeon Builder world.", ChatFormatting.RED);
         return false;
      }
   }

   private String toolName() {
      return display(this.tool.name().toLowerCase()) + " Wand";
   }

   private static String display(String id) {
      String value = id.replace('_', ' ');
      return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
   }

   private static void message(ServerPlayer player, String text, ChatFormatting color) {
      player.displayClientMessage(Component.literal(text).withStyle(color), true);
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return true;
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, level, tooltip, flag);
      tooltip.add(
         Component.translatable("tooltip.sololeveling.dungeon_builder." + this.tool.name().toLowerCase() + ".description").withStyle(this.tool.color())
      );
      tooltip.add(
         Component.translatable(
               "tooltip.sololeveling.dungeon_builder.mode", Component.translatable("tooltip.sololeveling.dungeon_builder.mode." + this.mode(stack))
            )
            .withStyle(ChatFormatting.WHITE)
      );
      tooltip.add(Component.translatable("tooltip.sololeveling.dungeon_builder.controls.primary").withStyle(ChatFormatting.GRAY));
      tooltip.add(Component.translatable("tooltip.sololeveling.dungeon_builder.controls.cycle").withStyle(ChatFormatting.DARK_GRAY));
      if (level != null && !DungeonBuilderMode.isActive(level)) {
         tooltip.add(Component.translatable("tooltip.sololeveling.dungeon_builder.builder_world_only").withStyle(ChatFormatting.RED));
      }
   }
}
