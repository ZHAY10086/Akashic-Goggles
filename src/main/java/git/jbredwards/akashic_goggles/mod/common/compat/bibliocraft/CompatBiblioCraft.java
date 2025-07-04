package git.jbredwards.akashic_goggles.mod.common.compat.bibliocraft;

import baubles.api.BaubleType;
import baubles.api.cap.BaublesCapabilities;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.mod.AkashicGoggles;
import jds.bibliocraft.items.ItemReadingGlasses;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public final class CompatBiblioCraft
{
    @Nonnull
    static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Tags.MOD_ID, "baubles_cap");

    @SubscribeEvent
    static void attachBaublesCapability(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
        if(event.getObject().getItem() instanceof ItemReadingGlasses) event.addCapability(CAPABILITY_ID, new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(stack -> BaubleType.HEAD) : null;
            }
        });
    }

    public static void preInit() {
        if(AkashicGoggles.HAS_BAUBLES) MinecraftForge.EVENT_BUS.register(CompatBiblioCraft.class);
    }

    @SideOnly(Side.CLIENT)
    public static void postInitClient() {
        if(AkashicGoggles.HAS_BAUBLES) Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((skin, render) -> render.addLayer(new LayerBaublesBiblioCraftGlasses(render)));
    }
}
