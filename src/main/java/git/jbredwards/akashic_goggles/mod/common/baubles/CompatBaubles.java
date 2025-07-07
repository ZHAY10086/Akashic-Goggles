package git.jbredwards.akashic_goggles.mod.common.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import git.jbredwards.akashic_goggles.Tags;
import jds.bibliocraft.items.ItemReadingGlasses;
import mods.railcraft.common.items.ItemGoggles;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import teamroots.embers.item.ItemAshenCloak;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author jbred
 *
 */
public enum CompatBaubles
{
    BIBLIOCRAFT("bibliocraft", stack -> BaubleType.HEAD, stack -> stack.getItem() instanceof ItemReadingGlasses),
    EMBERS("embers", stack -> BaubleType.HEAD, stack -> stack.getItem() instanceof ItemAshenCloak && ((ItemAshenCloak)stack.getItem()).armorType == EntityEquipmentSlot.HEAD),
    RAILCRAFT("railcraft", stack -> BaubleType.HEAD, stack -> stack.getItem() instanceof ItemGoggles);

    @Nonnull public final Predicate<ItemStack> condition;
    @Nonnull public final IBauble bauble;
    @Nonnull public final String modid;

    @Nonnull static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Tags.MOD_ID, "baubles_cap");
    @Nonnull static final List<CompatBaubles> LOADED_HANDLERS = new ArrayList<>();

    CompatBaubles(@Nonnull final String modidIn, @Nonnull final IBauble baubleIn, @Nonnull final Predicate<ItemStack> conditionIn) {
        condition = conditionIn;
        bauble = baubleIn;
        modid = modidIn;
    }

    @SubscribeEvent
    public void attachBaublesCapability(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
        if(condition.test(event.getObject())) event.addCapability(CAPABILITY_ID, new ICapabilityProvider() {
            @Override
            public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(bauble) : null;
            }
        });
    }

    public static void preInit() {
        LOADED_HANDLERS.addAll(Arrays.asList(Arrays.stream(values()).filter(ch -> Loader.isModLoaded(ch.modid)).toArray(CompatBaubles[]::new)));
        LOADED_HANDLERS.forEach(MinecraftForge.EVENT_BUS::register);
    }

    @SideOnly(Side.CLIENT)
    public static void postInitClient() {
        if(!LOADED_HANDLERS.isEmpty()) Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((skin, render) -> render.addLayer(new LayerBaublesArmor(render)));
    }
}
