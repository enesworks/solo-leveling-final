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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class Modeljinwoochest1<T extends Entity> extends EntityModel<T> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("sololeveling", "modeljinwoochest_1"), "main");
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;

   public Modeljinwoochest1(ModelPart root) {
      this.Body = root.getChild("Body");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create()
            .texOffs(0, 32)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(28, 28)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild(
         "RightArm",
         CubeListBuilder.create()
            .texOffs(56, 0)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(52, 44)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(-5.0F, 2.0F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild(
         "LeftArm",
         CubeListBuilder.create()
            .texOffs(52, 28)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(0, 48)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(5.0F, 2.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   @Override
   public void renderToBuffer(
      PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha
   ) {
      this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.RightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
      this.LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   @Override
   public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.RightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
      this.LeftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
   }
}
