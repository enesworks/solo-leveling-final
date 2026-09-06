package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public class DKnight2Entity extends Monster {
   public DKnight2Entity(SpawnEntity packet, Level world) {
      this(SololevelingModEntities.D_KNIGHT_2.get(), world);
   }

   public DKnight2Entity(EntityType<DKnight2Entity> type, Level world) {
      super(type, world);
      this.setMaxUpStep(0.6F);
      this.xpReward = 5;
      this.setNoAi(false);
      this.setPersistenceRequired();
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SololevelingModItems.IGRISLONGSWORD.get()));
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
         @Override
         protected double getAttackReachSqr(LivingEntity entity) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
         }
      });
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Slime.class, false, true));
      this.targetSelector.addGoal(5, new HurtByTargetGoal(this).setAlertOthers());
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(7, new FloatGoal(this));
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
   public void playStepSound(BlockPos pos, BlockState blockIn) {
      this.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.iron_golem.step")), 0.15F, 1.0F);
   }

   @Override
   public SoundEvent getHurtSound(DamageSource ds) {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.iron_golem.hurt"));
   }

   @Override
   public SoundEvent getDeathSound() {
      return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.iron_golem.death"));
   }

   @Override
   public boolean hurt(DamageSource damagesource, float amount) {
      if (damagesource.is(DamageTypes.IN_FIRE)) {
         return false;
      } else if (damagesource.is(DamageTypes.CACTUS)) {
         return false;
      } else {
         return damagesource.is(DamageTypes.DROWN) ? false : super.hurt(damagesource, amount);
      }
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
      return builder.add(Attributes.FOLLOW_RANGE, 16.0);
   }
}
