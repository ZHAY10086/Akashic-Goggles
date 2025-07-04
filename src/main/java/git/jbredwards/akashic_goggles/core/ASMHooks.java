package git.jbredwards.akashic_goggles.core;

import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import jds.bibliocraft.events.EventBlockMarkerHighlight;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.item.IBurstViewerBauble;
import vazkii.botania.api.item.ICosmeticAttachable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public final class ASMHooks
{
    public static boolean isNonEmpty(@Nonnull final ItemStack stack) {
        return !stack.isEmpty();
    }

    public static void registerGoggles(@Nonnull final Item item) {
        InventoryAkashicGoggles.VALID_ITEMS.put(item, 0);
    }

    // ----------
    // AutoRegLib
    // ----------

    @SideOnly(Side.CLIENT)
    public static int getSlotIndex(@Nonnull final Slot slot) {
        // Creative inventory is weird... try finding a slot id that can be referenced from the server
        @Nullable final EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        if(player != null && player.openContainer instanceof GuiContainerCreative.ContainerCreative) {
            @Nonnull final Optional<Slot> invSlot = player.inventoryContainer.inventorySlots.stream()
                    .filter(s -> slot.isSameInventory(s) && slot.getSlotIndex() == s.getSlotIndex())
                    .findFirst();
            // Slot found, return the slot's position in the inventory slot list
            if(invSlot.isPresent()) return invSlot.get().slotNumber;
        }
        // Use slot's position in the slot list (instead of Slot.getSlotIndex(), which isn't the same)
        return slot.slotNumber;
    }

    // -----------
    // BiblioCraft
    // -----------

    @Nonnull
    public static final NBTTagCompound CLIENT_READING_DATA = new NBTTagCompound();

    @Nonnull
    @SideOnly(Side.CLIENT)
    public static ItemStack getReadingGlasses() {
        @Nullable final EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        return player != null ? AkashicGogglesUtil.findStack(player, EventBlockMarkerHighlight::canHeadArmorRead) : ItemStack.EMPTY;
    }

    // -------
    // Botania
    // -------

    public static boolean hasMonocle(@Nonnull final EntityPlayer player) {
        return !AkashicGogglesUtil.findStack(player, stack -> {
            if(stack.getItem() instanceof IBurstViewerBauble) return true;
            else if(stack.getItem() instanceof ICosmeticAttachable) {
                @Nullable final ItemStack cosmetic = ((ICosmeticAttachable)stack.getItem()).getCosmeticItem(stack);
                return cosmetic != null && cosmetic.getItem() instanceof IBurstViewerBauble;
            }

            return false;
        }).isEmpty();
    }
}
