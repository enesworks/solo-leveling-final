package dev.eness.sololevelingfinal.core.procedures;

import java.util.Optional;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;

public class ElderBeastOnInitialEntitySpawnProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (world.getLevelData().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            world.getLevelData().getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(false, world.getServer());
            Entity _ent = entity;
            if (!_ent.level().isClientSide() && _ent.getServer() != null) {
               Optional<CommandFunction> _fopt = _ent.getServer().getFunctions().get(new ResourceLocation("mod:circle"));
               if (_fopt.isPresent()) {
                  _ent.getServer().getFunctions().execute(_fopt.get(), _ent.createCommandSourceStack());
               }
            }

            world.getLevelData().getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(true, world.getServer());
         } else {
            Entity _ent = entity;
            if (!_ent.level().isClientSide() && _ent.getServer() != null) {
               Optional<CommandFunction> _fopt = _ent.getServer().getFunctions().get(new ResourceLocation("mod:circle"));
               if (_fopt.isPresent()) {
                  _ent.getServer().getFunctions().execute(_fopt.get(), _ent.createCommandSourceStack());
               }
            }
         }
      }
   }
}
