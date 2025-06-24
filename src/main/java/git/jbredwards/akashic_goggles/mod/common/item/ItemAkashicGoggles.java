package git.jbredwards.akashic_goggles.mod.common.item;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.mod.common.container.InventoryAkashicGoggles;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@Optional.Interface(modid = "baubles", iface = "")
public class ItemAkashicGoggles extends ItemArmor implements IBauble
{
    public ItemAkashicGoggles(@Nonnull final ArmorMaterial material) {
        super(material, 0, EntityEquipmentSlot.HEAD);
        setMaxDamage(0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHelmetOverlay(@Nonnull final ItemStack goggles, @Nonnull final EntityPlayer player, @Nonnull final ScaledResolution resolution, final float partialTicks) {
        AkashicGogglesUtil.getContainedGoggles(goggles).forEach(stack -> stack.getItem().renderHelmetOverlay(stack, player, resolution, partialTicks));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return new ICapabilityProvider() {
            @Nonnull final InventoryAkashicGoggles inventory = new InventoryAkashicGoggles();
            @Override
            public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
                return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
                return hasCapability(capability, facing) ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : null;
            }
        };
    }

    // -------------------
    // Baubles Integration
    // -------------------

    @Nonnull
    @Optional.Method(modid = "baubles")
    @Override
    public BaubleType getBaubleType(@Nonnull final ItemStack goggles) {
        return BaubleType.TRINKET; // TODO
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onEquipped(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        AkashicGogglesUtil.getContainedGoggles(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .forEach(entry -> entry.getRight().onEquipped(entry.getLeft(), wearer));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onUnequipped(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        AkashicGogglesUtil.getContainedGoggles(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .forEach(entry -> entry.getRight().onUnequipped(entry.getLeft(), wearer));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onWornTick(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        AkashicGogglesUtil.getContainedGoggles(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .forEach(entry -> entry.getRight().onWornTick(entry.getLeft(), wearer));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public boolean willAutoSync(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        return AkashicGogglesUtil.getContainedGoggles(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .anyMatch(entry -> entry.getRight().willAutoSync(entry.getLeft(), wearer));
    }
}
