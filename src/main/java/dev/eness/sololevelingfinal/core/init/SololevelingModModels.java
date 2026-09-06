package dev.eness.sololevelingfinal.core.init;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.client.model.ModelFlameArrow;
import dev.eness.sololevelingfinal.core.client.model.ModelSlash;
import dev.eness.sololevelingfinal.core.client.model.ModelSlash2;
import dev.eness.sololevelingfinal.core.client.model.ModelSlash3;
import dev.eness.sololevelingfinal.core.client.model.ModelSlash4;
import dev.eness.sololevelingfinal.core.client.model.ModelSlash5;
import dev.eness.sololevelingfinal.core.client.model.ModelSlash6;
import dev.eness.sololevelingfinal.core.client.model.Modelchoicloak;
import dev.eness.sololevelingfinal.core.client.model.Modelgoliathchest;
import dev.eness.sololevelingfinal.core.client.model.Modelgoliathfeet;
import dev.eness.sololevelingfinal.core.client.model.Modelgoliathhelm;
import dev.eness.sololevelingfinal.core.client.model.Modelgoliathlegs;
import dev.eness.sololevelingfinal.core.client.model.Modelicecle;
import dev.eness.sololevelingfinal.core.client.model.Modelinv;
import dev.eness.sololevelingfinal.core.client.model.Modeljinwoochest1;
import dev.eness.sololevelingfinal.core.client.model.Modeljinwoochest2;
import dev.eness.sololevelingfinal.core.client.model.Modeljinwoolegs1;
import dev.eness.sololevelingfinal.core.client.model.Modeljinwoolegs2;
import dev.eness.sololevelingfinal.core.client.model.Modelkang;
import dev.eness.sololevelingfinal.core.client.model.Modelkangtaeshik;
import dev.eness.sololevelingfinal.core.client.model.Modelkangtaeshikhair;
import dev.eness.sololevelingfinal.core.client.model.Modellight_ball;
import dev.eness.sololevelingfinal.core.client.model.Modelshabots;
import dev.eness.sololevelingfinal.core.client.model.Modelshaces;
import dev.eness.sololevelingfinal.core.client.model.Modelshadowfeet;
import dev.eness.sololevelingfinal.core.client.model.Modelshadowhead;
import dev.eness.sololevelingfinal.core.client.model.Modelshadowlegs;
import dev.eness.sololevelingfinal.core.client.model.Modelshadowsoul;
import dev.eness.sololevelingfinal.core.client.model.Modelshadowtorso;
import dev.eness.sololevelingfinal.core.client.model.Modelshahed;
import dev.eness.sololevelingfinal.core.client.model.Modelshalegs;
import dev.eness.sololevelingfinal.core.client.model.Modelshardparticle_Converted;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class SololevelingModModels {
   @SubscribeEvent
   public static void registerLayerDefinitions(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(Modelshadowlegs.LAYER_LOCATION, Modelshadowlegs::createBodyLayer);
      event.registerLayerDefinition(Modelshardparticle_Converted.LAYER_LOCATION, Modelshardparticle_Converted::createBodyLayer);
      event.registerLayerDefinition(ModelSlash6.LAYER_LOCATION, ModelSlash6::createBodyLayer);
      event.registerLayerDefinition(Modelgoliathhelm.LAYER_LOCATION, Modelgoliathhelm::createBodyLayer);
      event.registerLayerDefinition(Modelgoliathchest.LAYER_LOCATION, Modelgoliathchest::createBodyLayer);
      event.registerLayerDefinition(Modelgoliathlegs.LAYER_LOCATION, Modelgoliathlegs::createBodyLayer);
      event.registerLayerDefinition(Modelgoliathfeet.LAYER_LOCATION, Modelgoliathfeet::createBodyLayer);
      event.registerLayerDefinition(Modelshalegs.LAYER_LOCATION, Modelshalegs::createBodyLayer);
      event.registerLayerDefinition(Modelshadowsoul.LAYER_LOCATION, Modelshadowsoul::createBodyLayer);
      event.registerLayerDefinition(ModelSlash4.LAYER_LOCATION, ModelSlash4::createBodyLayer);
      event.registerLayerDefinition(Modeljinwoochest2.LAYER_LOCATION, Modeljinwoochest2::createBodyLayer);
      event.registerLayerDefinition(Modelicecle.LAYER_LOCATION, Modelicecle::createBodyLayer);
      event.registerLayerDefinition(Modelkang.LAYER_LOCATION, Modelkang::createBodyLayer);
      event.registerLayerDefinition(Modeljinwoolegs1.LAYER_LOCATION, Modeljinwoolegs1::createBodyLayer);
      event.registerLayerDefinition(ModelSlash2.LAYER_LOCATION, ModelSlash2::createBodyLayer);
      event.registerLayerDefinition(Modeljinwoochest1.LAYER_LOCATION, Modeljinwoochest1::createBodyLayer);
      event.registerLayerDefinition(Modelkangtaeshikhair.LAYER_LOCATION, Modelkangtaeshikhair::createBodyLayer);
      event.registerLayerDefinition(Modelinv.LAYER_LOCATION, Modelinv::createBodyLayer);
      event.registerLayerDefinition(Modelshaces.LAYER_LOCATION, Modelshaces::createBodyLayer);
      event.registerLayerDefinition(Modelshabots.LAYER_LOCATION, Modelshabots::createBodyLayer);
      event.registerLayerDefinition(Modelshadowtorso.LAYER_LOCATION, Modelshadowtorso::createBodyLayer);
      event.registerLayerDefinition(ModelSlash3.LAYER_LOCATION, ModelSlash3::createBodyLayer);
      event.registerLayerDefinition(Modelkangtaeshik.LAYER_LOCATION, Modelkangtaeshik::createBodyLayer);
      event.registerLayerDefinition(ModelSlash.LAYER_LOCATION, ModelSlash::createBodyLayer);
      event.registerLayerDefinition(Modelshadowfeet.LAYER_LOCATION, Modelshadowfeet::createBodyLayer);
      event.registerLayerDefinition(Modeljinwoolegs2.LAYER_LOCATION, Modeljinwoolegs2::createBodyLayer);
      event.registerLayerDefinition(ModelSlash5.LAYER_LOCATION, ModelSlash5::createBodyLayer);
      event.registerLayerDefinition(Modelshahed.LAYER_LOCATION, Modelshahed::createBodyLayer);
      event.registerLayerDefinition(Modellight_ball.LAYER_LOCATION, Modellight_ball::createBodyLayer);
      event.registerLayerDefinition(Modelchoicloak.LAYER_LOCATION, Modelchoicloak::createBodyLayer);
      event.registerLayerDefinition(Modelshadowhead.LAYER_LOCATION, Modelshadowhead::createBodyLayer);
      event.registerLayerDefinition(ModelFlameArrow.LAYER_LOCATION, ModelFlameArrow::createBodyLayer);
   }
}
