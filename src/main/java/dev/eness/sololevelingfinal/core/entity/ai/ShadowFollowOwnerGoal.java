package dev.eness.sololevelingfinal.core.entity.ai;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public final class ShadowFollowOwnerGoal extends FollowOwnerGoal {
   private final TamableAnimal shadow;

   public ShadowFollowOwnerGoal(TamableAnimal shadow) {
      super(shadow, 1.4, 8.0F, 3.0F, false);
      this.shadow = shadow;
   }

   @Override
   public boolean canUse() {
      return ShadowMonarchManager.shouldFollowOwner(this.shadow) && !this.hasLiveTarget() && super.canUse();
   }

   @Override
   public boolean canContinueToUse() {
      return ShadowMonarchManager.shouldFollowOwner(this.shadow) && !this.hasLiveTarget() && super.canContinueToUse();
   }

   private boolean hasLiveTarget() {
      return this.shadow.getTarget() != null && this.shadow.getTarget().isAlive();
   }
}
