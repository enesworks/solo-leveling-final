package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

@EventBusSubscriber
public class JobAdvPointGainProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof DKnight1Entity || event.getEntity() instanceof DKnight2Entity || event.getEntity() instanceof DKnight3Entity) {
         Entity creditedSource = ShadowKillCreditHelper.creditedSourceForDeath(
            event.getEntity().level(), event.getEntity(), event.getSource().getEntity(), event.getSource().getDirectEntity()
         );
         ServerPlayer player = ShadowKillCreditHelper.creditedServerPlayer(event.getEntity().level(), creditedSource);
         if (player != null) {
            JobChangeQuestManager.grantAdvancementPoint(player, event.getEntity());
         }
      }
   }

   public static void execute(Entity entity, Entity sourceentity) {
      if (entity != null) {
         ServerPlayer player = ShadowKillCreditHelper.creditedServerPlayer(entity.level(), sourceentity);
         if (player != null && (entity instanceof DKnight1Entity || entity instanceof DKnight2Entity || entity instanceof DKnight3Entity)) {
            JobChangeQuestManager.grantAdvancementPoint(player, entity);
         }
      }
   }
}
