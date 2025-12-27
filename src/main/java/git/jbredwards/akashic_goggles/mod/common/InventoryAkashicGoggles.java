package git.jbredwards.akashic_goggles.mod.common;

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
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
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
    public static final String NBT_INVENTORY = Tags.MOD_ID + ":inventory", NBT_IS_VALID = Tags.MOD_ID + ":is_valid", NBT_MUTABLE = Tags.MOD_ID + ":mutable";

    @Nonnull
    public final ItemStack goggles;
    public InventoryAkashicGoggles(@Nonnull final ItemStack gogglesIn) { goggles = gogglesIn; }

    @Override
    public boolean canDropItemIn(@Nullable final EntityPlayer player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) {
        return stack.getItem() instanceof IAkashicGoggles && ((IAkashicGoggles)stack.getItem()).canDropInAkashic(player, goggles, stack) && AkashicGogglesUtil
                .getContainedStacks(goggles).noneMatch(other -> ((IAkashicGoggles)stack.getItem()).compareDuringAkashicDropIn(player, goggles, stack, other));
    }

    @Nonnull
    @Override
    public ItemStack dropItemIn(@Nullable final EntityPlayer player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) {
        if(canDropItemIn(player, goggles, stack)) {
            @Nullable NBTTagList inventory = ItemNBTHelper.getList(goggles, NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, true);
            if(inventory == null) ItemNBTHelper.setList(goggles, NBT_INVENTORY, inventory = new NBTTagList());
            inventory.appendTag(ItemHandlerHelper.copyStackWithSize(stack, 1).serializeNBT());

            stack.shrink(1);
            if(player != null && isValid(goggles)) player.world.playSound(null, player.posX, player.posY, player.posZ, AkashicGoggles.ITEM_GOGGLES_INSERT, player.getSoundCategory(), 1, 1);
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

    // Let's prevent people from inserting items into JEI akashic goggles...
    public static void setValid(@Nonnull final ItemStack goggles) { ItemNBTHelper.setBoolean(goggles, NBT_IS_VALID, true); }
    public static void setInvalid(@Nonnull final ItemStack goggles) { ItemNBTHelper.setBoolean(goggles, NBT_IS_VALID, false); }
    public static boolean isValid(@Nonnull final ItemStack goggles) {
        return ItemNBTHelper.getBoolean(goggles, NBT_IS_VALID, false) && ItemNBTHelper.getBoolean(goggles, NBT_MUTABLE, true);
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
    public static int SLOT_SIZE = 18;
    public static int height() { return AkashicGogglesConfig.Goggles.height; }
    public static int width() { return AkashicGogglesConfig.Goggles.width; }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void renderTooltipBackground(@Nonnull final RenderTooltipEvent.PostBackground event) {
        if(event.getStack().getItem() instanceof ItemAkashicGoggles) {
            @Nonnull final NBTTagList slotData = ItemNBTHelper.getList(event.getStack(), NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, false);
            if(slotData.isEmpty() && !ItemNBTHelper.getBoolean(event.getStack(), NBT_IS_VALID, false)) return;

            final int slots = Math.max(slotData.tagCount() + 1, width());
            final int zLevel = 300;

            final int backgroundColor = 0xF0260F08;
            final int borderColorStart = 0x50FFFFFF;
            final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;

            final int width = SLOT_SIZE * Math.min(slots, width());
            final int height = SLOT_SIZE * Math.max((int)Math.ceil(slots / (double)width()), height());
            final int x = event.getX() + (event.getWidth() >> 1) - (width >> 1);
            final int y = event.getY() - height - 12;

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
            final int renderSize = Math.max(inventory.length + width() - inventory.length % width(), width() * height());

            final int xOffset = (event.getWidth() >> 1) - (SLOT_SIZE * width() >> 1);
            final int yOffset = SLOT_SIZE *- (int)Math.ceil(renderSize / (double)width()) - 12;

            // draw real slots
            for(int slot = 0; slot < inventory.length; slot++) {
                final int x = event.getX() + SLOT_SIZE * (slot % width()) + xOffset;
                final int y = event.getY() + SLOT_SIZE * (slot / width()) + yOffset;
                drawSlot(SLOT_TEXTURE, event.getFontRenderer(), inventory[slot], x, y);
            }

            // draw fake empty slots to fill remaining box
            for(int slot = inventory.length; slot < renderSize; slot++) {
                final int x = event.getX() + SLOT_SIZE * (slot % width()) + xOffset;
                final int y = event.getY() + SLOT_SIZE * (slot / width()) + yOffset;
                drawSlot(SLOT_TEXTURE, event.getFontRenderer(), ItemStack.EMPTY, x, y);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawSlot(@Nonnull final ResourceLocation texture, @Nonnull final FontRenderer font, @Nonnull final ItemStack stack, final int x, final int y) {
        GlStateManager.enableBlend();
        GlStateManager.pushMatrix();
        {
            @Nonnull final Minecraft mc = Minecraft.getMinecraft();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.color(1, 1, 1);
            {
                mc.getTextureManager().bindTexture(texture);
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
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
            GlStateManager.disableDepth();
        }
        GlStateManager.popMatrix();
    }
}
