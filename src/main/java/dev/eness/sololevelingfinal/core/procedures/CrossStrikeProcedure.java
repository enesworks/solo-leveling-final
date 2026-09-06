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
import dev.eness.sololevelingfinal.core.entity.CrossStrikeEntity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class CrossStrikeProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         if (!(world instanceof Level level && level.isClientSide())) {
            livingEntity.swing(InteractionHand.MAIN_HAND, true);
            float attack = (float)livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
            double strength = TemporaryStatBonusManager.effectiveStrength(entity);
            float damage = (float)(attack * 0.85F + 4.0 + strength / 40.0);
            CooldownManager.set(entity, "Cross Strike", 200);
            CrossStrikeEntity.spawn(world, livingEntity, damage, 1.0F);
            playSlashSound(world, x, y, z);
         }
      }
   }

   private static void playSlashSound(LevelAccessor world, double x, double y, double z) {
      if (world instanceof Level level) {
         float pitch = Mth.nextFloat(level.getRandom(), 1.22F, 1.45F);
         level.playSound(
            (Player)null,
            BlockPos.containing(x, y, z),
            ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:basic_slash")),
            SoundSource.NEUTRAL,
            0.75F,
            pitch
         );
      }
   }
}
