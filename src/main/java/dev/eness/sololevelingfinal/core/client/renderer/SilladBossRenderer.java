package dev.eness.sololevelingfinal.core.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import dev.eness.sololevelingfinal.core.entity.SilladBossEntity;

public final class SilladBossRenderer extends HumanoidMobRenderer<SilladBossEntity, SilladBossRenderer.SilladHumanoidModel> {
   private static final ResourceLocation PLACEHOLDER_TEXTURE = new ResourceLocation("sololeveling", "textures/entities/sillad_boss_placeholder.png");

   public SilladBossRenderer(Context context) {
      super(context, new SilladBossRenderer.SilladHumanoidModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
      this.addLayer(
         new HumanoidArmorLayer<>(
            this,
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()
         )
      );
      this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(SilladBossEntity entity) {
      return PLACEHOLDER_TEXTURE;
   }

   static final class SilladHumanoidModel extends HumanoidModel<SilladBossEntity> {
      private SilladHumanoidModel(ModelPart root) {
         super(root);
      }

      public void setupAnim(SilladBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         this.body.xRot = 0.0F;
         SilladBossEntity.Action action = entity.getCombatAction();
         if (action != SilladBossEntity.Action.IDLE) {
            float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
            float actionTime = entity.getActionTick() + partialTick;
            float pulse = Mth.sin(actionTime * 0.22F) * 0.08F;
            float authority = entity.isSpiritualized() ? 0.12F : 0.0F;
            switch (action) {
               case FROST_CLEAVE:
                  this.poseCleave(actionTime);
                  break;
               case ICE_SPEAR:
               case GLACIAL_EXECUTION:
                  this.poseIceSpear(actionTime);
                  break;
               case FROST_COUNTER:
                  this.poseCounter(pulse);
                  break;
               case PHASE_TRANSITION:
               case ABSOLUTE_ZERO:
                  this.poseSovereignCast(pulse, authority);
                  break;
               case FROZEN_PATH:
               case FROST_STEP:
                  this.poseFrostMovement(pulse);
                  break;
               case FLASH_FREEZE:
               case STILLNESS_DECREE:
               case SPIRE_CAGE:
               case WHITEOUT_PROCESSION:
               case WINTER_REMEMBERS:
               case CROWN_OF_WINTER:
                  this.poseForwardCast(pulse, authority);
               case IDLE:
            }
         }
      }

      private void poseCleave(float actionTime) {
         float sweep = Mth.sin(Mth.clamp(actionTime / 12.0F, 0.0F, 1.0F) * (float) Math.PI);
         this.body.yRot = -0.32F + sweep * 0.64F;
         this.rightArm.xRot = -1.75F + sweep * 0.55F;
         this.rightArm.yRot = -0.65F + sweep * 1.25F;
         this.rightArm.zRot = -0.18F;
         this.leftArm.xRot = -0.55F;
         this.leftArm.yRot = 0.28F;
      }

      private void poseIceSpear(float actionTime) {
         float draw = Mth.clamp(actionTime / 10.0F, 0.0F, 1.0F);
         this.rightArm.xRot = -1.35F - draw * 0.55F;
         this.rightArm.yRot = -0.38F;
         this.rightArm.zRot = 0.1F;
         this.leftArm.xRot = -0.78F;
         this.leftArm.yRot = 0.42F;
         this.leftArm.zRot = -0.18F;
      }

      private void poseCounter(float pulse) {
         this.rightArm.xRot = -1.22F + pulse;
         this.rightArm.yRot = -0.82F;
         this.rightArm.zRot = -0.18F;
         this.leftArm.xRot = -1.22F - pulse;
         this.leftArm.yRot = 0.82F;
         this.leftArm.zRot = 0.18F;
      }

      private void poseSovereignCast(float pulse, float authority) {
         this.rightArm.xRot = -2.68F - authority + pulse;
         this.rightArm.yRot = -0.22F;
         this.rightArm.zRot = 0.2F;
         this.leftArm.xRot = -2.68F - authority - pulse;
         this.leftArm.yRot = 0.22F;
         this.leftArm.zRot = -0.2F;
         this.head.xRot -= 0.12F;
      }

      private void poseFrostMovement(float pulse) {
         this.body.xRot = 0.18F;
         this.rightArm.xRot = 0.72F + pulse;
         this.rightArm.yRot = -0.12F;
         this.rightArm.zRot = 0.12F;
         this.leftArm.xRot = 0.72F - pulse;
         this.leftArm.yRot = 0.12F;
         this.leftArm.zRot = -0.12F;
      }

      private void poseForwardCast(float pulse, float authority) {
         this.rightArm.xRot = -1.42F - authority + pulse;
         this.rightArm.yRot = -0.28F;
         this.rightArm.zRot = 0.08F;
         this.leftArm.xRot = -1.42F - authority - pulse;
         this.leftArm.yRot = 0.28F;
         this.leftArm.zRot = -0.08F;
      }
   }
}
