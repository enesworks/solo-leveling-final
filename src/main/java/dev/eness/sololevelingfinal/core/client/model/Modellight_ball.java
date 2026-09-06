package dev.eness.sololevelingfinal.core.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class Modellight_ball<T extends Entity> extends EntityModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("sololeveling", "modellight_ball"), "main");
   public final ModelPart ball;

   public Modellight_ball(ModelPart root) {
      this.ball = root.getChild("ball");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition ball = partdefinition.addOrReplaceChild(
         "ball",
         CubeListBuilder.create()
            .texOffs(36, 15)
            .addBox(-8.25F, -3.75F, -3.75F, 14.25F, 7.5F, 7.5F, new CubeDeformation(0.0F))
            .texOffs(0, 8)
            .addBox(-6.75F, -5.25F, -5.25F, 12.75F, 10.5F, 10.5F, new CubeDeformation(0.0F))
            .texOffs(36, 24)
            .addBox(-7.5F, -4.5F, -4.5F, 13.5F, 9.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(4, 0)
            .mirror()
            .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(8, 16)
            .addBox(-5.25F, -6.75F, -5.25F, 10.5F, 12.75F, 10.5F, new CubeDeformation(0.0F))
            .texOffs(20, 37)
            .addBox(-4.5F, -7.5F, -4.5F, 9.0F, 13.5F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(28, 37)
            .addBox(-3.75F, -8.25F, -3.75F, 7.5F, 14.25F, 7.5F, new CubeDeformation(0.0F))
            .texOffs(6, 13)
            .addBox(-4.5F, -4.5F, -7.5F, 9.0F, 9.0F, 13.5F, new CubeDeformation(0.0F))
            .texOffs(16, 16)
            .addBox(-5.25F, -5.25F, -6.75F, 10.5F, 10.5F, 12.75F, new CubeDeformation(0.0F))
            .texOffs(16, 23)
            .mirror()
            .addBox(-3.75F, -3.75F, -8.25F, 7.5F, 7.5F, 14.25F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(11, 28)
            .mirror()
            .addBox(-4.5F, -4.5F, -6.0F, 9.0F, 9.0F, 13.5F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 4)
            .addBox(-5.25F, -5.25F, -6.0F, 10.5F, 10.5F, 12.75F, new CubeDeformation(0.0F))
            .texOffs(16, 32)
            .addBox(-3.75F, -3.75F, -6.0F, 7.5F, 7.5F, 14.25F, new CubeDeformation(0.0F))
            .texOffs(0, 38)
            .addBox(-4.5F, -6.0F, -4.5F, 9.0F, 13.5F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(12, 16)
            .addBox(-5.25F, -6.0F, -5.25F, 10.5F, 12.75F, 10.5F, new CubeDeformation(0.0F))
            .texOffs(30, 8)
            .addBox(-3.75F, -6.0F, -3.75F, 7.5F, 14.25F, 7.5F, new CubeDeformation(0.0F))
            .texOffs(39, 26)
            .addBox(-6.0F, -4.5F, -4.5F, 13.5F, 9.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(24, 24)
            .addBox(-6.0F, -5.25F, -5.25F, 12.75F, 10.5F, 10.5F, new CubeDeformation(0.0F))
            .texOffs(39, 14)
            .addBox(-6.0F, -3.75F, -3.75F, 14.25F, 7.5F, 7.5F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 15.75F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   @Override
   public void renderToBuffer(
      PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha
   ) {
      this.ball.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   @Override
   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }
}
