package dev.eness.sololevelingfinal.core.procedures;

import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.guild.GuildData;
import dev.eness.sololevelingfinal.core.guild.GuildSavedData;

@EventBusSubscriber
public class GuildBossKillProcedure {
   private static final TagKey<EntityType<?>> SOLOBOSS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"));
   private static final Map<String, Integer> DIM_XP = Map.of(
      "dungeon_dimension_d",
      300,
      "dungeon_dimension_snow",
      400,
      "dungeon_dimension_c",
      600,
      "dungeon_dimension_b",
      1200,
      "dungeon_dimension_a",
      2500,
      "dungeon_dimension_s",
      5000
   );
   private static final Set<String> EXCLUDED_DIMS = Set.of(
      "dungeon_dimension_igris", "dungeon_dimension_kasaka", "dungeon_dimension_dkc", "system_void_dimension", "survival_dimension"
   );
   private static final int DEFAULT_XP = 200;

   @SubscribeEvent
   public static void onBossDeath(LivingDeathEvent event) {
      if (!event.getEntity().getPersistentData().getBoolean("slr_dungeon_spawned")) {
         if (event.getEntity().getType().is(SOLOBOSS)) {
            Entity source = event.getSource().getEntity();
            if (source != null) {
               ServerPlayer player = ShadowKillCreditHelper.creditedServerPlayer(event.getEntity().level(), source);
               if (player != null && !player.level().isClientSide()) {
                  GuildSavedData data = GuildSavedData.get(player.serverLevel());
                  GuildData guild = data.getGuildForPlayer(player.getUUID());
                  if (guild != null) {
                     ResourceKey<Level> dim = event.getEntity().level().dimension();
                     String dimPath = dim.location().getPath();
                     if (!EXCLUDED_DIMS.contains(dimPath) && !DkcFloorRegistry.isDkc(dim)) {
                        int xp = DIM_XP.getOrDefault(dimPath, 200);
                        guild.awardXp(xp);
                        guild.totalClears++;
                        data.markDirty();
                        player.sendSystemMessage(Component.literal("§6[Guild] §eBoss cleared! §7Your guild §e" + guild.name + " §7gained §a" + xp + " XP§7."));
                     }
                  }
               }
            }
         }
      }
   }
}
