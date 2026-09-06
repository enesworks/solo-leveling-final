package dev.eness.sololevelingfinal.core.entity.model;

import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public final class ShadowIronModel extends GeoModel<ShadowIronEntity> {
   private static final ResourceLocation MODEL = new ResourceLocation("sololeveling", "geo/shadow_iron.geo.json");
   private static final ResourceLocation ANIMATIONS = new ResourceLocation("sololeveling", "animations/shadow_iron.animation.json");
   private static final ResourceLocation NORMAL = new ResourceLocation("sololeveling", "textures/entities/iron_shadow.png");
   private static final ResourceLocation DOMAIN = new ResourceLocation("sololeveling", "textures/entities/iron_shadow_domain.png");

   public ResourceLocation getModelResource(ShadowIronEntity entity) {
      return MODEL;
   }

   public ResourceLocation getTextureResource(ShadowIronEntity entity) {
      return entity.isDomainBoosted() ? DOMAIN : NORMAL;
   }

   public ResourceLocation getAnimationResource(ShadowIronEntity entity) {
      return ANIMATIONS;
   }

   public void setCustomAnimations(ShadowIronEntity entity, long instanceId, AnimationState<ShadowIronEntity> state) {
      if (!entity.isActing()) {
         CoreGeoBone head = this.getAnimationProcessor().getBone("bone34");
         if (head != null) {
            EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(data.headPitch() * (float) (Math.PI / 180.0));
            head.setRotY(data.netHeadYaw() * (float) (Math.PI / 180.0));
         }
      }
   }
}
