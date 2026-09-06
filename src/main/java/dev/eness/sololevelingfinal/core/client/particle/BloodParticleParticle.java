package dev.eness.sololevelingfinal.core.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

@OnlyIn(Dist.CLIENT)
public class BloodParticleParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public static BloodParticleParticle.BloodParticleParticleProvider provider(SpriteSet spriteSet) {
      return new BloodParticleParticle.BloodParticleParticleProvider(spriteSet);
   }

   protected BloodParticleParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(world, x, y, z);
      this.spriteSet = spriteSet;
      this.setSize(0.2F, 0.2F);
      this.lifetime = 10;
      this.gravity = 1.0F;
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
      Level world = this.level;
      if (this.onGround) {
         world.addParticle(SololevelingModParticleTypes.BLOOD_PARTICLE_LAND.get(), this.x, this.y, this.z, 0.0, 0.0, 0.0);
         this.remove();
      }
   }

   public static class BloodParticleParticleProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public BloodParticleParticleProvider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new BloodParticleParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
      }
   }
}
