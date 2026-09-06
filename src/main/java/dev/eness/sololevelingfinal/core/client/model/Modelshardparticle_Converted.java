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

public class Modelshardparticle_Converted<T extends Entity> extends EntityModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("sololeveling", "modelshardparticle_converted"), "main");
   public final ModelPart group;

   public Modelshardparticle_Converted(ModelPart root) {
      this.group = root.getChild("group");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition group = partdefinition.addOrReplaceChild(
         "group",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(0.0F, -0.5F, -1.0F, 0.0F, 3.5F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-1.0F, -0.5F, 0.0F, 2.0F, 3.5F, 0.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-0.25F, -0.25F, -0.25F, 0.5F, 3.0F, 0.5F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 14.75F, -0.5F)
      );
      PartDefinition cube_r1 = group.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.5F, -1.0F, 0.0F, 3.5F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
      );
      PartDefinition cube_r2 = group.addOrReplaceChild(
         "cube_r2",
         CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.5F, -1.0F, 0.0F, 3.5F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   @Override
   public void renderToBuffer(
      PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha
   ) {
      this.group.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   @Override
   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
   }
}
