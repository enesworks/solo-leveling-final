package dev.eness.sololeveling3.campaign;

import dev.eness.sololeveling3.SoloLeveling3;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.solocraft.network.SololevelingModVariables;
import net.solocraft.procedures.XPGainProcedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** One-shot native Solo Leveling rewards for authored campaign chapter completions. */
public final class CampaignRewards {
    private static final String ROOT = "campaign_rewards";
    private static final String PAID = "paid";
    private static final String XP = "xp";
    private static final String GOLD = "gold";
    private static final String TROPHY = "trophy";
    private static final String ITEM_PREFIX = "item_";
    private static final String ITEM_COUNT_SUFFIX = "_count";
    private static final String PENDING = "pending";

    private CampaignRewards() {
    }

    /**
     * Grants the bundle for a just-completed active chapter.
     *
     * @return true when the reward is fully paid, or when this stage has no reward bundle.
     */
    public static boolean grant(ServerPlayer player, CampaignStage completed) {
        if (player == null || completed == null) {
            return false;
        }

        Reward reward = rewardForStage(completed.name());
        if (reward == null) {
            return true;
        }

        queue(player, reward.key());
        return apply(player, reward);
    }

    /** Retries any completed chapter reward that could not be fully paid during the return transition. */
    public static void tick(ServerPlayer player) {
        if (player == null || player.server == null || player.tickCount % 20 != 0) {
            return;
        }

        CompoundTag root = root(player);
        ListTag pending = root.getList(PENDING, Tag.TAG_STRING);
        if (pending.isEmpty()) {
            return;
        }

        List<String> keys = new ArrayList<>(pending.size());
        for (Tag item : pending) {
            keys.add(item.getAsString());
        }
        for (String key : keys) {
            Reward reward = rewardForKey(key);
            if (reward == null || state(player, key).getBoolean(PAID)) {
                unqueue(player, key);
            } else {
                apply(player, reward);
            }
        }
    }

    private static boolean apply(ServerPlayer player, Reward reward) {
        CompoundTag state = state(player, reward.key());
        if (state.getBoolean(PAID)) {
            unqueue(player, reward.key());
            return true;
        }

        try {
            if (!state.getBoolean(XP)) {
                awardNativeXp(player, reward.xp());
                state.putBoolean(XP, true);
                CampaignDirector.changed(player);
            }

            if (!state.getBoolean(GOLD)) {
                awardNativeGold(player, reward.gold());
                state.putBoolean(GOLD, true);
                CampaignDirector.changed(player);
            }

            List<ItemStack> items = reward.items();
            for (int i = 0; i < items.size(); i++) {
                String flag = ITEM_PREFIX + i;
                deliverCounted(player, state, reward.key(), flag, flag, items.get(i));
            }

            if (reward.trophy() && !state.getBoolean(TROPHY)) {
                deliver(player, trophy(reward.key()));
                state.putBoolean(TROPHY, true);
                CampaignDirector.changed(player);
            }

            state.putBoolean(PAID, true);
            unqueue(player, reward.key());
            CampaignDirector.changed(player);
            player.sendSystemMessage(Component.literal("[SYSTEM] ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(reward.title()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" ödülü alındı: "))
                    .append(Component.literal(reward.xp() + " XP, " + reward.gold() + "G").withStyle(ChatFormatting.YELLOW)));
            return true;
        } catch (RuntimeException error) {
            SoloLeveling3.LOGGER.error("Campaign reward grant failed for {} at {}", player.getUUID(), reward.key(), error);
            player.sendSystemMessage(Component.literal("[SYSTEM] Ödül güvenli şekilde bekletildi; tekrar denenecek.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
    }

    /** Alias for return handlers that keep the old active stage around. */
    public static boolean grantOnReturn(ServerPlayer player, CampaignStage oldStage) {
        return grant(player, oldStage);
    }

    private static CompoundTag root(ServerPlayer player) {
        CompoundTag progress = CampaignDirector.progress(player);
        CompoundTag root = progress.getCompound(ROOT);
        progress.put(ROOT, root);
        return root;
    }

    private static CompoundTag state(ServerPlayer player, String key) {
        CompoundTag root = root(player);
        CompoundTag stage = root.getCompound(key);
        root.put(key, stage);
        return stage;
    }

    private static void queue(ServerPlayer player, String key) {
        CompoundTag root = root(player);
        ListTag pending = root.getList(PENDING, Tag.TAG_STRING);
        boolean exists = false;
        for (Tag item : pending) {
            if (key.equals(item.getAsString())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            pending.add(StringTag.valueOf(key));
            root.put(PENDING, pending);
            CampaignDirector.changed(player);
        }
    }

    private static void unqueue(ServerPlayer player, String key) {
        CompoundTag root = root(player);
        ListTag pending = root.getList(PENDING, Tag.TAG_STRING);
        ListTag kept = new ListTag();
        boolean changed = false;
        for (Tag item : pending) {
            if (key.equals(item.getAsString())) {
                changed = true;
            } else {
                kept.add(StringTag.valueOf(item.getAsString()));
            }
        }
        if (changed) {
            root.put(PENDING, kept);
            CampaignDirector.changed(player);
        }
    }

    private static Reward rewardForStage(String stageName) {
        return switch (stageName) {
            case "DOUBLE_ACTIVE" -> new Reward("double", "Çifte Zindan", 9_000, 350, false,
                    stack("medium_health_potion", 4, Items.GOLDEN_APPLE),
                    stack("medium_mana_potion", 4, Items.LAPIS_LAZULI),
                    stack("small_fatigue_potion", 2, Items.COOKIE));
            case "LEGIA_ACTIVE" -> new Reward("legia", "Legia", 15_000, 650, false,
                    stack("large_health_potion", 4, Items.GOLDEN_APPLE),
                    stack("large_mana_potion", 4, Items.LAPIS_LAZULI),
                    stack("holy_water_of_life", 1, Items.DIAMOND));
            case "FROST_ACTIVE" -> new Reward("frost_retreat", "Buz Hükümdarı'nın Kaçışı", 12_000, 550, false,
                    stack("large_health_potion", 5, Items.GOLDEN_APPLE),
                    stack("large_mana_potion", 5, Items.LAPIS_LAZULI),
                    stack("large_fatigue_potion", 2, Items.COOKED_BEEF));
            case "TRIO_ACTIVE" -> new Reward("three_monarchs", "Üç Hükümdar", 20_000, 900, false,
                    stack("large_health_potion", 7, Items.GOLDEN_APPLE),
                    stack("large_mana_potion", 7, Items.LAPIS_LAZULI),
                    stack("holy_water_of_life", 2, Items.DIAMOND),
                    stack("random_special_box", 1, Items.NETHERITE_INGOT));
            case "FINAL_ACTIVE" -> new Reward("antares", "Antares", 30_000, 1_500, true,
                    stack("large_health_potion", 10, Items.GOLDEN_APPLE),
                    stack("large_mana_potion", 10, Items.LAPIS_LAZULI),
                    stack("holy_water_of_life", 3, Items.DIAMOND),
                    stack("kamish_tooth", 1, Items.DRAGON_BREATH));
            default -> null;
        };
    }

    private static Reward rewardForKey(String key) {
        return switch (key) {
            case "double" -> rewardForStage("DOUBLE_ACTIVE");
            case "legia" -> rewardForStage("LEGIA_ACTIVE");
            case "frost_retreat" -> rewardForStage("FROST_ACTIVE");
            case "three_monarchs" -> rewardForStage("TRIO_ACTIVE");
            case "antares" -> rewardForStage("FINAL_ACTIVE");
            default -> null;
        };
    }

    private static void awardNativeXp(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }
        SololevelingModVariables.PlayerVariables before = variables(player)
                .orElseThrow(() -> new IllegalStateException("Missing SLR player variables"));
        if (!before.Player) {
            throw new IllegalStateException("Player has not unlocked SLR system state");
        }
        double start = before.Xp;
        XPGainProcedure.awardRewardXp(player, amount);
        SololevelingModVariables.PlayerVariables after = variables(player)
                .orElseThrow(() -> new IllegalStateException("Missing SLR player variables after XP reward"));
        if (after.Xp + 0.0001D < start + amount) {
            throw new IllegalStateException("SLR reward XP was not applied");
        }
    }

    private static void awardNativeGold(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return;
        }
        SololevelingModVariables.PlayerVariables vars = variables(player)
                .orElseThrow(() -> new IllegalStateException("Missing SLR player variables"));
        vars.golds += amount;
        vars.syncPlayerVariables(player);
    }

    private static Optional<SololevelingModVariables.PlayerVariables> variables(ServerPlayer player) {
        return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).resolve();
    }

    private static ItemStack stack(String slrId, int count, Item fallback) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("sololeveling", slrId));
        if (item == null || item == Items.AIR) {
            item = fallback;
        }
        return new ItemStack(item, count);
    }

    private static ItemStack trophy(String key) {
        ItemStack stack = new ItemStack(Items.DRAGON_HEAD);
        stack.setHoverName(Component.literal("Antares'in Hükümdar Tacı").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        return marked(stack, key, TROPHY);
    }

    private static ItemStack marked(ItemStack original, String stage, String kind) {
        ItemStack stack = original.copy();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("sl3_campaign_reward", true);
        tag.putString("sl3_campaign_reward_stage", stage);
        tag.putString("sl3_campaign_reward_kind", kind);
        tag.putString("sl3_campaign_reward_id", stage + ":" + kind);
        return stack;
    }

    private static void deliverCounted(ServerPlayer player, CompoundTag state, String stage, String flag, String rewardKind, ItemStack stack) {
        if (stack.isEmpty()) {
            state.putBoolean(flag, true);
            return;
        }
        int target = stack.getCount();
        String countKey = flag + ITEM_COUNT_SUFFIX;
        int delivered = Math.max(0, Math.min(target, state.getInt(countKey)));
        while (delivered < target) {
            ItemStack single = stack.copy();
            single.setCount(1);
            deliver(player, marked(single, stage, rewardKind));
            delivered++;
            state.putInt(countKey, delivered);
            CampaignDirector.changed(player);
        }
        state.putBoolean(flag, true);
        CampaignDirector.changed(player);
    }

    private static void deliver(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = stack.copy();
        player.getInventory().add(remaining);
        if (!remaining.isEmpty()) {
            ItemEntity dropped = player.drop(remaining, false);
            if (dropped == null) {
                throw new IllegalStateException("Reward item could not be inserted or dropped: " + remaining);
            }
        }
    }

    private record Reward(String key, String title, int xp, int gold, boolean trophy, List<ItemStack> items) {
        Reward(String key, String title, int xp, int gold, boolean trophy, ItemStack... items) {
            this(key, title, xp, gold, trophy, copy(items));
        }

        private static List<ItemStack> copy(ItemStack[] items) {
            List<ItemStack> copy = new ArrayList<>(items.length);
            for (ItemStack item : items) {
                copy.add(item.copy());
            }
            return copy;
        }
    }
}
