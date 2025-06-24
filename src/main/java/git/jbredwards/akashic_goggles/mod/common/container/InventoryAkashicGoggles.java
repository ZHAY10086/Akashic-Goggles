package git.jbredwards.akashic_goggles.mod.common.container;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class InventoryAkashicGoggles extends ItemStackHandler
{
    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
        return super.isItemValid(slot, stack);
    }

    @Override
    protected void onContentsChanged(final int slot) {
        super.onContentsChanged(slot);
    }

    @Override
    public int getSlotLimit(final int slot) { return 1; }
}
