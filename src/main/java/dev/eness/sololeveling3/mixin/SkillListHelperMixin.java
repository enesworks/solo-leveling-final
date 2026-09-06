package dev.eness.sololeveling3.mixin;

import dev.eness.sololeveling3.combat.GoGunheeCombatManager;
import net.solocraft.util.SkillListHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SkillListHelper.class, remap = false)
public abstract class SkillListHelperMixin {
    @Inject(method = "orderedForEquipList", at = @At("RETURN"), cancellable = true, remap = false)
    private static void sololeveling3$includeGoGunheeSkills(
            List<String> source,
            CallbackInfoReturnable<List<String>> cir
    ) {
        List<String> original = cir.getReturnValue();
        ArrayList<String> ordered = new ArrayList<>();
        for (String skill : GoGunheeCombatManager.SKILLS) {
            if (source.contains(skill) && !ordered.contains(skill)) {
                ordered.add(skill);
            }
        }
        for (String skill : original) {
            if (!ordered.contains(skill)) {
                ordered.add(skill);
            }
        }
        cir.setReturnValue(ordered);
    }
}
