package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.SlashProjectileHitsBlockProcedure;
import dev.eness.sololevelingfinal.core.procedures.SlashProjectileHitsLivingEntityProcedure;
import dev.eness.sololevelingfinal.core.procedures.SlashWhileProjectileFlyingTickProcedure;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class Slash3Entity extends AbstractArrow implements ItemSupplier {
   public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);

   public Slash3Entity(SpawnEntity packet, Level world) {
      super(SololevelingModEntities.SLASH_3.get(), world);
   }

   public Slash3Entity(EntityType<? extends Slash3Entity> type, Level world) {
      super(type, world);
   }

   public Slash3Entity(EntityType<? extends Slash3Entity> type, double x, double y, double z, Level world) {
      super(type, x, y, z, world);
   }

   public Slash3Entity(EntityType<? extends Slash3Entity> type, LivingEntity entity, Level world) {
      super(type, entity, world);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ItemStack getItem() {
      return PROJECTILE_ITEM;
   }

   @Override
   protected ItemStack getPickupItem() {
      return PROJECTILE_ITEM;
   }

   @Override
   protected void doPostHurtEffects(LivingEntity entity) {
      super.doPostHurtEffects(entity);
      entity.setArrowCount(entity.getArrowCount() - 1);
   }

   @Override
   public void onHitEntity(EntityHitResult entityHitResult) {
      super.onHitEntity(entityHitResult);
      SlashProjectileHitsLivingEntityProcedure.execute(this);
   }

   @Override
   public void onHitBlock(BlockHitResult blockHitResult) {
      super.onHitBlock(blockHitResult);
      SlashProjectileHitsBlockProcedure.execute(this);
   }

   @Override
   public void tick() {
      super.tick();
      SlashWhileProjectileFlyingTickProcedure.execute(this.getOwner(), this);
      if (this.inGround) {
         this.discard();
      }
   }

   public static Slash3Entity shoot(Level world, LivingEntity entity, RandomSource source) {
      return shoot(world, entity, source, 1.0F, 6.0, 1);
   }

   public static Slash3Entity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
      Slash3Entity entityarrow = new Slash3Entity(SololevelingModEntities.SLASH_3.get(), entity, world);
      entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
      entityarrow.setSilent(true);
      entityarrow.setCritArrow(false);
      entityarrow.setBaseDamage(damage);
      entityarrow.setKnockback(knockback);
      world.addFreshEntity(entityarrow);
      world.playSound(
         null,
         entity.getX(),
         entity.getY(),
         entity.getZ(),
         ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:slash")),
         SoundSource.PLAYERS,
         1.0F,
         1.0F / (random.nextFloat() * 0.5F + 1.0F) + power / 2.0F
      );
      return entityarrow;
   }

   public static Slash3Entity shoot(LivingEntity entity, LivingEntity target) {
      Slash3Entity entityarrow = new Slash3Entity(SololevelingModEntities.SLASH_3.get(), entity, entity.level());
      double dx = target.getX() - entity.getX();
      double dy = target.getY() + target.getEyeHeight() - 1.1;
      double dz = target.getZ() - entity.getZ();
      entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 2.0F, 12.0F);
      entityarrow.setSilent(true);
      entityarrow.setBaseDamage(6.0);
      entityarrow.setKnockback(1);
      entityarrow.setCritArrow(false);
      entity.level().addFreshEntity(entityarrow);
      entity.level()
         .playSound(
            null,
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:slash")),
            SoundSource.PLAYERS,
            1.0F,
            1.0F / (RandomSource.create().nextFloat() * 0.5F + 1.0F)
         );
      return entityarrow;
   }
}
