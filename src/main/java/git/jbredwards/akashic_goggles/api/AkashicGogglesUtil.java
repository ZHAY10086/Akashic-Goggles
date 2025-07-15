package git.jbredwards.akashic_goggles.api;

import baubles.api.BaublesApi;
import git.jbredwards.akashic_goggles.mod.AkashicGoggles;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import git.jbredwards.akashic_goggles.mod.common.ItemAkashicGoggles;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility functions for using Akashic Goggles.
 * @since 1.0.0
 * @author jbred
 *
 */
public enum AkashicGogglesUtil
{
    ;

    @Nonnull
    public static Stream<ItemStack> getContainedStacks(@Nonnull final ItemStack stack) {
        if(!(stack.getItem() instanceof ItemAkashicGoggles)) return Stream.empty();
        @Nullable final NBTTagList inventory = ItemNBTHelper.getList(stack, InventoryAkashicGoggles.NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, true);
        return inventory == null ? Stream.empty() : IntStream.range(0, inventory.tagCount()).mapToObj(slot -> new ItemStack(inventory.getCompoundTagAt(slot)));
    }

    /**
     * Searches the player's armor, then any Akashic Goggles equipped, for an ItemStack that matches the Predicate.
     * If this fails, Baubles slots are checked (if the Baubles mod is installed).
     *
     * @param player The player to be searched.
     * @param filter The Predicate that must be matched.
     * @return The first found ItemStack, or {@link ItemStack#EMPTY} if none is found.
     * @throws NullPointerException If player or filter are null.
     *
     * @since 1.0.0
     * @author jbred
     */
    @Nonnull
    public static ItemStack findStack(@Nonnull final EntityPlayer player, @Nonnull final Predicate<ItemStack> filter) {
        @Nonnull final ItemStack matchingArmor = findStack(new ItemStackHandler(player.inventory.armorInventory), filter);
        if(!matchingArmor.isEmpty()) return matchingArmor;
        // Baubles support.
        return AkashicGoggles.HAS_BAUBLES ? findStack(BaublesApi.getBaublesHandler(player), filter) : ItemStack.EMPTY;
    }

    /**
     * Searches the inventory, then any Akashic Goggles within the inventory, for an ItemStack that matches the filter.
     *
     * @param inventory The inventory to be searched.
     * @param filter The Predicate that must be matched.
     * @return The first found ItemStack, or {@link ItemStack#EMPTY} if none is found.
     * @throws NullPointerException If inventory or filter are null.
     *
     * @since 1.0.0
     * @author jbred
     */
    @Nonnull
    public static ItemStack findStack(@Nonnull final IItemHandler inventory, @Nonnull final Predicate<ItemStack> filter) {
        return IntStream.range(0, inventory.getSlots()).mapToObj(inventory::getStackInSlot).filter(filter).findFirst()
                // Search any Akashic Goggles if the stack is initially not found.
                .orElseGet(() -> IntStream.range(0, inventory.getSlots()).mapToObj(inventory::getStackInSlot)
                .flatMap(AkashicGogglesUtil::getContainedStacks).filter(filter).findFirst().orElse(ItemStack.EMPTY));
    }
}
