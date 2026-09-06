package dev.eness.sololevelingfinal.core.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RangerClientState;

public final class RangerManaQuiverHandRenderer {
   private static final Vec3 GENERIC_NOCK = new Vec3(0.5, 0.5, 0.5);
   private static final Vec3 SPIRIT_BOW_REST_NOCK = new Vec3(0.3671875, 0.934375, 0.4765625);
   private static final Vec3 SPIRIT_BOW_STRING_TOP = new Vec3(0.3671875, 1.1921875, 0.4765625);
   private static final Vec3 SPIRIT_BOW_STRING_BOTTOM = new Vec3(0.3671875, 0.6765625, 0.4765625);
   private static final double SPIRIT_BOW_MAX_STRING_PULL = 0.053125;

   private RangerManaQuiverHandRenderer() {
   }

   public static void renderNockedArrow(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffers) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      boolean bow = stack.getItem() instanceof BowItem;
      boolean activeUse = bow
         && player != null
         && player.isUsingItem()
         && player.getUseItem().getItem() instanceof BowItem
         && ItemStack.isSameItemSameTags(stack, player.getUseItem())
         && isRenderedActiveHand(player, context);
      float draw = 0.0F;
      if (activeUse) {
         float useTicks = player.getTicksUsingItem() + minecraft.getFrameTime();
         float rawDraw = Mth.clamp(useTicks / 20.0F, 0.0F, 1.0F);
         draw = Mth.clamp((rawDraw * rawDraw + rawDraw * 2.0F) / 3.0F, 0.0F, 1.0F);
      }

      boolean spiritBow = stack.is(SololevelingModItems.SPIRIT_BOW.get());
      double forwardZ = spiritBowForwardZ(context, leftHand);
      Vec3 spiritNock = SPIRIT_BOW_REST_NOCK;
      if (spiritBow) {
         double nockZ = SPIRIT_BOW_REST_NOCK.z - forwardZ * 0.053125 * draw;
         spiritNock = new Vec3(SPIRIT_BOW_REST_NOCK.x, SPIRIT_BOW_REST_NOCK.y, nockZ);
         ManaArrowVisual.renderBowString(poseStack, buffers, SPIRIT_BOW_STRING_TOP, spiritNock, 0.0019);
         ManaArrowVisual.renderBowString(poseStack, buffers, spiritNock, SPIRIT_BOW_STRING_BOTTOM, 0.0019);
      }

      if (context.firstPerson() && activeUse) {
         float formation = 0.26F + draw * 0.74F;
         Vec3 nock = spiritBow ? spiritNock : GENERIC_NOCK;
         if (!spiritBow) {
            forwardZ = leftHand ? -1.0 : 1.0;
         }

         Vec3 forward = new Vec3(0.0, 0.0, forwardZ);
         double visibleLength = spiritBow ? 0.04 + 0.0225 * formation : 0.105 + 0.055 * formation;
         Vec3 tip = nock.add(forward.scale(visibleLength));
         boolean manaArrow = RangerClientState.quiverActive
            && player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .map(variables -> Math.round(variables.Classes) == 6L)
               .orElse(false);
         if (!manaArrow) {
            if (spiritBow) {
               ManaArrowVisual.renderPhysicalNocked(poseStack, buffers, nock, tip, 0.0027, 0.016, 0.0065);
            }
         } else {
            int stage = Math.max(1, Mth.clamp(RangerClientState.chargeStage, 0, 3));
            float lock = RangerClientState.locked ? 1.0F : Mth.clamp(RangerClientState.lockProgress, 0.0F, 1.0F);
            float intensity = Mth.clamp(0.58F + draw * 0.3F + stage * 0.025F + lock * 0.045F, 0.0F, 1.0F);
            if (spiritBow) {
               ManaArrowVisual.renderNocked(poseStack, buffers, nock, tip, stage, RangerClientState.locked, intensity, 0.0032, 0.018, 0.0075);
            } else {
               ManaArrowVisual.renderNocked(poseStack, buffers, nock, tip, stage, RangerClientState.locked, intensity, 0.0055, 0.05, 0.02);
            }
         }
      }
   }

   private static double spiritBowForwardZ(ItemDisplayContext context, boolean leftHand) {
      return switch (context) {
         case FIRST_PERSON_RIGHT_HAND -> -1.0;
         case FIRST_PERSON_LEFT_HAND -> 1.0;
         case THIRD_PERSON_RIGHT_HAND -> 1.0;
         case THIRD_PERSON_LEFT_HAND -> -1.0;
         default -> leftHand ? 1.0 : -1.0;
      };
   }

   private static boolean isRenderedActiveHand(LocalPlayer player, ItemDisplayContext context) {
      HumanoidArm renderedArm;
      if (context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
         if (context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND && context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            return false;
         }

         renderedArm = HumanoidArm.LEFT;
      } else {
         renderedArm = HumanoidArm.RIGHT;
      }

      HumanoidArm activeArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
      return renderedArm == activeArm;
   }
}
