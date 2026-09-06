package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

public class HunterOnInitialEntitySpawnProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double rnk = 0.0;
         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_Eyes, Mth.nextInt(RandomSource.create(), 1, 8));
         }

         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_TopIn, Mth.nextInt(RandomSource.create(), 1, 4));
         }

         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_TopOut, Mth.nextInt(RandomSource.create(), 1, 15));
         }

         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_Foot, Mth.nextInt(RandomSource.create(), 1, 4));
         }

         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_EyeBs, Mth.nextInt(RandomSource.create(), 1, 2));
         }

         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_Hair, Mth.nextInt(RandomSource.create(), 1, 8));
         }

         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_Mouth, Mth.nextInt(RandomSource.create(), 1, 2));
         }

         if (entity instanceof HunterEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(HunterEntity.DATA_Bottom, Mth.nextInt(RandomSource.create(), 1, 5));
         }

         if (Math.random() < 0.25) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_Rank, "C");
            }

            rnk = 2.0;
         } else if (Math.random() < 0.25) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_Rank, "B");
            }

            rnk = 3.0;
         } else if (Math.random() < 0.25) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_Rank, "A");
            }

            rnk = 4.0;
         } else if (Math.random() < 0.25) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_Rank, "S");
            }

            rnk = 4.25;
         } else {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_Rank, "D");
            }

            rnk = 1.0;
         }

         if (Math.random() < 0.16666667F) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_HunterClass, "Assassin");
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_assassin")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            if (Math.random() < 0.33333334F && entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_assassin")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.OFF_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            ((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0 + rnk * 1.25);
            ((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0 + rnk * 5.0);
            ((LivingEntity)entity).getAttribute(Attributes.ARMOR).setBaseValue(rnk * 4.0);
            ((LivingEntity)entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25 + rnk * 0.1);
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }
         } else if (Math.random() < 0.2F) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_HunterClass, "Mage");
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_mage_healer")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.OFF_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            ((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0 + rnk * 10.0);
            ((LivingEntity)entity).getAttribute(Attributes.ARMOR).setBaseValue(rnk * 5.0);
            ((LivingEntity)entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3 + rnk * 0.025);
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }
         } else if (Math.random() < 0.25) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_HunterClass, "Fighter");
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_fighter")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_fighter_tanker_off")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.OFF_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            ((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0 + rnk * 1.75);
            ((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0 + rnk * 10.0);
            ((LivingEntity)entity).getAttribute(Attributes.ARMOR).setBaseValue(rnk * 5.0);
            ((LivingEntity)entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3 + rnk * 0.05);
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }
         } else if (Math.random() < 0.33333334F) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_HunterClass, "Tanker");
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_tanker")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_fighter_tanker_off")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.OFF_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            ((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0 + rnk * 1.0);
            ((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0 + rnk * 15.0);
            ((LivingEntity)entity).getAttribute(Attributes.ARMOR).setBaseValue(rnk * 8.0);
            ((LivingEntity)entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3 + rnk * 0.02);
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }
         } else if (Math.random() < 0.5) {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_HunterClass, "Ranger");
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_ranger")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            ((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0 + rnk * 12.0);
            ((LivingEntity)entity).getAttribute(Attributes.ARMOR).setBaseValue(rnk * 8.0);
            ((LivingEntity)entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3 + rnk * 0.04);
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }
         } else {
            if (entity instanceof HunterEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(HunterEntity.DATA_HunterClass, "Healer");
            }

            if (entity instanceof LivingEntity _entity) {
               ItemStack _setstack = new ItemStack(
                  ForgeRegistries.ITEMS
                     .tags()
                     .getTag(ItemTags.create(new ResourceLocation("hunter_mage_healer")))
                     .getRandomElement(RandomSource.create())
                     .orElseGet(() -> Items.AIR)
               );
               _setstack.setCount(1);
               _entity.setItemInHand(InteractionHand.OFF_HAND, _setstack);
               if (_entity instanceof Player _player) {
                  _player.getInventory().setChanged();
               }
            }

            ((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0 + rnk * 7.0);
            ((LivingEntity)entity).getAttribute(Attributes.ARMOR).setBaseValue(rnk * 5.0);
            ((LivingEntity)entity).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3 + rnk * 0.01);
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }
         }

         entity.getPersistentData().putDouble("int", rnk * 12.0);
      }
   }
}
