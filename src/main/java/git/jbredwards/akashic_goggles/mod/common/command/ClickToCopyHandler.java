/*
 * Copyright (C) <2026 to Present> <jbredwards>
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

package git.jbredwards.akashic_goggles.mod.common.command;

import git.jbredwards.akashic_goggles.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
public final class ClickToCopyHandler
{
    @Nonnull
    public static final ClickEvent.Action COPY_TO_CLIPBOARD = Objects.requireNonNull(EnumHelper.addEnum(ClickEvent.Action.class, Tags.MOD_ID, new Class[]{String.class, boolean.class}, Tags.MOD_ID, true));
    public static void construct() {
        if(FMLCommonHandler.instance().getSide().isClient()) MinecraftForge.EVENT_BUS.register(ClickToCopyHandler.class);
        ObfuscationReflectionHelper.<Map<String, ClickEvent.Action>, ClickEvent.Action>getPrivateValue(ClickEvent.Action.class, null, "field_150679_e").put(Tags.MOD_ID, COPY_TO_CLIPBOARD);
    }

    @Nonnull
    public static ITextComponent apply(@Nonnull final String text) {
        @Nonnull final HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("commands." + Tags.MOD_ID + ".copy.click"));
        return new TextComponentString(text).setStyle(new Style().setUnderlined(true).setColor(TextFormatting.BLUE).setClickEvent(new ClickEvent(COPY_TO_CLIPBOARD, text)).setHoverEvent(hover));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void handleClick(@Nonnull final GuiScreenEvent.MouseInputEvent.Pre event) {
        if(event.getGui() instanceof GuiChat && Mouse.getEventButtonState() && Mouse.getEventButton() == 0 && !GuiScreen.isShiftKeyDown()) {
            @Nullable final ITextComponent clicked = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
            if(clicked != null) {
                @Nullable final ClickEvent clickEvent = clicked.getStyle().getClickEvent();
                if(clickEvent != null && clickEvent.getAction() == COPY_TO_CLIPBOARD) {
                    GuiScreen.setClipboardString(clickEvent.getValue());
                    event.setCanceled(true);
                }
            }
        }
    }
}
