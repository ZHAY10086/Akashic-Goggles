/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.akashic_goggles.mod.common;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.render.IRenderBauble;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import git.jbredwards.akashic_goggles.Tags;
import git.jbredwards.akashic_goggles.api.AkashicGogglesUtil;
import git.jbredwards.akashic_goggles.api.IAkashicGoggles;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import vazkii.arl.item.ItemMod;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.arl.util.TooltipHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 *
 * @author jbred
 *
 */
@Optional.InterfaceList({
@Optional.Interface(modid = "baubles", iface = "baubles.api.render.IRenderBauble"),
@Optional.Interface(modid = "thaumcraft", iface = "thaumcraft.api.items.IGoggles"),
@Optional.Interface(modid = "thaumcraft", iface = "thaumcraft.api.items.IRevealer")})
public class ItemAkashicGoggles extends ItemMod implements IRenderBauble, IGoggles, IRevealer
{
    public ItemAkashicGoggles() {
        super("goggles");
        setCreativeTab(AkashicGoggles.TAB).setMaxStackSize(1);
    }

    @Override
    public void onArmorTick(@Nonnull final World world, @Nonnull final EntityPlayer player, @Nonnull final ItemStack goggles) {
        AkashicGogglesUtil.getContainedStacks(goggles).forEach(stack -> {
            @Nullable final IAkashicGoggles equipable = IAkashicGoggles.get(stack);
            if(equipable != null) equipable.onAkashicTick(player, goggles, stack);
        });
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHelmetOverlay(@Nonnull final ItemStack goggles, @Nonnull final EntityPlayer player, @Nonnull final ScaledResolution resolution, final float partialTicks) {
        AkashicGogglesUtil.getContainedStacks(goggles).forEach(stack -> {
            GlStateManager.color(1, 1, 1, 1);
            stack.getItem().renderHelmetOverlay(stack, player, resolution, partialTicks);
        });
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull final ItemStack goggles, @Nullable final World worldIn, @Nonnull final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        TooltipHandler.tooltipIfShift(tooltip, () -> TooltipHandler.addToTooltip(tooltip, getTranslationKey() + ".tooltip"));
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull final ItemStack goggles) {
        return super.getTranslationKey(goggles) + (InventoryAkashicGoggles.isMutable(goggles) ? "" : ".locked");
    }

    @Nonnull
    @Override
    public IRarity getForgeRarity(@Nonnull final ItemStack goggles) {
        return InventoryAkashicGoggles.isMutable(goggles) ? super.getForgeRarity(goggles) : new IRarity() {
            @Nonnull
            @Override
            public TextFormatting getColor() { return TextFormatting.RED; }

            @Nonnull
            @Override
            public String getName() { return "locked"; }
        };
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull final World worldIn, @Nonnull final EntityPlayer playerIn, @Nonnull final EnumHand handIn) {
        @Nonnull final ItemStack goggles = playerIn.getHeldItem(handIn);
        @Nonnull final NBTTagList inventory = ItemNBTHelper.getList(goggles, InventoryAkashicGoggles.NBT_INVENTORY, Constants.NBT.TAG_COMPOUND, false);
        if(!inventory.isEmpty() && InventoryAkashicGoggles.isMutable(goggles)) {
            final int inventoryLocked = ItemNBTHelper.getInt(goggles, InventoryAkashicGoggles.NBT_INVENTORY_LOCKED, 0);
            if(inventoryLocked < inventory.tagCount()) {
                if(!worldIn.isRemote) {
                    if(!playerIn.isSneaking()) ItemHandlerHelper.giveItemToPlayer(playerIn, new ItemStack((NBTTagCompound)inventory.removeTag(inventory.tagCount() - 1)));
                    else { // Allow players to remove all items inside the Akashic Goggles at once.
                        for(int slot = inventory.tagCount() - 1; slot >= inventoryLocked; slot--) ItemHandlerHelper.giveItemToPlayer(playerIn,
                                new ItemStack(inventoryLocked == 0 ? inventory.getCompoundTagAt(slot) : (NBTTagCompound)inventory.removeTag(slot)));
                        if(inventoryLocked == 0) ItemNBTHelper.getNBT(goggles).removeTag(InventoryAkashicGoggles.NBT_INVENTORY);
                    }
                }

                playerIn.playSound(AkashicGoggles.ITEM_GOGGLES_EMPTY, 1, 1);
                return ActionResult.newResult(EnumActionResult.SUCCESS, goggles);
            }
        }

        return ActionResult.newResult(EnumActionResult.PASS, goggles);
    }

    @Override
    public void onCreated(@Nonnull final ItemStack goggles, @Nonnull final World worldIn, @Nonnull final EntityPlayer playerIn) {
        InventoryAkashicGoggles.setValid(goggles);
    }

    @Override
    public void onUpdate(@Nonnull final ItemStack goggles, @Nonnull final World worldIn, @Nonnull final Entity entityIn, final int itemSlot, final boolean isSelected) {
        InventoryAkashicGoggles.setValid(goggles);
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

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull final ItemStack goggles, @Nullable final NBTTagCompound nbt) {
        return !AkashicGoggles.HAS_BAUBLES ? new InventoryAkashicGoggles(goggles) : new InventoryAkashicGoggles(goggles) {
            @Override
            public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE && AkashicGogglesConfig.Goggles.baubleType != AkashicGogglesConfig.BaubleTypeAdapter.NONE || super.hasCapability(capability, facing);
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE && AkashicGogglesConfig.Goggles.baubleType != AkashicGogglesConfig.BaubleTypeAdapter.NONE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(new IBauble() {
                    @Nonnull
                    @Override
                    public BaubleType getBaubleType(@Nonnull final ItemStack goggles) {
                        return AkashicGogglesConfig.Goggles.baubleType.getBaubleType(goggles);
                    }

                    @Override
                    public void onEquipped(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
                        AkashicGogglesUtil.getContainedStacks(goggles)
                                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                                .forEach(entry -> entry.getRight().onEquipped(entry.getLeft(), wearer));
                    }

                    @Override
                    public void onUnequipped(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
                        AkashicGogglesUtil.getContainedStacks(goggles)
                                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                                .forEach(entry -> entry.getRight().onUnequipped(entry.getLeft(), wearer));
                    }

                    @Override
                    public boolean willAutoSync(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
                        return AkashicGogglesUtil.getContainedStacks(goggles)
                                .filter(stack -> stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
                                .map(stack -> Pair.of(stack, stack.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null)))
                                .anyMatch(entry -> entry.getRight().willAutoSync(entry.getLeft(), wearer));
                    }

                    @Override
                    public void onWornTick(@Nonnull final ItemStack goggles, @Nonnull final EntityLivingBase wearer) {
                        AkashicGogglesUtil.getContainedStacks(goggles).forEach(stack -> {
                            @Nullable final IAkashicGoggles equipable = IAkashicGoggles.get(stack);
                            if(equipable != null) equipable.onAkashicTick(wearer, goggles, stack);
                        });
                    }
                }) : super.getCapability(capability, facing);
            }
        };
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

    // -------
    // Utility
    // -------

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull final EntityEquipmentSlot slot, @Nonnull final ItemStack goggles) {
        @Nonnull final Multimap<String, AttributeModifier> modifiers = MultimapBuilder.hashKeys().arrayListValues().build();
        if(AkashicGogglesConfig.Goggles.armorAttributes) AkashicGogglesUtil.getContainedStacks(goggles).forEach(stack -> modifiers.putAll(stack.getAttributeModifiers(slot)));
        return mergeDuplicateAttributeModifiers(modifiers);
    }

    @Nonnull
    protected static Multimap<String, AttributeModifier> mergeDuplicateAttributeModifiers(@Nonnull final Multimap<String, AttributeModifier> modifiers) {
        @Nonnull final Multimap<String, AttributeModifier> modifiersMerged = HashMultimap.create();
        modifiers.asMap().forEach((key, attributes) -> {
            @Nonnull final Map<UUID, AttributeModifier[]> mergedMap = new HashMap<>();
            attributes.forEach(attribute -> {
                @Nonnull final AttributeModifier[] values = mergedMap.computeIfAbsent(attribute.getID(), id -> new AttributeModifier[3]);
                if(values[attribute.getOperation()] == null) values[attribute.getOperation()] = attribute;
                else {
                    @Nonnull final AttributeModifier merged;
                    switch(attribute.getOperation()) {
                        case Constants.AttributeModifierOperation.ADD:
                        case Constants.AttributeModifierOperation.ADD_MULTIPLE:
                            merged = new AttributeModifier(attribute.getID(), attribute.getName(),
                            attribute.getAmount() + values[attribute.getOperation()].getAmount(),
                            attribute.getOperation());
                            break;
                        default:
                            merged = new AttributeModifier(attribute.getID(), attribute.getName(),
                            (attribute.getAmount() + 1) * values[attribute.getOperation()].getAmount(),
                            attribute.getOperation());
                    }

                    values[attribute.getOperation()] = merged;
                }
            });

            mergedMap.values().forEach(values -> {
                for(@Nullable final AttributeModifier value : values) {
                    if(value != null) modifiersMerged.put(key, value);
                }
            });
        });

        return modifiersMerged;
    }
}
