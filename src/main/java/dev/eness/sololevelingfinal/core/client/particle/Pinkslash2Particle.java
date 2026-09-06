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
public class Pinkslash2Particle extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public static Pinkslash2Particle.Pinkslash2ParticleProvider provider(SpriteSet spriteSet) {
      return new Pinkslash2Particle.Pinkslash2ParticleProvider(spriteSet);
   }

   protected Pinkslash2Particle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(world, x, y, z);
      this.spriteSet = spriteSet;
      this.setSize(0.5F, 0.5F);
      this.quadSize *= 4.0F;
      this.lifetime = 10;
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
         this.setSprite(this.spriteSet.get(this.age / 4 % 5 + 1, 5));
      }
   }

   public static class Pinkslash2ParticleProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Pinkslash2ParticleProvider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new Pinkslash2Particle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
      }
   }
}
