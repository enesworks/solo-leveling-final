package dev.eness.sololevelingfinal.core.item;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.client.model.Modelshabots;
import dev.eness.sololevelingfinal.core.client.model.Modelshaces;
import dev.eness.sololevelingfinal.core.client.model.Modelshahed;
import dev.eness.sololevelingfinal.core.client.model.Modelshalegs;
import dev.eness.sololevelingfinal.core.procedures.ShadowARMORHelmetTickEventProcedure;

public abstract class ShadowARMORItem extends ArmorItem {
   public ShadowARMORItem(Type type, Properties properties) {
      super(new ArmorMaterial() {
         @Override
         public int getDurabilityForType(Type type) {
            return new int[]{13, 15, 16, 11}[type.getSlot().getIndex()] * 70;
         }

         @Override
         public int getDefenseForType(Type type) {
            return new int[]{2, 12, 16, 6}[type.getSlot().getIndex()];
         }

         @Override
         public int getEnchantmentValue() {
            return 0;
         }

         @Override
         public SoundEvent getEquipSound() {
            return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.hurt"));
         }

         @Override
         public Ingredient getRepairIngredient() {
            return Ingredient.of();
         }

         @Override
         public String getName() {
            return "shadow_armor";
         }

         @Override
         public float getToughness() {
            return 4.0F;
         }

         @Override
         public float getKnockbackResistance() {
            return 0.1F;
         }
      }, type, properties);
   }

   public static class Boots extends ShadowARMORItem {
      public Boots() {
         super(Type.BOOTS, new Properties());
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
                           (new Modelshabots(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshabots.LAYER_LOCATION))).RightLeg,
                           "right_leg",
                           (new Modelshabots(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshabots.LAYER_LOCATION))).RightLeg,
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
         return "sololeveling:textures/entities/shaarm.png";
      }

      @Override
      public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
         return false;
      }

      @Override
      public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
         super.inventoryTick(itemstack, world, entity, slot, selected);
         if (entity instanceof Player player && Iterables.contains(player.getArmorSlots(), itemstack)) {
            ShadowARMORHelmetTickEventProcedure.execute(world, entity, itemstack);
         }
      }
   }

   public static class Chestplate extends ShadowARMORItem {
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
                           (new Modelshaces(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshaces.LAYER_LOCATION))).Body,
                           "left_arm",
                           (new Modelshaces(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshaces.LAYER_LOCATION))).LeftArm,
                           "right_arm",
                           (new Modelshaces(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshaces.LAYER_LOCATION))).RightArm,
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
         return "sololeveling:textures/entities/shaarm.png";
      }

      @Override
      public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
         return false;
      }

      @Override
      public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
         super.inventoryTick(itemstack, world, entity, slot, selected);
         if (entity instanceof Player player && Iterables.contains(player.getArmorSlots(), itemstack)) {
            ShadowARMORHelmetTickEventProcedure.execute(world, entity, itemstack);
         }
      }
   }

   public static class Helmet extends ShadowARMORItem {
      public Helmet() {
         super(Type.HELMET, new Properties());
      }

      @Override
      public void initializeClient(Consumer<IClientItemExtensions> consumer) {
         consumer.accept(
            new IClientItemExtensions() {
               @Override
               public HumanoidModel getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
                  HumanoidModel armorModel = new HumanoidModel(
                     new ModelPart(
                        Collections.emptyList(),
                        Map.of(
                           "head",
                           (new Modelshahed(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshahed.LAYER_LOCATION))).Head,
                           "hat",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "body",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "right_arm",
                           new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                           "left_arm",
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
         return "sololeveling:textures/entities/shaarm.png";
      }

      @Override
      public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
         return false;
      }

      @Override
      public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
         super.inventoryTick(itemstack, world, entity, slot, selected);
         if (entity instanceof Player player && Iterables.contains(player.getArmorSlots(), itemstack)) {
            ShadowARMORHelmetTickEventProcedure.execute(world, entity, itemstack);
         }
      }
   }

   public static class Leggings extends ShadowARMORItem {
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
                           (new Modelshalegs(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshalegs.LAYER_LOCATION))).RightLeg,
                           "right_leg",
                           (new Modelshalegs(Minecraft.getInstance().getEntityModels().bakeLayer(Modelshalegs.LAYER_LOCATION))).RightLeg,
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
         return "sololeveling:textures/entities/shaarm.png";
      }

      @Override
      public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
         return false;
      }

      @Override
      public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
         super.inventoryTick(itemstack, world, entity, slot, selected);
         if (entity instanceof Player player && Iterables.contains(player.getArmorSlots(), itemstack)) {
            ShadowARMORHelmetTickEventProcedure.execute(world, entity, itemstack);
         }
      }
   }
}
