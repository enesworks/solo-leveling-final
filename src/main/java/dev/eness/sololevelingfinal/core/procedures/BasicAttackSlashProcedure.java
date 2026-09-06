package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.BasicAttackSlashEntity;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class BasicAttackSlashProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, int style, int swingIndex) {
      if (entity instanceof LivingEntity livingEntity) {
         livingEntity.swing(InteractionHand.MAIN_HAND, true);
         float attack = (float)livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
         double strength = TemporaryStatBonusManager.effectiveStrength(entity);

         float damage = switch (style) {
            case 0 -> (float)((3.0 + strength / 18.0) * (swingIndex == 1 ? 1.45 : 1.0));
            default -> attack;
            case 2 -> attack * 0.72F;
            case 3 -> attack * 0.58F;
         };

         float scale = switch (style) {
            case 0 -> swingIndex == 1 ? 1.15F : 1.0F;
            default -> 1.05F;
            case 2 -> 0.9F + swingIndex * 0.06F;
            case 3 -> 0.98F + swingIndex * 0.05F;
         };
         BasicAttackSlashEntity.spawn(world, livingEntity, style, swingIndex, damage, scale);
         playSound(world, x, y, z, style);
      }
   }

   private static void playSound(LevelAccessor world, double x, double y, double z, int style) {
      if (world instanceof Level level) {
         boolean fist = style == 0;
         ResourceLocation sound = fist ? new ResourceLocation("sololeveling:impact1") : new ResourceLocation("sololeveling:basic_slash");
         float volume = fist ? 0.55F : 0.5F;
         float pitch = fist ? 0.82F : Mth.nextFloat(level.getRandom(), 1.35F, 1.65F);
         if (!level.isClientSide()) {
            level.playSound((Player)null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(sound), SoundSource.NEUTRAL, volume, pitch);
         } else {
            level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(sound), SoundSource.NEUTRAL, volume, pitch, false);
         }
      }
   }
}
