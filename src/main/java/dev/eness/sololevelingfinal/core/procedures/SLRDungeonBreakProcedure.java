package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.Portal1Entity;
import dev.eness.sololevelingfinal.core.entity.PortalBeruEntity;
import dev.eness.sololevelingfinal.core.entity.PortalLushEntity;
import dev.eness.sololevelingfinal.core.entity.PortalSewersEntity;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;

public class SLRDungeonBreakProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double a = 0.0;
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(64.0), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entityiterator instanceof Portal1Entity
               || entityiterator instanceof PortalLushEntity
               || entityiterator instanceof PortalBeruEntity
               || entityiterator instanceof PortalSewersEntity
               || entityiterator instanceof RedGateEntity) {
               a++;
               entityiterator.getPersistentData().putDouble("PortalLife", 96200.0);
            }
         }

         if (a == 0.0) {
            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("You need to be close to a gate to trigger \"Dungeon Break\""), true);
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x,
                     y,
                     z,
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F,
                     false
                  );
               }
            }
         }
      }
   }
}
