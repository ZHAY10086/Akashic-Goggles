/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.akashic_goggles.api;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Any block or item that implements this may be placed into Akashic Goggles.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface IAkashicGoggles
{
    /**
     * @return An {@link IAkashicGoggles} handler from the provided ItemStack.
     *
     * @throws NullPointerException If stack is null.
     * @since 1.0.0
     * @author jbred
     */
    @Nullable
    static IAkashicGoggles get(@Nonnull final ItemStack stack) {
        if(stack.getItem() instanceof IAkashicGoggles) return (IAkashicGoggles)stack.getItem();

        @Nonnull final Block block = Block.getBlockFromItem(stack.getItem());
        return block instanceof IAkashicGoggles ? (IAkashicGoggles)block : null;
    }

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
