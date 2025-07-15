package git.jbredwards.akashic_goggles.mod.common.baubles;

import baubles.api.BaublesApi;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import com.google.common.collect.Iterables;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.api.IAkashicGoggles;
import git.jbredwards.akashic_goggles.mod.common.AkashicGogglesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author jbred
 *
 */
public enum CompatHandler
{
    ACTUALLYADDITIONS("actuallyadditions", () -> AkashicGogglesConfig.modCompat.actuallyadditions.baubleType),
    BIBLIOCRAFT("bibliocraft", () -> AkashicGogglesConfig.modCompat.bibliocraft.baubleType),
    EMBERS("embers", () -> AkashicGogglesConfig.modCompat.embers.baubleType),
    EREBUS("erebus", () -> AkashicGogglesConfig.modCompat.erebus.baubleType),
    GALACTICRAFT("galacticraftcore", () -> AkashicGogglesConfig.modCompat.galacticraft.baubleType),
    OPENBLOCKS("openblocks", () -> AkashicGogglesConfig.modCompat.openblocks.baubleType),
    RAILCRAFT("railcraft", () -> AkashicGogglesConfig.modCompat.railcraft.baubleType),
    SIMPLYJETPACKS("simplyjetpacks", () -> AkashicGogglesConfig.modCompat.simplyjetpacks.baubleType);

    @Nonnull private final Supplier<AkashicGogglesConfig.BaubleTypeAdapter> bauble;
    @Nonnull public final String modid;

    @Nonnull private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Tags.MOD_ID, "baubles_cap");
    @Nonnull private static final List<CompatHandler> LOADED_HANDLERS = new ArrayList<>();

    CompatHandler(@Nonnull final String modidIn, @Nonnull final Supplier<AkashicGogglesConfig.BaubleTypeAdapter> baubleIn) {
        bauble = baubleIn;
        modid = modidIn;
    }

    public static void preInit() {
        LOADED_HANDLERS.addAll(Arrays.asList(Arrays.stream(values()).filter(ch -> Loader.isModLoaded(ch.modid)).toArray(CompatHandler[]::new)));
        MinecraftForge.EVENT_BUS.register(CompatHandler.class);
    }

    @SideOnly(Side.CLIENT)
    public static void postInitClient() {
        if(!LOADED_HANDLERS.isEmpty()) Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((skin, render) -> render.addLayer(new LayerBaublesArmor(render)));
    }

    @Nonnull
    public static Optional<CompatHandler> findFirst(@Nonnull final ItemStack stack) {
        if(!(stack.getItem() instanceof IAkashicGoggles) || stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType != EntityEquipmentSlot.HEAD) return Optional.empty();

        @Nullable final ResourceLocation id = stack.getItem().getRegistryName();
        return id != null ? LOADED_HANDLERS.stream().filter(cl -> cl.modid.equals(id.getNamespace())).findFirst() : Optional.empty();
    }

    // Useful for lambda expressions.
    public static boolean test(@Nonnull final ItemStack stack) { return findFirst(stack).isPresent(); }

    // ------
    // Events
    // ------

    @SubscribeEvent
    static void attachBaublesCapability(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
        findFirst(event.getObject()).ifPresent(cl -> event.addCapability(CAPABILITY_ID, new ICapabilityProvider() {
            @Nullable
            @Override
            public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
                @Nonnull final AkashicGogglesConfig.BaubleTypeAdapter adapter = cl.bauble.get();
                return hasCapability(capability, facing) ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(adapter) : null;
            }

            @Override
            public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE && cl.bauble.get() != AkashicGogglesConfig.BaubleTypeAdapter.NONE;
            }
        }));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void renderBaublesOverlays(@Nonnull final RenderGameOverlayEvent.Post event) {
        @Nonnull final Minecraft mc = Minecraft.getMinecraft();
        if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET && mc.gameSettings.thirdPersonView == 0) {
            @Nonnull final List<ItemStack> stacksForRender = new ArrayList<>();

            // Collect distinct items, to not render the same overlay multiple times.
            @Nonnull final List<ItemStack> headStacks = splitRenderCandidates(mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
            @Nonnull final IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(mc.player);
            IntStream.range(0, baubles.getSlots()).mapToObj(baubles::getStackInSlot).flatMap(stack -> splitRenderCandidates(stack).stream()).forEach(stack -> {
                if(!Iterables.any(Iterables.concat(headStacks, stacksForRender), stack.getHasSubtypes() ? stack::isItemEqual : stack::isItemEqualIgnoreDurability)) stacksForRender.add(stack);
            });

            // Render distinct item overlays.
            stacksForRender.forEach(stack -> {
                GlStateManager.color(1, 1, 1, 1);
                stack.getItem().renderHelmetOverlay(stack, mc.player, event.getResolution(), event.getPartialTicks());
            });
        }
    }

    @Nonnull
    private static List<ItemStack> splitRenderCandidates(@Nonnull final ItemStack goggles) {
        @Nonnull final List<ItemStack> stacks = AkashicGogglesUtil.getContainedStacks(goggles).collect(Collectors.toList());
        if(test(goggles)) stacks.add(goggles);
        return stacks;
    }
}
