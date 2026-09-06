package dev.eness.sololevelingfinal.core.client.aura;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class AuraSmokeField {
   private static final Map<Integer, List<AuraSmokeField.Puff>> PUFFS = new ConcurrentHashMap<>();
   private static final Map<Integer, Vec3> LEAN = new ConcurrentHashMap<>();
   private static final Random RNG = new Random();
   private static final int MAX_PER_ENTITY = 96;
   private static final double EMIT_RANGE_SQR = 3600.0;

   private AuraSmokeField() {
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft minecraft = Minecraft.getInstance();
         if (!minecraft.isPaused()) {
            tick(minecraft);
         }
      }
   }

   private static void tick(Minecraft minecraft) {
      ClientLevel level = minecraft.level;
      if (level == null) {
         PUFFS.clear();
         LEAN.clear();
      } else {
         long time = level.getGameTime();
         Vec3 reference = minecraft.player != null ? minecraft.player.position() : Vec3.ZERO;

         for (Player player : level.players()) {
            int id = player.getId();
            if (player.isSpectator()) {
               PUFFS.remove(id);
               LEAN.remove(id);
            } else {
               List<ClientPlayerAuraManager.AuraInstance> auras = PlayerAuraRenderer.activeAuras(player);
               if (auras.isEmpty()) {
                  PUFFS.remove(id);
                  LEAN.remove(id);
               } else {
                  updateLean(player);
                  if (player.distanceToSqr(reference.x, reference.y, reference.z) <= 3600.0) {
                     List<AuraSmokeField.Puff> list = PUFFS.computeIfAbsent(id, ignored -> new ArrayList<>());

                     for (ClientPlayerAuraManager.AuraInstance instance : auras) {
                        emit(list, player, instance, time);
                     }

                     trim(list);
                  }
               }
            }
         }

         Iterator<Entry<Integer, List<AuraSmokeField.Puff>>> entries = PUFFS.entrySet().iterator();

         while (entries.hasNext()) {
            List<AuraSmokeField.Puff> list = entries.next().getValue();
            Iterator<AuraSmokeField.Puff> puffs = list.iterator();

            while (puffs.hasNext()) {
               AuraSmokeField.Puff puff = puffs.next();
               integrate(puff, time);
               if (puff.age >= puff.maxAge) {
                  puffs.remove();
               }
            }

            if (list.isEmpty()) {
               entries.remove();
            }
         }
      }
   }

   private static void trim(List<AuraSmokeField.Puff> list) {
      while (list.size() > 96) {
         list.remove(0);
      }
   }

   private static void updateLean(Player player) {
      Vec3 velocity = player.getDeltaMovement();
      double targetX = Mth.clamp(-velocity.x * 2.4, -0.5, 0.5);
      double targetZ = Mth.clamp(-velocity.z * 2.4, -0.5, 0.5);
      Vec3 previous = LEAN.getOrDefault(player.getId(), Vec3.ZERO);
      double x = previous.x + (targetX - previous.x) * 0.22;
      double z = previous.z + (targetZ - previous.z) * 0.22;
      LEAN.put(player.getId(), new Vec3(x, 0.0, z));
   }

   private static void emit(List<AuraSmokeField.Puff> list, Player player, ClientPlayerAuraManager.AuraInstance instance, long time) {
      PlayerAuraDefinition definition = PlayerAuraRegistry.get(instance.auraId());
      if (definition != null) {
         float intensity = instance.intensity() * instance.envelope(0.0F, time);
         if (!(intensity <= 0.02F)) {
            boolean shadow = definition.fluid() != null && definition.fluid().style() == PlayerAuraDefinition.FluidStyle.SHADOW_RIFT;
            boolean whiteHair = definition.fluid() != null && definition.fluid().style() == PlayerAuraDefinition.FluidStyle.WHITE_FLAME_HAIR;
            float radius = Math.max(player.getBbWidth() * 0.5F + 0.2F, definition.radius());
            float height = Math.max(1.4F, player.getBbHeight() * definition.heightScale());
            float density = definition.fluid() != null ? definition.fluid().opacity() : 0.7F;
            float rate = (whiteHair ? 4.6F : (shadow ? 2.6F : 2.0F)) + intensity * (whiteHair ? 3.0F : 2.4F);
            int count = Mth.floor(rate);
            if (RNG.nextFloat() < rate - count) {
               count++;
            }

            Vec3 motion = player.getDeltaMovement();

            for (int i = 0; i < count; i++) {
               double angle = RNG.nextDouble() * Math.PI * 2.0;
               double distance = Math.sqrt(RNG.nextDouble()) * radius * (whiteHair ? 0.42 : 0.72);
               double x = player.getX() + Math.cos(angle) * distance;
               double z = player.getZ() + Math.sin(angle) * distance;
               double y = whiteHair
                  ? player.getY() + player.getBbHeight() * (0.78 + RNG.nextDouble() * 0.42)
                  : player.getY() + 0.08 + RNG.nextDouble() * height * 0.9;
               boolean bright = RNG.nextFloat() < (shadow ? 0.2F : 0.3F);
               double inherit = bright ? 0.42 : 0.6;
               AuraSmokeField.Puff puff = new AuraSmokeField.Puff();
               puff.px = puff.ppx = x;
               puff.py = puff.ppy = y;
               puff.pz = puff.ppz = z;
               puff.vx = motion.x * inherit + (RNG.nextDouble() - 0.5) * 0.02;
               puff.vz = motion.z * inherit + (RNG.nextDouble() - 0.5) * 0.02;
               puff.vy = (bright ? 0.03 : 0.018) + RNG.nextDouble() * 0.02 + (whiteHair ? 0.018 : 0.0);
               puff.age = 0.0;
               puff.maxAge = (bright ? 15 + RNG.nextInt(12) : 32 + RNG.nextInt(24)) * (shadow ? 1.25 : 1.0);
               puff.size = bright
                  ? radius * (whiteHair ? 0.14F + RNG.nextFloat() * 0.1F : 0.1F + RNG.nextFloat() * 0.07F)
                  : radius * (whiteHair ? 0.22F + RNG.nextFloat() * 0.28F : 0.3F + RNG.nextFloat() * 0.34F);
               if (definition.smokeColor() >= 0) {
                  puff.color = bright
                     ? mixColor(definition.smokeColor(), 16777215, 0.45F + RNG.nextFloat() * 0.4F)
                     : mixColor(definition.smokeColor(), 16777215, 0.05F + RNG.nextFloat() * 0.4F);
               } else {
                  puff.color = bright
                     ? mixColor(definition.primaryColor(), 16777215, 0.3F + RNG.nextFloat() * 0.25F)
                     : mixColor(definition.secondaryColor(), definition.primaryColor(), 0.28F + RNG.nextFloat() * 0.5F);
               }

               puff.bright = bright;
               puff.hair = whiteHair;
               puff.baseAlpha = Mth.clamp(intensity * density * (bright ? 1.05F : 0.72F), 0.0F, 1.0F);
               puff.seed = RNG.nextInt(16);
               puff.texture = definition.fallbackTexture();
               list.add(puff);
            }
         }
      }
   }

   private static void integrate(AuraSmokeField.Puff puff, long time) {
      puff.ppx = puff.px;
      puff.ppy = puff.py;
      puff.ppz = puff.pz;
      double t = time * 0.12 + puff.seed * 1.3;
      double acceleration = puff.bright ? 7.0E-4 : 0.0013;
      double buoyancy = (puff.bright ? 0.011 : 0.006) + (puff.hair ? 0.006 : 0.0);
      puff.vx = puff.vx + (Math.sin(puff.py * 2.1 + t * 1.6 + puff.seed) * acceleration + Math.sin(puff.pz * 1.7 - t) * acceleration * 0.5);
      puff.vz = puff.vz + (Math.cos(puff.py * 2.3 - t * 1.3 + puff.seed) * acceleration + Math.cos(puff.px * 1.9 + t * 0.8) * acceleration * 0.5);
      puff.vy += buoyancy;
      double drag = puff.bright ? 0.9 : 0.93;
      puff.vx *= drag;
      puff.vy *= drag;
      puff.vz *= drag;
      puff.px = puff.px + puff.vx;
      puff.py = puff.py + puff.vy;
      puff.pz = puff.pz + puff.vz;
      puff.age++;
   }

   private static int mixColor(int a, int b, float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      int r = Math.round(ar + (br - ar) * t);
      int g = Math.round(ag + (bg - ag) * t);
      int bl = Math.round(ab + (bb - ab) * t);
      return r << 16 | g << 8 | bl;
   }

   public static Vec3 lean(int entityId) {
      return LEAN.getOrDefault(entityId, Vec3.ZERO);
   }

   static Map<Integer, List<AuraSmokeField.Puff>> puffs() {
      return PUFFS;
   }

   static final class Puff {
      double px;
      double py;
      double pz;
      double ppx;
      double ppy;
      double ppz;
      double vx;
      double vy;
      double vz;
      double age;
      double maxAge;
      float size;
      float baseAlpha;
      int color;
      boolean bright;
      boolean hair;
      int seed;
      ResourceLocation texture;
   }
}
