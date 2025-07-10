package git.jbredwards.akashic_goggles.mod.common.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import com.google.common.collect.Sets;
import de.ellpeck.actuallyadditions.mod.items.ItemEngineerGoggles;
import erebus.items.ItemCompoundGoggles;
import git.jbredwards.akashic_goggles.Tags;
import jds.bibliocraft.items.ItemReadingGlasses;
import micdoodle8.mods.galacticraft.core.items.ItemSensorGlasses;
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
import openblocks.common.item.ItemImaginationGlasses;
import openblocks.common.item.ItemSonicGlasses;
import teamroots.embers.item.ItemAshenCloak;
import tonius.simplyjetpacks.item.ItemPilotGoggles;

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
public enum CompatHandler implements Predicate<ItemStack>
{
    ACTUALLYADDITIONS("actuallyadditions", "HEAD", stack -> stack.getItem() instanceof ItemEngineerGoggles),
    BIBLIOCRAFT("bibliocraft", "HEAD", stack -> stack.getItem() instanceof ItemReadingGlasses),
    EMBERS("embers", "HEAD", stack -> stack.getItem() instanceof ItemAshenCloak && ((ItemAshenCloak)stack.getItem()).armorType == EntityEquipmentSlot.HEAD),
    EREBUS("erebus", "HEAD", stack -> stack.getItem() instanceof ItemCompoundGoggles),
    GALACTICRAFT("galacticraft", "HEAD", stack -> stack.getItem() instanceof ItemSensorGlasses),
    OPENBLOCKS("openblocks", "HEAD", stack -> stack.getItem() instanceof ItemImaginationGlasses || stack.getItem() instanceof ItemSonicGlasses),
    RAILCRAFT("railcraft", "HEAD", stack -> stack.getItem() instanceof ItemGoggles),
    SIMPLYJETPACKS("simplyjetpacks", "HEAD", stack -> stack.getItem() instanceof ItemPilotGoggles);

    @Nonnull Predicate<ItemStack> condition;
    @Nonnull public final String bauble;
    @Nonnull public final String modid;

    @Nonnull static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Tags.MOD_ID, "baubles_cap");
    @Nonnull static final List<CompatHandler> LOADED_HANDLERS = new ArrayList<>();

    CompatHandler(@Nonnull final String modidIn, @Nonnull final String baubleIn, @Nonnull final Predicate<ItemStack> conditionIn) {
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
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(getBauble()) : null;
            }
        });
    }

    public static void preInit() {
        LOADED_HANDLERS.addAll(Arrays.asList(Arrays.stream(values()).filter(ch -> Loader.isModLoaded(ch.modid)).toArray(CompatHandler[]::new)));
        Sets.difference(Sets.newHashSet(values()), Sets.newHashSet(LOADED_HANDLERS)).forEach(ct -> ct.condition = stack -> false);
        LOADED_HANDLERS.forEach(MinecraftForge.EVENT_BUS::register);
    }

    @SideOnly(Side.CLIENT)
    public static void postInitClient() {
        if(!LOADED_HANDLERS.isEmpty()) Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((skin, render) -> render.addLayer(new LayerBaublesArmor(render)));
    }

    @Nonnull
    public IBauble getBauble() {
        @Nonnull final BaubleType type = Enum.valueOf(BaubleType.class, bauble);
        return stack -> type;
    }

    @Override
    public boolean test(@Nonnull final ItemStack stack) { return condition.test(stack); }
}
