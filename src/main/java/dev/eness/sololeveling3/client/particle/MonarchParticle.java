package dev.eness.sololeveling3.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class MonarchParticle extends TextureSheetParticle {
    public enum Style {
        CHAIN,
        FROST,
        SPIKE,
        CRIMSON_AURA,
        CRIMSON_IMPACT,
        CRIMSON_DUST
    }

    private final SpriteSet sprites;
    private final Style style;
    private final float initialSize;

    private MonarchParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            SpriteSet sprites,
            Style style
    ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.sprites = sprites;
        this.style = style;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        if (style == Style.CHAIN) {
            this.quadSize = 0.23F;
            this.lifetime = 5;
        } else if (style == Style.SPIKE) {
            this.quadSize = 0.55F + random.nextFloat() * 0.35F;
            this.lifetime = 13 + random.nextInt(8);
            this.yd += 0.025D;
        } else if (style == Style.CRIMSON_AURA) {
            this.quadSize = 0.26F + random.nextFloat() * 0.28F;
            this.lifetime = 12 + random.nextInt(7);
            this.yd += 0.025D + random.nextDouble() * 0.035D;
            this.friction = 0.9F;
            setColor(1.0F, 0.12F, 0.16F);
        } else if (style == Style.CRIMSON_IMPACT) {
            this.quadSize = 0.72F + random.nextFloat() * 0.48F;
            this.lifetime = 7;
            this.friction = 0.82F;
            setColor(1.0F, 0.22F, 0.18F);
        } else if (style == Style.CRIMSON_DUST) {
            this.quadSize = 0.16F + random.nextFloat() * 0.17F;
            this.lifetime = 9 + random.nextInt(6);
            this.gravity = 0.045F;
            this.friction = 0.88F;
            setColor(0.95F, 0.05F, 0.08F);
        } else {
            this.quadSize = 0.2F + random.nextFloat() * 0.18F;
            this.lifetime = 11 + random.nextInt(7);
        }
        this.initialSize = this.quadSize;
        this.roll = random.nextFloat() * ((float)Math.PI * 2.0F);
        this.oRoll = this.roll;
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
            alpha = Math.max(0.0F, 1.0F - (float)age / (float)lifetime);
            if (style == Style.CRIMSON_IMPACT) {
                quadSize = initialSize * Math.max(0.1F, 1.0F - (float)age / (float)lifetime);
            } else if (style == Style.CRIMSON_AURA) {
                quadSize = initialSize * (0.7F + 0.3F * alpha);
                oRoll = roll;
                roll += 0.045F;
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        if (style == Style.CRIMSON_AURA
                || style == Style.CRIMSON_IMPACT
                || style == Style.CRIMSON_DUST) {
            return 0xF000F0;
        }
        return super.getLightColor(partialTick);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Style style;

        public Provider(SpriteSet sprites, Style style) {
            this.sprites = sprites;
            this.style = style;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double velocityX,
                double velocityY,
                double velocityZ
        ) {
            return new MonarchParticle(level, x, y, z, velocityX, velocityY, velocityZ, sprites, style);
        }
    }
}
