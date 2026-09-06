package dev.eness.sololevelingfinal.core.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import dev.eness.sololevelingfinal.core.vfx.RakanVfxMesh;
import dev.eness.sololevelingfinal.core.vfx.RakanVfxMesh.Style;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** World-oriented, tapered ribbons. One server particle packet per major effect;
 * all expansion/fading is client-side. No model cubes or terrain edits. */
public final class RakanCombatParticle extends TextureSheetParticle {
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private final Style style;
    private final Vec3 forward;
    private final Vec3 right;
    private final double effectScale;

    private RakanCombatParticle(ClientLevel level, double x, double y, double z,
                                double dx, double dy, double dz, SpriteSet sprites, Style style) {
        super(level, x, y, z, 0, 0, 0);
        this.style = style;
        this.effectScale = style == Style.SLAM && dy > 0 ? Mth.clamp(dy, .5, 1) : 1;
        this.forward = new Vec3(dx, 0, dz).lengthSqr() < 1e-5 ? new Vec3(0, 0, 1) : new Vec3(dx, 0, dz).normalize();
        this.right = new Vec3(forward.z, 0, -forward.x);
        this.xd = this.yd = this.zd = 0;
        this.hasPhysics = false;
        this.gravity = 0;
        this.lifetime = RakanVfxMesh.duration(style);
        setSize(style == Style.ROAR ? 18 : style == Style.SLAM ? 10 : 5, style == Style.ROAR ? 18 : 5);
        pickSprite(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    @Override
    public void render(VertexConsumer out, Camera camera, float partialTick) {
        Vec3 center = new Vec3(x,y,z).subtract(camera.getPosition());
        RakanVfxMesh.emit(style,age+partialTick,center,forward,
                (a,b,c,d,r,g,blue,alpha) -> quad(out,
                        center.add(a.subtract(center).scale(effectScale)), center.add(b.subtract(center).scale(effectScale)),
                        center.add(c.subtract(center).scale(effectScale)), center.add(d.subtract(center).scale(effectScale)), r,g,blue,alpha));
    }

    private void quad(VertexConsumer out, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float r, float g, float blue, float alpha) {
        // Submit both windings: the effect remains visible from either side.
        vertex(out,a,r,g,blue,alpha);vertex(out,b,r,g,blue,alpha);vertex(out,c,r,g,blue,alpha);vertex(out,d,r,g,blue,alpha);
        vertex(out,d,r,g,blue,alpha);vertex(out,c,r,g,blue,alpha);vertex(out,b,r,g,blue,alpha);vertex(out,a,r,g,blue,alpha);
    }

    private void vertex(VertexConsumer out, Vec3 p, float r, float g, float b, float alpha) {
        out.vertex(p.x,p.y,p.z).uv((getU0()+getU1())*.5F,(getV0()+getV1())*.5F)
                .color(r,g,b,alpha).uv2(0xF000F0).endVertex();
    }

    public record Provider(SpriteSet sprites, Style style) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new RakanCombatParticle(level,x,y,z,dx,dy,dz,sprites,style);
        }
    }
}
