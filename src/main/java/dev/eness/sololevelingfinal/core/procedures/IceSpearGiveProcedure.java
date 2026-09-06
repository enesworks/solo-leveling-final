package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class IceSpearGiveProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double motionZ = 0.0;
         double deltaZ = 0.0;
         double deltaX = 0.0;
         double motionY = 0.0;
         double deltaY = 0.0;
         double motionX = 0.0;
         double speed = 0.0;
         if (!CooldownManager.isOnCooldown(entity, "spear_creation")) {
            if (!(entity instanceof Player _playerHasItem && _playerHasItem.getInventory().contains(new ItemStack(SololevelingModItems.ICE_SPEAR.get())))) {
               CooldownManager.set(entity, "spear_creation", 3000);
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 5, 1.0, 1.0, 1.0, 0.0);
               }

               if (world instanceof Level _level && _level.isClientSide()) {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                  );
               }

               if (entity instanceof Player _player) {
                  ItemStack _setstack = new ItemStack(SololevelingModItems.ICE_SPEAR.get());
                  _setstack.setCount(1);
                  ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal(CooldownManager.getRemainingSeconds(entity, "spear_creation") + " Seconds Left!"), true);
         }

         if (!CooldownManager.isOnCooldown(entity, "job_3")
            && (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.ICE_SPEAR.get()) {
            CooldownManager.set(entity, "job_3", 160);
            if (!world.isClientSide()) {
               entity.getPersistentData().putString("icedash", "move");
            }

            deltaX = -Math.sin(entity.getYRot() / 180.0F * (float) Math.PI);
            deltaY = 0.0;
            deltaZ = Math.cos(entity.getYRot() / 180.0F * (float) Math.PI);
            speed = 3.0;
            motionX = deltaX * speed;
            motionY = 0.0;
            motionZ = deltaZ * speed;
            entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
            SololevelingMod.queueServerWork(10, () -> {
               if (!world.isClientSide()) {
                  entity.getPersistentData().putString("icedash", "");
               }
            });
         }
      }
   }
}
