package dev.eness.sololevelingfinal.core.mixins;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import dev.eness.sololevelingfinal.core.worldgen.VillageUtilityStructureInjector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JigsawStructure.class)
public abstract class VillageJigsawPlacementMixin {
   @Shadow
   @Final
   private Holder<StructureTemplatePool> startPool;

   @Inject(
      method = "findGenerationPoint(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;)Ljava/util/Optional;",
      at = @At("RETURN"),
      cancellable = true
   )
   private void sololeveling$addVillageUtilities(GenerationContext context, CallbackInfoReturnable<Optional<GenerationStub>> callback) {
      callback.setReturnValue(VillageUtilityStructureInjector.inject(context, this.startPool, (Optional<GenerationStub>)callback.getReturnValue()));
   }
}
