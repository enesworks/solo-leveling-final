package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class KatanaStierRightclickedProcedure {
   public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
      if (entity != null) {
         double x = 0.0;
         double z = 0.0;
         double yaw = 0.0;
         if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.KATANA_STIER.get()) {
            if (entity instanceof Player _player && !_player.isCreative()) {
               _player.getCooldowns().addCooldown(itemstack.getItem(), 100);
            }

            entity.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 99, false, false));
            }

            if (entity instanceof LivingEntity _entity) {
               _entity.swing(InteractionHand.MAIN_HAND, true);
            }

            x = entity.getX();
            z = entity.getZ();
            yaw = entity.getYRot();
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
                     "summon sololeveling:slasheffectsword " + x + " ~ " + z + " {Rotation:[" + yaw + "f,0f]}"
                  );
            }

            Vec3 _center = new Vec3(entity.getX() + 2.0 * entity.getLookAngle().x, entity.getY(), entity.getZ() + 2.0 * entity.getLookAngle().z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.5), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entityiterator != entity
                  && entityiterator instanceof LivingEntity
                  && !entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("side")))) {
                  entityiterator.setDeltaMovement(new Vec3(2.0 * entity.getLookAngle().x, 0.0, 2.0 * entity.getLookAngle().z));
                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), entity),
                     (float)(
                        ((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue()
                           + TemporaryStatBonusManager.effectiveStrength(entity) / 20.0
                     )
                  );
               }
            }
         }
      }
   }
}
