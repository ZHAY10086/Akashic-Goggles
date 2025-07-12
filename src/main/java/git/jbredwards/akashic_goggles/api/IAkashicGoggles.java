package git.jbredwards.akashic_goggles.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface IAkashicGoggles
{
    default boolean canDropInAkashic(@Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) {
        return true;
    }

    /**
     * @param goggles The Akashic Goggles ItemStack.
     * @param stack This ItemStack.
     * @param other The ItemStack to compare.
     * @return True if this stack matches another stack inside the Akashic Goggles.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     * @author jbred
     */
    default boolean compareDuringAkashicDropIn(@Nonnull final ItemStack goggles, @Nonnull final ItemStack stack, @Nonnull final ItemStack other) {
        return stack.getHasSubtypes() ? ItemStack.areItemsEqual(stack, other) : ItemStack.areItemsEqualIgnoreDurability(stack, other);
    }

    /**
     *
     * @param goggles
     * @param stack
     * @param player
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     * @author jbred
     */
    default void onAkashicTick(@Nonnull final ItemStack goggles, @Nonnull final ItemStack stack, @Nonnull final EntityLivingBase player) {}
}
