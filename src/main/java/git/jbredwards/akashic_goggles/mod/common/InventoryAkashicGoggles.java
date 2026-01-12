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

package git.jbredwards.akashic_goggles.mod.common;

import com.google.common.primitives.Ints;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.api.IAkashicGoggles;
import git.jbredwards.akashic_goggles.mod.AkashicGoggles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.ArrayUtils;
import vazkii.arl.util.AbstractDropIn;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class InventoryAkashicGoggles extends AbstractDropIn
{
    @Nonnull
    public static final String
            NBT_INVENTORY = Tags.MOD_ID + ":inventory", // Holds all items.
            NBT_INVENTORY_LOCKED = Tags.MOD_ID + ":inventory_locked", // Number of locked slots.
            NBT_IS_VALID = Tags.MOD_ID + ":is_valid", // This has been initialized (boolean).
            NBT_MUTABLE = Tags.MOD_ID + ":mutable"; // This may have items added or removed (boolean).

    @Nonnull
    public final ItemStack goggles;
    public InventoryAkashicGoggles(@Nonnull final ItemStack gogglesIn) { goggles = gogglesIn; }

    @Override
    public boolean canDropItemIn(@Nullable final EntityPlayer player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) { return canDropIn(player, goggles, stack); }
    public static boolean canDropIn(@Nullable final EntityPlayer player, @Nullable final ItemStack goggles, @Nonnull final ItemStack stack) {
        if(!(stack.getItem() instanceof IAkashicGoggles) || !((IAkashicGoggles)stack.getItem()).canDropInAkashic(player, goggles, stack)) return false;
        else if(ArrayUtils.contains(AkashicGogglesConfig.Goggles.blacklist, String.valueOf(stack.getItem().getRegistryName()))) return false;

        else return goggles == null || AkashicGogglesUtil.getContainedStacks(goggles).noneMatch(other -> ((IAkashicGoggles)stack.getItem()).compareDuringAkashicDropIn(player, goggles, stack, other));
    }

    @Nonnull
    @Override
    public ItemStack dropItemIn(@Nullable final EntityPlayer player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) {
        if(canDropItemIn(player, goggles, stack)) {
            @Nullable NBTTagList inventory = ItemNBTHelper.getList(goggles, NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, true);
            if(inventory == null) ItemNBTHelper.setList(goggles, NBT_INVENTORY, inventory = new NBTTagList());
            inventory.appendTag(ItemHandlerHelper.copyStackWithSize(stack, 1).serializeNBT());

            stack.shrink(1);
            if(player != null && !(player instanceof FakePlayer) && isValid(goggles))
                player.world.playSound(null, player.posX, player.posY, player.posZ, AkashicGoggles.ITEM_GOGGLES_INSERT, player.getSoundCategory(), 1, 1);
        }

        return goggles;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return super.hasCapability(capability, null) && isValid(goggles) ? super.getCapability(capability, null) : null;
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return super.hasCapability(capability, null) && isValid(goggles);
    }

    // Allow akashic goggles to be locked globally.
    public static boolean isMutable(@Nonnull final ItemStack goggles) {
        return ItemNBTHelper.getBoolean(goggles, NBT_MUTABLE, true);
    }

    // Let's prevent people from inserting items into JEI akashic goggles...
    public static void setValid(@Nonnull final ItemStack goggles) { ItemNBTHelper.setBoolean(goggles, NBT_IS_VALID, true); }
    public static void setInvalid(@Nonnull final ItemStack goggles) { ItemNBTHelper.setBoolean(goggles, NBT_IS_VALID, false); }
    public static boolean isValid(@Nonnull final ItemStack goggles) {
        return ItemNBTHelper.getBoolean(goggles, NBT_IS_VALID, false) && isMutable(goggles);
    }

    @SubscribeEvent
    static void updateSelected(@Nonnull final TickEvent.PlayerTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            @Nonnull final ItemStack selected = event.player.inventory.getItemStack();
            if(selected.getItem() instanceof ItemAkashicGoggles) setValid(selected);
        }
    }

    // ---------
    // Rendering
    // ---------

    @Nonnull
    public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/slot.png");
    public static int SLOT_SIZE = 18, HEIGHT_RENDER_OFFSET = -12;
    public static int height() { return AkashicGogglesConfig.Goggles.height; }
    public static int width() { return AkashicGogglesConfig.Goggles.width; }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void renderTooltipBackground(@Nonnull final RenderTooltipEvent.PostBackground event) {
        if(event.getStack().getItem() instanceof ItemAkashicGoggles) {
            @Nonnull final NBTTagList slotData = ItemNBTHelper.getList(event.getStack(), NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, false);
            if(slotData.isEmpty() && !ItemNBTHelper.getBoolean(event.getStack(), NBT_IS_VALID, false)) return;

            final int slots = Ints.max(slotData.tagCount() + (isMutable(event.getStack()) ? 1 : 0), 1, width());
            final int zLevel = 300;

            final int backgroundColor = 0xF0260F08;
            final int borderColorStart = 0x50FFFFFF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;

            final int width = SLOT_SIZE * Math.min(slots, width());
            final int height = SLOT_SIZE * Math.max(MathHelper.ceil(slots / (double)width()), height());
            final int x = event.getX() + (event.getWidth() >> 1) - (width >> 1);
            final int y = event.getY() - height + HEIGHT_RENDER_OFFSET;

            // copied from GuiUtils
            GuiUtils.drawGradientRect(zLevel, x - 3, y - 4, x + width + 3, y - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, x - 3, y + height + 3, x + width + 3, y + height + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + width + 3, y + height + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, x - 4, y - 3, x - 3, y + height + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, x + width + 3, y - 3, x + width + 4, y + height + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + width + 3, y - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(zLevel, x - 3, y + height + 2, x + width + 3, y + height + 3, borderColorEnd, borderColorEnd);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void renderTooltipSlots(@Nonnull final RenderTooltipEvent.PostText event) {
        if(event.getStack().getItem() instanceof ItemAkashicGoggles) {
            if(!ItemNBTHelper.getBoolean(event.getStack(), NBT_IS_VALID, false) && ItemNBTHelper.getList(event.getStack(), NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, false).isEmpty()) return;

            @Nonnull final ItemStack[] inventory = AkashicGogglesUtil.getContainedStacks(event.getStack()).toArray(ItemStack[]::new);
            final int inventoryLocked = ItemNBTHelper.getInt(event.getStack(), NBT_INVENTORY_LOCKED, 0);

            final boolean immutable = !isMutable(event.getStack());
            final int slotsForRender = inventory.length - (immutable ? 1 : 0);
            final int renderSize = Math.max(slotsForRender + width() - slotsForRender % width(), width() * height());

            final int xOffset = (event.getWidth() >> 1) - (SLOT_SIZE * width() >> 1);
            final int yOffset = SLOT_SIZE *- (int)Math.ceil(renderSize / (double)width()) + HEIGHT_RENDER_OFFSET;

            // draw real slots
            for(int slot = 0; slot < inventory.length; slot++) {
                final int x = event.getX() + SLOT_SIZE * (slot % width()) + xOffset;
                final int y = event.getY() + SLOT_SIZE * (slot / width()) + yOffset;
                drawSlot(SLOT_TEXTURE, event.getFontRenderer(), inventory[slot], x, y, immutable || slot < inventoryLocked);
            }

            // draw fake empty slots to fill remaining box
            for(int slot = inventory.length; slot < renderSize; slot++) {
                final int x = event.getX() + SLOT_SIZE * (slot % width()) + xOffset;
                final int y = event.getY() + SLOT_SIZE * (slot / width()) + yOffset;
                drawSlot(SLOT_TEXTURE, event.getFontRenderer(), ItemStack.EMPTY, x, y, immutable);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawSlot(@Nonnull final ResourceLocation texture, @Nonnull final FontRenderer font, @Nonnull final ItemStack stack, final int x, final int y, final boolean immutable) {
        GlStateManager.enableBlend();
        GlStateManager.pushMatrix();
        {
            @Nonnull final Minecraft mc = Minecraft.getMinecraft();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.color(1, 1, 1);
            {
                mc.getTextureManager().bindTexture(texture);
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE << 1, SLOT_SIZE);
            }
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().zLevel = 200;
            {
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x + 1, y + 1);
                mc.getRenderItem().renderItemOverlayIntoGUI(font, stack, x + 1, y + 1, null);
            }
            mc.getRenderItem().zLevel = 0;
            RenderHelper.disableStandardItemLighting();
            GlStateManager.color(1, 1, 1);
            GlStateManager.disableDepth();
            if(immutable) {
                GlStateManager.translate(0, 0, 1000);
                mc.getTextureManager().bindTexture(texture);
                Gui.drawModalRectWithCustomSizedTexture(x, y, SLOT_SIZE, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE << 1, SLOT_SIZE);
            }
        }
        GlStateManager.popMatrix();
    }
}
