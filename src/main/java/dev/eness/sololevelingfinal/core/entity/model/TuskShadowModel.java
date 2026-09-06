package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class TuskShadowModel extends GeoModel<TuskShadowEntity> {
   public ResourceLocation getAnimationResource(TuskShadowEntity entity) {
      return new ResourceLocation("sololeveling", "animations/kardalgan_stand.animation.json");
   }

   public ResourceLocation getModelResource(TuskShadowEntity entity) {
      return new ResourceLocation("sololeveling", "geo/kardalgan_stand.geo.json");
   }

   public ResourceLocation getTextureResource(TuskShadowEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }

   public void setCustomAnimations(TuskShadowEntity animatable, long instanceId, AnimationState animationState) {
      CoreGeoBone head = this.getAnimationProcessor().getBone("tete");
      if (head != null) {
         EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
      }
   }
}
