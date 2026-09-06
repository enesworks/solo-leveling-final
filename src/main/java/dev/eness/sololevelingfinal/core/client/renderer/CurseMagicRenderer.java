package dev.eness.sololevelingfinal.core.client.renderer;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.client.model.Modelinv;
import dev.eness.sololevelingfinal.core.entity.CurseMagicEntity;

public class CurseMagicRenderer extends MobRenderer<CurseMagicEntity, Modelinv<CurseMagicEntity>> {
   public CurseMagicRenderer(Context context) {
      super(context, new Modelinv<>(context.bakeLayer(Modelinv.LAYER_LOCATION)), 0.0F);
   }

   public ResourceLocation getTextureLocation(CurseMagicEntity entity) {
      return new ResourceLocation("sololeveling:textures/entities/invistext.png");
   }

   protected boolean isBodyVisible(CurseMagicEntity entity) {
      return false;
   }

   protected boolean isShaking(CurseMagicEntity entity) {
      return true;
   }
}
