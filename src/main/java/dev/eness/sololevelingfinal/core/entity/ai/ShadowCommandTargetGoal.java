package dev.eness.sololevelingfinal.core.entity.ai;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public final class ShadowCommandTargetGoal extends Goal {
   private final Mob shadow;

   public ShadowCommandTargetGoal(Mob shadow) {
      this.shadow = shadow;
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      return this.hasLiveOwner();
   }

   @Override
   public boolean canContinueToUse() {
      return this.hasLiveOwner();
   }

   @Override
   public void start() {
      ShadowMonarchManager.tickShadowTargeting(this.shadow);
   }

   @Override
   public void tick() {
      ShadowMonarchManager.tickShadowTargeting(this.shadow);
   }

   @Override
   public void stop() {
      this.shadow.setTarget(null);
      this.shadow.getNavigation().stop();
   }

   private boolean hasLiveOwner() {
      Player owner = ShadowMonarchManager.getShadowOwnerPlayer(this.shadow);
      return ShadowMonarchManager.isShadowEntity(this.shadow) && owner != null && owner.isAlive();
   }
}
