package git.jbredwards.akashic_goggles.core;

import de.ellpeck.actuallyadditions.api.misc.IGoggles;
import de.ellpeck.naturesaura.events.ClientEvents;
import de.ellpeck.naturesaura.items.ModItems;
import erebus.items.ItemCompoundGoggles;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.mod.common.AkashicGogglesConfig;
import jds.bibliocraft.events.EventBlockMarkerHighlight;
import micdoodle8.mods.galacticraft.api.item.ISensorGlassesArmor;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.client.gui.overlay.OverlaySensorGlasses;
import mods.railcraft.client.core.AuraKeyHandler;
import mods.railcraft.common.items.ItemGoggles;
import mods.railcraft.common.items.RailcraftItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import openblocks.common.item.ItemImaginationGlasses;
import openblocks.common.item.ItemSonicGlasses;
import openblocks.common.tileentity.TileEntityImaginary;
import org.cyclops.evilcraft.entity.monster.VengeanceSpirit;
import org.cyclops.evilcraft.item.SpectralGlasses;
import org.lwjgl.opengl.GL11;
import teamroots.embers.api.item.IInfoGoggles;
import vazkii.botania.api.item.IBurstViewerBauble;
import vazkii.botania.api.item.ICosmeticAttachable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
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

    // ------------------
    // Actually Additions
    // ------------------

    public static boolean isWearing(@Nonnull final EntityPlayer player) {
        return !AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof IGoggles).isEmpty();
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public static Object getWearing(@Nonnull final EntityPlayer player) {
        @Nonnull final ItemStack wearing = AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof IGoggles && ((IGoggles)stack.getItem()).displaySpectralMobs());
        return wearing.isEmpty() ? AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof IGoggles) : wearing;
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

    public static boolean isHelmet(@Nonnull final ItemArmor item) {
        return item.armorType == EntityEquipmentSlot.HEAD;
    }

    // ------
    // Erebus
    // ------

    public static boolean isWearingGoggles(@Nullable final EntityPlayer player) {
        return player != null && !AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof ItemCompoundGoggles).isEmpty();
    }

    // ---------
    // EvilCraft
    // ---------

    @SideOnly(Side.CLIENT)
    public static boolean isWearingGlasses(@Nonnull final VengeanceSpirit spirit) {
        @Nullable final EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        return spirit.isEnabledVengeance(player) || player != null && !AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof SpectralGlasses).isEmpty();
    }

    // ------------
    // Galacticraft
    // ------------

    @SideOnly(Side.CLIENT)
    public static boolean overrideMobTexture() {
        @Nullable final EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        return player != null && !AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof ISensorGlassesArmor).isEmpty();
    }

    @SideOnly(Side.CLIENT)
    public static void renderSensorGlassesMain(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player, @Nonnull final ScaledResolution resolution, final float partialTicks) {
        if(AkashicGogglesConfig.modCompat.galacticraft.renderOverlayTexture) OverlaySensorGlasses.renderSensorGlassesMain(stack, player, resolution, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    public static void renderValuablesTexture(final double x, final double y, final double z, final double width, final double height, @Nonnull final BlockVec3 coords) {
        if(AkashicGogglesConfig.modCompat.galacticraft.renderValuablesTexture) {
            @Nonnull final World world = Minecraft.getMinecraft().world;
            @Nullable final IBlockState state = coords.getBlockState(world);

            if(state != null) {
                @Nonnull final TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state).getParticleTexture();
                if(sprite != Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
                    Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                    drawCentered(x, y, z, width, height, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
                    return;
                }
            }
        }

        drawCentered(x, y, z, width, height, 0, 1, 0, 1);
    }

    // Utility function.
    @SideOnly(Side.CLIENT)
    private static void drawCentered(final double x, final double y, final double z, final double width, final double height,
                                     final double minU, final double maxU, final double minV, final double maxV) {
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        Tessellator.getInstance().getBuffer().pos(x - width * 0.5, y + height * 0.5, z).tex(minU, maxV).endVertex();
        Tessellator.getInstance().getBuffer().pos(x + width * 0.5, y + height * 0.5, z).tex(maxU, maxV).endVertex();
        Tessellator.getInstance().getBuffer().pos(x + width * 0.5, y - height * 0.5, z).tex(maxU, minV).endVertex();
        Tessellator.getInstance().getBuffer().pos(x - width * 0.5, y - height * 0.5, z).tex(minU, minV).endVertex();
        Tessellator.getInstance().draw();
    }

    // -------------
    // Nature's Aura
    // -------------

    @Nullable
    private static Field heldEye, heldOcular;

    @SideOnly(Side.CLIENT)
    public static boolean getEyes(final boolean searchBaubles) {
        @Nullable final EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        if(player != null) {
            if(heldEye == null) heldEye = ReflectionHelper.findField(ClientEvents.class, "heldEye");
            if(heldOcular == null) heldOcular = ReflectionHelper.findField(ClientEvents.class, "heldOcular");
            @Nonnull final ItemStack eye = AkashicGogglesUtil.findStack(player, stack -> stack.getItem() == ModItems.EYE);
            @Nonnull final ItemStack ocular = AkashicGogglesUtil.findStack(player, stack -> stack.getItem() == ModItems.EYE_IMPROVED);
            try {
                heldEye.set(null, eye);
                heldOcular.set(null, ocular);
            }
            // Unpossible?
            catch(@Nonnull final Exception e) { throw new ReflectionHelper.UnableToAccessFieldException(e); }
        }

        return searchBaubles;
    }

    // ----------
    // OpenBlocks
    // ----------

    @Nonnull
    public static ItemStack getImaginaryGlasses(@Nonnull final EntityPlayer player, @Nonnull final TileEntityImaginary tile, @Nonnull final TileEntityImaginary.Property what) {
        return AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof ItemImaginationGlasses && ((ItemImaginationGlasses)stack.getItem()).checkBlock(what, stack, tile));
    }

    @Nonnull
    public static ItemStack getSonicGlasses(@Nonnull final EntityPlayer player) {
        return AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof ItemSonicGlasses);
    }

    public static boolean isSameColor(@Nonnull final ItemStack stack, @Nonnull final ItemStack other) {
        return ItemStack.areItemsEqualIgnoreDurability(stack, other) && (!AkashicGogglesConfig.modCompat.openblocks.stackCrayonGlasses
                || ItemImaginationGlasses.getGlassesColor(stack) == ItemImaginationGlasses.getGlassesColor(other));
    }

    // ---------
    // Railcraft
    // ---------

    @Nonnull
    public static ItemStack getGoggles(@Nullable final EntityPlayer player, @Nullable final ItemGoggles.GoggleAura aura) {
        return player == null ? ItemStack.EMPTY : AkashicGogglesUtil.findStack(player, stack -> stack.getItem() instanceof ItemGoggles && (aura == null || aura == ItemGoggles.getCurrentAura(stack)));
    }

    public static boolean isGoggleAuraActive(@Nonnull final ItemGoggles.GoggleAura aura) {
        return RailcraftItems.GOGGLES.isLoaded() ? isPlayerWearing(FMLClientHandler.instance().getClientPlayerEntity(), aura) : AuraKeyHandler.isAuraEnabled(aura);
    }

    public static boolean isPlayerWearing(@Nullable final EntityPlayer player, @Nullable final ItemGoggles.GoggleAura aura) {
        return !getGoggles(player, aura).isEmpty();
    }

    public static boolean isSameAura(@Nonnull final ItemStack stack, @Nonnull final ItemStack other) {
        return ItemStack.areItemsEqualIgnoreDurability(stack, other) && ItemGoggles.getCurrentAura(stack) == ItemGoggles.getCurrentAura(other);
    }
}
