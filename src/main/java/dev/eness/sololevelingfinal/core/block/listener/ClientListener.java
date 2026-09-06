package dev.eness.sololevelingfinal.core.block.listener;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.block.renderer.DungeonWallTileRenderer;
import dev.eness.sololevelingfinal.core.block.renderer.HunterRankEvaluatorTileRenderer;
import dev.eness.sololevelingfinal.core.block.renderer.InstanceCoverTileRenderer;
import dev.eness.sololevelingfinal.core.block.renderer.InstanceDungeonKeyLoggerTileRenderer;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlockEntities;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD)
public class ClientListener {
   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public static void registerRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer(SololevelingModBlockEntities.INSTANCE_DUNGEON_KEY_LOGGER.get(), context -> new InstanceDungeonKeyLoggerTileRenderer());
      event.registerBlockEntityRenderer(SololevelingModBlockEntities.INSTANCE_COVER.get(), context -> new InstanceCoverTileRenderer());
      event.registerBlockEntityRenderer(SololevelingModBlockEntities.HUNTER_RANK_EVALUATOR.get(), context -> new HunterRankEvaluatorTileRenderer());
      event.registerBlockEntityRenderer(SololevelingModBlockEntities.DUNGEON_WALL.get(), context -> new DungeonWallTileRenderer());
   }
}
