package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.SlashEffectEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

@EventBusSubscriber
public class SlashFurryProcProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player.getX(), event.player.getY(), event.player.getZ(), event.player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).slashfur) {
            entity.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .slashfurtimer
               + 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.slashfurtimer = _setval;
               capability.syncPlayerVariables(entity);
            });
            int slashTimer = (int)entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .slashfurtimer;
            if (slashTimer >= 3 && slashTimer <= 31 && slashTimer % 2 == 1) {
               spawnSlash(world, x, y, z, entity, (slashTimer - 3) / 2);
               if (slashTimer == 31) {
                  CooldownManager.set(entity, "Slash Fury", 120);
               }
            }

            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).slashfurtimer
               >= 32.0) {
               boolean _setvalx = false;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.slashfur = _setval;
                  capability.syncPlayerVariables(entity);
               });
               double _setvalxx = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.slashfurtimer = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }
         }
      }
   }

   private static void spawnSlash(LevelAccessor world, double x, double y, double z, Entity entity, int index) {
      if (entity instanceof LivingEntity livingEntity) {
         livingEntity.swing(InteractionHand.MAIN_HAND, true);
         float yawOffset = (index % 5 - 2) * 7.5F;
         float roll = (index % 2 == 0 ? -1.0F : 1.0F) * (28.0F + index % 3 * 12.0F);
         float sequence = Math.min(index, 14) / 14.0F;
         float scale = 0.9F + sequence * 0.85F + index % 3 * 0.05F;
         float damage = (float)(livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 0.16);
         Vec3 look = entity.getLookAngle();
         double forwardDistance = 1.25 + index * 0.28;
         double side = (index % 3 - 1) * (0.22 + sequence * 0.28);
         Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
         double slashX = x + look.x * forwardDistance + right.x * side;
         double slashY = y + 1.2 + sequence * 0.35 + (index % 4 - 1.5) * 0.12;
         double slashZ = z + look.z * forwardDistance + right.z * side;
         SlashEffectEntity.spawn(
            world, livingEntity, slashX, slashY, slashZ, entity.getYRot() + yawOffset, entity.getXRot() * 0.35F, roll, scale, damage, index
         );
         if (world instanceof Level level) {
            if (!level.isClientSide()) {
               level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.attack.sweep")),
                  SoundSource.NEUTRAL,
                  0.8F,
                  0.85F + index % 4 * 0.08F
               );
            } else {
               level.playLocalSound(
                  x,
                  y,
                  z,
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.attack.sweep")),
                  SoundSource.NEUTRAL,
                  0.8F,
                  0.85F + index % 4 * 0.08F,
                  false
               );
            }
         }
      }
   }
}
