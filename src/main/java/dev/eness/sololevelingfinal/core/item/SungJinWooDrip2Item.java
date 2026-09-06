package dev.eness.sololevelingfinal.core.item;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import dev.eness.sololevelingfinal.core.client.model.Modeljinwoochest2;
import dev.eness.sololevelingfinal.core.client.model.Modeljinwoolegs2;

public abstract class SungJinWooDrip2Item extends ArmorItem {
   public SungJinWooDrip2Item(Type type, Properties properties) {
      super(new ArmorMaterial() {
         @Override
         public int getDurabilityForType(Type type) {
            return new int[]{13, 15, 16, 11}[type.getSlot().getIndex()] * 0;
         }

         @Override
         public int getDefenseForType(Type type) {
            return new int[]{3, 6, 7, 3}[type.getSlot().getIndex()];
         }

         @Override
         public int getEnchantmentValue() {
            return 9;
         }

         @Override
         public SoundEvent getEquipSound() {
            return SoundEvents.EMPTY;
         }

         @Override
         public Ingredient getRepairIngredient() {
            return Ingredient.of();
         }

         @Override
         public String getName() {
            return "sung_jin_woo_drip_2";
         }

         @Override
         public float getToughness() {
            return 0.5F;
         }

         @Override
         public float getKnockbackResistance() {
            return 0.0F;
         }
      }, type, properties);
   }

   public static class Chestplate extends SungJinWooDrip2Item {
      public Chestplate() {
         super(Type.CHESTPLATE, new Properties());
      }

      @Override
      public void initializeClient(Consumer<IClientItemExtensions> consumer) {
         consumer.accept(
            new IClientItemExtensions() {
               @OnlyIn(Dist.CLIENT)
               @Override
               public HumanoidModel getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                  HumanoidModel armorModel = new HumanoidModel(
                     new ModelPart(
                        Collections.emptyList(),
                        Map.of(
                           "body",
                           (new Modeljinwoochest2(Minecraft.getInstance().getEntityModels().bakeLayer(Modeljinwoochest2.LAYER_LOCATION))).Body,
                           "left_arm",
                           (new Modeljinwoochest2(Minecraft.getInstance().getEntityModels().bakeLayer(Modeljinwoochest2.LAYER_LOCATION))).LeftArm,
                           "right_arm",
                           (new Modeljinwoochest2(Minecraft.getInstance().getEntityModels().bakeLayer(Modeljinwoochest2.LAYER_LOCATION))).RightArm,
                           "head",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "hat",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "right_leg",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "left_leg",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap())
                        )
                     )
                  );
                  armorModel.crouching = living.isShiftKeyDown();
                  armorModel.riding = defaultModel.riding;
                  armorModel.young = living.isBaby();
                  return armorModel;
               }
            }
         );
      }

      @Override
      public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
         super.appendHoverText(itemstack, world, list, flag);
      }

      @Override
      public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
         return "sololeveling:textures/entities/jinwoo2text.png";
      }

      @Override
      public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
         return false;
      }
   }

   public static class Leggings extends SungJinWooDrip2Item {
      public Leggings() {
         super(Type.LEGGINGS, new Properties());
      }

      @Override
      public void initializeClient(Consumer<IClientItemExtensions> consumer) {
         consumer.accept(
            new IClientItemExtensions() {
               @OnlyIn(Dist.CLIENT)
               @Override
               public HumanoidModel getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                  HumanoidModel armorModel = new HumanoidModel(
                     new ModelPart(
                        Collections.emptyList(),
                        Map.of(
                           "left_leg",
                           (new Modeljinwoolegs2(Minecraft.getInstance().getEntityModels().bakeLayer(Modeljinwoolegs2.LAYER_LOCATION))).RightLeg,
                           "right_leg",
                           (new Modeljinwoolegs2(Minecraft.getInstance().getEntityModels().bakeLayer(Modeljinwoolegs2.LAYER_LOCATION))).RightLeg,
                           "head",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "hat",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "body",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "right_arm",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "left_arm",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap())
                        )
                     )
                  );
                  armorModel.crouching = living.isShiftKeyDown();
                  armorModel.riding = defaultModel.riding;
                  armorModel.young = living.isBaby();
                  return armorModel;
               }
            }
         );
      }

      @Override
      public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
         super.appendHoverText(itemstack, world, list, flag);
      }

      @Override
      public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
         return "sololeveling:textures/entities/jinwoo2text.png";
      }

      @Override
      public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
         return false;
      }
   }
}
