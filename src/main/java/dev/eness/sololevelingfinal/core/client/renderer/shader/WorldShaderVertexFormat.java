package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public final class WorldShaderVertexFormat {
   public static final VertexFormat NEW_ENTITY = new VertexFormat(
      ImmutableMap.builder()
         .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
         .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
         .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
         .put("UV1", DefaultVertexFormat.ELEMENT_UV1)
         .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
         .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
         .put("Padding", DefaultVertexFormat.ELEMENT_PADDING)
         .build()
   );

   private WorldShaderVertexFormat() {
   }
}
