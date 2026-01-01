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

package git.jbredwards.akashic_goggles.mod.client.config;

import com.google.common.collect.Lists;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.mod.common.AkashicGogglesConfig;
import git.jbredwards.akashic_goggles.mod.common.compat.CompatHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.DefaultGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
public final class AkashicGogglesGuiFactory extends DefaultGuiFactory
{
    @Nonnull
    private final Set<Class<?>> activeModCategories;
    public AkashicGogglesGuiFactory() {
        super(Tags.MOD_ID, Tags.MOD_NAME);
        activeModCategories = new HashSet<>();
    }

    @Override
    public void initialize(@Nonnull final Minecraft minecraftInstance) {
        super.initialize(minecraftInstance);
        activeModCategories.addAll(Lists.transform(CompatHandler.getLoadedHandlers(), handler -> handler.config));
    }

    @Nonnull
    @Override
    public GuiScreen createConfigGui(@Nonnull final GuiScreen parentScreen) {
        @Nonnull final List<IConfigElement> elements;
        if(activeModCategories.isEmpty()) elements = ConfigElement.from(AkashicGogglesConfig.Goggles.class).getChildElements();
        else elements = Lists.newArrayList(ConfigElement.from(AkashicGogglesConfig.Goggles.class), new DummyConfigElement.DummyCategoryElement("", "config." + Tags.MOD_ID + ".modCompat",
                activeModCategories.stream().map(ConfigElement::from).sorted(Comparator.comparing(element -> I18n.format(element.getLanguageKey()))).collect(Collectors.toList())));

        return new GuiConfigTranslucent(parentScreen, elements, modid, false, false, title, null);
    }
}
