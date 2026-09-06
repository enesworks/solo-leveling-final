package dev.eness.sololevelingfinal.core.client.aura;

import net.minecraft.resources.ResourceLocation;

public record PlayerAuraDefinition(
   String id,
   int primaryColor,
   int secondaryColor,
   ResourceLocation fallbackTexture,
   PlayerAuraDefinition.Facing facing,
   float radius,
   float heightScale,
   float speed,
   int shellLayers,
   int wispCount,
   int spikeCount,
   PlayerAuraDefinition.FluidProfile fluid,
   boolean groundRing,
   int smokeColor
) {
   public static final int SMOKE_FROM_COLORS = -1;

   public PlayerAuraDefinition(
      String id,
      int primaryColor,
      int secondaryColor,
      ResourceLocation fallbackTexture,
      PlayerAuraDefinition.Facing facing,
      float radius,
      float heightScale,
      float speed,
      int shellLayers,
      int wispCount,
      int spikeCount,
      PlayerAuraDefinition.FluidProfile fluid,
      boolean groundRing
   ) {
      this(id, primaryColor, secondaryColor, fallbackTexture, facing, radius, heightScale, speed, shellLayers, wispCount, spikeCount, fluid, groundRing, -1);
   }

   public PlayerAuraDefinition(
      String id,
      int primaryColor,
      int secondaryColor,
      ResourceLocation fallbackTexture,
      PlayerAuraDefinition.Facing facing,
      float radius,
      float heightScale,
      float speed,
      int shellLayers,
      int wispCount,
      int spikeCount,
      PlayerAuraDefinition.FluidProfile fluid,
      boolean groundRing,
      int smokeColor
   ) {
      if (id == null || id.isBlank()) {
         throw new IllegalArgumentException("Aura id cannot be blank");
      }

      if (fallbackTexture != null && facing != null) {
         radius = Math.max(0.1F, radius);
         heightScale = Math.max(0.1F, heightScale);
         speed = Math.max(0.0F, speed);
         shellLayers = Math.max(0, Math.min(4, shellLayers));
         wispCount = Math.max(0, Math.min(24, wispCount));
         spikeCount = Math.max(0, Math.min(24, spikeCount));
         this.id = id;
         this.primaryColor = primaryColor;
         this.secondaryColor = secondaryColor;
         this.fallbackTexture = fallbackTexture;
         this.facing = facing;
         this.radius = radius;
         this.heightScale = heightScale;
         this.speed = speed;
         this.shellLayers = shellLayers;
         this.wispCount = wispCount;
         this.spikeCount = spikeCount;
         this.fluid = fluid;
         this.groundRing = groundRing;
         this.smokeColor = smokeColor;
      } else {
         throw new IllegalArgumentException("Aura texture and facing are required");
      }
   }

   public enum Facing {
      CAMERA,
      HORIZONTAL_CAMERA,
      CROSSED;
   }

   public record FluidProfile(
      int lobeCount, int veilCount, int backflowCount, float radiusScale, float opacity, float turbulence, float speed, PlayerAuraDefinition.FluidStyle style
   ) {
      public FluidProfile(int lobeCount, int veilCount, int backflowCount, float radiusScale, float opacity, float turbulence, float speed) {
         this(lobeCount, veilCount, backflowCount, radiusScale, opacity, turbulence, speed, PlayerAuraDefinition.FluidStyle.LIQUID_FLAME);
      }

      public FluidProfile {
         lobeCount = Math.max(0, Math.min(32, lobeCount));
         veilCount = Math.max(0, Math.min(16, veilCount));
         backflowCount = Math.max(0, Math.min(12, backflowCount));
         radiusScale = Math.max(0.45F, Math.min(2.0F, radiusScale));
         opacity = Math.max(0.05F, Math.min(1.0F, opacity));
         turbulence = Math.max(0.0F, Math.min(2.0F, turbulence));
         speed = Math.max(0.0F, Math.min(3.0F, speed));
         if (style == null) {
            style = PlayerAuraDefinition.FluidStyle.LIQUID_FLAME;
         }
      }
   }

   public enum FluidStyle {
      LIQUID_FLAME,
      SHADOW_RIFT,
      WHITE_FLAME_HAIR;
   }
}
