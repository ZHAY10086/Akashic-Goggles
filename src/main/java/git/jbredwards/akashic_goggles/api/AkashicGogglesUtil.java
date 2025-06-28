package git.jbredwards.akashic_goggles.api;

import git.jbredwards.akashic_goggles.core.ASMHooks;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import git.jbredwards.akashic_goggles.mod.common.ItemAkashicGoggles;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public enum AkashicGogglesUtil
{
    ;

    @Nonnull
    public static Stream<ItemStack> getContainedGoggles(@Nonnull final ItemStack stack) {
        if(!(stack.getItem() instanceof ItemAkashicGoggles)) return Stream.empty();

        @Nullable final IItemHandler inventory = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        return inventory != null ? IntStream.range(0, inventory.getSlots()).mapToObj(inventory::getStackInSlot) : Stream.empty();
    }

    @Nonnull
    public static ItemStack getMatchingGoggles(@Nonnull final EntityLivingBase entity, @Nonnull final ItemStack target) {
        for(@Nonnull final ItemStack armor : entity.getArmorInventoryList()) {
            if(target.isItemEqualIgnoreDurability(armor)) return armor;
            // Search stored eyewear if the armor is akashic goggles.
            @Nonnull final Optional<ItemStack> matching = getContainedGoggles(armor).filter(target::isItemEqualIgnoreDurability).findFirst();
            if(matching.isPresent()) return matching.get();
        }

        return ASMHooks.HAS_BAUBLES ? ASMHooks.getMatchingGogglesBaubles(entity, target) : ItemStack.EMPTY;
    }

    public static void registerSupportedGoggles(@Nonnull final ItemStack stack) {
        InventoryAkashicGoggles.SUPPORTED_GOGGLES.put(stack.getItem(), stack.getItemDamage());
    }
}
