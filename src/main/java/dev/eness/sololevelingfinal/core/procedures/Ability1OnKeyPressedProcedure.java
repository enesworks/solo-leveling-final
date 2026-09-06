package dev.eness.sololevelingfinal.core.procedures;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.BeastMonarchManager;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.FrostMonarchManager;
import dev.eness.sololevelingfinal.core.util.GoliathCombatManager;
import dev.eness.sololevelingfinal.core.util.LiuZhigangCombatManager;
import dev.eness.sololevelingfinal.core.world.inventory.FireGriamoreMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowSummonGUIMenu;

public class Ability1OnKeyPressedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (FrostMonarchManager.isDirectAbilityMode(entity)) {
            FrostMonarchManager.castFlashFreeze(entity);
         } else if (BeastMonarchManager.isFangStance(entity)) {
            BeastMonarchManager.beginPredatorsIntercept(entity);
         } else if (GoliathCombatManager.isCombatStance(entity)) {
            GoliathCombatManager.beginPursuit(entity);
         } else if (LiuZhigangCombatManager.isCombatStance(entity)) {
            LiuZhigangCombatManager.beginDragonFlash(entity);
         } else {
            Entity shadow = null;
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).combatmode
               )
             {
               DaggerRushActProcedure.execute(world, x, y, z, entity);
            } else if (entity.level().dimension() != ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:survival_dimension"))) {
               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 1.0) {
                  if (entity instanceof ServerPlayer _ent) {
                     final BlockPos _bpos = BlockPos.containing(x, y, z);
                     NetworkHooks.openScreen(_ent, new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                           return Component.literal("ShadowSummonGUI");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                           return new ShadowSummonGUIMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                        }
                     }, _bpos);
                  }
               } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .JOB
                  == 2.0) {
                  if (entity.isShiftKeyDown()) {
                     if (entity instanceof ServerPlayer _ent) {
                        final BlockPos _bpos = BlockPos.containing(x, y, z);
                        NetworkHooks.openScreen(_ent, new MenuProvider() {
                           @Override
                           public Component getDisplayName() {
                              return Component.literal("FireGriamore");
                           }

                           @Override
                           public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                              return new FireGriamoreMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                           }
                        }, _bpos);
                     }
                  } else {
                     if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.USING_FIRE.get(), 999, 1, false, false));
                     }

                     CooldownManager.set(entity, "mana_refresh", 999);
                  }
               } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 4.0
                  && !CooldownManager.isOnCooldown(entity, "job_1")) {
                  boolean _setval = true;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.monarchbeam = _setval;
                     capability.syncPlayerVariables(entity);
                  });
               }
            } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("Your Job Abilities are Disabled in this dimension!"), true);
            }
         }
      }
   }
}
