package git.jbredwards.akashic_goggles.mod.common;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.render.IRenderBauble;
import com.google.common.collect.Multimap;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.mod.AkashicGoggles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import vazkii.arl.interf.IDropInItem;
import vazkii.arl.item.ItemMod;
import vazkii.arl.util.AbstractDropIn;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.arl.util.TooltipHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
@Optional.InterfaceList({
@Optional.Interface(modid = "baubles", iface = "baubles.api.IBauble"),
@Optional.Interface(modid = "baubles", iface = "baubles.api.render.IRenderBauble"),
@Optional.Interface(modid = "thaumcraft", iface = "thaumcraft.api.items.IGoggles"),
@Optional.Interface(modid = "thaumcraft", iface = "thaumcraft.api.items.IRevealer")})
public class ItemAkashicGoggles extends ItemMod implements IBauble, IRenderBauble, IGoggles, IRevealer
{
    @Nonnull
    public static final String NBT_KEY_INV = Tags.MOD_ID + ":inventory", NBT_KEY_VALID = Tags.MOD_ID + ":is_valid";
    public ItemAkashicGoggles() {
        super("goggles");
        setCreativeTab(AkashicGoggles.TAB).setMaxStackSize(1);
        AkashicGogglesUtil.registerSupportedGoggles(new ItemStack(Items.DIAMOND_SWORD));
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull final EntityEquipmentSlot slot, @Nonnull final ItemStack goggles) {
        @Nonnull final Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, goggles);
        AkashicGogglesUtil.getContainedStacks(goggles).forEach(stack -> modifiers.putAll(stack.getItem().getAttributeModifiers(slot, stack)));
        return modifiers;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHelmetOverlay(@Nonnull final ItemStack goggles, @Nonnull final EntityPlayer player, @Nonnull final ScaledResolution resolution, final float partialTicks) {
        AkashicGogglesUtil.getContainedStacks(goggles).forEach(stack -> stack.getItem().renderHelmetOverlay(stack, player, resolution, partialTicks));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull final ItemStack goggles, @Nullable final World worldIn, @Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        TooltipHandler.tooltipIfShift(tooltip, () -> TooltipHandler.addToTooltip(tooltip, getTranslationKey(goggles) + ".tooltip"));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull final World worldIn, @Nonnull final EntityPlayer playerIn, @Nonnull final EnumHand handIn) {
        @Nonnull final ItemStack held = playerIn.getHeldItem(handIn);
        @Nullable final IItemHandler inventory = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        boolean emptied = false;
        if(inventory != null) for(int slot = inventory.getSlots() - 1; slot >= 0; slot--) {
            @Nullable final EntityItem tossed = playerIn.dropItem(inventory.extractItem(slot, inventory.getSlotLimit(slot), worldIn.isRemote), false, false);
            if(tossed != null) {
                emptied = true;
                if(!worldIn.isRemote) worldIn.spawnEntity(tossed);
                if(!playerIn.isSneaking()) break;
            }
        }

        if(emptied) {
            playerIn.swingArm(handIn);
            playerIn.playSound(AkashicGoggles.ITEM_GOGGLES_EMPTY, 1, 1);
        }

        return ActionResult.newResult(emptied ? EnumActionResult.SUCCESS : EnumActionResult.PASS, held);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull final ItemStack goggles, @Nullable final NBTTagCompound nbt) {
        return new AbstractDropIn() {
            @Override
            public boolean canDropItemIn(@Nonnull final EntityPlayer player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) {
                final int total = stack.getCount(), remaining = ItemHandlerHelper.insertItem(getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack, true).getCount();
                return total > remaining;
            }

            @Nonnull
            @Override
            public ItemStack dropItemIn(@Nonnull final EntityPlayer player, @Nonnull final ItemStack goggles, @Nonnull final ItemStack stack) {
                ItemHandlerHelper.insertItem(getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack, false);
                return goggles;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
                return capability == IDropInItem.DROP_IN_CAPABILITY && isValid(goggles) ? super.getCapability(capability, null)
                        : capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new InventoryAkashicGoggles(goggles)) : null;
            }

            @Override
            public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
                return super.hasCapability(capability, null) && isValid(goggles) || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
            }
        };
    }

    // Let's prevent people from inserting items into JEI akashic goggles...
    public static boolean isValid(@Nonnull final ItemStack goggles) { return ItemNBTHelper.getBoolean(goggles, NBT_KEY_VALID, false); }
    public static void setValid(@Nonnull final ItemStack goggles) { ItemNBTHelper.setBoolean(goggles, NBT_KEY_VALID, true); }

    @Override
    public void onCreated(@Nonnull final ItemStack goggles, @Nonnull final World worldIn, @Nonnull final EntityPlayer playerIn) {
        setValid(goggles);
    }

    @Override
    public void onUpdate(@Nonnull final ItemStack goggles, @Nonnull final World worldIn, @Nonnull final Entity entityIn, final int itemSlot, final boolean isSelected) {
        setValid(goggles);
    }

    @Nonnull
    @Override
    public String getModNamespace() { return Tags.MOD_ID; }

    @Nonnull
    @Override
    public String getTranslationKey() { return getTranslationKey(ItemStack.EMPTY); }

    @Nonnull
    @Override
    public EnumRarity getRarity(@Nonnull final ItemStack stack) { return EnumRarity.UNCOMMON; }

    @Nullable
    @Override
    public EntityEquipmentSlot getEquipmentSlot(@Nonnull final ItemStack stack) { return EntityEquipmentSlot.HEAD; }

    // -------------------
    // Baubles Integration
    // -------------------

    @Optional.Method(modid = "baubles")
    @Override
    public void onEquipped(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        AkashicGogglesUtil.getContainedStacks(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .forEach(entry -> entry.getRight().onEquipped(entry.getLeft(), wearer));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onUnequipped(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        AkashicGogglesUtil.getContainedStacks(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .forEach(entry -> entry.getRight().onUnequipped(entry.getLeft(), wearer));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onWornTick(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        AkashicGogglesUtil.getContainedStacks(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .forEach(entry -> entry.getRight().onWornTick(entry.getLeft(), wearer));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public boolean willAutoSync(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
        return AkashicGogglesUtil.getContainedStacks(goggles)
                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                .anyMatch(entry -> entry.getRight().willAutoSync(entry.getLeft(), wearer));
    }

    @Optional.Method(modid = "baubles")
    @Override
    public void onPlayerBaubleRender(@Nonnull final ItemStack goggles, @Nonnull final EntityPlayer player, @Nonnull final RenderType renderType, final float partialTicks) {
        if(renderType == RenderType.BODY && player instanceof AbstractClientPlayer) {
            if(player.isSneaking()) GlStateManager.translate(0, 0.2, 0);
            Objects.requireNonNull((RenderPlayer)Minecraft.getMinecraft().getRenderManager().<AbstractClientPlayer>getEntityRenderObject(player)).getMainModel().bipedHead.postRender(0.0625f);
            GlStateManager.translate(0, -0.25, 0);

            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.scale(0.625, -0.625, -0.625);
            Minecraft.getMinecraft().getItemRenderer().renderItem(player, goggles, ItemCameraTransforms.TransformType.HEAD);
        }
    }

    @Nonnull
    @Optional.Method(modid = "baubles")
    @Override
    public BaubleType getBaubleType(@Nonnull final ItemStack goggles) {
        return BaubleType.TRINKET; // TODO
    }

    // ----------------------
    // Thaumcraft Integration
    // ----------------------

    @Optional.Method(modid = "thaumcraft")
    @Override
    public boolean showIngamePopups(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase player) {
        return player.getHeldItemMainhand() != goggles && player.getHeldItemOffhand() != goggles && AkashicGogglesUtil.getContainedStacks(goggles)
                .anyMatch(stack -> stack.getItem() instanceof IGoggles && ((IGoggles)stack.getItem()).showIngamePopups(stack, player));
    }

    @Optional.Method(modid = "thaumcraft")
    @Override
    public boolean showNodes(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase player) {
        return player.getHeldItemMainhand() != goggles && player.getHeldItemOffhand() != goggles && AkashicGogglesUtil.getContainedStacks(goggles)
                .anyMatch(stack -> stack.getItem() instanceof IRevealer && ((IRevealer)stack.getItem()).showNodes(stack, player));
    }
}
