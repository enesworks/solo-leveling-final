package dev.eness.sololevelingfinal.core.util;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;

@EventBusSubscriber
public final class JobChangeKnightBalance {
   public static final String QUEST_KNIGHT_TAG = "slr_job_change_advancement_knight";
   private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("ec2143c1-231f-4e27-940b-dd8e94bfbf3d");
   private static final double DAMAGE_MULTIPLIER = -0.6;

   private JobChangeKnightBalance() {
   }

   public static void markAndBalance(Entity entity) {
      if (isKnight(entity)) {
         entity.getPersistentData().putBoolean("slr_job_change_advancement_knight", true);
         apply(entity);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      Entity entity = event.getEntity();
      if (!event.getLevel().isClientSide() && entity.getPersistentData().getBoolean("slr_job_change_advancement_knight")) {
         apply(entity);
      }
   }

   private static void apply(Entity entity) {
      if (isKnight(entity)) {
         AttributeInstance attack = ((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE);
         if (attack != null) {
            if (attack.getModifier(DAMAGE_MODIFIER_ID) != null) {
               attack.removeModifier(DAMAGE_MODIFIER_ID);
            }

            attack.addPermanentModifier(new AttributeModifier(DAMAGE_MODIFIER_ID, "SLR Job Change knight damage tuning", -0.6, Operation.MULTIPLY_TOTAL));
         }
      }
   }

   private static boolean isKnight(Entity entity) {
      return entity instanceof DKnight1Entity || entity instanceof DKnight2Entity || entity instanceof DKnight3Entity;
   }
}
