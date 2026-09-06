package dev.eness.sololeveling3.mixin;

import net.minecraft.core.BlockPos;
import net.solocraft.util.CartenonTempleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = CartenonTempleManager.class, remap = false)
public interface CartenonTempleManagerAccessor {
    @Invoker("instanceOrigin")
    static BlockPos sololeveling3$instanceOrigin(int instance) {
        throw new AssertionError();
    }
}