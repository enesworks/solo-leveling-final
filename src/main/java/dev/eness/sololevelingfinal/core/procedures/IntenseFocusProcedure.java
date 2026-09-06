package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.EntityHighlightSystem;

public final class IntenseFocusProcedure {
   private static final String HIGHLIGHT_SOURCE = "skill:intense_focus";
   private static final int DURATION_TICKS = 200;

   private IntenseFocusProcedure() {
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (world instanceof ServerLevel level && entity instanceof ServerPlayer player) {
         Vec3 center = new Vec3(x, y, z);
         List targets = level.getEntitiesOfClass(
               LivingEntity.class, new AABB(center, center).inflate(30.0), targetx -> targetx != player && EntityHighlightSystem.isPerceptionCandidate(targetx)
            )
            .stream()
            .sorted(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(center)))
            .toList();
         String party = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).party;

         for (LivingEntity target : targets) {
            String targetParty = target.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .party;
            if (party.isBlank() || !party.equals(targetParty)) {
               EntityHighlightSystem.show(player, target, "skill:intense_focus", EntityHighlightSystem.perceptionColor(target), 200, 150);
               target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2, false, false));
            }
         }
      }
   }
}
