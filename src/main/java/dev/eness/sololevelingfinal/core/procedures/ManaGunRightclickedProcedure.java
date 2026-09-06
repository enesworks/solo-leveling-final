package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.ManaBulletEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.item.ManaGunItem;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class ManaGunRightclickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
      if (entity != null) {
         if (entity instanceof Player _player && !_player.isCreative()) {
            _player.getCooldowns().addCooldown(itemstack.getItem(), 20);
         }

         if (itemstack.getItem() instanceof ManaGunItem) {
            itemstack.getOrCreateTag().putString("geckoAnim", "shoot");
         }

         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.amethyst_block.break")),
                  SoundSource.NEUTRAL,
                  2.0F,
                  0.5F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.amethyst_block.break")), SoundSource.NEUTRAL, 2.0F, 0.5F, false
               );
            }
         }

         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  0.5F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")), SoundSource.NEUTRAL, 1.0F, 0.5F, false
               );
            }
         }

         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
               == 6.0
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
               >= 100.0 + TemporaryStatBonusManager.effectiveIntelligence(entity)) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .MP
               - (100.0 + TemporaryStatBonusManager.effectiveIntelligence(entity));
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
            Entity _shootFrom = entity;
            Level projectileLevel = _shootFrom.level();
            if (!projectileLevel.isClientSide()) {
               Projectile _entityToSpawn = (new Object() {
                  public Projectile getArrow(Level level, Entity shooter, float damage, int knockback, byte piercing) {
                     AbstractArrow entityToSpawn = new ManaBulletEntity(SololevelingModEntities.MANA_BULLET.get(), level);
                     entityToSpawn.setOwner(shooter);
                     entityToSpawn.setBaseDamage(damage);
                     entityToSpawn.setKnockback(knockback);
                     entityToSpawn.setSilent(true);
                     entityToSpawn.setPierceLevel(piercing);
                     return entityToSpawn;
                  }
               }).getArrow(projectileLevel, entity, (float)(3.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 50.0), 0, (byte)1);
               _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
               _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 4.0F, 0.0F);
               projectileLevel.addFreshEntity(_entityToSpawn);
            }
         }
      }
   }
}
