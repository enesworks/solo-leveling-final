package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.MagicalSkullEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class MagicalSkullModel extends GeoModel<MagicalSkullEntity> {
   public ResourceLocation getAnimationResource(MagicalSkullEntity entity) {
      return new ResourceLocation("sololeveling", "animations/skeleton_skull.animation.json");
   }

   public ResourceLocation getModelResource(MagicalSkullEntity entity) {
      return new ResourceLocation("sololeveling", "geo/skeleton_skull.geo.json");
   }

   public ResourceLocation getTextureResource(MagicalSkullEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }

   public void setCustomAnimations(MagicalSkullEntity animatable, long instanceId, AnimationState animationState) {
      CoreGeoBone head = this.getAnimationProcessor().getBone("body");
      if (head != null) {
         EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
      }
   }
}
