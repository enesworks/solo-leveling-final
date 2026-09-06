package dev.eness.sololevelingfinal.core.dungeon.runtime.layout;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataTypes;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonTemplatePlacer;

public final class DungeonRoomGeometry {
   private static final Mirror MIRROR = Mirror.NONE;
   private static final BlockPos PIVOT = BlockPos.ZERO;
   private final ResourceLocation templateId;
   private final Vec3i sourceSize;
   private final Vec3i rotatedSize;
   private final Rotation rotation;
   private final BlockPos desiredWorldMinimum;
   private final BlockPos placementOrigin;
   private final DungeonTemplatePlacer.WorldBounds worldBounds;
   @Nullable
   private final DungeonTemplatePlacer.PreparedTemplate runtimeTemplate;

   private DungeonRoomGeometry(
      ResourceLocation templateId,
      Vec3i sourceSize,
      Vec3i rotatedSize,
      Rotation rotation,
      BlockPos desiredWorldMinimum,
      BlockPos placementOrigin,
      DungeonTemplatePlacer.WorldBounds worldBounds,
      @Nullable DungeonTemplatePlacer.PreparedTemplate runtimeTemplate
   ) {
      this.templateId = templateId;
      this.sourceSize = sourceSize;
      this.rotatedSize = rotatedSize;
      this.rotation = rotation;
      this.desiredWorldMinimum = desiredWorldMinimum;
      this.placementOrigin = placementOrigin;
      this.worldBounds = worldBounds;
      this.runtimeTemplate = runtimeTemplate;
   }

   public static DungeonRoomGeometry loaded(DungeonTemplatePlacer.PreparedTemplate prepared) {
      if (prepared == null) {
         throw new IllegalArgumentException("Prepared template is required.");
      } else {
         return new DungeonRoomGeometry(
            prepared.templateId(),
            prepared.sourceSize(),
            prepared.rotatedSize(),
            prepared.rotation(),
            prepared.desiredWorldMinimum(),
            prepared.placementOrigin(),
            prepared.worldBounds(),
            prepared
         );
      }
   }

   public static DungeonRoomGeometry declared(ResourceLocation templateId, DungeonDataTypes.Int3 declaredSize, BlockPos desiredWorldMinimum, Rotation rotation) {
      if (templateId != null && declaredSize != null && desiredWorldMinimum != null && rotation != null && declaredSize.isPositive()) {
         Vec3i sourceSize = new Vec3i(declaredSize.x(), declaredSize.y(), declaredSize.z());
         int maxSourceX = sourceSize.getX() - 1;
         int maxSourceZ = sourceSize.getZ() - 1;
         BlockPos[] corners = new BlockPos[]{
            BlockPos.ZERO, new BlockPos(maxSourceX, 0, 0), new BlockPos(0, 0, maxSourceZ), new BlockPos(maxSourceX, 0, maxSourceZ)
         };
         int minX = Integer.MAX_VALUE;
         int minZ = Integer.MAX_VALUE;
         int maxX = Integer.MIN_VALUE;
         int maxZ = Integer.MIN_VALUE;

         for (BlockPos corner : corners) {
            BlockPos transformed = StructureTemplate.transform(corner, MIRROR, rotation, PIVOT);
            minX = Math.min(minX, transformed.getX());
            minZ = Math.min(minZ, transformed.getZ());
            maxX = Math.max(maxX, transformed.getX());
            maxZ = Math.max(maxZ, transformed.getZ());
         }

         Vec3i rotatedSize = new Vec3i(maxX - minX + 1, sourceSize.getY(), maxZ - minZ + 1);
         BlockPos placementOrigin = desiredWorldMinimum.offset(-minX, 0, -minZ);
         DungeonTemplatePlacer.WorldBounds bounds = new DungeonTemplatePlacer.WorldBounds(
            desiredWorldMinimum, desiredWorldMinimum.offset(rotatedSize.getX() - 1, rotatedSize.getY() - 1, rotatedSize.getZ() - 1)
         );
         return new DungeonRoomGeometry(templateId, sourceSize, rotatedSize, rotation, desiredWorldMinimum, placementOrigin, bounds, null);
      } else {
         throw new IllegalArgumentException("Template id, positive declared size, minimum, and rotation are required.");
      }
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

   public boolean runtimePlaceable() {
      return this.runtimeTemplate != null;
   }

   public DungeonTemplatePlacer.PreparedTemplate runtimeTemplate() {
      if (this.runtimeTemplate == null) {
         throw new IllegalStateException("Metadata-only room geometry cannot be placed directly.");
      } else {
         return this.runtimeTemplate;
      }
   }

   public BlockPos transformRelative(BlockPos relative) {
      if (relative == null) {
         throw new IllegalArgumentException("Relative position is required.");
      } else {
         return this.placementOrigin.offset(StructureTemplate.transform(relative, MIRROR, this.rotation, PIVOT));
      }
   }

   public Direction transformDirection(Direction direction) {
      if (direction == null) {
         throw new IllegalArgumentException("Direction is required.");
      } else {
         return this.rotation.rotate(direction);
      }
   }

   public DungeonTemplatePlacer.WorldBounds transformRelativeBounds(DungeonTemplatePlacer.RelativeBounds bounds) {
      if (bounds == null) {
         throw new IllegalArgumentException("Relative bounds are required.");
      } else {
         return new DungeonTemplatePlacer.WorldBounds(this.transformRelative(bounds.min()), this.transformRelative(bounds.max()));
      }
   }

   public DungeonTemplatePlacer.TransformedSocket transformSocket(DungeonTemplatePlacer.RelativeBounds opening, Direction facing) {
      return new DungeonTemplatePlacer.TransformedSocket(this.transformRelativeBounds(opening), this.transformDirection(facing));
   }
}
