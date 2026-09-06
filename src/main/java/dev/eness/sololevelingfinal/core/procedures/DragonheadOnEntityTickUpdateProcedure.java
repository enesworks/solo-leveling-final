package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.DragonFireballEntity;
import dev.eness.sololevelingfinal.core.entity.DragonheadEntity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class DragonheadOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof DragonheadEntity _datEntSetI) {
            _datEntSetI.getEntityData()
               .set(
                  DragonheadEntity.DATA_life, (entity instanceof DragonheadEntity _datEntI ? _datEntI.getEntityData().get(DragonheadEntity.DATA_life) : 0) + 1
               );
         }

         if (!world.getEntitiesOfClass(KargalganEntity.class, AABB.ofSize(new Vec3(x, y, z), 10.0, 10.0, 10.0), e -> true).isEmpty()
            && (entity instanceof DragonheadEntity _datEntI ? _datEntI.getEntityData().get(DragonheadEntity.DATA_life) : 0) >= 10
            && (entity instanceof DragonheadEntity _datEntI ? _datEntI.getEntityData().get(DragonheadEntity.DATA_life) : 0) < 35
            && (entity instanceof DragonheadEntity _datEntI ? _datEntI.getEntityData().get(DragonheadEntity.DATA_life) : 0) % 5 == 0) {
            Entity _shootFrom = entity;
            Level projectileLevel = _shootFrom.level();
            if (!projectileLevel.isClientSide()) {
               Projectile _entityToSpawn = (new Object() {
                     public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new DragonFireballEntity(SololevelingModEntities.DRAGON_FIREBALL.get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        entityToSpawn.setKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                     }
                  })
                  .getArrow(
                     projectileLevel,
                     world.getEntitiesOfClass(KargalganEntity.class, AABB.ofSize(new Vec3(x, y, z), 10.0, 10.0, 10.0), e -> true)
                        .stream()
                        .sorted((new Object() {
                           Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                              return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                           }
                        }).compareDistOf(x, y, z))
                        .findFirst()
                        .orElse(null),
                     10.0F,
                     1
                  );
               _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
               _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 1.0F, 0.0F);
               projectileLevel.addFreshEntity(_entityToSpawn);
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                  );
               }
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                  );
               }
            }
         }

         if ((entity instanceof DragonheadEntity _datEntI ? _datEntI.getEntityData().get(DragonheadEntity.DATA_life) : 0) >= 40
            && !entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }
}
