package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class DKnight1Entity extends Monster implements RangedAttackMob {
   public DKnight1Entity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.D_KNIGHT_1.get(), world);
   }

   public DKnight1Entity(EntityType<DKnight1Entity> type, Level world) {
      super(type, world);
      this.setMaxUpStep(0.6F);
      this.xpReward = 5;
      this.setNoAi(false);
      this.setPersistenceRequired();
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
         }
      });
      this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(4, new HurtByTargetGoal(this));
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(6, new FloatGoal(this));
      this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 12.0F) {
         @Override
         public boolean canContinueToUse() {
            return this.canUse();
         }
      });
   }

   @Override
   public MobType getMobType() {
      return MobType.UNDEAD;
   }

   @Override
   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   @Override
   public double getMyRidingOffset() {
      return -0.35;
   }

   @Override
   public SoundEvent getHurtSound(DamageSource ds) {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
   }

   @Override
   public boolean hurt(DamageSource damagesource, float amount) {
      if (damagesource.is(DamageTypes.CACTUS)) {
         return false;
      } else {
         return damagesource.is(DamageTypes.DROWN) ? false : super.hurt(damagesource, amount);
      }
   }

   @Override
   public void performRangedAttack(LivingEntity target, float flval) {
      Arrow entityarrow = new Arrow(this.level(), this);
      double d0 = target.getY() + target.getEyeHeight() - 1.1;
      double d1 = target.getX() - this.getX();
      double d3 = target.getZ() - this.getZ();
      entityarrow.shoot(d1, d0 - entityarrow.getY() + Math.sqrt(d1 * d1 + d3 * d3) * 0.2F, d3, 1.6F, 12.0F);
      this.level().addFreshEntity(entityarrow);
   }

   @Override
   public boolean canBreatheUnderwater() {
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      Level world = entity.level();
      return true;
   }

   public static void init() {
   }

   public static Builder createAttributes() {
      Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.2);
      builder = builder.add(Attributes.MAX_HEALTH, 12.0);
      builder = builder.add(Attributes.ARMOR, 0.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 3.0);
      return builder.add(Attributes.FOLLOW_RANGE, 32.0);
   }
}
