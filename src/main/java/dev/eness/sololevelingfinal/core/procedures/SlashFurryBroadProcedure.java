package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class SlashFurryBroadProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double particleNum = 0.0;
         double vX = 0.0;
         double vY = 0.0;
         double vZ = 0.0;
         double i = 0.0;
         double x_pos = 0.0;
         double z_pos = 0.0;
         double hei = 0.0;
         double speed = 0.0;
         double arcAngle = 0.0;
         double radAngle = 0.0;
         double radYaw = 0.0;
         double radPitch = 0.0;
         double angle = 0.0;
         double y_pos = 0.0;
         double radius = 0.0;
         if (entity instanceof LivingEntity _entity) {
            _entity.swing(InteractionHand.MAIN_HAND, true);
         }

         radius = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).slashfurtimer;
         hei = 0.0;
         speed = 15.0;
         particleNum = 120.0;
         arcAngle = 180.0;
         radYaw = Math.toRadians(entity.getYRot() + 90.0F);
         radPitch = Math.toRadians((entity.getXRot() + 90.0F) * -1.0F);

         for (int index0 = 0; index0 < (int)particleNum; index0++) {
            angle = i * (arcAngle / particleNum);
            radAngle = Math.toRadians(angle);
            vX = (Math.sin(radAngle) * Math.sin(radPitch) * Math.cos(radYaw) + Math.cos(radAngle) * Math.sin(radYaw)) * -1.0;
            vY = Math.sin(radAngle) * Math.cos(radPitch);
            vZ = Math.sin(radAngle) * Math.sin(radPitch) * Math.sin(radYaw) * -1.0 + Math.cos(radAngle) * Math.cos(radYaw);
            x_pos = x + radius * vX;
            y_pos = y + radius * vY;
            z_pos = z + radius * vZ;
            i++;
            if (angle >= 60.0 && angle <= 120.0) {
               world.addParticle(SololevelingModParticleTypes.RED_DUST_PARTICLE.get(), x_pos, y_pos + 1.8, z_pos, 0.0, 0.0, 0.0);
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.attack.sweep")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.attack.sweep")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F,
                        false
                     );
                  }
               }

               Vec3 _center = new Vec3(x_pos, y_pos + 1.4, z_pos);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator && !(entityiterator instanceof ExperienceOrb) && !(entityiterator instanceof ItemEntity)) {
                     entityiterator.hurt(
                        new DamageSource(
                           world.registryAccess()
                              .registryOrThrow(Registries.DAMAGE_TYPE)
                              .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:fighter"))),
                           entity
                        ),
                        (float)(10.0 + TemporaryStatBonusManager.effectiveStrength(entity) / 12.0)
                     );
                     entityiterator.setDeltaMovement(new Vec3(1.0 * entity.getLookAngle().x, 0.875 * entity.getLookAngle().y, 1.0 * entity.getLookAngle().z));
                  }
               }
            }
         }
      }
   }
}
