package dev.eness.sololevelingfinal.core.client.aura;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class PlayerAuraRegistry {
   private static final Map<String, PlayerAuraDefinition> DEFINITIONS = new LinkedHashMap<>();
   private static final ResourceLocation GOLD_GLOW = new ResourceLocation("sololeveling", "textures/particle/glow_yellow.png");
   private static final ResourceLocation BLUE_GLOW = new ResourceLocation("sololeveling", "textures/particle/mana_blue.png");
   private static final ResourceLocation PURPLE_GLOW = new ResourceLocation("sololeveling", "textures/particle/aura_glow_purple.png");
   public static final PlayerAuraDefinition GOLIATH = register(
      new PlayerAuraDefinition(
         "goliath_manifestation",
         16774855,
         13202450,
         GOLD_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         0.82F,
         1.35F,
         0.72F,
         0,
         0,
         0,
         new PlayerAuraDefinition.FluidProfile(20, 8, 6, 1.0F, 0.52F, 1.15F, 0.78F),
         false,
         16762178
      )
   );
   public static final PlayerAuraDefinition LIU_DRAGON_SWORDS = register(
      new PlayerAuraDefinition(
         "liu_dragon_swords",
         16774320,
         14060296,
         GOLD_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         0.76F,
         1.46F,
         1.2F,
         0,
         0,
         0,
         new PlayerAuraDefinition.FluidProfile(8, 3, 2, 0.92F, 0.48F, 1.36F, 1.42F),
         false,
         16765257
      )
   );
   public static final PlayerAuraDefinition SUNG_IL_HWAN_SPIRITUALIZATION = register(
      new PlayerAuraDefinition(
         "sung_il_hwan_spiritualization",
         16774069,
         12086024,
         GOLD_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         0.84F,
         1.54F,
         1.48F,
         0,
         0,
         0,
         new PlayerAuraDefinition.FluidProfile(26, 11, 8, 1.08F, 0.68F, 1.68F, 1.72F),
         false,
         16765786
      )
   );
   public static final PlayerAuraDefinition SHADOW_MONARCH_MANIFESTATION = register(
      new PlayerAuraDefinition(
         "shadow_monarch_manifestation",
         12937471,
         1048607,
         PURPLE_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         0.92F,
         1.58F,
         0.94F,
         0,
         0,
         0,
         new PlayerAuraDefinition.FluidProfile(0, 0, 0, 1.12F, 0.92F, 1.72F, 1.12F, PlayerAuraDefinition.FluidStyle.SHADOW_RIFT),
         false
      )
   );
   public static final PlayerAuraDefinition WHITE_FLAME_SPIRITUALIZATION = register(
      new PlayerAuraDefinition(
         "white_flame_spiritualization",
         16777215,
         10345727,
         BLUE_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         0.72F,
         1.48F,
         1.18F,
         0,
         0,
         0,
         new PlayerAuraDefinition.FluidProfile(22, 9, 6, 0.92F, 0.74F, 1.42F, 1.28F, PlayerAuraDefinition.FluidStyle.WHITE_FLAME_HAIR),
         false,
         15399935
      )
   );
   public static final PlayerAuraDefinition WHITE_FLAME_DOPPELGANGER = register(
      new PlayerAuraDefinition(
         "white_flame_doppelganger",
         16777215,
         12378111,
         BLUE_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         1.0F,
         1.28F,
         1.6F,
         1,
         9,
         0,
         null,
         false,
         15596031
      )
   );
   public static final PlayerAuraDefinition BEAST_WHITE_FANG = register(
      new PlayerAuraDefinition(
         "beast_white_fang",
         16775399,
         10424346,
         GOLD_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         0.9F,
         1.52F,
         1.34F,
         0,
         0,
         0,
         new PlayerAuraDefinition.FluidProfile(24, 10, 7, 1.12F, 0.68F, 1.58F, 1.46F, PlayerAuraDefinition.FluidStyle.LIQUID_FLAME),
         false,
         14209989
      )
   );
   public static final PlayerAuraDefinition ANTARES_MANIFESTATION = register(
      new PlayerAuraDefinition(
         "antares_manifestation",
         16724804,
         1442055,
         PURPLE_GLOW,
         PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
         0.96F,
         1.62F,
         1.08F,
         0,
         0,
         0,
         new PlayerAuraDefinition.FluidProfile(24, 10, 7, 1.16F, 0.72F, 1.52F, 1.22F, PlayerAuraDefinition.FluidStyle.LIQUID_FLAME),
         false,
         3867913
      )
   );
   public static final PlayerAuraDefinition RULER_BLUE = register(
      new PlayerAuraDefinition("ruler_blue", 15137535, 1478143, BLUE_GLOW, PlayerAuraDefinition.Facing.CAMERA, 0.76F, 1.12F, 1.0F, 2, 7, 5, null, true)
   );
   public static final PlayerAuraDefinition SHADOW_FLOW = register(
      new PlayerAuraDefinition(
         "shadow_flow", 14264319, 4329600, PURPLE_GLOW, PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA, 0.78F, 1.22F, 0.68F, 2, 10, 4, null, true
      )
   );

   private PlayerAuraRegistry() {
   }

   public static synchronized PlayerAuraDefinition register(PlayerAuraDefinition definition) {
      if (DEFINITIONS.putIfAbsent(definition.id(), definition) != null) {
         throw new IllegalArgumentException("Duplicate player aura id: " + definition.id());
      } else {
         return definition;
      }
   }

   public static PlayerAuraDefinition get(String id) {
      return DEFINITIONS.get(id);
   }

   public static Collection<PlayerAuraDefinition> values() {
      return Collections.unmodifiableCollection(DEFINITIONS.values());
   }
}
