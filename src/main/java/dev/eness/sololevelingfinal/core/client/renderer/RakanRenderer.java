package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.eness.sololevelingfinal.core.client.model.RakanModel;
import dev.eness.sololevelingfinal.core.entity.RakanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class RakanRenderer extends GeoEntityRenderer<RakanEntity> {
    public RakanRenderer(EntityRendererProvider.Context context) {
        super(context, new RakanModel());
        this.shadowRadius = 0.52F;
    }

    @Override
    protected void applyRotations(RakanEntity entity, PoseStack pose, float age, float yaw, float partialTick) {
        super.applyRotations(entity, pose, age, yaw, partialTick);
        // The accepted atlas has the face on the SOUTH (+Z) side.
        pose.mulPose(Axis.YP.rotationDegrees(180));
    }
}
