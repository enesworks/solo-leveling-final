package dev.eness.sololevelingfinal.core.client.model;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.entity.RakanEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public final class RakanModel extends GeoModel<RakanEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "geo/rakan.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "textures/entity/rakan.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "animations/rakan.animation.json");

    @Override
    public ResourceLocation getModelResource(RakanEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RakanEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RakanEntity entity) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(RakanEntity entity, long instanceId, AnimationState<RakanEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
        if (entity.getCombatAction() != RakanEntity.ACTION_NONE) return;
        // Locomotion uses the same articulated rig without adding looping combat clips.
        for (String name : new String[]{"root","body","head","right_arm","left_arm","right_forearm","left_forearm",
                "right_leg","left_leg","right_calf","left_calf"}) {
            var bone = getAnimationProcessor().getBone(name);
            if (bone == null) continue;
            bone.setRotX(0); bone.setRotY(0); bone.setRotZ(0);
            bone.setPosX(0); bone.setPosY(0); bone.setPosZ(0);
        }
        float stride = Mth.cos(state.getLimbSwing() * .6662F) * Math.min(state.getLimbSwingAmount(),1F) * .65F;
        getAnimationProcessor().getBone("right_leg").setRotX(stride);
        getAnimationProcessor().getBone("left_leg").setRotX(-stride);
        getAnimationProcessor().getBone("right_arm").setRotX(-stride*.7F-.12F);
        getAnimationProcessor().getBone("left_arm").setRotX(stride*.7F-.12F);
        getAnimationProcessor().getBone("right_forearm").setRotX(-.25F);
        getAnimationProcessor().getBone("left_forearm").setRotX(-.25F);
        var head = getAnimationProcessor().getBone("head");
        head.setRotX(entity.getXRot()*Mth.DEG_TO_RAD);
        head.setRotY(-Mth.wrapDegrees(entity.yHeadRot-entity.yBodyRot)*Mth.DEG_TO_RAD);
    }
}
