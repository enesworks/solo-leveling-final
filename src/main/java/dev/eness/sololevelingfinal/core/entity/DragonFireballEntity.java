package dev.eness.sololevelingfinal.core.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.procedures.DragonFireballProjectileHitsBlockProcedure;
import dev.eness.sololevelingfinal.core.procedures.DragonFireballProjectileHitsLivingEntityProcedure;
import dev.eness.sololevelingfinal.core.procedures.DragonFireballProjectileHitsPlayerProcedure;
import dev.eness.sololevelingfinal.core.procedures.DragonFireballWhileProjectileFlyingTickProcedure;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class DragonFireballEntity extends AbstractArrow implements ItemSupplier {
   public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);

   public DragonFireballEntity(SpawnEntity packet, Level world) {
      super(SololevelingModEntities.DRAGON_FIREBALL.get(), world);
   }

   public DragonFireballEntity(EntityType<? extends DragonFireballEntity> type, Level world) {
      super(type, world);
   }

   public DragonFireballEntity(EntityType<? extends DragonFireballEntity> type, double x, double y, double z, Level world) {
      super(type, x, y, z, world);
   }

   public DragonFireballEntity(EntityType<? extends DragonFireballEntity> type, LivingEntity entity, Level world) {
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
   public void playerTouch(Player entity) {
      super.playerTouch(entity);
      DragonFireballProjectileHitsPlayerProcedure.execute(this.level(), this);
   }

   @Override
   public void onHitEntity(EntityHitResult entityHitResult) {
      super.onHitEntity(entityHitResult);
      DragonFireballProjectileHitsLivingEntityProcedure.execute(this.level(), this);
   }

   @Override
   public void onHitBlock(BlockHitResult blockHitResult) {
      super.onHitBlock(blockHitResult);
      DragonFireballProjectileHitsBlockProcedure.execute(this);
   }

   @Override
   public void tick() {
      super.tick();
      DragonFireballWhileProjectileFlyingTickProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
      if (this.inGround) {
         this.discard();
      }
   }

   public static DragonFireballEntity shoot(Level world, LivingEntity entity, RandomSource source) {
      return shoot(world, entity, source, 1.0F, 10.0, 0);
   }

   public static DragonFireballEntity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
      DragonFireballEntity entityarrow = new DragonFireballEntity(SololevelingModEntities.DRAGON_FIREBALL.get(), entity, world);
      entityarrow.shoot(entity.getViewVector(1.0F).x, entity.getViewVector(1.0F).y, entity.getViewVector(1.0F).z, power * 2.0F, 0.0F);
      entityarrow.setSilent(true);
      entityarrow.setCritArrow(false);
      entityarrow.setBaseDamage(damage);
      entityarrow.setKnockback(knockback);
      world.addFreshEntity(entityarrow);
      return entityarrow;
   }

   public static DragonFireballEntity shoot(LivingEntity entity, LivingEntity target) {
      DragonFireballEntity entityarrow = new DragonFireballEntity(SololevelingModEntities.DRAGON_FIREBALL.get(), entity, entity.level());
      double dx = target.getX() - entity.getX();
      double dy = target.getY() + target.getEyeHeight() - 1.1;
      double dz = target.getZ() - entity.getZ();
      entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 2.0F, 12.0F);
      entityarrow.setSilent(true);
      entityarrow.setBaseDamage(10.0);
      entityarrow.setKnockback(0);
      entityarrow.setCritArrow(false);
      entity.level().addFreshEntity(entityarrow);
      return entityarrow;
   }
}
