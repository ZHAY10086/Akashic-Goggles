package git.jbredwards.akashic_goggles.mod;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.core.ASMHandler;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 *
 * @author jbred
 *
 */
public final class AkashicGoggles extends DummyModContainer
{
    // -------------------------------------
    // Internal mod container stuffs (START)
    // -------------------------------------

    @Nonnull
    private final String creditsKey, descKey;
    public AkashicGoggles() throws IOException {
        super(createMetadata());
        creditsKey = getMetadata().credits;
        descKey = getMetadata().description;
    }

    @Nonnull
    private static ModMetadata createMetadata() throws IOException {
        try(@Nonnull final ZipFile jar = new ZipFile(ASMHandler.MOD_LOCATION)) {
            return MetadataCollection.from(jar.getInputStream(jar.getEntry("mcmod.info")), jar.getName()).getMetadataForId(Tags.MOD_ID, ImmutableMap.of("name", Tags.MOD_NAME, "version", Tags.VERSION));
        }
    }

    @Subscribe
    @SideOnly(Side.CLIENT)
    public void createMetadataTranslated(@Nonnull final FMLInitializationEvent event) {
        AkashicGogglesUtil.registerResourceListener(VanillaResourceType.LANGUAGES, manager -> {
            getMetadata().credits = I18n.format(creditsKey).replace("\\n", "\n");
            getMetadata().description = I18n.format(descKey);
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

    // -----------------------------------
    // Internal mod container stuffs (END)
    // -----------------------------------
}
