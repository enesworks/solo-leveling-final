package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonGateHandler;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.guild.GuildGateHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.MagicReadingHelper;

public class Portal1RightClickedOnEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (sourceentity != null) {
         if (ProceduralDungeonGateHandler.isProceduralGate(entity)) {
            ProceduralDungeonGateHandler.enter(world, x, y, z, entity, sourceentity);
         } else {
            if (!MagicReadingHelper.isHoldingMagicReader(sourceentity)) {
               if (GuildGateHelper.prepareGateEntry(world, entity, sourceentity)) {
                  return;
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(250.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
                     && entityiterator instanceof TamableAnimal _tamIsTamedBy
                     && sourceentity instanceof LivingEntity _livEnt
                     && _tamIsTamedBy.isOwnedBy(_livEnt)
                     && !entityiterator.level().isClientSide()) {
                     entityiterator.discard();
                  }
               }

               double _setval = sourceentity.getX();
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.DunX = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               double _setvalx = sourceentity.getY();
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.DunY = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               double _setvalxx = sourceentity.getZ();
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.DunZ = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               sourceentity.setNoGravity(true);
            } else {
               MagicReadingHelper.showRankReading(sourceentity, ProceduralDungeonRank.D);
            }
         }
      }
   }
}
