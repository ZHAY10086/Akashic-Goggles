package git.jbredwards.akashic_goggles.mod.common;

import git.jbredwards.akashic_goggles.Tags;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import vazkii.arl.interf.IDropInItem;
import vazkii.arl.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.IntStream;

/**
 *
 * @author jbred
 *
 */
public class InventoryAkashicGoggles extends ItemStackHandler
{
    @Nonnull
    public static final Object2IntMap<Item> VALID_ITEMS = new Object2IntOpenHashMap<>();
    static { VALID_ITEMS.defaultReturnValue(-2); }

    @Nonnull
    public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/slot.png");
    public static int WIDTH = 6, HEIGHT = 2, SLOT_SIZE = 18;

    @Nonnull
    public final ItemStack goggles;
    public InventoryAkashicGoggles(@Nonnull final ItemStack gogglesIn) {
        super(WIDTH * HEIGHT);
        goggles = gogglesIn;
        deserializeNBT(ItemNBTHelper.getNBT(gogglesIn).getCompoundTag(ItemAkashicGoggles.NBT_KEY_INV));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void renderTooltipBackground(@Nonnull final RenderTooltipEvent.PostBackground event) {
        if(event.getStack().getItem() instanceof ItemAkashicGoggles && event.getStack().hasCapability(IDropInItem.DROP_IN_CAPABILITY, null)) {
            @Nullable final IItemHandler inventory = event.getStack().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(inventory != null) {
                final int zLevel = 300;
                final int slots = getSlotsFromNonEmptyRows(inventory).size() + WIDTH;

                final int backgroundColor = 0xF0260F08;
                final int borderColorStart = 0x50FFFFFF;
                final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;

                final int width = SLOT_SIZE * Math.min(slots, WIDTH);
                final int height = SLOT_SIZE * (int)Math.ceil(slots / (double)WIDTH);
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
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void renderTooltipSlots(@Nonnull final RenderTooltipEvent.PostText event) {
        if(event.getStack().getItem() instanceof ItemAkashicGoggles && event.getStack().hasCapability(IDropInItem.DROP_IN_CAPABILITY, null)) {
            @Nullable final IItemHandler inventory = event.getStack().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(inventory != null) {
                @Nonnull final IntList slots = getSlotsFromNonEmptyRows(inventory);
                final int xOffset = (event.getWidth() >> 1) - (SLOT_SIZE * Math.min(slots.size(), WIDTH) >> 1);
                final int yOffset = SLOT_SIZE *- (int)Math.ceil(slots.size() / (double)WIDTH) - SLOT_SIZE - 12;

                // draw real slots
                for(int slot = 0; slot < slots.size(); slot++) {
                    final int x = event.getX() + SLOT_SIZE * (slot % WIDTH) + xOffset;
                    final int y = event.getY() + SLOT_SIZE * (slot / WIDTH) + yOffset;
                    drawSlot(SLOT_TEXTURE, event.getFontRenderer(), inventory.getStackInSlot(slots.get(slot)), x, y);
                }

                // draw fake empty slots at the bottom
                for(int slot = slots.size(); slot < slots.size() + WIDTH; slot++) {
                    final int x = event.getX() + SLOT_SIZE * (slot % WIDTH) + xOffset;
                    final int y = event.getY() + SLOT_SIZE * (slot / WIDTH) + yOffset;
                    drawSlot(SLOT_TEXTURE, event.getFontRenderer(), ItemStack.EMPTY, x, y);
                }
            }
        }
    }

    @SubscribeEvent
    static void updateSelected(@Nonnull final TickEvent.PlayerTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            @Nonnull final ItemStack selected = event.player.inventory.getItemStack();
            if(selected.getItem() instanceof ItemAkashicGoggles) ItemAkashicGoggles.setValid(selected);
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

    @Override
    protected void onContentsChanged(final int slot) {
        if(stacks.stream().allMatch(ItemStack::isEmpty)) goggles.removeSubCompound(ItemAkashicGoggles.NBT_KEY_INV);
        else ItemNBTHelper.setCompound(goggles, ItemAkashicGoggles.NBT_KEY_INV, serializeNBT());
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
        final int targetDamage = stack.isItemStackDamageable() ? 0 : stack.getItemDamage();
        return VALID_ITEMS.getInt(stack.getItem()) == targetDamage && stacks.stream().noneMatch(stack::isItemEqualIgnoreDurability);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
        if(stack.isEmpty() || isItemValid(slot, stack)) {
            @Nonnull final ItemStack result = super.insertItem(slot, stack, simulate);
            addRow(stack);
            return result;
        }

        return stack;
    }

    @Override
    public void setStackInSlot(final int slot, @Nonnull final ItemStack stack) {
        if(stack.isEmpty() || isItemValid(slot, stack)) {
            super.setStackInSlot(slot, stack);
            addRow(stack);
        }
    }

    @Override
    public int getSlotLimit(final int slot) { return 1; }
    protected void addRow(@Nonnull final ItemStack stack) {
        if(!stack.isEmpty() && stacks.stream().noneMatch(ItemStack::isEmpty)) {
            @Nonnull final NonNullList<ItemStack> newStacks = NonNullList.withSize(stacks.size() + WIDTH, ItemStack.EMPTY);
            for(int i = 0; i < stacks.size(); i++) newStacks.set(i, stacks.get(i));

            stacks = newStacks;
            ItemNBTHelper.setCompound(goggles, ItemAkashicGoggles.NBT_KEY_INV, serializeNBT());
        }
    }

    @Nonnull
    protected static IntList getSlotsFromNonEmptyRows(@Nonnull final IItemHandler inventory) {
        @Nonnull final IntList slots = new IntArrayList();
        for(int row = 0; row < inventory.getSlots(); row += WIDTH) {
            if(row < WIDTH * (HEIGHT - 1) || !IntStream.range(row, row + WIDTH).mapToObj(inventory::getStackInSlot).allMatch(ItemStack::isEmpty))
                slots.addElements(slots.size(), IntStream.range(row, row + WIDTH).toArray());
        }

        return slots;
    }
}
