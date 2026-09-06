package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.FlagOfProtectionEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class FlagOfProtectionSummonProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            Entity entityToSpawn = SololevelingModEntities.FLAG_OF_PROTECTION.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
            if (entityToSpawn != null) {
               entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
            }
         }

         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:flagdeploy")),
                  SoundSource.NEUTRAL,
                  1.2F,
                  1.0F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:flagdeploy")), SoundSource.NEUTRAL, 1.2F, 1.0F, false
               );
            }
         }

         CooldownManager.set(entity, "Protection Mark", 1200);
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entityiterator instanceof FlagOfProtectionEntity && !(entityiterator instanceof TamableAnimal _tamEnt && _tamEnt.isTame())) {
               entityiterator.setCustomName(Component.literal(entity.getDisplayName().getString() + "'s Flag of Protection"));
               if (entityiterator instanceof TamableAnimal _toTame && entity instanceof Player _owner) {
                  _toTame.tame(_owner);
               }
            }
         }
      }
   }
}
