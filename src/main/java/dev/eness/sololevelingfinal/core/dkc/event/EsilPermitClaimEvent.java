package dev.eness.sololevelingfinal.core.dkc.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import dev.eness.sololevelingfinal.core.entity.EsilRadiruEntity;

@Cancelable
public final class EsilPermitClaimEvent extends Event {
   private final EsilRadiruEntity esil;
   private final ServerPlayer player;
   private EsilPermitClaimEvent.Decision decision = EsilPermitClaimEvent.Decision.PASS;

   public EsilPermitClaimEvent(EsilRadiruEntity esil, ServerPlayer player) {
      this.esil = esil;
      this.player = player;
   }

   public EsilRadiruEntity esil() {
      return this.esil;
   }

   public ServerPlayer player() {
      return this.player;
   }

   public EsilPermitClaimEvent.Decision decision() {
      return this.decision;
   }

   public void grantPermit() {
      this.decision = EsilPermitClaimEvent.Decision.GRANT;
   }

   public void deny() {
      this.decision = EsilPermitClaimEvent.Decision.DENY;
   }

   public enum Decision {
      PASS,
      DENY,
      GRANT;
   }
}
