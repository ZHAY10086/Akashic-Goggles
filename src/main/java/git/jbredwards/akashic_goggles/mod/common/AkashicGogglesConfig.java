package git.jbredwards.akashic_goggles.mod.common;

import baubles.api.BaubleType;
import git.jbredwards.akashic_goggles.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Config(modid = Tags.MOD_ID)
public final class AkashicGogglesConfig
{
    @Config.LangKey("config." + Tags.MOD_ID + ".applyArmorAttributes")
    public static boolean applyArmorAttributes = true;

    @Nonnull
    @Config.LangKey("config." + Tags.MOD_ID + ".baubleType")
    public static BaubleTypeAdapter baubleType = BaubleTypeAdapter.TRINKET;
    public enum BaubleTypeAdapter
    {
        AMULET, RING, BELT, TRINKET, HEAD, BODY, CHARM;

        @Nonnull
        @Optional.Method(modid = "baubles")
        public BaubleType get() { return BaubleType.values()[ordinal()]; }
    }

    @Nonnull
    @Config.LangKey("config." + Tags.MOD_ID + ".compat")
    public static Compat compat = new Compat();
    public static class Compat
    {
        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.actuallyadditions")
        public ActuallyAdditions actuallyadditions = new ActuallyAdditions();
        public static class ActuallyAdditions
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.actuallyadditions.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.bibliocraft")
        public BiblioCraft bibliocraft = new BiblioCraft();
        public static class BiblioCraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.bibliocraft.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.embers")
        public Embers embers = new Embers();
        public static class Embers
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.embers.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.erebus")
        public Erebus erebus = new Erebus();
        public static class Erebus
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.erebus.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.galacticraft")
        public Galacticraft galacticraft = new Galacticraft();
        public static class Galacticraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.galacticraft.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.openblocks")
        public OpenBlocks openblocks = new OpenBlocks();
        public static class OpenBlocks
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.openblocks.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.railcraft")
        public Railcraft railcraft = new Railcraft();
        public static class Railcraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.railcraft.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.simplyjetpacks")
        public SimplyJetpacks simplyjetpacks = new SimplyJetpacks();
        public static class SimplyJetpacks
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.simplyjetpacks.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;
        }
    }

    @SubscribeEvent
    static void sync(@Nonnull final ConfigChangedEvent.OnConfigChangedEvent event) {
        if(Tags.MOD_ID.equals(event.getModID())) ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }
}
