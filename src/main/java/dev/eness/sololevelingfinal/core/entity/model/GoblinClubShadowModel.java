package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.GoblinClubShadowEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GoblinClubShadowModel extends GeoModel<GoblinClubShadowEntity> {
   public ResourceLocation getAnimationResource(GoblinClubShadowEntity entity) {
      return new ResourceLocation("sololeveling", "animations/goblin_club.animation.json");
   }

   public ResourceLocation getModelResource(GoblinClubShadowEntity entity) {
      return new ResourceLocation("sololeveling", "geo/goblin_club.geo.json");
   }

   public ResourceLocation getTextureResource(GoblinClubShadowEntity entity) {
      return new ResourceLocation("sololeveling", "textures/entities/" + entity.getTexture() + ".png");
   }

   public void setCustomAnimations(GoblinClubShadowEntity animatable, long instanceId, AnimationState animationState) {
      CoreGeoBone head = this.getAnimationProcessor().getBone("h_head");
      if (head != null) {
         EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
      }
   }
}
