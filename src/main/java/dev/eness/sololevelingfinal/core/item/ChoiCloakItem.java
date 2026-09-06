package dev.eness.sololevelingfinal.core.item;

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
import dev.eness.sololevelingfinal.core.client.model.Modelchoicloak;

public abstract class ChoiCloakItem extends ArmorItem {
   public ChoiCloakItem(Type type, Properties properties) {
      super(new ArmorMaterial() {
         @Override
         public int getDurabilityForType(Type type) {
            return new int[]{13, 15, 16, 11}[type.getSlot().getIndex()] * 100;
         }

         @Override
         public int getDefenseForType(Type type) {
            return new int[]{0, 0, 10, 0}[type.getSlot().getIndex()];
         }

         @Override
         public int getEnchantmentValue() {
            return 9;
         }

         @Override
         public SoundEvent getEquipSound() {
            return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.armor.equip_leather"));
         }

         @Override
         public Ingredient getRepairIngredient() {
            return Ingredient.of();
         }

         @Override
         public String getName() {
            return "choi_cloak";
         }

         @Override
         public float getToughness() {
            return 0.0F;
         }

         @Override
         public float getKnockbackResistance() {
            return 0.0F;
         }
      }, type, properties);
   }

   public static class Chestplate extends ChoiCloakItem {
      public Chestplate() {
         super(Type.CHESTPLATE, new Properties().fireResistant());
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
                           (new Modelchoicloak(Minecraft.getInstance().getEntityModels().bakeLayer(Modelchoicloak.LAYER_LOCATION))).Body,
                           "left_arm",
                           (new Modelchoicloak(Minecraft.getInstance().getEntityModels().bakeLayer(Modelchoicloak.LAYER_LOCATION))).LeftArm,
                           "right_arm",
                           (new Modelchoicloak(Minecraft.getInstance().getEntityModels().bakeLayer(Modelchoicloak.LAYER_LOCATION))).RightArm,
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
         list.add(Component.literal("A Cloak worn by the Mage type S rank Hunter Choi Jong-In"));
         list.add(Component.literal("This cloak boosts players fire damage."));
      }

      @Override
      public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
         return "sololeveling:textures/entities/choicloakye.png";
      }

      @Override
      public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
         return false;
      }
   }
}
