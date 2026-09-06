package dev.eness.sololevelingfinal.core.world.dimension.rift;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate.Sampler;

public final class DimensionalRiftBiomeSource extends BiomeSource {
   public static final Codec<DimensionalRiftBiomeSource> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Biome.CODEC.fieldOf("battlefield").forGetter(source -> source.battlefield),
            Biome.CODEC.fieldOf("wasteland").forGetter(source -> source.wasteland),
            Biome.CODEC.fieldOf("rift_scar").forGetter(source -> source.riftScar),
            Biome.CODEC.fieldOf("void_biome").forGetter(source -> source.voidBiome),
            Biome.LIST_CODEC.fieldOf("territories").forGetter(source -> HolderSet.direct(source.territories)),
            Codec.DOUBLE.optionalFieldOf("safe_center_radius", 256.0).forGetter(source -> source.safeCenterRadius),
            Codec.DOUBLE.optionalFieldOf("star_core_radius", 600.0).forGetter(source -> source.starCoreRadius),
            Codec.DOUBLE.optionalFieldOf("star_tip_radius", 1200.0).forGetter(source -> source.starTipRadius),
            Codec.DOUBLE.optionalFieldOf("star_exponent", 1.7).forGetter(source -> source.starExponent),
            Codec.DOUBLE.optionalFieldOf("scar_half_width", 56.0).forGetter(source -> source.scarHalfWidth),
            Codec.DOUBLE.optionalFieldOf("playable_radius", 3500.0).forGetter(source -> source.playableRadius)
         )
         .apply(instance, DimensionalRiftBiomeSource::new)
   );
   private final Holder<Biome> battlefield;
   private final Holder<Biome> wasteland;
   private final Holder<Biome> riftScar;
   private final Holder<Biome> voidBiome;
   private final List<Holder<Biome>> territories;
   private final double safeCenterRadius;
   private final double starCoreRadius;
   private final double starTipRadius;
   private final double starExponent;
   private final double scarHalfWidth;
   private final double playableRadius;

   public DimensionalRiftBiomeSource(
      Holder<Biome> battlefield,
      Holder<Biome> wasteland,
      Holder<Biome> riftScar,
      Holder<Biome> voidBiome,
      HolderSet<Biome> territories,
      double safeCenterRadius,
      double starCoreRadius,
      double starTipRadius,
      double starExponent,
      double scarHalfWidth,
      double playableRadius
   ) {
      this.battlefield = battlefield;
      this.wasteland = wasteland;
      this.riftScar = riftScar;
      this.voidBiome = voidBiome;
      this.territories = List.copyOf(territories.stream().toList());
      if (this.territories.size() != RiftTerritory.values().length) {
         throw new IllegalArgumentException("Dimensional Rift requires exactly eight ordered territory biomes");
      }

      this.safeCenterRadius = Math.max(0.0, safeCenterRadius);
      this.starCoreRadius = Math.max(this.safeCenterRadius, starCoreRadius);
      this.starTipRadius = Math.max(this.starCoreRadius, starTipRadius);
      this.starExponent = Math.max(0.1, starExponent);
      this.scarHalfWidth = Math.max(0.0, scarHalfWidth);
      this.playableRadius = Math.max(this.starTipRadius, playableRadius);
   }

   @Override
   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   @Override
   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      List<Holder<Biome>> biomes = new ArrayList<>(12);
      biomes.add(this.battlefield);
      biomes.add(this.wasteland);
      biomes.add(this.riftScar);
      biomes.add(this.voidBiome);
      biomes.addAll(this.territories);
      return biomes.stream();
   }

   @Override
   public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Sampler sampler) {
      double x = quartX << 2;
      double z = quartZ << 2;
      RiftGeometry.Region region = RiftGeometry.resolve(
         x, z, this.safeCenterRadius, this.starCoreRadius, this.starTipRadius, this.starExponent, this.scarHalfWidth, this.playableRadius
      );

      return switch (region.type()) {
         case CENTER -> this.battlefield;
         case WASTELAND -> this.wasteland;
         case RIFT_SCAR -> this.riftScar;
         case VOID -> this.voidBiome;
         case TERRITORY -> (Holder)this.territories.get(region.territory().ordinal());
      };
   }
}
