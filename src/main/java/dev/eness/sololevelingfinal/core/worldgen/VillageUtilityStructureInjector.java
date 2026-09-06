package dev.eness.sololevelingfinal.core.worldgen;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class VillageUtilityStructureInjector {
   private static final ResourceLocation INSTANCE_ENTRANCE = new ResourceLocation("sololeveling", "istanceenterance");
   private static final ResourceLocation EVALUATOR = new ResourceLocation("sololeveling", "evaluation");
   private static final Holder<StructureProcessorList> PROCESSORS = Holder.direct(new StructureProcessorList(List.of(BlockIgnoreProcessor.STRUCTURE_BLOCK)));
   private static final int[] SEARCH_RADII = new int[]{28, 34, 40, 46, 52, 58, 64, 72, 80, 92};
   private static final int DIRECTION_COUNT = 16;
   private static final int PIECE_CLEARANCE = 3;
   private static final int MAX_PREFERRED_SLOPE = 4;

   private VillageUtilityStructureInjector() {
   }

   public static boolean isVanillaVillageStartPool(Holder<StructureTemplatePool> startPool) {
      return startPool == null ? false : startPool.unwrapKey().map(key -> {
         ResourceLocation id = key.location();
         String path = id.getPath();
         return "minecraft".equals(id.getNamespace()) && path.startsWith("village/") && path.endsWith("/town_centers");
      }).orElse(false);
   }

   public static Optional<GenerationStub> inject(GenerationContext context, Holder<StructureTemplatePool> startPool, Optional<GenerationStub> generated) {
      if (isVanillaVillageStartPool(startPool) && !generated.isEmpty()) {
         GenerationStub original = generated.get();
         StructurePiecesBuilder pieces = original.getPiecesBuilder();
         RandomSource random = RandomSource.create(villageSeed(context));
         int directionOffset = random.nextInt(16);
         int radiusOffset = random.nextInt(SEARCH_RADII.length);
         addPiece(context, pieces, EVALUATOR, directionOffset, radiusOffset, 0);
         addPiece(context, pieces, INSTANCE_ENTRANCE, directionOffset + 8, radiusOffset + SEARCH_RADII.length / 3, 1);
         return Optional.of(new GenerationStub(original.position(), Either.right(pieces)));
      } else {
         return generated;
      }
   }

   private static void addPiece(
      GenerationContext context, StructurePiecesBuilder pieces, ResourceLocation templateId, int directionOffset, int radiusOffset, int pieceIndex
   ) {
      StructureTemplateManager templates = context.structureTemplateManager();
      StructurePoolElement element = StructurePoolElement.single(templateId.toString(), PROCESSORS).apply(Projection.RIGID);
      VillageUtilityStructureInjector.Placement bestFallback = null;

      for (int radiusIndex = 0; radiusIndex < SEARCH_RADII.length; radiusIndex++) {
         int radius = SEARCH_RADII[Math.floorMod(radiusIndex + radiusOffset, SEARCH_RADII.length)];

         for (int directionIndex = 0; directionIndex < 16; directionIndex++) {
            int direction = Math.floorMod(directionIndex + directionOffset + radiusIndex * 3, 16);
            double angle = direction * Math.PI * 2.0 / 16.0;
            int centerX = context.chunkPos().getMiddleBlockX() + (int)Math.round(Math.cos(angle) * radius);
            int centerZ = context.chunkPos().getMiddleBlockZ() + (int)Math.round(Math.sin(angle) * radius);
            Rotation rotation = rotationFacingVillage(direction);
            VillageUtilityStructureInjector.Placement placement = placementAt(context, templates, element, rotation, centerX, centerZ);
            if (placement != null && !collides(pieces, placement.box())) {
               if (bestFallback == null || placement.slope() < bestFallback.slope()) {
                  bestFallback = placement;
               }

               if (placement.slope() <= 4 && placement.waterDepth() <= 1) {
                  add(pieces, templates, element, rotation, placement);
                  return;
               }
            }
         }
      }

      if (bestFallback != null) {
         add(pieces, templates, element, bestFallback.rotation(), bestFallback);
      } else {
         int sign = pieceIndex == 0 ? 1 : -1;
         int centerX = context.chunkPos().getMiddleBlockX() + sign * 112;
         int centerZ = context.chunkPos().getMiddleBlockZ() - sign * 48;
         Rotation rotation = sign > 0 ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
         VillageUtilityStructureInjector.Placement fallback = placementAt(context, templates, element, rotation, centerX, centerZ);
         if (fallback != null) {
            add(pieces, templates, element, rotation, fallback);
         }
      }
   }

   private static VillageUtilityStructureInjector.Placement placementAt(
      GenerationContext context, StructureTemplateManager templates, StructurePoolElement element, Rotation rotation, int desiredCenterX, int desiredCenterZ
   ) {
      BoundingBox zeroBox = element.getBoundingBox(templates, BlockPos.ZERO, rotation);
      int originX = desiredCenterX - Math.floorDiv(zeroBox.minX() + zeroBox.maxX(), 2);
      int originZ = desiredCenterZ - Math.floorDiv(zeroBox.minZ() + zeroBox.maxZ(), 2);
      int minX = originX + zeroBox.minX();
      int maxX = originX + zeroBox.maxX();
      int minZ = originZ + zeroBox.minZ();
      int maxZ = originZ + zeroBox.maxZ();
      int[] heights = new int[]{
         surfaceHeight(context, desiredCenterX, desiredCenterZ),
         surfaceHeight(context, minX, minZ),
         surfaceHeight(context, minX, maxZ),
         surfaceHeight(context, maxX, minZ),
         surfaceHeight(context, maxX, maxZ)
      };
      int minHeight = Integer.MAX_VALUE;
      int maxHeight = Integer.MIN_VALUE;

      for (int height : heights) {
         minHeight = Math.min(minHeight, height);
         maxHeight = Math.max(maxHeight, height);
      }

      int groundY = heights[0];
      int oceanFloor = context.chunkGenerator()
         .getBaseHeight(desiredCenterX, desiredCenterZ, Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
      BlockPos origin = new BlockPos(originX, groundY, originZ);
      BoundingBox box = element.getBoundingBox(templates, origin, rotation);
      return new VillageUtilityStructureInjector.Placement(origin, box, rotation, maxHeight - minHeight, Math.max(0, groundY - oceanFloor));
   }

   private static int surfaceHeight(GenerationContext context, int x, int z) {
      return context.chunkGenerator().getBaseHeight(x, z, Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
   }

   private static boolean collides(StructurePiecesBuilder pieces, BoundingBox candidate) {
      return pieces.findCollisionPiece(candidate.inflatedBy(3)) != null;
   }

   private static void add(
      StructurePiecesBuilder pieces,
      StructureTemplateManager templates,
      StructurePoolElement element,
      Rotation rotation,
      VillageUtilityStructureInjector.Placement placement
   ) {
      pieces.addPiece(new PoolElementStructurePiece(templates, element, placement.origin(), element.getGroundLevelDelta(), rotation, placement.box()));
   }

   private static Rotation rotationFacingVillage(int direction) {
      int quadrant = Math.floorMod((direction + 2) / 4, 4);

      return switch (quadrant) {
         case 0 -> Rotation.COUNTERCLOCKWISE_90;
         case 1 -> Rotation.CLOCKWISE_180;
         case 2 -> Rotation.CLOCKWISE_90;
         default -> Rotation.NONE;
      };
   }

   private static long villageSeed(GenerationContext context) {
      long chunk = context.chunkPos().toLong();
      return context.seed() ^ Long.rotateLeft(chunk, 21) ^ 6002262933928430657L;
   }

   private record Placement(BlockPos origin, BoundingBox box, Rotation rotation, int slope, int waterDepth) {
   }
}
