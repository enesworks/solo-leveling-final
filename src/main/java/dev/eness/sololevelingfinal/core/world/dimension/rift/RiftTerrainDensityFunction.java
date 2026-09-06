package dev.eness.sololevelingfinal.core.world.dimension.rift;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext;
import net.minecraft.world.level.levelgen.DensityFunction.SimpleFunction;
import net.minecraft.world.level.levelgen.DensityFunction.Visitor;

public record RiftTerrainDensityFunction(
   DensityFunction baseNoise, DensityFunction detailNoise, DensityFunction caveNoise, double fadeStart, double playableRadius, String territory
) implements SimpleFunction {
   public static final KeyDispatchDataCodec<RiftTerrainDensityFunction> CODEC = KeyDispatchDataCodec.of(
      RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("base_noise").forGetter(RiftTerrainDensityFunction::baseNoise),
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("detail_noise").forGetter(RiftTerrainDensityFunction::detailNoise),
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("cave_noise").forGetter(RiftTerrainDensityFunction::caveNoise),
               Codec.doubleRange(256.0, 3.0E7).optionalFieldOf("fade_start", 3180.0).forGetter(RiftTerrainDensityFunction::fadeStart),
               Codec.doubleRange(512.0, 3.0E7).optionalFieldOf("playable_radius", 3500.0).forGetter(RiftTerrainDensityFunction::playableRadius),
               Codec.STRING.optionalFieldOf("territory", "").forGetter(RiftTerrainDensityFunction::territory)
            )
            .apply(instance, RiftTerrainDensityFunction::new)
      )
   );

   public RiftTerrainDensityFunction {
      fadeStart = Math.max(1200.0, fadeStart);
      playableRadius = Math.max(fadeStart + 16.0, playableRadius);
      territory = territory == null ? "" : territory.strip().toLowerCase().replace('-', '_').replace(' ', '_');
      RiftTerritory forcedTerritory = RiftTerritory.fromName(territory);
      if (!territory.isBlank() && forcedTerritory == null) {
         throw new IllegalArgumentException("Unknown Monarch territory '" + territory + "'");
      }

      if (forcedTerritory != null) {
         territory = forcedTerritory.id();
      }
   }

   @Override
   public double compute(FunctionContext context) {
      double x = context.blockX();
      double z = context.blockZ();
      double radius = RiftGeometry.distance(x, z);
      RiftTerritory forcedTerritory = RiftTerritory.fromName(this.territory);
      if (forcedTerritory == null && radius >= this.playableRadius) {
         return -1.0;
      }

      double broad = Mth.clamp(this.baseNoise.compute(context), -1.0, 1.0);
      double detail = Mth.clamp(this.detailNoise.compute(context), -1.0, 1.0);
      double height = forcedTerritory == null
         ? terrainHeight(RiftGeometry.resolveDefault(x, z), broad, detail)
         : territoryHeight(forcedTerritory, broad, detail);
      if (forcedTerritory == null && radius > this.fadeStart) {
         double fade = RiftGeometry.smoothStep((radius - this.fadeStart) / (this.playableRadius - this.fadeStart));
         height = Mth.lerp(fade, height, -92.0);
      }

      double density = (height - context.blockY()) / 24.0;
      if (context.blockY() < height - 12.0 && context.blockY() > -48) {
         double cave = Math.abs(this.caveNoise.compute(context));
         double carve = Mth.clamp((0.105 - cave) * 18.0, 0.0, 1.35);
         density -= carve;
      }

      return Mth.clamp(density, -1.0, 1.0);
   }

   private static double terrainHeight(RiftGeometry.Region region, double broad, double detail) {
      return switch (region.type()) {
         case CENTER -> 82.0 + broad * 2.5 + detail;
         case WASTELAND -> 78.0 + broad * 7.0 + detail * 3.0;
         case RIFT_SCAR -> 64.0 + broad * 6.0 - Math.abs(detail) * 9.0;
         case VOID -> -92.0;
         case TERRITORY -> territoryHeight(region.territory(), broad, detail);
      };
   }

   private static double territoryHeight(RiftTerritory territory, double broad, double detail) {
      return switch (territory) {
         case DESTRUCTION -> 82.0 + broad * 24.0 + Math.abs(detail) * 12.0;
         case FROST -> 102.0 + broad * 38.0 + Math.abs(detail) * 20.0;
         case FANGS -> 86.0 + broad * 19.0 + detail * 8.0;
         case PLAGUES -> 70.0 + broad * 9.0 - Math.abs(detail) * 5.0;
         case IRON_BODY -> 94.0 + broad * 30.0 + detail * 7.0;
         case WHITE_FLAMES -> 80.0 + broad * 20.0 + Math.abs(detail) * 11.0;
         case TRANSFIGURATION -> 94.0 + broad * 31.0 + detail * 16.0;
         case BEGINNING -> 86.0 + broad * 23.0 + detail * 8.0;
      };
   }

   @Override
   public DensityFunction mapAll(Visitor visitor) {
      return visitor.apply(
         new RiftTerrainDensityFunction(
            this.baseNoise.mapAll(visitor),
            this.detailNoise.mapAll(visitor),
            this.caveNoise.mapAll(visitor),
            this.fadeStart,
            this.playableRadius,
            this.territory
         )
      );
   }

   @Override
   public double minValue() {
      return -1.0;
   }

   @Override
   public double maxValue() {
      return 1.0;
   }

   @Override
   public KeyDispatchDataCodec<? extends DensityFunction> codec() {
      return CODEC;
   }
}
