package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.util.DungeonBuilderMode;
import dev.eness.sololevelingfinal.core.util.StoryModeIntroManager;

@EventBusSubscriber
public class ClassSelectorGiveProcedure {
   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!DungeonBuilderMode.isActive(world)) {
            if (!(entity instanceof ServerPlayer player && StoryModeIntroManager.shouldSuppressClassSelection(player))) {
               if (!(
                  entity instanceof ServerPlayer _plr0
                     && _plr0.level() instanceof ServerLevel
                     && _plr0.getAdvancements()
                        .getOrStartProgress(_plr0.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:awakened")))
                        .isDone()
               )) {
                  if (entity instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:awakened"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }

                  Entity _ent = entity;
                  if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                     _ent.getServer()
                        .getCommands()
                        .performPrefixedCommand(
                           new CommandSourceStack(
                              CommandSource.NULL,
                              _ent.position(),
                              _ent.getRotationVector(),
                              _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                              4,
                              _ent.getName().getString(),
                              _ent.getDisplayName(),
                              _ent.level().getServer(),
                              _ent
                           ),
                           "/title @p title {\"text\":\"Awakened!\",\"color\":\"#FF3184\",\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false}"
                        );
                  }

                  _ent = entity;
                  if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                     _ent.getServer()
                        .getCommands()
                        .performPrefixedCommand(
                           new CommandSourceStack(
                              CommandSource.NULL,
                              _ent.position(),
                              _ent.getRotationVector(),
                              _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                              4,
                              _ent.getName().getString(),
                              _ent.getDisplayName(),
                              _ent.level().getServer(),
                              _ent
                           ),
                           "/title @p subtitle {\"text\":\"Find an Evaluator!\",\"color\":\"#FF0000\",\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false}"
                        );
                  }

                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           0.7F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 0.7F, 1.0F, false
                        );
                     }
                  }
               }
            }
         }
      }
   }
}
