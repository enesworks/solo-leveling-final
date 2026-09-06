package dev.eness.sololevelingfinal.core.init;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.DkcQuestProgressClientState;
import dev.eness.sololevelingfinal.core.client.gui.FrostArchitectureClientState;
import dev.eness.sololevelingfinal.core.client.gui.dungeonbuilder.DungeonBuilderStudioClient;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemPanelScreen;
import dev.eness.sololevelingfinal.core.network.AbilitiesGUIButtonMessage;
import dev.eness.sololevelingfinal.core.network.Ability1Message;
import dev.eness.sololevelingfinal.core.network.Ability2Message;
import dev.eness.sololevelingfinal.core.network.Ability3Message;
import dev.eness.sololevelingfinal.core.network.Ability4Message;
import dev.eness.sololevelingfinal.core.network.DMessage;
import dev.eness.sololevelingfinal.core.network.QuestInfoMessage;
import dev.eness.sololevelingfinal.core.network.SkillCycleButtonMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.TripleJumpMessage;
import dev.eness.sololevelingfinal.core.network.UseSkillMessage;
import dev.eness.sololevelingfinal.core.util.DungeonBuilderMode;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class SololevelingModKeyMappings {
   public static final KeyMapping OPEN_PANEL = new KeyMapping("key.sololeveling.open_panel", 78, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.screen == null) {
               if (DungeonBuilderMode.isActive(mc.level)) {
                  DungeonBuilderStudioClient.requestOpen();
               } else if (SystemPlayerAccess.hasSystem(mc.player)) {
                  mc.setScreen(new SystemPanelScreen());
               } else {
                  BlockPos pos = mc.player.blockPosition();
                  SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(5, pos.getX(), pos.getY(), pos.getZ()));
               }
            }
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping SKILL_CYCLE_BUTTON = new KeyMapping("key.sololeveling.skill_cycle_button", 82, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new SkillCycleButtonMessage(0, 0));
            SkillCycleButtonMessage.pressAction(Minecraft.getInstance().player, 0, 0);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping USE_SKILL = new KeyMapping("key.sololeveling.use_skill", 90, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new UseSkillMessage(0, 0));
            UseSkillMessage.pressAction(Minecraft.getInstance().player, 0, 0);
            SololevelingModKeyMappings.USE_SKILL_LASTPRESS = System.currentTimeMillis();
         } else if (this.isDownOld != isDown && !isDown) {
            int dt = (int)(System.currentTimeMillis() - SololevelingModKeyMappings.USE_SKILL_LASTPRESS);
            SololevelingMod.PACKET_HANDLER.sendToServer(new UseSkillMessage(1, dt));
            UseSkillMessage.pressAction(Minecraft.getInstance().player, 1, dt);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping D = new KeyMapping("key.sololeveling.d", Type.MOUSE, 1, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new DMessage(0, 0));
            DMessage.pressAction(Minecraft.getInstance().player, 0, 0);
            SololevelingModKeyMappings.D_LASTPRESS = System.currentTimeMillis();
         } else if (this.isDownOld != isDown && !isDown) {
            int dt = (int)(System.currentTimeMillis() - SololevelingModKeyMappings.D_LASTPRESS);
            SololevelingMod.PACKET_HANDLER.sendToServer(new DMessage(1, dt));
            DMessage.pressAction(Minecraft.getInstance().player, 1, dt);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping ABILITY_1 = new KeyMapping("key.sololeveling.ability_1", 88, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            if (SololevelingModKeyMappings.canUseAbilityKeys()) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new Ability1Message(0, 0));
               Ability1Message.pressAction(Minecraft.getInstance().player, 0, 0);
               SololevelingModKeyMappings.ABILITY_1_LASTPRESS = System.currentTimeMillis();
            }
         } else if (this.isDownOld != isDown && !isDown && SololevelingModKeyMappings.canUseAbilityKeys()) {
            int dt = (int)(System.currentTimeMillis() - SololevelingModKeyMappings.ABILITY_1_LASTPRESS);
            SololevelingMod.PACKET_HANDLER.sendToServer(new Ability1Message(1, dt));
            Ability1Message.pressAction(Minecraft.getInstance().player, 1, dt);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping ABILITY_2 = new KeyMapping("key.sololeveling.ability_2", 67, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            if (SololevelingModKeyMappings.canUseAbilityKeys()) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new Ability2Message(0, 0));
               SololevelingModKeyMappings.ABILITY_2_LASTPRESS = System.currentTimeMillis();
            }
         } else if (this.isDownOld != isDown && !isDown && SololevelingModKeyMappings.canUseAbilityKeys()) {
            int dt = (int)Math.min(2147483647L, System.currentTimeMillis() - SololevelingModKeyMappings.ABILITY_2_LASTPRESS);
            SololevelingMod.PACKET_HANDLER.sendToServer(new Ability2Message(1, dt));
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping ABILITY_3 = new KeyMapping("key.sololeveling.ability_3", 86, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown && SololevelingModKeyMappings.canUseAbilityKeys()) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new Ability3Message(0, 0));
            Ability3Message.pressAction(Minecraft.getInstance().player, 0, 0);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping ABILITY_4 = new KeyMapping("key.sololeveling.ability_4", 66, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown && SololevelingModKeyMappings.canUseAbilityKeys()) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new Ability4Message(0, 0));
            Ability4Message.pressAction(Minecraft.getInstance().player, 0, 0);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping TRIPLE_JUMP = new KeyMapping("key.sololeveling.triple_jump", 32, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            Player player = Minecraft.getInstance().player;
            Vec3 motion = player != null ? player.getDeltaMovement() : Vec3.ZERO;
            SololevelingMod.PACKET_HANDLER.sendToServer(new TripleJumpMessage(0, 0, motion.x, motion.z));
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_1 = new KeyMapping("key.sololeveling.ab_1", 49, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(1);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(1);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_2 = new KeyMapping("key.sololeveling.ab_2", 50, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(2);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(2);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_3 = new KeyMapping("key.sololeveling.ab_3", 51, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(3);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(3);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_4 = new KeyMapping("key.sololeveling.ab_4", 52, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(4);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(4);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_5 = new KeyMapping("key.sololeveling.ab_5", 53, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(5);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(5);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_6 = new KeyMapping("key.sololeveling.ab_6", 54, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(6);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(6);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping QUEST_INFO = new KeyMapping("key.sololeveling.quest_info", 258, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new QuestInfoMessage(0, 0));
            QuestInfoMessage.pressAction(Minecraft.getInstance().player, 0, 0);
            SololevelingModKeyMappings.QUEST_INFO_LASTPRESS = System.currentTimeMillis();
         } else if (this.isDownOld != isDown && !isDown) {
            int dt = (int)(System.currentTimeMillis() - SololevelingModKeyMappings.QUEST_INFO_LASTPRESS);
            SololevelingMod.PACKET_HANDLER.sendToServer(new QuestInfoMessage(1, dt));
            QuestInfoMessage.pressAction(Minecraft.getInstance().player, 1, dt);
            DkcQuestProgressClientState.clear();
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_7 = new KeyMapping("key.sololeveling.ab_7", 55, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(7);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(7);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping AB_8 = new KeyMapping("key.sololeveling.ab_8", 56, "key.categories.sololeveling") {
      private boolean isDownOld = false;

      @Override
      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            SololevelingModKeyMappings.pressHotbarSkill(8);
         } else if (this.isDownOld != isDown && !isDown) {
            SololevelingModKeyMappings.releaseHotbarSkill(8);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping DDD = new KeyMapping("key.sololeveling.ddd", Type.MOUSE, 0, "key.categories.misc");
   private static long USE_SKILL_LASTPRESS = 0L;
   private static long D_LASTPRESS = 0L;
   private static long ABILITY_1_LASTPRESS = 0L;
   private static long ABILITY_2_LASTPRESS = 0L;
   private static long QUEST_INFO_LASTPRESS = 0L;
   private static final long[] HOTBAR_LASTPRESS = new long[8];

   private static void pressHotbarSkill(int slot) {
      int type = 9 + slot;
      HOTBAR_LASTPRESS[slot - 1] = System.currentTimeMillis();
      SololevelingMod.PACKET_HANDLER.sendToServer(new UseSkillMessage(type, 0));
      UseSkillMessage.pressAction(Minecraft.getInstance().player, type, 0);
   }

   private static void releaseHotbarSkill(int slot) {
      int type = 19 + slot;
      int dt = (int)(System.currentTimeMillis() - HOTBAR_LASTPRESS[slot - 1]);
      FrostArchitectureClientState.releaseAndSend();
      SololevelingMod.PACKET_HANDLER.sendToServer(new UseSkillMessage(type, dt));
      UseSkillMessage.pressAction(Minecraft.getInstance().player, type, dt);
   }

   private static boolean canUseAbilityKeys() {
      if (Minecraft.getInstance().player == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables variables = Minecraft.getInstance()
         .player
         .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      return variables.combatmode || (int)variables.JOB == 3;
   }

   @SubscribeEvent
   public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
      event.register(OPEN_PANEL);
      event.register(SKILL_CYCLE_BUTTON);
      event.register(USE_SKILL);
      event.register(D);
      event.register(ABILITY_1);
      event.register(ABILITY_2);
      event.register(ABILITY_3);
      event.register(ABILITY_4);
      event.register(TRIPLE_JUMP);
      event.register(AB_1);
      event.register(AB_2);
      event.register(AB_3);
      event.register(AB_4);
      event.register(AB_5);
      event.register(AB_6);
      event.register(QUEST_INFO);
      event.register(AB_7);
      event.register(AB_8);
      event.register(DDD);
   }

   @EventBusSubscriber(Dist.CLIENT)
   public static class KeyEventListener {
      @SubscribeEvent
      public static void onClientTick(ClientTickEvent event) {
         if (Minecraft.getInstance().screen == null) {
            SololevelingModKeyMappings.OPEN_PANEL.consumeClick();
            SololevelingModKeyMappings.SKILL_CYCLE_BUTTON.consumeClick();
            SololevelingModKeyMappings.USE_SKILL.consumeClick();
            SololevelingModKeyMappings.D.consumeClick();
            SololevelingModKeyMappings.ABILITY_1.consumeClick();
            SololevelingModKeyMappings.ABILITY_2.consumeClick();
            SololevelingModKeyMappings.ABILITY_3.consumeClick();
            SololevelingModKeyMappings.ABILITY_4.consumeClick();
            SololevelingModKeyMappings.TRIPLE_JUMP.consumeClick();
            SololevelingModKeyMappings.AB_1.consumeClick();
            SololevelingModKeyMappings.AB_2.consumeClick();
            SololevelingModKeyMappings.AB_3.consumeClick();
            SololevelingModKeyMappings.AB_4.consumeClick();
            SololevelingModKeyMappings.AB_5.consumeClick();
            SololevelingModKeyMappings.AB_6.consumeClick();
            SololevelingModKeyMappings.QUEST_INFO.consumeClick();
            SololevelingModKeyMappings.AB_7.consumeClick();
            SololevelingModKeyMappings.AB_8.consumeClick();
         }
      }
   }
}
