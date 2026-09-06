package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class BaranModel extends GeoModel<BaranEntity> {
   public ResourceLocation getAnimationResource(BaranEntity entity) {
      return new ResourceLocation("sololeveling", "animations/baran.animation.json");
   }

   public ResourceLocation getModelResource(BaranEntity entity) {
      return new ResourceLocation("sololeveling", "geo/baran.geo.json");
   }

   public ResourceLocation getTextureResource(BaranEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }

   public void setCustomAnimations(BaranEntity animatable, long instanceId, AnimationState animationState) {
      CoreGeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null) {
         EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
      }
   }
}
