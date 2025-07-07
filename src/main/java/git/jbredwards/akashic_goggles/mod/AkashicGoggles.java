package git.jbredwards.akashic_goggles.mod;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.core.ASMHandler;
import git.jbredwards.akashic_goggles.mod.client.ModelHeadwear;
import git.jbredwards.akashic_goggles.mod.common.InventoryAkashicGoggles;
import git.jbredwards.akashic_goggles.mod.common.baubles.CompatBaubles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 *
 * @author jbred
 *
 */
public final class AkashicGoggles extends DummyModContainer
{
    @Nullable public static Item GOGGLES;
    @Nonnull public static final SoundEvent
            ITEM_GOGGLES_EMPTY = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "item.empty")),
            ITEM_GOGGLES_EQUIP = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "item.equip")),
            ITEM_GOGGLES_INSERT = new SoundEvent(new ResourceLocation(Tags.MOD_ID, "item.insert"));

    @Nonnull public static final CreativeTabs TAB = new CreativeTabs(Tags.MOD_ID + ".tab") {
        @Nonnull
        @Override
        public ItemStack createIcon() { return new ItemStack(GOGGLES); }
    };

    public static boolean HAS_BAUBLES = false;

    @Subscribe
    public void preInit(@Nonnull final FMLPreInitializationEvent event) {
        if(HAS_BAUBLES = Loader.isModLoaded("baubles")) CompatBaubles.preInit();
    }

    @Subscribe
    @SideOnly(Side.CLIENT)
    public void postInitClient(@Nonnull final FMLPostInitializationEvent event) {
        if(HAS_BAUBLES) CompatBaubles.postInitClient();
        createMetadataTranslated(getMetadata());
    }

    // -------------------------------------
    // Internal mod container stuffs (START)
    // -------------------------------------

    @Nonnull private final List<String> ownedPackages;
    @Nonnull private final String creditsKey, descKey;

    public AkashicGoggles() throws IOException {
        super(createMetadata());
        ownedPackages = new ArrayList<>();
        creditsKey = getMetadata().credits;
        descKey = getMetadata().description;

        getMetadata().dependencies.add(VersionParser.parseVersionReference("autoreglib@[1.3-32,)"));
        getMetadata().requiredMods.add(VersionParser.parseVersionReference("autoreglib@[1.3-32,)"));
    }

    @Nonnull
    private static ModMetadata createMetadata() throws IOException {
        try(@Nonnull final ZipFile jar = new ZipFile(ASMHandler.MOD_LOCATION)) {
            return MetadataCollection.from(jar.getInputStream(jar.getEntry("mcmod.info")), jar.getName()).getMetadataForId(Tags.MOD_ID, ImmutableMap.of("name", Tags.MOD_NAME, "version", Tags.VERSION));
        }
    }

    @SideOnly(Side.CLIENT)
    private void createMetadataTranslated(@Nonnull  final ModMetadata metadata) {
        registerResourceListener(VanillaResourceType.LANGUAGES, manager -> {
            metadata.credits = I18n.format(creditsKey).replace("\\n", "\n");
            metadata.description = I18n.format(descKey);
        });
    }

    @Subscribe
    public void createOwnedPackages(@Nonnull final FMLConstructionEvent event) {
        ownedPackages.addAll(Arrays.asList(event.getASMHarvestedData().getCandidatesFor("git.jbredwards.akashic_goggles").stream().map(ModCandidate::getContainedPackages).flatMap(List::stream).distinct().toArray(String[]::new)));
        MinecraftForge.EVENT_BUS.register(InventoryAkashicGoggles.class);
        MinecraftForge.EVENT_BUS.register(getClass());
    }

    @SideOnly(Side.CLIENT)
    public static void registerResourceListener(@Nonnull final IResourceType type, @Nonnull final IResourceManagerReloadListener listener) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener)(manager, condition) -> {
            if(condition.test(type)) listener.onResourceManagerReload(manager);
        });
    }

    @Override
    public boolean registerBus(@Nonnull final EventBus bus, @Nonnull final LoadController controller) {
        bus.register(this);
        return true;
    }

    @Nonnull
    @Override
    public File getSource() { return ASMHandler.MOD_LOCATION; }

    @Nonnull
    @Override
    public List<String> getOwnedPackages() { return ownedPackages; }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Class<?> getCustomResourcePackClass() { return FMLFileResourcePack.class; }

    @Nonnull
    @Override
    public List<ArtifactVersion> getDependencies() { return getMetadata().dependencies; }

    @Nonnull
    @Override
    public Set<ArtifactVersion> getRequirements() { return getMetadata().requiredMods; }

    // -----------------------------------
    // Internal mod container stuffs (END)
    // -----------------------------------

    @SubscribeEvent
    static void registerItems(@Nonnull final RegistryEvent.Register<Item> event) throws ReflectiveOperationException {
        GOGGLES = (Item)Class.forName("git.jbredwards.akashic_goggles.mod.common.ItemAkashicGoggles").newInstance();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerModels(@Nonnull final ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(ModelHeadwear.Loader.INSTANCE);
    }

    @SubscribeEvent
    static void registerRecipes(@Nonnull final RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new ShapedOreRecipe(null, GOGGLES, "SSS", "GBG", 'S', "string", 'G', "paneGlass", 'B', OreDictionary.doesOreNameExist("bookshelf") ? "bookshelf" : Blocks.BOOKSHELF).setRegistryName(Tags.MOD_ID, "goggles"));
    }

    @SubscribeEvent
    static void registerSounds(@Nonnull final RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(ITEM_GOGGLES_EMPTY.setRegistryName(Tags.MOD_ID, "item.empty"), ITEM_GOGGLES_EQUIP.setRegistryName(Tags.MOD_ID, "item.equip"), ITEM_GOGGLES_INSERT.setRegistryName(Tags.MOD_ID, "item.insert"));
    }
}
