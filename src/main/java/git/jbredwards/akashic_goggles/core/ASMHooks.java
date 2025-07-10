package git.jbredwards.akashic_goggles.core;

import de.ellpeck.actuallyadditions.api.misc.IGoggles;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import jds.bibliocraft.events.EventBlockMarkerHighlight;
import mods.railcraft.api.items.InvToolsAPI;
import mods.railcraft.client.core.AuraKeyHandler;
import mods.railcraft.common.items.ItemGoggles;
import mods.railcraft.common.items.RailcraftItems;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.api.item.IInfoGoggles;
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

    // ------------------
    // Actually Additions
    // ------------------

    public static boolean isWearing(@Nonnull final EntityPlayer player) {
        return !AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof IGoggles).isEmpty();
    }

    @Nonnull
    public static ItemStack getWearing(@Nonnull final EntityPlayer player) {
        return AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof IGoggles && ((IGoggles)stack.getItem()).displaySpectralMobs());
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

    public static void registerGlasses(@Nonnull final Item item) {
        for(int meta = 0; meta < 3; meta++) InventoryAkashicGoggles.VALID_ITEMS.put(item, meta);
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

    // ------
    // Embers
    // ------

    public static boolean isGoggles(@Nonnull final EntityPlayer player, @Nonnull final EntityEquipmentSlot slot) {
        if(slot == EntityEquipmentSlot.HEAD) return !AkashicGogglesUtil.findStack(player, stack
                -> stack.getItem() instanceof IInfoGoggles && ((IInfoGoggles)stack.getItem())
                .shouldDisplayInfo(player, stack, EntityEquipmentSlot.HEAD)).isEmpty();

        @Nonnull final ItemStack stack = player.getItemStackFromSlot(slot);
        return stack.getItem() instanceof IInfoGoggles && ((IInfoGoggles)stack.getItem()).shouldDisplayInfo(player, stack, slot);
    }

    public static void registerGoggles(@Nonnull final ItemArmor item) {
        if(item.armorType == EntityEquipmentSlot.HEAD) registerGoggles((Item)item);
    }

    // ---------
    // Railcraft
    // ---------

    @Nullable
    public static ItemStack getGoggles(@Nullable final EntityPlayer player, @Nullable final ItemGoggles.GoggleAura aura) {
        return player == null ? null : AkashicGogglesUtil.findStack(player, stack
                -> stack.getItem() instanceof ItemGoggles && (aura == null || aura == ItemGoggles.getCurrentAura(stack)));
    }

    public static boolean isGoggleAuraActive(@Nonnull final ItemGoggles.GoggleAura aura) {
        return RailcraftItems.GOGGLES.isLoaded() ? isPlayerWearing(FMLClientHandler.instance().getClientPlayerEntity(), aura) : AuraKeyHandler.isAuraEnabled(aura);
    }

    public static boolean isPlayerWearing(@Nullable final EntityPlayer player, @Nullable final ItemGoggles.GoggleAura aura) {
        return !InvToolsAPI.isEmpty(getGoggles(player, aura));
    }
}
