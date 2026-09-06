package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class BloodRedComIgrisModel extends GeoModel<BloodRedComIgrisEntity> {
   public ResourceLocation getAnimationResource(BloodRedComIgrisEntity entity) {
      return new ResourceLocation("sololeveling", "animations/igrismarcus.animation.json");
   }

   public ResourceLocation getModelResource(BloodRedComIgrisEntity entity) {
      return new ResourceLocation("sololeveling", "geo/igrismarcus.geo.json");
   }

   public ResourceLocation getTextureResource(BloodRedComIgrisEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }

   public void setCustomAnimations(BloodRedComIgrisEntity animatable, long instanceId, AnimationState animationState) {
      CoreGeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null) {
         EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
      }
   }
}
