package git.jbredwards.akashic_goggles.api;

import git.jbredwards.akashic_goggles.mod.common.item.ItemAkashicGoggles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public enum AkashicGogglesUtil
{
    ;

    @Nonnull
    public static Stream<ItemStack> getContainedGoggles(@Nonnull final ItemStack stack) {
        if(!(stack.getItem() instanceof ItemAkashicGoggles)) return Stream.empty();


    }

    @SideOnly(Side.CLIENT)
    public static void registerResourceListener(@Nonnull final IResourceType type, @Nonnull final IResourceManagerReloadListener listener) {
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener)(manager, condition) -> {
            if(condition.test(type)) listener.onResourceManagerReload(manager);
        });
    }
}
