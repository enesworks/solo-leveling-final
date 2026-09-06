package dev.eness.sololeveling3.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class OldKeyItem extends Item {
    public OldKeyItem() {
        super(new Properties().stacksTo(3).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
