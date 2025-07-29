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
@Config(modid = Tags.MOD_ID)
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

    @Nonnull
    @Config.LangKey("config." + Tags.MOD_ID + ".goggles")
    public static Goggles goggles = new Goggles();
    public static class Goggles
    {
        @Config.LangKey("config." + Tags.MOD_ID + ".armorAttributes")
        public boolean armorAttributes = false;

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".baubleType")
        public BaubleTypeAdapter baubleType = BaubleTypeAdapter.TRINKET;

        @Config.SlidingOption
        @Config.RangeInt(min = 1, max = 5)
        @Config.LangKey("config." + Tags.MOD_ID + ".height")
        public int height = 1;

        @Config.SlidingOption
        @Config.RangeInt(min = 1, max = 15)
        @Config.LangKey("config." + Tags.MOD_ID + ".width")
        public int width = 5;
    }

    @Nonnull
    @Config.LangKey("config." + Tags.MOD_ID + ".modCompat")
    public static ModCompat modCompat = new ModCompat();
    public static class ModCompat
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
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.erebus")
        public EvilCraft evilcraft = new EvilCraft();
        public static class EvilCraft
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.evilcraft.baubleType")
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

            @Config.LangKey("config." + Tags.MOD_ID + "compat.galacticraft.renderOverlayTexture")
            public boolean renderOverlayTexture = true;

            @Config.LangKey("config." + Tags.MOD_ID + "compat.galacticraft.renderValuablesTexture")
            public boolean renderValuablesTexture = false;
        }

        @Nonnull
        @Config.LangKey("config." + Tags.MOD_ID + ".compat.openblocks")
        public OpenBlocks openblocks = new OpenBlocks();
        public static class OpenBlocks
        {
            @Nonnull
            @Config.LangKey("config." + Tags.MOD_ID + "compat.openblocks.baubleType")
            public BaubleTypeAdapter baubleType = BaubleTypeAdapter.HEAD;

            @Config.LangKey("config." + Tags.MOD_ID + "compat.openblocks.stackCrayonGlasses")
            public boolean stackCrayonGlasses = false;
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
