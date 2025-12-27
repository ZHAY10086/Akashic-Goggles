package git.jbredwards.akashic_goggles.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Any item that implements this may be placed into Akashic Goggles.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface IAkashicGoggles
{
    /**
     * @param player The entity wearing Akashic Goggles.
     * @param goggles The Akashic Goggles ItemStack.
     * @param stack This ItemStack.
     * @return True if this stack can be dropped into the Akashic Goggles.
     *
     * @throws NullPointerException If stack is null.
     * @since 1.0.0
     * @author jbred
     */
    default boolean canDropInAkashic(@Nullable final EntityLivingBase player, @Nullable final ItemStack goggles, @Nonnull final ItemStack stack) {
        return true;
    }

    /**
     * @param player The entity wearing Akashic Goggles.
     * @param goggles The Akashic Goggles ItemStack.
     * @param stack This ItemStack.
     * @param other The ItemStack to compare.
     * @return True if this stack matches another stack inside the Akashic Goggles.
     *
     * @throws NullPointerException If any parameters aside from player are null.
     * @since 1.0.0
     * @author jbred
     */
    default boolean compareDuringAkashicDropIn(@Nullable final EntityLivingBase player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack, @Nonnull final ItemStack other) {
        return stack.getHasSubtypes() ? ItemStack.areItemsEqual(stack, other) : ItemStack.areItemsEqualIgnoreDurability(stack, other);
    }

    /**
     * Called each tick while Akashic Goggles containing this item are worn.
     * @param player The entity wearing Akashic Goggles.
     * @param goggles The Akashic Goggles ItemStack.
     * @param stack This ItemStack.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     * @author jbred
     */
    default void onAkashicTick(@Nonnull final EntityLivingBase player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) {

    }
}
