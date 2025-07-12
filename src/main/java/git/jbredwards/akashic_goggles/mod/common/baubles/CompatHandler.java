package git.jbredwards.akashic_goggles.mod.common.baubles;

import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.api.IAkashicGoggles;
import git.jbredwards.akashic_goggles.mod.common.AkashicGogglesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author jbred
 *
 */
public enum CompatHandler implements Predicate<ItemStack>
{
    ACTUALLYADDITIONS("actuallyadditions", () -> AkashicGogglesConfig.compat.actuallyadditions.baubleType),
    BIBLIOCRAFT("bibliocraft", () -> AkashicGogglesConfig.compat.bibliocraft.baubleType),
    EMBERS("embers", () -> AkashicGogglesConfig.compat.embers.baubleType),
    EREBUS("erebus", () -> AkashicGogglesConfig.compat.erebus.baubleType),
    GALACTICRAFT("galacticraft", () -> AkashicGogglesConfig.compat.galacticraft.baubleType),
    OPENBLOCKS("openblocks", () -> AkashicGogglesConfig.compat.openblocks.baubleType),
    RAILCRAFT("railcraft", () -> AkashicGogglesConfig.compat.railcraft.baubleType),
    SIMPLYJETPACKS("simplyjetpacks", () -> AkashicGogglesConfig.compat.simplyjetpacks.baubleType);

    @Nonnull final Supplier<AkashicGogglesConfig.BaubleTypeAdapter> bauble;
    @Nonnull public final String modid;

    @Nonnull static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Tags.MOD_ID, "baubles_cap");
    @Nonnull static final List<CompatHandler> LOADED_HANDLERS = new ArrayList<>();

    CompatHandler(@Nonnull final String modidIn, @Nonnull final Supplier<AkashicGogglesConfig.BaubleTypeAdapter> baubleIn) {
        bauble = baubleIn;
        modid = modidIn;
    }

    @SubscribeEvent
    public void attachBaublesCapability(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
        if(test(event.getObject())) event.addCapability(CAPABILITY_ID, new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(getBauble()) : null;
            }
        });
    }

    public static void preInit() {
        LOADED_HANDLERS.addAll(Arrays.asList(Arrays.stream(values()).filter(ch -> Loader.isModLoaded(ch.modid)).toArray(CompatHandler[]::new)));
        LOADED_HANDLERS.forEach(MinecraftForge.EVENT_BUS::register);
    }

    @SideOnly(Side.CLIENT)
    public static void postInitClient() {
        if(!LOADED_HANDLERS.isEmpty()) Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((skin, render) -> render.addLayer(new LayerBaublesArmor(render)));
    }

    @Nonnull
    @Optional.Method(modid = "baubles")
    public IBauble getBauble() { return stack -> bauble.get().get(); }

    @Override
    public boolean test(@Nonnull final ItemStack stack) {
        if(stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType != EntityEquipmentSlot.HEAD) return false;
        else return stack.getItem() instanceof IAkashicGoggles && modid.equals(stack.getItem().delegate.name().getNamespace());
    }
}
