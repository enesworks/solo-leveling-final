package dev.eness.sololevelingfinal.core.util;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public final class MagicReadingHelper {
   private static final String MESSAGE_PREFIX = "Magic Reading: ";
   private static final String[] UNREADABLE_RESULTS = new String[]{"9999", "ERROR", "N/A", "Cannot Read!"};

   private MagicReadingHelper() {
   }

   public static boolean isHoldingMagicReader(Entity entity) {
      return (entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.MAGIC_READER.get();
   }

   public static void showRankReading(Entity reader, ProceduralDungeonRank rank) {
      MagicReadingHelper.ReadingRange range = rangeFor(rank);
      sendReading(reader, String.valueOf(Mth.nextInt(RandomSource.create(), range.minInclusive(), range.maxInclusive())));
   }

   public static void showUnreadableReading(Entity reader) {
      sendReading(reader, UNREADABLE_RESULTS[Mth.nextInt(RandomSource.create(), 0, UNREADABLE_RESULTS.length - 1)]);
   }

   public static MagicReadingHelper.ReadingRange rangeFor(ProceduralDungeonRank rank) {
      return switch (rank == null ? ProceduralDungeonRank.E : rank) {
         case E -> new MagicReadingHelper.ReadingRange(101, 199);
         case D -> new MagicReadingHelper.ReadingRange(201, 399);
         case C -> new MagicReadingHelper.ReadingRange(401, 599);
         case B -> new MagicReadingHelper.ReadingRange(601, 799);
         case A -> new MagicReadingHelper.ReadingRange(801, 999);
         case S -> new MagicReadingHelper.ReadingRange(1001, 1499);
      };
   }

   private static void sendReading(Entity reader, String value) {
      if (reader instanceof Player player && !player.level().isClientSide()) {
         player.displayClientMessage(Component.literal("Magic Reading: " + value), false);
      }
   }

   public record ReadingRange(int minInclusive, int maxInclusive) {
   }
}
