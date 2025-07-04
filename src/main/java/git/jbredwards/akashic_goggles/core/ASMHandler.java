package git.jbredwards.akashic_goggles.core;

import git.jbredwards.akashic_goggles.Tags;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name(Tags.MOD_NAME + " Plugin")
public final class ASMHandler implements IFMLLoadingPlugin
{
    public static File MOD_LOCATION;

    @Override
    public void injectData(@Nonnull final Map<String, Object> data) {
        MOD_LOCATION = (File)data.get("coremodLocation");
    }

    @Nonnull
    @Override
    public String getModContainerClass() {
        return "git.jbredwards.akashic_goggles.mod.AkashicGoggles";
    }

    @Nonnull
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"git.jbredwards.akashic_goggles.core.transformer.Transformer"};
    }

    // -----
    // NO-OP
    // -----

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }
}
