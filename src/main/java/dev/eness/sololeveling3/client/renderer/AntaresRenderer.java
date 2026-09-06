package dev.eness.sololeveling3.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.client.model.AntaresModel;
import dev.eness.sololeveling3.entity.AntaresEntity;
import dev.eness.sololeveling3.vfx.AntaresVfxMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class AntaresRenderer extends GeoEntityRenderer<AntaresEntity> {
    private static final ResourceLocation WHITE=ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID,"textures/particle/rakan_white.png");
    private boolean glowing;
    public AntaresRenderer(EntityRendererProvider.Context context){super(context,new AntaresModel());shadowRadius=.65F;}
    @Override protected void applyRotations(AntaresEntity e,PoseStack pose,float age,float yaw,float partial){super.applyRotations(e,pose,age,yaw,partial);pose.mulPose(Axis.YP.rotationDegrees(180));}
    @Override public boolean shouldRender(AntaresEntity e,Frustum frustum,double x,double y,double z){
        if(e.getCombatAction()==AntaresEntity.COMBO||e.getCombatAction()==AntaresEntity.RAY)return frustum.isVisible(e.getBoundingBox().expandTowards(e.getAim().scale(e.getBeamLength())).inflate(3));
        return super.shouldRender(e,frustum,x,y,z);
    }
    @Override public void render(AntaresEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        super.render(e,yaw,partial,pose,buffers,light);
        if(!e.isAlive()||e.getCombatAction()==AntaresEntity.NONE&&e.getCounterTick()==0)return;
        VertexConsumer v=buffers.getBuffer(RenderType.entityTranslucent(WHITE));
        double tick=e.getActionTick()+partial,range=e.getBeamLength();Vec3 aim=e.getAim();
        if(e.getCombatAction()==AntaresEntity.COMBO){Vec3 target=e.comboTargetPoint(partial);
            if(target!=null){Vec3 delta=target.subtract(e.socket("right_palm",tick));aim=delta.normalize();range=delta.length();}}
        if(e.getCombatAction()==AntaresEntity.RAY&&tick<=AntaresEntity.RAY_END){Vec3 target=e.lockedTargetPoint(partial);
            if(target!=null){Vec3 origin=e.socket("right_hand",tick).add(e.socket("left_hand",tick)).scale(.5);
                Vec3 delta=target.subtract(origin);aim=delta.normalize();range=delta.length();}}
        AntaresVfxMesh.emit(e.getCombatAction(),tick,e.getCounterTick()>0?e.getCounterTick()+partial:0,
                e.forward(),aim,range,e::socket,(a,b,c,d,r,g,blue,alpha)->{
                    vertex(v,pose,a,r,g,blue,alpha);vertex(v,pose,b,r,g,blue,alpha);vertex(v,pose,c,r,g,blue,alpha);vertex(v,pose,d,r,g,blue,alpha);
                    vertex(v,pose,d,r,g,blue,alpha);vertex(v,pose,c,r,g,blue,alpha);vertex(v,pose,b,r,g,blue,alpha);vertex(v,pose,a,r,g,blue,alpha);
                });
    }
    private void vertex(VertexConsumer v,PoseStack p,Vec3 a,float r,float g,float b,float alpha){v.vertex(p.last().pose(),(float)a.x,(float)a.y,(float)a.z)
        .color(r,g,b,alpha).uv(.5F,.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(p.last().normal(),0,1,0).endVertex();}
    @Override public void renderRecursively(PoseStack pose,AntaresEntity e,GeoBone bone,RenderType type,MultiBufferSource buffers,
                VertexConsumer v,boolean reRender,float partial,int light,int overlay,float r,float g,float b,float alpha){
        super.renderRecursively(pose,e,bone,type,buffers,v,reRender,partial,light,overlay,r,g,b,alpha);
        if(!glowing&&e.getCombatAction()==AntaresEntity.GUARD&&(bone.getName().endsWith("_pauldron")||bone.getName().endsWith("_forearm"))){
            glowing=true;
            try{RenderType glow=RenderType.eyes(getTextureLocation(e));super.renderRecursively(pose,e,bone,glow,buffers,buffers.getBuffer(glow),true,partial,0xF000F0,overlay,.7F,.05F,.09F,.3F);}
            finally{glowing=false;buffers.getBuffer(type);}
        }
    }
}
