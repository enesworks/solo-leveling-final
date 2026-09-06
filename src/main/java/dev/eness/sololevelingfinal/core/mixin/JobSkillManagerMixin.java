package dev.eness.sololevelingfinal.core.mixin;

import dev.eness.sololevelingfinal.core.combat.GoGunheeCombatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = JobSkillManager.class, remap = false)
public abstract class JobSkillManagerMixin {
    @Inject(method = "skillsForEntityJob", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$unlockSungIlHwanSkills(
            Entity entity,
            int job,
            CallbackInfoReturnable<List<String>> cir
    ) {
        if (job != 7 || entity == null) {
            return;
        }
        SololevelingModVariables.PlayerVariables vars = entity
                .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY)
                .orElse(new SololevelingModVariables.PlayerVariables());
        if ("sung_il_hwan".equals(vars.vesselIdentity)) {
            cir.setReturnValue(VesselProgressionManager.unlockedSkills(entity, job));
        }
    }

    @Inject(method = "skillsForJob", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$registerGoGunheeJob(int job, CallbackInfoReturnable<List<String>> cir) {
        if (job == 8) {
            cir.setReturnValue(GoGunheeCombatManager.SKILLS);
        }
    }

    @Inject(method = "isJobSkill", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$recognizeGoGunheeSkills(String skill, CallbackInfoReturnable<Boolean> cir) {
        if (GoGunheeCombatManager.isSkill(skill)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "skillColor", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$colorGoGunheeSkills(String skill, CallbackInfoReturnable<Integer> cir) {
        if (GoGunheeCombatManager.isSkill(skill)) {
            cir.setReturnValue(GoGunheeCombatManager.SKILL_COLOR);
        }
    }

    @Inject(method = "tooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$describeGoGunheeSkills(
            Entity entity,
            String skill,
            CallbackInfoReturnable<List<Component>> cir
    ) {
        if (GoGunheeCombatManager.isSkill(skill)) {
            cir.setReturnValue(GoGunheeCombatManager.tooltip(entity, skill));
        }
    }

    @Inject(method = "cast", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$castGoGunheeSkill(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            String skill,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (GoGunheeCombatManager.isSkill(skill)) {
            cir.setReturnValue(GoGunheeCombatManager.cast(entity, skill));
        }
    }

    @Inject(method = "shouldRemoveJobOwnedSkill", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$removeStaleGoGunheeSkill(
            String skill,
            List<String> granted,
            boolean keepFormations,
            CallbackInfoReturnable<Boolean> cir
    ) {
        String cleaned = skill == null ? "" : skill.trim();
        if (cleaned.startsWith(".")) {
            cleaned = cleaned.substring(1);
        }
        if (GoGunheeCombatManager.isSkill(cleaned)) {
            cir.setReturnValue(!granted.contains(cleaned));
        }
    }
}
