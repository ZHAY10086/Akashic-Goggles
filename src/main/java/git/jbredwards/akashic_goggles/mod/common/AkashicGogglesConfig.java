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

import baubles.api.BaubleType;
import baubles.api.IBauble;
import git.jbredwards.akashic_goggles.Tags;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class AkashicGogglesConfig
{
    @Optional.Interface(modid = "baubles", iface = "baubles.api.IBauble")
    public enum BaubleTypeAdapter implements IBauble
    {
        NONE, AMULET, RING, BELT, TRINKET, HEAD, BODY, CHARM;

        @Nonnull
        @Override
        @Optional.Method(modid = "baubles")
        public BaubleType getBaubleType(@Nonnull final ItemStack stack) { return BaubleType.values()[ordinal() - 1]; }
    }

    @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + Tags.MOD_ID)
    public static class Goggles
    {
        @Config.LangKey("config." + Tags.MOD_ID + ".addApplicableTooltip")
        public static boolean addApplicableTooltip = true;

        @Config.LangKey("config." + Tags.MOD_ID + ".armorAttributes")
        public static boolean armorAttributes = false;

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".baubleType")
        public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.TRINKET;

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".blacklist")
        public static String[] blacklist = new String[0];

        @Config.SlidingOption
        @Config.RangeInt(min = 1, max = 5)
        @Config.LangKey("config." + Tags.MOD_ID + ".height")
        public static int height = 1;

        @Config.SlidingOption
        @Config.RangeInt(min = 1, max = 15)
        @Config.LangKey("config." + Tags.MOD_ID + ".width")
        public static int width = 5;
    }

    public static class ModCompat
    {
        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/actuallyadditions")
        public static class ActuallyAdditions
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.actuallyadditions.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/bibliocraft")
        public static class BiblioCraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.bibliocraft.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/embers")
        public static class Embers
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.embers.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/erebus")
        public static class Erebus
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.erebus.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/evilcraft")
        public static class EvilCraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.evilcraft.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/galacticraft")
        public static class Galacticraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.galacticraft.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;

            @Config.LangKey("config." + Tags.MOD_ID + ".compat.galacticraft.renderOverlayTexture")
            public static boolean renderOverlayTexture = true;

            @Config.LangKey("config." + Tags.MOD_ID + ".compat.galacticraft.renderValuablesTexture")
            public static boolean renderValuablesTexture = false;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/openblocks")
        public static class OpenBlocks
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.openblocks.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;

            @Config.LangKey("config." + Tags.MOD_ID + ".compat.openblocks.stackCrayonGlasses")
            public static boolean stackCrayonGlasses = false;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/railcraft")
        public static class Railcraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.railcraft.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + '/' + "compat/simplyjetpacks")
        public static class SimplyJetpacks
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + ".compat.simplyjetpacks.baubleType")
            public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }
    }

    @SubscribeEvent
    static void sync(@Nonnull final ConfigChangedEvent.OnConfigChangedEvent event) {
        if(Tags.MOD_ID.equals(event.getModID())) ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }
}
