package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.Ab2CooldownProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ab9CooldownProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability1ReturnProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability2ReturnProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability3ReturnProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability4ReturnProcedure;
import dev.eness.sololevelingfinal.core.procedures.AuraAbilityCooldownProcedure;
import dev.eness.sololevelingfinal.core.procedures.CooldownRemainingOnTickProcedure;
import dev.eness.sololevelingfinal.core.procedures.DoesHaveTelekinesisProcedure;
import dev.eness.sololevelingfinal.core.procedures.IsInCombatModeProcedure;
import dev.eness.sololevelingfinal.core.procedures.IsUsingDashProcedure;
import dev.eness.sololevelingfinal.core.procedures.MeleeAbilityCooldownProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnCooldownAmountProcedure;
import dev.eness.sololevelingfinal.core.procedures.SelectedConProcedure;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;
import dev.eness.sololevelingfinal.core.procedures.SkillTextColorProcedure;
import dev.eness.sololevelingfinal.core.procedures.SkillTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.SlectedCon7Procedure;
import dev.eness.sololevelingfinal.core.procedures.SlectedCon8Procedure;
import dev.eness.sololevelingfinal.core.procedures.TelekinesisAbilityCooldownProcedure;
import dev.eness.sololevelingfinal.core.procedures.WeaponAbCooldownSymbolProcedure;
import dev.eness.sololevelingfinal.core.util.OrbOfAvariceManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber(Dist.CLIENT)
public class DisplayOverlay {
   private static final ResourceLocation SKILL_COOLDOWN_COVER = new ResourceLocation("sololeveling:textures/screens/newbasiccdcover.png");

   private static ResourceLocation getSkillTexture(String skillName, boolean avariceHeld) {
      if (ShadowMonarchManager.isFormationSkill(skillName)) {
         return new ResourceLocation("sololeveling:textures/screens/newshadowformation.png");
      }

      return switch (skillName) {
         case "Flame Weaving" -> fireMageTexture("firebullet", avariceHeld);
         case "Ignition Orb" -> fireMageTexture("fireball", avariceHeld);
         case "Inferno Lance" -> fireMageTexture("firelance", avariceHeld);
         case "Flashfire" -> fireMageTexture("firedash", avariceHeld);
         case "Cremation" -> fireMageTexture("cremation", avariceHeld);
         case "Furnace Dominion" -> fireMageTexture("furnace", avariceHeld);
         case "Heavenfall" -> fireMageTexture("meteor", avariceHeld);
         case "Fracture Bolt" -> new ResourceLocation("sololeveling:textures/screens/icon_mage_barrier_fracturebolt.png");
         case "Prism Rampart" -> new ResourceLocation("sololeveling:textures/screens/icon_mage_barrier_prismrampart.png");
         case "Repulsion Frame" -> new ResourceLocation("sololeveling:textures/screens/icon_mage_barrier_repulsionframe.png");
         case "Sealing Prism" -> new ResourceLocation("sololeveling:textures/screens/icon_mage_barrier_sealingprism.png");
         case "Mirror Ward" -> new ResourceLocation("sololeveling:textures/screens/icon_mage_barrier_mirrorward.png");
         case "Resonant Collapse" -> new ResourceLocation("sololeveling:textures/screens/icon_mage_barrier_resonantcollapse.png");
         case "Absolute Bastion" -> new ResourceLocation("sololeveling:textures/screens/icon_mage_barrier_absolutebastion.png");
         case "Aether Bolt" -> new ResourceLocation("sololeveling:textures/screens/icon_magicmissiles.png");
         case "Vector Step" -> new ResourceLocation("sololeveling:textures/screens/icon_shadowstep.png");
         case "Polarity Sphere" -> new ResourceLocation("sololeveling:textures/screens/icon_cursesphere.png");
         case "Runic Relay" -> new ResourceLocation("sololeveling:textures/screens/icon_telekinesis.png");
         case "Astral Arsenal" -> new ResourceLocation("sololeveling:textures/screens/icon_swordbeam.png");
         case "Dimensional Rend" -> new ResourceLocation("sololeveling:textures/screens/icon_slashfury.png");
         case "Grand Formula: Convergence" -> new ResourceLocation("sololeveling:textures/screens/icon_groundslam.png");
         case "Static Needle" -> new ResourceLocation("sololeveling:textures/screens/icon_magicmissiles.png");
         case "Slipstream" -> new ResourceLocation("sololeveling:textures/screens/icon_shadowstep.png");
         case "Thunderclap" -> new ResourceLocation("sololeveling:textures/screens/newbaranstormburst.png");
         case "Lightning Rod" -> new ResourceLocation("sololeveling:textures/screens/icon_telekinesis.png");
         case "Chain Lightning" -> new ResourceLocation("sololeveling:textures/screens/icon_mowf_lightningbreath.png");
         case "Thunderhead" -> new ResourceLocation("sololeveling:textures/screens/newbaranlightningstrike.png");
         case "Skybreaker" -> new ResourceLocation("sololeveling:textures/screens/newbaranlaser.png");
         case "Tempest Incarnate" -> new ResourceLocation("sololeveling:textures/screens/icon_mowf_hellstormdominion.png");
         case "Backstab", "Night Rend" -> new ResourceLocation("sololeveling:textures/screens/icon_backstab.png");
         case "Dualwield" -> new ResourceLocation("sololeveling:textures/screens/icon_dualwielding.png");
         case "Quickslashes", "Flash Cut" -> new ResourceLocation("sololeveling:textures/screens/icon_quickslashes.png");
         case "Dagger Throw" -> new ResourceLocation("sololeveling:textures/screens/icon_dualwielding.png");
         case "Dagger Rush" -> new ResourceLocation("sololeveling:textures/screens/icon_quickslashes.png");
         case "Shadowstep", "Ghost Step" -> new ResourceLocation("sololeveling:textures/screens/icon_shadowstep.png");
         case "Stealth" -> new ResourceLocation("sololeveling:textures/screens/icon_stealth.png");
         case "Murderious Intent" -> new ResourceLocation("sololeveling:textures/screens/icon_murderiousintend.png");
         case "Detection" -> new ResourceLocation("sololeveling:textures/screens/icon_detection.png");
         case "Slash Dash" -> new ResourceLocation("sololeveling:textures/screens/icon_slashdash.png");
         case "Cross Strike", "Critical Strike" -> new ResourceLocation("sololeveling:textures/screens/icon_criticalstrike.png");
         case "Sword of Light" -> new ResourceLocation("sololeveling:textures/screens/icon_swordoflight.png");
         case "Ground Slam" -> new ResourceLocation("sololeveling:textures/screens/icon_groundslam.png");
         case "Sword Dance" -> new ResourceLocation("sololeveling:textures/screens/icon_sworddance.png");
         case "Heal Beam" -> new ResourceLocation("sololeveling:textures/screens/icon_new_healing_beam.png");
         case "Slash Fury" -> new ResourceLocation("sololeveling:textures/screens/icon_slashfury.png");
         case "Blessing Mark" -> new ResourceLocation("sololeveling:textures/screens/icon_new_blessing_mark.png");
         case "Purification" -> new ResourceLocation("sololeveling:textures/screens/icon_purification.png");
         case "Physical Buff" -> new ResourceLocation("sololeveling:textures/screens/icon_physicalbuff.png");
         case "Haste Buff" -> new ResourceLocation("sololeveling:textures/screens/icon_hastebuff.png");
         case "Overheal" -> new ResourceLocation("sololeveling:textures/screens/icon_new_overheal.png");
         case "Tank Leap" -> new ResourceLocation("sololeveling:textures/screens/icon_tankleap.png");
         case "Protection Mark" -> new ResourceLocation("sololeveling:textures/screens/icon_protectionmark.png");
         case "Reinforcement" -> new ResourceLocation("sololeveling:textures/screens/icon_reinforcement.png");
         case "Shield Bash" -> new ResourceLocation("sololeveling:textures/screens/icon_shieldbash.png");
         case "Willpower" -> new ResourceLocation("sololeveling:textures/screens/icon_willpower.png");
         case "Taunt" -> new ResourceLocation("sololeveling:textures/screens/icon_taunt.png");
         case "Sharpshooter" -> new ResourceLocation("sololeveling:textures/screens/icon_sharpshooter.png");
         case "Mana Quiver" -> new ResourceLocation("sololeveling:textures/screens/icon_sharpshooter.png");
         case "Rapid Fire" -> new ResourceLocation("sololeveling:textures/screens/icon_firearrows.png");
         case "Arrow Shower" -> new ResourceLocation("sololeveling:textures/screens/icon_proximitytrap.png");
         case "Proximity Trap" -> new ResourceLocation("sololeveling:textures/screens/icon_proximitytrap.png");
         case "Back Step" -> new ResourceLocation("sololeveling:textures/screens/icon_backstep.png");
         case "High Value Target" -> new ResourceLocation("sololeveling:textures/screens/icon_highvaluetarget.png");
         case "Hawkeye" -> new ResourceLocation("sololeveling:textures/screens/icon_hawkeye.png");
         case "Hyper Focus" -> new ResourceLocation("sololeveling:textures/screens/icon_hyperfocus.png");
         case "Cold Blood" -> new ResourceLocation("sololeveling:textures/screens/icon_murderiousintend.png");
         case "Critical Attack" -> new ResourceLocation("sololeveling:textures/screens/icon_critical_strike.png");
         case "Mutilation" -> new ResourceLocation("sololeveling:textures/screens/icon_mutilation.png");
         case "Sword Beam" -> new ResourceLocation("sololeveling:textures/screens/icon_swordbeam.png");
         case "Arise" -> new ResourceLocation("sololeveling:textures/screens/newshadowarise.png");
         case "Shadow Summon" -> new ResourceLocation("sololeveling:textures/screens/newshadowsummon.png");
         case "Dismiss Shadows" -> new ResourceLocation("sololeveling:textures/screens/newshadowdismiss.png");
         case "Shadow Command" -> new ResourceLocation("sololeveling:textures/screens/icon_shadowcommand.png");
         case "Shadow Exchange" -> new ResourceLocation("sololeveling:textures/screens/newshadowexchange.png");
         case "Shadow Manifestation" -> new ResourceLocation("sololeveling:textures/screens/newshadowarmor.png");
         case "Fire Charge" -> new ResourceLocation("sololeveling:textures/screens/griamorefire.png");
         case "Meteor Rain" -> new ResourceLocation("sololeveling:textures/screens/icon_firearrows.png");
         case "Fireflies" -> new ResourceLocation("sololeveling:textures/screens/newmagesummoning.png");
         case "Ice Spear" -> new ResourceLocation("sololeveling:textures/screens/newfrostmonarchspear.png");
         case "Flash Freeze" -> new ResourceLocation("sololeveling:textures/screens/icon_frost_stillness.png");
         case "Frozen Path" -> new ResourceLocation("sololeveling:textures/screens/icon_frost_causeway.png");
         case "Frozen Architecture" -> new ResourceLocation("sololeveling:textures/screens/newfrostmonarchchunk.png");
         case "Frost Counter" -> new ResourceLocation("sololeveling:textures/screens/icon_frost_winter_remembers.png");
         case "Absolute Zero" -> new ResourceLocation("sololeveling:textures/screens/icon_frost_whiteout.png");
         case "Frost Monarch Spiritualization" -> new ResourceLocation("sololeveling:textures/screens/icon_frost_spiritualization.png");
         case "Monarch Beam" -> new ResourceLocation("sololeveling:textures/screens/newbaranlaser.png");
         case "Lightning Storm" -> new ResourceLocation("sololeveling:textures/screens/newbaranlightningstrike.png");
         case "Storm Burst" -> new ResourceLocation("sololeveling:textures/screens/newbaranstormburst.png");
         case "Lightning Breath" -> new ResourceLocation("sololeveling:textures/screens/icon_mowf_lightningbreath.png");
         case "Hellstorm Dominion" -> new ResourceLocation("sololeveling:textures/screens/icon_mowf_hellstormdominion.png");
         case "Radiru Blood Spear" -> new ResourceLocation("sololeveling:textures/screens/icon_mowf_radirubloodspear.png");
         case "Doppelganger" -> new ResourceLocation("sololeveling:textures/screens/icon_mowf_doppelganger.png");
         case "Hell's Army" -> new ResourceLocation("sololeveling:textures/screens/icon_mowf_hellsarmy.png");
         case "White Flame Spiritualization" -> new ResourceLocation("sololeveling:textures/screens/icon_spiritualize_mowf.png");
         case "Capture" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_1.png");
         case "Power Smash" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_2.png");
         case "Collapse" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_3.png");
         case "Spiritual Body Manifestation" -> new ResourceLocation("sololeveling:textures/screens/icon_spiritualize_goliath.png");
         case "Heavenly Counter" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_1.png");
         case "Golden Dragon Dance" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_2.png");
         case "Sovereign Sword Domain" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_3.png");
         case "Dragon Sword Manifestation" -> new ResourceLocation("sololeveling:textures/screens/icon_spiritualize_goliath.png");
         case "Predator's Presence" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_1.png");
         case "Assassin Stance" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_2.png");
         case "Spatial Execution" -> new ResourceLocation("sololeveling:textures/screens/icon_goliath_3.png");
         case "Spiritualization" -> new ResourceLocation("sololeveling:textures/screens/icon_spiritualize_goliath.png");
         case "Destruction Claw" -> new ResourceLocation("sololeveling:textures/screens/icon_antares_claw.png");
         case "Breath of Destruction" -> new ResourceLocation("sololeveling:textures/screens/icon_antares_breathofdestruction.png");
         case "Monarch's Descent" -> new ResourceLocation("sololeveling:textures/screens/icon_antares_monarchdescend.png");
         case "Sovereign Roar" -> new ResourceLocation("sololeveling:textures/screens/icon_antares_monarchsroar.png");
         case "Extinction" -> new ResourceLocation("sololeveling:textures/screens/icon_antares_extinction.png");
         case "Monarch Manifestation" -> new ResourceLocation("sololeveling:textures/screens/icon_antares_spiritualize.png");
         default -> new ResourceLocation("sololeveling:textures/screens/icon_template.png");
      };
   }

   private static ResourceLocation fireMageTexture(String name, boolean avariceHeld) {
      return new ResourceLocation("sololeveling:textures/screens/icon_mage_fire_" + name + (avariceHeld ? "_orb" : "") + ".png");
   }

   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
      int w = event.getWindow().getGuiScaledWidth();
      int h = event.getWindow().getGuiScaledHeight();
      Level world = null;
      double x = 0.0;
      double y = 0.0;
      double z = 0.0;
      Player entity = Minecraft.getInstance().player;
      if (entity != null) {
         world = entity.level();
         x = entity.getX();
         y = entity.getY();
         z = entity.getZ();
      }

      boolean visible = IsInCombatModeProcedure.execute(entity);
      if (visible) {
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         if (visible) {
            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/icon_melee.png"), w - 24, h - 24, 0.0F, 0.0F, 20, 20, 20, 20);
            if (WeaponAbCooldownSymbolProcedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/newbasiccdcover.png"), w - 24, h - 24, 0.0F, 0.0F, 20, 20, 20, 20);
            }

            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/icon_telekinesis.png"), w - 24, h - 47, 0.0F, 0.0F, 20, 20, 20, 20);
            if (Ab2CooldownProcedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/newbasiccdcover.png"), w - 24, h - 47, 0.0F, 0.0F, 20, 20, 20, 20);
            }

            if (DoesHaveTelekinesisProcedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/newbasicabilitylocked.png"), w - 23, h - 47, 0.0F, 0.0F, 20, 20, 20, 20);
            }

            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/icon_dash.png"), w - 24, h - 70, 0.0F, 0.0F, 20, 20, 20, 20);
            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/icon_aura.png"), w - 24, h - 93, 0.0F, 0.0F, 20, 20, 20, 20);
            if (Ab9CooldownProcedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/newbasiccdcover.png"), w - 24, h - 93, 0.0F, 0.0F, 20, 20, 20, 20);
            }

            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/keyplaceholder.png"), w - 31, h - 89, 0.0F, 0.0F, 12, 12, 12, 12);
            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/keyplaceholder.png"), w - 31, h - 66, 0.0F, 0.0F, 12, 12, 12, 12);
            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/keyplaceholder.png"), w - 31, h - 43, 0.0F, 0.0F, 12, 12, 12, 12);
            event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/keyplaceholder.png"), w - 31, h - 20, 0.0F, 0.0F, 12, 12, 12, 12);
            if (IsUsingDashProcedure.execute(entity)) {
               event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/newbasetick.png"), w - 16, h - 78, 0.0F, 0.0F, 20, 20, 20, 20);
            }

            event.getGuiGraphics()
               .blit(new ResourceLocation("sololeveling:textures/screens/icon_background.png"), w / 2 + -90, h - 22, 0.0F, 0.0F, 162, 22, 162, 22);
            int[] slotXOffsets = new int[]{-89, -69, -49, -29, -9, 11, 31, 51};
            SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables());
            boolean avariceHeld = OrbOfAvariceManager.isHeldBy(entity);

            for (int i = 0; i < 8; i++) {
               String skillName = SkillSlotHelper.getSlot(vars, vars.PskillPage >= 2.0 ? i + 9 : i + 1);
               ResourceLocation icon = getSkillTexture(skillName, avariceHeld);
               int slotX = w / 2 + slotXOffsets[i];
               int slotY = h - 21;
               event.getGuiGraphics().blit(icon, slotX, slotY, 0.0F, 0.0F, 20, 20, 20, 20);
               String cooldownLabel = CooldownRemainingOnTickProcedure.executeForSkill(entity, skillName);
               if (!cooldownLabel.isEmpty()) {
                  event.getGuiGraphics().blit(SKILL_COOLDOWN_COVER, slotX, slotY, 0.0F, 0.0F, 20, 20, 20, 20);
                  int labelX = slotX + (20 - Minecraft.getInstance().font.width(cooldownLabel)) / 2;
                  event.getGuiGraphics().drawString(Minecraft.getInstance().font, cooldownLabel, labelX, slotY + 6, 16777215, true);
               }
            }

            if (SelectedConProcedure.execute(entity, 1)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + -90, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            if (SelectedConProcedure.execute(entity, 2)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + -70, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            if (SelectedConProcedure.execute(entity, 3)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + -50, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            if (SelectedConProcedure.execute(entity, 4)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + -30, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            if (SelectedConProcedure.execute(entity, 5)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + -10, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            if (SelectedConProcedure.execute(entity, 6)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + 10, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            if (SlectedCon7Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + 30, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            if (SlectedCon8Procedure.execute(entity)) {
               event.getGuiGraphics()
                  .blit(new ResourceLocation("sololeveling:textures/screens/icon_frame.png"), w / 2 + 50, h - 22, 0.0F, 0.0F, 22, 22, 22, 22);
            }

            event.getGuiGraphics().drawString(Minecraft.getInstance().font, ReturnCooldownAmountProcedure.execute(entity), w / 2 + 74, h - 12, -26266, false);
            event.getGuiGraphics()
               .drawString(Minecraft.getInstance().font, SkillTextProcedure.execute(entity), w / 2 + 74, h - 22, SkillTextColorProcedure.execute(entity), false);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, Ability3ReturnProcedure.execute(), w - 28, h - 64, -1, false);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, Ability1ReturnProcedure.execute(), w - 28, h - 18, -1, false);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, Ability2ReturnProcedure.execute(), w - 28, h - 41, -1, false);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, Ability4ReturnProcedure.execute(), w - 27, h - 87, -1, false);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, MeleeAbilityCooldownProcedure.execute(entity), w - 18, h - 18, -1, false);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, TelekinesisAbilityCooldownProcedure.execute(entity), w - 18, h - 41, -1, false);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, AuraAbilityCooldownProcedure.execute(entity), w - 18, h - 87, -1, false);
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
