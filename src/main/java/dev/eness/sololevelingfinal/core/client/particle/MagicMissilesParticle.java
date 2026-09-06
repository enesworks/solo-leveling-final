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
public class MagicMissilesParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public static MagicMissilesParticle.MagicMissilesParticleProvider provider(SpriteSet spriteSet) {
      return new MagicMissilesParticle.MagicMissilesParticleProvider(spriteSet);
   }

   protected MagicMissilesParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(world, x, y, z);
      this.spriteSet = spriteSet;
      this.setSize(0.4F, 0.4F);
      this.quadSize *= 2.0F;
      this.lifetime = 15;
      this.gravity = 0.0F;
      this.hasPhysics = false;
      this.xd = vx * 0.0;
      this.yd = vy * 0.0;
      this.zd = vz * 0.0;
      this.setSpriteFromAge(spriteSet);
   }

   @Override
   public int getLightColor(float partialTick) {
      return 15728880;
   }

   @Override
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_LIT;
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.removed) {
         this.setSprite(this.spriteSet.get(this.age / 1 % 16 + 1, 16));
      }
   }

   public static class MagicMissilesParticleProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public MagicMissilesParticleProvider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new MagicMissilesParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
      }
   }
}
