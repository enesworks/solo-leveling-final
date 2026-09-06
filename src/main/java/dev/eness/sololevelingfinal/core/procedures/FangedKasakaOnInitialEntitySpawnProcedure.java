package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.FxspikEntity;

public class FangedKasakaOnInitialEntitySpawnProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putDouble("SideX", entity.getX());
         entity.getPersistentData().putDouble("SideZ", entity.getZ());
         if (entity instanceof FxspikEntity) {
            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entity instanceof LivingEntity _entity) {
                  _entity.hurt(
                     new DamageSource(_entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)) {
                        @Override
                        public Component getLocalizedDeathMessage(LivingEntity _msgEntity) {
                           String _translatekey = "death.attack.tail";
                           if (this.getEntity() == null && this.getDirectEntity() == null) {
                              return _msgEntity.getKillCredit() != null
                                 ? Component.translatable(_translatekey + ".player", _msgEntity.getDisplayName(), _msgEntity.getKillCredit().getDisplayName())
                                 : Component.translatable(_translatekey, _msgEntity.getDisplayName());
                           }

                           Component _component = this.getEntity() == null ? this.getDirectEntity().getDisplayName() : this.getEntity().getDisplayName();
                           ItemStack _itemstack = ItemStack.EMPTY;
                           if (this.getEntity() instanceof LivingEntity _livingentity) {
                              _itemstack = _livingentity.getMainHandItem();
                           }

                           return !_itemstack.isEmpty() && _itemstack.hasCustomHoverName()
                              ? Component.translatable(_translatekey + ".item", _msgEntity.getDisplayName(), _component, _itemstack.getDisplayName())
                              : Component.translatable(_translatekey, _msgEntity.getDisplayName(), _component);
                        }
                     },
                     6.0F
                  );
               }
            }
         }
      }
   }
}
