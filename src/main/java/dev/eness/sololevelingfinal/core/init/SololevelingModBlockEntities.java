package dev.eness.sololevelingfinal.core.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import dev.eness.sololevelingfinal.core.block.entity.CustomPortalBlockEntity;
import dev.eness.sololevelingfinal.core.block.entity.DungeonWallTileEntity;
import dev.eness.sololevelingfinal.core.block.entity.GuildComputerBlockEntity;
import dev.eness.sololevelingfinal.core.block.entity.HunterRankEvaluatorTileEntity;
import dev.eness.sololevelingfinal.core.block.entity.InstanceCoverTileEntity;
import dev.eness.sololevelingfinal.core.block.entity.InstanceDungeonKeyLoggerTileEntity;

public class SololevelingModBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "sololeveling");
   public static final RegistryObject<BlockEntityType<InstanceDungeonKeyLoggerTileEntity>> INSTANCE_DUNGEON_KEY_LOGGER = REGISTRY.register(
      "instance_dungeon_key_logger",
      () -> Builder.of(InstanceDungeonKeyLoggerTileEntity::new, SololevelingModBlocks.INSTANCE_DUNGEON_KEY_LOGGER.get()).build(null)
   );
   public static final RegistryObject<BlockEntityType<InstanceCoverTileEntity>> INSTANCE_COVER = REGISTRY.register(
      "instance_cover", () -> Builder.of(InstanceCoverTileEntity::new, SololevelingModBlocks.INSTANCE_COVER.get()).build(null)
   );
   public static final RegistryObject<BlockEntityType<HunterRankEvaluatorTileEntity>> HUNTER_RANK_EVALUATOR = REGISTRY.register(
      "hunter_rank_evaluator", () -> Builder.of(HunterRankEvaluatorTileEntity::new, SololevelingModBlocks.HUNTER_RANK_EVALUATOR.get()).build(null)
   );
   public static final RegistryObject<BlockEntityType<DungeonWallTileEntity>> DUNGEON_WALL = REGISTRY.register(
      "dungeon_wall", () -> Builder.of(DungeonWallTileEntity::new, SololevelingModBlocks.DUNGEON_WALL.get()).build(null)
   );
   public static final RegistryObject<BlockEntityType<?>> CUSTOM_PORTAL = register(
      "custom_portal", SololevelingModBlocks.CUSTOM_PORTAL, CustomPortalBlockEntity::new
   );
   public static final RegistryObject<BlockEntityType<GuildComputerBlockEntity>> GUILD_COMPUTER = REGISTRY.register(
      "guild_computer", () -> Builder.of(GuildComputerBlockEntity::new, SololevelingModBlocks.GUILD_COMPUTER.get()).build(null)
   );

   private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntitySupplier<?> supplier) {
      return REGISTRY.register(registryname, () -> Builder.of(supplier, block.get()).build(null));
   }
}
