package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.util.ArcaneMageSpellManager;
import dev.eness.sololevelingfinal.core.util.BarrierMageSpellManager;
import dev.eness.sololevelingfinal.core.util.FireMageSpellManager;
import dev.eness.sololevelingfinal.core.util.StormMageSpellManager;

public class RandomHunterMageTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double Rank = 0.0;
         double rand = 0.0;
         double dmg_modifier = 0.0;
         if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("S")) {
            dmg_modifier = 20.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("A")) {
            dmg_modifier = 14.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("B")) {
            dmg_modifier = 10.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("C")) {
            dmg_modifier = 6.0;
         } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("D")) {
            dmg_modifier = 5.0;
         }

         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            HunterAIHelper.casterBacklineTick(entity);
            entity.lookAt(
               Anchor.EYES,
               new Vec3(
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY()
                     + (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getBbHeight(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
               )
            );
            if ((entity instanceof HunterEntity _datEntI ? _datEntI.getEntityData().get(HunterEntity.DATA_IA) : 0) <= 45) {
               if (entity instanceof HunterEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(HunterEntity.DATA_IA, (entity instanceof HunterEntity _datEntI ? _datEntI.getEntityData().get(HunterEntity.DATA_IA) : 0) + 1);
               }

               if ((entity instanceof HunterEntity _datEntI ? _datEntI.getEntityData().get(HunterEntity.DATA_IA) : 0) == 30
                  && dmg_modifier >= 10.0
                  && (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) instanceof LivingEntity _livEnt38
                  && _livEnt38.hasEffect(MobEffects.INVISIBILITY)) {
                  DetectEyeSpawnProcedure.execute(world, x, y, z, entity);
               }

               if ((entity instanceof HunterEntity _datEntI ? _datEntI.getEntityData().get(HunterEntity.DATA_IA) : 0) == 40) {
                  int stage = FireMageSpellManager.outputStage(entity);
                  if (stage > 0) {
                     Entity combatTarget = entity instanceof Mob mob ? mob.getTarget() : null;
                     if (combatTarget == null) {
                        return;
                     }

                     entity.lookAt(Anchor.EYES, new Vec3(combatTarget.getX(), combatTarget.getY() + combatTarget.getBbHeight() * 0.5, combatTarget.getZ()));
                     rand = Mth.nextInt(RandomSource.create(), 1, 100);
                     String specialization = entity.getPersistentData().getString("sl_hunter_mage_specialization");
                     if (specialization.isBlank()) {
                        specialization = switch (entity.level().getRandom().nextInt(4)) {
                           case 1 -> "barrier";
                           case 2 -> "arcane";
                           case 3 -> "storm";
                           default -> "fire";
                        };
                        entity.getPersistentData().putString("sl_hunter_mage_specialization", specialization);
                     }

                     if ("storm".equals(specialization)) {
                        String stormSpell;
                        if (stage == 1) {
                           stormSpell = "Static Needle";
                        } else if (stage == 2) {
                           stormSpell = rand <= 52.0 ? "Static Needle" : (rand <= 78.0 ? "Thunderclap" : "Slipstream");
                        } else if (stage == 3) {
                           stormSpell = rand <= 32.0 ? "Static Needle" : (rand <= 55.0 ? "Lightning Rod" : (rand <= 78.0 ? "Thunderclap" : "Slipstream"));
                        } else if (stage == 4) {
                           stormSpell = rand <= 34.0
                              ? "Chain Lightning"
                              : (rand <= 56.0 ? "Lightning Rod" : (rand <= 76.0 ? "Thunderclap" : (rand <= 90.0 ? "Static Needle" : "Thunderhead")));
                        } else {
                           stormSpell = rand <= 30.0
                              ? "Chain Lightning"
                              : (
                                 rand <= 46.0
                                    ? "Thunderhead"
                                    : (
                                       rand <= 58.0
                                          ? "Skybreaker"
                                          : (
                                             rand <= 69.0
                                                ? "Lightning Rod"
                                                : (rand <= 82.0 ? "Thunderclap" : (rand <= 93.0 ? "Slipstream" : "Tempest Incarnate"))
                                          )
                                    )
                              );
                        }

                        if (!StormMageSpellManager.castNpc(entity, stormSpell)) {
                           StormMageSpellManager.castNpc(entity, "Static Needle");
                        }

                        return;
                     }

                     if ("arcane".equals(specialization)) {
                        String arcaneSpell;
                        if (stage == 1) {
                           arcaneSpell = "Aether Bolt";
                        } else if (stage == 2) {
                           arcaneSpell = rand <= 48.0 ? "Aether Bolt" : (rand <= 75.0 ? "Vector Step" : "Polarity Sphere");
                        } else if (stage == 3) {
                           arcaneSpell = rand <= 34.0 ? "Aether Bolt" : (rand <= 58.0 ? "Polarity Sphere" : (rand <= 78.0 ? "Runic Relay" : "Vector Step"));
                        } else if (stage == 4) {
                           arcaneSpell = rand <= 24.0
                              ? "Astral Arsenal"
                              : (rand <= 45.0 ? "Dimensional Rend" : (rand <= 67.0 ? "Polarity Sphere" : (rand <= 83.0 ? "Vector Step" : "Aether Bolt")));
                        } else {
                           arcaneSpell = rand <= 8.0
                              ? "Grand Formula: Convergence"
                              : (
                                 rand <= 29.0
                                    ? "Dimensional Rend"
                                    : (rand <= 50.0 ? "Astral Arsenal" : (rand <= 70.0 ? "Polarity Sphere" : (rand <= 86.0 ? "Vector Step" : "Aether Bolt")))
                              );
                        }

                        if (!ArcaneMageSpellManager.castNpc(entity, arcaneSpell)) {
                           ArcaneMageSpellManager.castNpc(entity, "Aether Bolt");
                        }

                        return;
                     }

                     if ("barrier".equals(specialization)) {
                        String barrierSpell;
                        if (stage == 1) {
                           barrierSpell = "Fracture Bolt";
                        } else if (stage == 2) {
                           barrierSpell = rand <= 50.0 ? "Fracture Bolt" : (rand <= 78.0 ? "Repulsion Frame" : "Prism Rampart");
                        } else if (stage == 3) {
                           barrierSpell = rand <= 34.0
                              ? "Fracture Bolt"
                              : (rand <= 59.0 ? "Repulsion Frame" : (rand <= 82.0 ? "Sealing Prism" : "Prism Rampart"));
                        } else if (stage == 4) {
                           barrierSpell = rand <= 23.0
                              ? "Mirror Ward"
                              : (rand <= 45.0 ? "Resonant Collapse" : (rand <= 69.0 ? "Sealing Prism" : (rand <= 86.0 ? "Repulsion Frame" : "Fracture Bolt")));
                        } else {
                           barrierSpell = rand <= 10.0
                              ? "Absolute Bastion"
                              : (
                                 rand <= 30.0
                                    ? "Resonant Collapse"
                                    : (rand <= 49.0 ? "Mirror Ward" : (rand <= 69.0 ? "Sealing Prism" : (rand <= 86.0 ? "Repulsion Frame" : "Fracture Bolt")))
                              );
                        }

                        if (!BarrierMageSpellManager.castNpc(entity, barrierSpell)) {
                           BarrierMageSpellManager.castNpc(entity, "Fracture Bolt");
                        }

                        return;
                     }

                     String spell;
                     if (stage == 1) {
                        spell = rand <= 42.0 ? "Flame Weaving" : "Ignition Orb";
                     } else if (stage == 2) {
                        spell = rand <= 34.0 ? "Ignition Orb" : (rand <= 76.0 ? "Inferno Lance" : "Flashfire");
                     } else if (stage == 3) {
                        spell = rand <= 34.0 ? "Inferno Lance" : (rand <= 62.0 ? "Flashfire" : (rand <= 80.0 ? "Cremation" : "Ignition Orb"));
                     } else if (stage == 4) {
                        spell = rand <= 17.0 ? "Furnace Dominion" : (rand <= 39.0 ? "Cremation" : (rand <= 72.0 ? "Inferno Lance" : "Flashfire"));
                     } else {
                        spell = rand <= 9.0
                           ? "Heavenfall"
                           : (rand <= 28.0 ? "Furnace Dominion" : (rand <= 52.0 ? "Cremation" : (rand <= 80.0 ? "Inferno Lance" : "Flashfire")));
                     }

                     if (!FireMageSpellManager.castNpc(entity, spell) && "Cremation".equals(spell)) {
                        FireMageSpellManager.castNpc(entity, "Inferno Lance");
                     }
                  }
               }
            } else if (entity instanceof HunterEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(HunterEntity.DATA_IA, 0);
            }
         }
      }
   }
}
