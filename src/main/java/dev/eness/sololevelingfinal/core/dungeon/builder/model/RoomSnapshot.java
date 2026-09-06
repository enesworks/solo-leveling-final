package dev.eness.sololevelingfinal.core.dungeon.builder.model;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record RoomSnapshot(ResourceLocation structureKey, BlockPos size, BlockPos captureMin, String checksum, long capturedAt, long metadataRevision) {
   public RoomSnapshot(ResourceLocation structureKey, BlockPos size, BlockPos captureMin, String checksum, long capturedAt, long metadataRevision) {
      if (structureKey != null && size != null && captureMin != null) {
         checksum = checksum == null ? "" : checksum;
         size = size.immutable();
         captureMin = captureMin.immutable();
         this.structureKey = structureKey;
         this.size = size;
         this.captureMin = captureMin;
         this.checksum = checksum;
         this.capturedAt = capturedAt;
         this.metadataRevision = metadataRevision;
      } else {
         throw new IllegalArgumentException("Snapshot structure key, size, and capture minimum are required.");
      }
   }
}
