package dev.eness.sololevelingfinal.core.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlowYellowParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public static GlowYellowParticle.GlowYellowParticleProvider provider(SpriteSet spriteSet) {
      return new GlowYellowParticle.GlowYellowParticleProvider(spriteSet);
   }

   protected GlowYellowParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(world, x, y, z);
      this.spriteSet = spriteSet;
      this.setSize(0.2F, 0.2F);
      this.lifetime = Math.max(1, 4 + (this.random.nextInt(2) - 1));
      this.gravity = 0.0F;
      this.hasPhysics = true;
      this.xd = vx * 1.0;
      this.yd = vy * 1.0;
      this.zd = vz * 1.0;
      this.pickSprite(spriteSet);
   }

   @Override
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @Override
   public void tick() {
      super.tick();
   }

   public static class GlowYellowParticleProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public GlowYellowParticleProvider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new GlowYellowParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
      }
   }
}
