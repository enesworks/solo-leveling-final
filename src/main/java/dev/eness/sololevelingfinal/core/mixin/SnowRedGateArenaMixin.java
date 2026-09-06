package dev.eness.sololevelingfinal.core.mixin;

import dev.eness.sololevelingfinal.core.campaign.CampaignDirector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonInstanceSavedData;
import dev.eness.sololevelingfinal.core.dungeon.runtime.SnowRedGateArenaManager;
import dev.eness.sololevelingfinal.core.world.dimension.rift.RiftTerritory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SnowRedGateArenaManager.class, remap = false)
public abstract class SnowRedGateArenaMixin {
    private static final ResourceLocation ICE_MONARCH_POOL = ResourceLocation.fromNamespaceAndPath("sololeveling3", "ice_monarch");

    /**
     * Keeps SLR's complete Red Gate encounter intact and appends Sillad after
     * Baruka. Because both are boss encounters in the same sequence, SLR does
     * not mark the dungeon complete or reveal the exit after Baruka alone.
     */
    @Inject(method = "configureInstance", at = @At("RETURN"), cancellable = true, remap = false)
    private static void sololeveling3$appendIceMonarchAfterBaruka(
            ServerLevel level,
            DungeonInstanceSavedData.Instance instance,
            BlockPos center,
            int partySize,
            long seed,
            RiftTerritory territory,
            CallbackInfoReturnable<String> cir
    ) {
        if (cir.getReturnValue() != null || territory != RiftTerritory.FROST) {
            return;
        }

        boolean participantHasQuest = instance.participants().stream()
                .map(id -> level.getServer().getPlayerList().getPlayer(id))
                .anyMatch(player -> player != null && CampaignDirector.awaitingFrost(player));
        if (!participantHasQuest) {
            return;
        }

        DungeonInstanceSavedData.EncounterState baruka = instance.encounter("baruka").orElse(null);
        if (baruka == null || baruka.markers().isEmpty()) {
            cir.setReturnValue("Could not append Ice Monarch: Baruka encounter marker is missing.");
            return;
        }

        DungeonInstanceSavedData.MutationResult<DungeonInstanceSavedData.EncounterState> created = instance.createEncounter(
                "ice_monarch",
                ICE_MONARCH_POOL,
                true,
                false,
                instance.effectiveLevel(),
                instance.effectiveLevel(),
                baruka.sequenceKey(),
                baruka.sequenceOrder() + 1,
                200
        );
        if (!created.success() || created.value() == null) {
            cir.setReturnValue("Could not append Ice Monarch: " + created.message());
            return;
        }

        BlockPos spawn = baruka.markers().get(0).position();
        if (!created.value().addMarker("ice_monarch_0", "boss", spawn)) {
            instance.removeEncounter("ice_monarch");
            cir.setReturnValue("Could not append Ice Monarch spawn marker.");
        }
    }
}
