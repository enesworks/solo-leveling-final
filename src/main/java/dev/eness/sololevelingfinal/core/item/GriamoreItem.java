package dev.eness.sololevelingfinal.core.item;

import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.item.inventory.GriamoreInventoryCapability;
import dev.eness.sololevelingfinal.core.item.renderer.GriamoreItemRenderer;
import dev.eness.sololevelingfinal.core.world.inventory.FireGriamoreMenu;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController.State;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GriamoreItem extends Item implements GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   public String animationprocedure = "empty";
   public static ItemDisplayContext transformType;

   public GriamoreItem() {
      super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
   }

   @Override
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      super.initializeClient(consumer);
      consumer.accept(new IClientItemExtensions() {
         private final BlockEntityWithoutLevelRenderer renderer = new GriamoreItemRenderer();

         @Override
         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return this.renderer;
         }
      });
   }

   public void getTransformType(ItemDisplayContext type) {
      transformType = type;
   }

   private PlayState idlePredicate(AnimationState event) {
      if (transformType != null && this.animationprocedure.equals("empty")) {
         event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
         return PlayState.CONTINUE;
      } else {
         return PlayState.STOP;
      }
   }

   private PlayState procedurePredicate(AnimationState event) {
      if (transformType != null) {
         if (!this.animationprocedure.equals("empty") && event.getController().getAnimationState() == State.STOPPED) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
            if (event.getController().getAnimationState() == State.STOPPED) {
               this.animationprocedure = "empty";
               event.getController().forceAnimationReset();
            }
         } else if (this.animationprocedure.equals("empty")) {
            return PlayState.STOP;
         }
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void registerControllers(ControllerRegistrar data) {
      AnimationController procedureController = new AnimationController<>(this, "procedureController", 0, this::procedurePredicate);
      data.add(procedureController);
      AnimationController idleController = new AnimationController<>(this, "idleController", 0, this::idlePredicate);
      data.add(idleController);
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, final Player entity, final InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      ItemStack itemstack = ar.getObject();
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      if (entity instanceof ServerPlayer serverPlayer) {
         NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
               return Component.literal("Fire Griamore");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
               FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
               packetBuffer.writeBlockPos(entity.blockPosition());
               packetBuffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
               return new FireGriamoreMenu(id, inventory, packetBuffer);
            }
         }, buf -> {
            buf.writeBlockPos(entity.blockPosition());
            buf.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
         });
      }

      return ar;
   }

   @Override
   public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag compound) {
      return new GriamoreInventoryCapability();
   }

   @Override
   public CompoundTag getShareTag(ItemStack stack) {
      CompoundTag nbt = super.getShareTag(stack);
      if (nbt != null) {
         stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> nbt.put("Inventory", ((ItemStackHandler)capability).serializeNBT()));
      }

      return nbt;
   }

   @Override
   public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
      super.readShareTag(stack, nbt);
      if (nbt != null) {
         stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
            .ifPresent(capability -> ((ItemStackHandler)capability).deserializeNBT((CompoundTag)nbt.get("Inventory")));
      }
   }
}
