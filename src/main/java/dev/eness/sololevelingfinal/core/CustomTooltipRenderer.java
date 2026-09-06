package dev.eness.sololevelingfinal.core;

import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent.Color;
import net.minecraftforge.client.event.RenderTooltipEvent.Pre;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.client.gui.WeaponTooltipProfiles;
import dev.eness.sololevelingfinal.core.client.gui.WeaponTooltipRenderer;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public class CustomTooltipRenderer {
   private static final String MODID = "sololeveling";
   private static final TagKey<Item> WEAPONS_TAG = TagKey.create(Registries.ITEM, new ResourceLocation("sololeveling", "weapons"));
   private static final Set<String> WEAPON_NAME_KEYS = Set.of(
      "sword", "long_sword", "longsword", "dagger", "katana", "axe", "war_axe", "hammer", "spear", "bow", "griamore", "grimoire", "gun", "wrath", "killer"
   );
   private static final boolean ANIMATE = true;
   private static final float PULSE_SPEED = 5.5E-4F;
   private static final float PULSE_MIN = 0.96F;
   private static final float PULSE_MAX = 1.0F;
   private static final float H_CYAN = 0.54F;
   private static final float H_BLUE = 0.6F;
   private static final float H_INDIGO = 0.72F;
   private static final float H_PURPLE = 0.78F;
   private static final float H_RED = 0.03F;
   private static final float H_GOLD = 0.11F;
   private static final float H_GREEN = 0.33F;
   private static final float H_ICE = 0.58F;
   private static final float SAT_LOW = 0.2F;
   private static final float SAT_BORDER = 0.35F;
   private static final float V_DARK1 = 0.22F;
   private static final float V_DARK2 = 0.16F;
   private static final float V_BORDER = 0.34F;

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onTooltipPre(Pre event) {
      WeaponTooltipProfiles.Profile profile = WeaponTooltipProfiles.find(event.getItemStack());
      if (profile != null) {
         WeaponTooltipRenderer.render(event, profile);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public static void onTooltipColor(Color event) {
      ItemStack stack = event.getItemStack();
      if (!stack.isEmpty() && isModItem(stack)) {
         if (WeaponTooltipProfiles.find(stack) != null) {
            event.setBackground(0);
            event.setBorderStart(0);
            event.setBorderEnd(0);
         } else {
            String name = getPath(stack.getItem()).toLowerCase(Locale.ROOT);
            if (name.equals("redkey")) {
               themeSoloDualBorder(event, 0.78F, 0.25F, 0.17F, 0.08F, 0.97F, 0.4725F, 0.38F, 0.11F, 0.35F, 0.34F);
            } else if (isModWeapon(stack)) {
               Rarity rarity = stack.getRarity();
               if (name.contains("shadow") || name.contains("monarch") || name.contains("igris") || name.contains("beru")) {
                  themeSolo(event, 0.72F, 0.2F, 0.22F, 0.16F, 0.54F, 0.35F, 0.34F);
               } else if (name.contains("kamish")) {
                  themeSolo(event, 0.03F, 0.23F, 0.24F, 0.18F, 0.11F, 0.315F, 0.32F);
               } else if (name.contains("frost") || name.contains("ice")) {
                  themeSolo(event, 0.58F, 0.17999999F, 0.24F, 0.18F, 0.54F, 0.35F, 0.34F);
               } else if (name.contains("emerald")) {
                  themeSolo(event, 0.33F, 0.21F, 0.22F, 0.16F, 0.4F, 0.28F, 0.3F);
               } else if (name.contains("demon")) {
                  themeSolo(event, 0.97F, 0.22000001F, 0.2F, 0.14F, 0.03F, 0.315F, 0.3F);
               } else if (name.contains("spirit_bow")
                  || name.contains("bow")
                  || name.contains("mana_gun")
                  || name.contains("griamore")
                  || name.contains("grimoire")) {
                  themeSolo(event, 0.6F, 0.2F, 0.22F, 0.16F, 0.54F, 0.35F, 0.34F);
               } else if (name.contains("axe") || name.contains("hammer")) {
                  themeSolo(event, 0.64F, 0.17999999F, 0.2F, 0.14F, 0.6F, 0.28F, 0.3F);
               } else if (name.contains("mythic")
                  || name.contains("s_tier")
                  || name.contains("stier")
                  || name.contains("knight_killer")
                  || name.contains("igrislongsword")) {
                  themeSolo(event, 0.78F, 0.2F, 0.22F, 0.16F, 0.54F, 0.35F, 0.34F);
               } else if (rarity == Rarity.EPIC) {
                  themeSolo(event, 0.78F, 0.2F, 0.22F, 0.16F, 0.54F, 0.35F, 0.34F);
               } else if (rarity == Rarity.RARE) {
                  themeSolo(event, 0.6F, 0.2F, 0.22F, 0.16F, 0.54F, 0.35F, 0.34F);
               } else if (!name.contains("spear") && !name.contains("katana") && !name.contains("dagger") && !name.contains("sword")) {
                  themeSolo(event, 0.64F, 0.17999999F, 0.2F, 0.14F, 0.6F, 0.28F, 0.3F);
               } else {
                  themeSolo(event, 0.6F, 0.2F, 0.22F, 0.16F, 0.54F, 0.35F, 0.34F);
               }
            }
         }
      }
   }

   private static boolean isModItem(ItemStack stack) {
      ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return key != null && "sololeveling".equals(key.getNamespace());
   }

   private static boolean isModWeapon(ItemStack stack) {
      Item item = stack.getItem();
      if (item instanceof BlockItem) {
         return false;
      }

      if (item instanceof ArmorItem) {
         return false;
      }

      if (item instanceof ForgeSpawnEggItem) {
         return false;
      }

      ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
      if (key == null || !"sololeveling".equals(key.getNamespace())) {
         return false;
      }

      if (stack.is(WEAPONS_TAG)) {
         return true;
      }

      if (!(item instanceof SwordItem)
         && !(item instanceof AxeItem)
         && !(item instanceof BowItem)
         && !(item instanceof CrossbowItem)
         && !(item instanceof TridentItem)) {
         String path = key.getPath().toLowerCase(Locale.ROOT);

         for (String s : WEAPON_NAME_KEYS) {
            if (path.contains(s)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static String getPath(Item item) {
      ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
      return key == null ? "" : key.getPath();
   }

   private static void themeSolo(Color event, float bgHue, float bgSat, float vStart, float vEnd, float borderHue, float borderSat, float borderV) {
      float pulse = easePulse(0.96F, 1.0F, 5.5E-4F);
      int bgStart = withAlpha(java.awt.Color.HSBtoRGB(bgHue, clamp01(bgSat), clamp01(vStart * pulse)));
      int bgEnd = withAlpha(java.awt.Color.HSBtoRGB(bgHue, clamp01(bgSat * 0.9F), clamp01(vEnd)));
      int border = withAlpha(java.awt.Color.HSBtoRGB(borderHue, clamp01(borderSat), clamp01(borderV * pulse)));
      event.setBackgroundStart(bgStart);
      event.setBackgroundEnd(bgEnd);
      event.setBorderStart(border);
      event.setBorderEnd(border);
   }

   private static void themeSoloDualBorder(
      Color event,
      float bgHue,
      float bgSat,
      float vStart,
      float vEnd,
      float borderStartHue,
      float borderStartSat,
      float borderStartV,
      float borderEndHue,
      float borderEndSat,
      float borderEndV
   ) {
      float pulse = easePulse(0.96F, 1.0F, 7.7E-4F);
      int bgStart = withAlpha(java.awt.Color.HSBtoRGB(bgHue, clamp01(bgSat), clamp01(vStart * pulse)));
      int bgEnd = withAlpha(java.awt.Color.HSBtoRGB(bgHue + 0.04F, clamp01(bgSat * 1.15F), clamp01(vEnd)));
      int borderStart = withAlpha(java.awt.Color.HSBtoRGB(borderStartHue, clamp01(borderStartSat), clamp01(borderStartV * pulse)));
      int borderEnd = withAlpha(java.awt.Color.HSBtoRGB(borderEndHue, clamp01(borderEndSat), clamp01(borderEndV * pulse)));
      event.setBackgroundStart(bgStart);
      event.setBackgroundEnd(bgEnd);
      event.setBorderStart(borderStart);
      event.setBorderEnd(borderEnd);
   }

   private static float easePulse(float min, float max, float speed) {
      double s = Math.sin((float)System.currentTimeMillis() * speed);
      float t = (float)((s + 1.0) * 0.5);
      return min + (max - min) * t;
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : Math.min(1.0F, v);
   }

   private static int withAlpha(int rgb) {
      return rgb | 0xFF000000;
   }
}
