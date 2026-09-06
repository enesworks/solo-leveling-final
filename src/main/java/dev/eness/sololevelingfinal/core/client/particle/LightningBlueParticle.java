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
public class LightningBlueParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public static LightningBlueParticle.LightningBlueParticleProvider provider(SpriteSet spriteSet) {
      return new LightningBlueParticle.LightningBlueParticleProvider(spriteSet);
   }

   protected LightningBlueParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(world, x, y, z);
      this.spriteSet = spriteSet;
      this.setSize(0.2F, 0.2F);
      this.quadSize *= 0.4F;
      this.lifetime = 2;
      this.gravity = 0.0F;
      this.hasPhysics = true;
      this.xd = vx * 1.0;
      this.yd = vy * 1.0;
      this.zd = vz * 1.0;
      this.pickSprite(spriteSet);
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
   }

   public static class LightningBlueParticleProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public LightningBlueParticleProvider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new LightningBlueParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
      }
   }
}
