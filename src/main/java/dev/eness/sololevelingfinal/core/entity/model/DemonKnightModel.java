package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import software.bernie.geckolib.model.GeoModel;

public class DemonKnightModel extends GeoModel<DemonKnightEntity> {
   private static final ResourceLocation MODEL = new ResourceLocation("sololeveling", "geo/el_demon_knight1.geo.json");
   private static final ResourceLocation ANIMATION = new ResourceLocation("sololeveling", "animations/el_demon_knight1.animation.json");
   private static final ResourceLocation TEXTURE_1 = new ResourceLocation("sololeveling", "textures/entities/el_demon_knight1_texture.png");
   private static final ResourceLocation TEXTURE_2 = new ResourceLocation("sololeveling", "textures/entities/el_demon_knight1_texture1.png");
   private static final ResourceLocation TEXTURE_3 = new ResourceLocation("sololeveling", "textures/entities/el_demon_knight1_texture2.png");

   public ResourceLocation getModelResource(DemonKnightEntity entity) {
      return MODEL;
   }

   public ResourceLocation getAnimationResource(DemonKnightEntity entity) {
      return ANIMATION;
   }

   public ResourceLocation getTextureResource(DemonKnightEntity entity) {
      return switch (entity.getVariant()) {
         case 1 -> TEXTURE_2;
         case 2 -> TEXTURE_3;
         default -> TEXTURE_1;
      };
   }
}
