package dev.eness.sololevelingfinal.core.dungeon.runtime;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class DungeonTemplatePlacer {
   private static final Mirror MIRROR = Mirror.NONE;
   private static final BlockPos ROTATION_PIVOT = BlockPos.ZERO;
   private static final int PLACEMENT_FLAGS = 2;

   private DungeonTemplatePlacer() {
   }

   public static DungeonTemplatePlacer.PreparationResult prepare(
      ServerLevel level, ResourceLocation templateId, BlockPos desiredWorldMinimum, Rotation rotation
   ) {
      if (level != null && templateId != null && desiredWorldMinimum != null && rotation != null) {
         try {
            StructureTemplate template = level.getStructureManager().get(templateId).orElse(null);
            if (template == null) {
               return DungeonTemplatePlacer.PreparationResult.failure(
                  DungeonTemplatePlacer.ErrorCode.MISSING_TEMPLATE, "Missing structure template " + templateId + "."
               );
            }

            Vec3i sourceSize = template.getSize();
            if (!hasVolume(sourceSize)) {
               return DungeonTemplatePlacer.PreparationResult.failure(
                  DungeonTemplatePlacer.ErrorCode.EMPTY_TEMPLATE, "Structure template " + templateId + " has no placeable volume."
               );
            }

            Vec3i rotatedSize = template.getSize(rotation);
            BlockPos placementOrigin = template.getZeroPositionWithTransform(desiredWorldMinimum, MIRROR, rotation);
            StructurePlaceSettings settings = settings(rotation);
            BoundingBox minecraftBounds = template.getBoundingBox(settings, placementOrigin);
            DungeonTemplatePlacer.WorldBounds worldBounds = DungeonTemplatePlacer.WorldBounds.from(minecraftBounds);
            return worldBounds.min().equals(desiredWorldMinimum)
                  && worldBounds.sizeX() == rotatedSize.getX()
                  && worldBounds.sizeY() == rotatedSize.getY()
                  && worldBounds.sizeZ() == rotatedSize.getZ()
               ? DungeonTemplatePlacer.PreparationResult.success(
                  new DungeonTemplatePlacer.PreparedTemplate(
                     templateId, template, sourceSize, rotatedSize, rotation, desiredWorldMinimum, placementOrigin, worldBounds
                  )
               )
               : DungeonTemplatePlacer.PreparationResult.failure(
                  DungeonTemplatePlacer.ErrorCode.TRANSFORM_MISMATCH, "Minecraft returned inconsistent rotated bounds for " + templateId + "."
               );
         } catch (RuntimeException exception) {
            return DungeonTemplatePlacer.PreparationResult.failure(
               DungeonTemplatePlacer.ErrorCode.LOAD_FAILED, "Could not load structure template " + templateId + ": " + exceptionMessage(exception)
            );
         }
      } else {
         return DungeonTemplatePlacer.PreparationResult.failure(
            DungeonTemplatePlacer.ErrorCode.INVALID_ARGUMENT, "Level, template id, desired world minimum, and rotation are required."
         );
      }
   }

   public static DungeonTemplatePlacer.PlacementResult place(ServerLevel level, ResourceLocation templateId, BlockPos desiredWorldMinimum, Rotation rotation) {
      DungeonTemplatePlacer.PreparationResult preparation = prepare(level, templateId, desiredWorldMinimum, rotation);
      return !preparation.success()
         ? DungeonTemplatePlacer.PlacementResult.failure(preparation.error(), preparation.message(), null)
         : place(level, preparation.template());
   }

   public static DungeonTemplatePlacer.PlacementResult place(ServerLevel level, @Nullable DungeonTemplatePlacer.PreparedTemplate prepared) {
      if (level != null && prepared != null) {
         try {
            boolean placed = prepared.template
               .placeInWorld(level, prepared.placementOrigin, prepared.placementOrigin, settings(prepared.rotation), level.random, 2);
            return !placed
               ? DungeonTemplatePlacer.PlacementResult.failure(
                  DungeonTemplatePlacer.ErrorCode.PLACEMENT_REJECTED,
                  "Minecraft rejected placement of structure template " + prepared.templateId + ".",
                  prepared
               )
               : DungeonTemplatePlacer.PlacementResult.success(prepared);
         } catch (RuntimeException exception) {
            return DungeonTemplatePlacer.PlacementResult.failure(
               DungeonTemplatePlacer.ErrorCode.PLACEMENT_FAILED,
               "Could not place structure template " + prepared.templateId + ": " + exceptionMessage(exception),
               prepared
            );
         }
      } else {
         return DungeonTemplatePlacer.PlacementResult.failure(
            DungeonTemplatePlacer.ErrorCode.INVALID_ARGUMENT, "A level and prepared template are required.", prepared
         );
      }
   }

   private static StructurePlaceSettings settings(Rotation rotation) {
      return new StructurePlaceSettings().setMirror(MIRROR).setRotation(rotation).setRotationPivot(ROTATION_PIVOT).setIgnoreEntities(true);
   }

   private static boolean hasVolume(Vec3i size) {
      return size.getX() > 0 && size.getY() > 0 && size.getZ() > 0;
   }

   private static String exceptionMessage(RuntimeException exception) {
      String message = exception.getMessage();
      return message != null && !message.isBlank() ? message : exception.getClass().getSimpleName();
   }

   public enum ErrorCode {
      NONE,
      INVALID_ARGUMENT,
      MISSING_TEMPLATE,
      EMPTY_TEMPLATE,
      LOAD_FAILED,
      TRANSFORM_MISMATCH,
      PLACEMENT_REJECTED,
      PLACEMENT_FAILED;
   }

   public record PlacementResult(
      boolean success, DungeonTemplatePlacer.ErrorCode error, String message, @Nullable DungeonTemplatePlacer.PreparedTemplate template
   ) {
      private static DungeonTemplatePlacer.PlacementResult success(DungeonTemplatePlacer.PreparedTemplate template) {
         return new DungeonTemplatePlacer.PlacementResult(true, DungeonTemplatePlacer.ErrorCode.NONE, "Placed " + template.templateId + ".", template);
      }

      private static DungeonTemplatePlacer.PlacementResult failure(
         DungeonTemplatePlacer.ErrorCode error, String message, @Nullable DungeonTemplatePlacer.PreparedTemplate template
      ) {
         return new DungeonTemplatePlacer.PlacementResult(false, error, message, template);
      }
   }

   public record PreparationResult(
      boolean success, DungeonTemplatePlacer.ErrorCode error, String message, @Nullable DungeonTemplatePlacer.PreparedTemplate template
   ) {
      private static DungeonTemplatePlacer.PreparationResult success(DungeonTemplatePlacer.PreparedTemplate template) {
         return new DungeonTemplatePlacer.PreparationResult(true, DungeonTemplatePlacer.ErrorCode.NONE, "Prepared " + template.templateId + ".", template);
      }

      private static DungeonTemplatePlacer.PreparationResult failure(DungeonTemplatePlacer.ErrorCode error, String message) {
         return new DungeonTemplatePlacer.PreparationResult(false, error, message, null);
      }
   }

   public static final class PreparedTemplate {
      private final ResourceLocation templateId;
      private final StructureTemplate template;
      private final Vec3i sourceSize;
      private final Vec3i rotatedSize;
      private final Rotation rotation;
      private final BlockPos desiredWorldMinimum;
      private final BlockPos placementOrigin;
      private final DungeonTemplatePlacer.WorldBounds worldBounds;
      @Nullable
      private List<BlockPos> transformedBlockEntityPositions;

      private PreparedTemplate(
         ResourceLocation templateId,
         StructureTemplate template,
         Vec3i sourceSize,
         Vec3i rotatedSize,
         Rotation rotation,
         BlockPos desiredWorldMinimum,
         BlockPos placementOrigin,
         DungeonTemplatePlacer.WorldBounds worldBounds
      ) {
         this.templateId = templateId;
         this.template = template;
         this.sourceSize = sourceSize;
         this.rotatedSize = rotatedSize;
         this.rotation = rotation;
         this.desiredWorldMinimum = desiredWorldMinimum;
         this.placementOrigin = placementOrigin;
         this.worldBounds = worldBounds;
      }

      public ResourceLocation templateId() {
         return this.templateId;
      }

      public Vec3i sourceSize() {
         return this.sourceSize;
      }

      public Vec3i rotatedSize() {
         return this.rotatedSize;
      }

      public Rotation rotation() {
         return this.rotation;
      }

      public BlockPos desiredWorldMinimum() {
         return this.desiredWorldMinimum;
      }

      public BlockPos placementOrigin() {
         return this.placementOrigin;
      }

      public DungeonTemplatePlacer.WorldBounds worldBounds() {
         return this.worldBounds;
      }

      public List<BlockPos> transformedBlockEntityPositions() {
         if (this.transformedBlockEntityPositions != null) {
            return this.transformedBlockEntityPositions;
         }

         CompoundTag saved = this.template.save(new CompoundTag());
         ListTag blocks = saved.getList("blocks", 10);
         List<BlockPos> result = new ArrayList<>();

         for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            if (block.contains("nbt", 10)) {
               ListTag position = block.getList("pos", 3);
               if (position.size() >= 3) {
                  result.add(this.transformRelative(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2))));
               }
            }
         }

         this.transformedBlockEntityPositions = List.copyOf(result);
         return this.transformedBlockEntityPositions;
      }

      public boolean containsRelative(BlockPos relative) {
         return relative != null
            && relative.getX() >= 0
            && relative.getX() < this.sourceSize.getX()
            && relative.getY() >= 0
            && relative.getY() < this.sourceSize.getY()
            && relative.getZ() >= 0
            && relative.getZ() < this.sourceSize.getZ();
      }

      public BlockPos transformRelative(BlockPos relative) {
         if (relative == null) {
            throw new IllegalArgumentException("Relative position is required.");
         }

         BlockPos rotated = StructureTemplate.transform(relative, DungeonTemplatePlacer.MIRROR, this.rotation, DungeonTemplatePlacer.ROTATION_PIVOT);
         return this.placementOrigin.offset(rotated);
      }

      public Direction transformDirection(Direction sourceDirection) {
         if (sourceDirection == null) {
            throw new IllegalArgumentException("Source direction is required.");
         } else {
            return this.rotation.rotate(sourceDirection);
         }
      }

      public DungeonTemplatePlacer.WorldBounds transformRelativeBounds(DungeonTemplatePlacer.RelativeBounds relativeBounds) {
         if (relativeBounds == null) {
            throw new IllegalArgumentException("Relative bounds are required.");
         } else {
            return new DungeonTemplatePlacer.WorldBounds(this.transformRelative(relativeBounds.min()), this.transformRelative(relativeBounds.max()));
         }
      }

      public DungeonTemplatePlacer.TransformedSocket transformSocket(DungeonTemplatePlacer.RelativeBounds opening, Direction sourceFacing) {
         return new DungeonTemplatePlacer.TransformedSocket(this.transformRelativeBounds(opening), this.transformDirection(sourceFacing));
      }
   }

   public record RelativeBounds(BlockPos min, BlockPos max) {
      public RelativeBounds(BlockPos min, BlockPos max) {
         if (min != null && max != null) {
            BlockPos normalizedMin = new BlockPos(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()));
            BlockPos normalizedMax = new BlockPos(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()));
            min = normalizedMin;
            max = normalizedMax;
            this.min = min;
            this.max = max;
         } else {
            throw new IllegalArgumentException("Relative bounds require two positions.");
         }
      }
   }

   public record TransformedSocket(DungeonTemplatePlacer.WorldBounds opening, Direction facing) {
   }

   public record WorldBounds(BlockPos min, BlockPos max) {
      public WorldBounds(BlockPos min, BlockPos max) {
         if (min != null && max != null) {
            BlockPos normalizedMin = new BlockPos(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()));
            BlockPos normalizedMax = new BlockPos(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()));
            min = normalizedMin;
            max = normalizedMax;
            this.min = min;
            this.max = max;
         } else {
            throw new IllegalArgumentException("World bounds require two positions.");
         }
      }

      private static DungeonTemplatePlacer.WorldBounds from(BoundingBox bounds) {
         return new DungeonTemplatePlacer.WorldBounds(
            new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ()), new BlockPos(bounds.maxX(), bounds.maxY(), bounds.maxZ())
         );
      }

      public int sizeX() {
         return this.max.getX() - this.min.getX() + 1;
      }

      public int sizeY() {
         return this.max.getY() - this.min.getY() + 1;
      }

      public int sizeZ() {
         return this.max.getZ() - this.min.getZ() + 1;
      }

      public boolean intersects(DungeonTemplatePlacer.WorldBounds other) {
         return this.max.getX() >= other.min.getX()
            && this.min.getX() <= other.max.getX()
            && this.max.getY() >= other.min.getY()
            && this.min.getY() <= other.max.getY()
            && this.max.getZ() >= other.min.getZ()
            && this.min.getZ() <= other.max.getZ();
      }
   }
}
