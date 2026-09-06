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
public class RedDustParticleParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public static RedDustParticleParticle.RedDustParticleParticleProvider provider(SpriteSet spriteSet) {
      return new RedDustParticleParticle.RedDustParticleParticleProvider(spriteSet);
   }

   protected RedDustParticleParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(world, x, y, z);
      this.spriteSet = spriteSet;
      this.setSize(0.3F, 0.3F);
      this.quadSize *= 4.0F;
      this.lifetime = 8;
      this.gravity = 0.0F;
      this.hasPhysics = false;
      this.xd = vx * 0.0;
      this.yd = vy * 0.0;
      this.zd = vz * 0.0;
      this.setSpriteFromAge(spriteSet);
   }

   @Override
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.removed) {
         this.setSprite(this.spriteSet.get(this.age / 1 % 8 + 1, 8));
      }
   }

   public static class RedDustParticleParticleProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public RedDustParticleParticleProvider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new RedDustParticleParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
      }
   }
}
